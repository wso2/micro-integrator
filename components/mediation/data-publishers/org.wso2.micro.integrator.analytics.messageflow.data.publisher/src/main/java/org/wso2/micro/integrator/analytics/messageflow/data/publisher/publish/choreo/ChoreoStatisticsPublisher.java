/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.choreo;

import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.Objects;

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
import org.wso2.micro.integrator.initializer.ServiceBusInitializer;

/**
 * This class is used to publish statistics to Choreo.
 *
 * How to enable Choreo Analytics
 *
 * 1. Add the following to the deployment.toml file.
 *
 * i. Enabling statistics for artifacts
 * [mediation]
 * flow.statistics.enable=true
 * flow.statistics.capture_all=true
 *
 * ii. Enabling Choreo Analytics
 * [analytics]
 * enabled=true
 * publisher="choreo"
 *
 * **Adding publisher is required. If not added, the default publisher will be used which is ELK flow**
*
* 2. Optionally, you can add the following to the deployment.toml file.
*
* [analytics]
* api_analytics.enabled = true
* proxy_service_analytics.enabled = true
* sequence_analytics.enabled = true
* endpoint_analytics.enabled = true
* inbound_endpoint_analytics.enabled = true
*
* Note that default value for above 5 will be true (If not added). If you want to disable analytics for any of the
* above, change the value to false.
*
* 3. Restart the server.
*
* Special Note - This is a WIP code so publishing to Choreo is not fully implemented.
*/
public class ChoreoStatisticsPublisher implements StatisticsPublisher {
    private static ChoreoStatisticsPublisher instance = null;
    private final Log log = LogFactory.getLog(ChoreoStatisticsPublisher.class);

    //These are the flags to disable analytics for each component
    private boolean isAPIAnalyticsEnabled;
    private boolean isSequenceAnalyticsEnabled;
    private boolean isProxyServiceAnalyticsEnabled;
    private boolean isEndpointAnalyticsEnabled;
    private boolean isInboundEndpointAnalyticsEnabled;

    private boolean isAnalyticsEnabled;

    public static ChoreoStatisticsPublisher GetInstance() {
        if (instance == null) {
            instance = new ChoreoStatisticsPublisher();
        }
        return instance;
    }

    /**
     * Load Configurations.
     *
     * This method is used to load configurations from synapse.properties file.
     */
    private void loadConfigurations() {
        isAPIAnalyticsEnabled = SynapsePropertiesLoader.getBooleanProperty(
                ChoreoConstants.SynapseConfigKeys.API_ANALYTICS_ENABLED, true);
        isSequenceAnalyticsEnabled = SynapsePropertiesLoader.getBooleanProperty(
                ChoreoConstants.SynapseConfigKeys.SEQUENCE_ANALYTICS_ENABLED, true);
        isProxyServiceAnalyticsEnabled = SynapsePropertiesLoader.getBooleanProperty(
                ChoreoConstants.SynapseConfigKeys.PROXY_SERVICE_ANALYTICS_ENABLED, true);
        isEndpointAnalyticsEnabled = SynapsePropertiesLoader.getBooleanProperty(
                ChoreoConstants.SynapseConfigKeys.ENDPOINT_ANALYTICS_ENABLED, true);
        isInboundEndpointAnalyticsEnabled = SynapsePropertiesLoader.getBooleanProperty(
                ChoreoConstants.SynapseConfigKeys.INBOUND_ENDPOINT_ANALYTICS_ENABLED, true);
        isAnalyticsEnabled = SynapsePropertiesLoader.getBooleanProperty(
                ChoreoConstants.SynapseConfigKeys.ANALYTICS_ENABLED, false);
    }

    protected ChoreoStatisticsPublisher() {
        loadConfigurations();
    }

    /**
     * Process Method of data publisher.
     *
     * In this,
     * It will be checked if analytics is enabled by configuration.
     * Then, it will be checked if the artifact's analytics is disabled by configuration.
     * Then, if artifact is Endpoint or Sequence, it will be checked if the artifact is null.
     *
     * @param publishingFlow The publishing flow
     * @param tenantId       The tenant id
     */
    @Override
    public void process(PublishingFlow publishingFlow, int tenantId) {
        if (!isAnalyticsEnabled) {
            return;
        }
        if (publishingFlow.getEvents() != null && publishingFlow.getEvents().toArray().length > 0) {
            publishingFlow.getEvents().forEach(event -> {
                if (event.getElasticMetadata() == null || !event.getElasticMetadata().isValid()) {
                    return;
                }
                if (StatisticsConstants.FLOW_STATISTICS_API.equals(event.getComponentType())) {
                    if (!isAPIAnalyticsEnabled) {
                        return;
                    }
                    publishAnalytics(event);
                }
                if (StatisticsConstants.FLOW_STATISTICS_SEQUENCE.equals(event.getComponentType())) {
                    if (!isSequenceAnalyticsEnabled ||
                            event.getElasticMetadata().getSequence(event.getComponentName()) == null) {
                        return;
                    }
                    publishAnalytics(event);
                }
                if (StatisticsConstants.FLOW_STATISTICS_ENDPOINT.equals(event.getComponentType())) {
                    if (!isEndpointAnalyticsEnabled ||
                            event.getElasticMetadata().getEndpoint(event.getComponentName()) == null) {
                        return;
                    }
                    publishAnalytics(event);
                }
                if (StatisticsConstants.FLOW_STATISTICS_INBOUNDENDPOINT.equals(event.getComponentType())) {
                    if (!isInboundEndpointAnalyticsEnabled) {
                        return;
                    }
                    publishAnalytics(event);
                }
                if (StatisticsConstants.FLOW_STATISTICS_PROXYSERVICE.equals(event.getComponentType())) {
                    if (!isProxyServiceAnalyticsEnabled) {
                        return;
                    }
                    publishAnalytics(event);
                }
            });
        }
    }

    /**
     * Publish the analytics.
     *
     * Publish the analytics to Event Hub (For Choreo Analytics).
     *
     * @param event This is the event published by synapse.
     */
    private void publishAnalytics(PublishingEvent event) {
        JsonObject payload = generateChoreoPayload(event);
        // In here, publishing data to event hub should be handled.
        // For example,
        // builder.addAttribute(entry.getKey(), entry.getValue());
        // counterMetric.incrementCount(builder);
        // Where entry has assigned by payload data.
        // https://github.com/wso2/apim-analytics-publisher handles API-M analytics publishing for Choreo.
        // Should be able to reuse the same method to publish data to event hub with some changes.
        log.info("Choreo Analytics is enabled. Payload: " + payload);
    }

    /**
     * Generate the payload.
     *
     * Generate the payload to send to Event Hub (For Choreo Analytics).
     *
     * @param event This is the event published by synapse.
     * @return This returns a JsonObject which contains the required flat Json by Choreo.
     */
    private JsonObject generateChoreoPayload (PublishingEvent event) {
        try {
            ElasticMetadata metadata = event.getElasticMetadata();
            JsonObject payload = new JsonObject();
            String type = event.getComponentType();
            ServerConfigurationInformation config = ServiceBusInitializer.getConfigurationInformation();

            // Server related information
            payload.addProperty(ChoreoConstants.PayloadKeys.SERVER_HOST_NAME,
                    (config != null) ? config.getHostName() : null);
            payload.addProperty(ChoreoConstants.PayloadKeys.SERVER_ID, SynapsePropertiesLoader.getPropertyValue(
                    ChoreoConstants.SynapseConfigKeys.IDENTIFIER,
                    payload.get(ChoreoConstants.PayloadKeys.SERVER_HOST_NAME).getAsString()));
            payload.addProperty(ChoreoConstants.PayloadKeys.SERVER_NAME,
                    (config != null) ? config.getServerName() : null);
            payload.addProperty(ChoreoConstants.PayloadKeys.SERVER_IP_ADDRESS,
                    (config != null) ? config.getIpAddress() : null);

            // Message related information
            // All artifact related messages should contain below information
            payload.addProperty(ChoreoConstants.PayloadKeys.NAME, getName(type, event));
            payload.addProperty(ChoreoConstants.PayloadKeys.TIMESTAMP, getTimeStamp(event.getStartTime()));
            payload.addProperty(ChoreoConstants.PayloadKeys.ENTITY_TYPE, event.getComponentType());
            payload.addProperty(ChoreoConstants.PayloadKeys.FAULT_RESPONSE, metadata.isFaultResponse());
            payload.addProperty(ChoreoConstants.PayloadKeys.FAILURE, event.getFaultCount() != 0);
            payload.addProperty(ChoreoConstants.PayloadKeys.LATENCY, event.getDuration());
            payload.addProperty(ChoreoConstants.PayloadKeys.MESSAGE_ID, metadata.getMessageId());

            if (Objects.equals(type, ChoreoConstants.ArtifactType.API)) {
                // Only APIs have below 4 information
                payload.addProperty(ChoreoConstants.PayloadKeys.API_SUB_REQUEST_PATH, metadata
                        .getProperty(RESTConstants.REST_SUB_REQUEST_PATH).toString());
                payload.addProperty(ChoreoConstants.PayloadKeys.API_METHOD, metadata
                        .getProperty(RESTConstants.REST_METHOD).toString());
                payload.addProperty(ChoreoConstants.PayloadKeys.API_CONTEXT, metadata.
                        getProperty(RESTConstants.REST_API_CONTEXT).toString());
                payload.addProperty(ChoreoConstants.PayloadKeys.API_TRANSPORT, metadata
                        .getProperty(SynapseConstants.TRANSPORT_IN_NAME).toString());
            } else {
                // Setting N/A because pL (Payload) is required to be common for all 5 artifacts.
                payload.addProperty(ChoreoConstants.PayloadKeys.API_SUB_REQUEST_PATH, ChoreoConstants.NOT_APPLICABLE);
                payload.addProperty(ChoreoConstants.PayloadKeys.API_METHOD, ChoreoConstants.NOT_APPLICABLE);
                payload.addProperty(ChoreoConstants.PayloadKeys.API_CONTEXT, ChoreoConstants.NOT_APPLICABLE);
                payload.addProperty(ChoreoConstants.PayloadKeys.API_TRANSPORT, ChoreoConstants.NOT_APPLICABLE);
            }

            if (metadata.getProperty(CorrelationConstants.CORRELATION_ID) == null &&
                    Boolean.parseBoolean(metadata.getProperty(ChoreoConstants.IS_INBOUND).toString())) {
                // Correlation ID is not available for inbound messages. Others should have it.
                // If correlation ID is not available, second condition will verify that it is an inbound message.
                // isInbound is only available for inbound messages.
                payload.addProperty(ChoreoConstants.PayloadKeys.CORRELATION_ID, ChoreoConstants.NOT_APPLICABLE);
            } else {
                payload.addProperty(ChoreoConstants.PayloadKeys.CORRELATION_ID,
                        metadata.getProperty(CorrelationConstants.CORRELATION_ID).toString());
            }
            return payload;
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
            case ChoreoConstants.ArtifactType.API:
                return metadata.getProperty(RESTConstants.SYNAPSE_REST_API).toString();
            case ChoreoConstants.ArtifactType.ENDPOINT:
                return metadata.getEndpoint(event.getComponentName()).getName();
            case ChoreoConstants.ArtifactType.SEQUENCE:
                return metadata.getSequence(event.getComponentName()).getName();
            case ChoreoConstants.ArtifactType.PROXY:
            case ChoreoConstants.ArtifactType.INBOUND_ENDPOINT:
                return event.getComponentName();
            default:
                return null;
        }
    }

    /**
     * Get the starting timestamp of the artifact.
     *
     * Timestamp comes as a long (In Unix timestamp). Using this method, it can be converted to  ISO 8601 format.
     *
     * @param startTime This is the event starting time.
     * @return This returns a String which contains the time in ISO 8601 format.
     */
    private String getTimeStamp(long startTime) {
        if (startTime == 0) {
            return Instant.now().toString();
        }
        return Instant.ofEpochMilli(startTime).toString();
    }
}
