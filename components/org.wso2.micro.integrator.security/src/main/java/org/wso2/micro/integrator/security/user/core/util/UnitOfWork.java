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
import javax.sql.DataSource;

/**
 * Support class to implement Unit of work patten.
 * <p>
 * Whenever the user wants to get the database connection they can call getDBConnection() to get their connection and
 * when they gets the connection, the backend will maintain a map with connection and data source. At the same time,
 * the user can start the transaction by calling beginTransaction(). It will start and create a new threatLocal and
 * increase the transaction depth count. The user can able to create multiple transactions within the same method. At
 * the end, user can end the transaction by calling commitTransaction()/rollbackTransaction(). it will decrease the
 * transaction depth count. When the transaction depth count is getting zero it will commit or rollback all
 * connection maintained by the threadLocal. Finally we can call closeTransaction() to close all the threatLocal and
 * database connections.
 * eg:
 * -->begin new Transaction 1 (depth = 0 => 0+1)
 * --> begin new Transaction 2 (depth = 1 => 1+1)
 * --> begin new Transaction3 (depth = 2 => 2+1)
 * ---------------
 * <-- end the Transaction 3 (depth =2 => 3-1)
 * <--end the Transaction 2 (depth =1 => 2-1)
 * --> end the Transaction 1 (depth = 0 => 1-1) ( this is the place the actual commit / rollback occurs)
 * --> finally close all the threatLocal and connections.
 */
public class UnitOfWork {

    private static final Log log = LogFactory.getLog(UnitOfWork.class);
    private static ThreadLocal<UnitOfWorkTransactionContext> transactionContextThreadLocal = new ThreadLocal<>();

    public UnitOfWork() {

        super();
    }

    /**
     * Begin the transaction process.
     */
    public static UnitOfWork beginTransaction() {

        UnitOfWorkTransactionContext UnitOfWorkTransactionContext = transactionContextThreadLocal.get();
        if (UnitOfWorkTransactionContext == null) {
            UnitOfWorkTransactionContext = new UnitOfWorkTransactionContext();
            transactionContextThreadLocal.set(UnitOfWorkTransactionContext);
        }
        UnitOfWorkTransactionContext.incrementTransactionDepth();
        return new UnitOfWork();
    }

    /**
     * Returns an database connection.
     *
     * @param dataSource dataSource of the connection.
     * @return current connection
     * @Deprecated The getDBConnection should handle both transaction and non-transaction connection. Earlier it
     * handle only the transactionConnection. Therefore this method was deprecated and changed as handle both
     * transaction and non-transaction connection. getDBConnection(DataSource dataSource, boolean autoCommit) method
     * used as alternative of this method.
     */
    @Deprecated
    public Connection getDBConnection(DataSource dataSource) throws SQLException {

        return getDBConnection(dataSource, true);
    }

    /**
     * Returns an database connection.
     *
     * @param dataSource dataSource of the connection.
     * @param autoCommit autocommit state of the connection.
     * @return current connection
     */
    public Connection getDBConnection(DataSource dataSource, boolean autoCommit) throws SQLException {

        UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
        if (unitOfWorkTransactionContext == null) {
            throw new UnitOfWorkException(
                    "Can not get a connection as Transaction context is not started for dataSource : " + dataSource);
        }
        Connection connection = unitOfWorkTransactionContext.getDBConnection(dataSource);

        if (!autoCommit && connection.getAutoCommit()) {
            //We need only set "autocommit==false", which indicate start of database transaction.
            connection.setAutoCommit(autoCommit);
        }
        return connection;
    }

    /**
     * End the transaction by committing to the transaction.
     */
    public void commitTransaction() {

        try {
            UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
            if (unitOfWorkTransactionContext == null) {
                throw new UnitOfWorkException("Can not get a connection as Transaction context is not started");

            }
            unitOfWorkTransactionContext.decrementTransactionDepth();
            if (unitOfWorkTransactionContext.getTransactionDepth() == 0 && !unitOfWorkTransactionContext
                    .isErrorOccurred()) {
                unitOfWorkTransactionContext.commitAllConnection();
            }
        } catch (SQLException e) {
            log.error("Error occurred while commit connection", e);
        }
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     */
    public void rollbackTransaction() {

        try {
            UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
            unitOfWorkTransactionContext.setErrorOccurred();
            if (unitOfWorkTransactionContext == null) {
                throw new UnitOfWorkException("Can not get a connection as Transaction context is not started");
            }
            unitOfWorkTransactionContext.decrementTransactionDepth();
            if (unitOfWorkTransactionContext.getTransactionDepth() == 0 && unitOfWorkTransactionContext
                    .isErrorOccurred()) {
                unitOfWorkTransactionContext.rollbackAllConnection();
            }
        } catch (SQLException e) {
            log.error("Error occurred while rollback connection", e);
        }
    }

    /**
     * close all the remaining transaction and the connections
     */
    public void closeTransaction() {

        try {
            UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
            if (unitOfWorkTransactionContext == null) {
                throw new UnitOfWorkException("Can not get a connection as Transaction context is not started");
            }
            unitOfWorkTransactionContext.closeConnection();
            transactionContextThreadLocal.remove();

        } catch (SQLException e) {
            log.error("Error occurred while close all the transaction and connection", e);
        }
    }

}
