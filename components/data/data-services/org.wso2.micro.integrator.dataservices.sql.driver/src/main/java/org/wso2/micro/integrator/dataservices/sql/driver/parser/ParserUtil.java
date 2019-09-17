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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ParamInfo;

public class ParserUtil {

    private static List<String> keyWords = new EntityList<String>();
    private static List<String> operators = new EntityList<String>();
    private static List<String> delimiters = new EntityList<String>();
    private static List<String> stringFunctions = new EntityList<String>();
    private static List<String> aggregateFunctions = new EntityList<String>();
    private static List<String> dmlTypes = new EntityList<String>();

    private static List<String> conditionalOperators = new EntityList<String>();

    static {
        keyWords.add(Constants.COUNT);
        keyWords.add(Constants.SELECT);
        keyWords.add(Constants.FROM);
        keyWords.add(Constants.WHERE);
        keyWords.add(Constants.MAX);
        keyWords.add(Constants.INSERT);
        keyWords.add(Constants.INTO);
        keyWords.add(Constants.VALUES);
        keyWords.add(Constants.GROUP_BY);
        keyWords.add(Constants.ORDER_BY);
        keyWords.add(Constants.DISTINCT);
        keyWords.add(Constants.UPDATE);
        keyWords.add(Constants.SET);
        keyWords.add(Constants.IN);
        keyWords.add(Constants.AND);
        keyWords.add(Constants.DELAYED);
        keyWords.add(Constants.LOW_PRIORITY);
        keyWords.add(Constants.HIGH_PRIORITY);
        keyWords.add(Constants.ON);
        keyWords.add(Constants.DUPLICATE);
        keyWords.add(Constants.KEY);
        keyWords.add(Constants.LAST_INSERT_ID);
        keyWords.add(Constants.ALL);
        keyWords.add(Constants.DISTINCTROW);
        keyWords.add(Constants.STRAIGHT_JOIN);
        keyWords.add(Constants.SQL_SMALL_RESULT);
        keyWords.add(Constants.SQL_BIG_RESULT);
        keyWords.add(Constants.SQL_BUFFER_RESULT);
        keyWords.add(Constants.SQL_CACHE);
        keyWords.add(Constants.SQL_NO_CACHE);
        keyWords.add(Constants.SQL_CALC_FOUND_ROWS);
        keyWords.add(Constants.ASC);
        keyWords.add(Constants.DESC);
        keyWords.add(Constants.OFFSET);
        keyWords.add(Constants.LIMIT);
        keyWords.add(Constants.WITH);
        keyWords.add(Constants.ROLLUP);
        keyWords.add(Constants.PROCEDURE);
        keyWords.add(Constants.OUTFILE);
        keyWords.add(Constants.DUMPFILE);
        keyWords.add(Constants.LOCK);
        keyWords.add(Constants.SHARE);
        keyWords.add(Constants.MODE);
        keyWords.add(Constants.CONCAT);
        keyWords.add(Constants.AS);
        keyWords.add(Constants.AVG);
        keyWords.add(Constants.MIN);
        keyWords.add(Constants.IS);
        keyWords.add(Constants.NULL);
        keyWords.add(Constants.LIKE);
        keyWords.add(Constants.OR);
        keyWords.add(Constants.JOIN);
        keyWords.add(Constants.INNER);
        keyWords.add(Constants.SUM);
        keyWords.add(Constants.VALUE);
        keyWords.add(Constants.DELETE);
        keyWords.add(Constants.CREATE);
        keyWords.add(Constants.SHEET);
        keyWords.add(Constants.DROP);

        operators.add(Constants.EQUAL);
        operators.add(Constants.MINUS);
        operators.add(Constants.PLUS);
        operators.add(Constants.FORWARD_SLASH);
        operators.add(Constants.ASTERISK);
        operators.add(Constants.GREATER_THAN);
        operators.add(Constants.DIVISION);

        delimiters.add(Constants.COMMA);
        delimiters.add(Constants.LESS_THAN);
        delimiters.add(Constants.SINGLE_QUOTATION);
        delimiters.add(Constants.SEMI_COLON);
        delimiters.add(Constants.COLON);
        delimiters.add(Constants.DOT);
        delimiters.add(Constants.LEFT_BRACE);
        delimiters.add(Constants.LEFT_BRACKET);
        delimiters.add(Constants.RIGHT_BRACE);
        delimiters.add(Constants.RIGHT_BRACKET);
        delimiters.add(Constants.HYPHEN);
        delimiters.add(Constants.NEW_LINE);
        delimiters.add(Constants.RETURN);
        //delimiters.add(Constants.WHITE_SPACE);

        aggregateFunctions.add(Constants.AVG);
        aggregateFunctions.add(Constants.MAX);
        aggregateFunctions.add(Constants.MIN);
        aggregateFunctions.add(Constants.COUNT);
        aggregateFunctions.add(Constants.SUM);

        stringFunctions.add(Constants.TRIM);
        stringFunctions.add(Constants.RTRIM);
        stringFunctions.add(Constants.LTRIM);
        stringFunctions.add(Constants.SUBSTR);
        stringFunctions.add(Constants.CONCAT);

        dmlTypes.add(Constants.INSERT);
        dmlTypes.add(Constants.UPDATE);
        dmlTypes.add(Constants.DELETE);

        conditionalOperators.add(Constants.AND);
        conditionalOperators.add(Constants.OR);

    }

    public static List<String> getConditionalOperatorList() {
        return conditionalOperators;
    }

    public static List<String> getKeyWordList() {
        return keyWords;
    }

    public static List<String> getDMLTypeList() {
        return dmlTypes;
    }

    public static List<String> getDelimiterList() {
        return delimiters;
    }

    public static List<String> getOperatorList() {
        return operators;
    }

    public static List<String> getAggregateFunctionList() {
        return aggregateFunctions;
    }

    public static List<String> getStringFunctionList() {
        return stringFunctions;
    }

    public static boolean isDelimiter(String token) {
        return ParserUtil.getDelimiterList().contains(token);
    }

    public static boolean isOperator(String token) {
        return ParserUtil.getOperatorList().contains(token);
    }

    public static boolean isAggregateFunction(String token) {
        return ParserUtil.getAggregateFunctionList().contains(token);
    }

    public static boolean isStringFunction(String token) {
        return ParserUtil.getStringFunctionList().contains(token);
    }

    public static boolean isKeyword(String token) {
        return ParserUtil.getKeyWordList().contains(token);
    }

    public static boolean isStringLiteral(String token) {
        return (!ParserUtil.isDelimiter(token) && !ParserUtil.isOperator(token) &&
                !ParserUtil.isKeyword(token));
    }

    public static synchronized Queue<String> getTokens(String sql) throws SQLException {
        boolean isQuoted = false;
        char[] inputCharacters;
        StringBuilder token = new StringBuilder();
        Queue<String> tokenQueue = new LinkedList<String>();

        inputCharacters = new char[sql.length()];
        sql.getChars(0, sql.length(), inputCharacters, 0);

        for (char c : inputCharacters) {
            String tmp = Character.valueOf(c).toString();
            if (Constants.SINGLE_QUOTATION.equals(tmp)) {
                isQuoted = !isQuoted;
                if (token.length() > 0) {
                    tokenQueue.add(token.toString());
                }
                tokenQueue.add(new StringBuilder().append(c).toString());
                token = new StringBuilder();
                continue;
            }
            if (isQuoted) {
                token.append(c);
            } else {
                if (!ParserUtil.isControlCharacter(tmp)) {
                    if (!Constants.WHITE_SPACE.equals(tmp)) {
                        token.append(c);
                    } else if (Constants.WHITE_SPACE.equals(tmp)) {
                        if (token.length() > 0) {
                            tokenQueue.add(token.toString());
                        }
                        token = new StringBuilder();
                    }
                } else {
                    if (token.length() > 0) {
                        tokenQueue.add(token.toString());
                    }
                    tokenQueue.add(new StringBuilder().append(c).toString());
                    token = new StringBuilder();
                }
            }
        }
        if (token.length() > 0) {
            tokenQueue.add(token.toString());
        }
        return tokenQueue;
    }

    public static boolean isControlCharacter(String token) {
        return ParserUtil.isDelimiter(token) || ParserUtil.isOperator(token);
    }

    public static boolean isDMLStatement(String type) {
        return ParserUtil.getDMLTypeList().contains(type.toUpperCase());
    }

    public static ParamInfo[] extractParameters(String sql) {
        List<ParamInfo> tmp = new ArrayList<ParamInfo>();
        int i = 0;
        int idx = 0;
        char[] s = sql.toCharArray();
        while (i < s.length) {
            final char c = s[i];
            if (c == '?') {
                ParamInfo param = new ParamInfo(idx, null);
                tmp.add(param);
                idx++;
            }
            i++;
        }
        return tmp.toArray(new ParamInfo[tmp.size()]);
    }

    public static String extractFirstKeyword(String sql) {
        int i = 0;
        char[] s = sql.toCharArray();
        StringBuffer b = new StringBuffer();
        while (i < s.length) {
            char c = s[i];
            if (c != ' ') {
                b.append(c);
            } else {
                break;
            }
            i++;
        }
        String token = b.toString();
        return (ParserUtil.isKeyword(token)) ? token : null;
    }

    public static ParamInfo getParameter(int orinal, ParamInfo[] parameters) {
        ParamInfo result = null;
        for (ParamInfo paramInfo : parameters) {
            if (paramInfo.getOrdinal() == orinal) {
                result = paramInfo;
            }
        }
        return result;
    }

    public static ParamInfo getParameter(String column, ParamInfo[] parameters) {
        ParamInfo result = null;
        for (ParamInfo paramInfo : parameters) {
            if (paramInfo.getName().equals(column)) {
                result = paramInfo;
            }
        }
        return result;
    }

    public static Map<Integer, DataRow> mergeRows(String operation, Map<Integer, DataRow> rows1,
                                                  Map<Integer, DataRow> rows2) {
        Map<Integer, DataRow> result = new HashMap<Integer, DataRow>();
        if (operation == null) {
            if (rows1 != null) {
                result = rows1;
            } else if (rows2 != null) {
                result = rows2;
            }
        } else if (Constants.OR.equals(operation)) {
            result = processORCondition(rows1, rows2);
        } else if (Constants.AND.equals(operation)) {
            result = processANDCondition(rows1, rows2);
        }
        return result;
    }

    public static Map<Integer, DataRow> processORCondition(Map<Integer, DataRow> rows1,
                                                           Map<Integer, DataRow> rows2) {
        Map<Integer, DataRow> result = new HashMap<Integer, DataRow>();
        for (Map.Entry<Integer, DataRow> row : rows1.entrySet()) {
            result.put(row.getKey(), row.getValue());
        }
        for (Map.Entry<Integer, DataRow> row : rows2.entrySet()) {
            result.put(row.getKey(), row.getValue());
        }
        return result;
    }

    public static Map<Integer, DataRow> processANDCondition(Map<Integer, DataRow> rows1,
                                                            Map<Integer, DataRow> rows2) {
        Map<Integer, DataRow> result = new HashMap<Integer, DataRow>();
        for (Map.Entry<Integer, DataRow> row : rows1.entrySet()) {
            if (rows2.containsKey(row.getKey())) {
                result.put(row.getKey(), row.getValue());
            }
        }
        return result;
    }


}
