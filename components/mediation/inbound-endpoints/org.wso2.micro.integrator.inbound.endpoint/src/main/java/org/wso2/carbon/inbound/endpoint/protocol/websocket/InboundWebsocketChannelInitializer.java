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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.ssl.SslHandler;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.ssl.InboundWebsocketSSLConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.ssl.SSLHandlerFactory;

import java.util.ArrayList;

public class InboundWebsocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private InboundWebsocketSSLConfiguration sslConfiguration;
    private int clientBroadcastLevel;
    private String outflowDispatchSequence;
    private String outflowErrorSequence;
    private ChannelHandler pipelineHandler;
    private boolean dispatchToCustomSequence;
    private ArrayList<AbstractSubprotocolHandler> subprotocolHandlers;
    private int portOffset;

    public InboundWebsocketChannelInitializer() {
    }

    public void setSslConfiguration(InboundWebsocketSSLConfiguration sslConfiguration) {
        this.sslConfiguration = sslConfiguration;
    }

    public void setPipelineHandler(ChannelHandler name) {
        this.pipelineHandler = name;
    }

    public void setDispatchToCustomSequence(String dispatchToCustomSequence) {
        this.dispatchToCustomSequence = Boolean.parseBoolean(dispatchToCustomSequence);
    }

    public void setClientBroadcastLevel(int clientBroadcastLevel) {
        this.clientBroadcastLevel = clientBroadcastLevel;
    }

    public void setOutflowDispatchSequence(String outflowDispatchSequence) {
        this.outflowDispatchSequence = outflowDispatchSequence;
    }

    public void setOutflowErrorSequence(String outflowErrorSequence) {
        this.outflowErrorSequence = outflowErrorSequence;
    }

    public void setSubprotocolHandlers(ArrayList<AbstractSubprotocolHandler> subprotocolHandlers) {
        this.subprotocolHandlers = subprotocolHandlers;
    }

    @Override
    protected void initChannel(SocketChannel websocketChannel) throws Exception {

        if (sslConfiguration != null) {
            SslHandler sslHandler = new SSLHandlerFactory(sslConfiguration).create();
            websocketChannel.pipeline().addLast("ssl", sslHandler);
        }

        ChannelPipeline p = websocketChannel.pipeline();
        p.addLast("codec", new HttpServerCodec());
        p.addLast("aggregator", new HttpObjectAggregator(65536));
        p.addLast("frameAggregator", new WebSocketFrameAggregator(Integer.MAX_VALUE));
        InboundWebsocketSourceHandler sourceHandler = new InboundWebsocketSourceHandler();
        sourceHandler.setClientBroadcastLevel(clientBroadcastLevel);
        sourceHandler.setDispatchToCustomSequence(dispatchToCustomSequence);
        sourceHandler.setPortOffset(portOffset);
        if (outflowDispatchSequence != null)
            sourceHandler.setOutflowDispatchSequence(outflowDispatchSequence);
        if (outflowErrorSequence != null)
            sourceHandler.setOutflowErrorSequence(outflowErrorSequence);
        if (subprotocolHandlers != null)
            sourceHandler.setSubprotocolHandlers(subprotocolHandlers);
        if (pipelineHandler != null)
            p.addLast("pipelineHandler", pipelineHandler.getClass().getConstructor().newInstance());
        p.addLast("handler", sourceHandler);
    }

    public void setPortOffset(int portOffset) {
        this.portOffset = portOffset;
    }
}