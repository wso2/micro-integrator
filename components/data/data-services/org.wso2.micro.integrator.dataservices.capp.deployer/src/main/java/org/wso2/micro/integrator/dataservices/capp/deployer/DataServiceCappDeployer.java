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
package org.wso2.micro.integrator.dataservices.capp.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.application.deployer.AppDeployerConstants;
import org.wso2.micro.application.deployer.CarbonApplication;
import org.wso2.micro.application.deployer.config.ApplicationConfiguration;
import org.wso2.micro.application.deployer.config.Artifact;
import org.wso2.micro.application.deployer.config.CappFile;
import org.wso2.micro.application.deployer.handler.AppDeploymentHandler;

/**
 * This class is the implementation of the data service deployer.
 */
public class DataServiceCappDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(DataServiceCappDeployer.class);
    private static final String DS_TYPE = "service/dataservice";
    private static final String DS_DIR = "dataservices";

    /**
     * Deploy the data service artifacts and add them to data services.
     *
     * @param carbonApp  find artifacts from this CarbonApplication instance.
     * @param axisConfig AxisConfiguration of the current tenant.
     */
    @Override
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("Deploying data services of carbon application - " + carbonApp.getAppName());
        }
        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> dependencies = appConfig.getApplicationArtifact().getDependencies();

        List<Artifact> artifacts = new ArrayList<>();
        for (Artifact.Dependency dependency : dependencies) {
            if (dependency.getArtifact() != null) {
                artifacts.add(dependency.getArtifact());
            }
        }
        deployDataSources(artifacts, axisConfig);
    }

    /**
     * Un-deploy the data services and remove them from data services.
     *
     * @param carbonApp  find artifacts from this CarbonApplication instance.
     * @param axisConfig AxisConfiguration of the current tenant.
     */
    @Override
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("Un-Deploying data services of carbon application - " + carbonApp.getAppName());
        }
        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> dependencies = appConfig.getApplicationArtifact().getDependencies();

        List<Artifact> artifacts = new ArrayList<>();
        for (Artifact.Dependency dependency : dependencies) {
            if (dependency.getArtifact() != null) {
                artifacts.add(dependency.getArtifact());
            }
        }
        undeployDataSources(artifacts, axisConfig);
    }

    /**
     * Deploy data services.
     * adding the data service to the data services.
     * there can be multiple data sources as separate xml files.
     *
     * @param artifacts  list of artifacts to be deployed.
     * @param axisConfig axis configuration.
     */
    private void deployDataSources(List<Artifact> artifacts, AxisConfiguration axisConfig)
            throws DeploymentException {
        for (Artifact artifact : artifacts) {
            if (DS_TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files == null || files.isEmpty()) {
                    throw new DeploymentException("DataServiceCappDeployer::deployDataServices --> "
                                                          + "Error No data services found in the artifact to deploy");
                }
                for (CappFile cappFile : files) {
                    String fileName = cappFile.getName();
                    String dataServiceConfigPath = artifact.getExtractedPath() + File.separator + fileName;

                    File file = new File(dataServiceConfigPath);
                    if (!file.exists()) {
                        throw new DeploymentException("DataServiceCappDeployer::deployDataServices --> "
                                                              + "Error Data service file cannot be found in artifact, "
                                                              + "file name - " + fileName);
                    }
                    // access the deployment engine through axis config
                    DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
                    Deployer deployer = deploymentEngine.getDeployer(DS_DIR, "dbs");

                    try {
                        // Call the deploy method of the deployer
                        deployer.deploy(new DeploymentFileData(new File(dataServiceConfigPath), deployer));
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw new DeploymentException(
                                "DataServiceCappDeployer::deployDataServices --> "
                                        + "Error in deploying data service: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Un-deploy data services.
     * removing the data service from data services.
     * there can be multiple data sources as separate xml files.
     *
     * @param artifacts  list of artifacts to be deployed.
     * @param axisConfig axis configuration.
     */
    private void undeployDataSources(List<Artifact> artifacts, AxisConfiguration axisConfig)
            throws DeploymentException {
        for (Artifact artifact : artifacts) {
            if (DS_TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files == null || files.isEmpty()) {
                    throw new DeploymentException("DataServiceCappDeployer::unDeployDataServices --> "
                                                          + "Error No data services found in the artifact to deploy");
                }
                for (CappFile cappFile : files) {
                    String fileName = cappFile.getName();
                    String dataServiceConfigPath = artifact.getExtractedPath() + File.separator + fileName;

                    File file = new File(dataServiceConfigPath);
                    if (!file.exists()) {
                        throw new DeploymentException("DataServiceCappDeployer::unDeployDataServices --> "
                                                              + "Error Data service file cannot be found in artifact, "
                                                              + "file name - " + fileName);
                    }

                    // access the deployment engine through axis config
                    DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
                    Deployer deployer = deploymentEngine.getDeployer(DS_DIR, "dbs");

                    if (AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.equals(
                            artifact.getDeploymentStatus())) {
                        try {
                            // Call the un-deploy method of the deployer
                            deployer.undeploy(dataServiceConfigPath);
                            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                            File artifactFile = new File(dataServiceConfigPath);
                            if (artifactFile.exists() && !artifactFile.delete()) {
                                log.warn("Couldn't delete artifact file : " + dataServiceConfigPath);
                            }
                        } catch (DeploymentException e) {
                            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                            throw new DeploymentException(
                                    "DataServiceCappDeployer::unDeployDataServices --> "
                                            + "Error in un-deploying data service: " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }
}
