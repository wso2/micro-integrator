/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Parent class for all admin services
 * <p>
 * Note: This has to be extended by Carbon admin services only. Such services should have the
 * "adminService" parameter.
 */
public abstract class AbstractAdmin {

    protected AxisConfiguration axisConfig;
    protected ConfigurationContext configurationContext;

    protected AbstractAdmin() {
        // Need permissions in order to instantiate AbstractAdmin
        MicroIntegratorBaseUtils.checkSecurity();
    }

    protected AbstractAdmin(AxisConfiguration axisConfig) throws Exception {
        this();
        this.axisConfig = axisConfig;
    }

    protected AxisConfiguration getAxisConfig() {
        checkAdminService();
        return (axisConfig != null) ? axisConfig : getConfigContext().getAxisConfiguration();
    }

    protected ConfigurationContext getConfigContext() {
        checkAdminService();
        if (configurationContext != null) {
            return configurationContext;
        }
        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        if (msgContext != null) {
            ConfigurationContext mainConfigContext = msgContext.getConfigurationContext();

            return mainConfigContext;
        } else {
            return CarbonConfigurationContextFactory.getConfigurationContext();
        }
    }

    protected String getTenantDomain() {
        checkAdminService();
        return Constants.SUPER_TENANT_DOMAIN_NAME;
    }

    protected void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        this.axisConfig = configurationContext.getAxisConfiguration();
    }

    protected HttpSession getHttpSession() {
        checkAdminService();
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSession = null;
        if (msgCtx != null) {
            HttpServletRequest request =
                    (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSession = request.getSession();
        }
        return httpSession;
    }

    private void checkAdminService() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        if (msgCtx == null) {
            return;
        }
        AxisService axisService = msgCtx.getAxisService();
        if (axisService.getParameter(Constants.ADMIN_SERVICE_PARAM_NAME) == null) {
            throw new RuntimeException(
                    "AbstractAdmin can only be extended by Carbon admin services. " + getClass().getName()
                            + " is not an admin service. Service name " + axisService.getName()
                            + ". The service should have defined the " + Constants.ADMIN_SERVICE_PARAM_NAME
                            + " parameter");
        }
    }
}
