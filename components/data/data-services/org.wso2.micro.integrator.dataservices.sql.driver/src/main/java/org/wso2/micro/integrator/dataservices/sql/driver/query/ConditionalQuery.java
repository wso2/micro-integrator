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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.Condition;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

public abstract class ConditionalQuery extends Query {

    private Condition condition;

    public ConditionalQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.condition = new Condition();
    }

    public Condition getCondition() {
        return condition;
    }

    public void processConditions(Queue<String> tokens, Condition rootCondition) {
        if (Constants.COLUMN.equals(tokens.peek())) {
            rootCondition.setLhs(new Condition());
            tokens.poll();
            rootCondition.getLhs().setColumn(tokens.poll());
            tokens.poll();
            rootCondition.getLhs().setOperator(tokens.poll());
            tokens.poll();
            rootCondition.getLhs().setValue(getColumnValue(tokens));
            if (tokens.isEmpty()) {
                return;
            }
            if (Constants.RIGHT_BRACKET.equals(tokens.peek())) {
                /* breaking the sequence when a right bracket is located */
                tokens.poll();
                return;
            }
            /* operator, e.g. OR / AND */
            rootCondition.setOperator(tokens.poll());
            rootCondition.setRhs(new Condition());
            this.processConditions(tokens, rootCondition.getRhs());
        } else if (Constants.LEFT_BRACKET.equals(tokens.peek())) {
            tokens.poll();
            rootCondition.setLhs(new Condition());
            this.processConditions(tokens, rootCondition.getLhs());
            if (tokens.isEmpty()) {
                return;
            }
            rootCondition.setOperator(tokens.poll());
            rootCondition.setRhs(new Condition());
            this.processConditions(tokens, rootCondition.getRhs());
        }
    }

    /**
     * Column values can be specified with/without single quotes. The following snippet
     * handles those two instances where the column values are specified within/without
     * single quotes.
     *
     * @param tokens    Input tokens
     * @return          Extracted column value
     */
    private String getColumnValue(Queue<String> tokens) {
        StringBuilder value = new StringBuilder();
        if (Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
            tokens.poll();
            while (!Constants.SINGLE_QUOTATION.equalsIgnoreCase(tokens.peek())) {
                value.append(tokens.poll());
            }
            tokens.poll();
        } else {
            value.append(tokens.poll());
        }
        return value.toString();
    }

}
