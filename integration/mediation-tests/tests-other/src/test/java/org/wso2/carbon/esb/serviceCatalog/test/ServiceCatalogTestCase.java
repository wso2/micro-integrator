/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.serviceCatalog.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;
import org.wso2.micro.application.deployer.AppDeployerUtils;
import org.wso2.micro.core.util.CarbonException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This class will test the service catalog feature.
 */
public class ServiceCatalogTestCase extends ESBIntegrationTest {

    private static final String FAULTY_CAPP = "invalidCompositeApplication_1.0.0.car";
    private static final String CAPP_WITH_META_AND_ENV = "blaCompositeExporter_1.0.0-SNAPSHOT.car";
    private static final String SH_FILE_NAME = "micro-integrator.sh";
    private static final String BAT_FILE_NAME = "micro-integrator.bat";
    private static final String SERVICE_URL = "serviceUrl";
    private static final String SERVICE_CATALOG_FOLDER_NAME = "ServiceCatalog";
    private static final String ZIP_FILE_NAME = "payload.zip";
    private ServerConfigurationManager serverConfigurationManager;
    private CarbonLogReader carbonLogReader = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        // Enabling service catalog
        serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyMIConfigurationWithRestart(new File(
                getESBResourceLocation() + File.separator + "serviceCatalog" + File.separator + "deployment.toml"));
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = {"wso2.esb"},
            description = "Test Service Catalog with a faulty CAPP", priority = 1)
    public void testServiceCatalogWithFaultyCAPP() throws InterruptedException, AutomationUtilException {
        serverConfigurationManager.restartMicroIntegrator();
        assertTrue(
                Utils.checkForLog(carbonLogReader, "Faulty CAPPs detected - aborting the service-catalog uploader", 10),
                "Did not receive the expected info log");
    }

    @Test(groups = {"wso2.esb"},
            description = "Test service catalog without faulty and new CAPPs (CAPPs which has metadata)", priority = 2)
    public void testServiceCatalog2WithoutFaultyCAPP()
            throws IOException, URISyntaxException, AutomationUtilException, InterruptedException {
        serverConfigurationManager.removeFromCarbonapps(FAULTY_CAPP);
        serverConfigurationManager.restartMicroIntegrator();
        assertTrue(Utils.checkForLog(carbonLogReader,
                "Could not find metadata to upload, aborting the service-catalog uploader", 10),
                "Did not receive the expected info log");
    }

    @Test(groups = {"wso2.esb"},
            description = "Test service catalog without setting env variables", priority = 3)
    public void testServiceCatalogMetadataWithoutEnv()
            throws IOException, AutomationUtilException, InterruptedException {
        File metadataCAPP = new File(
                getESBResourceLocation() + File.separator + "serviceCatalog" + File.separator + CAPP_WITH_META_AND_ENV);
        serverConfigurationManager.copyToCarbonapps(metadataCAPP);
        serverConfigurationManager.restartMicroIntegrator();
        assertTrue(Utils.checkForLog(carbonLogReader,
                "Environment variables are not configured correctly", 10),
                "Did not receive the expected info log");
    }

    @Test(groups = {"wso2.esb"},
            description = "Test service catalog after setting env variables", priority = 4)
    public void testServiceCatalogMetadataWithEnv()
            throws IOException, AutomationUtilException, InterruptedException {

        // Set env variables for linux and mac
        File newShFile = new File(
                getESBResourceLocation() + File.separator + "serviceCatalog" + File.separator + SH_FILE_NAME);
        File oldShFile =
                new File(CarbonBaseUtils.getCarbonHome() + File.separator + "bin" + File.separator + SH_FILE_NAME);
        serverConfigurationManager.applyConfigurationWithoutRestart(newShFile, oldShFile, true);

        // Set env variables for Windows
        File newBatFile = new File(
                getESBResourceLocation() + File.separator + "serviceCatalog" + File.separator + BAT_FILE_NAME);
        File oldBatFile =
                new File(CarbonBaseUtils.getCarbonHome() + File.separator + "bin" + File.separator + BAT_FILE_NAME);
        serverConfigurationManager.applyConfigurationWithoutRestart(newBatFile, oldBatFile, true);

        serverConfigurationManager.restartMicroIntegrator();
        assertTrue(Utils.checkForLog(carbonLogReader,
                "Error occurred while uploading metadata to service catalog endpoint", 10),
                "Did not receive the expected info log");
    }

    @Test(groups = {"wso2.esb"},
            description = "Test the ZIP file created by the service catalog", priority = 5)
    public void testServiceCatalogZipFile() throws CarbonException, FileNotFoundException {
        String payloadZipPath = CarbonBaseUtils.getCarbonHome() + File.separator + "tmp" + File.separator + SERVICE_CATALOG_FOLDER_NAME +
                File.separator + ZIP_FILE_NAME;
        File zipFile = new File(payloadZipPath);
        assertTrue(zipFile.exists(),"Could not find the payload.zip in the temp folder");
        String tempExtractedDirPath = AppDeployerUtils.extractCarbonApp(payloadZipPath);
        File extracted = new File(tempExtractedDirPath);
        assertTrue(extracted.exists(),"Error occurred while extracting the ZIP");
        File metadataFile = new File(extracted,"healthcare_v1.0.0-SNAPSHOT");
        File yamlFile = new File(metadataFile,"metadata.yaml");
        assertTrue(yamlFile.exists(),"Could not find the metadata yaml file");
        Yaml yaml = new Yaml();
        Map<String, Object> obj =
                (Map<String, Object>) yaml.load(new FileInputStream(yamlFile));
        String currentServiceUrl = (String) obj.get(SERVICE_URL);
        Assert.assertEquals("https://localhost:8290/health",currentServiceUrl,"Parameterized server url creation " +
                "failed");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        // Add the faulty CAPP back and restart the server
        serverConfigurationManager.copyToCarbonapps(new File(
                getESBResourceLocation() + File.separator + "serviceCatalog" + File.separator + FAULTY_CAPP));
        super.cleanup();
        serverConfigurationManager.restartGracefully();
    }
}
