/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.security.user.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Support class to maintain a mapping between dataSource and connection. At the same time it maintains commit and
 * rollback transaction.
 * By calling commitAllConnection() method user can commit all the transaction. If something happen at the middle
 * then it will rollback the remaining connection.
 * By calling rollbackAllConnection() method user can rollback all the transaction. If something happen at the middle
 * then it will ignore the current transaction and rollback the remaining transaction.
 * By calling closeConnection() method user can closs all the open connection. If something happen at the middle
 * then it will ignore the current connection and close the remaining connections.
 */
public class UnitOfWorkTransactionContext {

    private static final Log log = LogFactory.getLog(UnitOfWorkTransactionContext.class);
    private HashMap<DataSource, Connection> activeConnection = new HashMap();
    private int transactionDepth = 0;
    private boolean errorOccurred = false;

    /**
     * Commit all the transaction, if something happened at the middle then it throw all committed transaction and it
     * will rollback the remaining transaction.
     */
    void commitAllConnection() {

        StringBuilder commitErrorHandling = null;
        List<DataSource> listOfCommittedDataSource = null;
        HashMap<DataSource, Connection> rollBackConnections = null;

        for (Map.Entry entry : activeConnection.entrySet()) {
            try {
                Connection connection = (Connection) entry.getValue();
                if (connection != null) {
                    connection.commit();
                }
            } catch (SQLException e) {
                if (commitErrorHandling.equals(null)) {
                    commitErrorHandling = new StringBuilder();
                }
                if (listOfCommittedDataSource.equals(null)) {
                    listOfCommittedDataSource = new ArrayList<>();
                }
                if (rollBackConnections.equals(null)) {
                    rollBackConnections = new HashMap();
                }
                //get all the transaction that are committed before error occurs.
                for (Map.Entry activeEntry : activeConnection.entrySet()) {
                    if (!entry.getKey().equals(activeEntry.getKey())) {
                        listOfCommittedDataSource.add((DataSource) activeEntry.getKey());
                    } else {
                        break;
                    }
                }
                commitErrorHandling.append("Error occurred while committing the connection. Connection: ")
                        .append(entry.getValue())
                        .append(". We have committed few transaction before error occurred. Committed dataSource : ")
                        .append(listOfCommittedDataSource.toString()).append(". ");
                //get all the transaction that are not committed.
                for (Map.Entry activeEntry : activeConnection.entrySet()) {
                    if (!listOfCommittedDataSource.contains(activeEntry.getKey())) {
                        rollBackConnections.put((DataSource) activeEntry.getKey(), (Connection) activeEntry.getValue());
                    }
                }
                // this method do the rollback for all uncommitted transaction list get above.
                internalRollbackConnection(rollBackConnections, commitErrorHandling);
                log.error(commitErrorHandling.toString(), e);
            }
        }
    }

    /**
     * Rollback the remaining transaction if something happened at the middle while committing.
     *
     * @param rollBackConnections the connection map need to be rollback.
     * @param commitErrorHandling the error handling message.
     */
    private void internalRollbackConnection(HashMap<DataSource, Connection> rollBackConnections,
            StringBuilder commitErrorHandling) {

        boolean internalRollback = false;
        for (Map.Entry entry : rollBackConnections.entrySet()) {
            try {
                Connection connection = (Connection) entry.getValue();
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                commitErrorHandling.append("Error occurred while rollback the connection for dataSource: ")
                        .append(entry.getKey()).append(", ").append(e).append(". ");
                internalRollback = true;
            }
        }
        if (!internalRollback) {
            commitErrorHandling
                    .append("Successfully rollback the remaining open connection due to this commit error. ");
        }
    }

    /**
     * Rollback all the transaction, if something happened at the middle then it will continue and rollback all the
     * connection as much as possible.
     */
    void rollbackAllConnection() {

        StringBuilder rollbackErrorHandling = null;
        boolean rollbackErrorOccurred = false;
        for (Map.Entry entry : activeConnection.entrySet()) {
            try {
                Connection connection = (Connection) entry.getValue();
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                if (rollbackErrorHandling.equals(null)) {
                    rollbackErrorHandling = new StringBuilder();
                }
                rollbackErrorHandling.append("Error occurred while rollback the connection for dataSource: ")
                        .append(entry.getKey()).append(", ").append(e).append(". ");
                rollbackErrorOccurred = true;
            }
        }
        if (rollbackErrorOccurred) {
            log.error(rollbackErrorHandling);
        }
    }

    /**
     * Set and return the connection.
     *
     * @param dataSource dataSource of the connection
     * @return the current connection
     */
    public Connection getDBConnection(DataSource dataSource) throws SQLException {

        Connection currentConnectionForDataSource = activeConnection.get(dataSource);
        if (currentConnectionForDataSource == null) {
            currentConnectionForDataSource = dataSource.getConnection();
            activeConnection.put(dataSource, currentConnectionForDataSource);
        }
        return currentConnectionForDataSource;
    }

    /**
     * Get the transaction level.
     *
     * @return transaction level
     */
    int getTransactionDepth() {

        return transactionDepth;
    }

    /**
     * Get the transaction level.
     *
     * @return transaction level
     */
    boolean isErrorOccurred() {

        return errorOccurred;
    }

    /**
     * Increment the transaction depth by one to store the level of a transaction.
     */
    void setErrorOccurred() {

        this.errorOccurred = true;
    }

    /**
     * Increment the transaction depth by one to store the level of a transaction.
     */
    void incrementTransactionDepth() {

        transactionDepth++;
    }

    /**
     * Decrement the transaction depth by one to notice the remaining levels of a transaction.
     */
    void decrementTransactionDepth() {

        transactionDepth--;
    }

    /**
     * Close all the db connection.
     */
    void closeConnection() {

        boolean connectionErrorOccurred = false;
        StringBuilder connectionErrorHandling = null;
        for (Map.Entry entry : activeConnection.entrySet()) {
            Connection connection = (Connection) entry.getValue();
            try {
                connection.close();
            } catch (SQLException e) {
                if (connectionErrorHandling.equals(null)) {
                    connectionErrorHandling = new StringBuilder();
                }
                connectionErrorOccurred = true;
                connectionErrorHandling.append("Error occurred while close the connection:  ")
                        .append(connectionErrorHandling).append(", Error was : ").append(e).append(". ");

            }
        }
        if (connectionErrorOccurred) {
            log.error(connectionErrorHandling);
        }
    }
}
