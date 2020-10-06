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

package org.wso2.micro.integrator.business.messaging.hl7.store;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.message.MessageProducer;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class JDBCProducer implements MessageProducer {
    private static final Log logger = LogFactory.getLog(JDBCProducer.class.getName());

    private String id;

    private boolean isInitialized = false;
    private JDBCStore store;

    public JDBCProducer(JDBCStore store) {
        if (store == null) {
            logger.error("Cannot initialize.");
            return;
        }
        this.store = store;
        this.isInitialized = true;
    }

    @Override
    public boolean storeMessage(MessageContext messageContext) {
        try {

            Message hl7Message = (Message) ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext().getProperty(HL7Constants.HL7_MESSAGE_OBJECT);

            CallableStatement callableStatement = store.getConnection().prepareCall(JDBCUtils
                                                                                            .setMessage(store.getName(),
                                                                                                        messageContext
                                                                                                                .getMessageID(),
                                                                                                        messageContext
                                                                                                                .getEnvelope()
                                                                                                                .toString()));
            callableStatement.setString(1, messageContext.getMessageID());
            callableStatement.setString(2, hl7Message.encode());

            callableStatement.executeUpdate();

            return true;

        } catch (SQLException e) {
            logger.error("Could not store message in HL7 store: '" + store.getName() + "'. " + e.getMessage());
        } catch (HL7Exception e) {
            logger.error("Could not store message in HL7 store: '" + store.getName() + "'. " + e.getMessage());
        }

        return false;
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
