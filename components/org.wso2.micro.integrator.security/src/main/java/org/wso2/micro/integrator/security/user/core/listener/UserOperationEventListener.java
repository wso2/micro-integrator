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

import org.wso2.micro.integrator.security.user.api.Permission;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.UserStoreManager;

import java.util.Map;

/**
 * This allows an extension point to implement various additional operations before and after
 * actual user operation is done.
 */
public interface UserOperationEventListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Define any additional actions before actual authentication is happen
     *
     * @param userName         User name of User
     * @param credential       Credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions after actual authentication is happen
     *
     * @param userName         User name of User
     * @param authenticated    where user is authenticated or not
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostAuthenticate(String userName, boolean authenticated,
                                      UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Define any additional actions before user is added.
     *
     * @param userName         User name of User
     * @param credential       Credential/password of the user
     * @param roleList         role list of user
     * @param claims           Properties of the user
     * @param profile          profile name of user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreAddUser(String userName, Object credential, String[] roleList,
                                Map<String, String> claims, String profile,
                                UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after user is added.
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostAddUser(String userName, Object credential, String[] roleList,
                                 Map<String, String> claims, String profile,
                                 UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by user
     *
     * @param userName         User name of User
     * @param newCredential    new credential/password of the user
     * @param oldCredential    Old credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreUpdateCredential(String userName, Object newCredential,
                                         Object oldCredential,
                                         UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by user
     *
     * @param userName         User name of User
     * @param credential
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before credential is updated by Admin
     *
     * @param userName         User name of User
     * @param newCredential    new credential/password of the user
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                                UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after credential is updated by Admin
     *
     * @param userName         User name of User
     * @param credential
     * @param userStoreManager The underlying UserStoreManager  @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */

    public boolean doPostUpdateCredentialByAdmin(String userName, Object credential,
                                                 UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before user is deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user is deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is set by Admin
     *
     * @param userName         User name of User
     * @param claimURI         claim uri
     * @param claimValue       claim value
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue,
                                          String profileName, UserStoreManager userStoreManager)
            throws UserStoreException;


    /**
     * Defines any additional actions after user attribute is set by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are set by Admin
     *
     * @param userName         User name of User
     * @param claims           claim uri and claim value map
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims,
                                           String profileName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are set by Admin
     *
     * @param userName         User name of User
     * @param claims
     * @param profileName
     * @param userStoreManager The underlying UserStoreManager  @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims,
                                            String profileName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attributes are deleted by Admin
     *
     * @param userName         User name of User
     * @param claims           claim uri and claim value map
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
                                              UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user attributes are deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before user attribute is deleted by Admin
     *
     * @param userName         User name of User
     * @param claimURI         claim uri
     * @param profileName      user profile name
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManager
     */
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
                                             UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after user attribute is deleted by Admin
     *
     * @param userName         User name of User
     * @param userStoreManager The underlying UserStoreManager
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws UserStoreException Thrown by the underlying UserStoreManagern
     */
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions,
                                UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after adding a role.
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
                                 UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before deleting a role.
     *
     * @param roleName
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws UserStoreException
     */
    public boolean doPreUpdateRoleName(String roleName, String newRoleName,
                                       UserStoreManager userStoreManager) throws UserStoreException;

    /**
     * Defines any additional actions after updating a role name.
     *
     * @param roleName
     * @param newRoleName
     * @return
     * @throws UserStoreException
     */
    public boolean doPostUpdateRoleName(String roleName, String newRoleName,
                                        UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions before updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws UserStoreException
     */
    public boolean doPreUpdateUserListOfRole(String roleName, String deletedUsers[],
                                             String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Defines any additional actions after updating a role.
     *
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @return
     * @throws UserStoreException
     */
    public boolean doPostUpdateUserListOfRole(String roleName, String deletedUsers[],
                                              String[] newUsers, UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions before updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                             String[] newRoles,
                                             UserStoreManager userStoreManager)
            throws UserStoreException;

    /**
     * Define any additional actions after updating role list of user.
     *
     * @param userName
     * @param deletedRoles
     * @param newRoles
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles,
                                              String[] newRoles,
                                              UserStoreManager userStoreManager)
            throws UserStoreException;
}
