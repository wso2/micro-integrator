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
package org.wso2.micro.integrator.dataservices.sql.driver.processor.writer;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.wso2.micro.integrator.dataservices.sql.driver.TExcelConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataCell;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;

public class ExcelDataWriter implements DataWriter {

    private String filePath;

    private Map<String, DataTable> data;

    private static final Log log = LogFactory.getLog(ExcelDataWriter.class);

    private TExcelConnection connection;

    public ExcelDataWriter(TExcelConnection connection, Map<String, DataTable> data) {
        this.connection = connection;
        this.data = data;
    }

    public void writeData() throws SQLException {
        Set<DataTable> tables = (Set<DataTable>) this.getData().values();
        for (DataTable table : tables) {
            Set<DataRow> rows = (Set<DataRow>) table.getRows().values();
            for (DataRow row : rows) {
                Set<DataCell> cells = (Set<DataCell>) row.getCells().values();
                for (DataCell cell : cells) {

                }
            }
        }
    }

    public Map<String, DataTable> getData() {
        return data;
    }

    private void commitCell(DataCell cell) {
        int cellType = cell.getCellType();
        switch(cellType) {
            case Cell.CELL_TYPE_NUMERIC:

            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_STRING:

            case Cell.CELL_TYPE_BOOLEAN:

            default:

        }
    }
    
}
