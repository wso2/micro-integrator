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
package org.wso2.carbon.inbound.endpoint.protocol.generic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.OneTimeTriggerInboundTask;
import org.wso2.micro.integrator.ntask.core.impl.LocalTaskActionListener;

public class GenericOneTimeTask extends OneTimeTriggerInboundTask implements LocalTaskActionListener {

    private static final Log logger = LogFactory.getLog(GenericOneTimeTask.class.getName());
    private static final long MIN_RELISTEN_DELAY_MS = 1000;
    private static final long MAX_RELISTEN_DELAY_MS = 60000;
    private GenericEventBasedConsumer eventBasedConsumer;
    private boolean firstRunDone = false;
    // Set when the consumer asks to be listened again; cleared while a re-listen attempt runs.
    private volatile boolean relistenRequested = false;
    // Backoff state, only touched on the task scheduler thread.
    private long relistenDelay = MIN_RELISTEN_DELAY_MS;
    private long nextRelistenTime = 0;
    // Serialises re-listen attempts with pause/removal/resume so that an attempt already running when
    // the task is paused or removed cannot reopen the consumer after it was destroyed.
    private final Object lifecycleLock = new Object();
    // True from pause/removal until resume. Guarded by lifecycleLock.
    private boolean stopped = false;

    public GenericOneTimeTask(GenericEventBasedConsumer waitingConsumer) {
        logger.debug("Generic One time Task initalize.");
        this.eventBasedConsumer = waitingConsumer;
        waitingConsumer.setOneTimeTask(this);
    }

    protected void taskExecute() {
        logger.debug("One time task executing.");
        if (!firstRunDone) {
            firstRunDone = true;
            synchronized (lifecycleLock) {
                if (stopped) {
                    return;
                }
                try {
                    eventBasedConsumer.listen();
                } catch (Exception e) {
                    logger.error("Failed to start the event based consumer. Retrying in " + relistenDelay + " ms.", e);
                    nextRelistenTime = System.currentTimeMillis() + relistenDelay;
                    relistenDelay = Math.min(relistenDelay * 2, MAX_RELISTEN_DELAY_MS);
                    relistenRequested = true;
                    setReTrigger();
                }
            }
            return;
        }
        // Every later execution is a re-trigger raised by requestRelisten().
        synchronized (lifecycleLock) {
            if (stopped || !relistenRequested) {
                return;
            }
            if (System.currentTimeMillis() < nextRelistenTime) {
                setReTrigger();
                return;
            }
            // Cleared before listen() so that a request raised during this attempt is not lost.
            relistenRequested = false;
            try {
                eventBasedConsumer.listen();
                relistenDelay = MIN_RELISTEN_DELAY_MS;
                nextRelistenTime = 0;
            } catch (Exception e) {
                logger.error("Failed to restart the event based consumer. Retrying in " + relistenDelay + " ms.", e);
                nextRelistenTime = System.currentTimeMillis() + relistenDelay;
                relistenDelay = Math.min(relistenDelay * 2, MAX_RELISTEN_DELAY_MS);
                relistenRequested = true;
                setReTrigger();
            }
        }
    }

    void requestRelisten() {
        logger.info("Event based consumer requested a restart. It will be listened again on the next task cycle.");
        relistenRequested = true;
        setReTrigger();
    }

    public void init(SynapseEnvironment synapseEnvironment) {
        logger.debug("Initializing Task.");
    }

    public void destroy() {
        logger.debug("Destroying Task. ");
    }

    public GenericEventBasedConsumer getEventBasedConsumer() {
        return eventBasedConsumer;
    }

    /**
     * Method to notify when a local task is removed, it can be due to pause or delete.
     * Destroys the Generic task upon removal of the local task.
     *
     * @param taskName the name of the task that was deleted
     */
    @Override
    public void notifyLocalTaskRemoval(String taskName) {
        logger.info("Removing Generic One Time task: " + taskName);
        stopConsumer(taskName);
    }

    @Override
    public void notifyLocalTaskPause(String taskName) {
        logger.info("Pausing Generic One Time task: " + taskName);
        stopConsumer(taskName);
    }

    @Override
    public void notifyLocalTaskResume(String taskName) {
        logger.info("Resuming Generic One Time task: " + taskName);
        synchronized (lifecycleLock) {
            stopped = false;
            try {
                eventBasedConsumer.resume();
            } catch (AbstractMethodError e) {
                logger.warn("Task [" + taskName + "] : Unsupported operation 'resume()' for this version of "
                        + "EventBasedConsumer. If using a WSO2-released inbound, please upgrade to the latest version. " +
                        "If this is a custom inbound, implement the 'resume' logic accordingly.");
            }
        }
    }

    private void stopConsumer(String taskName) {
        synchronized (lifecycleLock) {
            stopped = true;
            relistenRequested = false;
            try {
                eventBasedConsumer.destroy();
            } catch (AbstractMethodError e) {
                logger.warn("Task [" + taskName + "] : Unsupported operation 'destroy()' for this version of "
                        + "EventBasedConsumer. If using a WSO2-released inbound, please upgrade to the latest version. "
                        + "If this is a custom inbound, implement the 'destroy' logic accordingly.");
            }
        }
    }
}
