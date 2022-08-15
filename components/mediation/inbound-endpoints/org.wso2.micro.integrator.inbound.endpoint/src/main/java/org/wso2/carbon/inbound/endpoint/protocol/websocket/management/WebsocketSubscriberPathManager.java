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

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketChannelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketSubscriberPathManager {

    private static WebsocketSubscriberPathManager instance = null;
    private static final Log log = LogFactory.getLog(WebsocketSubscriberPathManager.class);

    private ConcurrentHashMap<String, ConcurrentHashMap<String, List<InboundWebsocketChannelContext>>> inboundSubscriberPathMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, List<InboundWebsocketChannelContext>>>();

    public static WebsocketSubscriberPathManager getInstance() {
        if (instance == null) {
            instance = new WebsocketSubscriberPathManager();
        }
        return instance;
    }

    public void addChannelContext(String inboundName, String subscriberPath, InboundWebsocketChannelContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Adding Channel Context with channelID: " + ctx.getChannelIdentifier() + ", in the Thread,ID: "
                              + Thread.currentThread().getName() + "," + Thread.currentThread().getId());
        }
        ConcurrentHashMap<String, List<InboundWebsocketChannelContext>> subscriberPathMap = inboundSubscriberPathMap
                .get(inboundName);
        if (subscriberPathMap == null) {
            subscriberPathMap = new ConcurrentHashMap<String, List<InboundWebsocketChannelContext>>();
            ArrayList<InboundWebsocketChannelContext> listContext = new ArrayList<InboundWebsocketChannelContext>();
            listContext.add(ctx);
            subscriberPathMap.put(subscriberPath, listContext);
            inboundSubscriberPathMap.put(inboundName, subscriberPathMap);
        } else {
            List<InboundWebsocketChannelContext> listContext = subscriberPathMap.get(subscriberPath);
            if (listContext == null) {
                listContext = new ArrayList<InboundWebsocketChannelContext>();
                listContext.add(ctx);
                subscriberPathMap.put(subscriberPath, listContext);
            } else {
                listContext.add(ctx);
            }
        }
    }

    public void removeChannelContext(String inboundName, String subscriberPath, InboundWebsocketChannelContext ctx) {
        ConcurrentHashMap<String, List<InboundWebsocketChannelContext>> subscriberPathMap = inboundSubscriberPathMap
                .get(inboundName);
        List<InboundWebsocketChannelContext> listContext = subscriberPathMap.get(subscriberPath);
        for (Object context : listContext.toArray()) {
            if (((InboundWebsocketChannelContext) context).getChannelIdentifier().equals(ctx.getChannelIdentifier())) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing Channel Context with channelID: " + ctx.getChannelIdentifier()
                                      + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                                      + Thread.currentThread().getId());
                }
                listContext.remove(context);
                break;
            }
        }
        if (listContext.isEmpty()) {
            listContext.clear();
            subscriberPathMap.remove(subscriberPath);
        }
        if (subscriberPathMap.isEmpty()) {
            subscriberPathMap.clear();
            inboundSubscriberPathMap.remove(inboundName);
        }
    }

    public List<InboundWebsocketChannelContext> getSubscriberPathChannelContextList(String inboundName,
                                                                                    String subscriberPath) {
        return inboundSubscriberPathMap.get(inboundName).get(subscriberPath);
    }

    public void broadcastOnSubscriberPath(WebSocketFrame frame, String inboundName, String subscriberPath) {
        List<InboundWebsocketChannelContext> contextList = getSubscriberPathChannelContextList(inboundName,
                                                                                               subscriberPath);
        for (InboundWebsocketChannelContext context : contextList) {
            WebSocketFrame duplicatedFrame = frame.duplicate();
            context.writeToChannel(duplicatedFrame.retain());
        }
    }

    public void exclusiveBroadcastOnSubscriberPath(WebSocketFrame frame, String inboundName, String subscriberPath,
                                                   InboundWebsocketChannelContext ctx) {
        List<InboundWebsocketChannelContext> contextList = getSubscriberPathChannelContextList(inboundName,
                                                                                               subscriberPath);
        for (InboundWebsocketChannelContext context : contextList) {
            if (!context.getChannelIdentifier().equals(ctx.getChannelIdentifier())) {
                WebSocketFrame duplicatedFrame = frame.duplicate();
                context.writeToChannel(duplicatedFrame.retain());
            }
        }
    }

}
