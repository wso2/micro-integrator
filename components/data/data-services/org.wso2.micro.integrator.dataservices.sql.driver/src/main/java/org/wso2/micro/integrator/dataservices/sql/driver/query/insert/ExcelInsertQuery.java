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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.micro.integrator.dataservices.sql.driver.TDriverUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.TExcelConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ParamInfo;

public class ExcelInsertQuery extends InsertQuery {

    public ExcelInsertQuery(Statement stmt) throws SQLException {
        super(stmt);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        executeSQL();
        return null; //TODO: Need to check how the usual SQLs behave when used with execute() method
    }

    @Override
    public boolean execute() throws SQLException {
        return (executeSQL() > 0);
    }

    @Override
    public int executeUpdate() throws SQLException {
        return executeSQL();
    }

    private synchronized int executeSQL() throws SQLException {
        int rowCount = 0;
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
            int lastRowNo = sheet.getLastRowNum();

            if (getParameters() != null) {
                Row row = sheet.createRow(lastRowNo + 1);
                for (ParamInfo param : getParameters()) {
                    Cell cell = row.createCell(param.getOrdinal());
                    switch (param.getSqlType()) {
                        case Types.VARCHAR:
                            cell.setCellValue((String) param.getValue());
                            break;
                        case Types.INTEGER:
                            cell.setCellValue((Integer) param.getValue());
                            break;
                        case Types.DOUBLE:
                            cell.setCellValue((Double) param.getValue());
                            break;
                        case Types.BOOLEAN:
                            cell.setCellValue((Boolean) param.getValue());
                            break;
                        case Types.DATE:
                            cell.setCellValue((Date) param.getValue());
                            break;
                        default:
                            cell.setCellValue((String) param.getValue());
                            break;
                    }
                }
                rowCount++;
            }
            TDriverUtil.writeRecords(workbook, ((TExcelConnection) getConnection()).getPath());
            return rowCount;
        } finally {
            excelConnection.close();
        }
    }

}
