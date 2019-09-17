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
package org.wso2.micro.integrator.dataservices.sql.driver.query.delete;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.ParserUtil;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataReaderFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ConditionalQuery;

public abstract class DeleteQuery extends ConditionalQuery {

    private String targetTableName;

    private DataTable targetTable;

    public DeleteQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.targetTableName = this.extractTargetTableName(getProcessedTokens());
        this.targetTable =
                DataReaderFactory.createDataReader(getConnection()).getDataTable(
                        getTargetTableName());
        this.populateConditions(getProcessedTokens());
    }

    private String extractTargetTableName(Queue<String> tokens) throws SQLException {
        /* Dropping DELETE keyword */
        tokens.poll();
        /* Dropping FROM keyword */
        tokens.poll();
        /* Dropping TABLE identifier */
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        return tokens.poll();
    }

    private void populateConditions(Queue<String> tokens) throws SQLException {
        if (tokens.isEmpty()) {
            return;
        }
        /* Dropping WHERE keyword */
        tokens.poll();
        this.processConditions(tokens, getCondition());
    }

    public DataTable getTargetTable() {
        return targetTable;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public Map<Integer, DataRow> getResultantRows() throws SQLException {
        Map<Integer, DataRow> result;
        if (getCondition().getLhs() == null && getCondition().getRhs() == null) {
            result = getTargetTable().getRows();
        } else {
            result = getCondition().process(getTargetTable());
        }
        return result;
    }

}
