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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.grpc.response.mediator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.micro.integrator.inbound.endpoint.protocol.grpc.GRPCResponseObserverWrapper;
import org.wso2.micro.integrator.inbound.endpoint.protocol.grpc.InboundGrpcConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.apache.axis2.Constants.Configuration.MESSAGE_TYPE;
import static org.wso2.micro.integrator.inbound.endpoint.protocol.grpc.InboundGrpcConstants.CONTENT_TYPE_JSON_MIME_TYPE;

/**
 * Mediator that extracts data and the gRPC responseObserver from current message payload/header
 * according to the given configuration.
 * Extracted information is sent as an gRPC event.
 */
public class ResponseMediator extends AbstractMediator {
    private static final Log log = LogFactory.getLog(ResponseMediator.class);
    public boolean mediate(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("gRPC response mediator initiated.");
        }
        GRPCResponseObserverWrapper responseObserver =
                (GRPCResponseObserverWrapper) messageContext.getProperty(InboundGrpcConstants.GRPC_RESPONSE_OBSERVER);
        if (responseObserver != null) {
            org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            String content;
            String contentType = msgContext.getProperty(MESSAGE_TYPE).toString();
            if (log.isDebugEnabled()) {
                log.debug("Message content type retrieved in the message is: " + contentType);
            }
            if (contentType.equalsIgnoreCase(CONTENT_TYPE_JSON_MIME_TYPE)) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(JsonUtil.getJsonPayload(msgContext)));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    content = stringBuilder.toString();
                } catch (IOException e) {
                    String msg = "Error occurred while converting payload to json. " + e.getMessage();
                    log.error(msg, e);
                    throw new SynapseException(msg, e);
                }
            } else if (contentType.equalsIgnoreCase(InboundGrpcConstants.CONTENT_TYPE_XML_MIME_TYPE) ||
                    contentType.equalsIgnoreCase(InboundGrpcConstants.CONTENT_TYPE_TEXT_MIME_TYPE)) {
                content = msgContext.getEnvelope().getBody().toString();
            } else {
                String msg = "Error occurred when sending response. " + contentType + " type not supported";
                log.error(msg);
                throw new SynapseException(msg);
            }
            if (log.isDebugEnabled()) {
                log.debug("Extracted content: " + content);
            }
            responseObserver.sendResponse(content);
        } else {
            String msg = "Message context doesn't contain gRPC Response Observer. " +
                    "Please make sure the gRPC call accepts a response ";
            log.error(msg);
            throw new SynapseException(msg);
        }
        return true;
    }
}
