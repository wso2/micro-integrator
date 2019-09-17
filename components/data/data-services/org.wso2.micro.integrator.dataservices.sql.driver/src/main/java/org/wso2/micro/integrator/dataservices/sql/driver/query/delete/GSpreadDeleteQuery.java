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
package org.wso2.micro.integrator.dataservices.sql.driver.query.delete;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import org.wso2.micro.integrator.dataservices.sql.driver.TGSpreadConnection;

public class GSpreadDeleteQuery extends DeleteQuery {

    public GSpreadDeleteQuery(Statement stmt) throws SQLException {
        super(stmt);
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
        int count = 0;

        TGSpreadConnection connection = (TGSpreadConnection) getConnection();
        SpreadsheetFeed spreadsheetFeed =
                connection.getSpreadSheetFeed();
        SpreadsheetEntry spreadsheet = spreadsheetFeed.getEntries().get(0);
        try {
            WorksheetFeed worksheetFeed =
                    connection.getFeedProcessor().getFeed(spreadsheet.getWorksheetFeedUrl(),
                            WorksheetFeed.class);
            WorksheetEntry currentWorksheet = null;
            for (WorksheetEntry tmp : worksheetFeed.getEntries()) {
                if (getTargetTableName().equals(tmp.getTitle().getPlainText())) {
                    currentWorksheet = tmp;
                    break;
                }
            }
            if (currentWorksheet == null) {
                throw new SQLException("Sheet '" + getTargetTableName() + "' does not exist");
            }
            ListFeed listFeed =
                    connection.getFeedProcessor().getFeed(currentWorksheet.getListFeedUrl(), ListFeed.class);
            Set<Integer> rowKeys = this.getResultantRows().keySet();

            for (Integer rowKey : rowKeys) {
                listFeed.getEntries().get(rowKey - 1).delete();
                count++;
            }
        } catch (IOException e) {
            throw new SQLException("Error occurred while deleting the row", e);
        } catch (ServiceException e) {
            throw new SQLException("Error occurred while deleting the row", e);
        }
        return count;
    }

}
