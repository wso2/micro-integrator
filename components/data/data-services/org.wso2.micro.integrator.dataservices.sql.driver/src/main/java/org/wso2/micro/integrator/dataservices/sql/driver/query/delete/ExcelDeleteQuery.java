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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.Sheet;
import org.wso2.micro.integrator.dataservices.sql.driver.TDriverUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.TExcelConnection;

public class ExcelDeleteQuery extends DeleteQuery {

    public ExcelDeleteQuery(Statement stmt) throws SQLException {
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

    private int executeSQL() throws SQLException {
        TExcelConnection excelCon = (TExcelConnection)getConnection();
        //begin transaction,
        try {
            excelCon.beginExcelTransaction();
            Sheet currentWorkSheet = excelCon.getWorkbook().getSheet(getTargetTableName());
            for (Integer rowId : this.getResultantRows().keySet()) {
                currentWorkSheet.removeRow(currentWorkSheet.getRow(rowId + 1));
            }
            TDriverUtil.writeRecords(excelCon.getWorkbook(), excelCon.getPath());
            return this.getResultantRows().size();
        } finally {
            excelCon.close();
        }
    }

}
