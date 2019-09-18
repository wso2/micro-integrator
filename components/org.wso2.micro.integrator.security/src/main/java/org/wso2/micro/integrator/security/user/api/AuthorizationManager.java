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

import org.wso2.micro.integrator.security.user.api.UserStoreException;

public interface AuthorizationManager {

    /**
     * Checks for user authorization. Users do not have permissions in future.
     * Only roles can have permissions
     *
     * @param userName   The user name
     * @param resourceId Resource Id String
     * @param action     The action user is trying to perform
     * @return Returns true when user is authorized to perform the action on the
     * resource and false otherwise.
     * @throws UserStoreException
     */
    boolean isUserAuthorized(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Checks for role authorization.
     *
     * @param roleName   The role name
     * @param resourceId Resource Id String
     * @param action     The action the role is trying to perform
     * @return Returns true when the role is authorized to perform the action on
     * the resource and false otherwise
     * @throws UserStoreException
     */
    boolean isRoleAuthorized(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Retrieves a list of users allowed to perform the given action on the
     * resource
     *
     * @param resourceId Resource Id String
     * @param action     The action that is allowed to perform
     * @return Returns a list of users allowed to perform the given action on
     * the resource
     * @throws UserStoreException
     * @deprecated
     */
    String[] getExplicitlyAllowedUsersForResource(String resourceId, String action)
            throws UserStoreException;

    /**
     * Retrieves a list of roles allowed to perform the given action on the
     * resource
     *
     * @param resourceId Resource Id String
     * @param action     The action that is allowed to perform
     * @return Returns a list of roles allowed to perform the given action on
     * the resource
     * @throws UserStoreException
     */
    String[] getAllowedRolesForResource(String resourceId, String action)
            throws UserStoreException;

    /**
     * Retrieves a list of roles that are not allowed to perform the given
     * action on the resource
     *
     * @param resourceId Resource Id String
     * @param action     The action that is allowed to perform
     * @return Returns a list of roles allowed to perform the given action on
     * the resource
     * @throws UserStoreException
     */
    String[] getDeniedRolesForResource(String resourceId, String action)
            throws UserStoreException;

    /**
     * Retrieves a list of users explicitly denied access to a resource. Users
     * do not have permissions in future. Only roles can have permissions
     *
     * @param resourceId Resource Id String
     * @param action     The action that is disallowed to perform
     * @return
     * @throws UserStoreException
     * @deprecated
     */
    String[] getExplicitlyDeniedUsersForResource(String resourceId, String action)
            throws UserStoreException;

    /**
     * Grants authorizations to a user to perform an action on a resource. Users
     * do not have permissions in future. Only roles can have permissions
     *
     * @param userName   The user name
     * @param resourceId Resource identification string
     * @param action     The action granted to the user
     * @throws UserStoreException
     * @deprecated
     */
    void authorizeUser(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Grants authorizes to a role to perform an action on a resource.
     *
     * @param roleName   The role name
     * @param resourceId Resource identification string
     * @param action     The action granted to the role
     * @throws UserStoreException
     */
    void authorizeRole(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deny authorizations to a user to perform an action on a resource. Users
     * do not have permissions in future. Only roles can have permissions
     *
     * @param userName   The user name
     * @param resourceId Resource identification string
     * @param action     The action granted to the user
     * @throws UserStoreException
     * @deprecated
     */
    void denyUser(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deny authorizations to a role to perform an action on a resource.
     *
     * @param roleName   The role name
     * @param resourceId Resource identification string
     * @param action     The action granted to the role
     * @throws UserStoreException
     */
    void denyRole(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deletes an already granted authorization of a user. Users do not have
     * permissions in future. Only roles can have permissions
     *
     * @param userName   The user name
     * @param resourceId Resource identification string
     * @param action     The action granted
     * @throws UserStoreException
     * @deprecated
     */
    void clearUserAuthorization(String userName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Users do not have permissions in future. Only roles can have permissions
     *
     * @param userName The user name
     * @throws UserStoreException
     * @deprecated
     */
    void clearUserAuthorization(String userName) throws UserStoreException;

    /**
     * Deletes an already granted authorization of a role.
     *
     * @param roleName   The role name
     * @param resourceId Resource identification string
     * @param action     The action granted
     * @throws UserStoreException
     */
    void clearRoleAuthorization(String roleName, String resourceId, String action)
            throws UserStoreException;

    /**
     * Deletes the role's right to perform the action on all resources.
     *
     * @param roleName The role name
     * @param action   The action granted
     * @throws UserStoreException
     */
    void clearRoleActionOnAllResources(String roleName, String action)
            throws UserStoreException;

    /**
     * Used when deleting roles.
     *
     * @param roleName
     * @throws UserStoreException
     */
    void clearRoleAuthorization(String roleName) throws UserStoreException;

    /**
     * Deletes all granted authorization on a resource.
     *
     * @param resourceId Resource identification string
     * @throws UserStoreException
     */
    void clearResourceAuthorizations(String resourceId) throws UserStoreException;

    /**
     * Returns the complete set of UI resources allowed for User.
     *
     * @param userName
     * @return
     * @throws UserStoreException
     */
    String[] getAllowedUIResourcesForUser(String userName, String permissionRootPath)
            throws UserStoreException;

    /**
     * Returns the complete set of resources allowed for Role.
     *
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    default String[] getAllowedUIResourcesForRole(String roleName, String permissionRootPath)
            throws UserStoreException {
        //This is new API addition and giving default empty implementation to give the backward compatibility.
        return new String[0];
    }

    /**
     * This will get the tenant id associated with the user authorization
     * manager
     *
     * @return the tenant id of the authorization manager
     * @throws UserStoreException if the operation failed
     */
    int getTenantId() throws UserStoreException;

    /**
     * This will reset the permission of the renamed role
     *
     * @param roleName    The role name
     * @param newRoleName The new role name
     */
    void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException;

    /**
     * This method used to refresh the existing resource permissions which cached in the memory
     *
     * @param resourceId resource id path
     * @throws UserStoreException if something went wrong
     */
    void refreshAllowedRolesForResource(String resourceId) throws UserStoreException;
}
