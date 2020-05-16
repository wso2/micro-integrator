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

package org.wso2.micro.integrator.prometheus.observability.metrics.publish;

import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractExtendedSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;
import org.wso2.micro.integrator.prometheus.observability.metrics.util.PrometheusMetricsConstants;
import org.wso2.micro.integrator.prometheus.observability.metrics.util.PrometheusMetricsRegisterUtils;


/**
 * Class for instrumenting Prometheus Metrics.
 */
public class SynapseObservabilityHandler extends AbstractExtendedSynapseHandler {
    private static final String DELIMITER = "/";
    private static final String EMPTY = "";
    private static final String SPLIT = ":";
    private RESTUtils restUtils = new RESTUtils();
    private static Log log = LogFactory.getLog(SynapseObservabilityHandler.class);

    @Override
    public boolean handleRequestInFlow(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        // Get the Internal HTTP Inbound Endpoint port
        int portOffset = Integer.parseInt(System.getProperty(PrometheusMetricsConstants.PORT_OFFSET));
        int internalHttpApiPort = Integer.parseInt(synCtx.getEnvironment().getSynapseConfiguration().
                getProperty(PrometheusMetricsConstants.INTERNAL_HTTP_API_PORT));
        internalHttpApiPort = internalHttpApiPort + portOffset;

        if ((null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE))) {
            String proxyName = axis2MessageContext.getAxisService().getName();

            incrementProxyCount(proxyName);
            startTimers(synCtx, proxyName, SynapseConstants.PROXY_SERVICE_TYPE, null);

            synCtx.setProperty(PrometheusMetricsConstants.PROXY_LATENCY_TIMER,
                    PrometheusMetricsRegisterUtils.PROXY_LATENCY_HISTOGRAM.labels(proxyName).
                            startTimer());
        } else if (!axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL).toString().
                contains(PrometheusMetricsConstants.SERVICES)) {
            // Get the port used in service invoking
            String servicePort = ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                    getProperty(NhttpConstants.SERVICE_PREFIX).toString();
            servicePort = servicePort.substring((servicePort.lastIndexOf(SPLIT) + 1),
                    servicePort.lastIndexOf(DELIMITER));
            int serviceInvokePort = Integer.parseInt(servicePort);

            if ((null != synCtx.getProperty(SynapseConstants.IS_INBOUND) &&
                    (serviceInvokePort != internalHttpApiPort))) {
                String inboundEndpointName = synCtx.getProperty(SynapseConstants.INBOUND_ENDPOINT_NAME).toString();

                incrementInboundEndPointCount(inboundEndpointName);
                startTimers(synCtx, inboundEndpointName, PrometheusMetricsConstants.INBOUND_ENDPOINT,
                        null);

                synCtx.setProperty(PrometheusMetricsConstants.INBOUND_ENDPOINT_LATENCY_TIMER,
                        PrometheusMetricsRegisterUtils.INBOUND_ENDPOINT_LATENCY_HISTOGRAM.labels(inboundEndpointName).
                                startTimer());
            } else if ((serviceInvokePort != internalHttpApiPort)) {
                String context = axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL).
                        toString();
                String apiInvocationUrl = axis2MessageContext.getProperty(PrometheusMetricsConstants.SERVICE_PREFIX).
                        toString() + context.replaceFirst(DELIMITER, EMPTY);
                String apiName = getApiName(context, synCtx);

                if (apiName != null) {
                    incrementAPICount(apiName, apiInvocationUrl);
                    startTimers(synCtx, apiName, SynapseConstants.FAIL_SAFE_MODE_API, apiInvocationUrl);

                    synCtx.setProperty(PrometheusMetricsConstants.API_LATENCY_TIMER,
                            PrometheusMetricsRegisterUtils.API_REQUEST_LATENCY_HISTOGRAM.labels(apiName).
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
         org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        // Get the Internal HTTP Inbound Endpoint port
        int portOffset = Integer.parseInt(System.getProperty(PrometheusMetricsConstants.PORT_OFFSET));
        int internalHttpApiPort = Integer.parseInt(synCtx.getEnvironment().getSynapseConfiguration().
                getProperty(PrometheusMetricsConstants.INTERNAL_HTTP_API_PORT));
        internalHttpApiPort = internalHttpApiPort + portOffset;
        int serviceInvokePort = 0;

        if (null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE)) {
            stopTimers((Histogram.Timer) synCtx.getProperty(PrometheusMetricsConstants.PROXY_LATENCY_TIMER));
        } else if ((null != axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL)) &&
                (!axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL).toString().
                        contains(PrometheusMetricsConstants.SERVICES))) {
            // Get the port used in service invoking
            if (null != ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                    getProperty(NhttpConstants.SERVICE_PREFIX)) {
                String servicePort = ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                        getProperty(NhttpConstants.SERVICE_PREFIX).toString();
                servicePort = servicePort.substring((servicePort.lastIndexOf(':') + 1),
                        servicePort.lastIndexOf(DELIMITER));
                serviceInvokePort = Integer.parseInt(servicePort);
            }
            if ((null != synCtx.getProperty(SynapseConstants.IS_INBOUND) &&
                    (serviceInvokePort != internalHttpApiPort))) {
                stopTimers((Histogram.Timer) synCtx.getProperty
                        (PrometheusMetricsConstants.INBOUND_ENDPOINT_LATENCY_TIMER));
            } else if (null != synCtx.getProperty(PrometheusMetricsConstants.SYNAPSE_REST_API) &&
                    (serviceInvokePort != internalHttpApiPort)) {
                stopTimers((Histogram.Timer) synCtx.getProperty(PrometheusMetricsConstants.API_LATENCY_TIMER));
            }
        }
        return true;
    }

    @Override
    public boolean handleErrorResponse(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        if (axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL).toString().
                contains(PrometheusMetricsConstants.SERVICES)) {
            String name = synCtx.getProperty(SynapseConstants.PROXY_SERVICE).toString();
            if (axis2MessageContext.getProperty(PrometheusMetricsConstants.REMOTE_HOST) != null) {

                incrementProxyErrorCount(name);
                stopTimers((Histogram.Timer) synCtx.getProperty(PrometheusMetricsConstants.PROXY_LATENCY_TIMER));
            }
        } else if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND)) {
            String inboundEndpointName = synCtx.getProperty(PrometheusMetricsConstants.INBOUND_ENDPOINT_NAME).
                    toString();
            incrementInboundEndpointErrorCount(inboundEndpointName);
            stopTimers((Histogram.Timer) synCtx.getProperty(PrometheusMetricsConstants.
                    INBOUND_ENDPOINT_LATENCY_TIMER));
        } else {
            String context = axis2MessageContext.getProperty(PrometheusMetricsConstants.TRANSPORT_IN_URL).
                    toString();
            String apiInvocationUrl = axis2MessageContext.getProperty(PrometheusMetricsConstants.SERVICE_PREFIX).
                    toString() + context.replaceFirst(DELIMITER, EMPTY);
            String apiName = getApiName(context, synCtx);

            incrementAPIErrorCount(apiName, apiInvocationUrl);
            stopTimers((Histogram.Timer) synCtx.getProperty(PrometheusMetricsConstants.API_LATENCY_TIMER));
        }
        synCtx.setProperty(PrometheusMetricsConstants.IS_EXECUTED_ERROR_FLOW, true);
        return true;
    }

    @Override
    public boolean init() {
        String host = System.getProperty(MicroIntegratorBaseConstants.LOCAL_IP_ADDRESS);
        String port = System.getProperty(PrometheusMetricsConstants.HTTP_PORT);
        String javaVersion = System.getProperty(PrometheusMetricsConstants.JAVA_VERSION);
        String javaHome = System.getProperty(PrometheusMetricsConstants.JAVA_HOME);

        DefaultExports.initialize();
        PrometheusMetricsRegisterUtils.SERVER_UP.labels(host, port, javaHome, javaVersion).setToCurrentTime();
        return true;
    }

    @Override
    public boolean stopServer() {
        String host = System.getProperty(MicroIntegratorBaseConstants.LOCAL_IP_ADDRESS);
        String port = System.getProperty(PrometheusMetricsConstants.HTTP_PORT);
        String javaVersion = System.getProperty(PrometheusMetricsConstants.JAVA_VERSION);
        String javaHome = System.getProperty(PrometheusMetricsConstants.JAVA_HOME);

        DefaultExports.initialize();
        PrometheusMetricsRegisterUtils.SERVER_UP.labels(host, port, javaHome, javaVersion).set(0);

        return true;
    }

    @Override
    public boolean deployArtifacts(String artifactName, String artifactType, String startTime) {
        PrometheusMetricsRegisterUtils.SERVICE_UP.labels(artifactName, artifactType).setToCurrentTime();
        return true;
    }

    @Override
    public boolean unDeployArtifacts(String artifactName, String artifactType, String startTime) {
        PrometheusMetricsRegisterUtils.SERVICE_UP.labels(artifactName, artifactType).set(0);
        return true;
    }

    /**
     * Construct the API Name
     *
     * @param contextPath API Context
     * @param synCtx      Synapse Message Context
     */
    private String getApiName(String contextPath, MessageContext synCtx) {
        String apiName = null;
        for (API api : synCtx.getEnvironment().getSynapseConfiguration().getAPIs()) {
            if (restUtils.getRESTApiName(contextPath, api.getContext())) {
                apiName = api.getName();
                if (api.getVersionStrategy().getVersion() != null && !"".equals(api.getVersionStrategy().
                        getVersion())) {
                    apiName = apiName + ":v" + api.getVersionStrategy().getVersion();
                }
                synCtx.setProperty(PrometheusMetricsConstants.IS_ALREADY_PROCESSED_REST_API, true);
                synCtx.setProperty(PrometheusMetricsConstants.PROCESSED_API, api);
            }
        }
        return apiName;
    }

    /**
     * Start the latency time calculating timers
     *
     * @param synCtx           Synapse Message Context
     * @param serviceType      Type of the service
     * @param serviceName      service name
     * @param apiInvocationUrl API Invocation URL
     */
    private void startTimers(MessageContext synCtx, String serviceType, String serviceName, String apiInvocationUrl) {
        switch (serviceType) {
            case SynapseConstants.PROXY_SERVICE_TYPE:
                synCtx.setProperty(PrometheusMetricsConstants.PROXY_LATENCY_TIMER,
                        PrometheusMetricsRegisterUtils.PROXY_LATENCY_HISTOGRAM.labels(serviceName).startTimer());
                break;
            case PrometheusMetricsConstants.INBOUND_ENDPOINT:
                synCtx.setProperty(PrometheusMetricsConstants.INBOUND_ENDPOINT_LATENCY_TIMER,
                        PrometheusMetricsRegisterUtils.INBOUND_ENDPOINT_LATENCY_HISTOGRAM.
                                labels(serviceName).startTimer());
                break;
            case SynapseConstants.FAIL_SAFE_MODE_API:
                synCtx.setProperty(PrometheusMetricsConstants.API_LATENCY_TIMER,
                        PrometheusMetricsRegisterUtils.API_REQUEST_LATENCY_HISTOGRAM.
                                labels(serviceName, apiInvocationUrl).startTimer());
                break;
            default:
                log.error("Incorrect service type received.");
                break;
        }
    }

    /**
     * Terminate the latency time calculating timers
     *
     * @param timer Prometheus Histogram Timer
     */
    private void stopTimers(Histogram.Timer timer) {
        timer.observeDuration();
    }

    /**
     * Increment the Inbound Endpoint request count
     *
     * @param inboundEndpointName Inbound Endpoint Name
     */
    private void incrementInboundEndPointCount(String inboundEndpointName) {
        PrometheusMetricsRegisterUtils.TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT.labels
                (inboundEndpointName, PrometheusMetricsConstants.INBOUND_ENDPOINT).inc();
    }

    /**
     * Increment the Proxy request count
     *
     * @param proxyName proxy Name
     */
    private void incrementProxyCount(String proxyName) {
        PrometheusMetricsRegisterUtils.TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE.labels(proxyName,
                SynapseConstants.PROXY_SERVICE_TYPE).inc();
    }

    /**
     * Increment the API request count
     *
     * @param apiName          API Name
     * @param apiInvocationUrl API Invocation URL
     */
    private void incrementAPICount(String apiName, String apiInvocationUrl) {
        PrometheusMetricsRegisterUtils.TOTAL_REQUESTS_RECEIVED_API.labels(apiName, SynapseConstants.FAIL_SAFE_MODE_API,
                apiInvocationUrl).inc();
    }

    /**
     * Increment the Inbound Endpoint error count
     *
     * @param inboundEndpointName ServiceName
     */
    private void incrementInboundEndpointErrorCount(String inboundEndpointName) {
        PrometheusMetricsRegisterUtils.ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT.labels(inboundEndpointName,
                PrometheusMetricsConstants.INBOUND_ENDPOINT).inc();
    }

    /**
     * Increment the Proxy error count
     *
     * @param proxyName Proxy Name
     */
    private void incrementProxyErrorCount(String proxyName) {
        PrometheusMetricsRegisterUtils.ERROR_REQUESTS_RECEIVED_PROXY_SERVICE.labels(proxyName,
                SynapseConstants.PROXY_SERVICE_TYPE).inc();
    }

    /**
     * Increment the error count
     *
     * @param apiName          API Name
     * @param apiInvocationUrl API Invocation URL
     */
    private void incrementAPIErrorCount(String apiName, String apiInvocationUrl) {
        PrometheusMetricsRegisterUtils.ERROR_REQUESTS_RECEIVED_API.labels(apiName, SynapseConstants.FAIL_SAFE_MODE_API,
                apiInvocationUrl).inc();
    }
}
