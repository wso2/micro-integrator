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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.ParserUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataReaderFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ConditionalQuery;

public abstract class UpdateQuery extends ConditionalQuery {

    private String targetTableName;

    private DataTable targetTable;

    private ColumnInfo[] targetColumns;

    public UpdateQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.targetTableName = this.extractTargetTableName(getProcessedTokens());
        this.targetColumns = this.extractUpdatedColumns(getProcessedTokens());
        this.populateConditions(getProcessedTokens());
        this.targetTable =
                DataReaderFactory.createDataReader(getConnection()).getDataTable(
                        getTargetTableName());
    }

    private String extractTargetTableName(Queue<String> tokens) throws SQLException {
        /* Dropping UPDATE token */
        tokens.poll();
        if (!Constants.TABLE.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : 'TABLE' keyword is expected");
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        return tokens.poll();
    }

    private ColumnInfo[] extractUpdatedColumns(Queue<String> tokens) throws SQLException {
        /* Dropping SET token */
        tokens.poll();
        List<ColumnInfo> updatedColumns = new ArrayList<ColumnInfo>();
        this.processUpdatedColumns(tokens, updatedColumns, 0);
        return updatedColumns.toArray(new ColumnInfo[updatedColumns.size()]);
    }

    private void populateConditions(Queue<String> tokens) throws SQLException {
        if (tokens.isEmpty()) {
            return;
        }
        /* drops WHERE */
        tokens.poll();
        this.processConditions(tokens, getCondition());
    }

    private void processUpdatedColumns(Queue<String> tokens, List<ColumnInfo> updatedColumns,
                                       int targetColCount) throws SQLException {
        /* drops COLUMN */
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        ColumnInfo updatedColumn = new ColumnInfo(tokens.poll(), targetColCount);
        /* drops OPERATOR */
        tokens.poll();
        /* drops '=' */
        tokens.poll();
        /* drops PARAM_VALUE */
        tokens.poll();
        /* sets the value of the target column */
        updatedColumn.setValue(this.extractColumnValue(tokens));
        updatedColumns.add(updatedColumn);

        if (!tokens.isEmpty() && !Constants.WHERE.equalsIgnoreCase(tokens.peek())) {
            processUpdatedColumns(tokens, updatedColumns, targetColCount + 1);
        }
    }

    private String extractColumnValue(Queue<String> tokens) throws SQLException {
        StringBuilder value = new StringBuilder();
        if ("?".equalsIgnoreCase(tokens.peek()) || ParserUtil.isStringLiteral(tokens.peek())) {
            value.append(tokens.poll());
        } else if (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
            while (!Constants.SINGLE_QUOTATION.equals(tokens.peek())) {
                value.append(tokens.poll());
            }
            /* drops the ending SINGLE QUOTE */
            tokens.poll();
        }
        return value.toString();
    }

    public DataTable getTargetTable() {
        return targetTable;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public ColumnInfo[] getTargetColumns() {
        return targetColumns;
    }

}
