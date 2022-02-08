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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.core.UserStoreTemporaryService;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.UserStoreConfigConstants;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManagerFactory;
import org.wso2.micro.integrator.security.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.micro.integrator.security.user.core.ldap.ActiveDirectoryUserStoreManager;
import org.wso2.micro.integrator.security.user.core.ldap.ReadOnlyLDAPUserStoreManager;
import org.wso2.micro.integrator.security.user.core.ldap.ReadWriteLDAPUserStoreManager;
import org.wso2.micro.integrator.security.user.core.service.RealmService;
import org.wso2.micro.integrator.security.user.core.tracker.UserStoreManagerRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component(name = "user.store.mgt.dscomponent", immediate = true)
public class UserStoreMgtDSComponent {
    private static Log log = LogFactory.getLog(UserStoreMgtDSComponent.class);
    private static RealmService realmService;
    private static CarbonServerConfigurationService serverConfigurationService = null;
    private static ClaimManagerFactory claimManagerFactory = null;

    public static RealmService getRealmService() {
        return realmService;
    }

    @Reference(name = "user.realmservice.default", cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetRealmService")
    protected void setRealmService(RealmService rlmService) {
        realmService = rlmService;
    }

    public static CarbonServerConfigurationService getServerConfigurationService() {
        return UserStoreMgtDSComponent.serverConfigurationService;
    }

    @Reference(name = "server.configuration.service", cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(CarbonServerConfigurationService serverConfigurationService) {
        UserStoreMgtDSComponent.serverConfigurationService = serverConfigurationService;
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        if (Boolean.parseBoolean(System.getProperty("NonUserCoreMode"))) {
            log.debug("UserCore component activated in NonUserCoreMode Mode");
            return;
        }
        try {
            // We assume this component gets activated by super tenant
            UserStoreManager jdbcUserStoreManager = new JDBCUserStoreManager();
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), jdbcUserStoreManager, null);

            UserStoreManager readWriteLDAPUserStoreManager = new ReadWriteLDAPUserStoreManager();
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), readWriteLDAPUserStoreManager, null);

            UserStoreManager readOnlyLDAPUserStoreManager = new ReadOnlyLDAPUserStoreManager();
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), readOnlyLDAPUserStoreManager, null);

            UserStoreManager activeDirectoryUserStoreManager = new ActiveDirectoryUserStoreManager();
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), activeDirectoryUserStoreManager, null);

            UserStoreManagerRegistry.init(ctxt.getBundleContext());

            // Registering a TemporaryService so that app deployer service component can continue.
            // Micro-integrator initializer should wait for this bundle to be activated.
            UserStoreTemporaryService userStoreTemporaryService = new UserStoreTemporaryService();
            ctxt.getBundleContext().registerService(UserStoreTemporaryService.class.getName(),
                    userStoreTemporaryService, null);

            log.info("Carbon UserStoreMgtDSComponent activated successfully.");
        } catch (Exception e) {
            log.error("Failed to activate Carbon UserStoreMgtDSComponent ", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Carbon UserStoreMgtDSComponent is deactivated ");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        realmService = null;
    }

    protected void unsetServerConfigurationService(CarbonServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ServerConfigurationService");
        }
        UserStoreMgtDSComponent.serverConfigurationService = null;
    }

    public static ClaimManagerFactory getClaimManagerFactory() {
        return UserStoreMgtDSComponent.claimManagerFactory;
    }

    @Reference(name = "claim.mgt.component", cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetClaimManagerFactory")
    protected void setClaimManagerFactory(ClaimManagerFactory claimManagerFactory) {
        this.claimManagerFactory = claimManagerFactory;
        try {
            if (claimManagerFactory.createClaimManager(Constants.SUPER_TENANT_ID) != null) {
                ClaimManager claimManager = claimManagerFactory.createClaimManager(Constants.SUPER_TENANT_ID);
                setClaimManager(realmService.getBootstrapRealm(), claimManager);
                setClaimManager(realmService.getBootstrapRealm().getUserStoreManager(), claimManager);
                RealmConfiguration secondaryRealmConfiguration = realmService.getBootstrapRealm()
                        .getRealmConfiguration().getSecondaryRealmConfig();
                if (secondaryRealmConfiguration != null) {
                    do {
                        String userDomain = secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigConstants
                                .DOMAIN_NAME);
                        setClaimManager(realmService.getBootstrapRealm().getUserStoreManager()
                                .getSecondaryUserStoreManager(userDomain), claimManager);

                        secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();
                    } while (secondaryRealmConfiguration != null);
                }
            }
        } catch (Exception e) {
            log.error("Error while setting claim manager from claim manager factory");
        }

    }

    protected void unsetClaimManagerFactory(ClaimManagerFactory claimManagerFactory) {
        UserStoreMgtDSComponent.claimManagerFactory = null;
    }

    private void setClaimManager(Object object, ClaimManager claimManager) {
        try {
            Class<?> currentClass = object.getClass();
            Method method = null;
            while (currentClass != null && method == null) {
                try {
                    method = currentClass.getDeclaredMethod("setClaimManager", ClaimManager.class);
                } catch (NoSuchMethodException e) {
                    // method not present - try super class
                    currentClass = currentClass.getSuperclass();
                }
            }
            if (method != null) {
                method.setAccessible(true);
                method.invoke(object, claimManager);
                log.info("Claim manager set for " + object.getClass());
                method.setAccessible(false);
            } else {
                throw new NoSuchMethodException();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("No claim manager setter found for " + object.getClass() + " or its supper classes");
        }

    }
}
