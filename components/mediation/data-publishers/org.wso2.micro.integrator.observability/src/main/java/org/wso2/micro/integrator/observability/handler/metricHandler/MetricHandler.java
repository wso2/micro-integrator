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
package org.wso2.micro.integrator.observability.handler.metricHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractExtendedSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;
import org.wso2.micro.integrator.observability.handler.metricHandler.prometheus.reporter.PrometheusReporter;
import org.wso2.micro.integrator.observability.handler.util.MetricConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for instrumenting Metrics by wrapping the implementation and
 * there by allows the implementation to plug into the handler in runtime.
 */
public class MetricHandler extends AbstractExtendedSynapseHandler {
    private static final String METRIC_REPORTER = "metric_reporter";
    private static final String DELIMITER = "/";
    private static final String EMPTY = "";

    private MetricReporter metricReporterInstance;
    private Class loadedMetricClass;
    private static Log log = LogFactory.getLog(MetricHandler.class);

    private static final String serverPortOffset = System.getProperty(MetricConstants.PORT_OFFSET);
    private static final String host = System.getProperty(MicroIntegratorBaseConstants.LOCAL_IP_ADDRESS);
    private static final String port = System.getProperty(MetricConstants.HTTP_PORT);
    private static final String javaVersion = System.getProperty(MetricConstants.JAVA_VERSION);
    private static final String javaHome = System.getProperty(MetricConstants.JAVA_HOME);

    @Override
    public boolean handleInitServer() {
        getMetricReporter();
        metricReporterInstance.initMetrics();

        metricReporterInstance.serverUp(host, port, javaHome, javaVersion);
        return true;
    }

    /**
     * Load the MetricReporter class from the deployment.toml file if a user has defined a MetricReporter.
     * Use default PrometheusReporter if the user hasn't defined a MetricReporter or an error occurs
     * during custom MetricReporter class invocation.
     */
    private void getMetricReporter() {
        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        Object metricReporterClass = configs.get(MetricConstants.METRIC_HANDLER + "." + METRIC_REPORTER);

        if (metricReporterClass != null) {
            try {
                loadedMetricClass = Class.forName(metricReporterClass.toString());
                metricReporterInstance = (MetricReporter) loadedMetricClass.newInstance();
                log.debug("The class " + metricReporterClass + "loaded successfully");
            } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                log.error("Error in loading the class" + metricReporterClass.toString() +
                        "Hence loading the default PrometheusReporter class ");
                loadDefaultPrometheusReporter();
            }
        } else {
            loadDefaultPrometheusReporter();
        }
    }

    /**
     * Load the PrometheusReporter class by default.
     */
    private void loadDefaultPrometheusReporter() {
        try {
            loadedMetricClass = PrometheusReporter.class;
            metricReporterInstance = (MetricReporter) loadedMetricClass.newInstance();
            log.debug("The class org.wso2.micro.integrator.obsrvability.handler.metrics.publisher.prometheus." +
                    "reporter.PrometheusReporter loaded successfully");
        } catch (IllegalAccessException | InstantiationException e) {
            handleException("Error in loading the PrometheusReporter Class", e);
        }
    }

    @Override
    public boolean handleRequestInFlow(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        int serviceInvokePort ;
        int internalHttpApiPort;

        if ((null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE))) {
            String proxyName = axis2MessageContext.getAxisService().getName();

            incrementProxyCount(proxyName);
            startTimers(synCtx, proxyName, SynapseConstants.PROXY_SERVICE_TYPE, null);
        } else if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND)) {
            String inboundEndpointName = synCtx.getProperty(SynapseConstants.INBOUND_ENDPOINT_NAME).toString();

            incrementInboundEndPointCount(inboundEndpointName);
            startTimers(synCtx, inboundEndpointName, MetricConstants.INBOUND_ENDPOINT,
                    null);
        } else if ((null != axis2MessageContext.getProperty(MetricConstants.TRANSPORT_IN_URL) &&
                !axis2MessageContext.getProperty(MetricConstants.TRANSPORT_IN_URL).toString().
                        contains("services"))) {
            try {
                serviceInvokePort = getServiceInvokePort(synCtx);
                internalHttpApiPort = getInternalHTTPInboundEndpointPort(synCtx);

                if (serviceInvokePort != internalHttpApiPort) {
                    String context = axis2MessageContext.getProperty(MetricConstants.TRANSPORT_IN_URL).
                            toString();
                    String apiInvocationUrl = axis2MessageContext.getProperty(MetricConstants.SERVICE_PREFIX).
                            toString() + context.replaceFirst(DELIMITER, EMPTY);
                    String apiName = getApiName(context, synCtx);
                    if (apiName != null) {
                        incrementAPICount(apiName, apiInvocationUrl);
                        startTimers(synCtx, apiName, SynapseConstants.FAIL_SAFE_MODE_API, apiInvocationUrl);
                    }
                }
            } catch (NullPointerException ex) {
                log.error("Error in retrieving Service Invoke Port");
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

        int internalHttpApiPort;
        int serviceInvokePort;

        if ((null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE))) {
            stopTimers(synCtx.getProperty(MetricConstants.PROXY_LATENCY_TIMER), synCtx);
        } else if (null == axis2MessageContext.getProperty(MetricConstants.TRANSPORT_IN_URL)) {
            try {
                serviceInvokePort = getServiceInvokePort(synCtx);
                internalHttpApiPort = getInternalHTTPInboundEndpointPort(synCtx);

                if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND) &&
                        (serviceInvokePort != internalHttpApiPort)) {
                    stopTimers(synCtx.
                            getProperty(MetricConstants.INBOUND_ENDPOINT_LATENCY_TIMER), synCtx);
                }
                if (serviceInvokePort != internalHttpApiPort) {
                    stopTimers(synCtx.getProperty(MetricConstants.API_LATENCY_TIMER), synCtx);
                }

            } catch (NullPointerException e) {
                log.error("Error in retrieving Service Invoke Port");
            }
        }
        return true;
    }

    @Override
    public boolean handleError(MessageContext synCtx) {
          org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        int internalHttpApiPort;
        int serviceInvokePort;

        if (null == synCtx.getProperty(SynapseConstants.IS_ERROR_COUNT_ALREADY_PROCESSED)) {
            if (null != synCtx.getProperty("proxy.name")) {
                String name = synCtx.getProperty(SynapseConstants.PROXY_SERVICE).toString();
                incrementProxyErrorCount(name);
                stopTimers(synCtx.getProperty(MetricConstants.PROXY_LATENCY_TIMER), synCtx);
            } else if (null != axis2MessageContext.getProperty("TransportInURL") &&
                    !axis2MessageContext.getProperty("TransportInURL").toString().contains("services")) {
                if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND) &&
                        synCtx.getProperty(SynapseConstants.IS_INBOUND).toString().equals("true")) {
                    String inboundEndpointName = synCtx.getProperty(SynapseConstants.INBOUND_ENDPOINT_NAME).
                            toString();
                    incrementInboundEndpointErrorCount(inboundEndpointName);
                    stopTimers(synCtx.getProperty
                            (MetricConstants.INBOUND_ENDPOINT_LATENCY_TIMER), synCtx);
                } else {
                    serviceInvokePort = getServiceInvokePort(synCtx);
                    internalHttpApiPort = getInternalHTTPInboundEndpointPort(synCtx);

                    if (null != synCtx.getProperty(RESTConstants.SYNAPSE_REST_API) &&
                            (serviceInvokePort != internalHttpApiPort)) {
                        String context = axis2MessageContext.getProperty(MetricConstants.TRANSPORT_IN_URL).
                                toString();
                        String apiInvocationUrl = axis2MessageContext.getProperty(MetricConstants.SERVICE_PREFIX).
                                toString() + context.replaceFirst(DELIMITER, EMPTY);
                        String apiName = getApiName(context, synCtx);

                        incrementApiErrorCount(apiName, apiInvocationUrl);
                        stopTimers(synCtx.getProperty(MetricConstants.API_LATENCY_TIMER), synCtx);
                    }
                }
            }
        }
        synCtx.setProperty(SynapseConstants.IS_ERROR_COUNT_ALREADY_PROCESSED, true);
        return true;
    }

    @Override
    public boolean handleServerShutDown() {
        metricReporterInstance.serverDown(host, port, javaHome, javaVersion);
        return true;
    }

    @Override
    public boolean handleArtifactDeployment(String artifactName, String artifactType, String startTime) {
        metricReporterInstance.serviceUp(artifactName, artifactType);
        return true;
    }

    @Override
    public boolean handleArtifactUnDeployment(String artifactName, String artifactType, String startTime) {
        metricReporterInstance.serviceDown(artifactName, artifactType);
        return true;
    }

    /**
     * Start the timers to observe request latency.
     *
     * @param synCtx           The Synapse Message Context
     * @param serviceName      The proxy/api/inbound endpoint name
     * @param serviceType      The service type (proxy/api/inbound endpoint)
     * @param apiInvocationUrl The api invocation url
     */
    private void startTimers(MessageContext synCtx, String serviceName, String serviceType, String apiInvocationUrl) {
        switch (serviceType) {
            case SynapseConstants.PROXY_SERVICE_TYPE:
                Map<String, String[]> proxyMap = new HashMap();
                proxyMap.put(MetricConstants.PROXY_LATENCY_SECONDS, new String[]{serviceName, serviceType});

                synCtx.setProperty(MetricConstants.PROXY_LATENCY_TIMER,
                        metricReporterInstance.getTimer(MetricConstants.PROXY_LATENCY_SECONDS, proxyMap));
                break;
            case MetricConstants.INBOUND_ENDPOINT:
                Map<String, String[]> inboundEndpointMap = new HashMap();
                inboundEndpointMap.put(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS, new String[]{serviceName,
                        serviceType});
                synCtx.setProperty(MetricConstants.INBOUND_ENDPOINT_LATENCY_TIMER,
                        metricReporterInstance.getTimer(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS,
                                inboundEndpointMap));
                break;
            case SynapseConstants.FAIL_SAFE_MODE_API:
                Map<String, String[]> apiMap = new HashMap();
                apiMap.put(MetricConstants.API_LATENCY_SECONDS, new String[]{serviceName, serviceType,
                        apiInvocationUrl});

                synCtx.setProperty(MetricConstants.API_LATENCY_TIMER,
                        metricReporterInstance.getTimer(MetricConstants.API_LATENCY_SECONDS, apiMap));
                break;
            default:
                log.error("No proper service type found");
                break;
        }
    }

    /**
     * Stop the timer when the response is leaving the Synapse Engine.
     *
     * @param timer The Timer object used to observe latency
     * @ param synCtx The Synapse Message Context
     */
    private void stopTimers(Object timer, MessageContext synCtx) {
        if (null == synCtx.getProperty(SynapseConstants.IS_ERROR_COUNT_ALREADY_PROCESSED) && (null != timer)) {
             metricReporterInstance.observeTime(timer);
        }
    }

    /**
     * Increment the request count received by a proxy service.
     *
     * @param name The metric name
     */
    private void incrementProxyCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.PROXY_REQUEST_COUNT_TOTAL, new String[] {name,
                SynapseConstants.PROXY_SERVICE_TYPE});
        metricReporterInstance.incrementCount(MetricConstants.PROXY_REQUEST_COUNT_TOTAL, metricCountMap);
    }

    /**
     * Increment the request count received by an api.
     *
     * @param name             The metric name
     * @param apiInvocationUrl api Invocation URL
     */
    private void incrementAPICount(String name, String apiInvocationUrl) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.API_REQUEST_COUNT_TOTAL, new String[]{name,
                SynapseConstants.FAIL_SAFE_MODE_API, apiInvocationUrl});
        metricReporterInstance.incrementCount(MetricConstants.API_REQUEST_COUNT_TOTAL, metricCountMap);
    }

    /**
     * Increment the request count received by an inbound endpoint.
     *
     * @param name The metric name
     */
    private void incrementInboundEndPointCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, new String[]{name,
                MetricConstants.INBOUND_ENDPOINT});
        metricReporterInstance.incrementCount(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, metricCountMap);
    }

    /**
     * Increment the error request count received by a proxy service.
     *
     * @param name The metric name
     */
    private void incrementProxyErrorCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, new String[]{name,
                SynapseConstants.PROXY_SERVICE_TYPE});
        metricReporterInstance.incrementCount(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, metricCountMap);
    }

    /**
     * Increment the error request count received by an api.
     *
     * @param name             The metric name
     * @param apiInvocationUrl api Invocation URL
     */
    private void incrementApiErrorCount(String name, String apiInvocationUrl) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, new String[]{name,
                SynapseConstants.FAIL_SAFE_MODE_API, apiInvocationUrl});
        metricReporterInstance.incrementCount(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, metricCountMap);
    }

    /**
     * Increment the error request count received by an inbound endpoint.
     *
     * @param name The metric name
     */
    private void incrementInboundEndpointErrorCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, new String[]{name,
                MetricConstants.INBOUND_ENDPOINT});
        metricReporterInstance.incrementCount(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL,
                metricCountMap);
    }

    /**
     * Get the api name.
     *
     * @param contextPath The api context path
     * @param synCtx The Synapse Message Context
     * @return String The api name
     */
    private String getApiName(String contextPath, MessageContext synCtx) {
        String apiName = null;
        for (API api : synCtx.getEnvironment().getSynapseConfiguration().getAPIs()) {
            if (RESTUtils.isRESTApiPath(contextPath, api.getContext())) {
                apiName = api.getName();
                if (api.getVersionStrategy().getVersion() != null && !"".equals(api.getVersionStrategy().
                        getVersion())) {
                    apiName = apiName + ":v" + api.getVersionStrategy().getVersion();
                }
                synCtx.setProperty(RESTConstants.IS_PROMETHEUS_ENGAGED, true);
                synCtx.setProperty(RESTConstants.PROCESSED_API, api);
            }
        }
        return apiName;
    }

    /**
     * No Synapse Exception is thrown because the mediation flow should not be affected by
     * an exception thrown in the Observability Handler.
     *
     * @param e          The thrown exception
     * @param msg Method from which the exception is thrown
     */
    private void handleException(String msg, Exception e) {
        log.error(msg, e);
    }

    private int getInternalHTTPInboundEndpointPort(MessageContext synCtx) {
        int portOffset;
        int internalHttpApiPort = 0;

        if (null != serverPortOffset) {
            portOffset = Integer.parseInt(serverPortOffset);
            internalHttpApiPort = Integer.parseInt(synCtx.getEnvironment().getSynapseConfiguration().
                    getProperty((MetricConstants.INTERNAL_HTTP_API_PORT)));
            internalHttpApiPort = internalHttpApiPort + portOffset;
        } else {
            log.warn("Port Offset or Internal HTTP API port is null.");
        }
        return internalHttpApiPort;

    }

    private int getServiceInvokePort(MessageContext synCtx) {
        int serviceInvokePort = 0;

        if (null != ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(NhttpConstants.SERVICE_PREFIX)) {
            String servicePort = ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                    getProperty(NhttpConstants.SERVICE_PREFIX).toString();
            servicePort = servicePort.substring((servicePort.lastIndexOf(':') + 1),
                    servicePort.lastIndexOf(DELIMITER));
            serviceInvokePort = Integer.parseInt(servicePort);
        } else {
            log.warn("Service Prefix is null.");
        }
        return serviceInvokePort;
    }
}
