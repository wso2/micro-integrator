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
package org.wso2.carbon.inbound.endpoint.inboundfactory;

import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.inbound.InboundRequestProcessorFactory;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericEventBasedConsumer;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericEventBasedListener;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericInboundListener;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpListener;
import org.wso2.carbon.inbound.endpoint.protocol.https.InboundHttpsListener;
import org.wso2.carbon.inbound.endpoint.protocol.httpwebsocket.InboundHttpWebsocketListener;
import org.wso2.carbon.inbound.endpoint.protocol.httpssecurewebsocket.InboundHttpsSecureWebsocketListener;
import org.wso2.carbon.inbound.endpoint.protocol.kafka.KAFKAProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.mqtt.MqttListener;
import org.wso2.carbon.inbound.endpoint.protocol.rabbitmq.RabbitMQListener;
import org.wso2.carbon.inbound.endpoint.protocol.securewebsocket.InboundSecureWebsocketListener;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketListener;
import org.wso2.carbon.inbound.endpoint.protocol.file.VFSProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.grpc.InboundGRPCListener;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.InboundHL7Listener;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSProcessor;

/**
 * Class responsible for providing the implementation of the request processor according to the protocol.
 */
public class InboundRequestProcessorFactoryImpl implements InboundRequestProcessorFactory {

    public static enum Protocols {jms, file, http, https, hl7, kafka, mqtt, rabbitmq, ws, wss, grpc, httpws, httpswss}

    /**
     * return underlying Request Processor Implementation according to protocol
     *
     * @param params parameters specific to transports
     * @return InboundRequestProcessor Implementation
     */
    @Override
    public InboundRequestProcessor createInboundProcessor(InboundProcessorParams params) {
        String protocol = params.getProtocol();
        InboundRequestProcessor inboundRequestProcessor = null;
        if (protocol != null) {
            switch(Protocols.valueOf(protocol)) {
                case jms:
                    inboundRequestProcessor = new JMSProcessor(params);
                    break;
                case file:
                    inboundRequestProcessor = new VFSProcessor(params);
                    break;
                case http:
                    inboundRequestProcessor = new InboundHttpListener(params);
                    break;
                case https:
                    inboundRequestProcessor = new InboundHttpsListener(params);
                    break;
                case ws:
                    inboundRequestProcessor = new InboundWebsocketListener(params);
                    break;
                case wss:
                    inboundRequestProcessor = new InboundSecureWebsocketListener(params);
                    break;
                case hl7:
                    inboundRequestProcessor = new InboundHL7Listener(params);
                    break;
                case kafka:
                    inboundRequestProcessor = new KAFKAProcessor(params);
                    break;
                case mqtt:
                    inboundRequestProcessor = new MqttListener(params);
                    break;
                case rabbitmq:
                    inboundRequestProcessor = new RabbitMQListener(params);
                    break;
                case httpws:
                    inboundRequestProcessor = new InboundHttpWebsocketListener(params);
                    break;
                case httpswss:
                    inboundRequestProcessor = new InboundHttpsSecureWebsocketListener(params);
                    break;
                case grpc:
                    inboundRequestProcessor = new InboundGRPCListener(params);
                    break;
            }
        } else if (params.getClassImpl() != null) {
            if (GenericInboundListener.isListeningInboundEndpoint(params)) {
                inboundRequestProcessor = GenericInboundListener.getInstance(params);
            } else if (GenericEventBasedConsumer.isEventBasedInboundEndpoint(params)) {
                inboundRequestProcessor = new GenericEventBasedListener(params);
            } else {
                inboundRequestProcessor = new GenericProcessor(params);
            }
        } else {
            throw new SynapseException(
                    "Protocol or Class should be specified for Inbound Endpoint " + params.getName());
        }
        return inboundRequestProcessor;
    }
}
