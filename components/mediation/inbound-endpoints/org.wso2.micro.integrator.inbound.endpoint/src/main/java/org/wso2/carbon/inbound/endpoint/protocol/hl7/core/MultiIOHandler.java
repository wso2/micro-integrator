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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.IOSession;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiIOHandler extends MLLPSourceHandler {

    private static final Log log = LogFactory.getLog(MultiIOHandler.class);

    public ConcurrentHashMap<Integer, MLLPSourceHandler> handlers = new ConcurrentHashMap<Integer, MLLPSourceHandler>();

    private ConcurrentHashMap<Integer, HL7Processor> processorMap;

    private ConcurrentHashMap<String, IOSession> endpointSessions = new ConcurrentHashMap<String, IOSession>();

    public MultiIOHandler(ConcurrentHashMap<Integer, HL7Processor> processorMap) {
        super();
        this.processorMap = processorMap;
    }

    @Override
    public void connected(IOSession session) {

        InetSocketAddress remoteIsa = (InetSocketAddress) session.getRemoteAddress();
        InetSocketAddress localIsa = (InetSocketAddress) session.getLocalAddress();

        MLLPSourceHandler handler = new MLLPSourceHandler(processorMap.get(localIsa.getPort()));
        handlers.put(remoteIsa.getPort(), handler);
        endpointSessions.put(localIsa.getPort() + "-" + remoteIsa.getPort(), session);

        handler.connected(session);

    }

    @Override
    public void inputReady(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        MLLPSourceHandler handler = handlers.get(isa.getPort());
        handler.inputReady(session);

    }

    @Override
    public void outputReady(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        MLLPSourceHandler handler = handlers.get(isa.getPort());
        handler.outputReady(session);

    }

    @Override
    public void timeout(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        InetSocketAddress localIsa = (InetSocketAddress) session.getLocalAddress();
        MLLPSourceHandler handler = handlers.get(isa.getPort());
        handler.timeout(session);
        handlers.remove(handler);
        endpointSessions.remove(localIsa.getPort() + "-" + isa.getPort());

    }

    @Override
    public void disconnected(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        InetSocketAddress localIsa = (InetSocketAddress) session.getLocalAddress();
        if (isa == null) {
            return;
        }
        MLLPSourceHandler handler = handlers.get(isa.getPort());
        handler.disconnected(session);
        handlers.remove(handler);
        endpointSessions.remove(localIsa.getPort() + "-" + isa.getPort());

    }

    public void disconnectSessions(int localPort) {
        for (Map.Entry<String, IOSession> entry : endpointSessions.entrySet()) {
            if (entry.getKey().startsWith(String.valueOf(localPort))) {
                IOSession session = entry.getValue();
                disconnected(session);
            }
        }
    }
}
