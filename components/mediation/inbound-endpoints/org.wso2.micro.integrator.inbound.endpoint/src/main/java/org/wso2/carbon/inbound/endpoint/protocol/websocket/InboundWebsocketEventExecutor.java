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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.configuration.NettyThreadPoolConfiguration;

public class InboundWebsocketEventExecutor {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public InboundWebsocketEventExecutor(NettyThreadPoolConfiguration configuration) {
        bossGroup = new NioEventLoopGroup(configuration.getBossThreadPoolSize());
        workerGroup = new NioEventLoopGroup(configuration.getWorkerThreadPoolSize());
    }

    public EventLoopGroup getBossGroupThreadPool() {
        return bossGroup;
    }

    public EventLoopGroup getWorkerGroupThreadPool() {
        return workerGroup;
    }

    public void shutdownEventExecutor() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
