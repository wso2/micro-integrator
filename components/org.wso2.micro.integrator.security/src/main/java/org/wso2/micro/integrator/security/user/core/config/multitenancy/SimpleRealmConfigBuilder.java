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
import org.wso2.micro.integrator.security.user.core.tenant.Tenant;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.util.Map;

public class SimpleRealmConfigBuilder implements MultiTenantRealmConfigBuilder {

    private static Log log = LogFactory.getLog(SimpleRealmConfigBuilder.class);

    public RealmConfiguration getRealmConfigForTenantToCreateRealm(
            RealmConfiguration bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException {

        RealmConfiguration realmConfig;
        try {
            realmConfig = bootStrapConfig.cloneRealmConfigurationWithoutSecondary();
            realmConfig.setAdminUserName(persistedConfig.getAdminUserName());
            realmConfig.setAdminPassword(persistedConfig.getAdminPassword());
            realmConfig.setAdminRoleName(persistedConfig.getAdminRoleName());
            realmConfig.setEveryOneRoleName(persistedConfig.getEveryOneRoleName());
            Map<String, String> authz = realmConfig.getAuthzProperties();
            authz.put(UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION,
                    UserCoreConstants.UI_ADMIN_PERMISSION_COLLECTION);
            realmConfig.setSecondaryRealmConfig(persistedConfig.getSecondaryRealmConfig());
        } catch (Exception e) {
            String errorMessage = "Error while building tenant specific realm configuration" +
                    "when creating tenant's realm.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
        return realmConfig;
    }

    public RealmConfiguration getRealmConfigForTenantToCreateRealmOnTenantCreation(
            RealmConfiguration bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException {
        return persistedConfig;
    }

    public RealmConfiguration getRealmConfigForTenantToPersist(RealmConfiguration bootStrapConfig,
                                                               TenantMgtConfiguration tenantMgtConfig,
                                                               Tenant tenantInfo, int tenantId)
            throws UserStoreException {
        try {
            RealmConfiguration realmConfig = bootStrapConfig.cloneRealmConfigurationWithoutSecondary();
            removePropertiesFromTenantRealmConfig(realmConfig);
            realmConfig.setAdminUserName(UserCoreUtil.removeDomainFromName(tenantInfo.getAdminName()));
            realmConfig.setAdminPassword(UserCoreUtil.getDummyPassword());
            realmConfig.setTenantId(tenantId);

            realmConfig.setEveryOneRoleName(UserCoreUtil.
                    removeDomainFromName(realmConfig.getEveryOneRoleName()));

            return realmConfig;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    private void removePropertiesFromTenantRealmConfig(RealmConfiguration tenantRealmConfiguration) {
        tenantRealmConfiguration.getRealmProperties().clear();

        //remove sensitive information from user store properties before persisting
        //tenant specific user-mgt.xml
        //but keep the tenant manager property
        String tenantManagerKey = UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER;
        String tenantManagerValue = tenantRealmConfiguration.getUserStoreProperty(tenantManagerKey);
        // we are not keeping sensitive information with JDBC. Only the datasource name
        //tenantRealmConfiguration.getUserStoreProperties().clear();
        tenantRealmConfiguration.getUserStoreProperties().put(tenantManagerKey, tenantManagerValue);

        tenantRealmConfiguration.getAuthzProperties().clear();
    }

}
