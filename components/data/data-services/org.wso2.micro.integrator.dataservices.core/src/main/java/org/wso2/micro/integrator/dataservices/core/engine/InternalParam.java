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
package org.wso2.micro.integrator.dataservices.core.engine;

/**
 * This class represents a param that is used inside a query.
 */
public class InternalParam {

    private String name;

    /** i.e. scalar/array values */
    private ParamValue value;

    /** i.e. STRING, INTEGER */
    private String sqlType;

    /** i.e. IN, INOUT */
    private String type;

    private int ordinal;

    private String structType;

    public InternalParam(String name, ParamValue value, String sqlType, String type,
                         String structType, int ordinal) {
        this.name = name;
        this.value = value;
        this.sqlType = sqlType;
        this.type = type;
        this.structType = structType;
        this.ordinal = ordinal;
    }

    public InternalParam(InternalParam param, int ordinal) {
        this.name = param.name;
        this.value = param.value;
        this.sqlType = param.sqlType;
        this.type = param.type;
        this.structType = param.structType;
        this.ordinal = ordinal;
    }

    public String getName() {
        return name;
    }

    public String getSqlType() {
        return sqlType;
    }

    public String getType() {
        return type;
    }

    public ParamValue getValue() {
        return value;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getStructType() {
        return structType;
    }

    public String toString() {
        return "{" + this.getName() + ":" + this.getValue() + "}";
    }

}
