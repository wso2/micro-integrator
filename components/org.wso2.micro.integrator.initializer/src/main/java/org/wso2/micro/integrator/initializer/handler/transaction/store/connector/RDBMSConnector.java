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

package org.wso2.micro.integrator.initializer.handler.transaction.store.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.initializer.handler.transaction.TransactionCountHandler;
import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterException;
import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterInitializationException;
import org.wso2.micro.integrator.initializer.handler.transaction.security.CryptoUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.Cipher;
import javax.sql.DataSource;

/**
 * The connector class which deals with underlying internal transaction related tables.
 */
public class RDBMSConnector {

    private static final Log LOG = LogFactory.getLog(RDBMSConnector.class);
    private DataSource dataSource;
    private String nodeId;
    private Cipher cipher;

    /**
     * Constructor.
     *
     * @param dataSource - The datasource config to initiate the connection.
     * @throws TransactionCounterInitializationException - when something goes wrong while initializing RDBMS
     *                                                   connection.
     */
    public RDBMSConnector(DataSource dataSource, String nodeId, Cipher cipher)
            throws TransactionCounterInitializationException {
        this.nodeId = nodeId;
        this.cipher = cipher;
        this.dataSource = dataSource;
        try (Connection connection = getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseType = metaData.getDatabaseProductName();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully connected to : " + databaseType);
            }
        } catch (SQLException ex) {
            throw new TransactionCounterInitializationException("Error while initializing RDBMS connection.", ex);
        }
    }

    /**
     * Get connection.
     *
     * @return - Connection with auto commit true.
     * @throws SQLException -
     */
    private Connection getConnection() throws SQLException {

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * Add transaction count to the database.
     */
    public synchronized void addTransaction() throws SQLException {
        // if raw exists - update else and a new raw.
        if (checkDataExists()) {
            updateStats();
        } else {
            addNewRow();
        }
    }

    /**
     * Get transaction count of a given year and month.
     *
     * @param year        Year.
     * @param monthNumber Month.
     * @return Aggregated transaction count if exists. Else (-1)
     * @throws SQLException Error occurred while fetching data from the database.
     */
    public long getTransactionCountOfMonth(int year, int monthNumber) throws SQLException {

        String dateString = year + "-" + monthNumber + "-01";
        try (Connection dbConnection = getConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(TransactionQueryHelper.TRAN_COUNT_OF_MONTH)) {
            prepStmt.setString(1, dateString);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    Object count = rs.getObject(1);
                    if (count != null) {
                        return Long.parseLong(count.toString());
                    } else {
                        return -1L;
                    }
                }
            }
        }
        return -1L;
    }

    // Add new entry to the transaction stat table
    private void addNewRow() throws SQLException {
        long transactionCount = TransactionCountHandler.getTransactionCount();
        String encryptedCount;
        try {
            encryptedCount = CryptoUtil.doEncryption(cipher, Long.toString(transactionCount));
        } catch (TransactionCounterException e) {
            // not adding to the database when encryption error occurs.
            LOG.error("Error occurred while encrypting the transaction count", e);
            return;
        }

        try (Connection dbConnection = getConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(TransactionQueryHelper.INSERT_RAW)) {
            prepStmt.setString(1, getCurrentMonthAndYear());
            prepStmt.setString(2, nodeId);
            prepStmt.setLong(3, transactionCount);
            prepStmt.setString(4, encryptedCount);
            prepStmt.executeUpdate();
        }
    }

    // check a raw exists in the table for this node, for this month.
    private boolean checkDataExists() throws SQLException {
        try (Connection dbConnection = getConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(TransactionQueryHelper.GET_TRAN_COUNT)) {
            prepStmt.setString(1, getCurrentMonthAndYear());
            prepStmt.setString(2, nodeId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // update the transaction count of this node for this month.
    private void updateStats() throws SQLException {

        long transactionCount = TransactionCountHandler.getTransactionCount();
        String encryptedCount;
        try {
            encryptedCount = CryptoUtil.doEncryption(cipher, Long.toString(transactionCount));
        } catch (TransactionCounterException e) {
            // not adding to the database when encryption error occurs.
            LOG.error("Error occurred while encrypting the transaction count", e);
            return;
        }
        try (Connection dbConnection = getConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(TransactionQueryHelper.UPDATE_TRAN_COUNT)) {
            prepStmt.setString(4, getCurrentMonthAndYear());
            prepStmt.setString(3, nodeId);
            prepStmt.setLong(1, transactionCount);
            prepStmt.setString(2, encryptedCount);
            prepStmt.executeUpdate();
        }
    }

    // get current month and year in the format yyyy-mm-01
    private String getCurrentMonthAndYear() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getYear() + "-" + localDate.getMonthValue() + "-01";
    }

    /**
     * Set a new node id to the server.
     */
    public void setNewNodeId(String newNodeId) {
        this.nodeId = newNodeId;
    }
}
