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
import org.wso2.micro.integrator.initializer.handler.transaction.TransactionException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
     * @throws TransactionException - when something goes wrong while initializing RDBMS connection.
     */
    public RDBMSConnector(DataSource dataSource, String nodeId, Cipher cipher) throws TransactionException {
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
            throw new TransactionException("Error while initializing RDBMS connection.", ex);
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
     * Add transaction.
     *
     * @throws TransactionException -
     */
    public void addTransaction() throws TransactionException {

    }

    public int readTransactionCount() {
        return 0;
    }

    public long getCurrentTransactionCount() {
        // year
        // month
        // nodeId
        // decrypt if exists
        // return 0 otherwise
        return 0;
    }

    private Map<String, Integer> getMonthAndYear() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Map<String, Integer> monthAndYear = new HashMap<>();
        monthAndYear.put("month", localDate.getMonthValue());
        monthAndYear.put("year", localDate.getYear());

        return monthAndYear;
    }
}
