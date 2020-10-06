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
package org.wso2.micro.integrator.business.messaging.hl7.common.data.publisher;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.EventPublishConfigHolder;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.MessageData;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.conf.EventPublisherConfig;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.conf.ServerConfig;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.utils.EventConfigUtil;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.utils.StreamDefUtil;

import java.util.List;
import java.util.Map;

/**
 * This class represents HL7 data publisher
 */
public class HL7EventPublisher {

    public static final String UNDERSCORE = "_";

    private static Log log = LogFactory.getLog(HL7EventPublisher.class);

    private ServerConfig serverConfig;

    public HL7EventPublisher(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void publish(MessageData message) throws HL7Exception {

        List<Object> correlationData = EventConfigUtil.getCorrelationData(message);
        List<Object> metaData = EventConfigUtil.getMetaData(message);
        List<Object> payLoadData = EventConfigUtil.getEventData(message);
        Map<String, String> arbitraryDataMap = EventConfigUtil.getExtractedDataMap(message);
        StreamDefinition streamDef = null;

        try {
            streamDef = StreamDefUtil.getStreamDefinition();
        } catch (MalformedStreamDefinitionException e) {
            log.error("Unable to create HL7 StreamDefinition : " + e.getMessage(), e);
        }
        if (streamDef != null) {
            String key = serverConfig.getUrl() + UNDERSCORE + serverConfig.getUsername() + UNDERSCORE + serverConfig
                    .getPassword();
            EventPublisherConfig eventPublisherConfig = EventPublishConfigHolder.getEventPublisherConfig(key);
            if (serverConfig.isLoadBalancingConfig()) {
                loadBalancerPublisher(eventPublisherConfig, streamDef, key, correlationData, metaData, payLoadData,
                                      arbitraryDataMap);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("single node receiver mode working.");
                }
                if (eventPublisherConfig == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Newly creating publisher configuration.");
                    }
                    synchronized (HL7EventPublisher.class) {
                        eventPublisherConfig = new EventPublisherConfig();
                        DataPublisher asyncDataPublisher;
                        try {
                            if (serverConfig.getSecureUrl() != null) {
                                asyncDataPublisher = new DataPublisher(DataEndpointConstants.THRIFT_DATA_AGENT_TYPE,
                                                                       serverConfig.getUrl(),
                                                                       serverConfig.getSecureUrl(),
                                                                       serverConfig.getUsername(),
                                                                       serverConfig.getPassword());
                            } else {
                                asyncDataPublisher = new DataPublisher(serverConfig.getUrl(),
                                                                       serverConfig.getUsername(),
                                                                       serverConfig.getPassword());
                            }
                        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException | DataEndpointAuthenticationException | TransportException e) {
                            String errorMsg = "Error occurred while creating data publisher";
                            log.error(errorMsg);
                            throw new HL7Exception(errorMsg, e);

                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Created stream definition.");
                        }
                        eventPublisherConfig.setAsyncDataPublisher(asyncDataPublisher);
                        if (log.isDebugEnabled()) {
                            log.debug("Adding config info to map.");
                        }
                        EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                    }
                }
                DataPublisher asyncDataPublisher = eventPublisherConfig.getAsyncDataPublisher();
                asyncDataPublisher.publish(
                        DataBridgeCommonsUtils.generateStreamId(streamDef.getName(), streamDef.getVersion()),
                        getObjectArray(metaData), getObjectArray(correlationData), getObjectArray(payLoadData),
                        arbitraryDataMap);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully published data.");
                }
            }
        }
    }

    private void loadBalancerPublisher(EventPublisherConfig eventPublisherConfig, StreamDefinition streamDef,
                                       String key, List<Object> correlationData, List<Object> metaData,
                                       List<Object> payLoadData, Map<String, String> arbitraryDataMap)
            throws HL7Exception {
        if (log.isDebugEnabled()) {
            log.debug("Load balancing receiver mode working.");
        }
        if (eventPublisherConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("Newly creating publisher configuration.");
            }
            synchronized (HL7EventPublisher.class) {
                eventPublisherConfig = new EventPublisherConfig();
                DataPublisher loadBalancingDataPublisher;
                try {
                    if (serverConfig.getSecureUrl() != null) {
                        loadBalancingDataPublisher = new DataPublisher(DataEndpointConstants.THRIFT_DATA_AGENT_TYPE,
                                                                       serverConfig.getUrl(),
                                                                       serverConfig.getSecureUrl(),
                                                                       serverConfig.getUsername(),
                                                                       serverConfig.getPassword());
                    } else {
                        loadBalancingDataPublisher = new DataPublisher(serverConfig.getUrl(),
                                                                       serverConfig.getUsername(),
                                                                       serverConfig.getPassword());
                    }
                } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException | DataEndpointAuthenticationException | TransportException e) {
                    String errorMsg = "Error occurred while creating data publisher";
                    log.error(errorMsg);
                    throw new HL7Exception(errorMsg, e);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Created stream definition.");
                }
                eventPublisherConfig.setLoadBalancingDataPublisher(loadBalancingDataPublisher);
                if (log.isDebugEnabled()) {
                    log.debug("Adding config info to map.");
                }
                EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
            }
        }
        DataPublisher loadBalancingDataPublisher = eventPublisherConfig.getLoadBalancingDataPublisher();
        loadBalancingDataPublisher.publish(
                DataBridgeCommonsUtils.generateStreamId(streamDef.getName(), streamDef.getVersion()),
                getObjectArray(metaData), getObjectArray(correlationData), getObjectArray(payLoadData),
                arbitraryDataMap);
        if (log.isDebugEnabled()) {
            log.debug("Successfully published data.");
        }
    }

    private Object[] getObjectArray(List<Object> list) {
        if (list.size() > 0) {
            return list.toArray();
        }
        return null;
    }
}
