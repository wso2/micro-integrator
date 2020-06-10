package org.wso2.micro.integrator.prometheus.handler.util;

public class MetricConstants {

    public static final String PROXY_NAME = "proxy.name";
    public static final String SYNAPSE_REST_API = "SYNAPSE_REST_API";
    public static final String REMOTE_HOST = "REMOTE_HOST";
    public static final String INBOUND_ENDPOINT = "inbound-endpoint";
    public static final String INBOUND_ENDPOINT_NAME = "inbound.endpoint.name";

    public static final String PROXY_LATENCY_TIMER = "PROXY_LATENCY_TIMER";
    public static final String API_LATENCY_TIMER = "API_LATENCY_TIMER";
    public static final String INBOUND_ENDPOINT_LATENCY_TIMER = "INBOUND_ENDPOINT_LATENCY_TIMER";

    public static final String HTTP_PORT = "http.nio.port";
    public static final String JAVA_VERSION = "java.vm.specification.version";
    public static final String JAVA_HOME = "java.home";

    //Constants for Synapse properties
    public static final String HAS_EXECUTED_ERROR_FLOW = "HAS_EXECUTED_ERROR_FLOW";
    public static final String IS_ALREADY_PROCESSED_REST_API = "IS_ALREADY_PROCESSED_REST_API";
    public static final String PROCESSED_API = "PROCESSED_API";
    public static final String TRANSPORT_IN_URL = "TransportInURL";
    public static final String SERVICE_PREFIX = "SERVICE_PREFIX";
    public static final String PORT_OFFSET = "portOffset";
    public static final String INTERNAL_HTTP_API_PORT = "internal.http.api.port";

    //Constants for metric names
    public static final String PROXY_REQUEST_COUNT_TOTAL = "wso2_integration_proxy_request_count_total";
    public static final String PROXY_REQUEST_COUNT_ERROR_TOTAL = "wso2_integration_proxy_request_count_error_total";
    public static final String PROXY_LATENCY_SECONDS = "wso2_integration_proxy_latency_seconds";

    public static final String API_REQUEST_COUNT_TOTAL = "wso2_integration_api_request_count_total";
    public static final String API_REQUEST_COUNT_ERROR_TOTAL = "wso2_integration_api_request_count_error_total";
    public static final String API_LATENCY_SECONDS = "wso2_integration_papi_latency_seconds";

    public static final String INBOUND_ENDPOINT_REQUEST_COUNT_TOTAL = "wso2_integration_inbound_endpoint_request_count_total";
    public static final String INBOUND_ENDPOINT_REQUEST_COUNT_ERROR_TOTAL = "wso2_integration_inbound_endpoint_request_count_error_total";
    public static final String INBOUND_ENDPOINT_LATENCY_SECONDS = "wso2_integration_inbound_endpoint_latency_seconds";

    public static final String SERVER_UP = "wso2_integration_server_up";

    public static final String PROMETHEUS_HANDLER = "prometheus_handler";
    public static final String PROXY_LATENCY_BUCKETS = "proxy_latency_buckets";
    public static final String API_LATENCY_BUCKETS = "api_latency_buckets";
    public static final String INBOUND_ENDPOINT_LATENCY_BUCKETS = "inbound_endpoint_latency_buckets";

    //Constants for Prometheus Metrics types
    public static final String COUNTER = "Counter";
    public static final String HISTOGRAM = "Histogram";
    public static final String GAUGE = "Gauge";

}
