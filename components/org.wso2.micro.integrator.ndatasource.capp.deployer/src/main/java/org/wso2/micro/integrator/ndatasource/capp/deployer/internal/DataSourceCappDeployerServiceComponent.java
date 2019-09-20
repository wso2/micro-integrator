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
package org.wso2.micro.integrator.ndatasource.capp.deployer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.micro.application.deployer.handler.AppDeploymentHandler;
import org.wso2.micro.integrator.ndatasource.capp.deployer.DataSourceCappDeployer;

/**
 * @scr.component name="org.wso2.carbon.ndatasource.capp.deployer" immediate="true"
 */
public class DataSourceCappDeployerServiceComponent {

    private static final Log log = LogFactory.getLog(DataSourceCappDeployerServiceComponent.class);

    private ComponentContext ctx;

    protected synchronized void activate(ComponentContext ctx) {
        this.ctx = ctx;
        if (log.isDebugEnabled()) {
            log.debug("Data Source Capp deployer activated");
        }
        if (log.isDebugEnabled()) {
            log.debug("Data Source Capp deployer activated");
        }

        try {
            //register data source deployer as an OSGi service
            DataSourceCappDeployer dataSourceDeployer = new DataSourceCappDeployer();
            this.ctx.getBundleContext().registerService(
                    AppDeploymentHandler.class.getName(), dataSourceDeployer, null);
        } catch (Throwable e) {
            log.error("Failed to activate Data Source Capp Deployer", e);
        }
    }

    protected synchronized void deactivate(ComponentContext ctx) {
        this.ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Data Source Capp deployer deactivated");
        }
    }
}
