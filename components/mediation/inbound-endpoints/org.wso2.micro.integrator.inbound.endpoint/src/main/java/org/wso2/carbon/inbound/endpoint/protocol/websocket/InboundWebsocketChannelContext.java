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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class InboundWebsocketChannelContext {

    private ChannelHandlerContext ctx;
    private String channelIdentifier;

    public InboundWebsocketChannelContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.channelIdentifier = ctx.channel().toString();
    }

    public void writeToChannel(WebSocketFrame frame) {
        if (ctx.channel().isActive()) {
            ctx.channel().writeAndFlush(frame.retain());
        }
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return this.ctx;
    }

    public String getChannelIdentifier() {
        return channelIdentifier;
    }

    public void addCloseListener(final ChannelHandlerContext targetCtx) {
        ChannelFuture closeFuture = ctx.channel().closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    if (targetCtx.channel().isActive()) {
                        targetCtx.channel().write(new CloseWebSocketFrame()).addListener(ChannelFutureListener.CLOSE);
                    }
                }
            }
        });
    }

}
