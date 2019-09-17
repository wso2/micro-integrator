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
package org.wso2.micro.integrator.dataservices.sql.driver.query.create;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.micro.integrator.dataservices.sql.driver.query.Query;

public abstract class CreateQuery extends Query {

    private String tableName;

    private List<ColumnInfo> columns;

    public CreateQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.tableName = this.extractTableName(this.getProcessedTokens());
        this.columns = this.extractColumns(this.getProcessedTokens(), new ArrayList<ColumnInfo>());
    }

    private String extractTableName(Queue<String> tokens) {
        //Dropping CREATE keyword
        tokens.poll();
        //Dropping SHEET keyword;
        tokens.poll();
        //Dropping TABLE identifier
        tokens.poll();
        //Returning the table name
        return tokens.poll();
    }

    private List<ColumnInfo> extractColumns(Queue<String> tokens,
                                            List<ColumnInfo> columns) throws SQLException {
        if (tokens.isEmpty()) {
            /* Returns when a user creates a sheet without defining any columns */
            return columns;
        }
        if (!Constants.COLUMN.equals(tokens.peek())) {
             throw new SQLException("Unable to extract columns");
        }
        tokens.poll();
        columns.add(new ColumnInfo(tokens.poll(), this.getTableName(), -1));
        if (Constants.COLUMN.equals(tokens.peek())) {
            columns = extractColumns(tokens, columns);
        }
        return columns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

}
