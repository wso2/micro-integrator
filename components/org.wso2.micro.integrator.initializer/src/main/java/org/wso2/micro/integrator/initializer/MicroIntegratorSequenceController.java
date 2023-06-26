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
package org.wso2.micro.integrator.initializer;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SequenceFlowObserver;
import org.wso2.micro.core.util.CoreServerInitializerHolder;

public class MicroIntegratorSequenceController implements SequenceFlowObserver {

    private String name;

    private String seqName;

    @Override
    public void setName(String observerName) {
        name = observerName;
    }

    @Override
    public void start(MessageContext messageContext, String observedSeq) {
        if ("true".equals(messageContext.getProperty(ServiceBusConstants.AUTOMATION_MODE_INITIALIZED_PROPERTY))) {
            seqName = observedSeq;
            messageContext.setProperty(ServiceBusConstants.AUTOMATION_MODE_INITIALIZED_PROPERTY, "false");
        }
    }

    @Override
    public void complete(MessageContext messageContext, String observedSeq) {
        if (observedSeq != null && observedSeq.equals(seqName)) {
            CoreServerInitializerHolder coreServerInitializerHolder = CoreServerInitializerHolder.getInstance();
            coreServerInitializerHolder.shutdownGracefully();
        }
    }

}
