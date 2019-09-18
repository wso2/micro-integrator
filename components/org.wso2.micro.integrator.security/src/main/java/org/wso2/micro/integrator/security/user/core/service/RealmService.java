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
package org.wso2.micro.integrator.security.user.core.service;

import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.micro.integrator.security.user.core.tenant.TenantManager;

public interface RealmService extends org.wso2.micro.integrator.security.user.api.UserRealmService {
    /**
     * Get a user realm from a given configuration
     *
     * @param tenantRealmConfig
     * @return UserRealm
     * @throws UserStoreException
     */
    UserRealm getUserRealm(RealmConfiguration tenantRealmConfig) throws UserStoreException;

    /**
     * Get the boot strap realm configuration, this is always build from conf/user-mgt.xml
     *
     * @return tge realm configuration
     */
    RealmConfiguration getBootstrapRealmConfiguration();

    /**
     * Get the bootstrap realm
     *
     * @return
     * @throws UserStoreException
     */
    UserRealm getBootstrapRealm() throws UserStoreException;

    /**
     * Get tenant manager
     *
     * @return TenantManager
     */
    TenantManager getTenantManager();

    /**
     * @param tenantManager
     * @throws UserStoreException
     */
    void setTenantManager(TenantManager tenantManager) throws UserStoreException;

    /**
     * @return
     * @throws Exception
     */
    MultiTenantRealmConfigBuilder getMultiTenantRealmConfigBuilder() throws UserStoreException;

    /**
     * Only returns if available in cache.
     *
     * @param tenantId
     * @return
     * @throws UserStoreException
     */
    UserRealm getCachedUserRealm(int tenantId) throws UserStoreException;

    /**
     * Invalidate the realm in cache
     *
     * @param tenantId
     * @throws UserStoreException
     */
    void clearCachedUserRealm(int tenantId) throws UserStoreException;
}
