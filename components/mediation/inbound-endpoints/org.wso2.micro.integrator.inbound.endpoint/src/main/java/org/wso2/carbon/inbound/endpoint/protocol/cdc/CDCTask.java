/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;

import java.util.Properties;

/**
 * CDCTask class is used to schedule tasks for inbound CDC processor when
 * required (coordination==true)
 */
public class CDCTask extends InboundTask {

    private static final Log logger = LogFactory.getLog(CDCTask.class.getName());

    private CDCPollingConsumer pollingConsumer;

    public CDCTask(CDCPollingConsumer pollingConsumer, long interval) {
        logger.debug("CDC Task initialize.");
        this.interval = interval;
        this.pollingConsumer = pollingConsumer;
    }

    protected void taskExecute() {
        if (logger.isDebugEnabled()) {
            logger.debug("CDC Task executing.");
        }
        pollingConsumer.execute();
    }

    @Override
    public Properties getInboundProperties() {
        return pollingConsumer.getInboundProperties();
    }

    public void init(SynapseEnvironment synapseEnvironment) {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing Task.");
        }
    }

    public void destroy() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying Task. ");
        }
    }
}
