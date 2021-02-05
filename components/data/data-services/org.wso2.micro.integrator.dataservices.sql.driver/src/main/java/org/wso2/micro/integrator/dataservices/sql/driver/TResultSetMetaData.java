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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

public class TResultSetMetaData implements ResultSetMetaData {

    private ColumnInfo[] columns;

    private int columnCount;

    public TResultSetMetaData(ColumnInfo[] columns, int columnCount) {
        this.columns = columns;
        this.columnCount = columnCount;
    }

    private ColumnInfo getColumn(int i) throws SQLException {
        if (i < 0 || i > getColumnCount()) {
            throw new SQLException("Invalid column index");
        }
        return getColumns()[i - 1];
    }

    public ColumnInfo[] getColumns() {
        return columns;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return columnCount;
    }

    @Override
    public boolean isAutoIncrement(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean isCaseSensitive(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean isSearchable(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean isCurrency(int i) throws SQLException {
        return false;  
    }

    @Override
    public int isNullable(int i) throws SQLException {
        return 0;  
    }

    @Override
    public boolean isSigned(int i) throws SQLException {
        return false;  
    }

    @Override
    public int getColumnDisplaySize(int i) throws SQLException {
        return 0;  
    }

    @Override
    public String getColumnLabel(int i) throws SQLException {
        return getColumn(i).getAliasName() != null ? getColumn(i).getAliasName() : getColumn(i).getName();
    }

    @Override
    public String getColumnName(int i) throws SQLException {
        return getColumn(i).getName();
    }

    @Override
    public String getSchemaName(int i) throws SQLException {
        return "";
    }

    @Override
    public int getPrecision(int i) throws SQLException {
        return 0;  
    }

    @Override
    public int getScale(int i) throws SQLException {
        return 0;  
    }

    @Override
    public String getTableName(int i) throws SQLException {
        return getColumn(i).getTableName();
    }

    @Override
    public String getCatalogName(int i) throws SQLException {
        return null;  
    }

    @Override
    public int getColumnType(int i) throws SQLException {
        return 0;  
    }

    @Override
    public String getColumnTypeName(int i) throws SQLException {
        return null;  
    }

    @Override
    public boolean isReadOnly(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean isWritable(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean isDefinitelyWritable(int i) throws SQLException {
        return false;  
    }

    @Override
    public String getColumnClassName(int i) throws SQLException {
        return null;  
    }

    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
        return null;  
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;  
    }
}
