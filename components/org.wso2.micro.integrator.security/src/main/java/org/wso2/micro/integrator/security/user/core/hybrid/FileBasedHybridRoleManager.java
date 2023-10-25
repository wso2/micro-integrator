/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.micro.integrator.security.user.core.hybrid;

import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.file.FileBasedUserStoreManager;

import javax.sql.DataSource;

/**
 * RoleManager implementation for the file based user store.
 */
public class FileBasedHybridRoleManager extends HybridRoleManager {
    public FileBasedHybridRoleManager(DataSource dataSource, int tenantId, RealmConfiguration realmConfig,
                                      UserRealm realm) {
        super(dataSource, tenantId, realmConfig, realm);
    }

    /**
     * Get the list of roles of a user
     *
     * @param userName user name
     * @param filter   filter
     * @return list of roles
     * @throws UserStoreException if an error occurs
     */
    public String[] getHybridRoleListOfUser(String userName, String filter) throws UserStoreException {
        FileBasedUserStoreManager userStoreManager = FileBasedUserStoreManager.getUserStoreManager();
        if (userStoreManager.isUserExists(userName)) {
            if (userStoreManager.isAdmin(userName)) {
                return new String[]{"admin", "Internal/everyone"};
            } else {
                return new String[]{"Internal/everyone"};
            }
        } else {
            return new String[0];
        }
    }
}
