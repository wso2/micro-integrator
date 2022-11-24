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
package org.wso2.micro.integrator.dataservices.sql.driver.query.drop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.micro.integrator.dataservices.sql.driver.TDriverUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.TExcelConnection;

public class ExcelDropQuery extends DropQuery {

    public ExcelDropQuery(Statement stmt) throws SQLException {
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
        TExcelConnection excelConnection = (TExcelConnection) this.getConnection();
        //begin transaction,
        try {
            excelConnection.beginExcelTransaction();
            Workbook workbook = excelConnection.getWorkbook();

            if (!isSheetExists(workbook)) {
                throw new SQLException("Excel sheet named '" + this.getTableName() +
                        "' does not exist");
            }

            int sheetIndex = workbook.getSheetIndex(this.getTableName());
            workbook.removeSheetAt(sheetIndex);
            TDriverUtil.writeRecords(workbook, ((TExcelConnection) this.getConnection()).getPath());
        } finally {
            excelConnection.close();
        }
    }

    private boolean isSheetExists(Workbook workbook) {
        Sheet sheet = workbook.getSheet(this.getTableName());
        return (sheet != null);
    }

}
