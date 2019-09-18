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
package org.wso2.micro.integrator.security.user.api;

import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.TenantManager;
import org.wso2.micro.integrator.security.user.api.TenantMgtConfiguration;
import org.wso2.micro.integrator.security.user.api.UserRealm;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

/**
 * This is the OSGI service that provides the entry point to the UserRealm. This
 * service holds all UserRealms in the system.
 */
public interface UserRealmService {

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
     * Only returns if available in cache.
     *
     * @param tenantId
     * @return
     * @throws UserStoreException
     */
    UserRealm getTenantUserRealm(int tenantId) throws UserStoreException;

    /**
     * Get the boot strap realm configuration, this is always build from conf/user-mgt.xml
     *
     * @return the realm configuration
     */
    RealmConfiguration getBootstrapRealmConfiguration();

    /**
     * Set bootstrap realm configuration
     */
    void setBootstrapRealmConfiguration(RealmConfiguration realmConfiguration);

    /**
     * Get tenant mgt configuration read from tenant-mgt.xml
     */
    TenantMgtConfiguration getTenantMgtConfiguration();
}
