/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.initializer.deployment.application.deployer;

import java.io.File;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.application.deployer.AppDeployerUtils;
import org.wso2.micro.application.deployer.CarbonApplication;
import org.wso2.micro.core.util.FileManipulator;

public class CappDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(CappDeployer.class);

    private AxisConfiguration axisConfig;

    /**
     * Carbon application repository directory
     */
    private String cAppDir;

    /**
     * Carbon application file directory (i.e. '.car')
     */
    private String extension;

    public void init(ConfigurationContext configurationContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Capp Deployer..");
        }
        this.axisConfig = configurationContext.getAxisConfiguration();

        //delete the older extracted capps for this tenant.
        String appUnzipDir = AppDeployerUtils.getAppUnzipDir() + File.separator +
                AppDeployerUtils.getTenantIdString();
        FileManipulator.deleteDir(appUnzipDir);
    }

    /**
     * Axis2 deployment engine will call this method when a .car archive is deployed. So we only have to call the
     * cAppDeploymentManager to deploy it using the absolute path of the deployed .car file.
     *
     * @param deploymentFileData - info about the deployed file
     * @throws DeploymentException - error while deploying cApp
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String artifactPath = deploymentFileData.getAbsolutePath();
        try {
            CAppDeploymentManager.getInstance().deploy(artifactPath, axisConfig);
        } catch (Exception e) {
            log.error("Error while deploying carbon application " + artifactPath, e);
        }

        super.deploy(deploymentFileData);

    }

    public void setDirectory(String cAppDir) {
        this.cAppDir = cAppDir;
    }

    public String getDirectory() {
        return cAppDir;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Undeploys the cApp from system when the .car file is deleted from the repository. Find the relevant cApp using
     * the file path and call the undeploy method on applicationManager.
     *
     * @param filePath - deleted .car file path
     * @throws DeploymentException - error while un-deploying cApp
     */
    public void undeploy(String filePath) throws DeploymentException {
        String artifactPath = AppDeployerUtils.formatPath(filePath);
        CarbonApplication existingApp = null;
        for (CarbonApplication carbonApp : CAppDeploymentManager.getCarbonApps()) {
            if (artifactPath.equals(carbonApp.getAppFilePath())) {
                existingApp = carbonApp;
                break;
            }
        }
        if (existingApp != null) {
            CAppDeploymentManager.getInstance().undeployCarbonApp(existingApp, axisConfig);
        } else {
            log.info("Undeploying Faulty Carbon Application On : " + filePath);
            CAppDeploymentManager.getInstance().removeFaultyCarbonApp(filePath);
        }
        super.undeploy(filePath);
    }

    public void cleanup() {
        //cleanup the capp list during the unload
        CAppDeploymentManager.getInstance().cleanupCarbonApps();
    }
}
