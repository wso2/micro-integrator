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

/**
 * OnetimeTriggerInboundRunner class is used to run the non coordinated processors in
 * background. This Runner is only active for one task execution iteration ( One time trigger )
 */
public class OneTimeTriggerInboundRunner implements Runnable {

    private OneTimeTriggerInboundTask task;

    private static final Log log = LogFactory.getLog(OneTimeTriggerInboundRunner.class);

    public OneTimeTriggerInboundRunner(OneTimeTriggerInboundTask task, String tenantDomain) {
        this.task = task;
    }

    @Override
    public void run() {

        log.debug("Starting the Inbound Endpoint.");

        log.debug("Executing One-Time trigger connection consumer task in Inbound Endpoint.");

        try {
            task.taskExecute();
        } catch (Exception e) {
            log.error("Error executing the Inbound Endpoint once time execution", e);
        }

        log.debug("Exiting the Inbound Endpoint.");

    }
}
