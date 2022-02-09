/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.initializer.deployment.user.store.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.core.common.UserStoreDeploymentManager;

/**
 * Deploy user store configurations.
 */
public class UserStoreDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(UserStoreDeployer.class);
    private AxisConfiguration axisConfig;

    /**
     * Data Services repository directory
     */
    private String repoDir;

    /**
     * Data Services file directory (i.e. '.dbs')
     */
    private String extension;

    @Override
    public void init(ConfigurationContext configurationContext) {
        log.info("User Store Configuration Deployer initiated.");
        this.axisConfig = configurationContext.getAxisConfiguration();
    }

    @Override
    public void setDirectory(String repoDir) {
        this.repoDir = repoDir;
    }

    @Override
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Trigger deploying of new org.wso2.carbon.identity.user.store.configuration file
     *
     * @param deploymentFileData information about the user store org.wso2.carbon.identity.user.store.configuration
     * @throws org.apache.axis2.deployment.DeploymentException for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        if (deploymentFileData.getName().startsWith("FEDERATED")) {
            throw new DeploymentException("'FEDERATED' is a reserved user store domain prefix. "
                    + "Please start the file name with a different domain name.");
        }

        UserStoreDeploymentManager userStoreDeploymentManager = new UserStoreDeploymentManager();
        userStoreDeploymentManager.deploy(deploymentFileData.getAbsolutePath());
    }
}
