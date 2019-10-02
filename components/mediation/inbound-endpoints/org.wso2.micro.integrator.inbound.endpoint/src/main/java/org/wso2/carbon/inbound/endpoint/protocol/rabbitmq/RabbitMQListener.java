/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.protocol.rabbitmq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.common.InboundOneTimeTriggerRequestProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

import java.util.Properties;

/**
 * Listener to get a rabbitmq client from the factory
 */
public class RabbitMQListener extends InboundOneTimeTriggerRequestProcessor {

    private static final String ENDPOINT_POSTFIX = "RABBITMQ" + COMMON_ENDPOINT_POSTFIX;
    private static final Log log = LogFactory.getLog(RabbitMQListener.class);

    private String injectingSeq;
    private String onErrorSeq;

    private Properties rabbitmqProperties;
    private boolean sequential;

    private RabbitMQConnectionFactory rabbitMQConnectionFactory;
    private RabbitMQConnectionConsumer connectionConsumer;
    private RabbitMQInjectHandler injectHandler;
    private InboundProcessorParams params;

    public RabbitMQListener(InboundProcessorParams params) {
        this.name = params.getName();
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
        this.rabbitmqProperties = params.getProperties();
        this.params = params;

        this.sequential = true;
        if (rabbitmqProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL) != null) {
            this.sequential = Boolean
                    .parseBoolean(rabbitmqProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL));
        }

        this.coordination = true;
        if (rabbitmqProperties.getProperty(PollingConstants.INBOUND_COORDINATION) != null) {
            this.coordination = Boolean
                    .parseBoolean(rabbitmqProperties.getProperty(PollingConstants.INBOUND_COORDINATION));
        }

        rabbitMQConnectionFactory = new RabbitMQConnectionFactory(rabbitmqProperties);

        injectHandler = new RabbitMQInjectHandler(injectingSeq, onErrorSeq, sequential, synapseEnvironment);
    }

    @Override
    public void destroy() {
        connectionConsumer.requestShutdown();
        super.destroy();
    }

    @Override
    public void init() {
        log.info("RABBITMQ inbound endpoint " + name + " initializing ...");
        connectionConsumer = new RabbitMQConnectionConsumer(rabbitMQConnectionFactory, rabbitmqProperties,
                                                            injectHandler);
        connectionConsumer.setInboundName(name);
        start();
    }

    public void start() {

        RabbitMQTask rabbitMQTask = new RabbitMQTask(connectionConsumer);
        start(rabbitMQTask, ENDPOINT_POSTFIX);
    }

}
