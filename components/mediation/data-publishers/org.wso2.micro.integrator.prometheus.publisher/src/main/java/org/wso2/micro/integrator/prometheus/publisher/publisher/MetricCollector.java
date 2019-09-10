/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.prometheus.publisher.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.prometheus.publisher.model.PrometheusMetric;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Scrapes, collects and formats JMS metric data into Prometheus format
 */
class MetricCollector {

    private static final Log LOGGER = LogFactory.getLog(MetricCollector.class);

    private static final String SYNAPSE_DOMAIN_NAME = "org.apache.synapse:*";
    private static final String METRIC_TYPE_UNTYPED = "untyped";
    private static final String SEPARATOR = "_";
    private static final Pattern PROPERTY_PATTERN = Pattern
            .compile("([^,=:*?]+)" + // Name - non-empty, anything but comma, equals, colon, star, or question mark
                             "=" +  // Equals
                             "(" + // Either
                             "\"" + // Quoted
                             "(?:" + // A possibly empty sequence of
                             "[^\\\\\"]*" + // Greedily match anything but backslash or quote
                             "(?:\\\\.)?" + // Greedily see if we can match an escaped sequence
                             ")*" + "\"" + "|" + // Or
                             "[^,=:\"]*" + // Unquoted - can be empty, anything but comma, equals, colon, or quote
                             ")");

    /**
     * Collects metric data from JMX MBeans
     *
     * @return Metric data in Prometheus format
     */
    List<String> collect() {

        List<PrometheusMetric> metricList = new ArrayList<>();
        Map<ObjectName, LinkedHashMap<String, String>> keyPropertiesPerBean = new ConcurrentHashMap<>();

        try {

            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

            Set<ObjectName> mBeanNames = new HashSet<>();

            // get filtered MBean names for Synapse domain
            ObjectName synapseFilterName = new ObjectName(SYNAPSE_DOMAIN_NAME);
            for (ObjectInstance instance : beanServer.queryMBeans(synapseFilterName, null)) {
                mBeanNames.add(instance.getObjectName());
            }

            // scrape each MBean
            for (ObjectName beanName : mBeanNames) {
                List<PrometheusMetric> metrics = scrapeBean(beanServer, beanName, keyPropertiesPerBean);
                if (metrics != null && !metrics.isEmpty()) {
                    metricList.addAll(metrics);
                }
            }

            // sort metrics so that related metrics are grouped together
            if (!metricList.isEmpty()) {
                Collections.sort(metricList, new Comparator<PrometheusMetric>() {
                    @Override
                    public int compare(final PrometheusMetric object1, final PrometheusMetric object2) {

                        return object1.getMetricName().compareTo(object2.getMetricName());
                    }
                });

                // remove help and type tags for metrics with same name
                for (int i = 1; i < metricList.size(); i++) {
                    if (metricList.get(i).getMetricName().equals(metricList.get(i - 1).getMetricName())) {
                        metricList.get(i).setHelp("");
                        metricList.get(i).setType("");
                    }
                }

                // convert metric data into Prometheus format
                List<String> metrics = new ArrayList<>();
                for (PrometheusMetric metric : metricList) {
                    metrics.add(PrometheusMetric.formatMetric(metric));
                }

                return metrics;
            }

        } catch (MalformedObjectNameException e) {
            LOGGER.error("Error in retrieving metric data from JMX MBeans: ", e);
        }
        return Collections.emptyList();
    }

    private List<PrometheusMetric> scrapeBean(MBeanServer beanServer, ObjectName beanName,
                                              Map<ObjectName, LinkedHashMap<String, String>> keyPropertiesPerBean) {

        try {
            // get attributes of each bean
            MBeanInfo beanInfo = beanServer.getMBeanInfo(beanName);
            MBeanAttributeInfo[] beanAttributes = beanInfo.getAttributes();
            Map<String, MBeanAttributeInfo> attributeMap = new HashMap<>();
            for (MBeanAttributeInfo attribute : beanAttributes) {
                if (attribute.isReadable()) {
                    attributeMap.put(attribute.getName(), attribute);
                }
            }
            AttributeList attributes = beanServer.getAttributes(beanName, attributeMap.keySet().toArray(new String[0]));

            List<PrometheusMetric> metrics = new ArrayList<>();

            // process bean attributes
            for (Attribute attribute : attributes.asList()) {
                MBeanAttributeInfo attributeInfo = attributeMap.get(attribute.getName());
                PrometheusMetric metric = processBeanValue(toSnakeAndLowerCase(beanName.getDomain()),
                                                           toSnakeAndLowerCase(attributeInfo.getName()),
                                                           attributeInfo.getDescription(), attribute.getValue(),
                                                           getKeyPropertyList(keyPropertiesPerBean, beanName));
                if (metric != null) {
                    metrics.add(metric);
                }
            }
            return metrics;
        } catch (ReflectionException | IntrospectionException | InstanceNotFoundException e) {
            LOGGER.error("Error in scraping bean - " + beanName.toString(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Convert metric name into Prometheus format
     *
     * @param attrName Metric name
     * @return Formatted name
     */
    private String toSnakeAndLowerCase(String attrName) {

        if (attrName == null || attrName.isEmpty()) {
            return attrName;
        }
        char firstChar = attrName.subSequence(0, 1).charAt(0);
        boolean prevCharIsUpperCaseOrUnderscore = Character.isUpperCase(firstChar) || firstChar == '_';
        StringBuilder resultBuilder = new StringBuilder(attrName.length()).append(Character.toLowerCase(firstChar));
        for (char attrChar : attrName.substring(1).toCharArray()) {
            boolean charIsUpperCase = Character.isUpperCase(attrChar);
            if (attrChar == '.' || attrChar == '-') {
                resultBuilder.append("_");
            } else {
                if (!prevCharIsUpperCaseOrUnderscore && charIsUpperCase) {
                    resultBuilder.append("_");
                }
                resultBuilder.append(Character.toLowerCase(attrChar));
            }
            prevCharIsUpperCaseOrUnderscore = charIsUpperCase || attrChar == '_';
        }
        return resultBuilder.toString();
    }

    private PrometheusMetric processBeanValue(String domain, String attributeName, String attributeDesc,
                                              Object attributeValue, LinkedHashMap<String, String> beanProperties) {

        String beanName = "MBean attribute " + domain + SEPARATOR + attributeName;
        if (attributeValue == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(beanName + " has value null");
            }
        } else if (attributeValue instanceof Number || attributeValue instanceof String
                || attributeValue instanceof Boolean) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Recording MBean attribute " + domain + SEPARATOR + attributeName);
            }
            return recordBean(domain, attributeName, attributeDesc, attributeValue, beanProperties);
        } else if (attributeValue.getClass().isArray()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(beanName + " value is not supported");
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(beanName + " is not exported");
            }
        }
        return null;
    }

    private PrometheusMetric recordBean(String domain, String attributeName, String attributeDesc,
                                        Object attributeValue, LinkedHashMap<String, String> beanProperties) {

        if (isDoubleParsable(attributeValue.toString())) {
            PrometheusMetric prometheusMetric = new PrometheusMetric();
            prometheusMetric
                    .setMetricName(toSnakeAndLowerCase(formatMetricName(domain, attributeName, beanProperties)));
            prometheusMetric.setType(METRIC_TYPE_UNTYPED);
            prometheusMetric.setHelp(attributeDesc);
            prometheusMetric.setMetricValue(Double.valueOf(attributeValue.toString()));

            StringBuilder propString = new StringBuilder();
            propString.append("{");
            int i = 0;
            for (Map.Entry entry : beanProperties.entrySet()) {
                propString.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                if (i < beanProperties.size() - 1) {
                    propString.append(", ");
                } else if (i == beanProperties.size() - 1) {
                    propString.append("}");
                }
                i++;
            }
            prometheusMetric.setPropertyString(propString.toString());
            return prometheusMetric;
        }
        return null;
    }

    private String formatMetricName(String domain, String attributeName, LinkedHashMap<String, String> beanProperties) {

        StringBuilder name = new StringBuilder();
        name.append(domain);
        if (beanProperties.size() > 0) {
            name.append(SEPARATOR);
            name.append(beanProperties.values().iterator().next());
        }
        name.append(SEPARATOR).append(attributeName);
        return name.toString();
    }

    private boolean isDoubleParsable(String value) {

        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex = ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                "[+-]?(" +         // Optional sign character
                "NaN|" +           // "NaN" string
                "Infinity|" +      // "Infinity" string

                // A decimal floating-point string representing a finite positive
                // number without a leading sign has at most five basic pieces:
                // Digits . Digits ExponentPart FloatTypeSuffix
                //
                // Since this method allows integer-only strings as input
                // in addition to strings of floating-point literals, the
                // two sub-patterns below are simplifications of the grammar
                // productions from the Java Language Specification, 2nd
                // edition, section 3.10.2.

                // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                // . Digits ExponentPart_opt FloatTypeSuffix_opt
                "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                // Hexadecimal strings
                "((" +
                // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "(\\.)?)|" +

                // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                ")[pP][+-]?" + Digits + "))" + "[fFdD]?))" +
                // Optional trailing "whitespace"
                "[\\x00-\\x20]*");

        return Pattern.matches(fpRegex, value);
    }

    /**
     * Returns the list of labels and their values of each metric, which are the key properties of mBeans
     *
     * @param mBeanName Bean name
     * @return Map of key properties and their values
     */
    private LinkedHashMap<String, String> getKeyPropertyList(
            Map<ObjectName, LinkedHashMap<String, String>> keyPropertiesPerBean, ObjectName mBeanName) {

        LinkedHashMap<String, String> keyProperties = keyPropertiesPerBean.get(mBeanName);
        if (keyProperties == null) {
            keyProperties = new LinkedHashMap<>();
            String properties = mBeanName.getKeyPropertyListString();
            Matcher match = PROPERTY_PATTERN.matcher(properties);
            while (match.lookingAt()) {
                keyProperties.put(match.group(1), match.group(2));
                properties = properties.substring(match.end());
                if (properties.startsWith(",")) {
                    properties = properties.substring(1);
                }
                match.reset(properties);
            }
            keyPropertiesPerBean.put(mBeanName, keyProperties);
        }
        return keyProperties;
    }
}
