/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.security.internal;

import org.apache.axis2.engine.AxisConfiguration;
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
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.SecurityConstants;
import org.wso2.micro.integrator.security.config.RealmConfigXMLProcessor;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.micro.integrator.security.user.core.ldap.ReadOnlyLDAPUserStoreManager;

import java.util.Hashtable;

@Component (
        name = "org.wso2.micro.integrator.security",
        immediate = true
)
public class ServiceComponent {

    private static String POX_SECURITY_MODULE = "POXSecurityModule";
    private static final String DB_CHECK_SQL = "select * from UM_SYSTEM_USER";

    private static Log log = LogFactory.getLog(ServiceComponent.class);
    //to track whether this is the first time initialization of the pack.
    private static boolean isFirstInitialization = true;

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            engagePoxSecurity();
            String lazyInit = System.getProperty(SecurityConstants.MI_SECURITY_USERMGT_LAZY_INIT);
            if (lazyInit == null || Boolean.parseBoolean(lazyInit)) {
                log.debug("Initializing Security parameters lazily");
            } else {
                log.debug("Initializing Security parameters eagerly");
                initSecurityParams();
            }
        } catch (Throwable e) {
            log.error("Failed to activate Micro Integrator security bundle ", e);
        }
    }

    private void engagePoxSecurity() {
        try {
            String enablePoxSecurity = CarbonServerConfigurationService.getInstance()
                    .getFirstProperty("EnablePoxSecurity");
            if (enablePoxSecurity == null || "true".equals(enablePoxSecurity)) {
                AxisConfiguration mainAxisConfig = DataHolder.getInstance().getConfigCtx().getAxisConfiguration();
                // Check for the module availability
                if (mainAxisConfig.getModules().toString().contains(POX_SECURITY_MODULE)){
                    mainAxisConfig.engageModule(POX_SECURITY_MODULE);
                    log.debug("UT Security is activated");
                } else {
                    log.error("UT Security is not activated UTsecurity.mar is not available");
                }
            } else {
                log.debug("POX Security Disabled");
            }
        } catch (Throwable e) {
            log.error("Failed to activate Micro Integrator UT security module ", e);
        }
    }

    /**
     * This function will initialize security parameters
     */
    public static synchronized void initSecurityParams() throws UserStoreException {

        DataHolder dataHolder = DataHolder.getInstance();
        if (dataHolder.getRealmConfig() == null || dataHolder.getUserStoreManager() == null) {
            log.info("Initializing Security parameters");
            RealmConfiguration config = RealmConfigXMLProcessor.createRealmConfig();
            if (config == null) {
                throw new UserStoreException("Unable to create Realm Configuration");
            }
            dataHolder.setRealmConfig(config);

            UserStoreManager userStoreManager;
            String userStoreMgtClassStr = config.getUserStoreClass();
            // In MI there are only two user store managers by default. Hence just check that and create them. If
            // there is a custom user store manager, we have to perform class loading
            switch (userStoreMgtClassStr) {
                case SecurityConstants.DEFAULT_LDAP_USERSTORE_MANAGER:
                    userStoreManager = new ReadOnlyLDAPUserStoreManager(config, null, null);
                    break;
                case SecurityConstants.DEFAULT_JDBC_USERSTORE_MANAGER:
                    userStoreManager = new JDBCUserStoreManager(config, new Hashtable<>(), null, null, null,
                            Constants.SUPER_TENANT_ID, false);
                    break;
                default:
                    userStoreManager =
                            (UserStoreManager) MicroIntegratorSecurityUtils.createObjectWithOptions(userStoreMgtClassStr, config);
                    break;
            }
            dataHolder.setUserStoreManager(userStoreManager);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Micro Integrator Security bundle is deactivated ");
    }

    @Reference(name = "config.context.service",
            service = Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContext")
    protected void setConfigurationContext(Axis2ConfigurationContextService configCtx) {
        DataHolder.getInstance().setConfigCtx(configCtx.getServerConfigContext());
    }

    protected void unsetConfigurationContext(Axis2ConfigurationContextService configCtx) {
        // Nothing to do here yet
    }
}
