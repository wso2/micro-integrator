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
package org.wso2.micro.integrator.security.user.core.config.multitenancy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.TenantMgtConfiguration;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.ldap.LDAPConstants;
import org.wso2.micro.integrator.security.user.core.tenant.Tenant;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.util.Map;

/**
 * This is to create tenant specific realm configuration when
 * org.wso2.micro.integrator.security.user.core.tenant.FileSystemRealmConfigBuilder is used as the tenant manager
 * which supports any external ldap server.
 */

public class FileSystemRealmConfigBuilder implements MultiTenantRealmConfigBuilder {

    private static Log logger = LogFactory.getLog(FileSystemRealmConfigBuilder.class);

    public RealmConfiguration getRealmConfigForTenantToCreateRealm(
            RealmConfiguration bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException {
        RealmConfiguration realmConfig;
        //clone the bootstrap realm and insert tenant specific properties taken from tenant's user-mgt.xml
        try {
            realmConfig = bootStrapConfig.cloneRealmConfiguration();
            realmConfig.setAdminPassword(persistedConfig.getAdminPassword());
            realmConfig.setAdminUserName(persistedConfig.getAdminUserName());
            realmConfig.setAdminRoleName(persistedConfig.getAdminRoleName());
            realmConfig.setEveryOneRoleName(persistedConfig.getEveryOneRoleName());
            realmConfig.setTenantId(persistedConfig.getTenantId());

            Map<String, String> authz = realmConfig.getAuthzProperties();
            authz.put(UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION,
                    UserCoreConstants.UI_ADMIN_PERMISSION_COLLECTION);

            if (persistedConfig.getUserStoreProperties().get(LDAPConstants.USER_SEARCH_BASE) != null) {
                realmConfig.getUserStoreProperties().put(
                        LDAPConstants.USER_SEARCH_BASE,
                        persistedConfig.getUserStoreProperties().get(LDAPConstants.USER_SEARCH_BASE));
            }
            if (persistedConfig.getUserStoreProperties().get(LDAPConstants.GROUP_SEARCH_BASE) != null) {
                realmConfig.getUserStoreProperties().put(
                        LDAPConstants.GROUP_SEARCH_BASE,
                        persistedConfig.getUserStoreProperties().get(LDAPConstants.GROUP_SEARCH_BASE));
            }
            if (persistedConfig.getUserStoreProperties().get(LDAPConstants.USER_DN_PATTERN) != null) {
                realmConfig.getUserStoreProperties().put(
                        LDAPConstants.USER_DN_PATTERN,
                        persistedConfig.getUserStoreProperties().get(LDAPConstants.USER_DN_PATTERN));
            }

            //Tenant not allowed to use secondary user stores of bootstrap realm configuration
            realmConfig.setSecondaryRealmConfig(null);

            //If there exist tenant defined user stores,then add them as secondary
            if (persistedConfig.getSecondaryRealmConfig() != null) {
                realmConfig.setSecondaryRealmConfig(persistedConfig.getSecondaryRealmConfig());
            }

        } catch (Exception e) {
            String errorMessage = "Error while building tenant specific realm configuration" +
                    "when creating tenant's realm.";
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
        return realmConfig;
    }

    public RealmConfiguration getRealmConfigForTenantToPersist(RealmConfiguration bootStrapConfig,
                                                               TenantMgtConfiguration tenantMgtConfig,
                                                               Tenant tenantInfo, int tenantId)
            throws UserStoreException {

        try {
            RealmConfiguration ldapRealmConfig = bootStrapConfig.cloneRealmConfiguration();
            ldapRealmConfig.setAdminPassword(UserCoreUtil.getDummyPassword());
            ldapRealmConfig.setAdminUserName(tenantInfo.getAdminName());
            ldapRealmConfig.setTenantId(tenantId);

            //remove non-tenant specific info from tenant-specific user-mgt.xml before persisting.
            removePropertiesFromTenantRealmConfig(ldapRealmConfig);

            //remove domain name(if exist) from admin role name
            String adminRoleName = ldapRealmConfig.getAdminRoleName();
            ldapRealmConfig.setAdminRoleName(UserCoreUtil.removeDomainFromName(adminRoleName));

            Map<String, String> userStoreProperties = ldapRealmConfig.getUserStoreProperties();

            String partitionDN = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
            String organizationName = tenantInfo.getDomain();
            //eg: o=cse.rog
            String organizationRDN = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE) + "=" +
                    organizationName;
            //eg: ou=users
            String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            String userContextRDNValue = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_USER_CONTEXT_VALUE);
            if (userContextRDNValue == null) {
                //if property is not set use default value
                userContextRDNValue = LDAPConstants.USER_CONTEXT_NAME;
            }
            String userContextRDN = orgSubContextAttribute + "=" + userContextRDNValue;
            //eg: ou=users,o=cse.org, dc=cloud, dc=com
            String userSearchBase = userContextRDN + "," + organizationRDN + "," +
                    partitionDN;
            //replace the tenant specific user search base.
            userStoreProperties.put(LDAPConstants.USER_SEARCH_BASE, userSearchBase);

            //if read ldap group is enabled, set the tenant specific group search base
            if ("true".equals(bootStrapConfig.getUserStoreProperty(LDAPConstants.READ_LDAP_GROUPS))) {
                //eg: ou=groups
                String groupContextRDNValue = tenantMgtConfig.getTenantStoreProperties().get(
                        UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_GROUP_CONTEXT_VALUE);
                if (groupContextRDNValue == null) {
                    //if property is not set use default value
                    groupContextRDNValue = LDAPConstants.GROUP_CONTEXT_NAME;
                }
                String groupContextRDN = orgSubContextAttribute + "=" + groupContextRDNValue;
                //eg: ou=users,o=cse.org, dc=cloud, dc=com
                String groupSearchBase = groupContextRDN + "," + organizationRDN + "," + partitionDN;

                userStoreProperties.put(LDAPConstants.GROUP_SEARCH_BASE, groupSearchBase);
            }

            //if UserDNPattern is mentioned, replace it to align with tenant's user store.
            if (bootStrapConfig.getUserStoreProperties().containsKey(LDAPConstants.USER_DN_PATTERN)) {
                //get userDN pattern from super tenant realm config
                String userDNPattern = bootStrapConfig.getUserStoreProperties().get(
                        LDAPConstants.USER_DN_PATTERN);
                //obtain the identifier - eg: uid={0}
                String userIdentifier = userDNPattern.split(",")[0];
                //build tenant specific one - eg:uid={0},ou=Users,ou=cse.org,dc=wso2,dc=org
                String tenantUserDNPattern = userIdentifier + "," + userSearchBase;
                userStoreProperties.put(LDAPConstants.USER_DN_PATTERN, tenantUserDNPattern);
            }

            return ldapRealmConfig;

        } catch (Exception e) {
            String errorMessage = "Error while building tenant specific realm configuration " +
                    "to be persisted.";
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
    }

    public RealmConfiguration getRealmConfigForTenantToCreateRealmOnTenantCreation(
            RealmConfiguration bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException {

        return persistedConfig;
    }

    private void removePropertiesFromTenantRealmConfig(
            RealmConfiguration tenantRealmConfiguration) {
        //remove sensitive information from realm properties before persisting
        // tenant specific user-mgt.xml
        tenantRealmConfiguration.getRealmProperties().clear();

        //remove sensitive information from user store properties before persisting
        //tenant specific user-mgt.xml
        //but keep the tenant manager property
        String tenantManagerKey = UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER;
        String tenantManagerValue = tenantRealmConfiguration.getUserStoreProperty(tenantManagerKey);
        tenantRealmConfiguration.getUserStoreProperties().clear();
        tenantRealmConfiguration.getUserStoreProperties().put(tenantManagerKey, tenantManagerValue);
    }


}
