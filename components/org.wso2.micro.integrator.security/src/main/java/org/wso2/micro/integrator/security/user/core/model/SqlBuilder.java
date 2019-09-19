/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.security.user.core.model;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlBuilder {

    private static final String START_PARENTHESES = "(";
    private static final String CLOSE_PARENTHESES = ")";

    private List<String> wheres = new ArrayList<>();
    private StringBuilder sql;
    private StringBuilder tail;
    private int count = 1;
    private Map<Integer, Integer> integerParameters = new HashMap<>();
    private Map<Integer, String> stringParameters = new HashMap<>();
    private Map<Integer, Long> longParameters = new HashMap<>();
    private boolean addedWhereStatement = false;

    public SqlBuilder(StringBuilder sql) {

        this.sql = sql;
    }

    private void appendList(StringBuilder sql, List<String> list) {

        for (String s : list) {
            if (addedWhereStatement) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
                this.addedWhereStatement = true;
            }
            sql.append(s);
        }
    }

    public String getQuery() {

        appendList(sql, wheres);
        wheres = new ArrayList<>();
        this.addedWhereStatement = false;
        if (tail != null) {
            return buildExecutableSQLStatement(new StringBuilder(sql.toString() + tail));
        } else {
            return buildExecutableSQLStatement(new StringBuilder(sql.toString()));
        }
    }

    public SqlBuilder where(String expr, String value) {

        wheres.add(expr);
        stringParameters.put(count, value);
        count++;
        return this;
    }

    public SqlBuilder where(String expr, int value) {

        wheres.add(expr);
        integerParameters.put(count, value);
        count++;
        return this;
    }

    public SqlBuilder where(String expr, long value) {

        wheres.add(expr);
        longParameters.put(count, value);
        count++;
        return this;
    }

    public Map<Integer, Integer> getIntegerParameters() {

        return integerParameters;
    }

    public Map<Integer, String> getStringParameters() {

        return stringParameters;
    }

    public Map<Integer, Long> getLongParameters() {

        return longParameters;
    }

    public void setTail(String tail, Integer... placeHolders) {

        if (this.tail == null) {
            this.tail = new StringBuilder(tail);
        } else {
            this.tail.append(tail);
        }

        for (int value : placeHolders) {
            integerParameters.put(count, value);
            count++;
        }
    }

    public void setTail(String tail, String... placeHolders) {

        if (this.tail == null) {
            this.tail = new StringBuilder(tail);
        } else {
            this.tail.append(tail);
        }

        for (String value : placeHolders) {
            stringParameters.put(count, value);
            count++;
        }
    }

    public void setTail(String tail, Long... placeHolders) {

        if (this.tail == null) {
            this.tail = new StringBuilder(tail);
        } else {
            this.tail.append(tail);
        }

        for (long value : placeHolders) {
            longParameters.put(count, value);
            count++;
        }
    }

    public String getSql() {

        return sql.toString();
    }

    public List<String> getWheres() {

        return wheres;
    }

    public void updateSql(String append) {

        appendList(sql, wheres);
        wheres = new ArrayList<>();
        this.addedWhereStatement = false;
        this.sql.append(append);
    }

    public void updateSqlWithOROperation(String expr, Object value) {

        appendList(sql, wheres);
        wheres = new ArrayList<>();
        this.sql.append(" OR ").append(expr);
        if (value instanceof String) {
            stringParameters.put(count, String.valueOf(value));
        } else if (value instanceof Integer) {
            integerParameters.put(count, (Integer) value);
        } else if (value instanceof Long) {
            longParameters.put(count, (Long) value);
        }
        count++;
    }

    /**
     * This method to build an executable SQL statement.
     *
     * @param sqlQueryStringBuilder Final sql query string.
     */
    private String buildExecutableSQLStatement(StringBuilder sqlQueryStringBuilder) {

        // Check whether any parentheses which are not closed in the SQL statement if so close it.
        int startParenthesesCounts = StringUtils.countMatches(sqlQueryStringBuilder.toString(), START_PARENTHESES);
        int endParenthesesCounts = StringUtils.countMatches(sqlQueryStringBuilder.toString(), CLOSE_PARENTHESES);
        int needToBeCloseParenthesesCount = startParenthesesCounts - endParenthesesCounts;

        if (needToBeCloseParenthesesCount > 0) {
            for (int i = 0; i < needToBeCloseParenthesesCount; i++) {
                sqlQueryStringBuilder.append(CLOSE_PARENTHESES);
            }
        }
        return sqlQueryStringBuilder.toString();
    }

}
