/*
 * Copyright 2020 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.nats.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;

import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;
import org.wso2.carbon.inbound.endpoint.protocol.nats.NatsConstants;
import org.wso2.carbon.inbound.endpoint.protocol.nats.NatsInjectHandler;
import org.wso2.carbon.inbound.endpoint.protocol.nats.NatsMessageConsumer;

import java.io.IOException;
import java.util.Properties;

/**
 * Manager which handles NATS Listeners activities for Inbound Endpoints. This is the central place to mange Http Listeners
 * for Inbound endpoints
 */
public class NatsEndpointManager extends AbstractInboundEndpointManager {

    private static final Log log = LogFactory.getLog(NatsEndpointManager.class);
    private static NatsEndpointManager instance = null;
    private NatsMessageConsumer messageConsumer = null;

    public static NatsEndpointManager getInstance() {
        if (instance == null)
            instance = new NatsEndpointManager();
        return instance;
    }

    /**
     * Start NATS Listener
     * @param port  port (not needed for NATS Inbound Endpoint)
     * @param name  endpoint name
     * @param inboundParameters inbound endpoint params
     */
    @Override public boolean startListener(int port, String name, InboundProcessorParams inboundParameters) {
        try {
            messageConsumer.consumeMessage();
        } catch (IOException | InterruptedException e) {
            log.error("An error occurred while connecting to NATS server or consuming messages. " + e);
            messageConsumer.closeConnection();
            return true;
        } catch (SynapseException e) {
            log.error("Error while retrieving or injecting NATS message. " + e.getMessage(), e);
            return true;
        } catch (Exception e) {
            log.error("Error while retrieving or injecting NATS message. " + e.getMessage(), e);
            messageConsumer.closeConnection();
            return true;
        }
        return true;
    }

    /**
     * Start NATS Inbound endpoint
     * @param port  port (not needed for NATS Inbound Endpoint)
     * @param name  endpoint name
     * @param params inbound endpoint params
     */
    @Override public boolean startEndpoint(int port, String name, InboundProcessorParams params) {
        Properties natsProperties = params.getProperties();
        messageConsumer = new NatsMessageConsumer(natsProperties, name);
        boolean sequential = true;
        String inboundEndpointSequential = natsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL);
        if (inboundEndpointSequential != null) {
            sequential = Boolean.parseBoolean(inboundEndpointSequential);
        }
        messageConsumer.registerHandler(
                new NatsInjectHandler(params.getInjectingSeq(), params.getOnErrorSeq(), sequential,
                        params.getSynapseEnvironment(), natsProperties.getProperty(NatsConstants.CONTENT_TYPE)));
        messageConsumer.initializeMessageListener();
        startListener(port, name, params);
        return false;
    }

    /**
     * Stop Inbound Endpoint
     * @param port  port of the endpoint (not needed for NATS Inbound Endpoint)
     */
    @Override public void closeEndpoint(int port) {
        messageConsumer.closeConnection();
    }

    public NatsMessageConsumer getMessageConsumer() {
        return messageConsumer;
    }
}
