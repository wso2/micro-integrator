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

package org.wso2.carbon.inbound.endpoint.protocol.websocket;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.management.WebsocketEndpointManager;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.management.WebsocketSubscriberPathManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamException;

public class InboundWebsocketResponseSender implements InboundResponseSender {

    private Log log = LogFactory.getLog(InboundWebsocketResponseSender.class);
    private InboundWebsocketSourceHandler sourceHandler;

    public InboundWebsocketResponseSender(InboundWebsocketSourceHandler sourceHandler) {
        this.sourceHandler = sourceHandler;
    }

    public InboundWebsocketSourceHandler getSourceHandler() {
        return sourceHandler;
    }

    @Override
    public void sendBack(MessageContext msgContext) {
        String defaultContentType = sourceHandler.getDefaultContentType();
        if (msgContext != null) {
            Integer errorCode = null;
            String errorMessage = null;
            if (msgContext.getProperty("errorCode") != null) {
                errorCode = Integer.parseInt(msgContext.getProperty("errorCode").toString());
            }
            if (msgContext.getProperty("ERROR_MESSAGE") != null) {
                errorMessage = msgContext.getProperty("ERROR_MESSAGE").toString();
            }
            try {
                if (errorCode != null && errorMessage != null) {
                    CloseWebSocketFrame closeWebSocketFrame = new CloseWebSocketFrame(errorCode, errorMessage);
                    if (log.isDebugEnabled()) {
                        String customErrorMessage = "errorCode:" + errorCode + " error message: "
                                + errorMessage;
                        WebsocketLogUtil.printWebSocketFrame(log, closeWebSocketFrame,
                                                             sourceHandler.getChannelHandlerContext().getChannelHandlerContext(),
                                                             customErrorMessage, false);
                    }
                    sourceHandler.handleClientWebsocketChannelTermination(closeWebSocketFrame);
                }
            } catch (AxisFault fault) {
                log.error("Error occurred while sending close frames", fault);
            }
            Object isTCPTransport = ((Axis2MessageContext) msgContext).getAxis2MessageContext()
                    .getProperty(InboundWebsocketConstants.IS_TCP_TRANSPORT);
            if (msgContext.getProperty(InboundWebsocketConstants.SOURCE_HANDSHAKE_PRESENT) != null && msgContext
                    .getProperty(InboundWebsocketConstants.SOURCE_HANDSHAKE_PRESENT).equals(true)) {
                return;
            } else if (msgContext.getProperty(InboundWebsocketConstants.WEBSOCKET_TARGET_HANDSHAKE_PRESENT) != null
                    && msgContext.getProperty(InboundWebsocketConstants.WEBSOCKET_TARGET_HANDSHAKE_PRESENT)
                    .equals(true)) {
                if (msgContext.getProperty(InboundWebsocketConstants.WEBSOCKET_TARGET_HANDLER_CONTEXT) != null) {
                    ChannelHandlerContext targetCtx = (ChannelHandlerContext) msgContext
                            .getProperty(InboundWebsocketConstants.WEBSOCKET_TARGET_HANDLER_CONTEXT);
                    if (log.isDebugEnabled()) {
                        WebsocketLogUtil.printWebSocketFrame(log, new CloseWebSocketFrame(), targetCtx,
                                false);
                    }
                    sourceHandler.getChannelHandlerContext().addCloseListener(targetCtx);
                }
                return;
            } else if (msgContext.getProperty(InboundWebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT) != null
                    && msgContext.getProperty(InboundWebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT).equals(true)) {
                BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msgContext
                        .getProperty(InboundWebsocketConstants.WEBSOCKET_BINARY_FRAME);
                if (isTCPTransport != null && (boolean) isTCPTransport) {
                    try {
                        RelayUtils.buildMessage(((Axis2MessageContext) msgContext).getAxis2MessageContext(), false);
                        if (defaultContentType != null && defaultContentType
                                .startsWith(InboundWebsocketConstants.BINARY)) {
                            org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) msgContext)
                                    .getAxis2MessageContext();
                            MessageFormatter messageFormatter = BaseUtils.getMessageFormatter(msgCtx);
                            OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
                            byte[] message = messageFormatter.getBytes(msgCtx, format);
                            frame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(message));
                            InboundWebsocketChannelContext ctx = sourceHandler.getChannelHandlerContext();
                            int clientBroadcastLevel = sourceHandler.getClientBroadcastLevel();
                            String subscriberPath = sourceHandler.getSubscriberPath();
                            WebsocketSubscriberPathManager pathManager = WebsocketSubscriberPathManager.getInstance();
                            if (log.isDebugEnabled()) {
                                WebsocketLogUtil.printWebSocketFrame(log, frame,
                                        ctx.getChannelHandlerContext(), false);
                            }
                            handleSendBack(frame, ctx, clientBroadcastLevel, subscriberPath, pathManager);
                            return;
                        }
                    } catch (XMLStreamException ex) {
                        log.error("Error while building message", ex);
                    } catch (IOException ex) {
                        log.error("Failed for format message to specified output format", ex);
                    }
                }
                InboundWebsocketChannelContext ctx = sourceHandler.getChannelHandlerContext();
                int clientBroadcastLevel = sourceHandler.getClientBroadcastLevel();
                String subscriberPath = sourceHandler.getSubscriberPath();
                WebsocketSubscriberPathManager pathManager = WebsocketSubscriberPathManager.getInstance();
                if (log.isDebugEnabled()) {
                    WebsocketLogUtil.printWebSocketFrame(log, frame, ctx.getChannelHandlerContext(),
                            false);
                }
                handleSendBack(frame, ctx, clientBroadcastLevel, subscriberPath, pathManager);
            } else if (msgContext.getProperty(InboundWebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT) != null
                    && msgContext.getProperty(InboundWebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT).equals(true)) {
                TextWebSocketFrame frame = (TextWebSocketFrame) msgContext
                        .getProperty(InboundWebsocketConstants.WEBSOCKET_TEXT_FRAME);
                if (isTCPTransport != null && (boolean) isTCPTransport) {
                    try {
                        RelayUtils.buildMessage(((Axis2MessageContext) msgContext).getAxis2MessageContext(), false);
                        if (defaultContentType != null && defaultContentType
                                .startsWith(InboundWebsocketConstants.TEXT)) {
                            String backendMessageType = (String) (((Axis2MessageContext) msgContext)
                                    .getAxis2MessageContext())
                                    .getProperty(InboundWebsocketConstants.BACKEND_MESSAGE_TYPE);
                            ((Axis2MessageContext) msgContext).getAxis2MessageContext()
                                    .setProperty(InboundWebsocketConstants.MESSAGE_TYPE, backendMessageType);
                            frame = new TextWebSocketFrame(
                                    messageContextToText(((Axis2MessageContext) msgContext).getAxis2MessageContext()));
                            InboundWebsocketChannelContext ctx = sourceHandler.getChannelHandlerContext();
                            int clientBroadcastLevel = sourceHandler.getClientBroadcastLevel();
                            String subscriberPath = sourceHandler.getSubscriberPath();
                            WebsocketSubscriberPathManager pathManager = WebsocketSubscriberPathManager.getInstance();
                            if (log.isDebugEnabled()) {
                                WebsocketLogUtil.printWebSocketFrame(log, frame,
                                        ctx.getChannelHandlerContext(), false);
                            }
                            handleSendBack(frame, ctx, clientBroadcastLevel, subscriberPath, pathManager);
                            return;
                        }
                    } catch (XMLStreamException ex) {
                        log.error("Error while building message", ex);
                    } catch (IOException ex) {
                        log.error("Failed for format message to specified output format", ex);
                    }
                }
                InboundWebsocketChannelContext ctx = sourceHandler.getChannelHandlerContext();
                int clientBroadcastLevel = sourceHandler.getClientBroadcastLevel();
                String subscriberPath = sourceHandler.getSubscriberPath();
                WebsocketSubscriberPathManager pathManager = WebsocketSubscriberPathManager.getInstance();
                if (log.isDebugEnabled()) {
                    WebsocketLogUtil.printWebSocketFrame(log, frame, ctx.getChannelHandlerContext(),
                            false);
                }
                handleSendBack(frame, ctx, clientBroadcastLevel, subscriberPath, pathManager);
            } else {
                try {
                    Object wsCloseFrameStatusCode = msgContext
                            .getProperty(InboundWebsocketConstants.WS_CLOSE_FRAME_STATUS_CODE);
                    String wsCloseFrameReasonText = (String) (msgContext
                            .getProperty(InboundWebsocketConstants.WS_CLOSE_FRAME_REASON_TEXT));
                    int statusCode = InboundWebsocketConstants.WS_CLOSE_DEFAULT_CODE;
                    if (wsCloseFrameStatusCode != null) {
                        statusCode = (int) wsCloseFrameStatusCode;
                    }
                    if (wsCloseFrameStatusCode == null) {
                        wsCloseFrameReasonText = "Unexpected frame type";
                    }

                    if (wsCloseFrameStatusCode != null && wsCloseFrameReasonText != null) {
                        CloseWebSocketFrame closeWebSocketFrame = new CloseWebSocketFrame(statusCode,
                                wsCloseFrameReasonText);
                        if (log.isDebugEnabled()) {
                            WebsocketLogUtil.printWebSocketFrame(log, closeWebSocketFrame,
                                    sourceHandler.getChannelHandlerContext().getChannelHandlerContext(), false);
                        }
                        sourceHandler.handleClientWebsocketChannelTermination(closeWebSocketFrame);
                        return;
                    }
                    RelayUtils.buildMessage(((Axis2MessageContext) msgContext).getAxis2MessageContext(), false);
                    TextWebSocketFrame frame = new TextWebSocketFrame(
                            messageContextToText(((Axis2MessageContext) msgContext).getAxis2MessageContext()));
                    InboundWebsocketChannelContext ctx = sourceHandler.getChannelHandlerContext();
                    int clientBroadcastLevel = sourceHandler.getClientBroadcastLevel();
                    String subscriberPath = sourceHandler.getSubscriberPath();
                    WebsocketSubscriberPathManager pathManager = WebsocketSubscriberPathManager.getInstance();
                    if (log.isDebugEnabled()) {
                        WebsocketLogUtil.printWebSocketFrame(log, frame,
                                sourceHandler.getChannelHandlerContext().getChannelHandlerContext(), false);
                    }
                    handleSendBack(frame, ctx, clientBroadcastLevel, subscriberPath, pathManager);
                } catch (IOException ex) {
                    log.error("Failed for format message to specified output format", ex);
                } catch (XMLStreamException e) {
                    log.error("Error while building message", e);
                }
            }
        }
    }

    protected void handleSendBack(WebSocketFrame frame, InboundWebsocketChannelContext ctx, int clientBroadcastLevel,
                                  String subscriberPath, WebsocketSubscriberPathManager pathManager) {
        try {
            if (clientBroadcastLevel == 0) {
                ctx.writeToChannel(frame.retain());
            } else if (clientBroadcastLevel == 1) {
                String endpointName = WebsocketEndpointManager.getInstance().getEndpointName(sourceHandler.getPort(),
                                                                                             sourceHandler.getTenantDomain());
                pathManager.broadcastOnSubscriberPath(frame, endpointName, subscriberPath);
            } else if (clientBroadcastLevel == 2) {
                String endpointName = WebsocketEndpointManager.getInstance().getEndpointName(sourceHandler.getPort(),
                                                                                             sourceHandler.getTenantDomain());
                pathManager.exclusiveBroadcastOnSubscriberPath(frame, endpointName, subscriberPath, ctx);
            }
        } finally {
            ReferenceCountUtil.release(frame);
        }
    }

    protected String messageContextToText(org.apache.axis2.context.MessageContext msgCtx) throws IOException {
        OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(msgCtx);
        StringWriter sw = new StringWriter();
        OutputStream out = new WriterOutputStream(sw, format.getCharSetEncoding());
        messageFormatter.writeTo(msgCtx, format, out, true);
        out.close();
        return sw.toString();
    }

}
