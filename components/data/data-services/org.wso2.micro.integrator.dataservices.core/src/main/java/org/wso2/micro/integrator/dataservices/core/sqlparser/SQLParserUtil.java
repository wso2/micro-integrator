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
package org.wso2.micro.integrator.dataservices.core.sqlparser;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.sqlparser.analysers.AnalyzerFactory;import org.wso2.micro.integrator.dataservices.core.sqlparser.analysers.KeyWordAnalyzer;import org.wso2.micro.integrator.dataservices.core.sqlparser.mappers.SelectMapper;import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

import java.util.*;

public class SQLParserUtil {

    public static List<String> keyWords = new ArrayList<String>();
    public static List<String> operators = new ArrayList<String>();
    public static List<String> delimiters = new ArrayList<String>();
    public static List<String> specialFunctions = new ArrayList<String>();
    public static List<String> stringFunctions = new ArrayList<String>();
    public static List<String> aggregateFunctions = new ArrayList<String>();
    public static List<String> controlCharacters = new ArrayList<String>();

    static {
        keyWords.add(LexicalConstants.COUNT);
        keyWords.add(LexicalConstants.SELECT);
        keyWords.add(LexicalConstants.FROM);
        keyWords.add(LexicalConstants.WHERE);
        keyWords.add(LexicalConstants.MAX);
        keyWords.add(LexicalConstants.INSERT);
        keyWords.add(LexicalConstants.INTO);
        keyWords.add(LexicalConstants.VALUES);
        keyWords.add(LexicalConstants.GROUP_BY);
        keyWords.add(LexicalConstants.ORDER_BY);
        keyWords.add(LexicalConstants.DISTINCT);
        keyWords.add(LexicalConstants.UPDATE);
        keyWords.add(LexicalConstants.SET);
        keyWords.add(LexicalConstants.IN);
        keyWords.add(LexicalConstants.AND);
        keyWords.add(LexicalConstants.DELAYED);
        keyWords.add(LexicalConstants.LOW_PRIORITY);
        keyWords.add(LexicalConstants.HIGH_PRIORITY);
        keyWords.add(LexicalConstants.ON);
        keyWords.add(LexicalConstants.DUPLICATE);
        keyWords.add(LexicalConstants.KEY);
        keyWords.add(LexicalConstants.LAST_INSERT_ID);
        keyWords.add(LexicalConstants.ALL);
        keyWords.add(LexicalConstants.DISTINCTROW);
        keyWords.add(LexicalConstants.STRAIGHT_JOIN);
        keyWords.add(LexicalConstants.SQL_SMALL_RESULT);
        keyWords.add(LexicalConstants.SQL_BIG_RESULT);
        keyWords.add(LexicalConstants.SQL_BUFFER_RESULT);
        keyWords.add(LexicalConstants.SQL_CACHE);
        keyWords.add(LexicalConstants.SQL_NO_CACHE);
        keyWords.add(LexicalConstants.SQL_CALC_FOUND_ROWS);
        keyWords.add(LexicalConstants.ASC);
        keyWords.add(LexicalConstants.DESC);
        keyWords.add(LexicalConstants.OFFSET);
        keyWords.add(LexicalConstants.LIMIT);
        keyWords.add(LexicalConstants.WITH);
        keyWords.add(LexicalConstants.ROLLUP);
        keyWords.add(LexicalConstants.PROCEDURE);
        keyWords.add(LexicalConstants.OUTFILE);
        keyWords.add(LexicalConstants.DUMPFILE);
        keyWords.add(LexicalConstants.LOCK);
        keyWords.add(LexicalConstants.SHARE);
        keyWords.add(LexicalConstants.MODE);
        keyWords.add(LexicalConstants.CONCAT);
        keyWords.add(LexicalConstants.AS);
        keyWords.add(LexicalConstants.AVG);
        keyWords.add(LexicalConstants.MIN);
        keyWords.add(LexicalConstants.IS);
        keyWords.add(LexicalConstants.NULL);
        keyWords.add(LexicalConstants.LIKE);
        keyWords.add(LexicalConstants.OR);
        keyWords.add(LexicalConstants.JOIN);
        keyWords.add(LexicalConstants.INNER);
        keyWords.add(LexicalConstants.SUM);

        operators.add(LexicalConstants.EQUAL);
        operators.add(LexicalConstants.MINUS);
        operators.add(LexicalConstants.PLUS);
        operators.add(LexicalConstants.FORWARD_SLASH);
        operators.add(LexicalConstants.ASTERISK);
        operators.add(LexicalConstants.GREATER_THAN);
        operators.add(LexicalConstants.DIVISION);

        delimiters.add(LexicalConstants.EQUAL);
        delimiters.add(LexicalConstants.MINUS);
        delimiters.add(LexicalConstants.PLUS);
        delimiters.add(LexicalConstants.COMMA);
        delimiters.add(LexicalConstants.SINGLE_QUOTATION);
        delimiters.add(LexicalConstants.SEMI_COLON);
        delimiters.add(LexicalConstants.COLON);
        delimiters.add(LexicalConstants.DOT);
        delimiters.add(LexicalConstants.LEFT_BRACE);
        delimiters.add(LexicalConstants.LEFT_BRACKET);
        delimiters.add(LexicalConstants.RIGHT_BRACE);
        delimiters.add(LexicalConstants.RIGHT_BRACKET);
        delimiters.add(LexicalConstants.HYPHEN);
        delimiters.add(LexicalConstants.WHITE_SPACE);
        delimiters.add(LexicalConstants.RETURN);
        delimiters.add(LexicalConstants.NEW_LINE);

        aggregateFunctions.add(LexicalConstants.AVG);
        aggregateFunctions.add(LexicalConstants.MAX);
        aggregateFunctions.add(LexicalConstants.MIN);
        aggregateFunctions.add(LexicalConstants.COUNT);
        aggregateFunctions.add(LexicalConstants.SUM);

        stringFunctions.add(LexicalConstants.TRIM);
        stringFunctions.add(LexicalConstants.RTRIM);
        stringFunctions.add(LexicalConstants.LTRIM);
        stringFunctions.add(LexicalConstants.SUBSTR);
        stringFunctions.add(LexicalConstants.CONCAT);

        specialFunctions.add(LexicalConstants.OR);
        specialFunctions.add(LexicalConstants.AND);
        specialFunctions.add(LexicalConstants.IS);
        specialFunctions.add(LexicalConstants.LIKE);
        specialFunctions.add(LexicalConstants.NOT);
        specialFunctions.add(LexicalConstants.NULL);
        specialFunctions.add(LexicalConstants.IN);

        controlCharacters.add(LexicalConstants.NEW_LINE);
        controlCharacters.add(LexicalConstants.RETURN);
        controlCharacters.add(LexicalConstants.WHITE_SPACE);
    }

    public static List<String> getKeyWords() {
        return keyWords;
    }

    public static List<String> getOperators() {
        return operators;
    }

    public static List<String> getDelimiters() {
        return delimiters;
    }
    
    public static boolean isAggregateFunction(String token) {
        return aggregateFunctions.contains(token);
    }

    public static boolean isStringFunction(String token) {
        return stringFunctions.contains(token);
    }

    /**
     * This particular method transform a particular SQL string to a set of tokens and returns
     * a queue which contains the tokens(String objects) produced by the logic of the method.
     *
     * @param sql Input SQL string
     * @return A Queue of String objects.
     */
    public static Queue<String> getTokens(String sql) {
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
                if (!SQLParserUtil.isDelimiter(tmp)) {
                    if (!Constants.WHITE_SPACE.equals(tmp)) {
                        token.append(c);
                    } else if (Constants.WHITE_SPACE.equals(tmp)) {
                        if (token.length() > 0) {
                            tokenQueue.add(token.toString());
                            token.setLength(0);
                        }
                    }
                } else {
                    if (token.length() > 0) {
                        tokenQueue.add(token.toString());
                        token.setLength(0);
                    }
                    if (SQLParserUtil.isControlCharacter(tmp)) {
                        continue;
                    }
                    tokenQueue.add(new StringBuilder().append(c).toString());
                    token.setLength(0);
                }
            }
        }
        if (token.length() > 0) {
            tokenQueue.add(token.toString());
            token.setLength(0);
        }
        return tokenQueue;
    }

    public static boolean isDelimiter(String token) {
        return SQLParserUtil.getDelimiters().contains(token);
    }

    public static boolean isOperator(String token) {
        return SQLParserUtil.getOperators().contains(token);
    }

    public static List<String> getControlCharacters() {
        return controlCharacters;
    }

    public static boolean isControlCharacter(String token) {
        return SQLParserUtil.getControlCharacters().contains(token);
    }

    /**
     * This method returns the list of queried columns of a particular input sql string.
     *
     * @param sql Input SQL string
     * @return List if columns expected as the output
     * @throws DataServiceFault If any error occurs while parsing the SQL query
     */
    public static List<String> extractOutputColumns(String sql) throws DataServiceFault {
        KeyWordAnalyzer analyser;
        Queue<String> syntaxQueue = null;
        Queue<String> tmpQueue = new LinkedList<String>();
        Queue<String> tokens = SQLParserUtil.getTokens(sql);

        if (!tokens.isEmpty() && tokens.peek().toUpperCase().equals(LexicalConstants.SELECT)) {
            tokens.poll();
            while (!tokens.isEmpty() &&
                    !tokens.peek().toUpperCase().equals(LexicalConstants.FROM)) {
                tmpQueue.add(tokens.poll());
            }
            analyser = AnalyzerFactory.createAnalyzer(LexicalConstants.SELECT, tmpQueue);
            analyser.analyseStatement();
            syntaxQueue = analyser.getSyntaxQueue();
            tmpQueue.clear();
        }
        tokens.clear();
        return (new SelectMapper(syntaxQueue).getColumns());
    }

    /**
     * Extracts out the Input mappings names specified in the query
     *
     * @param sql Input SQL string
     * @return List of input mappings specified in the query
     */
    public static List<String> extractInputMappingNames(String sql) {
        int paramCount = 0;
        boolean isColon = false;
        List<String> inputMappings = new ArrayList<String>();
        Queue<String> tokens = SQLParserUtil.getTokens(sql);
        for (String token : tokens) {
            if (token != null && token.startsWith(LexicalConstants.COLON)) {
                isColon = true;
                continue;
            }
            if (LexicalConstants.QUESTION_MARK.equals(token)) {
                StringBuilder paramName = new StringBuilder();
                paramName.append("param").append(paramCount);
                inputMappings.add(paramCount, paramName.toString());
                paramCount++;
            }
            if (isColon && !LexicalConstants.COMMA.equals(token)) {
                if (!inputMappings.contains(token)) {
                    inputMappings.add(paramCount, token);
                    isColon = false;
                    paramCount++;
                } else {
                    isColon = false;
                }
            }
        }
        return inputMappings;
    }

}
