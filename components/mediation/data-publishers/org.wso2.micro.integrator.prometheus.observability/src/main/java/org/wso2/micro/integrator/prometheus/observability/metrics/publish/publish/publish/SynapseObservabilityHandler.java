/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.prometheus.observability.metrics.publish.publish.publish;

import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.rest.API;

import org.wso2.micro.integrator.prometheus.observability.metrics.publish.publish.util.PrometheusMetricsConstants;

/**
 * Class for instrumenting Prometheus Metrics.
 */

public class SynapseObservabilityHandler extends AbstractExtendedSynapseHandler {

    private static final String DELIMITER = "/";
    private static final String EMPTY = "";

    @Override
    public boolean handleErrorResponse(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        if ((synCtx.getProperty(PrometheusMetricsConstants.HAS_EXECUTED_ERROR_FLOW) == null)) {
            if (null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE)) {

                String name = synCtx.getProperty(PrometheusMetricsConstants.PROXY_NAME).toString();
                String host = axis2MessageContext.getProperty(PrometheusMetricsConstants.REMOTE_HOST).toString();

                PrometheusMetrics.ERROR_REQUESTS_RECEIVED_PROXY_SERVICE.labels(name,
                                                                   PrometheusMetricsConstants.PROXY_NAME, host).inc();
                Histogram.Timer proxyLatency = (Histogram.Timer) synCtx.getProperty
                                                                    (PrometheusMetricsConstants.PROXY_LATENCY_TIMER);
                proxyLatency.observeDuration();

            } else if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND)) {
                String inboundEndpointName = synCtx.getProperty(PrometheusMetricsConstants.INBOUND_ENDPOINT_NAME).
                                                                                                          toString();

                PrometheusMetrics.ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT.labels(inboundEndpointName,
                                                                  PrometheusMetricsConstants.INBOUND_ENDPOINT).inc();
                Histogram.Timer inboundEndpointLatency = (Histogram.Timer) synCtx.
                                               getProperty(PrometheusMetricsConstants.INBOUND_ENDPOINT_LATENCY_TIMER);
                inboundEndpointLatency.observeDuration();

            } else {
                String context = axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL).
                                                                                                   toString();
                String apiInvocationUrl = axis2MessageContext.getProperty(PrometheusMetricsConstants.SERVICE_PREFIX).
                        toString() + context.replaceFirst(DELIMITER, EMPTY);
                String apiName = getApiName(context,synCtx);
                String remoteAddress = (String) axis2MessageContext.getProperty
                                                               (org.apache.axis2.context.MessageContext.REMOTE_ADDR);

                PrometheusMetrics.ERROR_REQUESTS_RECEIVED_API.labels(apiName, PrometheusMetricsConstants.API,
                                                                               apiInvocationUrl, remoteAddress).inc();

                Histogram.Timer apiLatency = (Histogram.Timer) synCtx.getProperty
                                                                       (PrometheusMetricsConstants.API_LATENCY_TIMER);
                apiLatency.observeDuration();
            }
            synCtx.setProperty(PrometheusMetricsConstants.HAS_EXECUTED_ERROR_FLOW, true);
        }
        return true;
    }

    @Override
    public boolean handleRequestInFlow(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        String remoteAddr = (String) axis2MessageContext.getProperty(
                org.apache.axis2.context.MessageContext.REMOTE_ADDR);

        if (null == synCtx.getProperty(PrometheusMetricsConstants.REST_FULL_REQUEST_PATH)) {
            if (null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE)) {
                String proxyName = axis2MessageContext.getAxisService().getName();

                PrometheusMetrics.TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE.labels(proxyName,
                        PrometheusMetricsConstants.PROXY_SERVICE, remoteAddr).inc();
                synCtx.setProperty(PrometheusMetricsConstants.PROXY_LATENCY_TIMER,
                        PrometheusMetrics.PROXY_LATENCY_DURATION_HISTOGRAM.labels(proxyName).startTimer());

            } else if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND)) {
                String inboundEndpointName = synCtx.getProperty(SynapseConstants.INBOUND_ENDPOINT_NAME).toString();

                PrometheusMetrics.TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT.labels
                                       (inboundEndpointName, PrometheusMetricsConstants.INBOUND_ENDPOINT).inc();
                synCtx.setProperty(PrometheusMetricsConstants.INBOUND_ENDPOINT_LATENCY_TIMER,
                        PrometheusMetrics.INBOUND_ENDPOINT_LATENCY_HISTOGRAM.labels(inboundEndpointName).startTimer());

            } else {
                String context = axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL).
                                                                                                        toString();
                String apiInvocationUrl = axis2MessageContext.getProperty(PrometheusMetricsConstants.SERVICE_PREFIX).
                        toString() + context.replaceFirst(DELIMITER, EMPTY);
                String apiName = getApiName(context,synCtx);


                if (null != apiName) {
                    PrometheusMetrics.TOTAL_REQUESTS_RECEIVED_API.labels(apiName, PrometheusMetricsConstants.API,
                                                                            apiInvocationUrl,remoteAddr).inc();
                    synCtx.setProperty(PrometheusMetricsConstants.API_LATENCY_TIMER,
                             PrometheusMetrics.API_REQUEST_LATENCY_HISTOGRAM.labels(apiName,apiInvocationUrl).
                                                                                                       startTimer());
                }
            }
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext synCtx) {

        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext synCtx) {

        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext synCtx) {

        if (null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE)) {
            Histogram.Timer proxyLatency = (Histogram.Timer) synCtx.getProperty
                                                                   (PrometheusMetricsConstants.PROXY_LATENCY_TIMER);
            proxyLatency.observeDuration();
        } else if ((null != synCtx.getProperty(SynapseConstants.IS_INBOUND) && (!synCtx.getProperty
                              (PrometheusMetricsConstants.REST_FULL_REQUEST_PATH).
                                                        equals(PrometheusMetricsConstants.METRICS_ENDPOINT)))) {
            Histogram.Timer inboundEndpointLatency = (Histogram.Timer) synCtx.getProperty
                                              (PrometheusMetricsConstants.INBOUND_ENDPOINT_LATENCY_TIMER);
            inboundEndpointLatency.observeDuration();
        } else if (null != synCtx.getProperty(PrometheusMetricsConstants.SYNAPSE_REST_API) &&
                      (!synCtx.getProperty(PrometheusMetricsConstants.REST_FULL_REQUEST_PATH).
                                                          equals(PrometheusMetricsConstants.METRICS_ENDPOINT))) {
            Histogram.Timer apiLatency = (Histogram.Timer) synCtx.getProperty(
                                                                         PrometheusMetricsConstants.API_LATENCY_TIMER);
            apiLatency.observeDuration();
        }
        return true;
    }

    @Override
    public boolean init() {

        String host = System.getProperty(PrometheusMetricsConstants.CARBON_LOCAL_IP);
        String port = System.getProperty(PrometheusMetricsConstants.HTTP_PORT);
        String javaVersion = System.getProperty(PrometheusMetricsConstants.JAVA_VERSION);
        String javaHome = System.getProperty(PrometheusMetricsConstants.JAVA_HOME);
        String serverStartTime = System.getProperty(PrometheusMetricsConstants.SERVER_START_TIME);

        DefaultExports.initialize();
        PrometheusMetrics.SERVER_UP.labels(host, port, javaHome, javaVersion, serverStartTime).inc();

        return true;
    }

    @Override
    public boolean deployArtifacts(String artifactName, String artifactType, String startTime) {

        PrometheusMetrics.SERVICE_UP.labels(artifactName, artifactType, startTime).inc();

        return true;
    }

    private String getApiName(String contextPath, MessageContext synCtx) {

        String apiName = null;

        String apiContext = contextPath.substring(contextPath.indexOf(DELIMITER), contextPath.lastIndexOf(DELIMITER));
        for (API api : synCtx.getEnvironment().getSynapseConfiguration().getAPIs()) {
            if (apiContext.contains(api.getAPIName())) {
                apiContext = apiContext.substring(apiContext.lastIndexOf(DELIMITER));
            }

            if (api.getContext().equals(apiContext)) {
                apiName = api.getAPIName();
            }
        }
        return apiName;

    }
}

