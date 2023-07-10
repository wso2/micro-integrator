package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.choreo;

import com.google.gson.Gson;
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
                    publishAnalytics(event);
                }
            });
        }

    }

    private void publishAnalytics(PublishingEvent event) {
        ElasticMetadata metadata = event.getElasticMetadata();
        JsonObject pL = new JsonObject();

        ServerConfigurationInformation config = ServiceBusInitializer.getConfigurationInformation();

        pL.addProperty("serverHostname", (config != null) ? config.getHostName() : null);
        pL.addProperty("serverId", SynapsePropertiesLoader.getPropertyValue(
                ElasticConstants.SynapseConfigKeys.IDENTIFIER, pL.get("serverHostname").getAsString()));
        pL.addProperty("serverName", (config != null) ? config.getServerName() : null);
        pL.addProperty("serverIpAddress", (config != null) ? config.getIpAddress() : null);
        pL.addProperty("name", metadata.getProperty(RESTConstants.SYNAPSE_REST_API).toString());
        pL.addProperty("apiSubRequestPath", metadata.getProperty(RESTConstants.REST_SUB_REQUEST_PATH).toString());
        pL.addProperty("apiMethod", metadata.getProperty(RESTConstants.REST_METHOD).toString());
        pL.addProperty("apiContext", metadata.getProperty(RESTConstants.REST_API_CONTEXT).toString());
        pL.addProperty("apiTransport", metadata.getProperty(SynapseConstants.TRANSPORT_IN_NAME).toString());
        pL.addProperty("timestamp", event.getStartTime());
        pL.addProperty("entityType", event.getComponentType());
        pL.addProperty("faultResponse", metadata.isFaultResponse());
        pL.addProperty("failure", event.getFaultCount() != 0);
        pL.addProperty("latency", event.getDuration());
        pL.addProperty("correlationId", metadata.getProperty(CorrelationConstants.CORRELATION_ID).toString());
        pL.addProperty("messageId", metadata.getMessageId());

        System.out.println(pL);
    }
}
