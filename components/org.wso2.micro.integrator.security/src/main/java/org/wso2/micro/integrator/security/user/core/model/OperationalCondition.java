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

/**
 * This class is used to define a Operation condition like condition1 AND condition2. condition1 can be another
 * OperationalCondition or ExpressionCondition.
 */
public class OperationalCondition implements Condition {

    private String operation;
    private Condition leftCondition;
    private Condition rightCondition;

    @Override
    public String getOperation() {

        return operation;
    }

    @Override
    public void setOperation(String operation) {

        this.operation = operation;
    }

    public Condition getLeftCondition() {

        return leftCondition;
    }

    public void setLeftCondition(Condition leftCondition) {

        this.leftCondition = leftCondition;
    }

    public Condition getRightCondition() {

        return rightCondition;
    }

    public void setRightCondition(Condition rightCondition) {

        this.rightCondition = rightCondition;
    }

    public OperationalCondition(String operation, Condition leftCondition, Condition rightCondition) {

        this.operation = operation;
        this.leftCondition = leftCondition;
        this.rightCondition = rightCondition;
    }
}
