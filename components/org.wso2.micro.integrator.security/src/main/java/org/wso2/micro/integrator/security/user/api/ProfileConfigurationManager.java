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

import org.wso2.micro.integrator.security.user.api.ProfileConfiguration;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

/**
 * This is the interface to manage profiles in the system.
 * <p/>
 * A profile contains a set of claims.
 */
public interface ProfileConfigurationManager {

    /**
     * Gets the profile configuration given the profile name.
     *
     * @param profileName
     * @return
     * @throws UserStoreException
     */
    ProfileConfiguration getProfileConfig(String profileName) throws UserStoreException;

    /**
     * Adds a profile configuration
     *
     * @param profileConfig
     * @throws UserStoreException
     */
    void addProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException;

    /**
     * Updates a profile configuration
     *
     * @param profileConfig
     * @throws UserStoreException
     */
    void updateProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException;

    /**
     * Deletes a profile configuration
     *
     * @param profileConfig
     * @throws UserStoreException
     */
    void deleteProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException;

    /**
     * Retrieves all profiles
     *
     * @return An array of profiles in the system
     * @throws UserStoreException
     */
    ProfileConfiguration[] getAllProfiles() throws UserStoreException;

}