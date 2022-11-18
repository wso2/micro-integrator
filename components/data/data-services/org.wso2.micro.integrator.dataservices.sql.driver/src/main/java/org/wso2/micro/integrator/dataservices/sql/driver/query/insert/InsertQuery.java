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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.TDriverUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.ParserUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.micro.integrator.dataservices.sql.driver.query.Query;

public abstract class InsertQuery extends Query {

    private String targetTableName;

    private Map<Integer, String> columns;

    private Map<Integer, Object> columnValues;

    public InsertQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.targetTableName = this.extractTargetTableName(getProcessedTokens());
        this.columns = this.extractTargetColumns(getProcessedTokens());
        this.columnValues = this.extractTargetColumnValues(getProcessedTokens());
        if (this.getColumns().size() != this.getColumnValues().size()) {
            throw new SQLException("Parameter index is out of range. The column count does not " +
                    "match the value count");
        }
    }

    private String extractTargetTableName(Queue<String> tokens) throws SQLException {
        if (tokens == null || tokens.isEmpty()) {
            throw new SQLException("Unable to populate attributes");
        }
        /* Drops INSERT keyword */
        tokens.poll();
        /* Drops INTO keyword */
        tokens.poll();
        if (!Constants.TABLE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Table name is missing");
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Table name is missing");
        }
        return tokens.poll();
    }

    private Map<Integer, String> extractTargetColumns(Queue<String> tokens) throws SQLException {
        Map<Integer, String> targetColumns = new HashMap<Integer, String>();
        if (Constants.COLUMN.equals(tokens.peek())) {
            this.processColumnNames(tokens, targetColumns, 0);
        } else {
            targetColumns = this.getColumnMap();
        }
        return targetColumns;
    }

    private Map<Integer, Object> extractTargetColumnValues(Queue<String> tokens) throws
            SQLException {
        Map<Integer, Object> targetColumnValues = new HashMap<Integer, Object>();
        if (!(Constants.VALUES.equalsIgnoreCase(tokens.peek()) ||
                Constants.VALUE.equalsIgnoreCase(tokens.peek()))) {
            throw new SQLException("VALUE/VALUES keyword is missing");
        }
        tokens.poll();
        processColumnValues(tokens, targetColumnValues, 0, false, false, true);
        return targetColumnValues;
    }

    private void processColumnNames(Queue<String> tokens, Map<Integer, String> targetColumns,
                                    int colCount) throws SQLException {
        if (!Constants.COLUMN.equalsIgnoreCase(tokens.peek())) {
            return;
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        targetColumns.put(colCount, tokens.poll());
        if (Constants.COLUMN.equalsIgnoreCase(tokens.peek())) {
            processColumnNames(tokens, targetColumns, colCount + 1);
        }
    }

    private void processColumnValues(Queue<String> tokens, Map<Integer, Object> targetColumnValues,
                                     int valCount, boolean isParameterized, boolean isEnd,
                                     boolean isInit) throws SQLException {
        if (!isEnd) {
            if (!Constants.PARAM_VALUE.equalsIgnoreCase(tokens.peek())) {
                throw new SQLException("Syntax Error : 'PARAM_VALUE' is expected");
            }
            tokens.poll();
            if ("?".equalsIgnoreCase(tokens.peek())) {
                if (isInit) {
                    isParameterized = true;
                    isInit = false;
                }
                if (!isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                isParameterized = true;
                targetColumnValues.put(valCount, tokens.poll());
            } else if (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                tokens.poll();
                StringBuilder b = new StringBuilder();
                while (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek()) ||
                        tokens.isEmpty()) {
                    b.append(tokens.poll());
                }
                targetColumnValues.put(valCount, b.toString());
                tokens.poll();
            } else {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                targetColumnValues.put(valCount, tokens.poll());
            }
            if (!Constants.PARAM_VALUE.equalsIgnoreCase(tokens.peek())) {
                isEnd = true;
            }
            processColumnValues(tokens, targetColumnValues, valCount + 1, isParameterized, isEnd,
                    isInit);
        }
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public Map<Integer, String> getColumns() {
        return columns;
    }

    public Map<Integer, Object> getColumnValues() {
        return columnValues;
    }

    private Map<Integer, String> getColumnMap() throws SQLException {
        ColumnInfo[] headers =
                TDriverUtil.getHeaders(this.getConnection(), this.getTargetTableName());
        Map<Integer, String> columns = new HashMap<Integer, String>();
        for (ColumnInfo column : headers) {
            columns.put(column.getId(), column.getName());
        }
        return columns;
    }

}
