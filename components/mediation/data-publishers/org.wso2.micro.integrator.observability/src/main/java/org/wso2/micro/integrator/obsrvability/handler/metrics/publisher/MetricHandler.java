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
package org.wso2.micro.integrator.obsrvability.handler.metrics.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractExtendedSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;
import org.wso2.micro.integrator.obsrvability.handler.util.MetricConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MetricHandler extends AbstractExtendedSynapseHandler {

    public static final String METRIC_REPORTER = "metric_reporter";
    public static final String INCREMENT_COUNT = "incrementCount";
    public static final String GET_TIMER = "getTimer";

    private Class loadedMetricClass;
    private Method method;
    private Object metricClassObject;

    public void classLoader() {
        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        Object metricReporterClass = configs.get(MetricConstants.PROMETHEUS_HANDLER + "." + METRIC_REPORTER);
        log.info("Loading the class " + metricReporterClass);

        if (metricReporterClass != null) {
              invokeClass(metricReporterClass.toString());
        } else {
            invokeClass("org.wso2.micro.integrator.obsrvability.handler.metrics.publisher.prometheus." +
                    "reporter.PrometheusReporter");
        }
    }

    private void invokeClass(String className) {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            loadedMetricClass = classLoader.loadClass(className);
            java.lang.reflect.Constructor constructor = loadedMetricClass.getConstructor();
            metricClassObject = constructor.newInstance();
        } catch (Exception ex) {
            log.error(ex);
            handleException(ex, "invokeClass");
        }
    }

    private static Log log = LogFactory.getLog(MetricHandler.class);
    private static final String DELIMITER = "/";
    private static final String EMPTY = "";
    private RESTUtils restUtils = new RESTUtils();

    @Override
    public boolean handleRequestInFlow(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        int portOffset;
        int internalHttpApiPort = 0;
        int serviceInvokePort = 0;

        // Get the Internal HTTP Inbound Endpoint port
        if ((null != System.getProperty(MetricConstants.PORT_OFFSET))) {
            portOffset = Integer.parseInt(System.getProperty((MetricConstants.PORT_OFFSET)));
            internalHttpApiPort = Integer.parseInt(synCtx.getEnvironment().getSynapseConfiguration().
                    getProperty((MetricConstants.INTERNAL_HTTP_API_PORT)));
            internalHttpApiPort = internalHttpApiPort + portOffset;

        } else {
            log.warn("Port Offset or Internal HTTP API port is not set.");
        }

        if (null == synCtx.getProperty("HAS_EXECUTED_FLOW")) {
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
                    if (null != ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                            getProperty(NhttpConstants.SERVICE_PREFIX)) {
                        String servicePort = ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                                getProperty(NhttpConstants.SERVICE_PREFIX).toString();
                        servicePort = servicePort.substring((servicePort.lastIndexOf(':') + 1),
                                servicePort.lastIndexOf(DELIMITER));
                        serviceInvokePort = Integer.parseInt(servicePort);

                    }

                    if ((serviceInvokePort != internalHttpApiPort)) {
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
                } catch (Exception ex) {
                    log.error("Error in retrieving Service Invoke Port");
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

        int portOffset = 0;
        int internalHttpApiPort = 0;
        int serviceInvokePort = 0;

        // Get the Internal HTTP Inbound Endpoint port
        if ((null != System.getProperty(MetricConstants.PORT_OFFSET))) {
            portOffset = Integer.parseInt(System.getProperty(MetricConstants.PORT_OFFSET));
            internalHttpApiPort = Integer.parseInt(synCtx.getEnvironment().getSynapseConfiguration().
                    getProperty(MetricConstants.INTERNAL_HTTP_API_PORT));
            internalHttpApiPort = internalHttpApiPort + portOffset;

        } else {
            log.warn("Port Offset or Internal HTTP API port is not set.");
        }

        if ((null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE))) {
            stopTimers( synCtx.getProperty(MetricConstants.PROXY_LATENCY_TIMER), synCtx);
        } else if (null == axis2MessageContext.getProperty("TransportInURL")) {
            // Get the port used in service invoking
            try {
                if (null != ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                        getProperty(NhttpConstants.SERVICE_PREFIX)) {
                    String servicePort = ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                            getProperty(NhttpConstants.SERVICE_PREFIX).toString();
                    if (null != servicePort) {
                        servicePort = servicePort.substring((servicePort.lastIndexOf(':') + 1),
                                servicePort.lastIndexOf(DELIMITER));
                        serviceInvokePort = Integer.parseInt(servicePort);
                    }
                }
                if (null != synCtx.getProperty(SynapseConstants.IS_INBOUND) &&
                        (serviceInvokePort != internalHttpApiPort)) {
                    stopTimers(synCtx.
                            getProperty(MetricConstants.INBOUND_ENDPOINT_LATENCY_TIMER), synCtx);
                }
                if ((serviceInvokePort != internalHttpApiPort)) {
                    stopTimers( synCtx.getProperty(MetricConstants.API_LATENCY_TIMER), synCtx);
                }
            } catch (Exception e) {
                log.error("Error in retrieving Service Invoke Port");
            }
        }
        return true;
    }

    @Override
    public boolean handleError(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
                .getAxis2MessageContext();

        // Get the Internal HTTP Inbound Endpoint port
        int portOffset = Integer.parseInt(System.getProperty(MetricConstants.PORT_OFFSET));
        int internalHttpApiPort = Integer.parseInt(synCtx.getEnvironment().getSynapseConfiguration().
                getProperty(MetricConstants.INTERNAL_HTTP_API_PORT));
        internalHttpApiPort = internalHttpApiPort + portOffset;
        int serviceInvokePort = 0;

        if (null == synCtx.getProperty(SynapseConstants.IS_ERROR_COUNT_ALREADY_PROCESSED)) {
            if (null != synCtx.getProperty(SynapseConstants.PROXY_SERVICE)) {
                String name = synCtx.getProperty(MetricConstants.PROXY_NAME).toString();
                incrementProxyErrorCount(name);
                stopTimers(synCtx.getProperty(MetricConstants.PROXY_LATENCY_TIMER), synCtx);
            } else if ((null != synCtx.getProperty(SynapseConstants.IS_INBOUND) &&
                    synCtx.getProperty(SynapseConstants.IS_INBOUND).toString().equals("true")) ||
                    ((null != axis2MessageContext.getProperty(MetricConstants.TRANSPORT_IN_URL)) &&
                            !axis2MessageContext.getProperty(MetricConstants.TRANSPORT_IN_URL).toString().
                                    contains("services"))) {
                // Get the port used in service invoking
                if (null != ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                        getProperty(NhttpConstants.SERVICE_PREFIX)) {
                    String servicePort = ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                            getProperty(NhttpConstants.SERVICE_PREFIX).toString();
                    servicePort = servicePort.substring((servicePort.lastIndexOf(':') + 1),
                            servicePort.lastIndexOf(DELIMITER));
                    serviceInvokePort = Integer.parseInt(servicePort);
                }
                if ((null != synCtx.getProperty(SynapseConstants.IS_INBOUND))) {
                    String inboundEndpointName = synCtx.getProperty(MetricConstants.INBOUND_ENDPOINT_NAME).
                            toString();
                    incrementInboundEndpointErrorCount(inboundEndpointName);
                    stopTimers(synCtx.getProperty
                            (MetricConstants.INBOUND_ENDPOINT_LATENCY_TIMER), synCtx);
                } else if (null != synCtx.getProperty(MetricConstants.SYNAPSE_REST_API) &&
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
        synCtx.setProperty(SynapseConstants.IS_ERROR_COUNT_ALREADY_PROCESSED, true);
        return true;
    }

    @Override
    public boolean handleInit() {
        try {
            classLoader();
            Class invokeServerInitMethodArgs[] = {};
            Class[] invokeServiceInitMethodArgs = {String.class, String.class, String.class, String.class};

            String host = System.getProperty(MicroIntegratorBaseConstants.LOCAL_IP_ADDRESS);
            String port = System.getProperty(MetricConstants.HTTP_PORT);
            String javaVersion = System.getProperty(MetricConstants.JAVA_VERSION);
            String javaHome = System.getProperty(MetricConstants.JAVA_HOME);

            invokeServerInitMethod("initMetrics", invokeServerInitMethodArgs);
            invokeServiceInitMethod("serverUp", invokeServiceInitMethodArgs,
                    new String[]{host, port, javaHome, javaVersion});
        } catch (Exception e) {
            handleException(e, "handleInit()");
        }
        return true;
    }

    @Override
    public boolean handleStopServer() {
        Class[] cArg = {String.class, String.class, String.class, String.class};

        String host = System.getProperty(MicroIntegratorBaseConstants.LOCAL_IP_ADDRESS);
        String port = System.getProperty(MetricConstants.HTTP_PORT);
        String javaVersion = System.getProperty(MetricConstants.JAVA_VERSION);
        String javaHome = System.getProperty(MetricConstants.JAVA_HOME);

        invokeServiceInitMethod("serverDown", cArg, new String[]{host, port, javaHome, javaVersion});
        return true;
    }

    @Override
    public boolean handleDeployArtifacts(String artifactName, String artifactType, String startTime) {
        Class[] handleDeployArtifactsMethodArgs = {String.class, String.class};
        invokeServiceInitMethod("serviceUp", handleDeployArtifactsMethodArgs,
                new String[]{artifactType, artifactName});
        return true;
    }

    @Override
    public boolean handleUnDeployArtifacts(String artifactName, String artifactType, String startTime) {
        Class[] handleUnDeployArtifactsMethodArgs = {String.class, String.class};
        invokeServiceInitMethod("serviceDown", handleUnDeployArtifactsMethodArgs,
                new String[]{artifactType, artifactName});
        return true;
    }

    private void startTimers(MessageContext synCtx, String serviceName, String serviceType, String apiInvocationUrl) {
        if (null == synCtx.getProperty("HAS_EXECUTED_FLOW")) {
            switch (serviceType) {
                case SynapseConstants.PROXY_SERVICE_TYPE:
                    Map<String, String[]> proxyMap = new HashMap();
                    proxyMap.put(MetricConstants.PROXY_LATENCY_SECONDS, new String[]{serviceName, serviceType});

                    Class[] proxyArgs = {String.class, Map.class};
                    Object proxyTimer =  invokeIncrementCountMethod(GET_TIMER, proxyArgs,
                            MetricConstants.PROXY_LATENCY_SECONDS, proxyMap);
                    synCtx.setProperty(MetricConstants.PROXY_LATENCY_TIMER, proxyTimer);
                    break;
                case MetricConstants.INBOUND_ENDPOINT:
                    Map<String, String[]> inboundEndpointMap = new HashMap();
                    inboundEndpointMap.put(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS,
                            new String[]{serviceName, serviceType});

                    Class[] inboundEndpointArgs = {String.class, Map.class};
                    Object inboundEndpointTimer = invokeIncrementCountMethod(GET_TIMER,
                            inboundEndpointArgs, MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS, inboundEndpointMap);
                    synCtx.setProperty(MetricConstants.INBOUND_ENDPOINT_LATENCY_TIMER, inboundEndpointTimer);
                    break;
                case SynapseConstants.FAIL_SAFE_MODE_API:
                    Map<String, String[]> apiMap = new HashMap();
                    apiMap.put(MetricConstants.API_LATENCY_SECONDS, new String[]{serviceName, serviceType,
                            apiInvocationUrl});

                    Class[] apiArgs = {String.class, Map.class};
                    Object apiTimer = invokeIncrementCountMethod(GET_TIMER, apiArgs,
                            MetricConstants.API_LATENCY_SECONDS, apiMap);
                    synCtx.setProperty(MetricConstants.API_LATENCY_TIMER, apiTimer);
                    break;
                default:
                    log.error("Error in starting the latency timers.");
                    break;
            }
        }
    }

    private void stopTimers(Object timer, MessageContext synCtx) {
        if (null == synCtx.getProperty(SynapseConstants.IS_ERROR_COUNT_ALREADY_PROCESSED) && (null != timer)) {
            Class[] stopTimersMethodArgs = {Object.class};
            invokeStopTimerMethod("observeTime", stopTimersMethodArgs, timer);
        }
    }

    private void incrementProxyCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.PROXY_REQUEST_COUNT_TOTAL, new String[]{name,
                SynapseConstants.PROXY_SERVICE_TYPE});

        Class[] incrementProxyCountMethodArgs = {String.class, Map.class};
        invokeIncrementCountMethod(INCREMENT_COUNT, incrementProxyCountMethodArgs,
                MetricConstants.PROXY_REQUEST_COUNT_TOTAL, metricCountMap);
    }

    private void incrementAPICount(String name, String apiInvocationUrl) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.API_REQUEST_COUNT_TOTAL, new String[]{name,
                SynapseConstants.FAIL_SAFE_MODE_API, apiInvocationUrl});

        Class[] incrementAPICountMethodArgs = {String.class, Map.class};
        invokeIncrementCountMethod(INCREMENT_COUNT, incrementAPICountMethodArgs,
                MetricConstants.API_REQUEST_COUNT_TOTAL, metricCountMap);
    }

    private void incrementInboundEndPointCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, new String[]{name,
                MetricConstants.INBOUND_ENDPOINT});

        Class[] incrementInboundEndPointCountMethodArgs = {String.class, Map.class};
        invokeIncrementCountMethod(INCREMENT_COUNT, incrementInboundEndPointCountMethodArgs,
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, metricCountMap);
    }

    private void incrementProxyErrorCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, new String[]{name,
                SynapseConstants.PROXY_SERVICE_TYPE});

        Class[] incrementProxyErrorCountMethodArgs = {String.class, Map.class};
        invokeIncrementCountMethod(INCREMENT_COUNT, incrementProxyErrorCountMethodArgs,
                MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, metricCountMap);
    }

    private void incrementApiErrorCount(String name, String apiInvocationUrl) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, new String[]{name,
                SynapseConstants.FAIL_SAFE_MODE_API, apiInvocationUrl});

        Class[] incrementApiErrorCountMethodArgs = {String.class, Map.class};
        invokeIncrementCountMethod(INCREMENT_COUNT, incrementApiErrorCountMethodArgs,
                MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, metricCountMap);
    }

    private void incrementInboundEndpointErrorCount(String name) {
        Map<String, String[]> metricCountMap = new HashMap();
        metricCountMap.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, new String[]{name,
                MetricConstants.INBOUND_ENDPOINT});

        Class[] incrementInboundEndpointErrorCountMethodArgs = {String.class, Map.class};
        invokeIncrementCountMethod(INCREMENT_COUNT, incrementInboundEndpointErrorCountMethodArgs,
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, metricCountMap);
    }

    private String getApiName(String contextPath, MessageContext synCtx) {
        String apiName = null;
        for (API api : synCtx.getEnvironment().getSynapseConfiguration().getAPIs()) {
            if (restUtils.getRESTApiName(contextPath, api.getContext())) {
                apiName = api.getName();
                if (api.getVersionStrategy().getVersion() != null && !"".equals(api.getVersionStrategy().
                        getVersion())) {
                    apiName = apiName + ":v" + api.getVersionStrategy().getVersion();
                }
                synCtx.setProperty(MetricConstants.IS_ALREADY_PROCESSED_REST_API, true);
                synCtx.setProperty(MetricConstants.PROCESSED_API, api);
            }
        }
        return apiName;
    }

    private Object invokeIncrementCountMethod(String methodName, Class[] invokeIncrementCountMethodMethodArgs,
                                              String metricName, Map metricValue) {
        Object methodReturn = null;
        try {
            method = metricClassObject.getClass().getDeclaredMethod(methodName, invokeIncrementCountMethodMethodArgs);
            methodReturn = method.invoke(metricClassObject, metricName, metricValue);

        } catch (Exception e) {
           handleException(e, "invokeIncrementCountMethod()");
        }
        return methodReturn;
    }

    private void invokeStopTimerMethod(String methodName, Class[] invokeStopTimerMethodArgs, Object timer) {
        try {
            method = metricClassObject.getClass().getDeclaredMethod(methodName, invokeStopTimerMethodArgs);
            method.invoke(metricClassObject, timer);
        } catch (Exception e) {
            handleException(e,"invokeStopTimerMethod()" );
        }
    }

    private void invokeServerInitMethod(String methodName, Class[] invokeServerInitMethodArgs) {
        try {
            method = metricClassObject.getClass().getDeclaredMethod(methodName, invokeServerInitMethodArgs);
            method.invoke(metricClassObject);
        } catch (Exception e)  {
          handleException(e, "invokeServerInitMethod()");
        }
    }

    private void invokeServiceInitMethod(String methodName, Class[] invokeServiceInitMethodArgs, String[] params) {
        try {
            method = metricClassObject.getClass().getDeclaredMethod(methodName, invokeServiceInitMethodArgs);
            method.invoke(metricClassObject, params);
        } catch (Exception e) {
            handleException(e, "invokeServiceInitMethod()");
        }
    }

    private static void handleException(Exception e, String methodName) {
        if (e instanceof NoSuchMethodException) {
            try {
                throw new NoSuchMethodException();
            } catch (NoSuchMethodException ex) {
                 log.error("Specified method " + methodName + " not found.");
            }
        } else if (e instanceof IllegalAccessException) {
            try {
                throw new IllegalAccessException();
            } catch (IllegalAccessException ex) {
                log.error ("Specified method" + methodName + " does not have access to the specified instance.");
            }
        } else if (e instanceof InvocationTargetException) {
            try {
                throw new InvocationTargetException(e);
            } catch (InvocationTargetException ex) {
                log.error ("Error in invoking the " + methodName + " method");
            }
        } else {
            try {
                throw new ClassNotFoundException();
            } catch (ClassNotFoundException e1) {
                 log.error("Specified class " + methodName + " not found.");
            }
        }
    }
}

