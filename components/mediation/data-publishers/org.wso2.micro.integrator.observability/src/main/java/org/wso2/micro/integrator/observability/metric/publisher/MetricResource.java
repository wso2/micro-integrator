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
package org.wso2.micro.integrator.observability.metric.publisher;

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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

public class MetricResource extends APIResource {

    private static Log log = LogFactory.getLog(MetricResource.class);
    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    private CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    public MetricResource(String urlTemplate) {
        super(urlTemplate);

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

        try {
            String formattedMetric = MetricFormatter.formatMetrics(registry.
                    filteredMetricFamilySamples(parseQuery(query)));
            textRootElem.setText(formattedMetric);
        } catch (IOException e) {
            log.error("Error in parsing metrics.", e);
        }

        synCtx.getEnvelope().getBody().addChild(textRootElem);

        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axisCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, "text/plain");
        axisCtx.setProperty(Constants.Configuration.CONTENT_TYPE, "text/plain");
        axisCtx.removeProperty(NO_ENTITY_BODY);

        return true;
    }

    /**
     * Allows you to add "?name[]=metric_name_to_filter_by" to the end of the /metrics URL,
     * in case you don't want the client library to return all metric names.
     * (This is the implementation of the parseQuery() method of the Prometheus HTTP Server
     * https://github.com/prometheus/client_java/blob/master/simpleclient_httpserver/src/main/java/io/prometheus/
     * client/exporter/HTTPServer.java#L110).
     *
     * @param parameter the list of Endpoint URL to be queried
     * @return the set of names used in filtering the required list of metrics
     */
    private Set<String> parseQuery(String parameter) throws IOException {

        Set<String> names = new HashSet<>();
        if (parameter != null) {
            String[] pairs = parameter.split("&");
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
