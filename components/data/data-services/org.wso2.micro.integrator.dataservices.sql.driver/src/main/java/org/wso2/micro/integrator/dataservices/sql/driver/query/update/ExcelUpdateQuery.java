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
package org.wso2.micro.integrator.dataservices.sql.driver.query.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.micro.integrator.dataservices.sql.driver.TDriverUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.TExcelConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

public class ExcelUpdateQuery extends UpdateQuery {

    public ExcelUpdateQuery(Statement stmt) throws SQLException {
        super(stmt);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        executeSQL();
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return executeSQL();
    }

    @Override
    public boolean execute() throws SQLException {
        return (executeSQL() > 0);
    }

    private int executeSQL() throws SQLException {
        Map<Integer, DataRow> result;
        if (getCondition().getLhs() == null && getCondition().getRhs() == null) {
            result = getTargetTable().getRows();
        } else {
            result = getCondition().process(getTargetTable());
        }

        if (!(getConnection() instanceof TExcelConnection)) {
            throw new SQLException("Connection does not refer to a Excel connection");
        }
        TExcelConnection excelConnection = (TExcelConnection) this.getConnection();
        //begin transaction,
        try {
            excelConnection.beginExcelTransaction();
            Workbook workbook = excelConnection.getWorkbook();
            Sheet sheet = workbook.getSheet(getTargetTableName());
            if (sheet == null) {
                throw new SQLException("Excel sheet named '" + this.getTargetTableName() +
                        "' does not exist");
            }

            ColumnInfo[] headers = TDriverUtil.getHeaders(getConnection(), getTargetTableName());
            for (Map.Entry<Integer, DataRow> row : result.entrySet()) {
                Row updatedRow = sheet.getRow(row.getKey() + 1);
                for (ColumnInfo column : getTargetColumns()) {
                    int columnId = findColumnId(headers, column.getName());
                    updatedRow.getCell(columnId).setCellValue(column.getValue().toString());
                }
            }
            TDriverUtil.writeRecords(workbook, ((TExcelConnection) getConnection()).getPath());
            return 0;
        } finally {
            excelConnection.close();
        }
    }

    private int findColumnId(ColumnInfo[] headers, String headerName) throws SQLException {
        ColumnInfo column = null;
        for (ColumnInfo header : headers) {
            if (headerName.equalsIgnoreCase(header.getName())) {
                column = header;
            }
        }
        if (column == null) {
            throw new SQLException("Column '" + headerName + "' does not exist");
        }
        return column.getId();
    }

}
