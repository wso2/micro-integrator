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

public class GenericOneTimeTask extends OneTimeTriggerInboundTask {

    private static final Log logger = LogFactory.getLog(GenericOneTimeTask.class.getName());
    private GenericEventBasedConsumer eventBasedConsumer;

    public GenericOneTimeTask(GenericEventBasedConsumer waitingConsumer) {
        logger.debug("Generic One time Task initalize.");
        this.eventBasedConsumer = waitingConsumer;
    }

    protected void taskExecute() {
        logger.debug("One time task executing.");
        eventBasedConsumer.listen();
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

}
