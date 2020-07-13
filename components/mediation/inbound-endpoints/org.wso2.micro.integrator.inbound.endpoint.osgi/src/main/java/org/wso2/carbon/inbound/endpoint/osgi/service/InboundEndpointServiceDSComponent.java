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

package org.wso2.carbon.inbound.endpoint.osgi.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;

@Component(name = "org.wso2.carbon.inbound.endpoint.osgi.service.InboundEndpointServiceDSComponent",
        immediate = true)
public class InboundEndpointServiceDSComponent {

    private static final Log log = LogFactory.getLog(InboundEndpointServiceDSComponent.class);

    private static SecretCallbackHandlerService secretCallbackHandlerService;

    @Activate
    protected void activate(ComponentContext ctx) throws Exception {

        log.debug("Activating Inbound Endpoint service....!");

        BundleContext bndCtx = ctx.getBundleContext();
        bndCtx.registerService(InboundEndpointService.class.getName(), new InboundEndpointServiceImpl(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext compCtx) throws Exception {

    }

    @Reference(name = "config.context.service",
            service = Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(Axis2ConfigurationContextService configurationContextService) {

        log.debug("ConfigurationContextService bound to the ESB initialization process");

        ServiceReferenceHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(Axis2ConfigurationContextService configurationContextService) {

        log.debug("ConfigurationContextService unbound from the ESB environment");

        ServiceReferenceHolder.getInstance().setConfigurationContextService(null);
    }

    @Reference(
            name = "secret.callback.handler.service",
            service = org.wso2.carbon.securevault.SecretCallbackHandlerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretCallbackHandlerService")
    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {

        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService bound to the ESB initialization process");
        }
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {

        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService unbound from the ESB environment");
        }
        this.secretCallbackHandlerService = null;
    }

    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {

        return secretCallbackHandlerService;
    }
}
