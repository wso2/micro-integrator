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

import org.wso2.micro.integrator.security.user.api.Properties;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.tenant.Tenant;

import java.util.Map;

/**
 * This class is used to create a user store chain.
 */
public class IterativeUserStoreManager extends AbstractUserStoreManager {
    AbstractUserStoreManager abstractUserStoreManager;
    private IterativeUserStoreManager nextUserStoreManager;

    public IterativeUserStoreManager(AbstractUserStoreManager abstractUserStoreManager) {
        this.abstractUserStoreManager = abstractUserStoreManager;
    }

    /**
     * Set the next user store manager to create the ordered user store chain.
     */
    public void setNextUserStoreManager(AbstractUserStoreManager nextUserStoreManager) {
        if (nextUserStoreManager instanceof IterativeUserStoreManager) {
            this.nextUserStoreManager = (IterativeUserStoreManager) nextUserStoreManager;
        } else {
            this.nextUserStoreManager = new IterativeUserStoreManager(nextUserStoreManager);
        }
    }

    /**
     * This method will give the next user store manager of the current user store manager.
     */
    public IterativeUserStoreManager nextUserStoreManager() {
        return nextUserStoreManager;
    }

    /**
     * Get the abstract user store manager of the iterative user store manager.
     */
    public AbstractUserStoreManager getAbstractUserStoreManager() {
        return this.abstractUserStoreManager;
    }

    @Override
    protected Map<String, String> getUserPropertyValues(String userName, String[] propertyNames, String profileName) throws UserStoreException {

        return this.abstractUserStoreManager.getUserPropertyValues(userName, propertyNames, profileName);
    }

    @Override
    protected boolean doCheckExistingRole(String roleName) throws UserStoreException {

        return this.abstractUserStoreManager.doCheckExistingRole(roleName);
    }

    @Override
    protected RoleContext createRoleContext(String roleName) throws UserStoreException {

        return this.abstractUserStoreManager.createRoleContext(roleName);
    }

    @Override
    protected boolean doCheckExistingUser(String userName) throws UserStoreException {

        return this.abstractUserStoreManager.doCheckExistingUser(userName);
    }

    @Override
    protected String[] getUserListFromProperties(String property, String value, String profileName) throws UserStoreException {

        return this.abstractUserStoreManager.getUserListFromProperties(property, value, profileName);
    }

    @Override
    protected boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

        return this.abstractUserStoreManager.doAuthenticate(userName, credential);
    }

    @Override
    protected void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                             String profileName, boolean requirePasswordChange) throws UserStoreException {

        this.abstractUserStoreManager.doAddUser(userName, credential, roleList, claims, profileName,
                requirePasswordChange);

    }

    @Override
    protected void doUpdateCredential(String userName, Object newCredential, Object oldCredential) throws UserStoreException {

        this.abstractUserStoreManager.doUpdateCredential(userName, newCredential, oldCredential);
    }

    @Override
    protected void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {

        this.abstractUserStoreManager.doUpdateCredentialByAdmin(userName, newCredential);
    }

    @Override
    protected void doDeleteUser(String userName) throws UserStoreException {

        this.abstractUserStoreManager.doDeleteUser(userName);
    }

    @Override
    protected void doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName) throws UserStoreException {

        this.abstractUserStoreManager.doSetUserClaimValue(userName, claimURI, claimValue, profileName);
    }

    @Override
    protected void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName) throws UserStoreException {

        this.abstractUserStoreManager.doSetUserClaimValues(userName, claims, profileName);
    }

    @Override
    protected void doDeleteUserClaimValue(String userName, String claimURI, String profileName) throws UserStoreException {

        this.abstractUserStoreManager.doDeleteUserClaimValue(userName, claimURI, profileName);
    }

    @Override
    protected void doDeleteUserClaimValues(String userName, String[] claims, String profileName) throws UserStoreException {

        this.abstractUserStoreManager.doDeleteUserClaimValues(userName, claims, profileName);
    }

    @Override
    protected void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers) throws UserStoreException {

        this.abstractUserStoreManager.doUpdateUserListOfRole(roleName, deletedUsers, newUsers);
    }

    @Override
    protected void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles) throws UserStoreException {

        this.abstractUserStoreManager.doUpdateRoleListOfUser(userName, deletedRoles, newRoles);
    }

    @Override
    protected String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {

        return this.abstractUserStoreManager.doGetExternalRoleListOfUser(userName, filter);
    }

    @Override
    protected String[] doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter) throws UserStoreException {

        return this.abstractUserStoreManager.doGetSharedRoleListOfUser(userName, tenantDomain, filter);
    }

    @Override
    protected void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException {

        this.abstractUserStoreManager.doAddRole(roleName, userList, shared);
    }

    @Override
    protected void doDeleteRole(String roleName) throws UserStoreException {

        this.abstractUserStoreManager.doDeleteRole(roleName);
    }

    @Override
    protected void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {

        this.abstractUserStoreManager.doUpdateRoleName(roleName, newRoleName);
    }

    @Override
    protected String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {

        return this.abstractUserStoreManager.doGetRoleNames(filter, maxItemLimit);
    }

    @Override
    protected String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {

        return this.abstractUserStoreManager.doListUsers(filter, maxItemLimit);
    }

    @Override
    protected String[] doGetDisplayNamesForInternalRole(String[] userNames) throws UserStoreException {

        return this.abstractUserStoreManager.doGetDisplayNamesForInternalRole(userNames);
    }

    @Override
    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {

        return this.abstractUserStoreManager.doCheckIsUserInRole(userName, roleName);
    }

    @Override
    protected String[] doGetSharedRoleNames(String tenantDomain, String filter, int maxItemLimit) throws UserStoreException {

        return this.abstractUserStoreManager.doGetSharedRoleNames(tenantDomain, filter, maxItemLimit);
    }

    @Override
    protected String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {

        return this.abstractUserStoreManager.doGetUserListOfRole(roleName, filter);
    }

    @Override
    public String[] getProfileNames(String userName) throws UserStoreException {

        return this.abstractUserStoreManager.getProfileNames(userName);
    }

    @Override
    public String[] getAllProfileNames() throws UserStoreException {

        return this.abstractUserStoreManager.getAllProfileNames();
    }

    @Override
    public boolean isReadOnly() throws UserStoreException {

        return this.abstractUserStoreManager.isReadOnly();
    }

    @Override
    public int getUserId(String username) throws UserStoreException {

        return this.abstractUserStoreManager.getUserId(username);
    }

    @Override
    public int getTenantId(String username) throws UserStoreException {

        return this.abstractUserStoreManager.getTenantId(username);
    }

    @Override
    public int getTenantId() throws UserStoreException {

        return this.abstractUserStoreManager.getTenantId();
    }

    @Override
    public Map<String, String> getProperties(org.wso2.micro.integrator.security.user.api.Tenant tenant) throws org.wso2.micro.integrator.security.user.api.UserStoreException {

        return this.abstractUserStoreManager.getProperties(tenant);
    }

    @Override
    public boolean isMultipleProfilesAllowed() {

        return this.abstractUserStoreManager.isMultipleProfilesAllowed();
    }

    @Override
    public void addRememberMe(String userName, String token) throws org.wso2.micro.integrator.security.user.api.UserStoreException {

        this.abstractUserStoreManager.addRememberMe(userName, token);
    }

    @Override
    public boolean isValidRememberMeToken(String userName, String token) throws org.wso2.micro.integrator.security.user.api.UserStoreException {

        return this.abstractUserStoreManager.isValidRememberMeToken(userName, token);
    }

    @Override
    public Properties getDefaultUserStoreProperties() {

        return this.abstractUserStoreManager.getDefaultUserStoreProperties();
    }

    @Override
    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {

        return this.abstractUserStoreManager.getProperties(tenant);
    }

    @Override
    public boolean isBulkImportSupported() throws UserStoreException {

        return this.abstractUserStoreManager.isBulkImportSupported();
    }

    @Override
    public RealmConfiguration getRealmConfiguration() {

        return this.abstractUserStoreManager.getRealmConfiguration();
    }
}
