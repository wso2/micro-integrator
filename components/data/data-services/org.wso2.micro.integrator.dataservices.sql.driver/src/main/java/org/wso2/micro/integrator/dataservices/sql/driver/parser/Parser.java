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
package org.wso2.micro.integrator.dataservices.sql.driver.parser;

import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Parser {

    private static final String DELIMITER = ";";

    public enum QueryTypes {
        SELECT, UPDATE, INSERT, DELETE, DROP, CREATE
    }

    public static Queue<String> parse(String sql, String type) throws SQLException {
        QueryTypes types = QueryTypes.valueOf(type);
        Queue<String> tokens = ParserUtil.getTokens(sql);
        Queue<String> processed = new ConcurrentLinkedQueue<String>();
        switch (types) {
            case SELECT:
                parseSelect(tokens, processed);
                break;
            case INSERT:
                parseInsert(tokens, processed);
                break;
            case UPDATE:
                parseUpdate(tokens, processed);
                break;
            case DELETE:
                parseDelete(tokens, processed);
                break;
            case CREATE:
                parseCreate(tokens, processed);
                break;
            case DROP:
                parseDrop(tokens, processed);
                break;
            default:
                throw new SQLException("Query type unsupported");
        }
        return processed;
    }

    private static void parseDrop(Queue<String> tokens,
                                  Queue<String> processedTokens) throws SQLException {
        if (!Constants.DROP.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'DROP' keyword is expected");
        }
        processedTokens.add(tokens.poll().toUpperCase());
        if (!Constants.SHEET.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'SHEET' keyword is expected");
        }
        processedTokens.add(tokens.poll().toUpperCase());
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        processedTokens.add(Constants.TABLE);
        processedTokens.add(tokens.poll());
        processDelimiter(tokens);
        if (!tokens.isEmpty()) {
            throw new SQLException("Syntax Error : Unusual end to the statement");
        }
    }

    private static void parseCreate(Queue<String> tokens,
                                    Queue<String> processedTokens) throws SQLException {
        if (!Constants.CREATE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'CREATE' keyword is expected");
        }
        processedTokens.add(tokens.poll().toUpperCase());
        if (!Constants.SHEET.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'SHEET' keyword is expected");
        }
        processedTokens.add(tokens.poll().toUpperCase());
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        processedTokens.add(Constants.TABLE);
        processedTokens.add(tokens.poll());
        if (tokens.isEmpty()) {
            /* Handling the possibility where the user creates an empty table without columns */
            return;
        }
        if (!Constants.LEFT_BRACKET.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : '(' is expected");
        }
        tokens.poll();
        processColumnNames(tokens, processedTokens);
        if (!Constants.RIGHT_BRACKET.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : ')' is expected");
        }
    }

    private static void processColumnNames(Queue<String> tokens,
                                           Queue<String> processedTokens) throws SQLException {
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        processedTokens.add(Constants.COLUMN);
        processedTokens.add(tokens.poll());
        if (Constants.COMMA.equals(tokens.peek())) {
            tokens.poll();
            processColumnNames(tokens, processedTokens);
        }
    }

    private static void parseSelect(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {
        if (!Constants.SELECT.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'SELECT' keyword is expected");
        }
        processed.add(tokens.poll().toUpperCase());
        processSelectedColumns(tokens, processed);
        processFromClause(tokens, processed);
        if (tokens.isEmpty()) {
            return;
        }
        processWhereClause(tokens, processed);
    }

    private static void processSelectedColumns(Queue<String> tokens,
                                               Queue<String> processed) throws SQLException {
        if (Constants.ASTERISK.equalsIgnoreCase(tokens.peek())) {
            processed.add(tokens.poll());
            return;
        }
        if (ParserUtil.isStringLiteral(tokens.peek())) {
            processed.add(Constants.COLUMN);
            processed.add(tokens.poll());
            if (Constants.COMMA.equalsIgnoreCase(tokens.peek())) {
                tokens.poll();
                processSelectedColumns(tokens, processed);
            } else if(Constants.AS.equalsIgnoreCase(tokens.peek())){
                tokens.poll();
                processed.add(Constants.AS);
                processed.add(tokens.poll());
                processSelectedColumns(tokens, processed);
            }
        } else if (Constants.COMMA.equalsIgnoreCase(tokens.peek())) {
            tokens.poll();
            processSelectedColumns(tokens, processed);
        }
    }

    @SuppressWarnings("unused")
    private static void processSelectClause(Queue<String> tokens,
                                            Queue<String> processed) throws SQLException {
        StringBuilder sb;
        if (ParserUtil.isAggregateFunction(tokens.peek()) ||
                ParserUtil.isStringFunction(tokens.peek())) {
            if (ParserUtil.isAggregateFunction(tokens.peek())) {
                String aggFunction = tokens.poll();
                processed.add(Constants.AGGREGATE_FUNCTION);
                processed.add(aggFunction);

                if (!tokens.isEmpty()) {
                    throw new SQLException("Syntax Error");
                }
                processSelectClause(tokens, processed);
            } else {
                String strFunction = tokens.poll();
                processed.add(Constants.STRING_FUNCTION);
                processed.add(strFunction);

                if (!tokens.isEmpty()) {
                    throw new SQLException("Syntax Error");
                }
                processSelectClause(tokens, processed);
            }
        } else if (tokens.peek().equalsIgnoreCase(Constants.LEFT_BRACKET)) {
            tokens.poll();
            processed.add(Constants.START_OF_LBRACKET);
            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            processSelectClause(tokens, processed);

        } else if (tokens.peek().equalsIgnoreCase(Constants.RIGHT_BRACKET)) {
            tokens.poll();
            processed.add(Constants.START_OF_RBRACKET);
            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            processSelectClause(tokens, processed);
        } else if (tokens.peek().equalsIgnoreCase(Constants.SINGLE_QUOTATION)) {
            sb = new StringBuilder();
            tokens.poll();
            while (!tokens.peek().equalsIgnoreCase(Constants.SINGLE_QUOTATION)) {
                sb.append(tokens.poll());
            }
            processed.add(Constants.OP_VALUE);
            processed.add(sb.toString());
            tokens.poll();

            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            processSelectClause(tokens, processed);
        } else if (tokens.peek().equalsIgnoreCase(Constants.COMMA)) {
            tokens.poll();
            if (!tokens.isEmpty()) {
                throw new SQLException("Syntax Error");
            }
            processSelectClause(tokens, processed);
        } else if (tokens.peek().equalsIgnoreCase(Constants.AS)) {
            tokens.poll();
            if (ParserUtil.isStringLiteral(tokens.peek())) {
                sb = new StringBuilder();
                while (!tokens.isEmpty() && !tokens.peek().equalsIgnoreCase(Constants.COMMA)) {
                    sb.append(tokens.poll());
                }
                processed.add(Constants.AS_REF);
                processed.add(sb.toString());

                if (!tokens.isEmpty()) {
                    throw new SQLException("Syntax Error");
                }
                processSelectClause(tokens, processed);
            }
        } else {
            String strRef = tokens.poll();
            if (!tokens.isEmpty()) {
                if (tokens.peek().equalsIgnoreCase(Constants.DOT)) {

                    processed.add(Constants.TABLE);
                    processed.add(strRef);
                    tokens.poll();

                    if (!ParserUtil.isStringLiteral(tokens.peek())) {
                        throw new SQLException("Token is not a string literal");
                    }
                    String columnRef = tokens.poll();
                    processed.add(Constants.COLUMN);
                    processed.add(columnRef);

                    if (!tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    if (tokens.peek().equalsIgnoreCase(Constants.COMMA)) {
                        tokens.poll();
                        if (!tokens.isEmpty()) {
                            throw new SQLException("Syntax Error");
                        }
                        processSelectClause(tokens, processed);
                    } else if (tokens.peek().equalsIgnoreCase(Constants.RIGHT_BRACKET)) {
                        if (!tokens.isEmpty()) {
                            throw new SQLException("Syntax Error");
                        }
                        processSelectClause(tokens, processed);
                    }
                } else if (tokens.peek().equalsIgnoreCase(Constants.COMMA)) {
                    processed.add(Constants.COLUMN);
                    processed.add(strRef);
                    tokens.poll();
                    if (tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    processSelectClause(tokens, processed);
                } else if (tokens.peek().equalsIgnoreCase(Constants.AS)) {
                    processed.add(Constants.COLUMN);
                    processed.add(strRef);
                    tokens.poll();
                    processed.add(Constants.AS_COLUMN);
                    processed.add(tokens.poll());
                    if (tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    processSelectClause(tokens, processed);
                } else {
                    if (!ParserUtil.isStringLiteral(tokens.peek())) {
                        return;
                    }
                    processed.add(Constants.COLUMN);
                    processed.add(strRef);
                    if (tokens.isEmpty()) {
                        throw new SQLException("Syntax Error");
                    }
                    processSelectClause(tokens, processed);
                }
            } else {
                processed.add(Constants.COLUMN);
                processed.add(strRef);
            }
        }
    }

    private static void processFromClause(Queue<String> tokens,
                                          Queue<String> processed) throws SQLException {
        if (!Constants.FROM.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'FROM' keyword is missing");
        }
        processed.add(tokens.poll().toUpperCase());
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        processed.add(Constants.TABLE);
        processed.add(tokens.poll());
        if (Constants.COMMA.equalsIgnoreCase(tokens.peek()) ||
                Constants.JOIN.equalsIgnoreCase(tokens.peek()) ||
                Constants.INNER.equalsIgnoreCase(tokens.peek()) ||
                Constants.OUTER.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("JOINs are not supported");
        }
    }

    private static void processWhereClause(Queue<String> tokens,
                                           Queue<String> processed) throws SQLException {
        if (!Constants.WHERE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'WHERE' keyword is expected");
        }
        processed.add(tokens.poll().toUpperCase());
        processConditions(tokens, processed);
    }


    private static void processConditions(Queue<String> tokens,
                                          Queue<String> processed) throws SQLException {
        if (!tokens.isEmpty() && Constants.LEFT_BRACKET.equals(tokens.peek())) {
            processed.add(tokens.poll());
            if (Constants.LEFT_BRACKET.equals(tokens.peek())) {
                processConditions(tokens, processed);
            }
            if (!ParserUtil.isStringLiteral(tokens.peek())) {
                throw new SQLException("Syntax Error : String literal expected");
            }
            processed.add(Constants.COLUMN);
            processed.add(tokens.poll());
//            if (!Constants.EQUAL.equals(tokens.peek())) {
//                throw new SQLException("Syntax Error : '=' is expected");
//            }
            processed.add(Constants.OPERATOR);
            processed.add(tokens.poll());
            processColumnValue(tokens, processed);
            if (!tokens.isEmpty() && Constants.RIGHT_BRACKET.equals(tokens.peek())) {
                processed.add(tokens.poll());
            }
            if (tokens.isEmpty()) {
                return;
            }
            if (!(Constants.OR.equals(tokens.peek()) || Constants.AND.equals(tokens.peek()))) {
                throw new SQLException("Syntax Error : 'OR' or 'AND' keyword is expected");
            }
            processed.add(tokens.poll());
            processConditions(tokens, processed);
        } else if (!tokens.isEmpty() && ParserUtil.isStringLiteral(tokens.peek())) {
            processed.add(Constants.COLUMN);
            processed.add(tokens.poll());
//            if (!Constants.EQUAL.equals(tokens.peek())) {
//                throw new SQLException("Syntax Error : '=' is expected");
//            }
            processed.add(Constants.OPERATOR);
            processed.add(tokens.poll());
            processColumnValue(tokens, processed);
            if (!tokens.isEmpty() && Constants.RIGHT_BRACKET.equals(tokens.peek())) {
                processConditions(tokens, processed);
            }
            if (!tokens.isEmpty()) {
                if (!(Constants.OR.equals(tokens.peek()) || Constants.AND.equals(tokens.peek()))) {
                    throw new SQLException("Syntax Error : 'OR' or 'AND' keyword is expected");
                }
                processed.add(tokens.poll());
                processConditions(tokens, processed);
            }
        } else if (!tokens.isEmpty() && Constants.RIGHT_BRACKET.equals(tokens.peek())) {
            processed.add(tokens.poll());
            if (!tokens.isEmpty() && Constants.RIGHT_BRACKET.equals(tokens.peek())) {
                processConditions(tokens, processed);
            }
            if (tokens.isEmpty()) {
                return;
            }
            if (!(Constants.OR.equals(tokens.peek()) || Constants.AND.equals(tokens.peek()))) {
                throw new SQLException("Syntax Error : 'OR' or 'AND' keyword is expected");
            }
            processed.add(tokens.poll());
            processConditions(tokens, processed);
        }
    }

    private static void parseInsert(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {
        if (!Constants.INSERT.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : INSERT keyword is missing");
        }
        processed.add(tokens.poll().toUpperCase());
        if (!Constants.INTO.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : INTO keyword is missing");
        }
        processed.add(tokens.poll().toUpperCase());
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.TABLE);
        processed.add(tokens.poll());
        if (Constants.LEFT_BRACKET.equalsIgnoreCase(tokens.peek())) {
            tokens.poll();
            processInsertedColumns(tokens, processed);
            if (!Constants.RIGHT_BRACKET.equalsIgnoreCase(tokens.peek())) {
                throw new SQLException("Syntax Error : ')' expected");
            }
            tokens.poll();
        }
        if (!(Constants.VALUES.equalsIgnoreCase(tokens.peek()) ||
                Constants.VALUE.equalsIgnoreCase(tokens.peek()))) {
            throw new SQLException("Syntax Error : VALUE/VALUES keyword missing");
        }
        processed.add(tokens.poll().toUpperCase());
        if (!Constants.LEFT_BRACKET.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : Left bracket is expected");
        }
        tokens.poll();
        processInsertedValues(tokens, processed, false, false, true);
        if (!Constants.RIGHT_BRACKET.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax exception : ')' expected");
        }
        tokens.poll();
        processDelimiter(tokens);
    }

    private static void processInsertedColumns(Queue<String> tokens,
                                               Queue<String> processed) throws SQLException {
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.COLUMN);
        processed.add(tokens.poll());
        if (Constants.COMMA.equalsIgnoreCase(tokens.peek())) {
            tokens.poll();
            processInsertedColumns(tokens, processed);
        }
    }

    private static void processInsertedValues(Queue<String> tokens,
                                              Queue<String> processed,
                                              boolean isParameterized,
                                              boolean isEnd, boolean isInit) throws SQLException {
        if (!isEnd) {
            if (!ParserUtil.isStringLiteral(tokens.peek())) {
                throw new SQLException("Syntax Error : String literal expected");
            }
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
                processed.add(Constants.PARAM_VALUE);
                processed.add(tokens.poll());
            } else if (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                processed.add(tokens.poll());
                StringBuilder b = new StringBuilder();
                while (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek()) ||
                        tokens.isEmpty()) {
                    b.append(tokens.poll());
                }
                processed.add(b.toString());
                processed.add(Constants.SINGLE_QUOTATION);
            } else if (ParserUtil.isStringLiteral(tokens.peek())) {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                processed.add(tokens.peek());
            }
            if (!Constants.COMMA.equalsIgnoreCase(tokens.peek())) {
                isEnd = true;
            } else {
                tokens.poll();
            }
            processInsertedValues(tokens, processed, isParameterized, isEnd, isInit);
        }
    }

    private static void parseUpdate(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {
        if (!Constants.UPDATE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : UPDATE keyword missing");
        }
        processed.add(tokens.poll().toUpperCase());
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : Table name missing");
        }
        processed.add(Constants.TABLE);
        processed.add(tokens.poll());
        if (!Constants.SET.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : SET keyword missing");
        }
        processed.add(tokens.poll().toUpperCase());
        processUpdateTargets(tokens, processed);
        /* returns if no conditions exist upon the update */
        if (tokens.isEmpty()) {
            return;
        }
        /* processes WHERE clause */
        if (Constants.WHERE.equalsIgnoreCase(tokens.peek())) {
            processWhere(tokens, processed);
        }
        processDelimiter(tokens);
    }

    private static void processUpdateTargets(Queue<String> tokens,
                                             Queue<String> processed) throws SQLException {
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error");
        }
        processed.add(Constants.COLUMN);
        processed.add(tokens.poll());
        if (!Constants.EQUAL.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : '=' is expected");
        }
        processed.add(Constants.OPERATOR);
        processed.add(tokens.poll());
        processColumnValue(tokens, processed);
        if (tokens.isEmpty() || Constants.WHERE.equalsIgnoreCase(tokens.peek())) {
            return;
        }
        if (Constants.COMMA.equals(tokens.peek())) {
            /* drops COMMA */
            tokens.poll();
            processUpdateTargets(tokens, processed);
        } else {
            throw new SQLException("Syntax Error : Unexpected token found");
        }
    }

    private static void processColumnValue(Queue<String> tokens,
                                           Queue<String> processed) throws SQLException {
        if ("?".equalsIgnoreCase(tokens.peek())) {
            processed.add(Constants.PARAM_VALUE);
            processed.add(tokens.poll());
        } else if (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
            processed.add(Constants.PARAM_VALUE);
            processed.add(tokens.poll());
            while (!Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
                processed.add(tokens.poll());
            }
            if (!Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
                throw new SQLException("Syntax Error : Single quote is expected");
            }
            processed.add(tokens.poll());
        } else if (ParserUtil.isStringLiteral(tokens.peek())) {
            processed.add(Constants.PARAM_VALUE);
            StringBuilder tmp = new StringBuilder();
            while (!tokens.isEmpty() &&
                    !ParserUtil.getConditionalOperatorList().contains(tokens.peek()) &&
                    !Constants.RIGHT_BRACKET.equals(tokens.peek())) {
                tmp.append(tokens.poll());
            }
            validateParamValue(tmp.toString());
            processed.add(tmp.toString());
        }
    }

    private static void validateParamValue(String s) throws SQLException {
        try {
            if (!s.contains(".")) {
                Integer.parseInt(s);
            } else {
                Double.parseDouble(s);
            }
        } catch (Exception e) {
            throw new SQLException("Invalid parameter value ('" + s + "') specified", e);
        }
    }

    private static void parseDelete(Queue<String> tokens,
                                    Queue<String> processed) throws SQLException {
        if (!Constants.DELETE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'DELETE' expected");
        }
        processed.add(tokens.poll().toUpperCase());
        if (!Constants.FROM.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'FROM' expected");
        }
        processed.add(tokens.poll().toUpperCase());
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.TABLE);
        processed.add(tokens.poll());
        if (!tokens.isEmpty()) {
            processWhereClause(tokens, processed);
        }
        processDelimiter(tokens);
    }

    private static void processWhere(Queue<String> tokens,
                                     Queue<String> processed) throws SQLException {
        if (!Constants.WHERE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'WHERE' clause expected");
        }
        processed.add(tokens.poll().toUpperCase());
        processWhereTargets(tokens, processed);
        processDelimiter(tokens);
    }

    private static void processDelimiter(Queue<String> tokens) throws SQLException {
        if (Parser.DELIMITER.equalsIgnoreCase(tokens.peek())) {
            tokens.peek();
        } else if (ParserUtil.isKeyword(tokens.peek())) {
            throw new SQLException("Synatax Error : ';' expected");
        } else {
            //do nothing
        }
    }

    private static void processWhereTargets(Queue<String> tokens,
                                            Queue<String> processed) throws SQLException {
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal expected");
        }
        processed.add(Constants.COLUMN);
        processed.add(tokens.poll());
//        if (!Constants.EQUAL.equalsIgnoreCase(tokens.peek())) {
//            throw new SQLException("Syntax Error : '=' expected");
//        }
        processed.add(Constants.OPERATOR);
        processed.add(tokens.poll());
        processWhereColumnValues(tokens, processed, false, false, true);
    }

    private static void processWhereColumnValues(Queue<String> tokens,
                                                 Queue<String> processed,
                                                 boolean isParameterized,
                                                 boolean isEnd,
                                                 boolean isInit) throws SQLException {
        if (!isEnd) {
            if (!ParserUtil.isStringLiteral(tokens.peek())) {
                throw new SQLException("Syntax Error : String literal expected");
            }
            if ("?".equalsIgnoreCase(tokens.peek())) {
                if (isInit) {
                    isParameterized = true;
                    isInit = false;
                }
                if (!isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                processed.add(Constants.PARAM_VALUE);
                processed.add(tokens.poll());
            } else if (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
                if (isInit) {
                    isParameterized = false;
                    isInit = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                processed.add(tokens.poll());
                StringBuilder b = new StringBuilder();
                while (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek()) ||
                        tokens.isEmpty()) {
                    b.append(tokens.poll());
                }
                processed.add(b.toString());
                processed.add(Constants.SINGLE_QUOTATION);
            } else if (ParserUtil.isStringLiteral(tokens.peek())) {
                if (isInit) {
                    isParameterized = false;
                    isInit = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                isParameterized = false;
                processed.add(tokens.poll());
            }
            if (!Constants.AND.equalsIgnoreCase(tokens.peek()) ||
                    !Constants.OR.equalsIgnoreCase(tokens.peek())) {
                isEnd = true;
            }
            tokens.poll();
            processWhereColumnValues(tokens, processed, isParameterized, isEnd, isInit);
        }
    }

}
