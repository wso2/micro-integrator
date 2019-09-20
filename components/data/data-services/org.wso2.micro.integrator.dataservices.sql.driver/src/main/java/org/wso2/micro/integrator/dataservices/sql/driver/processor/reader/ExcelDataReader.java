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
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.micro.integrator.dataservices.sql.driver.TConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.TExcelConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

public class ExcelDataReader extends AbstractFixedDataReader {

    public ExcelDataReader(Connection connection) throws SQLException {
        super(connection);
    }

    public void populateData() throws SQLException {
        Workbook workbook = ((TExcelConnection) getConnection()).getWorkbook();
        int noOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < noOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            ColumnInfo[] headers = this.extractColumnHeaders(sheet);
            DataTable dataTable = new FixedDataTable(sheetName, headers);

            Iterator<Row> rowItr = sheet.rowIterator();
            while (rowItr.hasNext()) {
                Row row = rowItr.next();
                if (row.getRowNum() != 0) {
                    DataRow dataRow = new DataRow(row.getRowNum() - 1);
                    Iterator<Cell> cellItr = row.cellIterator();
                    int cellIndex = 0;
                    while (cellItr.hasNext()) {
                        Cell cell = cellItr.next();
                        DataCell dataCell =
                                new DataCell(cellIndex + 1, cell.getCellType(), extractCellValue(cell));
                        dataRow.addCell(dataCell.getColumnId(), dataCell);
                        cellIndex++;
                    }
                    dataTable.addRow(dataRow);
                }
            }
            this.getData().put(dataTable.getTableName(), dataTable);
        }
    }

    /**
     * Extracts the value of a particular cell depending on its type
     *
     * @param cell A populated Cell instance
     * @return Value of the cell
     */
    private Object extractCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return cell.getStringCellValue();
        }
    }

    /**
     * Extracts out the columns in the given excel sheet
     *
     * @param sheet Sheet instance corresponding to the desired Excel sheet
     * @return Array containing the column header data
     * @throws SQLException SQLException
     */
    private ColumnInfo[] extractColumnHeaders(Sheet sheet) throws SQLException {
        List<ColumnInfo> headers = new ArrayList<ColumnInfo>();

        /* If hasHeader property is set to false, populate header map with column names following
         * the format 'COLUMN' + 'i' where i corresponds to the column id */
        if (!((TConnection)getConnection()).hasHeader()) {
            int maxColumns = ((TConnection)getConnection()).getMaxColumns();
            for (int i = 0; i < maxColumns; i++) {
                headers.add(new ColumnInfo(i + 1, Constants.COLUMN + (i + 1), sheet.getSheetName(),
                        -1, i + 1));
            }
            return headers.toArray(new ColumnInfo[headers.size()]);
        }
        // Retrieving the first row of the sheet as the header row.
        Row row = sheet.getRow(0);
        if (row != null) {
            Iterator<Cell> itr = row.cellIterator();
            while (itr.hasNext()) {
                Cell cell = itr.next();
                if (cell != null) {
                    int cellType = cell.getCellType();
                    switch (cellType) {
                        case Cell.CELL_TYPE_STRING:
                            headers.add(new ColumnInfo(cell.getColumnIndex() + 1,
                                    cell.getStringCellValue(), sheet.getSheetName(), Types.VARCHAR,
                                    cell.getColumnIndex() + 1));
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            headers.add(new ColumnInfo(cell.getColumnIndex() + 1,
                                    String.valueOf(cell.getNumericCellValue()),
                                    sheet.getSheetName(), Types.INTEGER,
                                    cell.getColumnIndex() + 1));
                            break;
                        default:
                            throw new SQLException("Invalid column type");
                    }
                }
            }
        }
        return headers.toArray(new ColumnInfo[headers.size()]);
    }

}
