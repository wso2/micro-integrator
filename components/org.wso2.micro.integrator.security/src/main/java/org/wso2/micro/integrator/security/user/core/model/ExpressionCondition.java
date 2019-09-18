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
 * This class is used to define a Expression condition like userName = user1.
 */
public class ExpressionCondition implements Condition {

    private String operation;
    private String attributeName;
    private String attributeValue;

    @Override
    public String getOperation() {

        return operation;
    }

    @Override
    public void setOperation(String operation) {

        this.operation = operation;
    }

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    public String getAttributeValue() {

        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {

        this.attributeValue = attributeValue;
    }

    public ExpressionCondition(String operation, String attributeName, String attributeValue) {

        this.operation = operation;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }
}
