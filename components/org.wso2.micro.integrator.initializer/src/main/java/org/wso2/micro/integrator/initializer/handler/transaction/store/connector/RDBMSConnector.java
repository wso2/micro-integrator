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

import org.apache.axis2.databinding.utils.ConverterUtil;
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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public synchronized void addTransaction() throws TransactionCounterException {
        // if raw exists - update else and a new raw.
        try {
            if (checkDataExists()) {
                updateStats();
            } else {
                addNewRow();
            }
        } catch (SQLException e) {
            throw new TransactionCounterException(
                    "Error occurred while adding transaction count to the database", e);
        }
    }

    /**
     * Get transaction count of a given year and month.
     *
     * @param year        Year.
     * @param monthNumber Month.
     * @return Aggregated transaction count if exists. Else (-1)
     * @throws TransactionCounterException Error occurred while fetching data from the database.
     */
    public long getTransactionCountOfMonth(int year, int monthNumber) throws TransactionCounterException {
        String monthNumberStr = Integer.toString(monthNumber);
        monthNumberStr = monthNumberStr.length() == 1 ? "0" + monthNumberStr : monthNumberStr;
        String dateString = year + "-" + monthNumberStr + "-01";
        try (Connection dbConnection = getConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(TransactionQueryHelper.TRAN_COUNT_OF_MONTH)) {
            prepStmt.setDate(1, convertToSQLDate(dateString));
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
        } catch (SQLException e) {
            throw new TransactionCounterException(
                    "Error occurred while getting the transaction count from the database", e);
        }
        return -1L;
    }

    /**
     * Get transaction count data for a given period of time.
     *
     * @param startDate startDate
     * @param endDate   endDate
     * @return a list of string arrays with information of the column names and row data
     * @throws TransactionCounterException when an error occurs while fetching data from the database.
     */
    public List<String[]> getTransactionCountDataWithColumnNames(String startDate, String endDate)
            throws TransactionCounterException {

        List<String[]> data = new ArrayList<>();
        try (Connection dbConnection = getConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(
                     TransactionQueryHelper.GET_TRAN_COUNT_DATA_FOR_A_TIME_PERIOD)) {
            prepStmt.setDate(1,  convertToSQLDate(startDate));
            prepStmt.setDate(2, convertToSQLDate(endDate));
            try (ResultSet rs = prepStmt.executeQuery()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                data.add(new String[]{rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3),
                        rsmd.getColumnName(4)});
                while (rs.next()) {
                    data.add(new String[]{rs.getString(1), rs.getString(2),
                            String.valueOf(rs.getLong(3)), rs.getString(4)});
                }
            }
        } catch (SQLException e) {
            throw new TransactionCounterException(
                    "Error occurred while getting the transaction count data from the database for the given time "
                            + "period",
                    e);
        }
        return data;
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
            prepStmt.setDate(1, getCurrentMonthAndYear());
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
            prepStmt.setDate(1, getCurrentMonthAndYear());
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
            prepStmt.setDate(4, getCurrentMonthAndYear());
            prepStmt.setString(3, nodeId);
            prepStmt.setLong(1, transactionCount);
            prepStmt.setString(2, encryptedCount);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Create a Date object from the current date.
     *
     * @return SQL DataObject in the format of yyyy-mm-01
     */
    public static java.sql.Date getCurrentMonthAndYear() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String localDateStr = localDate.toString();
        return convertToSQLDate(localDateStr.substring(0, localDateStr.length() - 2) + "01");
    }

    /**
     * Set a new node id to the server.
     */
    public void setNewNodeId(String newNodeId) {
        this.nodeId = newNodeId;
    }

    private static java.sql.Date convertToSQLDate(String date) {
        return new java.sql.Date(ConverterUtil.convertToDate(date).getTime()) ;
    }
}
