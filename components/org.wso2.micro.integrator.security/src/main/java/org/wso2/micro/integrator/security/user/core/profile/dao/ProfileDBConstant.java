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
package org.wso2.micro.integrator.security.user.core.profile.dao;

public class ProfileDBConstant {

    public static final String ADD_PROFILE_CONFIG = "INSERT INTO UM_PROFILE_CONFIG "
            + "(UM_PROFILE_NAME, UM_DIALECT_ID, UM_TENANT_ID) VALUES(?,?,?)";

    public static final String ADD_CLAIM_BEHAVIOR = "INSERT INTO UM_CLAIM_BEHAVIOR "
            + "(UM_PROFILE_ID, UM_CLAIM_ID, UM_BEHAVIOUR, UM_TENANT_ID) VALUES(?, ?, ?, ?)";

    public static final String GET_ALL_PROFILE_CONFIGS = "SELECT UM_CLAIM_URI, UM_PROFILE_NAME," +
            "UM_BEHAVIOUR, UM_DIALECT_URI FROM UM_PROFILE_CONFIG, UM_CLAIM, UM_CLAIM_BEHAVIOR, UM_DIALECT " +
            "WHERE UM_CLAIM_BEHAVIOR.UM_CLAIM_ID=UM_CLAIM.UM_ID AND " +
            "UM_PROFILE_CONFIG.UM_ID=UM_CLAIM_BEHAVIOR.UM_PROFILE_ID AND " +
            "UM_PROFILE_CONFIG.UM_DIALECT_ID=UM_DIALECT.UM_ID AND " +
            "UM_PROFILE_CONFIG.UM_TENANT_ID=? AND UM_CLAIM.UM_TENANT_ID=? AND " +
            "UM_CLAIM_BEHAVIOR.UM_TENANT_ID=? AND UM_DIALECT.UM_TENANT_ID=?";

    public static final String DELETE_PROFILE_CONFIG = "DELETE FROM UM_PROFILE_CONFIG " +
            "WHERE UM_PROFILE_NAME=? AND UM_DIALECT_ID=(SELECT UM_ID FROM UM_DIALECT WHERE " +
            "UM_DIALECT_URI=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";

    public static final String DELETE_CLAIM_BEHAVIOR = "DELETE FROM UM_CLAIM_BEHAVIOR " +
            "WHERE UM_PROFILE_ID=(SELECT UM_ID FROM UM_PROFILE_CONFIG WHERE " +
            "UM_PROFILE_NAME=? AND UM_DIALECT_ID=(SELECT UM_ID FROM UM_DIALECT WHERE " +
            "UM_DIALECT_URI=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";

    public static final String GET_CLAIM_IDS = "SELECT UM_ID, UM_CLAIM_URI FROM UM_CLAIM WHERE UM_TENANT_ID=?";

    public static final String GET_DIALECT_ID = "SELECT UM_ID FROM UM_DIALECT WHERE " +
            "UM_DIALECT_URI=? AND UM_TENANT_ID=?";

    public static final String GET_PROFILE_ID = "SELECT UM_ID FROM UM_PROFILE_CONFIG WHERE " +
            "UM_PROFILE_NAME=? AND UM_TENANT_ID=?";

    public static final String ON_DIALECT_DELETE_REMOVE_PROFILE_CONFIGS = "DELETE FROM UM_PROFILE_CONFIG "
            + "WHERE UM_DIALECT_ID IN (SELECT UM_ID FROM UM_DIALECT WHERE UM_DIALECT_URI=? AND UM_REALM=?)";


}
