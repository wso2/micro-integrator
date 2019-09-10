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
package org.wso2.micro.integrator.prometheus.publisher.model;

import org.wso2.micro.integrator.prometheus.publisher.util.PrometheusPublisherConstants;

/**
 * Model class for metrics in Prometheus format
 */
public class PrometheusMetric {

    private static final String DELIMITER = " ";
    private static final String NEW_LINE = "\n";
    private String metricName;
    private String help;
    private String type;
    private Double metricValue;
    private String propertyString;

    /**
     * Formats the metric data into Prometheus format
     *
     * @param metric Metric object
     * @return Formatted metric string
     */
    public static String formatMetric(PrometheusMetric metric) {

        if (metricDataExist(metric)) {
            return formatMetricHelp(metric) + formatMetricType(metric) + metric.getMetricName() + metric
                    .getPropertyString() + DELIMITER + metric.getMetricValue() + NEW_LINE;
        }
        return "";
    }

    private static boolean metricDataExist(PrometheusMetric metric) {

        return valuesExist(metric.getMetricName()) && metric.getMetricValue() != null;
    }

    private static boolean valuesExist(String value) {

        return value != null && !value.isEmpty();
    }

    private static String formatMetricHelp(PrometheusMetric metric) {

        if (metric.getHelp() != null && !metric.getHelp().isEmpty()) {
            return PrometheusPublisherConstants.PROMETHEUS_HELP_TAG + metric.getMetricName() + DELIMITER + metric
                    .getHelp() + NEW_LINE;
        }
        return "";
    }

    private static String formatMetricType(PrometheusMetric metric) {

        if (metric.getType() != null && !metric.getType().isEmpty()) {
            return PrometheusPublisherConstants.PROMETHEUS_TYPE_TAG + metric.getMetricName() + DELIMITER + metric
                    .getType() + NEW_LINE;
        }
        return "";
    }

    public String getMetricName() {

        return metricName;
    }

    public void setMetricName(String metricName) {

        this.metricName = metricName;
    }

    public String getHelp() {

        return help;
    }

    public void setHelp(String help) {

        this.help = help;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public Double getMetricValue() {

        return metricValue;
    }

    public void setMetricValue(Double metricValue) {

        this.metricValue = metricValue;
    }

    public String getPropertyString() {

        return propertyString;
    }

    public void setPropertyString(String propertyString) {

        this.propertyString = propertyString;
    }
}
