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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import org.wso2.micro.integrator.dataservices.sql.driver.TConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.TDriverUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.TGSpreadConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

public class GSpreadDataReader extends AbstractFixedDataReader {

    public GSpreadDataReader(Connection connection) throws SQLException {
        super(connection);
    }

    public void populateData() throws SQLException {
        int tmp = -1;

        TGSpreadConnection gsConnection = (TGSpreadConnection) getConnection();
        WorksheetFeed workSheetFeed = gsConnection.getWorksheetFeed();
        if (workSheetFeed == null) {
            throw new SQLException("Work sheet feed it not initialized properly and is null");
        }
        List<WorksheetEntry> workSheets = workSheetFeed.getEntries();
        for (WorksheetEntry workSheet : workSheets) {
            DataRow dataRow = null;
            CellFeed cellFeed = TDriverUtil.getGSpreadCellFeed((TGSpreadConnection) getConnection(), workSheet);

            ColumnInfo[] headers = this.extractHeaders(workSheet);
            DataTable result = new FixedDataTable(workSheet.getTitle().getPlainText(), headers);
            for (CellEntry cell : cellFeed.getEntries()) {
                int rowId = TDriverUtil.getRowIndex(cell.getId());
                if (tmp != rowId && rowId != 1) {
                    if (dataRow != null) {
                        result.addRow(this.fillUpEmptyCells(dataRow, headers));
                    }
                    dataRow = new DataRow(rowId - 1);
                    tmp = rowId;
                }
                int columnId = TDriverUtil.getColumnIndex(cell.getId());
                if (columnId > headers.length) {
                    continue;
                }
                if (rowId != 1 && dataRow != null) {
                    DataCell dataCell =
                            new DataCell(TDriverUtil.getColumnIndex(cell.getId()),
                                    cell.getContent().getType(),
                                    cell.getTextContent().getContent().getPlainText());

                    dataRow.addCell(dataCell.getColumnId(), dataCell);
                }
            }
            /* adding the last row of the sheet */
            if (dataRow != null) {
                result.addRow(this.fillUpEmptyCells(dataRow, headers));
            }
            this.getData().put(result.getTableName(), result);
        }
    }
    
    /**
     * Google gdata-client spreadsheet API only returns the non-empty cells that exist in the
     * spreadsheet document that is being queried. This method fills up the data rows with the
     * dummy cells containing null as cell value in place of the missing empty rows.
     *
     * @param row     Data row to be modified
     * @param columns Column indices of the header row
     * @return Processed data row to add empty data cells
     */
    private DataRow fillUpEmptyCells(DataRow row, ColumnInfo[] columns) {
        DataRow fixedRow = new DataRow(row.getRowId());
        Set<Integer> existingColumns = row.getCells().keySet();
        for (ColumnInfo column : columns) {
            if (existingColumns.contains(column.getId())) {
                fixedRow.addCell(column.getId(), row.getCell(column.getId()));
            } else {
                fixedRow.addCell(column.getId(), new DataCell(column.getId(), -1, null));
            }
        }
        return fixedRow;
    }

    /**
     * Extracts out the header elements of the spreadsheet entry that is being queried.
     *
     * @param currentWorkSheet Worksheet being queried
     * @return Map containing the header names and their indices
     * @throws SQLException Is thrown if an error occurs while extracting the spreadsheet
     *                      cell feed
     */
    private ColumnInfo[] extractHeaders(WorksheetEntry currentWorkSheet) throws
            SQLException {
        List<ColumnInfo> headers = new ArrayList<ColumnInfo>();

        /* If hasHeader property is set to false, populate header map with column names following
         * the format 'COLUMN' + 'i' where i corresponds to the column id */
        if (!((TConnection) getConnection()).hasHeader()) {
            int maxColumns = ((TConnection) getConnection()).getMaxColumns();
            for (int i = 1; i < maxColumns + 1; i++) {
                headers.add(new ColumnInfo(i, Constants.COLUMN + i,
                        currentWorkSheet.getTitle().getPlainText(), -1, i));
            }
            return headers.toArray(new ColumnInfo[headers.size()]);
        }

        CellFeed cellFeed = TDriverUtil.getGSpreadCellFeed((TGSpreadConnection) getConnection(), currentWorkSheet);
        for (CellEntry cell : cellFeed.getEntries()) {
            if (!TDriverUtil.getCellPosition(cell.getId()).startsWith("R1")) {
                break;
            }
            int columnIndex = TDriverUtil.getColumnIndex(cell.getId());
            headers.add(new ColumnInfo(
                    columnIndex, cell.getTextContent().getContent().getPlainText(),
                    currentWorkSheet.getTitle().getPlainText(), Types.VARCHAR, columnIndex));
        }
        return headers.toArray(new ColumnInfo[headers.size()]);
    }

}
