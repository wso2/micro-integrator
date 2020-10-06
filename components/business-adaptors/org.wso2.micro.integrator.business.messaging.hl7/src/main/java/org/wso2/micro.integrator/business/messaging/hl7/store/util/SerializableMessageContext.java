/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.business.messaging.hl7.store.util;

import org.apache.synapse.message.store.impl.commons.Axis2Message;
import org.apache.synapse.message.store.impl.commons.SynapseMessage;

import java.io.Serializable;

public class SerializableMessageContext implements Serializable {

    private Axis2Message axis2message;

    private SynapseMessage synapseMessage;

    public SynapseMessage getSynapseMessage() {
        return synapseMessage;
    }

    public void setSynapseMessage(SynapseMessage synapseMessage) {
        this.synapseMessage = synapseMessage;
    }

    public Axis2Message getAxis2message() {
        return axis2message;
    }

    public void setAxis2message(Axis2Message axis2message) {
        this.axis2message = axis2message;
    }
}
