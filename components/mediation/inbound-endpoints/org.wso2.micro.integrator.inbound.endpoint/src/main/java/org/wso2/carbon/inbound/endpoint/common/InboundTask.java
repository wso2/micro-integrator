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

import java.util.Date;
import java.util.Properties;

/**
 * This class provides Generic Task implementation for inbound polling
 */
public abstract class InboundTask implements org.apache.synapse.task.Task, ManagedLifecycle, PinnedPollingTask {

    private static final Log logger = LogFactory.getLog(InboundTask.class.getName());

    protected long interval;

    public static final int TASK_THRESHOLD_INTERVAL = 1000;

    public void execute() {
        logger.debug("Common Inbound Task executing.");

        //If the thresehold value is greater than i second just run the cycle
        if (interval >= TASK_THRESHOLD_INTERVAL) {
            taskExecute();
        } else {
            long lStartTime = (new Date()).getTime();
            long lCurrentTime = lStartTime;
            //Run the cycles within one second (1000ms)
            while ((lCurrentTime - lStartTime) < TASK_THRESHOLD_INTERVAL) {
                taskExecute();
                long lEndTime = (new Date()).getTime();
                long lRequiredSleep = interval - (lEndTime - lCurrentTime);
                if (lRequiredSleep > 0) {
                    try {
                        Thread.sleep(lRequiredSleep);
                    } catch (InterruptedException e) {
                        logger.debug("Unable to sleep the inbound thread less than 1 second");
                    }
                }
                lCurrentTime = (new Date()).getTime();
            }
        }

    }

    protected abstract void taskExecute();

    public abstract Properties getInboundProperties();
}
