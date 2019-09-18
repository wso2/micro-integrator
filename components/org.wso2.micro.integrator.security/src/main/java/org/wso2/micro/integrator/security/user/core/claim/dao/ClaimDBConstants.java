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
package org.wso2.micro.integrator.security.user.core.claim.dao;

public final class ClaimDBConstants {

    public static final String ADD_DIALECT_SQL = "INSERT INTO UM_DIALECT (UM_DIALECT_URI, " +
            "UM_TENANT_ID) VALUES (?, ?)";
    public static final String ADD_CLAIM_SQL = "INSERT INTO UM_CLAIM(UM_DIALECT_ID, UM_CLAIM_URI, " +
            "UM_DISPLAY_TAG, UM_DESCRIPTION, UM_MAPPED_ATTRIBUTE, UM_REG_EX, " +
            "UM_SUPPORTED, UM_REQUIRED, UM_DISPLAY_ORDER, UM_TENANT_ID, UM_MAPPED_ATTRIBUTE_DOMAIN, UM_CHECKED_ATTRIBUTE,UM_READ_ONLY) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public static final String DELETE_CLAIM_SQL = "DELETE FROM UM_CLAIM WHERE UM_CLAIM_URI=? AND " +
            "UM_DIALECT_ID=(SELECT UM_ID FROM UM_DIALECT WHERE UM_DIALECT_URI=? " +
            "AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String GET_ALL_DIALECT_SQL = "SELECT * FROM UM_DIALECT";
    public static final String GET_ALL_CLAIMS_SQL = "SELECT UM_DIALECT_URI, UM_CLAIM_URI, UM_DISPLAY_TAG, " +
            "UM_DESCRIPTION, UM_MAPPED_ATTRIBUTE, UM_REG_EX, UM_SUPPORTED, UM_REQUIRED, UM_DISPLAY_ORDER," +
            "UM_MAPPED_ATTRIBUTE_DOMAIN, UM_CHECKED_ATTRIBUTE,UM_READ_ONLY " +
            "FROM UM_CLAIM, UM_DIALECT WHERE UM_CLAIM.UM_DIALECT_ID=UM_DIALECT.UM_ID " +
            "AND UM_CLAIM.UM_TENANT_ID=? AND UM_DIALECT.UM_TENANT_ID=?";
    public static final String GET_CLAIMS_FOR_DIALECTT_SQL = "SELECT UM_CLAIM_URI FROM " +
            "UM_CLAIM, UM_DIALECT WHERE UM_CLAIM.UM_DIALECT_ID=UM_DIALECT.UM_ID AND UM_DIALECT_URI=? " +
            "AND UM_CLAIM.UM_TENANT_ID=? AND UM_DIALECT.UM_TENANT_ID=?";
    public static final String GET_DIALECT_ID_SQL = "SELECT UM_ID FROM UM_DIALECT WHERE " +
            "UM_DIALECT_URI=? AND UM_TENANT_ID=?";
    public static final String COUNT_DIALECTS = "SELECT COUNT(UM_ID) FROM UM_DIALECT WHERE " +
            "UM_TENANT_ID=?";
    public static final String DELETE_DIALECT = "DELETE FROM UM_DIALECT WHERE UM_DIALECT_URI=?";
    public static final String UM_DIALECT_ID = "UM_DIALECT_ID";
    public static final String UM_CLAIM_URI = "UM_CLAIM_URI";
    public static final String UM_DISPLAY_TAG = "UM_DISPLAY_TAG";
    public static final String UM_DESCRIPTION = "UM_DESCRIPTION";
    public static final String UM_MAPPED_ATTR = "UM_MAPPED_ATTRIBUTE";
    public static final String UM_REG_EX = "UM_REG_EX";
    public static final String UM_SUPPORTED = "UM_SUPPORTED";
    public static final String UM_REQUIRED = "UM_REQUIRED";
    private ClaimDBConstants() {
    }

}
