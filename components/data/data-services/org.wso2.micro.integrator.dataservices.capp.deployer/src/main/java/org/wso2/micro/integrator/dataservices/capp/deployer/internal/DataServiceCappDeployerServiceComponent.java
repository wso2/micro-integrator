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
package org.wso2.micro.integrator.dataservices.capp.deployer.internal;

import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.application.deployer.handler.AppDeploymentHandler;
import org.wso2.micro.integrator.dataservices.capp.deployer.DataServiceCappDeployer;
import org.wso2.micro.integrator.ndatasource.capp.deployer.DataSourceCappDeployer;

@Component(name="org.wso2.micro.integrator.dataservices.capp.deployer", immediate = true)
public class DataServiceCappDeployerServiceComponent {

    private static final Log log = LogFactory.getLog(DataServiceCappDeployerServiceComponent.class);
    private ComponentContext ctx;
    private AppDeploymentHandler appDepHandler;

    protected synchronized void activate(ComponentContext ctx) {
        this.ctx = ctx;
        if (Objects.nonNull(appDepHandler)) {
            registerDataServiceCappDeployer();
        }
        if (log.isDebugEnabled()) {
            log.debug("Data Service Capp deployer activated");
        }
    }

    protected synchronized void deactivate(ComponentContext ctx) {
        this.ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Data Service Capp deployer deactivated");
        }
    }

    @Reference(
            name = "org.wso2.micro.application.deployer.handler",
            service = org.wso2.micro.application.deployer.handler.AppDeploymentHandler.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataServiceCappDeployer")
    protected void setDataServiceCappDeployer(AppDeploymentHandler appDeploymentHandler) {
        if (appDeploymentHandler instanceof DataSourceCappDeployer) {
            if (Objects.isNull(ctx)) {
                // save appDeploymentHandler
                appDepHandler = appDeploymentHandler;
            } else {
                registerDataServiceCappDeployer();
            }
        }
    }

    protected void unsetDataServiceCappDeployer(AppDeploymentHandler appDeploymentHandler) {
        if (appDeploymentHandler.equals(appDepHandler)) {
            appDepHandler = null;
        }
    }

    /**
     * Register data source deployer as an OSGi service.
     */
    private void registerDataServiceCappDeployer() {
        try {
            ctx.getBundleContext().registerService(AppDeploymentHandler.class.getName(),
                                                   new DataServiceCappDeployer(), null);
        } catch (Throwable e) {
            log.error("Failed to activate Data Service Capp Deployer", e);
        }
    }
}
