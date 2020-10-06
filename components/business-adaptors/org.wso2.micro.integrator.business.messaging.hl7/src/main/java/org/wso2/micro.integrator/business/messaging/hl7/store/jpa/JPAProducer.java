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

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.message.MessageProducer;
import org.wso2.micro.integrator.business.messaging.hl7.store.entity.PersistentHL7Message;
import org.wso2.micro.integrator.business.messaging.hl7.store.util.SerializableMessageContext;
import org.wso2.micro.integrator.business.messaging.hl7.store.util.SerializerUtils;
import org.wso2.micro.integrator.business.messaging.hl7.transport.HL7TransportOutInfo;

import javax.persistence.EntityManager;

public class JPAProducer implements MessageProducer {
    private static final Log logger = LogFactory.getLog(JPAProducer.class.getName());

    private String id;

    private boolean isInitialized = false;
    private JPAStore store;

    public JPAProducer(JPAStore store) {
        if (store == null) {
            logger.error("Cannot initialize.");
            return;
        }
        this.store = store;
        this.isInitialized = true;
    }

    // TODO: catch specific exceptions - do performance test
    @Override
    public boolean storeMessage(MessageContext messageContext) {
        SerializableMessageContext serializableMessageContext = SerializerUtils.toStorableMessage(messageContext,
                                                                                                  messageContext
                                                                                                          .getMessageID());

        HL7TransportOutInfo outInfo =
                (HL7TransportOutInfo) ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(
                        Constants.OUT_TRANSPORT_INFO);
        String controlId = "";
        if (outInfo != null) {
            controlId = outInfo.getMessageControllerID();
        }

        EntityManager manager = store.getEntityManager();

        try {
            PersistentHL7Message persistentHL7Message = new PersistentHL7Message(store.getName(),
                                                                                 messageContext.getMessageID(),
                                                                                 controlId, SerializerUtils.serialize(
                    serializableMessageContext));
            manager.getTransaction().begin();
            manager.persist(persistentHL7Message);
            manager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            logger.error("Could not store HL7 message. " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cleanup() {
        return false;
    }

    @Override
    public void setId(int id) {
        this.id = "[" + store.getName() + "-P-" + id + "]";
    }

    @Override
    public String getId() {
        return getIdAsString();
    }

    private String getIdAsString() {
        if (this.id == null) {
            return "[unknown-producer]";
        }
        return this.id;
    }
}

