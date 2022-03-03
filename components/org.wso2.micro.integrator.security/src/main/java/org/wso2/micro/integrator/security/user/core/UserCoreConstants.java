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
package org.wso2.micro.integrator.security.user.core;

import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

public class UserCoreConstants {

    public static final String DATA_SOURCE = "um.datasource";
    public static final String LDAP_CONNECTION_SOURCE = "ldapConnectionSource";
    public static final String TENANT_MGT_CONFIGURATION = "tenantMgtConfiguration";
    public static final String REALM_CONFIGURATION = "userMgtConfiguration";

    public static final String USE_SEPARATE_REALM_CONFIGURATION = "userSeparateRealmConfig";

    public static final short BEHAVIOUR_INHERITED = 0;
    public static final short BEHAVIOUR_HIDDEN = 1;
    public static final short BEHAVIOUR_OVERRIDDEN = 2;

    public static final String INTERNAL_USERSTORE = "Internal";
    public static final String EXTERNAL_USERSTORE = "External";

    public static final String DELEGATING_REALM = "Delegating";
    public static final String DEFAULT_REALM = "Default";
    public static final String HYBRID_REALM = "Hybrid";
    public static final String REALM_GENRE = "RealmGenre";
    public static final String DEFAULT_CARBON_DIALECT = "http://wso2.org/claims";

    public static final short DENY = 0;
    public static final short ALLOW = 1;

    public static final String UM_TENANT_COLUMN = "UM_TENANT_ID";

    public static final String SYSTEM_RESOURCE = "System";
    public static final String MSSQL_TYPE = "mssql";
    public static final String MYSQL_TYPE = "mysql";
    public static final String OPENEDGE_TYPE = "openedge";

    /*To hold a boolean property to track the first startup of the server.*/
    public static final String FIRST_STARTUP_CHECK = "FistStartupCheck";

    public static final int MAX_USER_ROLE_LIST = 100;
    public static final int MAX_SEARCH_TIME = 10000;   // ms

    public static final String INTERNAL_DOMAIN = "Internal";
    public static final String INTERNAL_DOMAIN_LOWER_CASED = "internal";
    public static final String PRIMARY_DEFAULT_DOMAIN_NAME = "PRIMARY";
    public static final String SYSTEM_DOMAIN_NAME = "SYSTEM";

    public static final String DEFAULT_CACHE_IDENTIFIER = "defaultCacheDomain";

    public static final String IS_USER_IN_ROLE_CACHE_IDENTIFIER = "@__isUserHasTheRole__@";

    public static final String DOMAIN_SEPARATOR;

    static {
        String userDomainSeparator = CarbonServerConfigurationService.getInstance().getFirstProperty("UserDomainSeparator");
        if (userDomainSeparator != null && !userDomainSeparator.trim().isEmpty()) {
            DOMAIN_SEPARATOR = userDomainSeparator.trim();
        } else {
            DOMAIN_SEPARATOR = "/";
        }
    }

    public static final String PRINCIPAL_USERNAME_SEPARATOR = "_";

    public static final String SHARED_ROLE_TENANT_SEPERATOR = "@SharedRoleSeperator@";

    public static final String NAME_COMBINER = "$_USERNAME_SEPARATOR_$";

    public static final String TENANT_DOMAIN_COMBINER = "@";

    public static final String SHARED_ROLE_TENANT_COMBINER = "~";

    public static final int USER_ROLE_CACHE_DEFAULT_TIME_OUT = 5;
    public static final String INVOKE_SERVICE_PERMISSION = "invoke-service";
    public static final String AUTHZ_CACHE = "AuthzCache";
    public static final String ROLE_CACHE = "RoleCache";
    public static final int MAX_OBJECTS_IN_CACHE = 5000;
    public static final int TTL_CACHE = 30;
    public static final int IDLE_TIME_IN_CACHE = 0;
    public static final String DEFAULT_PROFILE = "default";
    public static final String DEFAULT_PROFILE_CONFIGURATION = "default";
    public static final String PROFILE_CONFIGURATION = "profileConfiguration";
    public static final String PICTURE_CLAIM = "picture";
    public static final String ROLE_CLAIM = "http://wso2.org/claims/role";
    public static final String INT_ROLE_CLAIM = "http://wso2.org/claims/role/internal";
    public static final String EXT_ROLE_CLAIM = "http://wso2.org/claims/role/external";
    public static final String CLAIM_HIDDEN = "Hidden";
    public static final String CLAIM_OVERRIDEN = "Overridden";
    public static final String CLAIM_INHERITED = "Inherited";
    public static final String AUTHORIZATION_ACTION_LOGIN = "login";
    public static final String AUTHORIZATION_ACTION_MANAGE_CONFIGURATION = "manage-configuration";
    public static final String AUTHORIZATION_ACTION_MANAGE_SECURITY = "manage-security";
    public static final String LOCAL_NAME_PROPERTY = "Property";
    public static final String USER_LOCKED = "true";
    public static final String USER_UNLOCKED = "false";

    // Constants from org.wso2.carbon.CarbonConstants
    public static final String REGISTRY_ANONNYMOUS_USERNAME = "wso2.anonymous.user";
    public static final String REGISTRY_ANONNYMOUS_ROLE_NAME = "system/wso2.anonymous.role";
    public static final String REGISTRY_SYSTEM_USERNAME = "wso2.system.user";
    public static final String UI_PERMISSION_ACTION = "ui.execute";
    public static final String UI_PERMISSION_NAME = "permission";
    public static final int REMEMBER_ME_COOKIE_TTL = 604800; //in seconds // 7 days
    public static final String CARBON_HOME_PARAMETER = "${carbon.home}";
    public static final String IS_PASSWORD_TRIM_ENABLED = "EnablePasswordTrim";
    public static final String UI_PERMISSION_COLLECTION = "/" + UI_PERMISSION_NAME;
    public static final String UI_ADMIN_PERMISSION_COLLECTION = UI_PERMISSION_COLLECTION + "/admin";
    /**
     * This is used to get root context within CarbonJNDIContext when we need to operate
     * with LDAP.
     */
    public static final String REQUEST_BASE_CONTEXT = "org.wso2.carbon.context.RequestBaseContext";

    // Constants from org.wso2.carbon.utils.multitenancy.MultitenantConstants
    public static final String ENABLE_EMAIL_USER_NAME = "EnableEmailUserName";

    public static final class RealmConfig {
        public static final String LOCAL_NAME_USER_MANAGER = "UserManager";
        public static final String LOCAL_NAME_REALM = "Realm";
        public static final String LOCAL_NAME_CONFIGURATION = "Configuration";
        public static final String LOCAL_NAME_PROPERTY = "Property";
        public static final String LOCAL_NAME_ADD_ADMIN = "AddAdmin";
        public static final String LOCAL_NAME_RESERVED_ROLE_NAMES = "ReservedRoleNames";
        public static final String LOCAL_NAME_RESTRICTED_DOMAINS_FOR_SELF_SIGN_UP = "RestrictedDomainsForSelfSignUp";
        public static final String LOCAL_NAME_ADMIN_ROLE = "AdminRole";
        public static final String LOCAL_NAME_ADMIN_USER = "AdminUser";
        public static final String LOCAL_NAME_USER_NAME = "UserName";
        public static final String LOCAL_NAME_PASSWORD = "Password";
        public static final String LOCAL_NAME_AUTHENTICATOR = "Authenticator";
        public static final String LOCAL_NAME_USER_STORE_MANAGER = "UserStoreManager";
        public static final String LOCAL_NAME_ATHZ_MANAGER = "AuthorizationManager";
        public static final String LOCAL_NAME_SYSTEM_USER_NAME = "SystemUserName";
        public static final String LOCAL_NAME_EVERYONE_ROLE = "EveryOneRoleName";
        public static final String LOCAL_NAME_ANONYMOUS_USER = "AnonymousUser";
        public static final String LOCAL_PASSWORDS_EXTERNALLY_MANAGED = "PasswordsExternallyManaged";
        public static final String OVERRIDE_USERNAME_CLAIM_FROM_INTERNAL_USERNAME =
                "OverrideUsernameClaimFromInternalUsername";
        public static final String ATTR_NAME_CLASS = "class";
        public static final String ATTR_NAME_PROP_NAME = "name";
        public static final String PROPERTY_EVERYONEROLE_AUTHORIZATION = "EveryoneRoleManagementPermissions";
        public static final String PROPERTY_ADMINROLE_AUTHORIZATION = "AdminRoleManagementPermissions";
        public static final String PROPERTY_UPDATE_PERM_TREE_PERIODICALLY = "UpdatePermissionTreePeriodically";
        public static final String PROPERTY_USERNAME_UNIQUE = "UserNameUniqueAcrossTenants";
        public static final String PROPERTY_IS_EMAIL_USERNAME = "IsEmailUserName";
        public static final String PROPERTY_DOMAIN_CALCULATION = "DomainCalculation";
        public static final String PROPERTY_IS_USERS_OF_ROLE_LISTING = "IsUsersOfRoleListing";
        public static final String PROPERTY_READ_ONLY = "ReadOnly";
        public static final String CLASS_DESCRIPTION = "Description";
        public static final String PROPERTY_PRESERVE_CASE_FOR_RESOURCES = "PreserveCaseForResources";

        public static final String EMAIL_VALIDATION_REGEX = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";

        @Deprecated
        public static final String PROPERTY_INTERNAL_ROLES_ONLY = "InternalJDBCRolesOnly";
        public static final String PROPERTY_MAX_USER_LIST = "MaxUserNameListLength";
        public static final String PROPERTY_MAX_ROLE_LIST = "MaxRoleNameListLength";
        public static final String PROPERTY_MAX_SEARCH_TIME = "MaxSearchQueryTime";

        public static final String READ_GROUPS_ENABLED = "ReadGroups";

        public static final String WRITE_GROUPS_ENABLED = "WriteGroups";

        public static final String USER_STORE_DISABLED = "Disabled";

        public static final String PROPERTY_VALUE_DOMAIN_CALCULATION_DEFAULT = "default";
        public static final String PROPERTY_VALUE_DOMAIN_CALCULATION_CUSTOM = "custom";

        public static final String PROPERTY_VALUE_DEFAULT_MAX_COUNT = "100";
        public static final String PROPERTY_VALUE_DEFAULT_READ_ONLY = "false";

        public static final String PROPERTY_JAVA_REG_EX = "PasswordJavaRegEx";
        public static final String PROPERTY_JS_REG_EX = "PasswordJavaScriptRegEx";

        public static final String PROPERTY_USER_NAME_JAVA_REG_EX = "UsernameJavaRegEx";
        public static final String PROPERTY_USER_NAME_JAVA_REG = "UserNameJavaRegEx";
        public static final String PROPERTY_USER_NAME_JS_REG_EX = "UsernameJavaScriptRegEx";
        public static final String PROPERTY_USER_NAME_JS_REG = "UserNameJavaScriptRegEx";
        public static final String PROPERTY_USER_NAME_WITH_EMAIL_JS_REG_EX = "UsernameWithEmailJavaScriptRegEx";

        public static final String PROPERTY_ROLE_NAME_JAVA_REG_EX = "RolenameJavaRegEx";
        public static final String PROPERTY_ROLE_NAME_JS_REG_EX = "RolenameJavaScriptRegEx";

        public static final String PROPERTY_EXTERNAL_IDP = "ExternalIdP";

        public static final String PROPERTY_KDC_ENABLED = "kdcEnabled";
        public static final String DEFAULT_REALM_NAME = "defaultRealmName";

        public static final String PROPERTY_SCIM_ENABLED = "SCIMEnabled";

        /*configuration to enable or disable user role caching*/
        public static final String PROPERTY_ROLES_CACHE_ENABLED = "UserRolesCacheEnabled";

        //configuration to enable or disable authorization caching
        public static final String PROPERTY_AUTHORIZATION_CACHE_ENABLED = "AuthorizationCacheEnabled";

        public static final String PROPERTY_CASE_SENSITIVITY = "CaseSensitiveAuthorizationRules";

        //configuration to identify the cache uniquely
        public static final String PROPERTY_USER_CORE_CACHE_IDENTIFIER = "UserCoreCacheIdentifier";

        //configuration to identify the cache uniquely
        public static final String PROPERTY_USER_ROLE_CACHE_TIME_OUT = "UserCoreCacheTimeOut";

        /*configuration to replace escape characters in user name at user login*/
        public static final String PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN = "ReplaceEscapeCharactersAtUserLogin";

        public static final String PASSWORD_HASH_METHOD_PLAIN_TEXT = "PLAIN_TEXT";

        public static final String PROPERTY_DOMAIN_NAME = "DomainName";

        //Internal Properties
        public static final String STATIC_USER_STORE = "StaticUserStore";

        public static final String PROPERTY_GROUP_SEARCH_DOMAINS = "GroupSearchDomains";

        public static final String LOCAL_NAME_MULTIPLE_CREDENTIALS = "MultipleCredentials";
        public static final String LOCAL_NAME_CREDENTIAL = "Credential";
        public static final String ATTR_NAME_TYPE = "type";

        public static final String SHARED_GROUPS_ENABLED = "SharedGroupEnabled";
        public static final String DOMAIN_NAME_XPATH = "//UserStoreManager/Property[@name='DomainName']";
        public static final String LEADING_OR_TRAILING_SPACE_ALLOWED_IN_USERNAME =
                "LeadingOrTrailingSpaceAllowedInUserName";
    }

    public static final class ClaimTypeURIs {
        public static final String GIVEN_NAME = DEFAULT_CARBON_DIALECT + "/givenname";
        public static final String EMAIL_ADDRESS = DEFAULT_CARBON_DIALECT + "/emailaddress";
        public static final String SURNAME = DEFAULT_CARBON_DIALECT + "/lastname";
        public static final String STREET_ADDRESS = DEFAULT_CARBON_DIALECT + "/streetaddress";
        public static final String LOCALITY = DEFAULT_CARBON_DIALECT + "/locality";
        public static final String REGION = DEFAULT_CARBON_DIALECT + "/region";
        public static final String POSTAL_CODE = DEFAULT_CARBON_DIALECT + "/postalcode";
        public static final String COUNTRY = DEFAULT_CARBON_DIALECT + "/country";
        public static final String HONE = DEFAULT_CARBON_DIALECT + "/telephone";
        public static final String IM = DEFAULT_CARBON_DIALECT + "/im";
        public static final String ORGANIZATION = DEFAULT_CARBON_DIALECT + "/organization";
        public static final String URL = DEFAULT_CARBON_DIALECT + "/url";
        public static final String TITLE = DEFAULT_CARBON_DIALECT + "/title";
        public static final String ROLE = DEFAULT_CARBON_DIALECT + "/role";
        public static final String MOBILE = DEFAULT_CARBON_DIALECT + "/mobile";
        public static final String NICKNAME = DEFAULT_CARBON_DIALECT + "/nickname";
        public static final String DATE_OF_BIRTH = DEFAULT_CARBON_DIALECT + "/dob";
        public static final String GENDER = DEFAULT_CARBON_DIALECT + "/gender";
        public static final String IDENTITY_CLAIM_URI = DEFAULT_CARBON_DIALECT + "/identity";
        public static final String ACCOUNT_STATUS = IDENTITY_CLAIM_URI + "/accountLock";
        public static final String CHALLENGE_QUESTION_URI = IDENTITY_CLAIM_URI + "/challengeQuestion";
        public static final String TEMPORARY_EMAIL_ADDRESS = DEFAULT_CARBON_DIALECT + "/temporaryemailaddress";
    }

    public static final class TenantMgtConfig {
        public static final String LOCAL_NAME_TENANT_MANAGER = "TenantManager";
        public static final String ATTRIBUTE_NAME_CLASS = "class";
        public static final String LOCAL_NAME_PROPERTY = "Property";
        public static final String ATTR_NAME_PROPERTY_NAME = "name";
        public static final String PROPERTY_ROOT_PARTITION = "RootPartition";
        public static final String PROPERTY_ORGANIZATIONAL_OBJECT_CLASS = "OrganizationalObjectClass";
        public static final String PROPERTY_ORGANIZATIONAL_ATTRIBUTE = "OrganizationalAttribute";
        public static final String PROPERTY_ORG_SUB_CONTEXT_OBJ_CLASS =
                "OrganizationalSubContextObjectClass";
        public static final String PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE =
                "OrganizationalSubContextAttribute";
        public static final String PROPERTY_ORG_SUB_CONTEXT_USER_CONTEXT_VALUE =
                "OrganizationalSubContextUserContextName";
        public static final String PROPERTY_ORG_SUB_CONTEXT_GROUP_CONTEXT_VALUE =
                "OrganizationalSubContextGroupContextName";
        public static final String PROPERTY_MULTI_TENANT_REALM_CONFIG_BUILDER =
                "MultiTenantRealmConfigBuilder";

    }

    public static final class ErrorCode {

        public static final String USER_DOES_NOT_EXIST = "17001";
        public static final String INVALID_CREDENTIAL = "17002";
        public static final String USER_IS_LOCKED = "17003";

    }

    /**
     * Constants from org.wso2.carbon.caching.impl.CachingConstants
     */
    public final class CachingConstants {

        public static final int DEFAULT_CACHE_CAPACITY = 10000;

        public static final double CACHE_EVICTION_FACTOR = 0.25;
        public static final long MAX_CACHE_IDLE_TIME_MILLIS = 15 * 60 * 1000; // 15mins

        public static final long DEFAULT_CACHE_EXPIRY_MINS = 15;

        // Cache name prefix of local cache
        public static final String LOCAL_CACHE_PREFIX = "$__local__$.";

        // Cache name prefix of clear all
        public static final String CLEAR_ALL_PREFIX = "$__clear__all__$.";

        // Cache name prefix of Time Stamp cache
        public static final String TIMESTAMP_CACHE_PREFIX = "$_timestamp_$";
        public static final String FORCE_LOCAL_CACHE = "Cache.ForceLocalCache";
        //Keep or discard empty cache objects
        public static final String DISCARD_EMPTY_CACHES = "Cache.DiscardEmptyCaches";

        public static final String ILLEGAL_STATE_EXCEPTION_MESSAGE = "The cache status is not STARTED";

        private CachingConstants() {
        }
    }
}
