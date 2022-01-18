/**
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * This class will be used log the websocket communication related requests/frames between client and the gateway.
 */
public class WebsocketLogUtil {

    private static String getDirectionString(boolean isInbound) {
        if (isInbound) {
            return " >> ";
        }
        return " << ";
    }

    /**
     * Print the headers associated with the initial Http Request.
     * The header details will be printed in the following format.
     * <p>
     * " >> Headers [channelContextId] [header name] : [header value]"
     *
     * @param log {@link Log} object of the relevant class
     * @param msg {@link FullHttpRequest} response from the backend
     * @param ctx {@link ChannelHandlerContext} context
     */
    public static void printHeaders(Log log, FullHttpRequest msg, ChannelHandlerContext ctx) {
        //the direction is always inbound.
        String logStatement = getDirectionString(true) + "Headers " + resolveContextId(ctx) + " ";
        if (msg.headers() == null || msg.headers().isEmpty()) {
            log.debug(logStatement + "empty");
        } else {
            log.debug("Inbound WebSocket request url: " + msg.getUri());
            for (Map.Entry<String, String> entry : msg.headers().entries()) {
                log.debug(logStatement + entry.getKey() + ":" + entry.getValue());
            }
        }
    }

    /**
     * Print {@link WebSocketFrame} information.
     *
     * @param log       {@link Log} object of the relevant class
     * @param frame     {@link WebSocketFrame} frame
     * @param ctx       {@link ChannelHandlerContext} context
     * @param customMsg Custom message along with the frame information
     * @param isInbound true if the frame is inbound, false if it is outbound
     */
    public static void printWebSocketFrame(Log log, WebSocketFrame frame, ChannelHandlerContext ctx,
                                           String customMsg, boolean isInbound) {
        String channelContextId = resolveContextId(ctx);
        printWebSocketFrame(log, frame, channelContextId, customMsg, isInbound);
    }

    /**
     * Print {@link WebSocketFrame} information.
     *
     * @param log       {@link Log} object of the relevant class
     * @param frame     {@link WebSocketFrame} frame
     * @param ctx       {@link ChannelHandlerContext} context
     * @param isInbound true if the frame is inbound, false if it is outbound
     */
    public static void printWebSocketFrame(Log log, WebSocketFrame frame, ChannelHandlerContext ctx,
                                           boolean isInbound) {
        String channelContextId = resolveContextId(ctx);
        printWebSocketFrame(log, frame, channelContextId, null, isInbound);
    }

    /**
     * Print {@link WebSocketFrame} information.
     *
     * @param log              {@link Log} object of the relevant class
     * @param frame            {@link WebSocketFrame} frame
     * @param channelContextId {@link ChannelHandlerContext} context id as a String
     * @param customMsg        Log message which needs to be appended to the frame information,
     *                         if it is not required provide null
     * @param isInbound        true if the frame is inbound, false if it is outbound
     */
    private static void printWebSocketFrame(Log log, WebSocketFrame frame, String channelContextId,
                                            String customMsg, boolean isInbound) {

        String logStatement = getDirectionString(isInbound) + channelContextId;
        if (frame instanceof PingWebSocketFrame) {
            logStatement += " Ping frame";
        } else if (frame instanceof PongWebSocketFrame) {
            logStatement += " Pong frame";
        } else if (frame instanceof CloseWebSocketFrame) {
            logStatement += " Close frame";
        } else if (frame instanceof BinaryWebSocketFrame) {
            logStatement += " Binary frame";
        } else if (frame instanceof TextWebSocketFrame) {
            logStatement += " " + ((TextWebSocketFrame) frame).text();
        }

        //specifically for logging close websocket frames with error status
        if (customMsg != null) {
            logStatement += " " + customMsg;
        }
        log.debug(logStatement);

    }

    /**
     * Print specific debug logs with the {@link ChannelHandlerContext} context.
     *
     * @param log     log {@link Log} object of the relevant class
     * @param ctx     {@link ChannelHandlerContext} context
     * @param message Log message
     */
    public static void printSpecificLog(Log log, ChannelHandlerContext ctx, String message) {
        log.debug(" " + message + " on Context id : " + ctx.channel().toString());
    }

    private static String resolveContextId(ChannelHandlerContext ctx) {
        if (ctx == null || ctx.channel() == null) {
            return null;
        }
        return ctx.channel().toString();
    }
}
