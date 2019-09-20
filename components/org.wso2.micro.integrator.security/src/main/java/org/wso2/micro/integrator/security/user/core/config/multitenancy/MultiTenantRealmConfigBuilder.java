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

import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.TenantMgtConfiguration;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.tenant.Tenant;

/**
 * An implementation of this class should take care of building the realm
 * configuration.
 */
public interface MultiTenantRealmConfigBuilder {

    RealmConfiguration getRealmConfigForTenantToCreateRealm(RealmConfiguration bootStrapConfig,
                                                            RealmConfiguration persistedConfig, int tenantId) throws UserStoreException;

    /**
     * Clone the user-mgt.xml and edit necessary parameters to make it tenant-specific.
     *
     * @param bootStrapConfig        - representation of user-mgt.xml
     * @param tenantMgtConfiguration - representation of tenant-mgt.xml
     * @param tenantInfo
     * @param tenantId
     * @return
     * @throws UserStoreException
     */
    RealmConfiguration getRealmConfigForTenantToPersist(RealmConfiguration bootStrapConfig,
                                                        TenantMgtConfiguration tenantMgtConfiguration,
                                                        Tenant tenantInfo, int tenantId)
            throws UserStoreException;

    @Deprecated
    RealmConfiguration getRealmConfigForTenantToCreateRealmOnTenantCreation(
            RealmConfiguration bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException;

}