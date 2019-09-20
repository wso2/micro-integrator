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

import org.wso2.micro.core.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The object representing the realm configuration.
 */
public class RealmConfiguration {

    protected String userStoreClass = null;
    protected String authorizationManagerClass = null;
    protected String addAdmin = null;
    protected String adminRoleName = null;
    protected String adminUserName = null;
    protected String adminPassword = null;
    protected String everyOneRoleName = null;
    protected String realmClassName = null;
    protected String description = null;
    protected List<String> restrictedDomainsForSelfSignUp = new ArrayList<String>();
    protected List<String> reservedRoleNames = new ArrayList<String>();
    protected String isOverrideUsernameClaimFromInternalUsername = "false";
    protected Map<String, String> userStoreProperties = new HashMap<String, String>();
    protected Map<String, String> authzProperties = new HashMap<String, String>();
    protected Map<String, String> realmProperties = new HashMap<String, String>();
    protected int tenantId;
    protected Date persistedTimestamp;
    protected boolean passwordsExternallyManaged = false;
    protected boolean isPrimary = false;
    protected org.wso2.micro.integrator.security.user.api.RealmConfiguration secondaryRealmConfig;
    protected Map<String, Map<String, String>> multipleCredentialProps = new HashMap<String, Map<String, String>>();

    public RealmConfiguration() {
        tenantId = Constants.SUPER_TENANT_ID;
    }

    public boolean isRestrictedDomainForSlefSignUp(String domain) {
        if (restrictedDomainsForSelfSignUp.contains(domain.toUpperCase())) {
            return true;
        }
        return false;
    }

    public boolean isReservedRoleName(String roleName) {
        if (reservedRoleNames.contains(roleName.toUpperCase())) {
            return true;
        }
        return false;
    }

    public void addRestrictedDomainForSelfSignUp(String domain) {
        if (domain != null) {
            restrictedDomainsForSelfSignUp.add(domain.toUpperCase());
        }
    }

    public void addReservedRoleName(String roleName) {
        if (roleName != null) {
            reservedRoleNames.add(roleName.toUpperCase());
        }
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean priamry) {
        this.isPrimary = priamry;
    }

    public boolean isPasswordsExternallyManaged() {
        return passwordsExternallyManaged;
    }

    public void setPasswordsExternallyManaged(boolean passwordsExternallyManaged) {
        this.passwordsExternallyManaged = passwordsExternallyManaged;
    }

    public org.wso2.micro.integrator.security.user.api.RealmConfiguration cloneRealmConfigurationWithoutSecondary() throws Exception {
        return cloneRealmConfiguration(false);
    }

    public org.wso2.micro.integrator.security.user.api.RealmConfiguration cloneRealmConfiguration() throws Exception {
        return cloneRealmConfiguration(true);
    }

    private org.wso2.micro.integrator.security.user.api.RealmConfiguration cloneRealmConfiguration(boolean needSecondary) throws Exception {
        org.wso2.micro.integrator.security.user.api.RealmConfiguration realmConfig = new org.wso2.micro.integrator.security.user.api.RealmConfiguration();

        realmConfig.setRealmClassName(realmClassName);
        realmConfig.setUserStoreClass(userStoreClass);
        realmConfig.setAuthorizationManagerClass(authorizationManagerClass);
        realmConfig.setAddAdmin(addAdmin);
        realmConfig.setAdminRoleName(adminRoleName);
        realmConfig.setAdminUserName(adminUserName);
        realmConfig.setAdminPassword(adminPassword);
        realmConfig.setEveryOneRoleName(everyOneRoleName);
        realmConfig.setPrimary(isPrimary);

        if (needSecondary) {
            if (secondaryRealmConfig != null) {
                realmConfig.setSecondaryRealmConfig(secondaryRealmConfig
                        .cloneRealmConfiguration());
            }
        }

        for (Iterator<String> domainNameItr = restrictedDomainsForSelfSignUp.iterator(); domainNameItr.hasNext(); ) {
            realmConfig.addRestrictedDomainForSelfSignUp(domainNameItr.next());
        }

        for (Iterator<String> roleNameIts = reservedRoleNames.iterator(); roleNameIts.hasNext(); ) {
            realmConfig.addReservedRoleName(roleNameIts.next());
        }

        Map<String, String> mapUserstore = new HashMap<String, String>();
        mapUserstore.putAll(userStoreProperties);
        realmConfig.setUserStoreProperties(mapUserstore);

        Map<String, String> mapAuthz = new HashMap<String, String>();
        mapAuthz.putAll(authzProperties);
        realmConfig.setAuthzProperties(mapAuthz);

        Map<String, String> mapRealm = new HashMap<String, String>();
        mapRealm.putAll(realmProperties);
        realmConfig.setRealmProperties(mapRealm);

        return realmConfig;
    }

    public org.wso2.micro.integrator.security.user.api.RealmConfiguration getSecondaryRealmConfig() {
        return secondaryRealmConfig;
    }

    public void setSecondaryRealmConfig(org.wso2.micro.integrator.security.user.api.RealmConfiguration secondaryRealm) {
        this.secondaryRealmConfig = secondaryRealm;
    }

    public String getAuthorizationPropertyValue(String propertyName) {
        return authzProperties.get(propertyName);
    }

    public String getRealmProperty(String propertyName) {
        return realmProperties.get(propertyName);
    }

    public String getUserStoreProperty(String propertyName) {
        return userStoreProperties.get(propertyName);
    }

    public String getAddAdmin() {
        return addAdmin;
    }

    public void setAddAdmin(String addAdmin) {
        this.addAdmin = addAdmin;
    }

    public String getAdminRoleName() {
        return adminRoleName;
    }

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    // two public setter methods used for external editing
    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getEveryOneRoleName() {
        return everyOneRoleName;
    }

    public void setEveryOneRoleName(String everyOneRoleName) {
        this.everyOneRoleName = everyOneRoleName;
    }

    public String getAuthorizationManagerClass() {
        return authorizationManagerClass;
    }

    public void setAuthorizationManagerClass(String authorizationManagerClass) {
        this.authorizationManagerClass = authorizationManagerClass;
    }

    public String getAuthorizationManagerProperty(String key) {
        return authzProperties.get(key);
    }

    public String getUserStoreClass() {
        return userStoreClass;
    }

    public void setUserStoreClass(String userStoreClass) {
        this.userStoreClass = userStoreClass;
    }

    public Map<String, String> getUserStoreProperties() {
        return userStoreProperties;
    }

    public void setUserStoreProperties(Map<String, String> userStoreProperties) {
        this.userStoreProperties = userStoreProperties;
    }

    public Map<String, String> getAuthzProperties() {
        return authzProperties;
    }

    public void setAuthzProperties(Map<String, String> authzProperties) {
        this.authzProperties = authzProperties;
    }

    public Map<String, String> getRealmProperties() {
        return realmProperties;
    }

    public void setRealmProperties(Map<String, String> realmProperties) {
        this.realmProperties = realmProperties;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Date getPersistedTimestamp() {
        if (null != persistedTimestamp) {
            return (Date) persistedTimestamp.clone();
        } else {
            return null;
        }
    }

    public void setPersistedTimestamp(Date persistedTimestamp) {
        if (null != persistedTimestamp) {
            this.persistedTimestamp = (Date) persistedTimestamp.clone();
        } else {
            this.persistedTimestamp = null;
        }

    }

    public String getRealmClassName() {
        if (this.realmClassName == null) {
            return "org.wso2.micro.integrator.security.user.core.common.DefaultRealm";
        }
        return realmClassName;
    }

    public void setRealmClassName(String realmClassName) {
        this.realmClassName = realmClassName;
    }

    public void addMultipleCredentialProperties(String userStoreClass,
                                                Map<String, String> multipleCredentialsProperties) {
        multipleCredentialProps.put(userStoreClass, multipleCredentialsProperties);
    }

    public Map<String, Map<String, String>> getMultipleCredentialProps() {
        return multipleCredentialProps;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsOverrideUsernameClaimFromInternalUsername() {
        return isOverrideUsernameClaimFromInternalUsername;
    }

    public void setIsOverrideUsernameClaimFromInternalUsername(String isOverrideUsernameClaimFromInternalUsername) {
        this.isOverrideUsernameClaimFromInternalUsername = isOverrideUsernameClaimFromInternalUsername;
    }
}
