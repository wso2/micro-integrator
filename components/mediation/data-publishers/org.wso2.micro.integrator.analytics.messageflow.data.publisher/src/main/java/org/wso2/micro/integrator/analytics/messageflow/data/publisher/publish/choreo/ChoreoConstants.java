package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.choreo;

public class ChoreoConstants {

    public static final String NOT_APPLICABLE = "N/A";
    public static final String IS_INBOUND = "isInbound";

    public static class SynapseConfigKeys {
        public static final String IDENTIFIER = "analytics.id";
        public static final String ANALYTICS_ENABLED = "analytics.enabled";
        public static final String API_ANALYTICS_ENABLED = "analytics.api_analytics.enabled";
        public static final String PROXY_SERVICE_ANALYTICS_ENABLED = "analytics.proxy_service_analytics.enabled";
        public static final String SEQUENCE_ANALYTICS_ENABLED = "analytics.sequence_analytics.enabled";
        public static final String ENDPOINT_ANALYTICS_ENABLED = "analytics.endpoint_analytics.enabled";
        public static final String INBOUND_ENDPOINT_ANALYTICS_ENABLED = "analytics.inbound_endpoint_analytics.enabled";
    }

    public static class PayloadKeys {
        public static final String SERVER_HOST_NAME = "serverHostname";
        public static final String SERVER_ID = "serverId";
        public static final String SERVER_NAME = "serverName";
        public static final String SERVER_IP_ADDRESS = "serverIpAddress";
        public static final String NAME = "name";
        public static final String TIMESTAMP = "timestamp";
        public static final String ENTITY_TYPE = "entityType";
        public static final String FAULT_RESPONSE = "faultResponse";
        public static final String FAILURE = "failure";
        public static final String LATENCY = "latency";
        public static final String MESSAGE_ID = "messageId";
        public static final String API_SUB_REQUEST_PATH = "apiSubRequestPath";
        public static final String API_METHOD = "apiMethod";
        public static final String API_CONTEXT = "apiContext";
        public static final String API_TRANSPORT = "apiTransport";
        public static final String CORRELATION_ID = "correlationId";
    }

    public static class ArtifactType {
        public static final String API = "API";
        public static final String PROXY = "Proxy Service";
        public static final String SEQUENCE = "Sequence";
        public static final String ENDPOINT = "Endpoint";
        public static final String INBOUND_ENDPOINT = "Inbound EndPoint";
    }

}
