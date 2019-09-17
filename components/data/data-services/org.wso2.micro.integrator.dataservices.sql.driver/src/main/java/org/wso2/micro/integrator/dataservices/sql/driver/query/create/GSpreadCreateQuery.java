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
package org.wso2.micro.integrator.dataservices.sql.driver.query.create;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import org.wso2.micro.integrator.dataservices.sql.driver.TGSpreadConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.util.GSpreadFeedProcessor;

public class GSpreadCreateQuery extends CreateQuery {

    public GSpreadCreateQuery(Statement stmt) throws SQLException {
        super(stmt);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        this.executeSQL();
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        this.executeSQL();
        return 0;
    }

    @Override
    public boolean execute() throws SQLException {
        this.executeSQL();
        return false;
    }

    private synchronized void executeSQL() throws SQLException {
        SpreadsheetEntry currentSpreadSheet;
        WorksheetEntry currentWorkSheet;
        CellFeed cellFeed;

        TGSpreadConnection gspreadCon = (TGSpreadConnection) getConnection();
        GSpreadFeedProcessor feedProcessor = gspreadCon.getFeedProcessor();
        if (isWorkSheetExists(gspreadCon)) {
            throw new SQLException("A sheet named '" + this.getTableName() + "' already exists");
        }

        WorksheetEntry newWorkSheet = new WorksheetEntry();
        newWorkSheet.setTitle(new PlainTextConstruct(this.getTableName()));
        newWorkSheet.setRowCount(1);
        newWorkSheet.setColCount(this.getColumns().size());

        currentSpreadSheet = gspreadCon.getSpreadSheetFeed().getEntries().get(0);
        feedProcessor.insert(currentSpreadSheet.getWorksheetFeedUrl(),
                             newWorkSheet);
        currentWorkSheet = this.getCurrentWorksheetEntry(gspreadCon);

        try {
            cellFeed = feedProcessor.getFeed(currentWorkSheet.getCellFeedUrl(),
                    CellFeed.class);
            for (int i = 0; i < this.getColumns().size(); i++) {
                CellEntry cell = new CellEntry(1, i + 1, this.getColumns().get(i).getName());
                cellFeed.insert(cell);
            }
        } catch (IOException e) {
            throw new SQLException("Error occurred while adding column header to the sheet '" +
                    this.getTableName() + "'", e);
        } catch (ServiceException e) {
            throw new SQLException("Error occurred while adding column header to the sheet '" +
                    this.getTableName() + "'", e);
        }
    }

    private boolean isWorkSheetExists(TGSpreadConnection conn) {
        WorksheetFeed worksheetFeed = conn.getWorksheetFeed();
        for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
            if (this.getTableName().equals(worksheet.getTitle().getPlainText())) {
                return true;
            }
        }
        return false;
    }

    private WorksheetEntry getCurrentWorksheetEntry(TGSpreadConnection conn) throws SQLException {
        WorksheetEntry currentWorksheetEntry = null;
        SpreadsheetEntry currentSpreadsheetEntry =
                conn.getSpreadSheetFeed().getEntries().get(0);
        WorksheetFeed worksheetFeed =
                conn.getFeedProcessor().getFeed(
                        currentSpreadsheetEntry.getWorksheetFeedUrl(), WorksheetFeed.class);
        for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
            if (this.getTableName().equals(worksheetEntry.getTitle().getPlainText())) {
                currentWorksheetEntry = worksheetEntry;
                break;
            }
        }
        return currentWorksheetEntry;
    }

}
