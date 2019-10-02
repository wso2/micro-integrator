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

package org.wso2.carbon.inbound.endpoint.protocol.websocket.management;

import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketEventExecutor;

import java.util.concurrent.ConcurrentHashMap;

public class WebsocketEventExecutorManager {

    private ConcurrentHashMap<Integer, InboundWebsocketEventExecutor> executorPoolMap = new ConcurrentHashMap<Integer, InboundWebsocketEventExecutor>();

    private static WebsocketEventExecutorManager instance = null;

    public static WebsocketEventExecutorManager getInstance() {
        if (instance == null) {
            instance = new WebsocketEventExecutorManager();
        }
        return instance;
    }

    public void shutdownExecutor(int port) {
        executorPoolMap.get(port).shutdownEventExecutor();
        executorPoolMap.remove(port);
    }

    public void registerEventExecutor(int port, InboundWebsocketEventExecutor eventExecutor) {
        executorPoolMap.put(port, eventExecutor);
    }

    public boolean isRegisteredExecutor(int port) {
        return executorPoolMap.containsKey(port);
    }

}
