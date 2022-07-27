/*
 * Copyright (c) (2017-2022), WSO2 Inc. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.elasticsearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.api.API;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingEvent;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.apache.synapse.aspects.flow.statistics.util.StatisticsConstants;
import org.apache.synapse.commons.CorrelationConstants;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.netty.BridgeConstants;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.StatisticsPublisher;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.elasticsearch.schema.ElasticDataSchema;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.elasticsearch.schema.ElasticDataSchemaElement;

import java.util.Map;

public class ElasticStatisticsPublisher implements StatisticsPublisher {
    private static ElasticStatisticsPublisher instance = null;
    private final Log log = LogFactory.getLog(ElasticStatisticsPublisher.class);
    private boolean analyticsDisabledForAPI;
    private boolean analyticsDisabledForSequences;
    private boolean analyticsDisabledForProxyServices;
    private boolean analyticsDisabledForEndpoints;
    private boolean analyticsDisabledForInboundEndpoints;
    protected boolean enabled = false;
    private String analyticsDataPrefix;

    protected ElasticStatisticsPublisher() {
        ElasticDataSchema.init();
        loadConfigurations();
    }

    public static ElasticStatisticsPublisher GetInstance() {
        if (instance == null) {
            instance = new ElasticStatisticsPublisher();
        }
        return instance;
    }

    private void loadConfigurations() {
        analyticsDisabledForAPI = !SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.API_ANALYTICS_ENABLED, true);
        analyticsDisabledForSequences = !SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.SEQUENCE_ANALYTICS_ENABLED, true);
        analyticsDisabledForProxyServices = !SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.PROXY_SERVICE_ANALYTICS_ENABLED, true);
        analyticsDisabledForEndpoints = !SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.ENDPOINT_ANALYTICS_ENABLED, true);
        analyticsDisabledForInboundEndpoints = !SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.INBOUND_ENDPOINT_ANALYTICS_ENABLED, true);
        analyticsDataPrefix = SynapsePropertiesLoader.getPropertyValue(
                ElasticConstants.SynapseConfigKeys.ELASTICSEARCH_PREFIX, ElasticConstants.ELASTIC_DEFAULT_PREFIX);
        enabled = SynapsePropertiesLoader.getBooleanProperty(
                ElasticConstants.SynapseConfigKeys.ELASTICSEARCH_ENABLED, false);
    }

    @Override
    public void process(PublishingFlow publishingFlow, int tenantId) {
        if (!enabled) {
            return;
        }

        publishingFlow.getEvents().forEach(event -> {
            if (event.getMessageContext() == null) {
                return;
            }

            if (StatisticsConstants.FLOW_STATISTICS_API.equals(event.getComponentType())) {
                publishApiAnalytics(event);
            } else if (StatisticsConstants.FLOW_STATISTICS_SEQUENCE.equals(event.getComponentType())) {
                publishSequenceMediatorAnalytics(event);
            } else if (StatisticsConstants.FLOW_STATISTICS_ENDPOINT.equals(event.getComponentType())) {
                publishEndpointAnalytics(event);
            } else if (StatisticsConstants.FLOW_STATISTICS_INBOUNDENDPOINT.equals(event.getComponentType())) {
                publishInboundEndpointAnalytics(event);
            } else if (StatisticsConstants.FLOW_STATISTICS_PROXYSERVICE.equals(event.getComponentType())) {
                publishProxyServiceAnalytics(event);
            }
        });
    }

    void publishAnalytic(ElasticDataSchemaElement payload) {
        ElasticDataSchema dataSchemaInst = new ElasticDataSchema(payload);
        log.info(String.format("%s %s", analyticsDataPrefix, dataSchemaInst.getJsonString()));
    }

    private void publishApiAnalytics(PublishingEvent event) {
        if (analyticsDisabledForAPI) {
            return;
        }

        MessageContext synCtx = event.getMessageContext();
        ElasticDataSchemaElement analyticPayload = generateAnalyticsObject(event, API.class);

        ElasticDataSchemaElement apiDetails = new ElasticDataSchemaElement();
        apiDetails.setAttribute(ElasticConstants.EnvelopDef.API,
                synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        apiDetails.setAttribute(ElasticConstants.EnvelopDef.SUB_REQUEST_PATH,
                synCtx.getProperty(RESTConstants.REST_SUB_REQUEST_PATH));
        apiDetails.setAttribute(ElasticConstants.EnvelopDef.API_CONTEXT,
                synCtx.getProperty(RESTConstants.REST_API_CONTEXT));
        apiDetails.setAttribute(ElasticConstants.EnvelopDef.METHOD,
                synCtx.getProperty(RESTConstants.REST_METHOD));
        apiDetails.setAttribute(ElasticConstants.EnvelopDef.TRANSPORT,
                synCtx.getProperty(SynapseConstants.TRANSPORT_IN_NAME));
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.API_DETAILS, apiDetails);
        attachHttpProperties(analyticPayload, synCtx);

        publishAnalytic(analyticPayload);
    }

    private void publishSequenceMediatorAnalytics(PublishingEvent event) {
        if (analyticsDisabledForSequences) {
            return;
        }

        MessageContext synCtx = event.getMessageContext();
        SequenceMediator sequence = (SequenceMediator) synCtx.getSequence(event.getComponentName());

        if (sequence == null) {
            return;
        }

        ElasticDataSchemaElement analyticsPayload = generateAnalyticsObject(event, SequenceMediator.class);
        ElasticDataSchemaElement sequenceDetails = new ElasticDataSchemaElement();
        sequenceDetails.setAttribute(ElasticConstants.EnvelopDef.SEQUENCE_NAME, sequence.getName());
        analyticsPayload.setAttribute(ElasticConstants.EnvelopDef.SEQUENCE_DETAILS, sequenceDetails);
        publishAnalytic(analyticsPayload);
    }

    private void publishProxyServiceAnalytics(PublishingEvent event) {
        if (analyticsDisabledForProxyServices) {
            return;
        }

        MessageContext synCtx = event.getMessageContext();
        ElasticDataSchemaElement analyticsPayload = generateAnalyticsObject(event, ProxyService.class);
        analyticsPayload.setAttribute(ElasticConstants.EnvelopDef.PROXY_SERVICE_TRANSPORT,
                synCtx.getProperty(SynapseConstants.TRANSPORT_IN_NAME));
        analyticsPayload.setAttribute(ElasticConstants.EnvelopDef.PROXY_SERVICE_IS_DOING_REST,
                synCtx.getProperty(SynapseConstants.IS_CLIENT_DOING_REST));
        analyticsPayload.setAttribute(ElasticConstants.EnvelopDef.PROXY_SERVICE_IS_DOING_SOAP11,
                synCtx.getProperty(SynapseConstants.IS_CLIENT_DOING_SOAP11));

        ElasticDataSchemaElement proxyServiceDetails = new ElasticDataSchemaElement();
        proxyServiceDetails.setAttribute(ElasticConstants.EnvelopDef.PROXY_SERVICE_NAME, event.getComponentName());
        analyticsPayload.setAttribute(ElasticConstants.EnvelopDef.PROXY_SERVICE_DETAILS, proxyServiceDetails);
        attachHttpProperties(analyticsPayload, synCtx);

        publishAnalytic(analyticsPayload);
    }

    private void publishEndpointAnalytics(PublishingEvent event) {
        if (analyticsDisabledForEndpoints) {
            return;
        }

        MessageContext synCtx = event.getMessageContext();
        Endpoint endpoint = synCtx.getEndpoint(event.getComponentName());

        if (endpoint == null) {
            return;
        }

        ElasticDataSchemaElement analyticsPayload = generateAnalyticsObject(event, Endpoint.class);
        ElasticDataSchemaElement endpointDetails = new ElasticDataSchemaElement();
        endpointDetails.setAttribute(ElasticConstants.EnvelopDef.ENDPOINT_NAME, endpoint.getName());
        analyticsPayload.setAttribute(ElasticConstants.EnvelopDef.ENDPOINT_DETAILS, endpointDetails);

        publishAnalytic(analyticsPayload);
    }

    private void publishInboundEndpointAnalytics(PublishingEvent event) {
        if (analyticsDisabledForInboundEndpoints) {
            return;
        }

        MessageContext synCtx = event.getMessageContext();
        ElasticDataSchemaElement analyticsPayload = generateAnalyticsObject(event, InboundEndpoint.class);

        ElasticDataSchemaElement inboundEndpointDetails = new ElasticDataSchemaElement();
        inboundEndpointDetails.setAttribute(
                ElasticConstants.EnvelopDef.INBOUND_ENDPOINT_NAME, event.getComponentName());
        analyticsPayload.setAttribute(
                ElasticConstants.EnvelopDef.INBOUND_ENDPOINT_DETAILS, inboundEndpointDetails);
        attachHttpProperties(analyticsPayload, synCtx);

        publishAnalytic(analyticsPayload);
    }

    private ElasticDataSchemaElement generateAnalyticsObject(PublishingEvent event, Class<?> entityClass) {
        MessageContext synCtx = event.getMessageContext();
        ElasticDataSchemaElement analyticPayload = new ElasticDataSchemaElement();
        analyticPayload.setStartTime(event.getStartTime());
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.ENTITY_TYPE, entityClass.getSimpleName());
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.ENTITY_CLASS_NAME, entityClass.getName());
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.FAULT_RESPONSE, synCtx.isFaultResponse());
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.FAILURE, event.getFaultCount() != 0);
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.MESSAGE_ID, synCtx.getMessageID());
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.CORRELATION_ID,
                synCtx.getProperty(CorrelationConstants.CORRELATION_ID));
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.LATENCY, event.getDuration());

        ElasticDataSchemaElement metadata = new ElasticDataSchemaElement();
        analyticPayload.setAttribute(ElasticConstants.EnvelopDef.METADATA, metadata);
        Axis2MessageContext axis2mc = (Axis2MessageContext) synCtx;
        if (axis2mc.getAnalyticsMetadata() == null) {
            return analyticPayload;
        }

        for (Map.Entry<String, Object> entry : axis2mc.getAnalyticsMetadata().entrySet()) {
            if (entry.getValue() == null) {
                continue; // Logstash fails at null
            }
            metadata.setAttribute(entry.getKey(), entry.getValue());
        }

        return analyticPayload;
    }

    private void attachHttpProperties(ElasticDataSchemaElement payload, MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        if (axisCtx == null) {
            return;
        }

        payload.setAttribute(ElasticConstants.EnvelopDef.REMOTE_HOST,
                axisCtx.getProperty(BridgeConstants.REMOTE_HOST));
        payload.setAttribute(ElasticConstants.EnvelopDef.CONTENT_TYPE,
                axisCtx.getProperty(BridgeConstants.CONTENT_TYPE_HEADER));
        payload.setAttribute(ElasticConstants.EnvelopDef.HTTP_METHOD,
                axisCtx.getProperty(BridgeConstants.HTTP_METHOD));
    }

}
