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
package org.wso2.micro.integrator.security.user.core.ldap;

public class LDAPConstants {
    public static final String DRIVER_NAME = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String CONNECTION_URL = "ConnectionURL";
    public static final String CONNECTION_NAME = "ConnectionName";
    public static final String CONNECTION_PASSWORD = "ConnectionPassword";
    public static final String USER_SEARCH_BASE = "UserSearchBase";
    public static final String GROUP_SEARCH_BASE = "GroupSearchBase";
    public static final String USER_NAME_LIST_FILTER = "UserNameListFilter";
    public static final String USER_NAME_ATTRIBUTE = "UserNameAttribute";
    public static final String DISPLAY_NAME_ATTRIBUTE = "DisplayNameAttribute";
    public static final String DEFAULT_TENANT_USER_FILTER = "DefaultTenantUserFilter";
    public static final String USER_DN_PATTERN = "UserDNPattern";
    public static final String ROLE_DN_PATTERN = "RoleDNPattern";
    public static final String LDAP_INITIAL_CONTEXT_FACTORY = "LDAPInitialContextFactory";
    //Property that defines the status of the referral to be used:
    public static final String PROPERTY_REFERRAL = "Referral";

    public static final String LDAP_ATTRIBUTES_BINARY = "java.naming.ldap.attributes.binary";
    public static final String PRIMARY_GROUP_ID = "PrimaryGroupId";

    //filter attribute in user-mgt.xml that filters users by user name
    public static final String USER_NAME_SEARCH_FILTER = "UserNameSearchFilter";
    //this property indicates which object class should be used for user entries in LDAP
    public static final String USER_ENTRY_OBJECT_CLASS = "UserEntryObjectClass";
    // roles
    public static final String GROUP_NAME_LIST_FILTER = "GroupNameListFilter";
    public static final String SHARED_GROUP_NAME_LIST_FILTER = GROUP_NAME_LIST_FILTER; // "SharedGroupNameListFilter";
    public static final String ROLE_NAME_FILTER = "GroupNameSearchFilter";
    public static final String SHARED_GROUP_NAME_SEARCH_FILTER = ROLE_NAME_FILTER; // "SharedGroupNameSearchFilter";
    public static final String GROUP_NAME_ATTRIBUTE = "GroupNameAttribute";
    public static final String SHARED_GROUP_NAME_ATTRIBUTE = GROUP_NAME_ATTRIBUTE; // "SharedGroupNameAttribute";
    @Deprecated
    public static final String READ_LDAP_GROUPS = "ReadLDAPGroups";
    @Deprecated
    public static final String WRITE_EXTERNAL_ROLES = "WriteLDAPGroups";
    public static final String MEMBEROF_ATTRIBUTE = "MemberOfAttribute";
    public static final String MEMBERSHIP_ATTRIBUTE = "MembershipAttribute";
    public static final String EMPTY_ROLES_ALLOWED = "EmptyRolesAllowed";
    public static final String BACK_LINKS_ENABLED = "BackLinksEnabled";
    //ldap glossary
    public static final String OBJECT_CLASS_NAME = "objectClass";
    public static final String GROUP_ENTRY_OBJECT_CLASS = "GroupEntryObjectClass";
    // ldap glossary for shared group concept
    public static final String SHARED_GROUP_ENTRY_OBJECT_CLASS = GROUP_ENTRY_OBJECT_CLASS; // "SharedGroupEntryObjectClass";
    public static final String ADMIN_ENTRY_NAME = "admin";
    public static final String SHARED_GROUP_SEARCH_BASE = "SharedGroupSearchBase";
    //used in tenant management
    public static final String USER_CONTEXT_NAME = "users";
    public static final String GROUP_CONTEXT_NAME = "groups";

    //password
    public static final String PASSWORD_HASH_METHOD = "PasswordHashMethod";

    // Active Directory specific constants
    public static final String ACTIVE_DIRECTORY_LDS_ROLE = "isADLDSRole";
    public static final String ACTIVE_DIRECTORY_USER_ACCOUNT_CONTROL = "userAccountControl";
    public static final String ACTIVE_DIRECTORY_MSDS_USER_ACCOUNT_DISSABLED = "msDS-UserAccountDisabled";
    public static final String ACTIVE_DIRECTORY_UNICODE_PASSWORD_ATTRIBUTE = "unicodePwd";
    public static final String ACTIVE_DIRECTORY_DISABLED_NORMAL_ACCOUNT = Integer.toString(514);

    //KDC specific constants
    public static final String SERVER_PRINCIPAL_ATTRIBUTE_VALUE = "Service";

    //DNS related constants
    public static final String ACTIVE_DIRECTORY_DOMAIN_CONTROLLER_SERVICE = "_ldap._tcp.";
    public static final String SRV_ATTRIBUTE_NAME = "SRV";
    public static final String A_RECORD_ATTRIBUTE_NAME = "A";

    public static final String DNS_URL = "URLOfDNS";
    public static final String DNS_DOMAIN_NAME = "DNSDomainName";
    public static final String SHARED_TENANT_NAME_LIST_FILTER = "SharedTenantNameListFilter";
    public static final String SHARED_TENANT_NAME_ATTRIBUTE = "SharedTenantNameAttribute";
    public static final String SHARED_TENANT_OBJECT_CLASS = "SharedTenantObjectClass";

    public static final String CONNECTION_POOLING_ENABLED = "ConnectionPoolingEnabled";
    public static final String USER_CACHE_EXPIRY_MILLISECONDS = "UserCacheExpiryMilliseconds";
    public static final String USER_DN_CACHE_ENABLED = "UserDNCacheEnabled";

}
