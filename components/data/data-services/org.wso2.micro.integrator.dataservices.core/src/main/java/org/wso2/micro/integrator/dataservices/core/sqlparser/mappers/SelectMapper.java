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
package org.wso2.micro.integrator.dataservices.core.sqlparser.mappers;

import org.wso2.micro.integrator.dataservices.core.sqlparser.DataManipulator;
import org.wso2.micro.integrator.dataservices.core.sqlparser.LexicalConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * This classes maps SQL statements that appear between SELECT and FROM
 * keywords of a SQL query.
 */
public class SelectMapper {

    private Queue<String> processedTokens;
    private List<String> concatData;
    private List<String> columns;
    private DataManipulator dataManipulator;
    private boolean tableRefOn = false;

    public SelectMapper(Queue<String> processedTokens) {
        this.processedTokens = processedTokens;
        this.columns = new ArrayList<String>();
        this.dataManipulator = new DataManipulator();
        processSyntaxQueue();
    }

    /**
     * Processes the syntax data embedded token queue.
     */
    private void processSyntaxQueue() {

        while (processedTokens != null && !processedTokens.isEmpty()) {
            if (processedTokens.peek().equals(LexicalConstants.STRINGFUNC)) {
                processStringFunction(processedTokens.peek());
            } else if (processedTokens.peek().equals(LexicalConstants.AGGREGATEFUNC)) {
                processAggregateFunction(processedTokens.peek());
            } else {
                processOrdinaryFunction(processedTokens.peek());
            }
        }
    }

    /**
     * Processes Gerenal SQL statements that appear after the SELECT keyword.
     *
     * @param processedToken processedToken
     */
    private void processOrdinaryFunction(String processedToken) {

        if (processedToken.equals(LexicalConstants.COLUMN)) {
            processedTokens.poll();
            if (!processedTokens.peek().equals(LexicalConstants.ASTERISK)) {
                columns.add(processedTokens.poll());
            } else {
                processedTokens.poll();
                columns.add(LexicalConstants.ALL);
            }
        } else if (processedToken.equals(LexicalConstants.TABLE)) {
            tableRefOn = true;
            processedTokens.poll();
            processedTokens.poll();
            processOrdinaryFunction(processedTokens.peek());
        } else if (processedToken.equals(LexicalConstants.DOT) && tableRefOn) {
            processedTokens.poll();
            processOrdinaryFunction(processedTokens.peek());
        } else if (processedToken.equals(LexicalConstants.COLUMN)) {
            tableRefOn = false;
            processedTokens.poll();
            columns.add(LexicalConstants.COLUMN);
            columns.add(processedTokens.poll());
            if (!processedTokens.isEmpty()) {
                processOrdinaryFunction(processedTokens.peek());
            }
        }
    }

    /**
     * Processes the Aggregate Functions specified in a SQL query.
     *
     * @param processedToken processedToken
     */
    private void processAggregateFunction(String processedToken) {

        if (processedToken.equals(LexicalConstants.COUNT)) {
            dataManipulator.processCount(processedTokens);
        } else if (processedToken.equals(LexicalConstants.SUM)) {
            dataManipulator.processSum(processedTokens);
        } else if (processedToken.equals(LexicalConstants.AVG)) {
            dataManipulator.processAvg(processedTokens);
        } else if (processedToken.equals(LexicalConstants.MIN)) {
            dataManipulator.processMin(processedTokens);
        } else if (processedToken.equals(LexicalConstants.MAX)) {
            dataManipulator.processMax(processedTokens);
        }
    }

    /**
     * Processes the String Functions specified in a SQL query
     *
     * @param processedToken processedToken
     */
    private void processStringFunction(String processedToken) {
        List<String> columnData;
        String concatOperator;
        processedTokens.poll();
        if (processedTokens.peek().equals(LexicalConstants.CONCAT)) {
            processedTokens.poll();
            do {
                if (processedTokens.peek().equals(LexicalConstants.COLUMN)) {
                    processedTokens.poll();
                    columnData = dataManipulator.retrieveColumnData(processedTokens.poll());
                    if (processedTokens.peek().equals(LexicalConstants.OPVALUE)) {
                        processedTokens.poll();
                        concatOperator = processedTokens.poll();
                        concatData =
                                dataManipulator.concatDataFunction(columnData, concatOperator,
                                        concatData);
                    } else {
                        concatData = dataManipulator.concatDataFunction(columnData, null,
                                concatData);
                    }
                }
            } while ((!processedTokens.peek().equals(LexicalConstants.START_OF_RBRACKET)));
        }
    }

    /**
     * Returns the list of column names specified between SELECT and FROM keywords,
     * of a particular SQL query
     *
     * @return list of strings
     */
    public List<String> getColumns() {
        return columns;
    }
}
