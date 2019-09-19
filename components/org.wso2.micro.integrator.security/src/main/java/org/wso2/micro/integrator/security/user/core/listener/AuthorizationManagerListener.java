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

import org.wso2.micro.integrator.security.user.core.AuthorizationManager;
import org.wso2.micro.integrator.security.user.core.UserStoreException;

public interface AuthorizationManagerListener {

    static final int REGISTRY_AUTH_ROLE_LISTENER = 20;
    /**
     * execution id of the multi-tenancy restriction handler
     */
    static final int MULTITENANCY_USER_RESTRICTION_HANDLER = 10;
    /**
     * execution id of the permission authorization listener
     */
    static final int PERMISSION_AUTHORIZATION_LISTENER = 5;

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Checks for user authorization.
     *
     * @param userName             The user name
     * @param resourceId           Resource identification string
     * @param action               The action granted to the user
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean isUserAuthorized(String userName, String resourceId, String action,
                             AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Checks for role authorization.
     *
     * @param roleName             The role name
     * @param resourceId           Resource identification string
     * @param action               The action granted to the user
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean isRoleAuthorized(String roleName, String resourceId, String action,
                             AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Grants authorizations to a user to perform an action on a resource.
     *
     * @param userName             The user name
     * @param resourceId           Resource identification string
     * @param action               The action granted to the user
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean authorizeUser(String userName, String resourceId, String action,
                          AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Grants authorizes to a role to perform an action on a resource.
     *
     * @param roleName             The role name
     * @param resourceId           Resource identification string
     * @param action               The action granted to the role
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean authorizeRole(String roleName, String resourceId, String action,
                          AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Deny authorizations to a user to perform an action on a resource.
     *
     * @param userName             The user name
     * @param resourceId           Resource identification string
     * @param action               The action granted to the user
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean denyUser(String userName, String resourceId, String action,
                     AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Deny authorizations to a role to perform an action on a resource.
     *
     * @param roleName             The role name
     * @param resourceId           Resource identification string
     * @param action               The action granted to the role
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean denyRole(String roleName, String resourceId, String action,
                     AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Deletes an already granted authorization of a user.
     *
     * @param userName             The user name
     * @param resourceId           Resource identification string
     * @param action               The action granted
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean clearUserAuthorization(String userName, String resourceId, String action,
                                   AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * @param userName             the user name
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean clearUserAuthorization(String userName,
                                   AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Deletes an already granted authorization of a role.
     *
     * @param roleName             The role name
     * @param resourceId           Resource identification string
     * @param action               The action granted
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean clearRoleAuthorization(String roleName, String resourceId, String action,
                                   AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Deletes the role's right to perform the action on all resources.
     *
     * @param roleName             The role name
     * @param action               The action granted
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean clearRoleActionOnAllResources(String roleName, String action,
                                          AuthorizationManager authorizationManager)
            throws UserStoreException;

    /**
     * Used when deleting roles.
     *
     * @param roleName             The role name
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean clearRoleAuthorization(String roleName,
                                   AuthorizationManager authorizationManager) throws UserStoreException;

    /**
     * Deletes all granted authorization on a resource.
     *
     * @param resourceId           Resource identification string
     * @param authorizationManager The underlying AuthorizationManager
     * @return Whether execution of this method of the underlying
     * UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    boolean clearResourceAuthorizations(String resourceId,
                                        AuthorizationManager authorizationManager)
            throws UserStoreException;

    /**
     * this will reset the permission of the renamed role
     *
     * @param roleName
     * @param newRoleName
     * @throws UserStoreException
     */
    boolean resetPermissionOnUpdateRole(String roleName, String newRoleName,
                                        AuthorizationManager authorizationManager)
            throws UserStoreException;
}
