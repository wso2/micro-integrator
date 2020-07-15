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
package org.wso2.micro.integrator.security.user.core.hybrid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.authorization.AuthorizationCache;
import org.wso2.micro.integrator.security.user.core.common.UserRolesCache;
import org.wso2.micro.integrator.security.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.util.List;
import java.util.Map;

public class HybridRoleManager {

    private static Log log = LogFactory.getLog(JDBCUserStoreManager.class);
    protected UserRealm userRealm;
    protected UserRolesCache userRolesCache = null;
    int tenantId;
    private RealmConfiguration realmConfig;
    private boolean userRolesCacheEnabled = true;

    public HybridRoleManager(int tenantId, RealmConfiguration realmConfig, UserRealm realm) {
        this.tenantId = tenantId;
        this.realmConfig = realmConfig;
        this.userRealm = realm;
    }

    /**
     * @param roleName Domain-less role
     * @param userList Domain-aware user list
     * @throws UserStoreException
     */
    public void addHybridRole(String roleName, String[] userList) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
    }

    /**
     * @param tenantID
     */
    protected void clearUserRolesCacheByTenant(int tenantID) {
        if (userRolesCache != null) {
            userRolesCache.clearCacheByTenant(tenantID);
            AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
            authorizationCache.clearCacheByTenant(tenantID);
        }
    }

    /**
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isExistingRole(String roleName) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
        return false;
    }

    /**
     * @param filter
     * @return
     * @throws UserStoreException
     */
    public String[] getHybridRoles(String filter) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
        return null;
    }

    /**
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public String[] getUserListOfHybridRole(String roleName) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
        return null;
    }

    /**
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @throws UserStoreException
     */
    public void updateUserListOfHybridRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
    }

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    public String[] getHybridRoleListOfUser(String userName, String filter) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
        return null;
    }

    /**
     * Get hybrid role list of users
     *
     * @param userNames user name list
     * @return map of hybrid role list of users
     * @throws UserStoreException userStoreException
     */
    public Map<String, List<String>> getHybridRoleListOfUsers(List<String> userNames, String domainName) throws
            UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
        return null;
    }

    /**
     * @param user
     * @param deletedRoles
     * @param addRoles
     * @throws UserStoreException
     */
    public void updateHybridRoleListOfUser(String user, String[] deletedRoles, String[] addRoles)
            throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
    }

    /**
     * @param roleName
     * @throws UserStoreException
     */
    public void deleteHybridRole(String roleName) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
    }

    /**
     * @param roleName
     * @param newRoleName
     * @throws UserStoreException
     */
    public void updateHybridRoleName(String roleName, String newRoleName) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
    }

    /**
     * ##### This method is not used anywhere
     *
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
        return false;
    }

    /**
     * If a user is added to a hybrid role, that entry should be deleted upon deletion of the user.
     *
     * @param userName
     * @throws UserStoreException
     */
    public void deleteUser(String userName) throws UserStoreException {
        UserCoreUtil.logUnsupportedOperation();
    }

    /**
     *
     */
    protected void initUserRolesCache() {

        String userRolesCacheEnabledString = (realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED));

        if (userRolesCacheEnabledString != null && !userRolesCacheEnabledString.equals("")) {
            userRolesCacheEnabled = Boolean.parseBoolean(userRolesCacheEnabledString);
            if (log.isDebugEnabled()) {
                log.debug("User Roles Cache is configured to:" + userRolesCacheEnabledString);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.info("User Roles Cache is not configured. Default value: "
                        + userRolesCacheEnabled + " is taken.");
            }
        }

        if (userRolesCacheEnabled) {
            int timeOut = UserCoreConstants.USER_ROLE_CACHE_DEFAULT_TIME_OUT;
            String timeOutString = realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_ROLE_CACHE_TIME_OUT);
            if (timeOutString != null) {
                timeOut = Integer.parseInt(timeOutString);
            }
            userRolesCache = UserRolesCache.getInstance();
            userRolesCache.setTimeOut(timeOut);
        }
    }

    /**
     * @return
     */
    protected String getMyDomainName() {
        return UserCoreUtil.getDomainName(realmConfig);
    }
}
