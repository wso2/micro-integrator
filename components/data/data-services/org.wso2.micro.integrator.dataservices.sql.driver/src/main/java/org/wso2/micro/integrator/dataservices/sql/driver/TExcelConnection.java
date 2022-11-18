/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.dataservices.sql.driver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

public class TExcelConnection extends TConnection {
    private static final Log log = LogFactory.getLog(TExcelConnection.class);

    private Workbook workbook;

    /**
     * The time in seconds which the lock will try waiting to acquire the lock for the file
     */
    private final int LOCK_TIMEOUT = 20;

    /**
     * Lock used to lock the excel book while editing or modifying.
     */
    private static final Lock lock = new ReentrantLock();

    //variable used for debug purposes
    private static int lockCount = 0;

    private String filePath;

    public TExcelConnection(Properties props) throws SQLException {
        super(props);
        filePath = (String) props.get(Constants.DRIVER_PROPERTIES.FILE_PATH);
        this.workbook = this.createConnectionToExcelDocument(filePath);
    }

    /**
     * Creates a connection to the given Excel document and returns a workbook instance
     *
     * @param filePath Path to the Excel file
     * @return Instance of workbook class containing which represents a database in the
     *         world of SQL
     * @throws SQLException SQLException
     */
    private Workbook createConnectionToExcelDocument(String filePath) throws SQLException {
        return createConnectionToExcelDocument(filePath, true);
    }

    private Workbook createConnectionToExcelDocument(String filePath, boolean releaseLock) throws SQLException {
        Workbook workbook;
        InputStream fin = null;
        try {
            acquireLock();
            fin = TDriverUtil.getInputStreamFromPath(filePath);
            workbook = WorkbookFactory.create(fin);
        } catch (FileNotFoundException e) {
            throw new SQLException("Could not locate the EXCEL datasource in the provided " +
                    "location", e);
        } catch (IOException | InvalidFormatException e) {
            throw new SQLException("Error occurred while initializing the EXCEL datasource", e);
        } catch (InterruptedException e) {
            throw new SQLException("Error Acquiring the lock for the workbook path - " + filePath, e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ignore) {
                }
            }
            if (releaseLock) {
                releaseLock();
            }
        }
        return workbook;
    }

    /**
     * Helper method to acquire a lock for the transaction purpose.
     *
     * @throws InterruptedException
     * @throws SQLException
     */
    private synchronized void acquireLock() throws InterruptedException, SQLException {
        if (lock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
            if (log.isDebugEnabled()) {
                lockCount++;
                log.debug("Acquired the lock for the excel file to make it transactional, current lock count - " + lockCount);
            }
        } else {
            throw new SQLException("Error acquiring lock for the excel file even after 20 second wait, filePath - " + this.filePath);
        }
    }

    /**
     * Helper method to release the acquired lock at the end of the transaction.
     */
    private synchronized void releaseLock() {
        try {
            lock.unlock();
            if (log.isDebugEnabled()) {
                lockCount--;
                log.debug("Released the lock for excel file after the transaction, current lock count - " + lockCount);
            }
        } catch (IllegalMonitorStateException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to release the lock as it is already released, lock count - " + lockCount, e);
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to release the lock as it is already released, lock count - " + lockCount, e);
            }
        }
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Statement createStatement(String sql) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new TPreparedStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              int autoGeneratedKeys) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              int[] columnIndexes) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              String[] columnNames) throws SQLException {
        return null;
    }

    /**
     * Begin transaction method for Excel connections, This will reread the workbook and acquire the lock as well.
     *
     * @throws SQLException
     */
    public void beginExcelTransaction() throws SQLException {
        this.workbook = this.createConnectionToExcelDocument(filePath, false);
    }

    public void commit() throws SQLException {
        releaseLock();
    }

    public void rollback() throws SQLException {
        releaseLock();
    }

    public void close() throws SQLException {
        try {
            workbook.close();
        } catch (IOException ignore) {
        } finally {
            releaseLock();
        }
    }
}
