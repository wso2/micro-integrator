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

package org.wso2.micro.integrator.business.messaging.hl7.store.jpa;

import org.apache.synapse.MessageContext;
import org.apache.synapse.message.MessageConsumer;

public class JPAConsumer implements MessageConsumer {
    @Override
    public MessageContext receive() {
        return null;
    }

    @Override
    public boolean ack() {
        return false;
    }

    @Override
    public boolean cleanup() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    /**
     * Set availability of connectivity with the message store
     *
     * @param isAlive connection availability.
     */
    @Override
    public void setAlive(boolean isAlive) {
    }

    @Override
    public void setId(int i) {

    }

    @Override
    public String getId() {
        return null;
    }
}