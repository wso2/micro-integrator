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
package org.wso2.carbon.inbound.endpoint.persistence.service;

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
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;

@Component(name = "org.wso2.carbon.inbound.endpoint.persistence.service.InboundEndpointPersistenceServiceDSComponent",
        immediate = true)
public class InboundEndpointPersistenceServiceDSComponent {

    private static final Log log = LogFactory.getLog(InboundEndpointPersistenceServiceDSComponent.class);

    private static Axis2ConfigurationContextService configContextService = null;

    @Activate
    protected void activate(ComponentContext ctx) throws Exception {

        log.debug("Activating Inbound Endpoint Persistence service....!");

        BundleContext bndCtx = ctx.getBundleContext();
        bndCtx.registerService(InboundEndpointPersistenceService.class.getName(),
                               new InboundEndpointPersistenceServiceImpl(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext compCtx) throws Exception {

    }

    @Reference(name = "config.context.service",
            service = Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(Axis2ConfigurationContextService contextService) {

        this.configContextService = contextService;
    }

    protected void unsetConfigurationContextService(Axis2ConfigurationContextService contextService) {

        this.configContextService = null;
    }

    public static Axis2ConfigurationContextService getConfigContextService() {

        return configContextService;
    }
}
