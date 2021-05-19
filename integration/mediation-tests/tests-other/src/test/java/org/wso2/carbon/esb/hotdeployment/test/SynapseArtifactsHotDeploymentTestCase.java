/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.esb.hotdeployment.test;

import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * This class will test the ESB artifacts hot deployment.
 */
public class SynapseArtifactsHotDeploymentTestCase extends ESBIntegrationTest {

    private final String SERVER_DEPLOYMENT_DIR =
            System.getProperty(ESBTestConstant.CARBON_HOME) + File.separator + "repository" + File.separator
                    + "deployment" + File.separator + "server" + File.separator + "synapse-configs" + File.separator
                    + "default" + File.separator;
    private final String CAPP_DEPLOYMENT_DIR =
            System.getProperty(ESBTestConstant.CARBON_HOME) + File.separator + "repository" + File.separator
                    + "deployment" + File.separator + "server" + File.separator + "carbonapps" + File.separator;
    private final String SOURCE_DIR =
            TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP) + File.separator
                    + "hotdeployment" + File.separator;
    private static int SERVICE_DEPLOYMENT_DELAY = TestConfigurationProvider.getServiceDeploymentDelay();

    private final String proxyFileName = "HotDeploymentTestProxy.xml";
    private final String sequenceFileName = "HotDeploymentTestSequence.xml";
    private final String endpointFileName = "HotDeploymentTestEndpoint.xml";
    private final String apiFileName = "HotDeploymentTestAPI.xml";
    private final String localEntryFileName = "HotDeploymentTestLocalEntry.xml";
    private final String messageStoreFileName = "HotDeploymentTestMessageStore.xml";
    private final String cAppFileName = "HotDeployment_1.0.0.car";
    private final String cAppFileName2 = "SampleCappCompositeExporter_1.0.0.car";

    private final String proxyName = "HotDeploymentTestProxy";
    private final String sequenceName = "HotDeploymentTestSequence";
    private final String endpointName = "HotDeploymentTestEndpoint";
    private final String apiName = "HotDeploymentTestAPI";
    private final String localEntryName = "HotDeploymentTestLocalEntry";
    private final String messageStoreName = "HotDeploymentTestMessageStore";
    private final String cAppName = "HotDeploymentCompositeApplication";
    private final String cAppName2 = "SampleCappCompositeExporter";

    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        super.init();
        copyArtifactsToDeploymentDirectory();
    }

    @Test(groups = "wso2.esb",
          description = "Carbon Application Hot Deployment")
    public void testHotDeployment() throws Exception {
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isProxyHotDeployed(proxyName));

        assertTrue(checkProxyServiceExistence(proxyName), "Proxy Service Deployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isSequenceHotDeployed(sequenceName));
        assertTrue(checkSequenceExistence(sequenceName), "Sequence Deployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isEndpointHotDeployed(endpointName));
        assertTrue(checkEndpointExistence(endpointName), "Endpoint Deployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isApiHotDeployed(apiName));
        assertTrue(checkApiExistence(apiName), "API Deployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isLocalEntryHotDeployed(localEntryName));
        assertTrue(checkLocalEntryExistence(localEntryName), "Local Entry Deployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isMessageStoreHotDeployed(messageStoreName));
        assertTrue(checkMessageStoreExistence(messageStoreName), "Message Store Deployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAppHotDeployed(cAppName));
        assertTrue(checkCarbonAppExistence(cAppName), "Carbon application Deployment failed");
    }

    @Test(groups = "wso2.esb",
            description = "Carbon Application Hot Deployment through REST API")
    public void testCarbonAppDeploymentFromRESTApi() throws Exception {
        log.info("--------  Running testCarbonAppDeploymentFromRESTApi --------");
        String cAppFile = SOURCE_DIR + cAppFileName2;
        File file = new File(cAppFile);
        deployCarbonApplication(file);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAppHotDeployed(cAppName2));
        assertTrue(checkCarbonAppExistence(cAppName2), "Carbon application Deployment failed");
    }

    @Test(groups = "wso2.esb",
            description = "Carbon Application un-deploy through REST API")
    public void testCarbonAppUnDeploymentFromRESTApi() throws Exception {
        log.info("--------  Running testCarbonAppUnDeploymentFromRESTApi ---------");
        unDeployCarbonApplication(cAppName2 + "_1.0.0");
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAppUnDeployed(cAppName2));
        assertFalse(checkCarbonAppExistence(cAppName2), "Carbon Application Undeployment failed");
    }

    @Test(groups = "wso2.esb", dependsOnMethods = "testHotDeployment",
          description = "Carbon Application Hot Un-deployment")
    public void testHotUnDeployment() throws Exception {

        String proxyServiceFile = SERVER_DEPLOYMENT_DIR + "proxy-services" + File.separator + proxyFileName;
        Awaitility.await().atMost(20, SECONDS).until(fileDelete(proxyServiceFile));

        String sequenceFile = SERVER_DEPLOYMENT_DIR + "sequences" + File.separator + sequenceFileName;
        Awaitility.await().atMost(20, SECONDS).until(fileDelete(sequenceFile));

        String endpointFile = SERVER_DEPLOYMENT_DIR + "endpoints" + File.separator + endpointFileName;
        Awaitility.await().atMost(20, SECONDS).until(fileDelete(endpointFile));

        String apiFile = SERVER_DEPLOYMENT_DIR + "api" + File.separator + apiFileName;
        Awaitility.await().atMost(20, SECONDS).until(fileDelete(apiFile));

        String localEntryFile = SERVER_DEPLOYMENT_DIR + "local-entries" + File.separator + localEntryFileName;
        Awaitility.await().atMost(20, SECONDS).until(fileDelete(localEntryFile));

        String messageStoreFile = SERVER_DEPLOYMENT_DIR + "message-stores" + File.separator + messageStoreFileName;
        Awaitility.await().atMost(20, SECONDS).until(fileDelete(messageStoreFile));

        String cAppFile = CAPP_DEPLOYMENT_DIR + File.separator + cAppFileName;
        Awaitility.await().atMost(20, SECONDS).until(fileDelete(cAppFile));


        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isProxyUnDeployed(proxyName));
        assertFalse(checkProxyServiceExistence(proxyName), "Proxy Undeployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isSequenceUnDeployed(sequenceName));
        assertFalse(checkSequenceExistence(sequenceName), "Sequence Undeployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isEndpointUnDeployed(endpointName));
        assertFalse(checkEndpointExistence(endpointName), "Endpoint Undeployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isApiUnDeployed(apiName));
        assertFalse(checkApiExistence(apiName), "API Undeployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isLocalEntryUnDeployed(localEntryName));
        assertFalse(checkLocalEntryExistence(localEntryName), "Local Entry Undeployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isMessageStoreUnDeployed(messageStoreName));
        assertFalse(checkMessageStoreExistence(messageStoreName), "Message Store Undeployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isCAppUnDeployed(cAppName));
        assertFalse(checkCarbonAppExistence(cAppName), "Carbon Application Undeployment failed");
    }

    @AfterClass(alwaysRun = true)
    public void unDeployService() throws Exception {
        super.cleanup();
    }

    private void copyArtifactsToDeploymentDirectory() throws IOException {
        String proxyFile = SOURCE_DIR + proxyFileName;
        String sequenceFile = SOURCE_DIR + sequenceFileName;
        String endpointFile = SOURCE_DIR + endpointFileName;
        String apiFile = SOURCE_DIR + apiFileName;
        String localEntryFile = SOURCE_DIR + localEntryFileName;
        String messageStoreFile = SOURCE_DIR + messageStoreFileName;
        String cAppFile = SOURCE_DIR + cAppFileName;
        FileUtils.copyFile(new File(proxyFile),
                           new File(SERVER_DEPLOYMENT_DIR + "proxy-services" + File.separator + proxyFileName));
        FileUtils.copyFile(new File(sequenceFile),
                           new File(SERVER_DEPLOYMENT_DIR + "sequences" + File.separator + sequenceFileName));
        FileUtils.copyFile(new File(endpointFile),
                           new File(SERVER_DEPLOYMENT_DIR + "endpoints" + File.separator + endpointFileName));
        FileUtils.copyFile(new File(apiFile), new File(SERVER_DEPLOYMENT_DIR + "api" + File.separator + apiFileName));
        FileUtils.copyFile(new File(localEntryFile),
                           new File(SERVER_DEPLOYMENT_DIR + "local-entries" + File.separator + localEntryFileName));
        FileUtils.copyFile(new File(messageStoreFile),
                           new File(SERVER_DEPLOYMENT_DIR + "message-stores" + File.separator + messageStoreFileName));
        FileUtils.copyFile(new File(cAppFile),
                           new File(CAPP_DEPLOYMENT_DIR + File.separator + cAppFileName));
    }

    private Callable<Boolean> fileDelete(final String filePath) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return FileManager.deleteFile(filePath);
            }
        };
    }

    private Callable<Boolean> isProxyHotDeployed(String proxyName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return checkProxyServiceExistence(proxyName);
            }
        };
    }

    private Callable<Boolean> isProxyUnDeployed(String proxyName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !checkProxyServiceExistence(proxyName);
            }
        };
    }

    private Callable<Boolean> isApiHotDeployed(String apiName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return checkApiExistence(apiName);
            }
        };
    }

    private Callable<Boolean> isApiUnDeployed(String apiName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !checkApiExistence(apiName);
            }
        };
    }

    private Callable<Boolean> isSequenceHotDeployed(String seqName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return checkSequenceExistence(seqName);
            }
        };
    }

    private Callable<Boolean> isSequenceUnDeployed(String seqName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !checkSequenceExistence(seqName);
            }
        };
    }

    private Callable<Boolean> isEndpointHotDeployed(String endpointName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return checkEndpointExistence(endpointName);
            }
        };
    }

    private Callable<Boolean> isEndpointUnDeployed(String endpointName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !checkEndpointExistence(endpointName);
            }
        };
    }

    private Callable<Boolean> isLocalEntryHotDeployed(String localEntryName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return checkLocalEntryExistence(localEntryName);
            }
        };
    }

    private Callable<Boolean> isLocalEntryUnDeployed(String localEntryName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !checkLocalEntryExistence(localEntryName);
            }
        };
    }

    private Callable<Boolean> isMessageStoreHotDeployed(String messageStoreName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return checkMessageStoreExistence(messageStoreName);
            }
        };
    }

    private Callable<Boolean> isMessageStoreUnDeployed(String messageStoreName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !checkMessageStoreExistence(messageStoreName);
            }
        };
    }

    private Callable<Boolean> isCAppHotDeployed(String cAppName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return checkCarbonAppExistence(cAppName);
            }
        };
    }

    private Callable<Boolean> isCAppUnDeployed(String cAppName) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !checkCarbonAppExistence(cAppName);
            }
        };
    }
}
