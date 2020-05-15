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
package org.wso2.micro.integrator.prometheus.publisher.service;

import io.prometheus.client.CollectorRegistry;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.micro.integrator.prometheus.publisher.publisher.MetricPublisher;
import org.wso2.micro.integrator.prometheus.publisher.util.MetricFormatter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resource class for endpoint exposing metric data
 */
public class MetricResource extends APIResource {

    private static Log log = LogFactory.getLog(MetricResource.class);
    private MetricPublisher metricPublisher;
    private MetricFormatter metricFormatter;
    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    private CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public MetricResource(String urlTemplate) {

        super(urlTemplate);
        metricPublisher = new MetricPublisher();
        metricFormatter = new MetricFormatter();
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add("GET");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext synCtx) {

        buildMessage(synCtx);
        synCtx.setProperty("Success", true);
        String query = ((Axis2MessageContext) synCtx).getAxis2MessageContext().getOptions().getTo().getAddress();

        OMElement textRootElem = OMAbstractFactory.getOMFactory().createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER);

        log.debug("Retrieving metric data to be published to Prometheus");

        List<String> metrics = metricPublisher.getMetrics();

        if (metrics != null && !metrics.isEmpty()) {
            log.debug("Retrieving metric data successful");
            try {
                StringBuilder sbr = metricFormatter.formatMetrics(registry.
                                                                     filteredMetricFamilySamples(parseQuery(query)));
                textRootElem.setText(sbr.toString());
            } catch (IOException e) {
                log.error("Error in parsing metrics.", e);
            }
        } else {
            textRootElem.setText("");
            log.info("No metrics retrieved to be published to Prometheus");
        }

        synCtx.getEnvelope().getBody().addChild(textRootElem);

        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axisCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, "text/plain");
        axisCtx.setProperty(Constants.Configuration.CONTENT_TYPE, "text/plain");
        axisCtx.removeProperty(NO_ENTITY_BODY);

        return true;
    }

    private Set<String> parseQuery(String query) throws IOException {

        Set<String> names = new HashSet<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return names;
    }
}
