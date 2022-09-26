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

package org.wso2.carbon.inbound.endpoint.protocol.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.inbound.endpoint.protocol.grpc.util.Event;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Inject gRPC message into the sequence.
 */
public class GRPCInjectHandler {
    private static final Log log = LogFactory.getLog(GRPCInjectHandler.class);
    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    private SynapseEnvironment synapseEnvironment;

    /**
     * constructor initialize parameters and synapseEnvironment
     *
     * @param injectingSeq injecting sequence mentioned in the inbound endpoint
     * @param onErrorSeq error Sequence mentioned in the inbound endpoint
     * @param sequential is sequential
     * @param synapseEnvironment The SynapseEnvironment allows access into the the host SOAP engine. It allows
     *                           the sending of messages, class loader access etc
     */
    GRPCInjectHandler(String injectingSeq, String onErrorSeq, boolean sequential,
                      SynapseEnvironment synapseEnvironment) {
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.sequential = sequential;
        this.synapseEnvironment = synapseEnvironment;
    }

    /**
     * This method will be called when the initiated gRPC call is expecting an response.
     * This will inject the gRPC message to the to the message sequence as well as the responseObserver .
     * inject the message.
     * @param receivedEvent received event from gRPC
     * @param responseObserver object to be used when sending the response back to the gRPC client
     */
    public void invokeProcess(Event receivedEvent, StreamObserver<Event> responseObserver) {
        try {
            org.apache.synapse.MessageContext msgCtx = createMessageContext();
            msgCtx.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER,
                    new GRPCResponseSender(responseObserver));
            initiateSequenceAndInjectPayload(responseObserver, receivedEvent, msgCtx);
        } catch (AxisFault e) {
            log.error("Error while processing the gRPC Message", e);
            throw new SynapseException("Error while processing the gRPC Message", e);
        }
    }

    /**
     * This method will be called when the initiated gRPC call is not expecting an response.
     * This will inject the gRPC message to the to the message sequence as well as the responseObserver .
     * inject the message.
     * @param receivedEvent received event from gRPC
     * @param responseObserver object to be used to send message processing error back to gRPC client
     */
    public void invokeConsume(Event receivedEvent, StreamObserver<Empty> responseObserver) {
        try {
            initiateSequenceAndInjectPayload(responseObserver, receivedEvent, createMessageContext());
        } catch (AxisFault e) {
            log.error("Error while consuming the gRPC Message", e);
            throw new SynapseException("Error while consuming the JMS Message", e);
        }
    }

    private void initiateSequenceAndInjectPayload(StreamObserver responseObserver,
                                                  Event receivedEvent,
                                                  org.apache.synapse.MessageContext msgCtx) throws AxisFault {
        String msgPayload = receivedEvent.getPayload();
        String sequenceName = receivedEvent.getHeadersMap().get(InboundGRPCConstants.HEADER_MAP_SEQUENCE_PARAMETER_NAME);
        SequenceMediator seq;
        if (sequenceName != null) {
            if (log.isDebugEnabled()) {
                log.debug(sequenceName + " sequence, received via gRPC headers.");
            }
            seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(sequenceName);
        } else {
            if (injectingSeq == null || injectingSeq.isEmpty()) {
                log.error("Sequence name is not specified in inbound endpoint or empty.");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug(injectingSeq + " sequence, received via the inbound endpoint.");
            }
            seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(injectingSeq);
        }
        msgCtx.setProperty(SynapseConstants.IS_INBOUND, true);
        //validating the sequence
        if (seq != null) {
            if (!seq.isInitialized()) {
                seq.init(synapseEnvironment);
            }
            seq.setErrorHandler(onErrorSeq);
            if (log.isDebugEnabled()) {
                log.debug("injecting received gRPC message to sequence : " + injectingSeq);
            }
            if (!synapseEnvironment.injectInbound(msgCtx, seq, this.sequential)) {
                return;
            }
        } else {
            log.error("Sequence: " + injectingSeq + " not found");
        }
        MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
        //setting transport headers
        axis2MsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, new HashMap<>(receivedEvent.getHeadersMap()));
        String contentType = receivedEvent.getHeadersMap().
                get(InboundGRPCConstants.HEADER_MAP_CONTENT_TYPE_PARAMETER_NAME);
        if (log.isDebugEnabled()) {
            log.debug(contentType + " Content-Type, received via the gRPC headers.");
        }
        // Determine the message builder to use
        if (contentType != null) {
            if (InboundGRPCConstants.CONTENT_TYPE_JSON.equalsIgnoreCase(contentType)) {
                contentType = InboundGRPCConstants.CONTENT_TYPE_JSON_MIME_TYPE;
            } else if (InboundGRPCConstants.CONTENT_TYPE_XML.equalsIgnoreCase(contentType)) {
                contentType = InboundGRPCConstants.CONTENT_TYPE_XML_MIME_TYPE;
            } else if (InboundGRPCConstants.CONTENT_TYPE_TEXT.equalsIgnoreCase(contentType)) {
                contentType = InboundGRPCConstants.CONTENT_TYPE_TEXT_MIME_TYPE;
            } else {
                log.error("Error occurred when processing gRPC message. " + contentType +
                        " type found in gRPC header is not supported");
                responseObserver.onError(
                        new Throwable("Error occurred when processing gRPC message. " + contentType +
                                " type found in gRPC header is not supported"));
                return;
            }
        } else {
            log.error("Invalid content type found in gRPC header. JSON, XML and text is supported");
            responseObserver.onError(
                    new Throwable("Invalid content type found in gRPC header. JSON, XML and text is supported"));
            return;
        }

        Builder builder = BuilderUtil.getBuilderFromSelector(contentType, axis2MsgCtx);
        OMElement documentElement;
        // set the message payload to the message context
        InputStream in = null;
        try {
            in = new AutoCloseInputStream(new ByteArrayInputStream(msgPayload.getBytes()));
            documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
        } catch (AxisFault ex) {
            // Handle message building error
            log.error("Error while building the message", ex);
            return;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Exception occurred when closing InputStream when reading messagePayload.", e);
                }
            }
        }
        // Inject the message to the sequence.
        msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
    }

    /**
     * Create the initial message context for gRPC
     */
    private org.apache.synapse.MessageContext createMessageContext() {
        org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
        return msgCtx;
    }
}
