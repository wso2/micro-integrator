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

import org.wso2.micro.integrator.security.user.api.AuthorizationManager;
import org.wso2.micro.integrator.security.user.api.ClaimManager;
import org.wso2.micro.integrator.security.user.api.ProfileConfigurationManager;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;

/**
 * The Realm of the WSO2 user Kernel.
 * <p/>
 * The realm represents a user store. This is a collection of interfaces.
 * <p/>
 * To enable WSO2 platform with a custom realm, implement this interface and
 * add the class to the class path. Provide the class name in the configuration
 * file and the framework will pick the new realm code.
 */
public interface UserRealm {

    /**
     * Get the AuthorizationReader of the system
     *
     * @return The AuthorizationReader the system
     * @throws UserStoreException
     */
    AuthorizationManager getAuthorizationManager() throws UserStoreException;

    /**
     * Get the UserStoreManager of the system
     *
     * @return The UserStoreManager of the system
     * @throws UserStoreException
     */
    UserStoreManager getUserStoreManager() throws UserStoreException;

    /**
     * Get the ClaimManager of the system
     *
     * @return The ClaimManager of the system
     * @throws UserStoreException
     */
    ClaimManager getClaimManager() throws UserStoreException;

    /**
     * Get the ProfileConfigurationManager of the system
     *
     * @return The ProfileConfigurationManager of the system
     * @throws UserStoreException
     */
    ProfileConfigurationManager getProfileConfigurationManager() throws UserStoreException;

    /**
     * Get the realm configuration
     *
     * @return
     * @throws UserStoreException
     */
    RealmConfiguration getRealmConfiguration() throws UserStoreException;

}
