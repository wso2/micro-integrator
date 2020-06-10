package org.wso2.micro.integrator.prometheus.handler.handler;

import org.wso2.micro.integrator.prometheus.handler.util.MetricConstants;

import java.util.HashMap;
import java.util.Map;

public class PrometheusMetricCreator {

    private static final String SERVICE_NAME = "service_name";
    private static final String SERVICE_TYPE = "service_type";
    private static final String INVOCATION_URL = "invocation_url";

    static PrometheusReporter prometheusReporter = new PrometheusReporter();

    void createProxyServiceMetric() {
        Map<String,String[]> map = new HashMap<>();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE};
        map.put(MetricConstants.PROXY_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.PROXY_LATENCY_SECONDS, labels);

        prometheusReporter.initMetric("PROXY", MetricConstants.COUNTER, MetricConstants.PROXY_REQUEST_COUNT_TOTAL,
                "Total number of requests to a proxy service", map);
        prometheusReporter.initMetric("PROXY", MetricConstants.HISTOGRAM, MetricConstants.PROXY_LATENCY_SECONDS,
                "Latency of requests to a proxy service", map);
    }

    void createAPIServiceMetric() {
        HashMap<String, String[]> map = new HashMap();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE, INVOCATION_URL};
        map.put(MetricConstants.API_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.API_LATENCY_SECONDS, labels);

        prometheusReporter.initMetric("API", MetricConstants.COUNTER, MetricConstants.API_REQUEST_COUNT_TOTAL,
                "Total number of requests to an api", map);
        prometheusReporter.initMetric("API", MetricConstants.HISTOGRAM, MetricConstants.API_LATENCY_SECONDS,
                "Latency of requests to an api", map);
    }

     void createInboundEndpointMetric() {
        HashMap<String, String[]> map = new HashMap<>();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE};
        map.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL, labels);
        map.put(MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS, labels);

        prometheusReporter.initMetric("INBOUND_ENDPOINT", MetricConstants.COUNTER,
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL,
                "Total number of requests to an inbound endpoint.", map);

        prometheusReporter.initMetric("INBOUND_ENDPOINT", MetricConstants.HISTOGRAM, MetricConstants.INBOUND_ENDPOINT_LATENCY_SECONDS,
                "Latency of requests to an inbound endpoint.", map);

    }

   void createProxyServiceErrorMetric() {

        HashMap<String, String[]> map = new HashMap();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE};

        map.put(MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL, labels);

        prometheusReporter.initErrorMetrics("PROXY", MetricConstants.COUNTER, MetricConstants.PROXY_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to a proxy service", map);
    }

    public void createApiErrorMetric() {

        HashMap<String, String[]> map = new HashMap();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE, INVOCATION_URL};

        map.put(MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL, labels);

        prometheusReporter.initErrorMetrics("API", "Counter", MetricConstants.API_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to an api", map);
    }

    void createInboundEndpointErrorMetric() {

        HashMap<String, String[]> map = new HashMap();
        String[] labels = {SERVICE_NAME, SERVICE_TYPE};

        map.put(MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL, labels);

        prometheusReporter.initErrorMetrics("INBOUND_ENDPOINT", "Counter",
                MetricConstants.INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL,
                "Total number of error requests to an inbound endpoint.", map);
    }

//    public static void createServerUpMetrics() {
//        HashMap map = new HashMap();
//        String[] labels = {HOST, PORT, JAVA_HOME, JAVA_VERSION};
//        map.put("wso2_integration_server_up", labels);
//
//        prometheusReporter.initMetric("API", "Gauge", MetricConstants.SERVER_UP,
//                "Server Status", map);
//    }

//    public static void createServiceUpMetrics() {
//
//        HashMap map = new HashMap();
//        String[] labels = {SERVICE_NAME, SERVICE_TYPE};
//        map.put("wso2_integration_server_up", labels);
//
//        prometheusReporter.createMetric(SERVICE_NAME, SERVICE_TYPE);
//    }
}
