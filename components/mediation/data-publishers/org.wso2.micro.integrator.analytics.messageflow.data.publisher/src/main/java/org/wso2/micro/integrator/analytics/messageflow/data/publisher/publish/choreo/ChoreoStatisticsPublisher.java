package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.choreo;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.aspects.flow.statistics.elasticsearch.ElasticMetadata;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingEvent;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;

import org.apache.synapse.aspects.flow.statistics.util.StatisticsConstants;
import org.apache.synapse.commons.CorrelationConstants;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.StatisticsPublisher;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.elasticsearch.ElasticConstants;
import org.wso2.micro.integrator.initializer.ServiceBusInitializer;

import java.time.Instant;
import java.util.Objects;

public class ChoreoStatisticsPublisher implements StatisticsPublisher {
    private static ChoreoStatisticsPublisher instance = null;
    private final Log log = LogFactory.getLog(ChoreoStatisticsPublisher.class);

    //These are the flags to disable analytics for each component
    private boolean analyticsEnabledForAPI;
    private boolean analyticsEnabledForSequences;
    private boolean analyticsEnabledForProxyServices;
    private boolean analyticsEnabledForEndpoints;
    private boolean analyticsEnabledForInboundEndpoints;

    public static ChoreoStatisticsPublisher GetInstance() {
        if (instance == null) {
            instance = new ChoreoStatisticsPublisher();
        }
        return instance;
    }

    private void loadConfigurations() {
        analyticsEnabledForAPI = SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.API_ANALYTICS_ENABLED, true);
        analyticsEnabledForSequences = SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.SEQUENCE_ANALYTICS_ENABLED, true);
        analyticsEnabledForProxyServices = SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.PROXY_SERVICE_ANALYTICS_ENABLED, true);
        analyticsEnabledForEndpoints = SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.ENDPOINT_ANALYTICS_ENABLED, true);
        analyticsEnabledForInboundEndpoints = SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.INBOUND_ENDPOINT_ANALYTICS_ENABLED, true);
    }

    protected ChoreoStatisticsPublisher() {
        loadConfigurations();
    }

    @Override
    public void process(PublishingFlow publishingFlow, int tenantId) {
        if (publishingFlow.getEvents().toArray().length > 0) {
            publishingFlow.getEvents().forEach(event -> {
                if (event.getElasticMetadata() == null || !event.getElasticMetadata().isValid()) {
                    return;
                }
                if ((StatisticsConstants.FLOW_STATISTICS_API.equals(event.getComponentType()) &&
                                analyticsEnabledForAPI) ||
                        (StatisticsConstants.FLOW_STATISTICS_SEQUENCE.equals(event.getComponentType()) &&
                                analyticsEnabledForSequences) ||
                        (StatisticsConstants.FLOW_STATISTICS_ENDPOINT.equals(event.getComponentType()) &&
                                analyticsEnabledForEndpoints) ||
                        (StatisticsConstants.FLOW_STATISTICS_INBOUNDENDPOINT.equals(event.getComponentType()) &&
                                analyticsEnabledForInboundEndpoints) ||
                        (StatisticsConstants.FLOW_STATISTICS_PROXYSERVICE.equals(event.getComponentType()) &&
                                analyticsEnabledForProxyServices)) {
                    if ((StatisticsConstants.FLOW_STATISTICS_SEQUENCE.equals(event.getComponentType()) &&
                            event.getElasticMetadata().getSequence(event.getComponentName()) == null) ||
                            (StatisticsConstants.FLOW_STATISTICS_ENDPOINT.equals(event.getComponentType()) &&
                            event.getElasticMetadata().getEndpoint(event.getComponentName()) == null)) {
                        // If Endpoint or Sequence is null
                        return;
                    }
                    publishAnalytics(event);
                }
            });
        }
    }

    private void publishAnalytics(PublishingEvent event) {
        JsonObject payload = choreoPayload(event);
    }

    private JsonObject choreoPayload (PublishingEvent event) {
        try {
            ElasticMetadata metadata = event.getElasticMetadata();
            JsonObject pL = new JsonObject();
            String type = event.getComponentType();
            ServerConfigurationInformation config = ServiceBusInitializer.getConfigurationInformation();

            // Server related information
            pL.addProperty("serverHostname", (config != null) ? config.getHostName() : null);
            pL.addProperty("serverId", SynapsePropertiesLoader.getPropertyValue(
                    ElasticConstants.SynapseConfigKeys.IDENTIFIER, pL.get("serverHostname").getAsString()));
            pL.addProperty("serverName", (config != null) ? config.getServerName() : null);
            pL.addProperty("serverIpAddress", (config != null) ? config.getIpAddress() : null);

            // Message related information
            // All artifact related messages should contain below information
            pL.addProperty("name", getName(type, event));
            pL.addProperty("timestamp", getTimeStamp(event.getStartTime()));
            pL.addProperty("entityType", event.getComponentType());
            pL.addProperty("faultResponse", metadata.isFaultResponse());
            pL.addProperty("failure", event.getFaultCount() != 0);
            pL.addProperty("latency", event.getDuration());
            pL.addProperty("messageId", metadata.getMessageId());

            if (Objects.equals(type, "API")) {
                // Only APIs have below 4 information
                pL.addProperty("apiSubRequestPath", metadata.getProperty(RESTConstants.REST_SUB_REQUEST_PATH).toString());
                pL.addProperty("apiMethod", metadata.getProperty(RESTConstants.REST_METHOD).toString());
                pL.addProperty("apiContext", metadata.getProperty(RESTConstants.REST_API_CONTEXT).toString());
                pL.addProperty("apiTransport", metadata.getProperty(SynapseConstants.TRANSPORT_IN_NAME).toString());
            } else {
                // Setting N/A because pL (Payload) is required to be common for all 5 artifacts.
                pL.addProperty("apiSubRequestPath", "N/A");
                pL.addProperty("apiMethod", "N/A");
                pL.addProperty("apiContext", "N/A");
                pL.addProperty("apiTransport", "N/A");
            }

            if (metadata.getProperty(CorrelationConstants.CORRELATION_ID) == null &&
                    Boolean.parseBoolean(metadata.getProperty("isInbound").toString())) {
                // Correlation ID is not available for inbound messages. Others should have it.
                // If correlation ID is not available, second condition will verify that it is an inbound message.
                // isInbound is only available for inbound messages.
                pL.addProperty("correlationId", "N/A");
            } else {
                pL.addProperty("correlationId", metadata.getProperty(CorrelationConstants.CORRELATION_ID).toString());
            }
            System.out.println(pL);
            return pL;
        } catch (Exception e) {
            log.error("Error while collecting information for Choreo Statistics Payload. Exception : ", e);
            return null;
        }
    }

    /**
     * Get the name of the artifact.
     *
     * This is to get the name of the artifact from the Publishing flow.
     *
     * @param type This is the type of the artifact.
     * @param event This is the event published by synapse.
     * @return This returns a String which contains the artifact name.
     */
    private String getName(String type, PublishingEvent event) {
        ElasticMetadata metadata = event.getElasticMetadata();
        switch (type) {
            case "API":
                return metadata.getProperty(RESTConstants.SYNAPSE_REST_API).toString();
            case "Endpoint":
                return metadata.getEndpoint(event.getComponentName()).getName();
            case "Sequence":
                return metadata.getSequence(event.getComponentName()).getName();
            case "Proxy Service":
            case "Inbound EndPoint":
                return event.getComponentName();
            default:
                return null;
        }
    }

    private String getTimeStamp(long startTime) {
        if (startTime == 0) {
            return Instant.now().toString();
        }
        return Instant.ofEpochMilli(startTime).toString();
    }
}
