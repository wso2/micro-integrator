package org.wso2.micro.integrator.prometheus.handler.handler;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.integrator.prometheus.handler.MetricReporter;
import org.wso2.micro.integrator.prometheus.handler.util.MetricConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheusReporter implements MetricReporter {
    private static final String SERVICE_NAME = "service_name";
    private static final String SERVICE_TYPE = "service_type";
    private static final String INVOCATION_URL = "invocation_url";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String JAVA_VERSION = "java_version";
    private static final String JAVA_HOME = "java_home";

    static Counter TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE ;
    static Counter TOTAL_REQUESTS_RECEIVED_API;
    static Counter TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT;
    static Counter ERROR_REQUESTS_RECEIVED_PROXY_SERVICE;
    static Counter ERROR_REQUESTS_RECEIVED_API;
    static Counter ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT;

    static Histogram PROXY_LATENCY_HISTOGRAM;
    static Histogram API_LATENCY_HISTOGRAM;
    static Histogram INBOUND_ENDPOINT_LATENCY_HISTOGRAM;

    static Gauge SERVER_UP;

    static Map metricMap = new HashMap();

    static final Gauge SERVICE_UP = Gauge.build("wso2_service_up", "Service status").
            labelNames(SERVICE_NAME, SERVICE_TYPE).register();

    @Override
    public void initMetric(String serviceType, String type, String metricName, String metricHelp, Map<String,
            String[]> properties) {

        double[] proxyLatencyBuckets = {0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};
        double[] apiLatencyBuckets = {0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};
        double[] inboundEndpointLatencyBuckets = {0.19, 0.20, 0.25, 0.30, 0.35, 0.40, 0.50, 0.60, 1, 5};

        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        Object proxyConfigBuckets = configs.get(MetricConstants.PROMETHEUS_HANDLER + "." + MetricConstants.PROXY_LATENCY_BUCKETS);
        Object apiConfigBuckets = configs.get(MetricConstants.PROMETHEUS_HANDLER + "." + MetricConstants.API_LATENCY_BUCKETS);
        Object inboundEndpointConfigBuckets = configs.get(MetricConstants.PROMETHEUS_HANDLER + "." + MetricConstants.INBOUND_ENDPOINT_LATENCY_BUCKETS);

        if (null != proxyConfigBuckets) {
            List<Object> list = Arrays.asList(proxyConfigBuckets);

            int size = ((ArrayList) proxyConfigBuckets).size();
            for (int i = 0; i < size; i++) {
                proxyLatencyBuckets[i] = (double)((ArrayList)list.get(0)).get(i);
            }
        }
        if (null != apiConfigBuckets) {
            List<Object> list = Arrays.asList(apiConfigBuckets);

            int size = ((ArrayList) apiConfigBuckets).size();
            for (int i = 0; i < size; i++) {
                proxyLatencyBuckets[i] = (double)((ArrayList)list.get(0)).get(i);
            }
        }
        if (null != inboundEndpointConfigBuckets) {
            List<Object> list = Arrays.asList(inboundEndpointConfigBuckets);

            int size = ((ArrayList) inboundEndpointConfigBuckets).size();
            for (int i = 0; i < size; i++) {
                proxyLatencyBuckets[i] = (double) ((ArrayList) list.get(0)).get(i);
            }
        }

        String[] value = properties.get(metricName);

        if (serviceType.equals(service.PROXY.name())) {
            if (type.equals(MetricConstants.COUNTER)) {
                TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build(MetricConstants.PROXY_REQUEST_COUNT_TOTAL, metricHelp).
                        labelNames(value).register();
                metricMap.put(metricName, TOTAL_REQUESTS_RECEIVED_PROXY_SERVICE);

            } else if (type.equals(MetricConstants.HISTOGRAM)) {
                PROXY_LATENCY_HISTOGRAM = Histogram.build()
                        .name(MetricConstants.PROXY_LATENCY_SECONDS)
                        .help(metricHelp)
                        .labelNames(value)
                        .buckets(proxyLatencyBuckets)
                        .register();
                metricMap.put(metricName, PROXY_LATENCY_HISTOGRAM);
            }
        }  else if (serviceType.equals(service.API.name())) {
            if (type.equals(MetricConstants.COUNTER)) {
                TOTAL_REQUESTS_RECEIVED_API = Counter.build(MetricConstants.API_REQUEST_COUNT_TOTAL, metricHelp).
                        labelNames(value).register();
                metricMap.put(metricName, TOTAL_REQUESTS_RECEIVED_API);
            } else if (type.equals(MetricConstants.HISTOGRAM)) {
                API_LATENCY_HISTOGRAM = Histogram.build()
                        .name(MetricConstants.API_LATENCY_SECONDS)
                        .help(metricHelp)
                        .labelNames(value)
                        .buckets(apiLatencyBuckets)
                        .register();
                metricMap.put(metricName, API_LATENCY_HISTOGRAM);
            }
        } else if (serviceType.equals(service.INBOUND_ENDPOINT.name())) {
            if (type.equals(MetricConstants.COUNTER)) {
                TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT = Counter.build
                        (MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, metricHelp).
                        labelNames(value).register();
                metricMap.put(metricName, TOTAL_REQUESTS_RECEIVED_INBOUND_ENDPOINT);

            } else if (type.equals(MetricConstants.HISTOGRAM)) {
                INBOUND_ENDPOINT_LATENCY_HISTOGRAM = Histogram.build()
                        .name(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS)
                        .help(metricHelp)
                        .labelNames(value)
                        .buckets(inboundEndpointLatencyBuckets)
                        .register();
                metricMap.put(metricName, INBOUND_ENDPOINT_LATENCY_HISTOGRAM);
            }
        }
    }

    @Override
    public void initErrorMetrics(String serviceType, String type, String metricName, String metricHelp, Map<String, String[]> properties) {
        String[] value = properties.get(metricName);

        if (serviceType.equals(service.PROXY.name())) {
            ERROR_REQUESTS_RECEIVED_PROXY_SERVICE = Counter.build(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, metricHelp).
                    labelNames(value).register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_PROXY_SERVICE);
        } else if (serviceType.equals(service.API.name())) {
            ERROR_REQUESTS_RECEIVED_API = Counter.build(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, metricHelp).
                    labelNames(value).register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_API);
        } else if (serviceType.equals(service.INBOUND_ENDPOINT.name())) {
            ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT = Counter.build(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, metricHelp).labelNames(value).register();
            metricMap.put(metricName, ERROR_REQUESTS_RECEIVED_INBOUND_ENDPOINT);

        }
    }

    @Override
    public void createMetric(String type, String metricName) {

//     SERVICE_UP = Gauge.build("wso2_service_up", "Service status").
//                labelNames(SERVICE_NAME, SERVICE_TYPE).register();

    }

    @Override
    public void incrementCount(String metricName, Map<String, String[]> properties) {
        Counter counter = (Counter) metricMap.get(metricName);
        String[] value = properties.get(metricName);

        counter.labels(value).inc();
    }

    @Override
    public void decrementCount(String metricName, Map<String, String> properties) {

    }

//    @Override
//    public void incrementErrorCount(String metricName, Map<String, String[]> properties) {
//        Counter counter = (Counter) metricMap.get(metricName);
//        String[] value = properties.get(metricName);
//
//        counter.labels(value).inc();
//        System.out.println(counter.labels(value).get());
//
//    }

    @Override
    public Object getTimer(String metricName, Map<String, String[]> properties) {
        String[] value = properties.get(metricName);
        Histogram timer = (Histogram) metricMap.get(metricName);

        return timer.labels(value).startTimer();
    }

    @Override
    public void observeTime(Object timer) {
        Histogram.Timer timer2 = (Histogram.Timer) timer;
        timer2.observeDuration();
    }

    @Override
    public void serverUp(String host, String port, String javaVersion, String javaHome) {
        SERVER_UP = Gauge.build(MetricConstants.SERVER_UP, "Server status").
                labelNames(HOST, PORT, JAVA_VERSION, JAVA_HOME).register();
        SERVER_UP.labels(host, port, javaHome, javaVersion).setToCurrentTime();
    }

    public void serverDown(String host, String port, String javaVersion, String javaHome){
        SERVER_UP.labels(host, port, javaHome, javaVersion).set(0);
    }

    @Override
    public void serviceUp(String serviceName, String serviceType, String startTime) {
        SERVICE_UP.labels(serviceName,serviceType).setToCurrentTime();
    }

    public void serviceState(){
//        SERVICE_UP = Gauge.build("wso2_service_up", "Service status").
//            labelNames(SERVICE_NAME, SERVICE_TYPE).register();
    }

    enum service {
        PROXY,
        API,
        INBOUND_ENDPOINT
    }
}