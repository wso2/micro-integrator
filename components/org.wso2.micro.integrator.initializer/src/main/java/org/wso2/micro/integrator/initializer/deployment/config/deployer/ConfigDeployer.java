/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.micro.integrator.initializer.deployment.config.deployer;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.registry.Registry;
import org.wso2.micro.application.deployer.CarbonApplication;
import org.wso2.micro.application.deployer.config.ApplicationConfiguration;
import org.wso2.micro.application.deployer.config.Artifact;
import org.wso2.micro.application.deployer.config.CappFile;
import org.wso2.micro.application.deployer.handler.AppDeploymentHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ConfigDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(ConfigDeployer.class);

    private static final String PROPERTY_TYPE = "config/property";

    private static final String PROPERTY_FILE_NAME = "config.properties";

    public static final char URL_SEPARATOR_CHAR = '/';

    public ConfigDeployer() {
    }

    @Override
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("Deploying properties  - " + carbonApp.getAppName());
        }
        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> deps = appConfig.getApplicationArtifact().getDependencies();

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        deployConfigArtifacts(artifacts, carbonApp.getAppNameWithVersion());
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) throws DeploymentException {

    }

    private void deployConfigArtifacts(List<Artifact> artifacts, String parentAppName) {
        artifacts.stream().filter(artifact -> PROPERTY_TYPE.equals(artifact.getType())).forEach(artifact -> {
            if (log.isDebugEnabled()) {
                log.debug("Deploying config artifact: " + artifact.getName());
            }
            writePropertyToConf(artifact, parentAppName);
        });
    }

    private void writePropertyToConf(Artifact artifact, String appName) {
        // get the file path of the registry config file
        List<CappFile> files = artifact.getFiles();
        if (files.size() == 1) {
            String configPath = artifact.getExtractedPath() + File.separator + PROPERTY_FILE_NAME;
            File regConfigFile = new File(configPath);
            if (regConfigFile.exists()) {
                try {
                    Files.copy(Paths.get(configPath), Paths.get(getHome(), "conf", PROPERTY_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                log.error("Config file not found at : " + configPath);
            }
        } else {
            log.error("config/property type must have a single file which declares " +
                    "config. But " + files.size() + " files found.");
        }
    }


    public static String getHome() {
        String carbonHome = System.getProperty("carbon.home");
        if (carbonHome == null || "".equals(carbonHome) || ".".equals(carbonHome)) {
            carbonHome = getSystemDependentPath(new File(".").getAbsolutePath());
        }
        return carbonHome;
    }

    public static String getSystemDependentPath(String path) {
        return path.replace(URL_SEPARATOR_CHAR, File.separatorChar);
    }

}
