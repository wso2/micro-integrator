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
package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * Polling consumer for NATS to initialize connection and poll messages.
 */
public class NatsMessageConsumer {

    private static final Log log = LogFactory.getLog(NatsMessageConsumer.class.getName());
    private NatsInjectHandler injectHandler;
    private Properties natsProperties;
    private NatsMessageListener natsMessageListener;
    private String subject;
    private String inboundEndpointName;

    public NatsMessageConsumer(Properties natsProperties, String inboundEndpointName) {
        this.natsProperties = natsProperties;
        this.inboundEndpointName = inboundEndpointName;
        this.subject = natsProperties.getProperty(NatsConstants.SUBJECT);
        if (subject == null)
            throw new SynapseException("NATS subject cannot be null.");
    }

    /**
     * Initialize the message listener to use (Core NATS or NATS Streaming).
     */
    public void initializeMessageListener() {
        if (log.isDebugEnabled()) {
            log.debug("Create the NATS message listener.");
        }
        if (Boolean.parseBoolean(natsProperties.getProperty(NatsConstants.NATS_STREAMING))) {
            natsMessageListener = new StreamingListener(subject, injectHandler, natsProperties);
            return;
        }
        natsMessageListener = new CoreListener(subject, injectHandler, natsProperties);
    }

    /**
     * Create the NATS connection and poll messages.
     */
    public void initializeConsumer() throws IOException, InterruptedException, TimeoutException {
        natsMessageListener.initializeConsumer(inboundEndpointName);
    }

    public void closeConnection() {
        if (log.isDebugEnabled()) {
            log.debug("Closing NATS connection");
        }
        natsMessageListener.closeConnection();
    }

    /**
     * Register a handler to implement injection of the retrieved message.
     *
     * @param injectHandler the injectHandler
     */
    public void registerHandler(NatsInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }
}
