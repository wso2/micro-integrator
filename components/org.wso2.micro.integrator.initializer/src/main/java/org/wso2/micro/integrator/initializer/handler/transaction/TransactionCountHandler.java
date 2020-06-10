/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer.handler.transaction;

import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.micro.integrator.initializer.handler.DataHolder;
import org.wso2.micro.integrator.initializer.handler.transaction.store.TransactionStore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class TransactionCountHandler extends AbstractSynapseHandler {

    private static final Log LOG = LogFactory.getLog(TransactionCountHandler.class);
    private static final double MAX_TRANSACTION_COUNT = Integer.MAX_VALUE * 0.9;
    private static volatile int transactionCount = 0;
    private ExecutorService transactionCountExecutor;
    private final Object lock = new Object();

    public TransactionCountHandler(ExecutorService executor) {
        this.transactionCountExecutor = executor;
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        try {
            transactionCountExecutor.execute(() -> handleTransactionCount(messageContext));
        } catch (RejectedExecutionException e) {
            LOG.error("Transaction could not be counted.", e);
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        return true;
    }

    public static long getTransactionCount() {
        return transactionCount;
    }

    /**
     * Increment the transaction count by 1 if the INTERNAL_TRANSACTION_COUNTED message property is not set to true.
     * Commit the current transaction count to database and set the transaction count to zero if it has exceeded the
     * Integer.MAX_VALUE
     */
    private void handleTransactionCount(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object transactionProperty = axis2MessageContext.getProperty(BaseConstants.INTERNAL_TRANSACTION_COUNTED);
        // increment the transaction count by 1 if the INTERNAL_TRANSACTION_COUNTED message property has not been set
        // or if the value of the property is set to false.
        if (!(transactionProperty instanceof Boolean && (Boolean) transactionProperty)) {
            axis2MessageContext.setProperty(BaseConstants.INTERNAL_TRANSACTION_COUNTED, true);
            // commit the current transaction count to database and set the transaction count to zero if it has
            // reached to Integer.MAX_VALUE
            synchronized (lock) {
                commitAndResetTransactionCountIfReachedToMaxIntegerValue();
                transactionCount += 1;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction " + axis2MessageContext.getLogCorrelationID() + " is counted.");
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipped counting the transaction " + axis2MessageContext.getLogCorrelationID()
                                  + " since it has been counted before.");
            }
        }
    }

    /**
     * If the current transaction count has reached or exceeded the Max_Integer_value * 0.9, commit the current
     * transaction to the database, set a new node id, and reset the transaction count to zero.
     */
    private void commitAndResetTransactionCountIfReachedToMaxIntegerValue() {
        if (transactionCount >= MAX_TRANSACTION_COUNT) {
            DataHolder dataHolder = DataHolder.getInstance();
            TransactionStore transactionStore = dataHolder.getTransactionStore();
            try {
                transactionStore.addTransaction();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Transaction count reached to max Integer value. Hence, adding the current transaction "
                                      + "count: " + transactionCount + " to the database.");
                }
                // set new node id to the server and make the tx count zero. This should happen in this order.
                transactionStore.setNewNodeId();
                transactionCount = 0;
            } catch (Throwable e) {
                LOG.fatal("Could not persist the transaction count for the last period of " + DataHolder.getInstance()
                        .getTransactionUpdateInterval() + "min.", e);
            }
        }
    }
}
