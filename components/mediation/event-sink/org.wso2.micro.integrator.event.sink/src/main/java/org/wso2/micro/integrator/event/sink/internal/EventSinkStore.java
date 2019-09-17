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

package org.wso2.micro.integrator.event.sink.internal;

import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.event.sink.EventSink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class that stores event sinks deployed in the server.
 */
public class EventSinkStore {
    private static EventSinkStore instance = new EventSinkStore();

    //tenant id|sink name -> sink
    private Map<String, EventSink> eventSinkMap = Collections.synchronizedMap(new HashMap<String, EventSink>());

    /**
     * returns singleton instance of EventSinkStore.
     *
     * @return Singleton instance
     */
    public static EventSinkStore getInstance() {
        return instance;
    }

    private EventSinkStore() {
    }

    /**
     * Adds new event sink to store.
     *
     * @param eventSink Event sink to be added to the store.
     */
    public void addEventSink(EventSink eventSink) {
        String key = Constants.SUPER_TENANT_ID + "|" + eventSink.getName();
        eventSinkMap.put(key, eventSink);
    }

    /**
     * Removes event sink specified from current store.
     *
     * @param eventSinkName the name of the event sink to be removed from store
     */
    public void removeEventSink(String eventSinkName) {
        String key = Constants.SUPER_TENANT_ID + "|" + eventSinkName;
        eventSinkMap.remove(key);
    }

    /**
     * Finds event sink with given name that is registered.
     *
     * @param name Name of the event sink
     * @return Event sink registered with given name. If no event sink found with name, returns null
     */
    public EventSink getEventSink(String name) {
        String key = Constants.SUPER_TENANT_ID + "|" + name;
        return eventSinkMap.get(key);
    }

    /**
     * Returns list of all event sinks registered.
     *
     * @return list of all event sinks registered
     */
    public List<EventSink> getEventSinkList() {
        List<EventSink> list = new ArrayList<EventSink>();
        synchronized (eventSinkMap) {
            for (Map.Entry<String, EventSink> entry : eventSinkMap.entrySet()) {
                if (entry.getKey().startsWith(Constants.SUPER_TENANT_ID + "|")) {
                    list.add(entry.getValue());
                }
            }
        }
        return list;
    }
}
