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

package org.wso2.micro.integrator.security.user.core.constants;

/**
 * This class is holds the constants related with UserCore Error Management.
 */
public class UserCoreErrorConstants {

    /**
     * Relevant error messages and error codes.
     */
    public enum ErrorMessages {

        // Generic error messages
        ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE("30001", "Unsupported credential type"),
        ERROR_CODE_READONLY_USER_STORE("30002", INVALID_OPERATION + " Invalid operation. User store is read only"),
        ERROR_CODE_INVALID_PASSWORD("30003", "Credential is not valid. Credential must be a non null string with "
                + "following format, %s"),
        ERROR_CODE_USER_ALREADY_EXISTS("30004", EXISTING_USER + "Username %s already exists in the system. Please pick "
                + "another username."),
        ERROR_CODE_INVALID_CLAIM_URI("30005", "Claim URI is invalid, %s"),
        ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH("30006", INVALID_PASSWORD + " Old credential does not match with the "
                + "existing credentials."),
        ERROR_CODE_NON_EXISTING_USER("30007", USER_NOT_FOUND + ": User %s does not exist in: %s"),
        ERROR_CODE_CANNOT_REMOVE_ADMIN_ROLE_FROM_ADMIN("30008", REMOVE_ADMIN_USER + " Cannot remove Admin user from "
                + "Admin role"),
        ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE("30009", "Cannot update everyone role"),
        ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE("30010", "Cannot update admin role"),
        ERROR_CODE_INVALID_ROLE_NAME("30011",  INVALID_ROLE + " Role name %s is not valid. Role name must be a "
                + "non null string with following format, %s"),
        ERROR_CODE_ROLE_ALREADY_EXISTS("30012", EXISTING_ROLE + "Role name: %s exists in the system. Please pick "
                + "another role name."),
        ERROR_CODE_SHARED_ROLE_NOT_SUPPORTED("30013", SHARED_USER_ROLES + "User store does not support shared user "
                + "roles functionality"),
        ERROR_CODE_WRITE_GROUPS_NOT_ENABLED("30014", NO_READ_WRITE_PERMISSIONS + " Write groups is not enabled in "
                + "user store"),
        ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE("30015", "Constrain violation while writing to database."),


        // Error code related with authentication
        ERROR_CODE_ERROR_WHILE_AUTHENTICATION("31001", "Un-expected error while authenticating"),
        ERROR_CODE_ERROR_WHILE_PRE_AUTHENTICATION("31002", "Un-expected error while pre-authenticating, %s"),
        ERROR_CODE_TENANT_DEACTIVATED("31003", "Tenant has been deactivated. TenantID : %s"),
        ERROR_CODE_ERROR_WHILE_POST_AUTHENTICATION("31004", "TUn-expected error while post-authentication, %s"),

        // Error code related with getClaimValue
        ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE("32001", "Un-expected error while getting user-claim value, "
                + "%s"),
        ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE("32002", "Un-expected error in post get user-claims,%s"),

        // Error code related with GetClaimValues
        ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_URI("33001", "Un-expected error while getting claim uri, %s"),
        ERROR_CODE_ERROR_WHILE_GETTING_CLAIM_VALUES("33002", "Un-expected error while getting claim values, %s"),
        ERROR_CODE_ERROR_IN_POST_GET_CLAIM_VALUES("33003", "Un-expected error during post get claim values, %s"),

        // Error code related with Add ClaimValues
        ERROR_CODE_DUPLICATE_ERROR_WHILE_ADDING_CLAIM_MAPPINGS("33004", "Duplicate entries are found when adding " +
                "claim mappings."),

        // Error code related with GetUserList
        ERROR_CODE_INVALID_CLAIM_VALUE("34002", "Claim Value is invalid"),
        ERROR_CODE_ERROR_WHILE_GETTING_USER_LIST("34003", "Un-expected error while getting user list, %s"),
        ERROR_CODE_ERROR_DURING_POST_GET_USER_LIST("34004", "Un-expected error during post get user list, %s"),
        ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST("34005", "Un-expected error during pre get user list, %s"),
        ERROR_CODE_ERROR_DURING_POST_GET_CONDITIONAL_USER_LIST("34006", "Un-expected error during post get " +
                "conditional user list,%s"),
        ERROR_CODE_ERROR_DURING_PRE_GET__CONDITIONAL_USER_LIST("34007", "Un-expected error during pre get " +
                "conditional user list, %s"),

        // Error code related with GetPaginatedUserList
        ERROR_CODE_ERROR_WHILE_GETTING_PAGINATED_USER_LIST("34103", "Un-expected error while getting paginated user " +
                "list, %s"),
        ERROR_CODE_ERROR_DURING_POST_GET_PAGINATED_USER_LIST("34104", "Un-expected error during post get " +
                "paginated user list, %s"),
        ERROR_CODE_ERROR_DURING_PRE_GET_PAGINATED_USER_LIST("34105", "Un-expected error during pre get paginated user" +
                "list, %s"),


        // Error code related with ListPaginatedUsers
        ERROR_CODE_ERROR_WHILE_LISTING_PAGINATED_USERS("34203", "Un-expected error while listing paginated user, %s"),
        ERROR_CODE_ERROR_DURING_POST_LIST_PAGINATED_USER("34204", "Un-expected error during post list paginated " +
                "users, %s"),
        ERROR_CODE_ERROR_DURING_PRE_LIST_PAGINATED_USER("34205", "Un-expected error during pre list paginated " +
                "users, %s"),

        // Error code related with update credential
        ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL("35001", "Un-expected error during pre update credential, %s"),
        ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL("35002", "Un-expected error while updating credential, %s"),
        ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL("35003", "Un-expected error while updating credential, %s"),

        // Error code related with update credential by admin
        ERROR_CODE_ERROR_DURING_PRE_UPDATE_CREDENTIAL_BY_ADMIN("36001", "Un-expected error during pre update credential "
                + "by admin, %s"),
        ERROR_CODE_ERROR_WHILE_UPDATING_CREDENTIAL_BY_ADMIN("36002", "Un-expected error while updating credential by "
                + "admin, %s"),
        ERROR_CODE_ERROR_DURING_POST_UPDATE_CREDENTIAL_BY_ADMIN("36003", "Un-expected error during post update "
                + "credential by admin, %s"),

        // Error code related with delete user
        ERROR_CODE_DELETE_LOGGED_IN_USER("37001", LOGGED_IN_USER + " Cannot delete logged in user"),
        ERROR_CODE_DELETE_ADMIN_USER("37002", ADMIN_USER + "Cannot delete admin user"),
        ERROR_CODE_DELETE_ANONYMOUS_USER("37003", ANONYMOUS_USER + "Cannot delete anonymous user"),
        ERROR_CODE_ERROR_DURING_PRE_DELETE_USER("37004", "Un-expected error during pre delete user, %s"),
        ERROR_CODE_ERROR_WHILE_DELETING_USER("37005", "Un-expected error while deleting user, %s"),
        ERROR_CODE_ERROR_DURING_POST_DELETE_USER("37006", "Un-expected error during post delete user, %s"),

        // Error code related with setting user claim value.
        ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUE("38001", "Un-expected error during pre-step of set user claim "
                + "value, %s"),
        ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUE("38002", "Un-expected error while setting user claim value, %s"),
        ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUE("38003", "Un-expected error during post step of set user "
                + "claim value, %s"),

        // Error code related with setting user claim values.
        ERROR_CODE_ERROR_DURING_PRE_SET_USER_CLAIM_VALUES("39001", "Un-expected error during pre-step of set user claim "
                + "values, %s"),
        ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES("39002", "Un-expected error while setting user claim values,"
                + " %s"),
        ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES("39003", "Un-expected error during post step of set user "
                + "claim values, %s"),

        // Error code related with deleting user claim values.
        ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUES("31101", "Un-expected error during pre-step of deleting "
                + "user claim values, %s"),
        ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUES("31102", "Un-expected error during post step of deleting "
                + "user claim values, %s"),
        ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES("31103", "Un-expected error while deleting user claim "
                + "value, %s"),

        // Error code related with deleting user claim value.
        ERROR_CODE_ERROR_DURING_PRE_DELETE_USER_CLAIM_VALUE("31201", "Un-expected error during pre-step of deleting "
                + "user claim value, %s"),
        ERROR_CODE_ERROR_DURING_POST_DELETE_USER_CLAIM_VALUE("31202", "Un-expected error during post step of deleting "
                + "user claim value, %s"),
        ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE("31203", "Un-expected error while deleting user claim value, "
                + "%s"),

        // Error code related with adding user
        ERROR_CODE_INVALID_USER_NAME("31301", "Username %s is not valid. User name must be a non null string with "
                + "following format, %s"),
        ERROR_CODE_ERROR_DURING_PRE_ADD_USER("31302", "Un-expected error during pre-step of adding user, %s"),
        ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS("31303", "Internal role does not exist : %s"),
        ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS("31304", "External role does not exist : %s"),
        ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING("31305", "Error while obtaining claim mapping for %s"),
        ERROR_CODE_ERROR_WHILE_ADDING_USER("31306", "Un-expected error while adding user, %s"),
        ERROR_CODE_ERROR_DURING_POST_ADD_USER("31307", "Un-expected error during post-step of adding user, %s"),
        ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_USER("31308", "Constrain violation while adding a system user."),
        ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER("31309", "Constrain violation while adding a user."),

        // Error code related with updating role list of user
        ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_OF_USER("31401", "Un-expected error during pre-step of updating role of "
                + "user, %s"),
        ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_OF_USER("31402", "Un-expected error while update role of user, %s"),
        ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER("31403", "Un-expected error during post-step of updating role "
                + "of user, %s"),

        // Error code related with updating role name
        ERROR_CODE_CANNOT_RENAME_ROLE("31501", "The role cannot renamed"),
        ERROR_CODE_ERROR_DURING_PRE_UPDATE_ROLE_NAME("31502", "Un-expected error during pre-step of updating role "
                + "name, %s"),
        ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_NAME("31503", "Un-expected error while updating role name, %s"),
        ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_NAME("31504", "Un-expected error during post-step of updating role "
                + "name, %s"),

        // Error code while getting users of role
        ERROR_CODE_ERROR_WHILE_GETTING_USER_OF_ROLE("31601", "Un-expected error while updating users of user, %s"),
        ERROR_CODE_ERROR_DURING_POST_GET_USER_OF_ROLE("31602", "Un-expected error while updating users of user, %s"),

        // Error code while adding role
        ERROR_CODE_ERROR_DURING_PRE_ADD_ROLE("31701", "Un-expected error during pre-step of add role, %s"),
        ERROR_CODE_ERROR_WHILE_ADDING_ROLE("31702", "Un-expected error while adding role, %s"),
        ERROR_CODE_ERROR_DURING_POST_ADD_ROLE("31703", "Un-expected error during post-step of adding role, "
                + "%s"),
        ERROR_CODE_CANNOT_ADD_EMPTY_ROLE("31704", "Cannot add role with empty role name"),
        ERROR_CODE_DUPLICATE_WHILE_ADDING_A_SYSTEM_ROLE("31705", "Constrain violation while adding a system role."),
        ERROR_CODE_DUPLICATE_WHILE_ADDING_A_HYBRID_ROLE("31706", "Constrain violation while adding a hybrid role."),
        ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE("31707", "Constrain violation while adding a role."),

        // Error code while deleting role
        ERROR_CODE_ERROR_DURING_PRE_DELETE_ROLE("31801", "Un-expected error during pre-step of delete "
                + "role, %s"),
        ERROR_CODE_ERROR_WHILE_DELETE_ROLE("31802", "Un-expected error while deleting role, %s"),
        ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE("31803", "Un-expected error during post-step of delete role"
                + ", %s"),
        ERROR_CODE_CANNOT_DELETE_ADMIN_ROLE("31804", "Cannot delete admin role"),
        ERROR_CODE_CANNOT_DELETE_EVERYONE_ROLE("31805", "Cannot delete everyone role"),
        ERROR_CODE_CANNOT_DELETE_NON_EXISTING_ROLE("31806", "Cannot delete non-existing role"),

        // Error code while updating user list of role
        ERROR_CODE_ERROR_DURING_UPDATE_USERS_OF_ROLE("31901", "Un-expected error while updating user list of "
                + "role, %s"),
        ERROR_CODE_ERROR_WHILE_PRE_UPDATE_USERS_OF_ROLE("31902", "Un-expected error during pre-step of "
                + "updating user list of role, %s"),
        ERROR_CODE_ERROR_WHILE_POST_UPDATE_USERS_OF_ROLE("31903", "Un-expected error during post-step of "
                + "updating user list of role, %s"),

        // Error code related with updating permissions of role.
        ERROR_CODE_ERROR_WHILE_UPDATING_PERMISSIONS_OF_ROLE("32101", "Un-expected error while updating permissions of  "
                + "updating role,  %s");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + " - " + message;
        }

    }

    private static final String INVALID_OPERATION = "InvalidOperation";
    private static final String EXISTING_USER = "UserAlreadyExisting";
    private static final String INVALID_PASSWORD = "PasswordInvalid";
    private static final String LOGGED_IN_USER = "LoggedInUser";
    private static final String ADMIN_USER = "AdminUser";
    private static final String ANONYMOUS_USER = "AnonymousUser";
    private static final String USER_NOT_FOUND = "UserNotFound";
    private static final String REMOVE_ADMIN_USER = "RemoveAdminUser";
    private static final String INVALID_ROLE = "InvalidRole";
    private static final String SHARED_USER_ROLES = "SharedUserRoles";
    private static final String EXISTING_ROLE = "RoleExisting";
    private static final String NO_READ_WRITE_PERMISSIONS = "NoReadWritePermission";
    public static final String AUDIT_LOGGER_CLASS_NAME = "AuditLogger";
    public static final String PRE_LISTENER_TASKS_FAILED_MESSAGE = "Pre-listener tasks failed";
    public static final String POST_LISTENER_TASKS_FAILED_MESSAGE = "Post-listener tasks failed";
}
