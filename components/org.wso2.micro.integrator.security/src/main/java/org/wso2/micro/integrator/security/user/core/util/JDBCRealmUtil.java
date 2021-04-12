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
package org.wso2.micro.integrator.security.user.core.util;

import org.wso2.micro.integrator.security.user.core.jdbc.JDBCRealmConstants;
import org.wso2.micro.integrator.security.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;

import java.util.Map;

/**
 * Map default JDBC User store properties if they are not configured in user-mgt.xml
 */

public class JDBCRealmUtil {

    public static Map<String, String> getSQL(Map<String, String> properties) {

        if (!properties.containsKey(JDBCRealmConstants.SELECT_USER)) {
            properties.put(JDBCRealmConstants.SELECT_USER, JDBCRealmConstants.SELECT_USER_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_ROLE_LIST)) {
            properties.put(JDBCRealmConstants.GET_ROLE_LIST, JDBCRealmConstants.GET_ROLE_LIST_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_SHARED_ROLE_LIST)) {
            properties.put(JDBCRealmConstants.GET_SHARED_ROLE_LIST, JDBCRealmConstants.GET_SHARED_ROLE_LIST_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_USER_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USER_ROLE, JDBCRealmConstants.GET_USER_ROLE_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_IS_USER_ROLE_EXIST)) {
            properties.put(JDBCRealmConstants.GET_IS_USER_ROLE_EXIST, JDBCRealmConstants.GET_IS_USER_ROLE_EXIST_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USERS_ROLE, JDBCRealmConstants.GET_USERS_ROLE_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER)) {
            properties.put(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER,
                    JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USER_FILTER)) {
            properties.put(JDBCRealmConstants.GET_USER_FILTER,
                    JDBCRealmConstants.GET_USER_FILTER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USER_FILTER_PAGINATED)) {
            properties.put(JDBCRealmConstants.GET_USER_FILTER_PAGINATED,
                    JDBCRealmConstants.GET_USER_FILTER_PAGINATED_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_DB2)) {
            properties.put(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_DB2,
                    JDBCRealmConstants.GET_USER_FILTER_PAGINATED_SQL_DB2);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_ORACLE)) {
            properties.put(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_ORACLE,
                    JDBCRealmConstants.GET_USER_FILTER_PAGINATED_SQL_ORACLE);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_MSSQL)) {
            properties.put(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_MSSQL,
                    JDBCRealmConstants.GET_USER_FILTER_PAGINATED_SQL_MSSQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_COUNT)) {
            properties.put(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_COUNT,
                    JDBCRealmConstants.GET_USER_FILTER_PAGINATED_COUNT_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_IS_ROLE_EXISTING)) {
            properties.put(JDBCRealmConstants.GET_IS_ROLE_EXISTING,
                    JDBCRealmConstants.GET_IS_ROLE_EXISTING_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_IN_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USERS_IN_ROLE,
                    JDBCRealmConstants.GET_USERS_IN_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_IS_USER_EXISTING)) {
            properties.put(JDBCRealmConstants.GET_IS_USER_EXISTING,
                    JDBCRealmConstants.GET_IS_USER_EXISTING_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROPS_FOR_PROFILE)) {
            properties.put(JDBCRealmConstants.GET_PROPS_FOR_PROFILE,
                    JDBCRealmConstants.GET_PROPS_FOR_PROFILE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE)) {
            properties.put(JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE,
                    JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROP_FOR_PROFILE)) {
            properties.put(JDBCRealmConstants.GET_PROP_FOR_PROFILE,
                    JDBCRealmConstants.GET_PROP_FOR_PROFILE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_FOR_PROP)) {
            properties.put(JDBCRealmConstants.GET_USERS_FOR_PROP,
                    JDBCRealmConstants.GET_USERS_FOR_PROP_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP)) {
            properties.put(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP,
                    JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_DB2)) {
            properties.put(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_DB2,
                    JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_SQL_DB2);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_ORACLE)) {
            properties.put(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_ORACLE,
                    JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_SQL_ORACLE);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_MSSQL)) {
            properties.put(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_MSSQL,
                    JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_SQL_MSSQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PAGINATED_USERS_COUNT_FOR_PROP)) {
            properties.put(JDBCRealmConstants.GET_PAGINATED_USERS_COUNT_FOR_PROP,
                    JDBCRealmConstants.GET_PAGINATED_USERS_COUNT_FOR_PROP_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROFILE_NAMES)) {
            properties.put(JDBCRealmConstants.GET_PROFILE_NAMES,
                    JDBCRealmConstants.GET_PROFILE_NAMES_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER)) {
            properties.put(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER,
                    JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERID_FROM_USERNAME)) {
            properties.put(JDBCRealmConstants.GET_USERID_FROM_USERNAME,
                    JDBCRealmConstants.GET_USERID_FROM_USERNAME_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME)) {
            properties.put(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME,
                    JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER)) {
            properties.put(JDBCRealmConstants.ADD_USER, JDBCRealmConstants.ADD_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_WITH_ID)) {
            properties.put(JDBCRealmConstants.ADD_USER_WITH_ID, JDBCRealmConstants.ADD_USER_WITH_ID_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_TO_ROLE)) {
            properties.put(JDBCRealmConstants.ADD_USER_TO_ROLE,
                    JDBCRealmConstants.ADD_USER_TO_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PERMISSION)) {
            properties.put(JDBCRealmConstants.ADD_USER_PERMISSION,
                    JDBCRealmConstants.ADD_USER_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE)) {
            properties.put(JDBCRealmConstants.ADD_ROLE, JDBCRealmConstants.ADD_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_SHARED_ROLE)) {
            properties.put(JDBCRealmConstants.ADD_SHARED_ROLE,
                    JDBCRealmConstants.ADD_SHARED_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_TO_USER)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_TO_USER,
                    JDBCRealmConstants.ADD_ROLE_TO_USER_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER)) {
            properties.put(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER,
                    JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_PERMISSION)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_PERMISSION,
                    JDBCRealmConstants.ADD_ROLE_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.REMOVE_USER_FROM_ROLE)) {
            properties.put(JDBCRealmConstants.REMOVE_USER_FROM_ROLE,
                    JDBCRealmConstants.REMOVE_USER_FROM_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE)) {
            properties.put(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE,
                    JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.REMOVE_ROLE_FROM_USER)) {
            properties.put(JDBCRealmConstants.REMOVE_ROLE_FROM_USER,
                    JDBCRealmConstants.REMOVE_ROLE_FROM_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.DELETE_ROLE)) {
            properties.put(JDBCRealmConstants.DELETE_ROLE, JDBCRealmConstants.DELETE_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE)) {
            properties.put(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE,
                    JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION)) {
            properties.put(JDBCRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION,
                    JDBCRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.DELETE_USER)) {
            properties.put(JDBCRealmConstants.DELETE_USER, JDBCRealmConstants.DELETE_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE)) {
            properties.put(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE,
                    JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE)) {
            properties.put(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE,
                    JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_USER_DELETE_PERMISSION)) {
            properties.put(JDBCRealmConstants.ON_DELETE_USER_DELETE_PERMISSION,
                    JDBCRealmConstants.ON_DELETE_USER_DELETE_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.UPDATE_USER_PASSWORD)) {
            properties.put(JDBCRealmConstants.UPDATE_USER_PASSWORD,
                    JDBCRealmConstants.UPDATE_USER_PASSWORD_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PROPERTY)) {
            properties.put(JDBCRealmConstants.ADD_USER_PROPERTY,
                    JDBCRealmConstants.ADD_USER_PROPERTY_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.USER_NAME_UNIQUE)) {
            properties.put(JDBCRealmConstants.USER_NAME_UNIQUE,
                    JDBCRealmConstants.USER_NAME_UNIQUE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.UPDATE_USER_PROPERTY)) {
            properties.put(JDBCRealmConstants.UPDATE_USER_PROPERTY,
                    JDBCRealmConstants.UPDATE_USER_PROPERTY_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.DELETE_USER_PROPERTY)) {
            properties.put(JDBCRealmConstants.DELETE_USER_PROPERTY,
                    JDBCRealmConstants.DELETE_USER_PROPERTY_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.UPDATE_ROLE_NAME)) {
            properties.put(JDBCRealmConstants.UPDATE_ROLE_NAME,
                    JDBCRealmConstants.UPDATE_ROLE_NAME_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL)) {
            properties.put(JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL,
                    JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL,
                    JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL)) {
            properties.put(JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL,
                    JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE,
                    JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USERS_ROLE, JDBCRealmConstants.GET_USERS_ROLE_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE)) {
            properties.put(JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE,
                    JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE_SQL);
        }

        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USERS_PROPS_FOR_PROFILE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USERS_PROPS_FOR_PROFILE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_USERS_PROPS_FOR_PROFILE_SQL_CASE_INSENSITIVE);
        }

        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USERS_ROLE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USERS_ROLE_CASE_INSENSITIVE, JDBCCaseInsensitiveConstants
                    .GET_USERS_ROLE_SQL_CASE_INSENSITIVE);
        }

        //openedge
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE)) {
            properties.put(JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE,
                    JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE,
                    JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE)) {
            properties.put(JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE,
                    JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE_SQL);
        }

        if (!properties.containsKey(JDBCCaseInsensitiveConstants.SELECT_USER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.SELECT_USER_CASE_INSENSITIVE, JDBCCaseInsensitiveConstants
                    .SELECT_USER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USER_ROLE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USER_ROLE_CASE_INSENSITIVE, JDBCCaseInsensitiveConstants
                    .GET_USER_ROLE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_IS_USER_ROLE_EXIST_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_IS_USER_ROLE_EXIST_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_USER_ROLE_EXIST_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USERS_ROLE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USERS_ROLE_CASE_INSENSITIVE, JDBCCaseInsensitiveConstants
                    .GET_USERS_ROLE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_SHARED_ROLES_FOR_USER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_SHARED_ROLES_FOR_USER_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_SHARED_ROLES_FOR_USER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_USER_FILTER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED,
                    JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_SQL);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_DB2)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_DB2,
                    JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_SQL_DB2);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_MSSQL)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_MSSQL,
                    JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_SQL_MSSQL);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_ORACLE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_ORACLE,
                    JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_SQL_ORACLE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_COUNT)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_COUNT,
                    JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_COUNT_SQL);
        }

        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_IS_USER_EXISTING_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_PROPS_FOR_PROFILE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_PROPS_FOR_PROFILE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_PROPS_FOR_PROFILE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USERS_PROPS_FOR_PROFILE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USERS_PROPS_FOR_PROFILE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_USERS_PROPS_FOR_PROFILE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_PROP_FOR_PROFILE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_PROP_FOR_PROFILE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_PROP_FOR_PROFILE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_USERID_FROM_USERNAME_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_USERID_FROM_USERNAME_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_USERID_FROM_USERNAME_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_SHARED_ROLE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.DELETE_USER_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.DELETE_USER_CASE_INSENSITIVE, JDBCCaseInsensitiveConstants
                    .DELETE_USER_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_USER_ROLE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_USER_ROLE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_USER_ROLE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.UPDATE_USER_PROPERTY_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE)) {
            properties.put(JDBCCaseInsensitiveConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE,
                    JDBCCaseInsensitiveConstants.DELETE_USER_PROPERTY_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_MSSQL)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_MSSQL,
                    JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_MSSQL_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_MSSQL)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_MSSQL,
                    JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_MSSQL_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_MSSQL)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_MSSQL,
                    JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_MSSQL_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_OPENEDGE)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_OPENEDGE,
                    JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_OPENEDGE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_OPENEDGE)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_OPENEDGE,
                    JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_OPENEDGE_SQL_CASE_INSENSITIVE);
        }
        if (!properties.containsKey(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_OPENEDGE)) {
            properties.put(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_OPENEDGE,
                    JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_OPENEDGE_SQL_CASE_INSENSITIVE);
        }

        return properties;
    }
}
