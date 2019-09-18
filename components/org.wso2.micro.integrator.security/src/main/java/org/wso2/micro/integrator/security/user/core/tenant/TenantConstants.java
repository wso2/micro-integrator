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
package org.wso2.micro.integrator.security.user.core.tenant;

public class TenantConstants {
    public static final String ADD_TENANT_WITH_ID_SQL = "INSERT INTO UM_TENANT (UM_ID,UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG) VALUES(?,?,?,?,?)";
    public static final String ADD_TENANT_SQL = "INSERT INTO UM_TENANT (UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG) VALUES(?,?,?,?)";
    public static final String UPDATE_TENANT_CONFIG_SQL = "UPDATE UM_TENANT SET UM_USER_CONFIG=? WHERE UM_ID=?";
    public static final String UPDATE_TENANT_SQL = "UPDATE UM_TENANT SET UM_DOMAIN_NAME=?, UM_EMAIL=?," +
            " UM_CREATED_DATE=? WHERE UM_ID=?";
    public static final String GET_TENANT_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_CREATED_DATE, UM_USER_CONFIG FROM UM_TENANT WHERE UM_ID=?";
    public static final String GET_ALL_TENANTS_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE FROM UM_TENANT ORDER BY UM_ID";
    public static final String GET_DOMAIN_SQL = "SELECT UM_DOMAIN_NAME FROM UM_TENANT WHERE UM_ID=?";
    public static final String GET_TENANT_ID_SQL = "SELECT UM_ID FROM UM_TENANT WHERE UM_DOMAIN_NAME=?";
    public static final String ACTIVATE_SQL = "UPDATE UM_TENANT SET UM_ACTIVE='1' WHERE UM_ID=?";
    public static final String DEACTIVATE_SQL = "UPDATE UM_TENANT SET UM_ACTIVE='0' WHERE UM_ID=?";
    public static final String IS_TENANT_ACTIVE_SQL = "SELECT UM_ACTIVE FROM UM_TENANT WHERE UM_ID=?";
    public static final String DELETE_TENANT_SQL = "DELETE FROM UM_TENANT WHERE UM_ID=?";
    public static final String GET_MATCHING_TENANT_IDS_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL," +
            " UM_CREATED_DATE, UM_ACTIVE FROM UM_TENANT WHERE UM_DOMAIN_NAME like ?";
}
