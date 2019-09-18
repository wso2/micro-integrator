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
package org.wso2.micro.integrator.security.user.core.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.integrator.security.user.core.listener.AuthorizationManagerListener;
import org.wso2.micro.integrator.security.user.core.listener.ClaimManagerListener;
import org.wso2.micro.integrator.security.user.core.listener.UserManagementErrorEventListener;
import org.wso2.micro.integrator.security.user.core.listener.UserOperationEventListener;
import org.wso2.micro.integrator.security.user.core.listener.UserStoreManagerListener;
import org.wso2.micro.integrator.security.user.core.tenant.LDAPTenantManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

//@Component(name = "org.wso2.micro.integrator.security.user.core.listener", immediate = true)
public class UMListenerServiceComponent {

    private static Map<Integer, AuthorizationManagerListener> authorizationManagerListeners;
    private static Map<Integer, UserStoreManagerListener> userStoreManagerListeners;
    private static Map<Integer, UserOperationEventListener> userOperationEventListeners;
    private static Map<Integer, ClaimManagerListener> claimManagerListeners;
    private static Map<Integer, UserManagementErrorEventListener> userManagementErrorEventListeners;
    private static Collection<AuthorizationManagerListener> authorizationManagerListenerCollection;
    private static Collection<UserStoreManagerListener> userStoreManagerListenerCollection;
    private static Collection<UserOperationEventListener> userOperationEventListenerCollection;
    private static Collection<ClaimManagerListener> claimManagerListenerCollection;
    private static Map<Integer, LDAPTenantManager> tenantManagers;
    private static Collection<UserManagementErrorEventListener> userManagementErrorEventListenerCollection;

//    @Reference(name = "authorization.manager.listener.service", cardinality = ReferenceCardinality.MULTIPLE,
//            policy = ReferencePolicy.DYNAMIC, unbind = "unsetAuthorizationManagerListenerService")
    protected synchronized void setAuthorizationManagerListenerService(
            AuthorizationManagerListener authorizationManagerListenerService) {
        authorizationManagerListenerCollection = null;
        if (authorizationManagerListeners == null) {
            authorizationManagerListeners =
                    new TreeMap<Integer, AuthorizationManagerListener>();
        }
        authorizationManagerListeners.put(authorizationManagerListenerService.getExecutionOrderId(),
                authorizationManagerListenerService);
    }

    protected synchronized void unsetAuthorizationManagerListenerService(
            AuthorizationManagerListener authorizationManagerListenerService) {
        if (authorizationManagerListenerService != null
                && authorizationManagerListeners != null) {
            authorizationManagerListeners.remove(
                    authorizationManagerListenerService.getExecutionOrderId());
            authorizationManagerListenerCollection = null;
        }
    }

//    @Reference(name = "user.store.manager.listener.service", cardinality = ReferenceCardinality.MULTIPLE,
//            policy = ReferencePolicy.DYNAMIC, unbind = "unsetUserStoreManagerListenerService")
    protected synchronized void setUserStoreManagerListenerService(
            UserStoreManagerListener userStoreManagerListenerService) {
        userStoreManagerListenerCollection = null;
        if (userStoreManagerListeners == null) {
            userStoreManagerListeners =
                    new TreeMap<Integer, UserStoreManagerListener>();
        }
        userStoreManagerListeners.put(userStoreManagerListenerService.getExecutionOrderId(),
                userStoreManagerListenerService);
    }

    protected synchronized void unsetUserStoreManagerListenerService(
            UserStoreManagerListener userStoreManagerListenerService) {
        if (userStoreManagerListenerService != null &&
                userStoreManagerListeners != null) {
            userStoreManagerListeners.remove(userStoreManagerListenerService.getExecutionOrderId());
            userStoreManagerListenerCollection = null;
        }
    }

    /**
     * Register UserManagementErrorEventListeners.
     *
     * @param userManagementErrorEventListenerService Relevant UserManagementErrorEventListenerService that need to
     *                                                be registered.
     */
//    @Reference(name = "user.management.error.event.listener.service", cardinality = ReferenceCardinality.MULTIPLE,
//            policy = ReferencePolicy.DYNAMIC, unbind = "unsetUserManagementErrorEventListenerService")
    protected synchronized void setUserManagementErrorEventListenerService(
            UserManagementErrorEventListener userManagementErrorEventListenerService) {

        userManagementErrorEventListenerCollection = null;
        if (userManagementErrorEventListeners == null) {
            userManagementErrorEventListeners = new TreeMap<>();
        }
        userManagementErrorEventListeners.put(userManagementErrorEventListenerService.getExecutionOrderId(),
                userManagementErrorEventListenerService);
    }

    /**
     * Un-register UserManagementErrorEventListeners.
     *
     * @param userManagementErrorEventListener Relevant UserManagementErrorEventListenerService that need to be
     *                                         un-registered.
     */
    protected synchronized void unsetUserManagementErrorEventListenerService(
            UserManagementErrorEventListener userManagementErrorEventListener) {

        if (userManagementErrorEventListener != null && userManagementErrorEventListeners != null) {
            userManagementErrorEventListeners.remove(userManagementErrorEventListener.getExecutionOrderId());
            userManagementErrorEventListenerCollection = null;
        }
    }

//    @Reference(name = "user.operation.event.listener.service", cardinality = ReferenceCardinality.MULTIPLE,
//            policy = ReferencePolicy.DYNAMIC, unbind = "unsetUserOperationEventListenerService")
    protected synchronized void setUserOperationEventListenerService(
            UserOperationEventListener userOperationEventListenerService) {
        userOperationEventListenerCollection = null;
        if (userOperationEventListeners == null) {
            userOperationEventListeners = new TreeMap<Integer, UserOperationEventListener>();
        }
        userOperationEventListeners.put(userOperationEventListenerService.getExecutionOrderId(),
                userOperationEventListenerService);
    }

    protected synchronized void unsetUserOperationEventListenerService(
            UserOperationEventListener userOperationEventListenerService) {
        if (userOperationEventListenerService != null &&
                userOperationEventListeners != null) {
            userOperationEventListeners.remove(userOperationEventListenerService.getExecutionOrderId());
            userOperationEventListenerCollection = null;
        }
    }

//    @Reference(name = "claim.manager.listener.service", cardinality = ReferenceCardinality.MULTIPLE,
//            policy = ReferencePolicy.DYNAMIC, unbind = "unsetClaimManagerListenerService")
    protected synchronized void setClaimManagerListenerService(
            ClaimManagerListener claimManagerListenerService) {
        claimManagerListenerCollection = null;
        if (claimManagerListeners == null) {
            claimManagerListeners = new TreeMap<Integer, ClaimManagerListener>();
        }
        claimManagerListeners.put(claimManagerListenerService.getExecutionOrderId(),
                claimManagerListenerService);
    }

    protected synchronized void unsetClaimManagerListenerService(
            ClaimManagerListener claimManagerListenerService) {
        if (claimManagerListenerService != null &&
                claimManagerListeners != null) {
            claimManagerListeners.remove(claimManagerListenerService.getExecutionOrderId());
            claimManagerListenerCollection = null;
        }
    }


    public static synchronized Collection<AuthorizationManagerListener> getAuthorizationManagerListeners() {
        if (authorizationManagerListeners == null) {
            authorizationManagerListeners = new TreeMap<Integer, AuthorizationManagerListener>();
        }
        if (authorizationManagerListenerCollection == null) {
            authorizationManagerListenerCollection =
                    authorizationManagerListeners.values();
        }
        return authorizationManagerListenerCollection;
    }

    public static synchronized Collection<UserStoreManagerListener> getUserStoreManagerListeners() {
        if (userStoreManagerListeners == null) {
            userStoreManagerListeners = new TreeMap<Integer, UserStoreManagerListener>();
        }
        if (userStoreManagerListenerCollection == null) {
            userStoreManagerListenerCollection =
                    userStoreManagerListeners.values();
        }
        return userStoreManagerListenerCollection;
    }

    /**
     * To get the UserManagementErrorEventListeners that are registered for handling error.
     *
     * @return relevant UserManagementErrorEventListeners that are registered in the current environment.
     */
    public static synchronized Collection<UserManagementErrorEventListener> getUserManagementErrorEventListeners() {

        if (userManagementErrorEventListeners == null) {
            userManagementErrorEventListeners = new TreeMap<>();
        }
        if (userManagementErrorEventListenerCollection == null) {
            userManagementErrorEventListenerCollection = userManagementErrorEventListeners.values();
        }
        return userManagementErrorEventListenerCollection;
    }
    
    /*protected void setCacheInvalidator(CacheInvalidator invalidator) {
        cacheInvalidator = invalidator;
    }
    
    protected void removeCacheInvalidator(CacheInvalidator invalidator) {
    	cacheInvalidator = null;
    }

    public static CacheInvalidator getCacheInvalidator() {
    	return cacheInvalidator;
    }*/

    public static synchronized Collection<UserOperationEventListener> getUserOperationEventListeners() {
        if (userOperationEventListeners == null) {
            userOperationEventListeners = new TreeMap<Integer, UserOperationEventListener>();
        }
        if (userOperationEventListenerCollection == null) {
            userOperationEventListenerCollection =
                    userOperationEventListeners.values();
        }
        return userOperationEventListenerCollection;
    }

    public static synchronized Collection<ClaimManagerListener> getClaimManagerListeners() {
        if (claimManagerListeners == null) {
            claimManagerListeners = new TreeMap<Integer, ClaimManagerListener>();
        }
        if (claimManagerListenerCollection == null) {
            claimManagerListenerCollection =
                    claimManagerListeners.values();
        }
        return claimManagerListenerCollection;
    }

    /**
     * Main purpose of this method is to make a dependency to LDAP server component.
     * Then LDAP server bundle will get started before user core.
     * In addition this method can be used to register all tenant managers.
     *
     * @param tenantManager An implementation of LDAPTenantManager.
     */
//    @Reference(name = "ldap.tenant.manager.listener.service", cardinality = ReferenceCardinality.MULTIPLE,
//            policy = ReferencePolicy.DYNAMIC, unbind = "removeLDAPTenantManager")
    protected synchronized void addLDAPTenantManager(LDAPTenantManager tenantManager) {

        if (tenantManagers == null) {
            tenantManagers = new HashMap<Integer, LDAPTenantManager>();
        }

        tenantManagers.put(tenantManager.hashCode(), tenantManager);

    }

    /**
     * This method will remove an already registered tenant manager.
     *
     * @param tenantManager An implementation of LDAPTenantManager.
     */
    protected synchronized void removeLDAPTenantManager(LDAPTenantManager tenantManager) {

        if (tenantManagers != null && tenantManagers.containsKey(tenantManager.hashCode())) {
            tenantManagers.remove(tenantManager.hashCode());
        }
    }

    /**
     * Returns all registered tenant managers.
     *
     * @return A map of tenant managers with their hash codes.
     */
    public static Map<Integer, LDAPTenantManager> getTenantManagers() {
        return tenantManagers;
    }


}
