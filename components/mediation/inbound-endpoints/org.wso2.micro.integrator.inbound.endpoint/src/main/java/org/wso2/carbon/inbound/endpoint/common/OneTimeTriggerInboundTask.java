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

package org.wso2.carbon.inbound.endpoint.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;

/**
 * This class provides Generic Task implementation for one time trigger Inbound Endpoint Tasks
 */
public abstract class OneTimeTriggerInboundTask implements org.apache.synapse.task.Task, ManagedLifecycle {

    private static final Log logger = LogFactory.getLog(InboundTask.class.getName());
    private boolean isOneTimeTriggered = false;
    private OneTimeTriggerAbstractCallback callback;
    // boolean used to identify the re-trigger of the task.
    private boolean reTrigger = false;

    public void execute() {
        //this check is there to synchronize task cycle round hit and connection lost reconnection
        //introduced due to task switches between multiple worker nodes, if connection lost happens
        //reconnection only happens if task is scheduled for that node at that given time
        if (callback != null && callback.isCallbackSuspended()) {
            callback.releaseCallbackSuspension();
        }
        if (!isOneTimeTriggered || reTrigger) {
            isOneTimeTriggered = true;
            reTrigger = false;
            logger.debug("Common One time trigger Inbound Task executing.");
            taskExecute();
        }
    }

    public void setCallback(OneTimeTriggerAbstractCallback callback) {
        this.callback = callback;
    }

    public OneTimeTriggerAbstractCallback getCallback() {
        return callback;
    }

    protected void setReTrigger() {
        logger.debug("Enabling re-trigger.");
        this.reTrigger = true;
    }

    protected abstract void taskExecute();
}
