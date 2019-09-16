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
package org.wso2.micro.integrator.analytics.messageflow.data.publisher.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.MessageFlowObserver;

import java.util.HashSet;
import java.util.Set;

public class MessageFlowObserverStore {

    private static final Log log = LogFactory.getLog(MessageFlowObserverStore.class);

    private Set<MessageFlowObserver> observers = new HashSet<MessageFlowObserver>();

    /**
     * Register a custom statistics consumer to receive updates from this
     * statistics store
     *
     * @param o The MediationStatisticsObserver instance to be notified of data updates
     */
    public void registerObserver(MessageFlowObserver o) {
        observers.add(o);
    }

    /**
     * Unregister the custom statistics consumer from the mediation statistics store
     *
     * @param o The MediationStatisticsObserver instance to be removed
     */
    public void unregisterObserver(MessageFlowObserver o) {
        if (observers.contains(o)) {
            observers.remove(o);
            o.destroy();
        }
    }

    void unregisterObservers() {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering mediation statistics observers");
        }

        for (MessageFlowObserver o : observers) {
            o.destroy();
        }
        observers.clear();
    }

    public void notifyObservers(PublishingFlow publishingFlow) {

        for (MessageFlowObserver o : observers) {
            try {
                o.updateStatistics(publishingFlow);
            } catch (Throwable t) {
                log.error("Error occurred while notifying the statistics observer", t);
            }
        }
    }

    public MessageFlowObserverStore() {
    }

}
