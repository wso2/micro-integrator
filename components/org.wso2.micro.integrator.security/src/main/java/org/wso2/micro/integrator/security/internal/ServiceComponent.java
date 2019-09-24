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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
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
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;
import org.wso2.micro.integrator.security.MicroIntegratorSecurityUtils;
import org.wso2.micro.integrator.security.callback.DefaultPasswordCallback;

@Component (
        name = "org.wso2.micro.integrator.security.internal.ServiceComponent",
        immediate = true
)
public class ServiceComponent {

    private static String POX_SECURITY_MODULE = "POXSecurityModule";

    private static Log log = LogFactory.getLog(ServiceComponent.class);

    private ConfigurationContext configCtx;

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            setSecurityParams();
            engagePoxSecurity();
        } catch (Throwable e) {
            log.error("Failed to activate Micro Integrator security bundle ", e);
        }
    }

    private void engagePoxSecurity() {
        try {
            String enablePoxSecurity = CarbonServerConfigurationService.getInstance()
                    .getFirstProperty("EnablePoxSecurity");
            if (enablePoxSecurity == null || "true".equals(enablePoxSecurity)) {
                AxisConfiguration mainAxisConfig = configCtx.getAxisConfiguration();
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

    private void setSecurityParams() {
        AxisConfiguration axisConfig = this.configCtx.getAxisConfiguration();

        Parameter passwordCallbackParam = new Parameter();
        DefaultPasswordCallback passwordCallbackClass = new DefaultPasswordCallback();
        passwordCallbackParam.setName("passwordCallbackRef");
        passwordCallbackParam.setValue(passwordCallbackClass);

        try {
            axisConfig.addParameter(passwordCallbackParam);
        } catch (AxisFault axisFault) {
            log.error("Failed to set axis configuration parameter ", axisFault);
        }

        DataHolder dataHolder = DataHolder.getInstance();

        RealmConfiguration config = passwordCallbackClass.getRealmConfig();
        dataHolder.setRealmConfig(config);

        try {
            UserStoreManager userStoreManager = (UserStoreManager) MicroIntegratorSecurityUtils.
                    createObjectWithOptions(config.getUserStoreClass(), config);
            dataHolder.setUserStoreManager(userStoreManager);
        } catch (UserStoreException e) {
            log.error("Error on initializing User Store Manager Class", e);
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
        this.configCtx = configCtx.getServerConfigContext();
    }

    protected void unsetConfigurationContext(Axis2ConfigurationContextService configCtx) {
        this.configCtx = null;
    }
}
