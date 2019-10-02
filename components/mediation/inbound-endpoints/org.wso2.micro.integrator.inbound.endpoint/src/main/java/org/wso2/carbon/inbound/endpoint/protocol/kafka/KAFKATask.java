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
package org.wso2.carbon.inbound.endpoint.protocol.kafka;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;

import java.util.Properties;

public class KAFKATask extends InboundTask {
    private static final Log logger = LogFactory.getLog(KAFKATask.class.getName());
    private KAFKAPollingConsumer kafkaPollingConsumer;

    public KAFKATask(KAFKAPollingConsumer kafkaPollingConsumer, long interval) {
        logger.debug("Initializing.");
        this.kafkaPollingConsumer = kafkaPollingConsumer;
        this.interval = interval;
    }

    public void init(SynapseEnvironment se) {
        logger.debug("Initializing.");
    }

    public void destroy() {
        logger.debug("Destroying.");
    }

    public void taskExecute() {
        logger.debug("Executing.");
        kafkaPollingConsumer.execute();
    }

    @Override
    public Properties getInboundProperties() {
        return kafkaPollingConsumer.getInboundProperties();
    }
}
