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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;

import java.util.concurrent.Callable;

public class CallableTask implements Callable<Boolean> {
    private static final Log log = LogFactory.getLog(CallableTask.class);

    private MessageContext requestMessageContext;

    private SequenceMediator injectingSequence;
    private SynapseEnvironment synapseEnvironment;

    public CallableTask(MessageContext synCtx, SequenceMediator injectingSequence) {
        this.requestMessageContext = synCtx;
        this.injectingSequence = injectingSequence;
        this.synapseEnvironment = synCtx.getEnvironment();
    }

    @Override
    public Boolean call() throws Exception {
        // inject to synapse here, call synchronously.
        return synapseEnvironment.injectInbound(requestMessageContext, injectingSequence, true);
    }
}