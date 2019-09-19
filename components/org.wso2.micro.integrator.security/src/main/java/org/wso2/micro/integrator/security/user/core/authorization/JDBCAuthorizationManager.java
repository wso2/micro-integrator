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

package org.wso2.micro.integrator.security.user.core.authorization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.AuthorizationManager;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.common.AbstractUserStoreManager;
import org.wso2.micro.integrator.security.user.core.constants.UserCoreDBConstants;
import org.wso2.micro.integrator.security.user.core.internal.UMListenerServiceComponent;
import org.wso2.micro.integrator.security.user.core.ldap.LDAPConstants;
import org.wso2.micro.integrator.security.user.core.listener.AuthorizationManagerListener;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfigurationManager;
import org.wso2.micro.integrator.security.user.core.util.DatabaseUtil;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import static org.wso2.micro.integrator.security.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE;

public class JDBCAuthorizationManager implements AuthorizationManager {

    /**
     * The root node of the tree
     */
    private static Log log = LogFactory.getLog(JDBCAuthorizationManager.class);
    private static boolean debug = log.isDebugEnabled();
    private final String GET_ALL_ROLES_OF_USER_ENABLED = "GetAllRolesOfUserEnabled";
    private DataSource dataSource = null;
    private PermissionTree permissionTree = null;
    private AuthorizationCache authorizationCache = null;
    private UserRealm userRealm = null;
    private RealmConfiguration realmConfig = null;
    private boolean caseInSensitiveAuthorizationRules;
    private boolean preserveCaseForResources = true;
    private boolean verifyByRetrievingAllUserRoles;
    private String cacheIdentifier;
    private int tenantId;
    private String isCascadeDeleteEnabled;
    private static final String DELETE_ROLE_PERMISSIONS = "DeleteRolePermissions";
    private static final String DELETE_USER_PERMISSIONS = "DeleteUserPermissions";
    private static final String DELETE_ROLE_PERMISSIONS_MYSQL = "DeleteRolePermissions-mysql";
    private static final String DELETE_USER_PERMISSIONS_MYSQL = "DeleteUserPermissions-mysql";

    private static final ThreadLocal<Boolean> isSecureCall = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public JDBCAuthorizationManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                    ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm,
                                    Integer tenantId) throws UserStoreException {

        authorizationCache = AuthorizationCache.getInstance();
        if (!"true".equals(realmConfig.getAuthorizationManagerProperty(UserCoreConstants.
                RealmConfig.PROPERTY_AUTHORIZATION_CACHE_ENABLED))) {
            authorizationCache.disableCache();
        }

        if (!"true".equals(realmConfig.getAuthorizationManagerProperty(UserCoreConstants.
                RealmConfig.PROPERTY_CASE_SENSITIVITY))) {
            caseInSensitiveAuthorizationRules = true;
        }

        if ("true".equals(realmConfig.getAuthorizationManagerProperty(GET_ALL_ROLES_OF_USER_ENABLED))) {
            verifyByRetrievingAllUserRoles = true;
        }

        if (!realmConfig.getAuthzProperties().containsKey(DELETE_ROLE_PERMISSIONS)) {
            realmConfig.getAuthzProperties().put(DELETE_ROLE_PERMISSIONS, DBConstants
                    .ON_DELETE_PERMISSION_UM_ROLE_PERMISSIONS_SQL);
        }

        if (!realmConfig.getAuthzProperties().containsKey(DELETE_USER_PERMISSIONS)) {
            realmConfig.getAuthzProperties().put(DELETE_USER_PERMISSIONS, DBConstants
                    .ON_DELETE_PERMISSION_UM_USER_PERMISSIONS_SQL);
        }

        if ("false".equals(realmConfig.getAuthorizationManagerProperty(UserCoreConstants.RealmConfig
                .PROPERTY_PRESERVE_CASE_FOR_RESOURCES))) {
            preserveCaseForResources = false;
        }

        if (!realmConfig.getAuthzProperties().containsKey(DELETE_ROLE_PERMISSIONS_MYSQL)) {
            realmConfig.getAuthzProperties().put(DELETE_ROLE_PERMISSIONS_MYSQL, DBConstants
                    .ON_DELETE_PERMISSION_UM_ROLE_PERMISSIONS_SQL_MYSQL);
        }

        if (!realmConfig.getAuthzProperties().containsKey(DELETE_USER_PERMISSIONS_MYSQL)) {
            realmConfig.getAuthzProperties().put(DELETE_USER_PERMISSIONS_MYSQL, DBConstants
                    .ON_DELETE_PERMISSION_UM_USER_PERMISSIONS_SQL_MYSQL);
        }

        String userCoreCacheIdentifier = realmConfig.getUserStoreProperty(UserCoreConstants.
                RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER);

        if (userCoreCacheIdentifier != null && userCoreCacheIdentifier.trim().length() > 0) {
            cacheIdentifier = userCoreCacheIdentifier;
        } else {
            cacheIdentifier = UserCoreConstants.DEFAULT_CACHE_IDENTIFIER;
        }

        dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
        if (dataSource == null) {
            dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
            properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
        }

        this.isCascadeDeleteEnabled = realmConfig.getRealmProperty(UserCoreDBConstants.CASCADE_DELETE_ENABLED);

        this.permissionTree = new PermissionTree(cacheIdentifier, tenantId, dataSource, preserveCaseForResources);
        this.realmConfig = realmConfig;
        this.userRealm = realm;
        this.tenantId = tenantId;
        if (log.isDebugEnabled()) {
            log.debug("The jdbcDataSource being used by JDBCAuthorizationManager :: "
                    + dataSource.hashCode());
        }
        this.populatePermissionTreeFromDB();
        this.addInitialData();
    }

    public boolean isRoleAuthorized(String roleName, String resourceId, String action) throws UserStoreException {

        if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            Object object = callSecure("isRoleAuthorized", new Object[]{roleName, resourceId, action}, argTypes);
            return (Boolean) object;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.isRoleAuthorized(roleName, resourceId, action, this)) {
                return false;
            }
        }

        permissionTree.updatePermissionTree();
        SearchResult sr = permissionTree.getRolePermission(roleName, PermissionTreeUtil
                .actionToPermission(action), null, null, PermissionTreeUtil
                .toComponenets(resourceId));


        if (log.isDebugEnabled()) {
            if (!sr.getLastNodeAllowedAccess()) {
                log.debug(roleName + " role is not Authorized to perform " + action + " on " + resourceId);
            }
        }

        return sr.getLastNodeAllowedAccess();
    }

    public boolean isUserAuthorized(String userName, String resourceId, String action)
            throws UserStoreException {

        if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            Object object = callSecure("isUserAuthorized", new Object[]{userName, resourceId, action}, argTypes);
            return (Boolean) object;
        }

        if (UserCoreConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
            return true;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.isUserAuthorized(userName, resourceId, action, this)) {
                return false;
            }
        }

        try {
            Boolean userAllowed = authorizationCache.isUserAuthorized(cacheIdentifier,
                    tenantId, userName, resourceId, action);
            if (log.isDebugEnabled()) {
                if (userAllowed != null && !userAllowed) {
                    log.debug("Authorization cache hit. " +
                            userName + " user is not Authorized to perform " + action +
                            " on " + resourceId);
                }
            }

            if (userAllowed != null) {
                return userAllowed;
            }

        } catch (AuthorizationCacheException e) {
            // Entry not found in the cache. Just continue.
        }

        if (log.isDebugEnabled()) {
            log.debug("Authorization cache miss for username : " + userName + " resource " + resourceId
                    + " action : " + action);
        }

        permissionTree.updatePermissionTree();

        //following is related with user permission, and it is not hit in the current flow.
        SearchResult sr =
                permissionTree.getUserPermission(userName,
                        PermissionTreeUtil.actionToPermission(action),
                        null, null,
                        PermissionTreeUtil.toComponenets(resourceId));
        if (sr.getLastNodeAllowedAccess()) {
            authorizationCache.addToCache(cacheIdentifier, tenantId, userName, resourceId, action, true);
            return true;
        }


        boolean userAllowed = false;
        String[] allowedRoles = getAllowedRolesForResource(resourceId, action);


        if (allowedRoles != null && allowedRoles.length > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Roles which have permission for resource : " + resourceId + " action : " + action);
                for (String allowedRole : allowedRoles) {
                    log.debug("Role :  " + allowedRole);
                }
            }

            if (verifyByRetrievingAllUserRoles) {
                String[] roles = null;
                try {
                    roles = userRealm.getUserStoreManager().getRoleListOfUser(userName);
                } catch (UserStoreException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error getting role list of user : " + userName, e);
                    }
                }

                if (roles == null || roles.length == 0) {
                    AbstractUserStoreManager manager = (AbstractUserStoreManager) userRealm.getUserStoreManager();
                    roles = manager.doGetRoleListOfUser(userName, "*");
                }

                loopAllowedRoles:
                for (String allowRole : allowedRoles) {
                    for (String userRole : roles) {
                        if (allowRole.equalsIgnoreCase(userRole)) {
                            userAllowed = true;
                            break loopAllowedRoles;
                        }
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug(userName + " user has permitted resource :  " + resourceId + ", action :" + action);
                }

            } else {
                AbstractUserStoreManager manager = (AbstractUserStoreManager) userRealm.getUserStoreManager();
                for (String role : allowedRoles) {
                    try {
                        if (manager.isUserInRole(userName, role)) {
                            if (log.isDebugEnabled()) {
                                log.debug(userName + " user is in role :  " + role);
                            }
                            userAllowed = true;
                            break;
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug(userName + " user is not in role :  " + role);
                            }
                        }
                    } catch (UserStoreException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(userName + " user is not in role :  " + role, e);
                        }
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No roles have permission for resource : " + resourceId + " action : " + action);
            }
        }

        //need to add the authorization decision taken by role based permission
        authorizationCache.addToCache(cacheIdentifier, this.tenantId, userName, resourceId, action,
                userAllowed);

        if (log.isDebugEnabled()) {
            if (!userAllowed) {
                log.debug(userName + " user is not Authorized to perform " + action + " on " + resourceId);
            }
        }

        return userAllowed;
    }

    public String[] getAllowedRolesForResource(String resourceId, String action)
            throws UserStoreException {

        if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getAllowedRolesForResource", new Object[]{resourceId, action}, argTypes);
            return (String[]) object;
        }

        TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
        permissionTree.updatePermissionTree();
        SearchResult sr =
                permissionTree.getAllowedRolesForResource(null,
                        null,
                        permission,
                        PermissionTreeUtil.toComponenets(resourceId));

        if (debug) {
            log.debug("Allowed roles for the ResourceID: " + resourceId + " Action: " + action);
            String[] roles = sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
            for (String role : roles) {
                log.debug("role: " + role);
            }
        }

        return sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public void refreshAllowedRolesForResource(String resourceId)
            throws UserStoreException {
        permissionTree.updatePermissionTree(resourceId);
    }

    public String[] getExplicitlyAllowedUsersForResource(String resourceId, String action)
            throws UserStoreException {

        if (resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getExplicitlyAllowedUsersForResource", new Object[]{resourceId, action}, argTypes);
            return (String[]) object;
        }

        TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
        permissionTree.updatePermissionTree();
        SearchResult sr =
                permissionTree.getAllowedUsersForResource(null,
                        null,
                        permission,
                        PermissionTreeUtil.toComponenets(resourceId));

        if (debug) {
            log.debug("Explicitly allowed roles for the ResourceID: " + resourceId + " Action: " + action);
            String[] roles = sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
            for (String role : roles) {
                log.debug("role: " + role);
            }
        }

        return sr.getAllowedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public String[] getDeniedRolesForResource(String resourceId, String action)
            throws UserStoreException {

        if (resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getDeniedRolesForResource", new Object[]{resourceId, action}, argTypes);
            return (String[]) object;
        }

        TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
        permissionTree.updatePermissionTree();
        SearchResult sr =
                permissionTree.getDeniedRolesForResource(null,
                        null,
                        permission,
                        PermissionTreeUtil.toComponenets(resourceId));
        return sr.getDeniedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public String[] getExplicitlyDeniedUsersForResource(String resourceId, String action)
            throws UserStoreException {

        if (resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getExplicitlyDeniedUsersForResource", new Object[]{resourceId, action},
                    argTypes);
            return (String[]) object;
        }

        TreeNode.Permission permission = PermissionTreeUtil.actionToPermission(action);
        permissionTree.updatePermissionTree();
        SearchResult sr =
                permissionTree.getDeniedUsersForResource(null,
                        null,
                        permission,
                        PermissionTreeUtil.toComponenets(resourceId));
        return sr.getDeniedEntities().toArray(new String[sr.getAllowedEntities().size()]);
    }

    public String[] getAllowedUIResourcesForUser(String userName, String permissionRootPath)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getAllowedUIResourcesForUser", new Object[]{userName, permissionRootPath},
                    argTypes);
            return (String[]) object;
        }

        if (verifyByRetrievingAllUserRoles) {

            List<String> lstPermissions = new ArrayList<String>();
            String[] roles = this.userRealm.getUserStoreManager().getRoleListOfUser(userName);
            permissionTree.updatePermissionTree();
            permissionTree.getUIResourcesForRoles(roles, lstPermissions, permissionRootPath);
            String[] permissions = lstPermissions.toArray(new String[lstPermissions.size()]);
            return UserCoreUtil.optimizePermissions(permissions);

        } else {

            List<String> lstPermissions = new ArrayList<String>();
            List<String> resourceIds = getUIPermissionId();
            if (resourceIds != null) {
                for (String resourceId : resourceIds) {
                    if (isUserAuthorized(userName, resourceId, UserCoreConstants.UI_PERMISSION_ACTION)) {
                        if (permissionRootPath == null) {
                            lstPermissions.add(resourceId);
                        } else {
                            if (resourceId.contains(permissionRootPath)) {
                                lstPermissions.add(resourceId);
                            }
                        }
                    }//authorization check up
                }//loop over resource list
            }//resource ID checkup

            String[] permissions = lstPermissions.toArray(new String[lstPermissions.size()]);
            String[] optimizedList = UserCoreUtil.optimizePermissions(permissions);

            if (debug) {
                log.debug("Allowed UI Resources for User: " + userName + " in permissionRootPath: " +
                        permissionRootPath);
                for (String resource : optimizedList) {
                    log.debug("Resource: " + resource);
                }
            }

            return optimizedList;
        }
    }

    public String[] getAllowedUIResourcesForRole(String roleName, String permissionRootPath)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            Object object = callSecure("getAllowedUIResourcesForRole",
                    new Object[]{roleName, permissionRootPath}, argTypes);
            return (String[]) object;
        }

        List<String> lstPermissions = new ArrayList<String>();
        List<String> resourceIds = getUIPermissionId();
        if (resourceIds != null) {
            for (String resourceId : resourceIds) {
                if (isRoleAuthorized(roleName, resourceId, UserCoreConstants.UI_PERMISSION_ACTION)) {
                    if (permissionRootPath == null) {
                        permissionRootPath = "/"; // Assign root path when permission path is null
                    }
                    if (resourceId.contains(permissionRootPath)) {
                        lstPermissions.add(resourceId);
                    }
                }//authorization check up
            }//loop over resource list
        }//resource ID checkup

        String[] permissions = lstPermissions.toArray(new String[lstPermissions.size()]);
        String[] optimizedList = UserCoreUtil.optimizePermissions(permissions);

        if (debug) {
            log.debug("Allowed UI Resources for Role: " + roleName + " in permissionRootPath: " +
                    permissionRootPath);
            for (String resource : optimizedList) {
                log.debug("Resource: " + resource);
            }
        }

        return optimizedList;
    }

    public void authorizeRole(String roleName, String resourceId, String action)
            throws UserStoreException {

        if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("authorizeRole", new Object[]{roleName, resourceId, action},
                    argTypes);
            return;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.authorizeRole(roleName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }
        addAuthorizationForRole(roleName, resourceId, action, UserCoreConstants.ALLOW, true);
    }

    public void denyRole(String roleName, String resourceId, String action)
            throws UserStoreException {

        if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("denyRole", new Object[]{roleName, resourceId, action},
                    argTypes);
            return;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.denyRole(roleName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }
        addAuthorizationForRole(roleName, resourceId, action, UserCoreConstants.DENY, true);
    }

    public void authorizeUser(String userName, String resourceId, String action)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("authorizeUser", new Object[]{userName, resourceId, action},
                    argTypes);
            return;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.authorizeUser(userName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }
        if (!preserveCaseForResources) {
            resourceId = resourceId.toLowerCase();
        }
        addAuthorizationForUser(userName, resourceId, action, UserCoreConstants.ALLOW, true);
    }

    public void denyUser(String userName, String resourceId, String action)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("denyUser", new Object[]{userName, resourceId, action},
                    argTypes);
            return;
        }


        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.denyUser(userName, resourceId, action, this)) {
                return;
            }
        }

        if (resourceId == null || action == null) {
            log.error("Invalid data provided at authorization code");
            throw new UserStoreException("Invalid data provided");
        }
        if (!preserveCaseForResources) {
            resourceId = resourceId.toLowerCase();
        }

        addAuthorizationForUser(userName, resourceId, action, UserCoreConstants.DENY, true);
    }

    public void clearResourceAuthorizations(String resourceId) throws UserStoreException {

        throw new UserStoreException("Cannot clear Authorizations. This Functionality is not available in WSO2 Micro " +
                "Integrator");
        // TODO Revisit and decide whether we need this for MI
        /*if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("clearResourceAuthorizations", new Object[]{resourceId},
                    argTypes);
            return;
        }


        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearResourceAuthorizations(resourceId, this)) {
                return;
            }
        }
        *//**
         * Need to clear authz cache when resource authorization is cleared.
         *//*
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            if(isCascadeDeleteEnabled == null || !Boolean.parseBoolean(isCascadeDeleteEnabled)) {
                DatabaseUtil.updateDatabase(dbConnection, realmConfig.getAuthzProperties().get(
                        DELETE_ROLE_PERMISSIONS), resourceId, tenantId);
                DatabaseUtil.updateDatabase(dbConnection, realmConfig.getAuthzProperties().get(
                        DELETE_USER_PERMISSIONS), resourceId, tenantId);
                String type = DatabaseCreator.getDatabaseType(dbConnection);
                if (UserCoreConstants.MYSQL_TYPE.equals(type)
                        || UserCoreConstants.MSSQL_TYPE.equals(type)) {
                    DatabaseUtil.updateDatabase(dbConnection, realmConfig.getAuthzProperties().get
                            (DELETE_ROLE_PERMISSIONS_MYSQL), resourceId, tenantId);
                    DatabaseUtil.updateDatabase(dbConnection, realmConfig.getAuthzProperties().get
                            (DELETE_USER_PERMISSIONS_MYSQL), resourceId, tenantId);
                } else {
                    DatabaseUtil.updateDatabase(dbConnection, realmConfig.getAuthzProperties().get
                            (DELETE_ROLE_PERMISSIONS), resourceId, tenantId);
                    DatabaseUtil.updateDatabase(dbConnection, realmConfig.getAuthzProperties().get
                            (DELETE_USER_PERMISSIONS), resourceId, tenantId);
                }
            }
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_PERMISSION_SQL,
                    resourceId, tenantId);
            permissionTree.clearResourceAuthorizations(resourceId);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while clearing resource authorizations for resource id : " + resourceId;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage =
                    "Error occurred while clearing resource authorization for resource id : " + resourceId;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }*/
    }

    public void clearRoleAuthorization(String roleName, String resourceId, String action)
            throws UserStoreException {

        if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("clearRoleAuthorization", new Object[]{roleName, resourceId, action},
                    argTypes);
            return;
        }


        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearRoleAuthorization(roleName, resourceId, action, this)) {
                return;
            }
        }

        /*need to clear tenant authz cache once role authorization is removed, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String domain = UserCoreUtil.extractDomainFromName(roleName);
            if (domain != null) {
                domain = domain.toUpperCase();
            }
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_ROLE_PERMISSION_SQL,
                    UserCoreUtil.removeDomainFromName(roleName), resourceId, action, tenantId, tenantId, tenantId, domain);
            permissionTree.clearRoleAuthorization(roleName, resourceId, action);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while clearing role authorizations for role : " + roleName + " & resource id : " +
                    resourceId + " & action : " + action;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public void clearUserAuthorization(String userName, String resourceId, String action)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class};
            callSecure("clearUserAuthorization", new Object[]{userName, resourceId, action},
                    argTypes);
            return;
        }


        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearUserAuthorization(userName, resourceId, action, this)) {
                return;
            }
        }

        this.authorizationCache.clearCacheEntry(cacheIdentifier, tenantId, userName, resourceId,
                action);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            int permissionId = this.getPermissionId(dbConnection, resourceId, action);
            DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_USER_PERMISSION_SQL,
                    userName, resourceId, action, tenantId, tenantId);
            permissionTree.clearUserAuthorization(userName, resourceId, action);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while clearing role authorizations for user : " + userName + " & resource id : " +
                    resourceId + " & action : " + action;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void clearRoleActionOnAllResources(String roleName, String action)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            callSecure("clearRoleActionOnAllResources", new Object[]{roleName, action},
                    argTypes);
            return;
        }


        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearRoleActionOnAllResources(roleName, action, this)) {
                return;
            }
        }

        /*need to clear tenant authz cache once role authorization is removed, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.clearRoleAuthorization(roleName, action);
            String domain = UserCoreUtil.extractDomainFromName(roleName);
            if (domain != null) {
                domain = domain.toUpperCase();
            }
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.DELETE_ROLE_PERMISSIONS_BASED_ON_ACTION, UserCoreUtil.removeDomainFromName(roleName),
                    action, tenantId, tenantId, tenantId, domain);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while clearing role action on all resources for role : " + roleName +
                    " & action : " + action;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void clearRoleAuthorization(String roleName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("clearRoleAuthorization", new Object[]{roleName},
                    argTypes);
            return;
        }


        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearRoleAuthorization(roleName, this)) {
                return;
            }
        }


        /*need to clear tenant authz cache once role authorization is removed, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.clearRoleAuthorization(roleName);
            String domain = UserCoreUtil.extractDomainFromName(roleName);
            if (domain != null) {
                domain = domain.toUpperCase();
            }
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.ON_DELETE_ROLE_DELETE_PERMISSION_SQL, UserCoreUtil.removeDomainFromName(roleName),
                    tenantId, tenantId, domain);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while clearing role authorizations for role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void clearUserAuthorization(String userName) throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class};
            callSecure("clearUserAuthorization", new Object[]{userName},
                    argTypes);
            return;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.clearUserAuthorization(userName, this)) {
                return;
            }
        }

        this.authorizationCache.clearCacheByTenant(tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.clearUserAuthorization(userName);
            DatabaseUtil.updateDatabase(dbConnection,
                    DBConstants.ON_DELETE_USER_DELETE_PERMISSION_SQL, userName, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while clearing user authorizations for user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }

    }

    public void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class};
            callSecure("resetPermissionOnUpdateRole", new Object[]{roleName, newRoleName},
                    argTypes);
            return;
        }

        for (AuthorizationManagerListener listener : UMListenerServiceComponent
                .getAuthorizationManagerListeners()) {
            if (!listener.resetPermissionOnUpdateRole(roleName, newRoleName, this)) {
                return;
            }
        }

        /*need to clear tenant authz cache when role is updated, currently there is
        no way to remove cache entry by role.*/
        authorizationCache.clearCacheByTenant(this.tenantId);

        String sqlStmt = DBConstants.UPDATE_UM_ROLE_NAME_PERMISSION_SQL;
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for update role name is null");
        }
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            permissionTree.updateRoleNameInCache(roleName, newRoleName);
            String domain = UserCoreUtil.extractDomainFromName(newRoleName);
            newRoleName = UserCoreUtil.removeDomainFromName(newRoleName);
            roleName = UserCoreUtil.removeDomainFromName(roleName);
            if (domain != null) {
                domain = domain.toUpperCase();
            }
            DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName, tenantId, tenantId, domain);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while resetting permission on update role : " + roleName + " & to new role : " +
                    newRoleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void addAuthorization(String subject, String resourceId, String action,
                                 boolean authorized, boolean isRole) throws UserStoreException {

        // We are lowering the case of permission since we are not planning to support case sensitivity for permissions.
        if (!preserveCaseForResources && resourceId != null) {
            resourceId = resourceId.toLowerCase();
        }

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[]{String.class, String.class, String.class, boolean.class, boolean.class};
            callSecure("addAuthorization", new Object[]{subject, resourceId, action, authorized, isRole},
                    argTypes);
            return;
        }

        short allow = 0;
        if (authorized) {
            allow = UserCoreConstants.ALLOW;
        }
        if (isRole) {
            addAuthorizationForRole(subject, resourceId, action, allow, false);
        } else {
            addAuthorizationForUser(subject, resourceId, action, allow, false);
        }
    }

    private void addAuthorizationForRole(String roleName, String resourceId, String action,
                                         short allow, boolean updateCache) throws UserStoreException {

        // Need to clear tenant authz cache once role authorization is added, currently there is
        // no way to remove cache entry by role.
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        short isAllowed = -1;
        boolean isRolePermissionExisting = false;
        try {
            dbConnection = getDBConnection();
            int permissionId = this.getPermissionId(dbConnection, resourceId, action);
            String domain = UserCoreUtil.extractDomainFromName(roleName);
            if (domain != null) {
                domain = domain.toUpperCase();
            }
            //check if system role
            boolean isSystemRole = UserCoreUtil.isSystemRole(roleName, this.tenantId, this.dataSource);

            if (isSystemRole) {
                domain = UserCoreConstants.SYSTEM_DOMAIN_NAME;
            } else if (domain == null) {
                // assume as primary domain
                domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
            }

            prepStmt = dbConnection.prepareStatement(UserCoreDBConstants.IS_EXISTING_ROLE_PERMISSION_MAPPING);
            prepStmt.setString(1, UserCoreUtil.removeDomainFromName(roleName));
            prepStmt.setString(2, resourceId);
            prepStmt.setString(3, action);
            prepStmt.setInt(4, tenantId);
            prepStmt.setInt(5, tenantId);
            prepStmt.setInt(6, tenantId);
            prepStmt.setString(7, domain);

            rs = prepStmt.executeQuery();

            if (rs != null && rs.next()) {
                isAllowed = rs.getShort(2);
                isRolePermissionExisting = true;
            } else {
                // Role permission not existing
                isRolePermissionExisting = false;
            }

            if (isRolePermissionExisting && isAllowed != allow) {
                DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_ROLE_PERMISSION_SQL,
                        UserCoreUtil.removeDomainFromName(roleName), resourceId, action,
                        tenantId, tenantId, tenantId, domain);
                isRolePermissionExisting = false;
            }

            if (!isRolePermissionExisting) {

                if (log.isDebugEnabled()) {
                    log.debug("Adding permission Id: " + permissionId + " to the role: "
                            + UserCoreUtil.removeDomainFromName(roleName) + " of tenant: " + tenantId
                            + " of domain: " + domain + " to resource: " + resourceId);
                }

                try {
                    DatabaseUtil.updateDatabase(dbConnection, DBConstants.ADD_ROLE_PERMISSION_SQL, permissionId,
                            UserCoreUtil.removeDomainFromName(roleName), allow, tenantId, tenantId,
                            domain);
                } catch (UserStoreException e) {
                    if (ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE.getCode().equals(e.getErrorCode())) {
                        log.warn("Permission Id: " + permissionId + " is already added to the role: " + roleName);
                    } else {
                        throw e;
                    }
                }
            }

            if (updateCache) {
                if (allow == UserCoreConstants.ALLOW) {
                    permissionTree.authorizeRoleInTree(roleName, resourceId, action, true);
                } else {
                    permissionTree.denyRoleInTree(roleName, resourceId, action, true);
                }
            }
            dbConnection.commit();
        } catch (Exception e) {
            /*
            The db.commit() throws SQLException
            authorizeRoleInTree method and denyRoleInTree method throws UserStoreException.
            dbConnection should be rolled back when an exception is thrown
            */
            try {
                if (dbConnection != null) {
                    dbConnection.rollback();
                }
            } catch (SQLException e1) {
                throw new UserStoreException("Error in connection rollback ", e1);
            }
            if (log.isDebugEnabled()) {
                log.debug("Error! " + e.getMessage(), e);
            }
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Closing result set failed when adding role permission", e);
                }
            }
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    private void addAuthorizationForUser(String userName, String resourceId, String action,
                                         short allow, boolean updateCache) throws UserStoreException {

        // Need to clear tenant authz cache once role authorization is removed, currently there is
        // no way to remove cache entry by role.
        authorizationCache.clearCacheByTenant(this.tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        short isAllowed = -1;
        boolean isUserPermissionExisting = false;
        try {
            dbConnection = getDBConnection();
            int permissionId = this.getPermissionId(dbConnection, resourceId, action);
            prepStmt = dbConnection.prepareStatement(UserCoreDBConstants.IS_EXISTING_USER_PERMISSION_MAPPING);
            prepStmt.setString(1, userName);
            prepStmt.setString(2, resourceId);
            prepStmt.setString(3, action);
            prepStmt.setInt(4, tenantId);
            prepStmt.setInt(5, tenantId);

            rs = prepStmt.executeQuery();

            if (rs != null && rs.next()) {
                isAllowed = rs.getShort(2);
                isUserPermissionExisting = true;
            } else {
                // User permission not existing
                isUserPermissionExisting = false;
            }

            if (isUserPermissionExisting && isAllowed != allow) {
                DatabaseUtil.updateDatabase(dbConnection, DBConstants.DELETE_USER_PERMISSION_SQL,
                        userName, resourceId, action, tenantId, tenantId);
                isUserPermissionExisting = false;
            }

            if (!isUserPermissionExisting) {

                if (log.isDebugEnabled()) {
                    log.debug("Adding permission Id: " + permissionId + " to the user: "
                            + userName + " of tenant: " + tenantId
                            + " to resource: " + resourceId);
                }
                DatabaseUtil.updateDatabase(dbConnection, DBConstants.ADD_USER_PERMISSION_SQL,
                        permissionId, userName, allow, tenantId);
            }
            if (updateCache) {
                if (allow == UserCoreConstants.ALLOW) {
                    permissionTree.authorizeUserInTree(userName, resourceId, action, true);
                } else {
                    permissionTree.denyUserInTree(userName, resourceId, action, true);
                    authorizationCache.clearCacheEntry(cacheIdentifier, tenantId, userName, resourceId,
                            action);
                }
            }
            dbConnection.commit();
        } catch (Exception e) {
            /*
            The db.commit() throws SQLException
            authorizeRoleInTree method and denyRoleInTree method throws UserStoreException.
            dbConnection should be rolled back when an exception is thrown
            */
            try {
                if (dbConnection != null) {
                    dbConnection.rollback();
                }
            } catch (SQLException e1) {
                throw new UserStoreException("Error in connection rollback ", e1);
            }
            if (log.isDebugEnabled()) {
                log.debug("Error! " + e.getMessage(), e);
            }
            throw new UserStoreException("Error! " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Closing result set failed when adding user permission", e);
                }
            }
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    private List<String> getUIPermissionId() throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> resourceIds = new ArrayList<String>();
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(DBConstants.GET_PERMISSION_SQL);
            prepStmt.setString(1, UserCoreConstants.UI_PERMISSION_ACTION);
            prepStmt.setInt(2, tenantId);

            rs = prepStmt.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    resourceIds.add(rs.getString(1));
                }
            }
            return resourceIds;
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting UI permission ID";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    private int getPermissionId(Connection dbConnection, String resourceId, String action)
            throws UserStoreException {

        int permissionId = this.getPermissionIdFromStore(dbConnection, resourceId, action);
        if (permissionId == -1) {
            this.addPermissionId(dbConnection, resourceId, action);
            permissionId = this.getPermissionIdFromStore(dbConnection, resourceId, action);
            if (permissionId == -1) {
                String errorMessage =
                        "Error occurred while getting UI permission ID for resource id : " + resourceId + " & action : " +
                                action;
                throw new UserStoreException(errorMessage);
            }
        }
        return permissionId;
    }

    private int getPermissionIdFromStore(Connection dbConnection, String resourceId, String action)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int value = -1;
        try {
            prepStmt = dbConnection.prepareStatement(DBConstants.GET_PERMISSION_ID_SQL);
            prepStmt.setString(1, resourceId);
            prepStmt.setString(2, action);
            prepStmt.setInt(3, tenantId);

            rs = prepStmt.executeQuery();
            if (rs.next()) {
                value = rs.getInt(1);
            }
            return value;
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while getting UI permission ID for resource id : " + resourceId + " & action : " +
                    action;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    private void addPermissionId(Connection dbConnection, String resourceId, String action)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(DBConstants.ADD_PERMISSION_SQL);
            prepStmt.setString(1, resourceId);
            prepStmt.setString(2, action);
            prepStmt.setInt(3, tenantId);
            int count = prepStmt.executeUpdate();
            dbConnection.commit();
            if (log.isDebugEnabled()) {
                log.debug("Executed query is " + DBConstants.ADD_PERMISSION_SQL
                        + " and number of updated rows :: " + count);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            String errorMessage =
                    "Error occurred while adding UI permission ID for resource id : " + resourceId + " & action : " +
                    action;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    private Connection getDBConnection() throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        return dbConnection;
    }

    public void populatePermissionTreeFromDB() throws UserStoreException {

        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[0];
            callSecure("populatePermissionTreeFromDB", new Object[0], argTypes);
            return;
        }
        permissionTree.updatePermissionTreeFromDB();
    }

    /**
     * This method will unload all permission data loaded from a database. This method is useful in a lazy loading
     * scenario.
     */
    public void clearPermissionTree() {
        if (!isSecureCall.get()) {
            Class argTypes[] = new Class[0];
            try {
                callSecure("clearPermissionTree", new Object[0], argTypes);
            } catch (UserStoreException e) {
                if(log.isDebugEnabled()){
                    log.debug("Error while clearing Permission Tree : " + e);
                }
            }
            return;
        }
        this.permissionTree.clear();
        this.authorizationCache.clearCache();
    }

    public int getTenantId() throws UserStoreException {
        return tenantId;
    }

    private void addInitialData() throws UserStoreException {
        String mgtPermissions = realmConfig
                .getAuthorizationManagerProperty(UserCoreConstants.RealmConfig.PROPERTY_EVERYONEROLE_AUTHORIZATION);
        if (mgtPermissions != null) {
            String everyoneRole = realmConfig.getEveryOneRoleName();
            String[] resourceIds = mgtPermissions.split(",");
            for (String resourceId : resourceIds) {
                if (!this.isRoleAuthorized(everyoneRole, resourceId,
                        UserCoreConstants.UI_PERMISSION_ACTION)) {
                    this.authorizeRole(everyoneRole, resourceId,
                            UserCoreConstants.UI_PERMISSION_ACTION);
                }
            }
        }

        mgtPermissions = realmConfig
                .getAuthorizationManagerProperty(UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION);
        if (mgtPermissions != null) {
            String[] resourceIds = mgtPermissions.split(",");
            String adminRole = realmConfig.getAdminRoleName();
            for (String resourceId : resourceIds) {
                if (!this.isRoleAuthorized(adminRole, resourceId,
                        UserCoreConstants.UI_PERMISSION_ACTION)) {
                    /* check whether admin role created in primary user store or as a hybrid role.
                     * if primary user store, & if not read only &/or if read ldap groups false,
                     * it is a hybrid role.
                     */
                    // as internal roles are created, role name must be appended with internal domain name
                    if (userRealm.getUserStoreManager().isReadOnly()) {
                        String readLDAPGroups = realmConfig.getUserStoreProperties().get(
                                LDAPConstants.READ_LDAP_GROUPS);
                        if (readLDAPGroups != null) {
                            if (!(Boolean.parseBoolean(readLDAPGroups))) {
                                this.authorizeRole(UserCoreConstants.INTERNAL_DOMAIN +
                                                UserCoreConstants.DOMAIN_SEPARATOR +
                                                UserCoreUtil.removeDomainFromName(adminRole),
                                        resourceId, UserCoreConstants.UI_PERMISSION_ACTION);
                                return;
                            }
                        } else {
                            this.authorizeRole(UserCoreConstants.INTERNAL_DOMAIN +
                                            UserCoreConstants.DOMAIN_SEPARATOR +
                                            UserCoreUtil.removeDomainFromName(adminRole),
                                    resourceId, UserCoreConstants.UI_PERMISSION_ACTION);
                            return;
                        }
                    }
                    //if role is in external primary user store, prefix admin role with domain name
                    adminRole = UserCoreUtil.addDomainToName(adminRole, realmConfig.getUserStoreProperty(
                            UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                    this.authorizeRole(adminRole, resourceId, UserCoreConstants.UI_PERMISSION_ACTION);
                }
            }
        }
    }

    @Override
    public String[] normalizeRoles(String[] roles) {
        if (roles != null && roles.length > 0) {
            int index = 0;
            List<String> normalizedRoles = new ArrayList<String>();
            for (String role : roles) {
                if ((index = role.indexOf(UserCoreConstants.TENANT_DOMAIN_COMBINER.toLowerCase())) >= 0) {
                    normalizedRoles.add(role.substring(0, index));
                } else {
                    normalizedRoles.add(role);
                }
            }
            return normalizedRoles.toArray(new String[normalizedRoles.size()]);
        }
        return roles;
    }

    /**
     * This method is used by the APIs' in the JDBCAuthorizationManager
     * to make compatible with Java Security Manager.
     */
    private Object callSecure(final String methodName, final Object[] objects, final Class[] argTypes)
            throws UserStoreException {

        final JDBCAuthorizationManager instance = this;

        isSecureCall.set(Boolean.TRUE);
        final Method method;
        try {
            Class clazz = Class.forName("org.wso2.micro.integrator.security.user.core.authorization.JDBCAuthorizationManager");
            method = clazz.getDeclaredMethod(methodName, argTypes);

        } catch (NoSuchMethodException e) {
            log.error("Error occurred when calling method " + methodName, e);
            throw new UserStoreException(e);
        } catch (ClassNotFoundException e) {
            log.error("Error occurred when calling class " + methodName, e);
            throw new UserStoreException(e);
        }

        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    return method.invoke(instance, objects);
                }
            });
        } catch (PrivilegedActionException e) {
            if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof
                    UserStoreException) {
                // Actual UserStoreException get wrapped with two exceptions
                throw new UserStoreException(e.getCause().getCause().getMessage(), e);

            } else {
                String msg = "Error occurred while accessing Java Security Manager Privilege Block";
                log.error(msg);
                throw new UserStoreException(msg, e);
            }
        } finally {
            isSecureCall.set(Boolean.FALSE);
        }
    }

}
