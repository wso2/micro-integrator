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

import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.listener.UserStoreManagerListener;

import java.util.Map;

public abstract class AbstractUserStoreManagerListener implements UserStoreManagerListener {

    public boolean authenticate(String userName, Object credential,
                                UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    public boolean addUser(String userName, Object credential, String[] roleList,
                           Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    public boolean updateCredential(String userName, Object newCredential, Object oldCredential,
                                    UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    public boolean updateCredentialByAdmin(String userName, Object newCredential,
                                           UserStoreManager userStoreManager) throws UserStoreException {
        return true;
    }

    public boolean deleteUser(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {
        return true;
    }

    public boolean updateRoleName(String roleName, String newRoleName) throws UserStoreException {
        return true;
    }
}
