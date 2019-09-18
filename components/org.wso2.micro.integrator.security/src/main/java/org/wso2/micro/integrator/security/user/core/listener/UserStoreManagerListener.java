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
package org.wso2.micro.integrator.security.user.core.listener;

import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.UserStoreManager;

import java.util.Map;

public interface UserStoreManagerListener {

    /**
     * The execution order of identity
     */
    static final int IDENTITY_UM_LISTENER_EXECUTION_ORDER_ID = 50;

    // 100 - 150 for registry

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Given the user name and a credential object, the implementation code must
     * validate whether the user is authenticated.
     *
     * @param userName         The user name
     * @param credential       The credential of a user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean authenticate(String userName, Object credential,
                         UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Add a user to the user store.
     *
     * @param userName         User name of the user
     * @param credential       The credential/password of the user
     * @param roleList         The roles that user belongs
     * @param claims           Properties of the user
     * @param profileName      The name of the profile
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean addUser(String userName, Object credential, String[] roleList,
                    Map<String, String> claims, String profileName,
                    UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Update the credential/password of the user
     *
     * @param userName         The user name
     * @param newCredential    The new credential/password
     * @param oldCredential    The old credential/password
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean updateCredential(String userName, Object newCredential, Object oldCredential,
                             UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Update credential/password by the admin of another user
     *
     * @param userName         The user name
     * @param newCredential    The new credential
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean updateCredentialByAdmin(String userName, Object newCredential,
                                    UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Delete the user with the given user name
     *
     * @param userName         The user name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean deleteUser(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Update the role name of given role
     *
     * @param roleName
     * @param newRoleName
     * @throws UserStoreException
     */
    boolean updateRoleName(String roleName, String newRoleName) throws UserStoreException;


}
