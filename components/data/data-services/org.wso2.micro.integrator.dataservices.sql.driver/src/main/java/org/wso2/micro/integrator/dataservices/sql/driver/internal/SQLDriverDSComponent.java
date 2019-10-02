/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.dataservices.sql.driver.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;

public class SQLDriverDSComponent {

    private static Log log = LogFactory.getLog(SQLDriverDSComponent.class);

    private static Axis2ConfigurationContextService configurationContextService = null;

    protected void activate(ComponentContext ctxt) {
    try {
            BundleContext bundleContext = ctxt.getBundleContext();
            log.debug("SQL driver bundle is activated ");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("SQL driver bundle is deactivated ");
    }

    @Reference(
            name = "org.wso2.carbon.dataservices.sql.driver",
            service = org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAxis2ConfigurationContextService")
    protected void setAxis2ConfigurationContextService(Axis2ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Axis2 Configuration Context Service");
        }
        SQLDriverDSComponent.configurationContextService = configurationContextService;
    }

    protected void unsetAxis2ConfigurationContextService(Axis2ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Axis2 Configuration Context Service");
        }
        SQLDriverDSComponent.configurationContextService = null;
    }

    public static Axis2ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }
}
