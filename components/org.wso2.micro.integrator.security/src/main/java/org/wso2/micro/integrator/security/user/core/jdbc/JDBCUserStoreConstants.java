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
package org.wso2.micro.integrator.security.user.core.jdbc;


import org.wso2.micro.integrator.security.user.api.Property;
import org.wso2.micro.integrator.security.user.core.UserStoreConfigConstants;
import org.wso2.micro.integrator.security.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;

import java.util.ArrayList;

public class JDBCUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> JDBC_UM_MANDATORY_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> JDBC_UM_OPTIONAL_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> JDBC_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();
    private static final String usernameJavaRegExViolationErrorMsg = "UsernameJavaRegExViolationErrorMsg";
    private static final String usernameJavaRegExViolationErrorMsgDescription = "Error message when the Username is not " +
            "matched with UsernameJavaRegEx";
    private static final String passwordJavaRegExViolationErrorMsg = "PasswordJavaRegExViolationErrorMsg";
    private static final String passwordJavaRegExViolationErrorMsgDescription = "Error message when the Password is " +
            "not matched with passwordJavaRegEx";
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim values";
    private static final String VALIDATION_INTERVAL = "validationInterval";

    static {

        //setMandatoryProperty
        setMandatoryProperty(JDBCRealmConstants.URL, "Connection URL", "",
                "URL of the user store database", false);
        setMandatoryProperty(JDBCRealmConstants.USER_NAME, "Connection Name", "",
                "Username for the database", false);
        setMandatoryProperty(JDBCRealmConstants.PASSWORD, "Connection Password", "",
                "Password for the database", true);
        setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME, "Driver Name", "",
                "Full qualified driver name", false);

        //set optional properties
        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false", UserStoreConfigConstants.disabledDescription);
        setProperty("ReadOnly", "Read-only", "false", "Indicates whether the user store of this realm operates in the user read only mode or not");
        setProperty(UserStoreConfigConstants.readGroups, "Read Groups", "true", UserStoreConfigConstants.readLDAPGroupsDescription);
        setProperty(UserStoreConfigConstants.writeGroups, "Write Groups", "true", UserStoreConfigConstants.writeGroupsDescription);
        setProperty("UsernameJavaRegEx", "Username RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate user names");
        setProperty("UsernameJavaScriptRegEx", "Username RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression " +
                "used by the font-end components for username validation");
        setProperty(usernameJavaRegExViolationErrorMsg, "Username RegEx Violation Error Message",
                "Username pattern policy violated.", usernameJavaRegExViolationErrorMsgDescription);

        setProperty("PasswordJavaRegEx", "Password RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate passwords");
        setProperty("PasswordJavaScriptRegEx", "Password RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression " +
                "used by the font-end components for password validation");
        setProperty(passwordJavaRegExViolationErrorMsg, "Password RegEx Violation Error Message",
                "Password pattern policy violated.", passwordJavaRegExViolationErrorMsgDescription);
        setProperty("RolenameJavaRegEx", "Role Name RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate role names");
        setProperty("RolenameJavaScriptRegEx", "Role Name RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression used by the font-end components for role name validation");
        setProperty(JDBCCaseInsensitiveConstants.CASE_SENSITIVE_USERNAME, "Case Insensitive Username", "false",
                JDBCCaseInsensitiveConstants.CASE_SENSITIVE_USERNAME_DESCRIPTION);

        //set Advanced properties
        setAdvancedProperty(UserStoreConfigConstants.SCIMEnabled, "Enable SCIM", "false", UserStoreConfigConstants.SCIMEnabledDescription);
        setAdvancedProperty("IsBulkImportSupported", "Is Bulk Import Supported", "false", "Support Bulk User Import " +
                "Operation for this user store");
        setAdvancedProperty(JDBCRealmConstants.DIGEST_FUNCTION, "Password Hashing Algorithm", "SHA-256", UserStoreConfigConstants
                .passwordHashMethodDescription);
        setAdvancedProperty(MULTI_ATTRIBUTE_SEPARATOR, "Multiple Attribute Separator", ",", MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION);

        setAdvancedProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS, "Enable Salted Passwords", "true", "Indicates whether to salt " +
                "the password");

        setAdvancedProperty(UserStoreConfigConstants.maxUserNameListLength, "Maximum User List Length", "100", UserStoreConfigConstants
                .maxUserNameListLengthDescription);
        setAdvancedProperty(UserStoreConfigConstants.maxRoleNameListLength, "Maximum Role List Length", "100", UserStoreConfigConstants
                .maxRoleNameListLengthDescription);

        setAdvancedProperty(UserStoreConfigConstants.userRolesCacheEnabled, "Enable User Role Cache", "true", UserStoreConfigConstants
                .userRolesCacheEnabledDescription);

        setAdvancedProperty("UserNameUniqueAcrossTenants", "Make Username Unique Across Tenants", "false", "An attribute used for multi-tenancy");


        setAdvancedProperty(JDBCRealmConstants.VALIDATION_QUERY, "validationQuery for the database", "",
                "validationQuery is the SQL query that will be used to validate connections. This query MUST be an " +
                        "SQL SELECT statement that returns at least one row");
        setAdvancedProperty(VALIDATION_INTERVAL, "Validation Interval(time in milliseconds)", "", "Used to avoid " +
                "excess validation, only run validation at most at this frequency");

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_AUTO_COMMIT, "Default Auto commit", "",
                "The default auto-commit state of connections created by this pool");

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_READ_ONLY, "Default Read Only", "",
                "The default read-only state of connections created by this pool");

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_TRANSACTION_ISOLATION, "Default Transaction Isolation",
                "", "The default TransactionIsolation state of connections created by this pool");

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_CATALOG, "Default Catalog",
                "", "The default catalog of connections created by this pool");

        setAdvancedProperty(JDBCRealmConstants.INITIAL_SIZE, "Initial Size",
                "", "The initial number of connections that are created when the pool is started");

        setAdvancedProperty(JDBCRealmConstants.TEST_ON_RETURN, "Test On Return", "false", "The indication of " +
                "whether objects will be validated before being returned to the pool");

        setAdvancedProperty(JDBCRealmConstants.TEST_ON_BORROW, "Test On Borrow", "false", "The indication of " +
                "whether objects will be validated before being borrowed from the pool");

        setAdvancedProperty(JDBCRealmConstants.VALIDATOR_CLASS_NAME, "Validator Class Name",
                "", "The name of a class which implements the org.apache.tomcat.jdbc.pool.Validator interface and " +
                        "provides a no-arg constructor (may be implicit)");

        setAdvancedProperty(JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN, "Num Tests Per Eviction Run", "",
                " Property not used in tomcat-jdbc-pool");

        setAdvancedProperty(JDBCRealmConstants.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED, "Access To Underlying " +
                "Connection Allowed", "", "Property not used. Access can be achieved by calling unwrap on " +
                "the pooled connection");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_ABANDONED, "Remove Abandoned", "false",
                "Flag to remove abandoned connections if they exceed the removeAbandonedTimeout");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT, "Remove Abandoned Timeout", "",
                "Timeout in seconds before an abandoned(in use) connection can be removed");

        setAdvancedProperty(JDBCRealmConstants.LOG_ABANDONED, "Log Abandoned", "false",
                "Flag to log stack traces for application code which abandoned a Connection");

        setAdvancedProperty(JDBCRealmConstants.CONNECTION_PROPERTIES, "Connection Properties", "",
                "The connection properties that will be sent to our JDBC driver when establishing new connections");

        setAdvancedProperty(JDBCRealmConstants.INIT_SQL, "Init SQL", "",
                "A custom query to be run when a connection is first created");

        setAdvancedProperty(JDBCRealmConstants.JDBC_INTERCEPTORS, "JDBC Interceptors", "",
                "JDBC Interceptors");

        setAdvancedProperty(JDBCRealmConstants.JMX_ENABLED, "JMX Enabled", "true",
                "Register the pool with JMX or not");

        setAdvancedProperty(JDBCRealmConstants.FAIR_QUEUE, "Fiar Queue", "true",
                "Set to true if you wish that calls to getConnection should be treated fairly in a true FIFO fashion");

        setAdvancedProperty(JDBCRealmConstants.ABANDON_WHEN_PERCENTAGE_FULL, "Abandon when percentage full", "",
                "Connections that have been abandoned (timed out) wont get closed and reported up unless the number" +
                        " of connections in use are above the percentage defined by abandonWhenPercentageFull");

        setAdvancedProperty(JDBCRealmConstants.MAX_AGE, "Max Age", "",
                "Time in milliseconds to keep the connection");

        setAdvancedProperty(JDBCRealmConstants.USE_EQUALS, "Use Equals", "true",
                "Set to true if you wish the ProxyConnection class to use String.equals and set to false when you " +
                        "wish to use == when comparing method names");

        setAdvancedProperty(JDBCRealmConstants.SUSPECT_TIMEOUT, "Suspect Timeout", "",
                "Similar to to the removeAbandonedTimeout value but instead of treating the connection as " +
                        "abandoned, and potentially closing the connection, this simply logs the warning if " +
                        "logAbandoned is set to true");

        setAdvancedProperty(JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT, "Validation Query Timeout", "",
                "The timeout in seconds before a connection validation queries fail");

        setAdvancedProperty(JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED, "Alternate Username Allowed", "false",
                "If enabled, the pool size is still managed on a global level, and not on a per schema level");

        setAdvancedProperty(JDBCRealmConstants.COMMIT_ON_RETURN, "Commit On Return", "false",
                "If autoCommit==false then the pool can complete the transaction by calling commit on the " +
                        "connection as it is returned to the pool If rollbackOnReturn==true then this attribute is " +
                        "ignored");

        setAdvancedProperty(JDBCRealmConstants.ROLLBACK_ON_RETURN, "Rollback On Return", "false",
                "If autoCommit==false then the pool can terminate the transaction by calling rollback on the " +
                        "connection as it is returned to the pool");


        setAdvancedProperty("CountRetrieverClass", "Count Implementation",
                "org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever",
                "Name of the class that implements the count functionality");

        //Advanced Properties (No descriptions added for each property)
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER, "Select User SQL", JDBCRealmConstants.SELECT_USER_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_CASE_INSENSITIVE, "Select User SQL With " +
                "Case Insensitivie Username", JDBCCaseInsensitiveConstants.SELECT_USER_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty("GetRoleListSQL", "Get Role List SQL", "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME LIKE ? AND UM_TENANT_ID=? AND UM_SHARED_ROLE ='0' ORDER BY UM_ROLE_NAME", "");
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLE_LIST, "Get Shared Role List SQP", JDBCRealmConstants.GET_SHARED_ROLE_LIST_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER, "User Filter SQL", JDBCRealmConstants.GET_USER_FILTER_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE, "User Filter SQL With" +
                        " Case Insensitive Username", JDBCCaseInsensitiveConstants.GET_USER_FILTER_SQL_CASE_INSENSITIVE,
                "");
        setAdvancedProperty(JDBCRealmConstants.GET_USER_ROLE, "User Role SQL", JDBCRealmConstants.GET_USER_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_ROLE_EXIST, "User Role Exist SQL",
                JDBCRealmConstants.GET_IS_USER_ROLE_EXIST_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_ROLE_CASE_INSENSITIVE, "User Role SQL With " +
                "Case Insensitive Username", JDBCCaseInsensitiveConstants.GET_USER_ROLE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_ROLE_EXIST_CASE_INSENSITIVE,
                "User Role Exist " + "SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USER_ROLE_EXIST_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER, "User Shared Role SQL", JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_SHARED_ROLES_FOR_USER_CASE_INSENSITIVE, "User " +
                "Shared Role SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .GET_SHARED_ROLES_FOR_USER_SQL_CASE_INSENSITIVE, "");


        setAdvancedProperty(JDBCRealmConstants.GET_IS_ROLE_EXISTING, "Is Role Existing SQL", JDBCRealmConstants.GET_IS_ROLE_EXISTING_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_ROLE, "Get User List Of Role SQL", JDBCRealmConstants.GET_USERS_IN_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE, "Get User List Of Shared Role SQL", JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_SQL, "");

        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_EXISTING, "Is User Existing SQL", JDBCRealmConstants.GET_IS_USER_EXISTING_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE, "Is User " +
                "Existing SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .GET_IS_USER_EXISTING_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE, "Get User Properties for Profile SQL", JDBCRealmConstants.GET_PROPS_FOR_PROFILE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_PROPS_FOR_PROFILE_CASE_INSENSITIVE, "Get User " +
                "Properties for Profile SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .GET_PROPS_FOR_PROFILE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROP_FOR_PROFILE, "Get User Property for Profile SQL", JDBCRealmConstants.GET_PROP_FOR_PROFILE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_PROP_FOR_PROFILE_CASE_INSENSITIVE, "Get User " +
                "Property for Profile SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .GET_PROP_FOR_PROFILE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_FOR_PROP, "Get User List for Property SQL", JDBCRealmConstants.GET_USERS_FOR_PROP_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES, "Get Profile Names SQL", JDBCRealmConstants.GET_PROFILE_NAMES_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER, "Get User Profile Names SQL", JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE, "Get User " +
                "Profile Names SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .GET_PROFILE_NAMES_FOR_USER_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERID_FROM_USERNAME, "Get User ID From Username SQL", JDBCRealmConstants.GET_USERID_FROM_USERNAME_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USERID_FROM_USERNAME_CASE_INSENSITIVE, "Get User ID" +
                " From Username SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .GET_USERID_FROM_USERNAME_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERNAME_FROM_TENANT_ID, "Get Username From Tenant ID SQL", JDBCRealmConstants.GET_USERNAME_FROM_TENANT_ID_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME, "Get Tenant ID From Username SQL", JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE, "Get " +
                "Tenant ID From Username SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .GET_TENANT_ID_FROM_USERNAME_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.ADD_USER, "Add User SQL", JDBCRealmConstants.ADD_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_WITH_ID, "Add User With ID SQL", JDBCRealmConstants.ADD_USER_WITH_ID_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE, "Add User To Role SQL", JDBCRealmConstants.ADD_USER_TO_ROLE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE, "Add User To Role " +
                "SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .ADD_USER_TO_ROLE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE, "Add Role SQL", JDBCRealmConstants.ADD_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE, "Add Shared Role SQL", JDBCRealmConstants.ADD_SHARED_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER, "Add Role To User SQL", JDBCRealmConstants.ADD_ROLE_TO_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER, "Add Shared Role To User SQL",
                JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE, "Add Shared " +
                "Role To User SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .ADD_SHARED_ROLE_TO_USER_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE, "Remove User From Shared Roles SQL", JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE, "Remove User " +
                "From Role SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .REMOVE_USER_FROM_ROLE_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_ROLE, "Remove User From Role SQL", JDBCRealmConstants.REMOVE_USER_FROM_ROLE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE, "Remove User " +
                "From Role SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .REMOVE_USER_FROM_ROLE_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER, "Remove Role From User SQL", JDBCRealmConstants.REMOVE_ROLE_FROM_USER_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE, "Remove Role " +
                "From User SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .REMOVE_ROLE_FROM_USER_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.DELETE_ROLE, "Delete Roles SQL", JDBCRealmConstants.DELETE_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE, "On Delete Role Remove User Role Mapping SQL", JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL, "");
        setAdvancedProperty("DeleteUserSQL", "Delete User SQL", "DELETE FROM UM_USER WHERE UM_USER_NAME = ? AND UM_TENANT_ID=?", "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.DELETE_USER_CASE_INSENSITIVE, "Delete User SQL With " +
                "Case Insensitive Username", JDBCCaseInsensitiveConstants.DELETE_USER_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE, "On Delete User Remove User Role Mapping SQL", JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE, "On Delete User Remove User Attribute SQL", JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_CASE_INSENSITIVE, "On " +
                        "Delete User Remove User Attribute SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PASSWORD, "Update User Password SQL", JDBCRealmConstants.UPDATE_USER_PASSWORD_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE, "Update User " +
                "Password SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .UPDATE_USER_PASSWORD_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.UPDATE_ROLE_NAME, "Update Role Name SQL", JDBCRealmConstants.UPDATE_ROLE_NAME_SQL, "");

        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY, "Add User Property SQL", JDBCRealmConstants.ADD_USER_PROPERTY_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY, "Update User Property SQL", JDBCRealmConstants.UPDATE_USER_PROPERTY_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE, "Update User " +
                "Property SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .UPDATE_USER_PROPERTY_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER_PROPERTY, "Delete User Property SQL", JDBCRealmConstants.DELETE_USER_PROPERTY_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE, "Delete User " +
                "Property SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .DELETE_USER_PROPERTY_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.USER_NAME_UNIQUE, "User Name Unique Across Tenant SQL", JDBCRealmConstants.USER_NAME_UNIQUE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE, "User Name Unique " +
                "Across Tenant SQL With Case Insensitive Username", JDBCCaseInsensitiveConstants
                .USER_NAME_UNIQUE_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.IS_DOMAIN_EXISTING, "Is Domain Existing SQL", JDBCRealmConstants.IS_DOMAIN_EXISTING_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_DOMAIN, "Add Domain SQL", JDBCRealmConstants.ADD_DOMAIN_SQL, "");

        // mssql
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL, "Add User To Role SQL (MSSQL)", JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL, "Add Role To User SQL (MSSQL)", JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL, "Add User Property (MSSQL)", JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_MSSQL, "Add User To " +
                "Role SQL With Case Insensitive Username (MSSQL)", JDBCCaseInsensitiveConstants
                .ADD_USER_TO_ROLE_MSSQL_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_MSSQL, "Add Role To " +
                "User SQL With Case Insensitive Username (MSSQL)", JDBCCaseInsensitiveConstants
                .ADD_ROLE_TO_USER_MSSQL_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_MSSQL, "Add User " +
                "Property With Case Insensitive Username (MSSQL)", JDBCCaseInsensitiveConstants
                .ADD_USER_PROPERTY_MSSQL_SQL_CASE_INSENSITIVE, "");

        //openedge
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE, "Add User To Role SQL (OpenEdge)", JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE, "Add Role To User SQL (OpenEdge)", JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE, "Add User Property (OpenEdge)", JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE_SQL, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_OPENEDGE, "Add User " +
                "To Role SQL With Case Insensitive Username (OpenEdge)", JDBCCaseInsensitiveConstants
                .ADD_USER_TO_ROLE_OPENEDGE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_OPENEDGE, "Add Role " +
                "To User SQL With Case Insensitive Username (OpenEdge)", JDBCCaseInsensitiveConstants
                .ADD_ROLE_TO_USER_OPENEDGE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_OPENEDGE, "Add User " +
                "Property With Case Insensitive Username (OpenEdge)", JDBCCaseInsensitiveConstants
                .ADD_USER_PROPERTY_OPENEDGE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(UserStoreConfigConstants.claimOperationsSupported, UserStoreConfigConstants.getClaimOperationsSupportedDisplayName, "true",
                UserStoreConfigConstants.claimOperationsSupportedDescription);
        setProperty("UniqueID", "", "", "");
    }


    private static void setProperty(String name, String displayName, String value,
                                    String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        JDBC_UM_OPTIONAL_PROPERTIES.add(property);

    }

    private static void setMandatoryProperty(String name, String displayName, String value,
                                             String description, boolean encrypt) {
        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, null);
        JDBC_UM_MANDATORY_PROPERTIES.add(property);

    }

    private static void setAdvancedProperty(String name, String displayName, String value,
                                            String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        JDBC_UM_ADVANCED_PROPERTIES.add(property);

    }


}
