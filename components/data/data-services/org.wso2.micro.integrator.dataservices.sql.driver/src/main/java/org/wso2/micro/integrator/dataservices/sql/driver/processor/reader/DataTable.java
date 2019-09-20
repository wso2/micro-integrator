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
package org.wso2.micro.integrator.dataservices.sql.driver.processor.reader;

import java.sql.SQLException;
import java.util.Map;

import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

/**
 * This interface represents a data table the parser will work on.
 */
public abstract class DataTable {

    private String tableName;

    private ColumnInfo[] headers;

    private boolean hasHeader;

    public DataTable(String tableName, boolean hasHeader) {
        this.tableName = tableName;
        this.hasHeader = hasHeader;
    }

    public DataTable(String tableName, ColumnInfo[] headers) {
        this.tableName = tableName;
        this.headers = headers;
        try {
            if (this.getHeaders() != null && this.getHeaders().length > 0) {
                this.hasHeader = true;
            }
        } catch (SQLException e) {
            //do nothing
        }
    }

	public abstract Map<Integer, DataRow> getRows() throws SQLException;
		
	public abstract void addRow(DataRow dataRow) throws SQLException;
	
	public abstract void updateRows(DataRow... dataRows) throws SQLException;
	
	public abstract void deleteRows(int... rowIds) throws SQLException;
	
	public abstract Map<Integer, DataRow> applyCondition(String column, String value,
			String operator) throws SQLException;

    public String getTableName() {
        return tableName;
    }

	public ColumnInfo[] getHeaders() throws SQLException {
        return headers;
    }

    public void setHeaders(ColumnInfo[] headers) {
        this.headers = headers;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    public ColumnInfo getHeader(String name) throws SQLException {
        ColumnInfo result = null;
        for (ColumnInfo column : this.getHeaders()) {
            if (column.getName().equalsIgnoreCase(name)) {
                result = column;
            }
        }
        return result;
    }

}
