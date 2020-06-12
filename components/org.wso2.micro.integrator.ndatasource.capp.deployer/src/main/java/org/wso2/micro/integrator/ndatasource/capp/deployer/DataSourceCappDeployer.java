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
package org.wso2.micro.integrator.ndatasource.capp.deployer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.micro.application.deployer.AppDeployerConstants;
import org.wso2.micro.application.deployer.CarbonApplication;
import org.wso2.micro.application.deployer.config.ApplicationConfiguration;
import org.wso2.micro.application.deployer.config.Artifact;
import org.wso2.micro.application.deployer.config.CappFile;
import org.wso2.micro.application.deployer.handler.AppDeploymentHandler;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.DataSourceManager;
import org.wso2.micro.integrator.ndatasource.core.DataSourceMetaInfo;
import org.wso2.micro.integrator.ndatasource.core.utils.DataSourceUtils;

/**
 * This class is the implementation of the data source deployer which will deploy data sources to the server.
 */
public class DataSourceCappDeployer implements AppDeploymentHandler {
    private static final Log log = LogFactory.getLog(DataSourceCappDeployer.class);

    public static final String DATA_SOURCE_TYPE = "datasource/datasource";

    /**
     * Deploy the data source artifacts and add them to datasources.
     *
     * @param carbonApp  - store info in this object after deploying
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    @Override
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("Deploying carbon application - " + carbonApp.getAppName());
        }
        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> deps = appConfig.getApplicationArtifact().getDependencies();

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        deployUnDeployDataSources(true, artifacts);
    }

    /**
     * Un-deploy the data sources and remove them from datasources.
     *
     * @param carbonApp  - all information about the existing artifacts are in this instance.
     * @param axisConfig - AxisConfiguration of the current tenant.
     */
    @Override
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("Un-Deploying carbon application - " + carbonApp.getAppName());
        }
        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> deps = appConfig.getApplicationArtifact().getDependencies();

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        deployUnDeployDataSources(false, artifacts);
    }

    /**
     * Deploy or un-deploy data sources. if deploying, adding the data source to the data sources and if undeploying,
     * removing the data source from data sources. there can be multiple data sources as separate xml files.
     *
     * @param deploy    - identify whether deployment process or un-deployment process.
     * @param artifacts - list of artifacts to be deployed.
     */
    private void deployUnDeployDataSources(boolean deploy, List<Artifact> artifacts) throws DeploymentException {
        for (Artifact artifact : artifacts) {
            if (DATA_SOURCE_TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files == null || files.isEmpty()) {
                    throw new DeploymentException("DataSourceCappDeployer::deployUnDeployDataSources --> " +
                                                  "Error No data sources found in the artifact to deploy");
                }
                for (CappFile cappFile : files) {
                    String fileName = cappFile.getName();
                    String dataSourceConfigPath = artifact.getExtractedPath() + File.separator + fileName;

                    File file = new File(dataSourceConfigPath);
                    if (!file.exists()) {
                        throw new DeploymentException("DataSourceCappDeployer::deployUnDeployDataSources --> " +
                                                      "Error Data source file cannot be found in artifact, " +
                                                      "file name - " + fileName);
                    }
                    DataSourceMetaInfo dataSourceMetaInfo = readDataSourceFile(file);
                    /**
                     * If some artifact fail to deploy, then whole car file will fail, then it
                     * will invoke un-deploy flow for the car file so that should only un-deploy
                     * artifacts which are already deployed. To identify that we use deployment
                     * status of the artifact
                     */
                    if (deploy) {
                        try {
                            if (DataSourceManager.getInstance().getDataSourceRepository().getDataSource(
                                    dataSourceMetaInfo.getName()) != null) {
                                artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                                throw new DeploymentException("DataSourceCappDeployer::deployUnDeployDataSources --> " +
                                                              "Error in deploying data source: data source " +
                                                              "with same name already exist, " +
                                                              "data source name - " + dataSourceMetaInfo.getName());
                            }
                            dataSourceMetaInfo.setCarbonApplicationDeployed(true);
                            DataSourceManager.getInstance().getDataSourceRepository().addDataSource(dataSourceMetaInfo);
                            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                        } catch (DataSourceException e) {
                            throw new DeploymentException("DataSourceCappDeployer::deployUnDeployDataSources --> " +
                                                          "Error in deploying data source: " + e.getMessage(), e);
                        }
                    } else {
                        try {
                            if (DataSourceManager.getInstance().getDataSourceRepository().getDataSource(
                                    dataSourceMetaInfo.getName()) != null && artifact.getDeploymentStatus().equals(
                                    AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED)) {
                                DataSourceManager.getInstance().getDataSourceRepository().deleteDataSource(
                                        dataSourceMetaInfo.getName());
                            }
                        } catch (DataSourceException e) {
                            throw new DeploymentException("DataSourceCappDeployer::deployUnDeployDataSources --> " +
                                                          "Error in undeploying data source: " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to read data source file and create the object from it.
     *
     * @param file - xml file
     * @return - dataSourceMetaInfo object which is created using the xml file.
     * @throws - org.apache.axis2.deployment.DeploymentException.
     */
    private DataSourceMetaInfo readDataSourceFile(File file) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("Reading data source file from car file - " + file.getName());
        }
        try {
            OMElement doc = DataSourceUtils.convertToOMElement(file);
            DataSourceUtils.secureResolveOMElement(doc);
            // create JAXB context and initializing Marshaller
            JAXBContext jaxbContext = JAXBContext.newInstance(DataSourceMetaInfo.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // this will create Java object - data source from xml file
            DataSourceMetaInfo dataSourceMetaInfo =
                    (DataSourceMetaInfo) jaxbUnmarshaller.unmarshal(doc.getXMLStreamReader());
            return dataSourceMetaInfo;
        } catch (JAXBException e) {
            throw new DeploymentException("DataSourceCappDeployer::readDataSourceFile --> " +
                                          "Error in reading data source file: " + e.getMessage(), e);
        } catch (DataSourceException e) {
            throw new DeploymentException("DataSourceCappDeployer::readDataSourceFile --> " +
                                          "Error in decrypting data source file: " + e.getMessage(), e);
        }
    }
}
