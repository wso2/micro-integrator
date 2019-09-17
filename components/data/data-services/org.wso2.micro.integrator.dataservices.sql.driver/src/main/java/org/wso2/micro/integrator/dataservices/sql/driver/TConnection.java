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

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

public abstract class TConnection implements Connection {

    private String type;

    private String path;

    private String username;

    private String password;

    private boolean hasHeader = true;

    private int maxColumns = -1;

    public TConnection(Properties props) {
        this.type = props.getProperty(Constants.DRIVER_PROPERTIES.DATA_SOURCE_TYPE).toUpperCase();
        this.path = props.getProperty(Constants.DRIVER_PROPERTIES.FILE_PATH);
        this.username = props.getProperty(Constants.DRIVER_PROPERTIES.USER);
        this.password = props.getProperty(Constants.DRIVER_PROPERTIES.PASSWORD);
        if (props.getProperty(Constants.DRIVER_PROPERTIES.HAS_HEADER) != null) {
            this.hasHeader = Boolean.parseBoolean(props.getProperty(Constants.DRIVER_PROPERTIES.HAS_HEADER));
        }
        if (props.getProperty(Constants.DRIVER_PROPERTIES.MAX_COLUMNS) != null) {
            this.maxColumns = Integer.parseInt(props.getProperty(Constants.DRIVER_PROPERTIES.MAX_COLUMNS));
        }
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
    
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public String nativeSQL(String sql) throws SQLException {
        return null;  
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        
    }

    public boolean getAutoCommit() throws SQLException {
        return false;  
    }

    public void commit() throws SQLException {
        
    }

    public void rollback() throws SQLException {
        
    }

    public void close() throws SQLException {
        
    }

    public boolean isClosed() throws SQLException {
        return false;  
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return new TDatabaseMetaData(this);  
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        
    }

    public boolean isReadOnly() throws SQLException {
        return false;  
    }

    public void setCatalog(String catalog) throws SQLException {
        
    }

    public String getCatalog() throws SQLException {
        return null;  
    }

    public void setTransactionIsolation(int level) throws SQLException {
        
    }

    public int getTransactionIsolation() throws SQLException {
        return 0;  
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;  
    }

    public void clearWarnings() throws SQLException {
        
    }

    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency) throws SQLException {
        return null;  
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;  
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        
    }

    public void setHoldability(int holdability) throws SQLException {
        
    }

    public int getHoldability() throws SQLException {
        return 0;  
    }

    public Savepoint setSavepoint() throws SQLException {
        return null;  
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return null;  
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        return null;  
    }

    public Clob createClob() throws SQLException {
        return null;  
    }

    public Blob createBlob() throws SQLException {
        return null;  
    }

    public NClob createNClob() throws SQLException {
        return null;  
    }

    public SQLXML createSQLXML() throws SQLException {
        return null;  
    }

    public boolean isValid(int timeout) throws SQLException {
        return false;  
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        
    }

    public String getClientInfo(String name) throws SQLException {
        return null;  
    }

    public Properties getClientInfo() throws SQLException {
        return null;  
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return null;  
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return null;  
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  
    }

    public void setSchema(String schema) throws SQLException {

    }

    public String getSchema() throws SQLException {
        return null;
    }

    public void abort(Executor executor) throws SQLException {

    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    public int getNetworkTimeout() throws SQLException{
        return 0;
    }

}
