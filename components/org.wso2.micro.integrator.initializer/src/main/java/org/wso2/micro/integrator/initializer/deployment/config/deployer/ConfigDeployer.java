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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.property.PropertyLoader;
import org.apache.synapse.transport.nhttp.config.SslSenderTrustStoreHolder;
import org.apache.synapse.transport.nhttp.config.TrustStoreHolder;
import org.wso2.micro.application.deployer.CarbonApplication;
import org.wso2.micro.application.deployer.config.ApplicationConfiguration;
import org.wso2.micro.application.deployer.config.Artifact;
import org.wso2.micro.application.deployer.config.CappFile;
import org.wso2.micro.application.deployer.handler.AppDeploymentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ConfigDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(ConfigDeployer.class);

    private static final String PROPERTY_TYPE = "config/property";

    private static final String LOCAL_CONFIG_FILE_NAME = "config.properties";
    private static final String GLOBAL_CONFIG_FILE_NAME = "file.properties";

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
            writePropertyToMap(artifact);
        });
    }

    private void writePropertyToMap(Artifact artifact) {
        // get the file path of the registry config file
        List<CappFile> files = artifact.getFiles();
        if (files.size() == 1) {
            String globalConfigFilePath = Paths.get(getHome()) + File.separator + "conf" + File.separator + GLOBAL_CONFIG_FILE_NAME;
            String localConfigFilePath = artifact.getExtractedPath() + File.separator + LOCAL_CONFIG_FILE_NAME;
            File localConfigFile = new File(localConfigFilePath);
            File globalConfigFile = new File(globalConfigFilePath);
            Properties localProperties = new Properties();
            Properties globalProperties = new Properties();
            if (localConfigFile.exists()) {
                try (FileInputStream localFileReader = new FileInputStream(localConfigFile);
                     FileInputStream globalFileReader = new FileInputStream(globalConfigFile)) {
                    localProperties.load(localFileReader);
                    globalProperties.load(globalFileReader);
                    for (Map.Entry<Object, Object> entry : localProperties.entrySet()) {
                        String key = entry.getKey().toString();
                        String propertyValue = System.getenv(key);
                        if (propertyValue == null) {
                            propertyValue = System.getProperty(key);
                            if (propertyValue == null) {
                                propertyValue = globalProperties.getProperty(key);
                            }
                        }
                        if (PropertyLoader.getInstance().hasKey(key)) {
                            String oldValue = PropertyLoader.getInstance().getPropertyValue(key);
                            if (!Objects.equals(oldValue, propertyValue)) {
                                log.error(String.format("The value:[%s] of the key:[%s] has been " +
                                        "replaced with the new value:[%s].", oldValue, key, propertyValue));
                            }
                        }
                        if (propertyValue != null) {
                            if (Objects.equals(entry.getValue().toString(), "cert")) {
                                // Load the default truststore
                                char[] password = SslSenderTrustStoreHolder.getInstance().getPassword().toCharArray();
                                String type = SslSenderTrustStoreHolder.getInstance().getType();
                                Path trustStorePath = Paths.get(getHome(), SslSenderTrustStoreHolder.getInstance().getLocation());
                                try (FileInputStream trustStoreStream = new FileInputStream(trustStorePath.toFile())) {
                                    KeyStore trustStore = KeyStore.getInstance(type);
                                    trustStore.load(trustStoreStream, password);

                                    // Load the certificate file
                                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                                    try (FileInputStream certStream = new FileInputStream(propertyValue)) {
                                        Certificate cert = certFactory.generateCertificate(certStream);
                                        // Add the certificate to the truststore
                                        trustStore.setCertificateEntry(key, cert);
                                        System.out.println("Certificate added with alias: " + key);
                                    }
                                    // Save the truststore with the new certificate
                                    try (FileOutputStream outputStream = new FileOutputStream(trustStorePath.toFile())) {
                                        trustStore.store(outputStream, password);
                                        System.out.println("Truststore updated successfully at: " + trustStorePath);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("Failed to import certificate: " + e.getMessage());
                                }
                            }
                            PropertyLoader.getInstance().setProperty(key, propertyValue);
                        } else {
                            log.error(String.format("The value of the key:[%s] is not found.", key));
                        }
                    }
                } catch (FileNotFoundException e) {
                    log.debug("Config file not found.:" + e.getMessage());
                } catch (IOException e) {
                    log.error("config/property type must have a single file which declares " +
                            "config. But " + files.size() + " files found.");
                }
            } else {
                log.info("No configuration was used in the integration");
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
