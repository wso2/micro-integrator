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
package org.wso2.micro.integrator.dataservices.sql.driver.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.wso2.micro.integrator.dataservices.sql.driver.TPreparedStatement;


public abstract class Query {

    private Statement stmt;

    private Connection connection;

    private Queue<String> processedTokens;

    private ParamInfo[] parameters;
    
    public Query(Statement stmt) throws SQLException {
        this.stmt = stmt;
        this.connection = stmt.getConnection();
        this.processedTokens = ((TPreparedStatement) getStatement()).getProcessedTokens();
        this.parameters = ((TPreparedStatement) getStatement()).getParameters();
        List<String> tokens = new ArrayList<String>(processedTokens);
        this.processedTokens =
                this.mergeParameterValues(tokens.toArray(new String[tokens.size()]), parameters);
    }

    public abstract ResultSet executeQuery() throws SQLException;

    public abstract int executeUpdate() throws SQLException;

    public abstract boolean execute() throws SQLException;

    private Queue<String> mergeParameterValues(String[] tokens, ParamInfo[] parameters) {
        int paramIndex = 0;
        for (int i = 0; i < tokens.length; i++) {
            if ("?".equals(tokens[i])) {
                ParamInfo parameter = parameters[paramIndex];
                switch (parameter.getSqlType()) {
                    case Types.INTEGER:
                    case Types.NUMERIC:
                        tokens[i] = parameter.getValue().toString();
                        break;
                    case Types.VARCHAR:
                        //tokens[i] = "'" + parameter.getValue().toString() + "'";
                        tokens[i] = parameter.getValue().toString();
                        break;
                }
                paramIndex++;
            }
        }
        return new ConcurrentLinkedQueue<String>(Arrays.asList(tokens));
    }

    public ParamInfo findParam(int index) {
        ParamInfo param = null;
        for (ParamInfo paramInfo : getParameters()) {
            if (paramInfo.getOrdinal() == index) {
                param = paramInfo;
                break;
            }
        }
        return param;
    }

    public Statement getStatement() {
        return stmt;
    }

    public Connection getConnection() {
        return connection;
    }

    public Queue<String> getProcessedTokens() {
        return processedTokens;
    }

    public ParamInfo[] getParameters() {
        return parameters;
    }

}
