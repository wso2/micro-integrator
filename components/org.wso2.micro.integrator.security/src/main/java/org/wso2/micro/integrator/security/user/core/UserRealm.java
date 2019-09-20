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

import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.claim.ClaimMapping;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfiguration;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfigurationManager;

import java.util.Map;

/**
 * The Realm of the WSO2 user Kernel.
 * <p/>
 * The realm represents a user store. This is a collection of interfaces.
 * <p/>
 * To enable WSO2 platform with a clustom realm, implement this interface and
 * add the class to the class path. Provide the class name in the configuration
 * file and the framework will pick the new realm code.
 */
public interface UserRealm extends org.wso2.micro.integrator.security.user.api.UserRealm {

    /**
     * Initialize the realm. Used in tests.
     *
     * @param configBean - Configuration details of the realm
     * @throws UserStoreException
     */
    void init(RealmConfiguration configBean, Map<String, ClaimMapping> claimMapping,
              Map<String, ProfileConfiguration> profileConfigs,
              int tenantId) throws UserStoreException;

    /**
     * Initialize the realm.
     *
     * @param configBean
     * @param tenantId
     * @throws UserStoreException
     */
    void init(RealmConfiguration configBean, Map<String, Object> properties, int tenantId)
            throws UserStoreException;

    /**
     * Get the AuthorizationReader of the system
     *
     * @return The AuthorizationReader the system
     * @throws UserStoreException
     */
    AuthorizationManager getAuthorizationManager() throws UserStoreException;

    /**
     * Get the UserStoreManager of the system.
     *
     * @return The UserStoreManager of the system
     * @throws UserStoreException
     */
    UserStoreManager getUserStoreManager() throws UserStoreException;


    ClaimManager getClaimManager() throws UserStoreException;

    /**
     * Get the ProfileConfigurationManager of the system.
     *
     * @return The ProfileConfigurationManager of the system
     * @throws UserStoreException
     */
    ProfileConfigurationManager getProfileConfigurationManager() throws UserStoreException;

    /**
     * Clean up the system. Clean up dead data.
     *
     * @throws UserStoreException
     */
    void cleanUp() throws UserStoreException;

    /**
     * Get the realm configuration
     *
     * @return
     * @throws UserStoreException
     */
    RealmConfiguration getRealmConfiguration() throws UserStoreException;

}
