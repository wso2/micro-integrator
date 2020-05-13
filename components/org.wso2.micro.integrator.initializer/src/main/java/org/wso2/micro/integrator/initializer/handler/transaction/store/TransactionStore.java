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

package org.wso2.micro.integrator.initializer.handler.transaction.store;

import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterException;
import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterInitializationException;
import org.wso2.micro.integrator.initializer.handler.transaction.store.connector.RDBMSConnector;

import java.sql.SQLException;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.sql.DataSource;

/**
 * The layer which connects to the transaction data.
 */
public class TransactionStore {

    /**
     * Connector for the data base.
     */
    private RDBMSConnector rdbmsConnector;

    /**
     * Constructor.
     *
     * @param dataSource - The datasource config to initiate the connection.
     * @throws TransactionCounterInitializationException - when something goes wrong while initializing RDBMS
     *                                                   connection
     */
    public TransactionStore(DataSource dataSource, String nodeId, Cipher cipher)
            throws TransactionCounterInitializationException {
        this.rdbmsConnector = new RDBMSConnector(dataSource, nodeId, cipher);
    }

    /**
     * Add transaction.
     *
     * @throws TransactionCounterException -
     */
    public void addTransaction() throws TransactionCounterException {
        try {
            this.rdbmsConnector.addTransaction();
        } catch (SQLException e) {
            throw new TransactionCounterException(
                    "Error occurred while adding transaction count to the database", e);
        }
    }

    public long getTransactionCountOfMonth(int year, int monthNumber) throws TransactionCounterException {
        try {
            return this.rdbmsConnector.getTransactionCountOfMonth(year, monthNumber);
        } catch (SQLException e) {
            throw new TransactionCounterException(
                    "Error occurred while getting the transaction count from the database", e);
        }
    }

    /**
     * Set a new node id to the server.
     */
    public void setNewNodeId() {
        this.rdbmsConnector.setNewNodeId(UUID.randomUUID().toString());
    }
}
