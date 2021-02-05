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

public class ColumnInfo {

    private String name;

    private String tableName;

    private int sqlType;

    private int id;

    private int ordinal;

    private Object value;

    // This is used to change column names later when "AS" keyword is used.
    private String aliasName;

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public ColumnInfo(String name, String tableName, int sqlType) {
        this.name = name;
        this.tableName = tableName;
        this.sqlType = sqlType;
    }

    public ColumnInfo(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }
    
    public ColumnInfo(int id, String name, String tableName, int sqlType, int ordinal) {
    	this.id = id;
        this.name = name;
        this.tableName = tableName;
        this.sqlType = sqlType;
        this.ordinal = ordinal;
    }

    public ColumnInfo(String name) {
        this.name = name;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public int getSqlType() {
        return sqlType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
    
}
