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
package org.wso2.micro.integrator.dataservices.sql.driver.query.insert;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;
import org.wso2.micro.integrator.dataservices.sql.driver.TDriverUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.TGSpreadConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ParamInfo;

public class GSpreadInsertQuery extends InsertQuery {

    ColumnInfo[] columns;

    public GSpreadInsertQuery(Statement stmt) throws SQLException {
        super(stmt);
        columns = TDriverUtil.getHeaders(getConnection(), getTargetTableName());
        processParameters(columns);
    }

    private void processParameters(ColumnInfo[] columns) {
        for (ColumnInfo column : columns) {
            for (ParamInfo param : getParameters()) {
                if (param.getOrdinal() == column.getId()) {
                    param.setName(column.getName());
                }
            }
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        this.executeSQL();
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return this.executeSQL();
    }

    @Override
    public boolean execute() throws SQLException {
        return (this.executeSQL() > 0);
    }

    private synchronized int executeSQL() throws SQLException {
        int count = 1;
        TGSpreadConnection connection = (TGSpreadConnection) getConnection();
        WorksheetEntry currentWorkSheet =
                TDriverUtil.getCurrentWorkSheetEntry(connection, getTargetTableName());
        if (currentWorkSheet == null) {
            throw new SQLException("WorkSheet '" + getTargetTableName() + "' does not exist");
        }

        ListFeed listFeed = TDriverUtil.getListFeed(connection, currentWorkSheet);
        ListEntry row = new ListEntry();
        for (ColumnInfo column : TDriverUtil.getHeaders(getConnection(), getTargetTableName())) {
            ParamInfo matchingParam = TDriverUtil.findParam(column, getParameters());
            if (matchingParam != null) {
                row.getCustomElements().setValueLocal(column.getName(),
                        matchingParam.getValue().toString());
            } else {
                row.getCustomElements().setValueLocal(column.getName(), null);
            }
        }
        try {
            listFeed.insert(row);
        } catch (ServiceException e) {
            throw new SQLException("Error occurred while writing the record to GSpread " +
                    "worksheet", e);
        } catch (IOException e) {
            throw new SQLException("Error occurred while writing the record to GSpread " +
                    "worksheet", e);
        }
        return count;
    }

}
