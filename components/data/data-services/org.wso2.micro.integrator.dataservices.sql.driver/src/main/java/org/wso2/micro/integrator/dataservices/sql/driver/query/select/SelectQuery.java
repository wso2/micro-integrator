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
package org.wso2.micro.integrator.dataservices.sql.driver.query.select;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.TResultSet;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.ParserUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataReaderFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.FixedDataTable;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ConditionalQuery;

public abstract class SelectQuery extends ConditionalQuery {

    private ColumnInfo[] targetColumns;

    private String targetTableName;

    private DataTable targetTable;

    private boolean isAllColumnsSelected;

    public SelectQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.isAllColumnsSelected = this.checkAllColumnsSelected(getProcessedTokens());
        this.targetColumns = this.extractTargetColumns(getProcessedTokens());
        this.targetTableName = this.extractTargetTableName(getProcessedTokens());
        this.populateConditions(getProcessedTokens());
        this.targetTable = DataReaderFactory.createDataReader(getConnection()).getDataTable(
                getTargetTableName());
        /* Handling the scenario where the user selects ALL ('*') columns to be in the SELECT
           query result. Here, the need of differed assignment of the targetColumns field is needed
           as the tableName is not present at the time the column processing is initially done */
        if (isAllColumnsSelected()) {
            this.targetColumns = this.getTargetTable().getHeaders();
        }
        this.processTargetColumnIds();
    }

    @Override
    public int executeUpdate() throws SQLException {
        throw new SQLException("'executeUpdate() is only allowed to be used with DML statements " +
                "such as INSERT, UPDATE and DELETE");
    }

    public synchronized ResultSet executeSQL() throws SQLException {
        Map<Integer, DataRow> result;
        FixedDataTable table =
                new FixedDataTable(getTargetTableName(), this.getTargetTable().getHeaders());

        if (getCondition().getLhs() == null && getCondition().getRhs() == null) {
            result = getTargetTable().getRows();
        } else {
            result = getCondition().process(getTargetTable());
        }
        table.setData(filterColumns(result));
        return new TResultSet(getStatement(), table, getTargetColumns());
    }

    private Map<Integer, DataRow> filterColumns(Map<Integer, DataRow> rows) throws SQLException {
        Map<Integer, DataRow> filteredData = new HashMap<Integer, DataRow>();
        for (Map.Entry<Integer, DataRow> entry : rows.entrySet()) {
            DataRow row = entry.getValue();
            DataRow filteredRow = new DataRow(entry.getValue().getRowId());
            for (ColumnInfo column : this.getTargetColumns()) {
                filteredRow.addCell(column.getOrdinal(), row.getCell(column.getId()));
            }
            filteredData.put(filteredRow.getRowId(), filteredRow);
        }
        return filteredData;
    }

    private ColumnInfo[] extractTargetColumns(Queue<String> tokens) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        if (this.isAllColumnsSelected()) {
            /* Drops ASTERISK */
            tokens.poll();
        } else {
            this.processTargetColumns(tokens, 1, columns);
        }
        return columns.toArray(new ColumnInfo[columns.size()]);
    }

    private boolean checkAllColumnsSelected(Queue<String> tokens) {
        /* Drops SELECT keyword */
        tokens.poll();
        return Constants.ASTERISK.equalsIgnoreCase(tokens.peek());
    }

    private String extractTargetTableName(Queue<String> tokens) throws SQLException {
        /* Drops FROM keyword */
        tokens.poll();
        if (!Constants.TABLE.equals(tokens.peek())) {
            throw new SQLException("'TABLE' keyword is expected");
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        return tokens.poll();
    }

    private void populateConditions(Queue<String> tokens) throws SQLException {
        if (tokens.isEmpty()) {
            return;
        }
        if (!Constants.WHERE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'WHERE' keyword is expected");
        }
        //Removing WHERE keyword
        tokens.poll();
        this.processConditions(tokens, getCondition());
    }

    private void processTargetColumnIds() throws SQLException {
        for (ColumnInfo column : this.getTargetColumns()) {
            ColumnInfo header = this.getTargetTable().getHeader(column.getName());
            column.setId(header.getId());
        }
    }

    private void processTargetColumns(Queue<String> tokens, int count,
                                      List<ColumnInfo> columns) throws SQLException {
        if (!Constants.COLUMN.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : 'COLUMN' keyword is expected");
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        ColumnInfo columnInfo = new ColumnInfo(tokens.poll(), count);
        if (Constants.AS.equals(tokens.peek())) {
            tokens.poll();
            // Set the new column name given by "AS" keyword.
            columnInfo.setAliasName(tokens.poll());
        }
        columns.add(columnInfo);
        if (Constants.COLUMN.equals(tokens.peek())) {
            processTargetColumns(tokens, count + 1, columns);
        }
    }

    public ColumnInfo[] getTargetColumns() {
        return targetColumns;
    }

    public DataTable getTargetTable() {
        return targetTable;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public boolean isAllColumnsSelected() {
        return isAllColumnsSelected;
    }

}
