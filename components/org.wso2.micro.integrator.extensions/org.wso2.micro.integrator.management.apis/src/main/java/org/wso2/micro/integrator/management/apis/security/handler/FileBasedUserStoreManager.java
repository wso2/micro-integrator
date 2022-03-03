/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.internal.http.api.UserInfo;
import org.wso2.micro.integrator.management.apis.ManagementApiParser;
import org.wso2.micro.integrator.management.apis.UserStoreUndefinedException;

import java.util.Map;

/**
 * This class is used to authenticate, authorize users against the File based user store defined in internal-apis.xml
 */
public class FileBasedUserStoreManager {

    private static final Log LOG = LogFactory.getLog(FileBasedUserStoreManager.class);
    private static final FileBasedUserStoreManager userStoreManager = new FileBasedUserStoreManager();
    private static Map<String, UserInfo> usersList;
    private static boolean isInitialized = false;

    private FileBasedUserStoreManager() {

        initializeUserStore();
    }

    /**
     * Method to retrieve FileBasedUserStoreManager
     *
     * @return FileBasedUserStoreManager
     */
    public static FileBasedUserStoreManager getUserStoreManager() {

        return userStoreManager;
    }

    /**
     * Authenticate the user against the file based user store.
     *
     * @param username the user to be authenticated
     * @param password the password used for authentication
     * @return true if authenticated
     */
    public boolean authenticate(String username, String password) {

        if (usersList.containsKey(username)) {
            String passwordFromStore = String.valueOf(usersList.get(username).getPassword());
            if (StringUtils.isNotBlank(passwordFromStore) && passwordFromStore.equals(password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to assert if a user is an admin
     *
     * @param username the user to be validated as an admin
     * @return true if the admin role is assigned to the user
     */
    public boolean isAdmin(String username) {

        if (usersList.containsKey(username)) {
            UserInfo userInfo = usersList.get(username);
            return userInfo.isAdmin();
        }
        return false;
    }

    /**
     * Method to check whether FileBasedUserStoreManager is initialized
     *
     * @return true if successfully initialized
     */
    public boolean isInitialized() {

        return isInitialized;
    }

    private static void initializeUserStore() {

        ManagementApiParser mgtParser = new ManagementApiParser();
        try {
            usersList = mgtParser.getUserMap();
            isInitialized = true;
        } catch (UserStoreUndefinedException e) {
            LOG.error("User store config has not been defined in file "
                    + ManagementApiParser.getConfigurationFilePath(), e);
        }
    }
}
