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
import java.util.Map;

import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;

public class Condition {

    private Condition lhs;

    private Condition rhs;

    private String column;

    private String value;

    private String operator;

    /**
     * <p>Processes the condition by traversing through the binary tree of conditions that it
     * creates while parsing the WHERE clause of a conditional SQL statement.</p>
     *
     * @param dataTable Input data
     * @return Filtered out data after evaluating the input against the provided conditions
     * @throws SQLException
     */
    public Map<Integer, DataRow> process(DataTable dataTable) throws SQLException {
        if (this.getLhs() != null && this.getRhs() == null) {
        	return this.getLhs().process(dataTable);
        } else if (this.getLhs() == null) {
            return this.applyCondition(dataTable);
        }
        return ParserUtil.mergeRows(this.getOperator(), 
        		this.getLhs().process(dataTable),
                this.getRhs().process(dataTable));
    }

    /**
     * <p>Applies the condition on each of the atomic condition which resides in a particular root
     * condition. For example, the leaf nodes of the binary tree that's built by parsing the
     * WHERE clause of a conditional SQL statement would be processed by this particular method</p>
     * 
     * @param dataTable Input data
     * @return          Filtered out data after evaluating the input against the provided conditions
     * @throws SQLException
     */
    private Map<Integer, DataRow> applyCondition(DataTable dataTable) throws SQLException {
        return dataTable.applyCondition(this.getColumn(), this.getValue(), 
        		this.getOperator());
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Condition getLhs() {
        return lhs;
    }

    public void setLhs(Condition lhs) {
        this.lhs = lhs;
    }

    public Condition getRhs() {
        return rhs;
    }

    public void setRhs(Condition rhs) {
        this.rhs = rhs;
    }

}
