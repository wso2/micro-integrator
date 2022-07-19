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

package org.wso2.micro.integrator.websocket.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketResponseSender;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketSourceHandler;
import org.wso2.micro.integrator.websocket.transport.utils.LogUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

public class WebsocketTransportSender extends AbstractTransportSender {

    private WebsocketConnectionFactory connectionFactory;

    private static final Log log = LogFactory.getLog(WebsocketTransportSender.class);

    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Initializing WS Connection Factory.");
        }
        super.init(cfgCtx, transportOut);
        connectionFactory = new WebsocketConnectionFactory(transportOut);
    }

    public void sendMessage(MessageContext msgCtx, String targetEPR, OutTransportInfo trpOut) throws AxisFault {
        String sourceIdentier = null;
        boolean handshakePresent = false;
        String responceDispatchSequence = null;
        String responceErrorSequence = null;
        String messageType = null;
        boolean isConnectionTerminate = false;
        Map<String, Object> customHeaders = new HashMap<>();

        if (log.isDebugEnabled()) {
            log.debug("Endpoint url: " + targetEPR);
        }
        InboundResponseSender responseSender = null;
        if (msgCtx.getProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER) != null) {
            responseSender = (InboundResponseSender) msgCtx
                    .getProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER);
            if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_CHANNEL_IDENTIFIER) != null) {
                sourceIdentier = msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_CHANNEL_IDENTIFIER).toString();
            } else {
                sourceIdentier = ((ChannelHandlerContext) msgCtx.
                        getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT)).channel().toString();
            }
        } else {
            sourceIdentier = WebsocketConstants.UNIVERSAL_SOURCE_IDENTIFIER;
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDSHAKE_PRESENT) != null && msgCtx
                .getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDSHAKE_PRESENT).equals(true)) {
            handshakePresent = true;
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE) != null) {
            responceDispatchSequence = (String) msgCtx
                    .getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE);
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE) != null) {
            responceErrorSequence = (String) msgCtx
                    .getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE);
        }

        if (msgCtx.getProperty(WebsocketConstants.CONTENT_TYPE) != null) {
            messageType = (String) msgCtx.getProperty(WebsocketConstants.CONTENT_TYPE);
        }

        if (Boolean.valueOf(true).equals(msgCtx.getProperty(WebsocketConstants.CONNECTION_TERMINATE))) {
            isConnectionTerminate = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("sendMessage triggered with sourceChannel: " + sourceIdentier + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                              + Thread.currentThread().getId());
        }

        /*
         * Get all the message property names and check whether the properties with the websocket custom header
         * prefix are exist in the property map.
         *
         * This is used to add new headers to the handshake request. The property format
         * <prefix>.<header>
         *
         * If there is any property with the prefix, extract the header string from the property key and put to the
         * customHeaders map.
         */
        Iterator<String> propertyNames = msgCtx.getPropertyNames();
        String webSocketCustomHeaderPrefix;
        Parameter wsCustomHeaderParam =
                msgCtx.getTransportOut().getParameter(WebsocketConstants.WEBSOCKET_CUSTOM_HEADER_CONFIG);

        // avoid using org.apache.commons.lang.StringUtils due to osgi issue
        if (wsCustomHeaderParam == null || wsCustomHeaderParam.getValue() == null || wsCustomHeaderParam.getValue()
                .toString().isEmpty()) {
            webSocketCustomHeaderPrefix = WebsocketConstants.WEBSOCKET_CUSTOM_HEADER_PREFIX;
        } else {
            webSocketCustomHeaderPrefix = wsCustomHeaderParam.getValue().toString();
        }
        while (propertyNames.hasNext()) {
            String propertyName = propertyNames.next();
            if (propertyName.startsWith(webSocketCustomHeaderPrefix)) {
                Object value = msgCtx.getProperty(propertyName);
                String headerSplits[] = propertyName.split(webSocketCustomHeaderPrefix);
                if (headerSplits.length > 1) {
                    customHeaders.put(headerSplits[1], value);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding Custom Header " + headerSplits[1] + ":" + value);
                    }
                } else {
                    log.warn("A header identified with having only the websocket custom-header prefix"
                            + " as the key (without a unique postfix). Hence dropping the header.");
                }
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching a Connection from the WS(WSS) Connection Factory with sourceChannel : "
                                  + sourceIdentier + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                                  + Thread.currentThread().getId());
            }
            WebSocketClientHandler clientHandler = connectionFactory
                    .getChannelHandler(new URI(targetEPR), sourceIdentier, handshakePresent, responceDispatchSequence,
                                       responceErrorSequence, messageType, isConnectionTerminate, customHeaders,
                                       responseSender, responceDispatchSequence, responceErrorSequence);
            if (clientHandler == null && isConnectionTerminate) {
                if (log.isDebugEnabled()) {
                    log.debug("Backend connection does not exist. No need to send close frame to backend "
                                      + "with sourceChannel : " + sourceIdentier + ", in the Thread,ID: "
                                      + Thread.currentThread().getName() + "," + Thread.currentThread().getId());
                }
                return;
            }
            clientHandler.setTenantDomain(org.wso2.micro.core.Constants.SUPER_TENANT_DOMAIN_NAME);

            if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT) != null && msgCtx
                    .getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT).equals(true)) {
                WebSocketFrame frame = (BinaryWebSocketFrame) msgCtx
                        .getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME);
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Sending the binary frame to the WS server on context id : "
                                          + clientHandler.getChannelHandlerContext().channel().toString());
                    }
                    if (clientHandler.getChannelHandlerContext().channel().isActive()) {
                        clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame.retain());
                        if (log.isDebugEnabled()) {
                            LogUtil.printWebSocketFrame(log, frame, clientHandler.getChannelHandlerContext(), false);
                        }
                    }
                } finally {
                    ReferenceCountUtil.release(frame);
                }
            } else if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT) != null && msgCtx
                    .getProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT).equals(true)) {
                WebSocketFrame frame = (TextWebSocketFrame) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME);
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Sending the passthrough text frame to the WS server on context id: "
                                          + clientHandler.getChannelHandlerContext().channel().toString() + ", "
                                          + ", sourceIdentifier: " + sourceIdentier + ", in the Thread,ID: "
                                          + Thread.currentThread().getName() + "," + Thread.currentThread().getId());
                    }
                    if (clientHandler.getChannelHandlerContext().channel().isActive()) {
                        clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame.retain());
                        if (log.isDebugEnabled()) {
                            LogUtil.printWebSocketFrame(log, frame, clientHandler.getChannelHandlerContext(), false);
                        }
                    }
                } finally {
                    ReferenceCountUtil.release(frame);
                }
            } else if (isConnectionTerminate) {
                if (log.isDebugEnabled()) {
                    log.debug("Sending CloseWebsocketFrame to WS server on context id: "
                                      + clientHandler.getChannelHandlerContext().channel().toString() + ", "
                                      + ", sourceIdentifier: " + sourceIdentier + ", in the Thread,ID: "
                                      + Thread.currentThread().getName() + "," + Thread.currentThread().getId());
                }
                Channel channel = clientHandler.getChannelHandlerContext().channel();
                channel.writeAndFlush(new CloseWebSocketFrame());
                channel.close();
            } else {
                if (!handshakePresent) {
                    RelayUtils.buildMessage(msgCtx, false);
                    OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
                    MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(msgCtx);
                    StringWriter sw = new StringWriter();
                    OutputStream out = new WriterOutputStream(sw, format.getCharSetEncoding());
                    messageFormatter.writeTo(msgCtx, format, out, true);
                    out.close();
                    final String msg = sw.toString();
                    WebSocketFrame frame = new TextWebSocketFrame(msg);
                    if (log.isDebugEnabled()) {
                        log.debug("Sending the text frame to the WS server on context id : " + clientHandler
                                .getChannelHandlerContext().channel().toString());
                    }
                    if (clientHandler.getChannelHandlerContext().channel().isActive()) {
                        clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame.retain());
                        if (log.isDebugEnabled()) {
                            LogUtil.printWebSocketFrame(log, frame, clientHandler.getChannelHandlerContext(), false);
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("AcknowledgeHandshake to WS server on context id: "
                                          + clientHandler.getChannelHandlerContext().channel().toString() + ", "
                                          + ", sourceIdentifier: " + sourceIdentier + ", in the Thread,ID: "
                                          + Thread.currentThread().getName() + "," + Thread.currentThread().getId());
                    }
                    clientHandler.acknowledgeHandshake();
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error parsing the WS endpoint url", e);
        } catch (ConnectException e) {
            handleClientConnectionError(responseSender, e);
        } catch (IOException e) {
            log.error("Error writing to the websocket channel", e);
        } catch (InterruptedException e) {
            log.error("Error writing to the websocket channel", e);
        } catch (XMLStreamException e) {
            handleException("Error while building message", e);
        }
    }

    private void handleClientConnectionError(InboundResponseSender responseSender, Exception e) {

        log.error("Error writing to the websocket channel", e);
        // we will close the client connection and notify with close frame
        InboundWebsocketSourceHandler sourceHandler = ((InboundWebsocketResponseSender) responseSender).getSourceHandler();
        CloseWebSocketFrame closeWebSocketFrame = new CloseWebSocketFrame(WebsocketConstants.WEBSOCKET_UPSTREAM_ERROR_SC,
                e.getMessage());
        try {
            sourceHandler.handleClientWebsocketChannelTermination(closeWebSocketFrame);
        } catch (AxisFault fault) {
            log.error("Error occurred while sending close frames", fault);
        }
    }
}
