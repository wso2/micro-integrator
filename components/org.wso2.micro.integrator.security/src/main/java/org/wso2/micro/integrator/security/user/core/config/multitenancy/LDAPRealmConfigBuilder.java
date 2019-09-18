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

import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.TenantMgtConfiguration;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.ldap.LDAPConstants;
import org.wso2.micro.integrator.security.user.core.tenant.Tenant;

import java.util.Map;

/**
 * This creates the tenant specific realm configuration (from bootstrap realm
 * config i.e: user-mgt.xml) when LDAP is used as the user store in tenant management.
 */
public class LDAPRealmConfigBuilder implements MultiTenantRealmConfigBuilder {

    private static Log log = LogFactory.getLog(LDAPRealmConfigBuilder.class);

    public RealmConfiguration getRealmConfigForTenantToCreateRealm(RealmConfiguration
                                                                           bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException {
        return persistedConfig;
    }

    /**
     * This will clone the bootstrap realm config and creates a tenant specific realm.
     * Here the user search base of the particular tenant is changes to the tenant specific user
     * store.
     *
     * @param bootStrapConfig
     * @param tenantInfo
     * @param tenantId
     * @return
     * @throws UserStoreException
     */
    public RealmConfiguration getRealmConfigForTenantToPersist(RealmConfiguration
                                                                       bootStrapConfig, TenantMgtConfiguration tenantMgtConfiguration, Tenant tenantInfo,
                                                               int tenantId)
            throws UserStoreException {
        RealmConfiguration ldapRealmConfig = null;
        try {
            ldapRealmConfig = bootStrapConfig.cloneRealmConfiguration();
            //TODO: Random password generation. 
            ldapRealmConfig.setAdminPassword(UIDGenerator.generateUID());
            ldapRealmConfig.setAdminUserName(tenantInfo.getAdminName());
            ldapRealmConfig.setTenantId(tenantId);

            Map<String, String> authz = ldapRealmConfig.getAuthzProperties();
            authz.put(UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION,
                    UserCoreConstants.UI_ADMIN_PERMISSION_COLLECTION);

            Map<String, String> userStoreProperties = ldapRealmConfig.getUserStoreProperties();

            //If the domain is wso2.com, partition dn is composed as dc=wso2,dc=com as follows:
            String partitionDN = "dc=" + tenantInfo.getDomain().split("\\.")[0] + ",dc=" +
                    tenantInfo.getDomain().split("\\.")[1];
            /*according to carbon/components/apacheds-server, users are stored under "ou=Users"
            context. So that is hard coded as the default in userSearchBase.*/
            String userSearchBase = "ou=Users," + partitionDN;
            //replace the tenant specific user search base.
            userStoreProperties.put(LDAPConstants.USER_SEARCH_BASE, userSearchBase);

            return ldapRealmConfig;

        } catch (Exception e) {
            String errorMessage = "Tenant specific realm config could not be created.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }

    }

    public RealmConfiguration getRealmConfigForTenantToCreateRealmOnTenantCreation(
            RealmConfiguration bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException {
        return persistedConfig;
    }
}
