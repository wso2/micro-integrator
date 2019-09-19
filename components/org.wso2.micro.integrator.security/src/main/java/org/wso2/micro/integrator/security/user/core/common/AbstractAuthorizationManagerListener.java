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
package org.wso2.micro.integrator.security.user.core.common;

import org.wso2.micro.integrator.security.user.core.AuthorizationManager;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.listener.AuthorizationManagerListener;

public abstract class AbstractAuthorizationManagerListener implements AuthorizationManagerListener {

    public boolean isUserAuthorized(String userName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean isRoleAuthorized(String userName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean authorizeUser(String userName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean authorizeRole(String roleName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean denyUser(String userName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean denyRole(String roleName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean clearUserAuthorization(String userName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean clearUserAuthorization(String userName, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean clearRoleAuthorization(String roleName, String resourceId, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean clearRoleActionOnAllResources(String roleName, String action, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean clearRoleAuthorization(String roleName, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean clearResourceAuthorizations(String resourceId, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

    public boolean resetPermissionOnUpdateRole(String roleName, String newRoleName, AuthorizationManager authorizationManager) throws UserStoreException {
        return true;
    }

}
