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
package org.wso2.micro.integrator.dataservices.sql.driver.query.drop;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.query.Query;

public abstract class DropQuery extends Query {

    private String tableName;

    public DropQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.tableName = this.extractTableName(this.getProcessedTokens());
    }

    private String extractTableName(Queue<String> tokens) {
        //Dropping DROP keyword
        tokens.poll();
        //Dropping SHEET keyword
        tokens.poll();
        //Dropping TABLE identifier
        tokens.poll();
        return tokens.poll();
    }

    public String getTableName() {
        return tableName;
    }

}
