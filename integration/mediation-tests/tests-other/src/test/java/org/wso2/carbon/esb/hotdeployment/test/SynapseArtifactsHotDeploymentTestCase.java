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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;


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
    private ServerConfigurationManager serverConfigurationManager;

    private final String proxyFileName = "HotDeploymentTestProxy.xml";
    private final String sequenceFileName = "HotDeploymentTestSequence.xml";
    private final String endpointFileName = "HotDeploymentTestEndpoint.xml";
    private final String apiFileName = "HotDeploymentTestAPI.xml";
    private final String localEntryFileName = "HotDeploymentTestLocalEntry.xml";
    private final String messageStoreFileName = "HotDeploymentTestMessageStore.xml";
    private final String cAppFileName = "HotDeployment_1.0.0.car";

    private CarbonLogReader carbonLogReader = new CarbonLogReader();

    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        //Changing synapse configuration to enable statistics and tracing
        serverConfigurationManager = new ServerConfigurationManager(
                new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
        serverConfigurationManager.applyMIConfigurationWithRestart(new File(
                getESBResourceLocation() + File.separator + "hotdeployment" + File.separator
                        + "deployment.toml"));
        super.init();
        copyArtifactsToDeploymentDirectory();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb",
          description = "Proxy Service Hot Deployment")
    public void testProxyServiceHotDeployment() throws Exception {
        String proxyName = "HotDeploymentTestProxy";
        String proxyServiceFile = SERVER_DEPLOYMENT_DIR + "proxy-services" + File.separator + proxyFileName;

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isProxyHotDeployed(proxyName));
        assertTrue(checkProxyServiceExistence(proxyName), "Proxy Deployment failed");

        carbonLogReader.clearLogs();
        FileUtils.touch(new File(proxyServiceFile));
        log.info(proxyFileName + " has been updated and waiting for redeployment");

        assertTrue(carbonLogReader.checkForLog("'HotDeploymentTestProxy' has been update from file", DEFAULT_TIMEOUT),
                   "Proxy deployment failed on updating file. Log message not found");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isProxyHotDeployed(proxyName));
        assertTrue(checkProxyServiceExistence(proxyName), "Proxy Deployment failed on updating file");

        Awaitility.await().atMost(20, SECONDS).until(fileDelete(proxyServiceFile));
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isProxyUnDeployed(proxyName));
        assertFalse(checkProxyServiceExistence(proxyName),
                           "Proxy Undeployment failed");
    }


    @Test(groups = "wso2.esb",
          description = "Sequence Hot Deployment")
    public void testSequenceHotDeployment() throws Exception {
        String sequenceName = "HotDeploymentTestSequence";
        String sequenceFile = SERVER_DEPLOYMENT_DIR + "sequences" + File.separator + sequenceFileName;


        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isSequenceHotDeployed(sequenceName));
        assertTrue(checkSequenceExistence(sequenceName), "Sequence Deployment failed");

        carbonLogReader.clearLogs();
        FileUtils.touch(new File(sequenceFile));
        log.info(sequenceFileName + " has been updated and waiting for redeployment");

        assertTrue(
                carbonLogReader.checkForLog("HotDeploymentTestSequence has been updated from the file", DEFAULT_TIMEOUT),
                "Sequence deployment failed on updating file. Log message not found");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isSequenceHotDeployed(sequenceName));
        assertTrue(checkSequenceExistence(sequenceName), "Sequence Deployment failed on updating file");

        Awaitility.await().atMost(20, SECONDS).until(fileDelete(sequenceFile));
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isSequenceUnDeployed(sequenceName));
        assertFalse(checkSequenceExistence(sequenceName),
                           "Sequence Undeployment failed");
    }

    @Test(groups = "wso2.esb",
          description = "Endpoint Hot Deployment")
    public void testEndpointHotDeployment() throws Exception {
        String endpointName = "HotDeploymentTestEndpoint";
        String endpointFile = SERVER_DEPLOYMENT_DIR + "endpoints" + File.separator + endpointFileName;


        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isEndpointHotDeployed(endpointName));
        assertTrue(checkEndpointExistence(endpointName), "Endpoint Deployment failed");

        carbonLogReader.clearLogs();
        FileUtils.touch(new File(endpointFile));
        log.info(endpointFileName + " has been updated and waiting for redeployment");

        assertTrue(
                carbonLogReader.checkForLog("HotDeploymentTestEndpoint has been updated from the file", DEFAULT_TIMEOUT),
                "Endpoint deployment failed on updating file. Log message not found");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isEndpointHotDeployed(endpointName));
        assertTrue(checkEndpointExistence(endpointName), "Endpoint Deployment failed on updating file");

        Awaitility.await().atMost(20, SECONDS).until(fileDelete(endpointFile));
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isEndpointUnDeployed(endpointName));
        assertFalse(checkEndpointExistence(endpointName),
                           "Endpoint Undeployment failed");
    }

    @Test(groups = "wso2.esb",
          description = "API Hot Deployment")
    public void testAPIHotDeployment() throws Exception {
        String apiName = "HotDeploymentTestAPI";
        String apiFile = SERVER_DEPLOYMENT_DIR + "api" + File.separator + apiFileName;

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isApiHotDeployed(apiName));
        assertTrue(checkApiExistence(apiName), "API Deployment failed");

        carbonLogReader.clearLogs();
        FileUtils.touch(new File(apiFile));
        log.info(apiFileName + " has been updated and waiting for redeployment");

        assertTrue(carbonLogReader.checkForLog("HotDeploymentTestAPI has been updated from the file", DEFAULT_TIMEOUT),
                   "API deployment failed on updating file. Log message not found");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isApiHotDeployed(apiName));
        assertTrue(checkApiExistence(apiName), "API Deployment failed on updating file");

        Awaitility.await().atMost(20, SECONDS).until(fileDelete(apiFile));
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isApiUnDeployed(apiName));
        assertFalse(checkApiExistence(apiName),
                           "API Undeployment failed");
    }

    @Test(groups = "wso2.esb",
          description = "Local Entry Hot Deployment")
    public void testLocalEntryHotDeployment() throws Exception {
        String localEntryName = "HotDeploymentTestLocalEntry";
        String localEntryFile = SERVER_DEPLOYMENT_DIR + "local-entries" + File.separator + localEntryFileName;

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isLocalEntryHotDeployed(localEntryName));
        assertTrue(checkLocalEntryExistence(localEntryName), "Local Entry Deployment failed");

        carbonLogReader.clearLogs();
        FileUtils.touch(new File(localEntryFile));
        log.info(localEntryFileName + " has been updated and waiting for redeployment");

        assertTrue(
                carbonLogReader.checkForLog("HotDeploymentTestLocalEntry has been updated from the file", DEFAULT_TIMEOUT),
                "Local Entry deployment failed on updating file. Log message not found");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isLocalEntryHotDeployed(localEntryName));
        assertTrue(checkLocalEntryExistence(localEntryName), "Local Entry Deployment failed on updating file");

        Awaitility.await().atMost(20, SECONDS).until(fileDelete(localEntryFile));
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isLocalEntryUnDeployed(localEntryName));
        assertFalse(checkLocalEntryExistence(localEntryName),
                           "Local Entry Undeployment failed");
    }

    @Test(groups = "wso2.esb",
          description = "Message Store Hot Deployment")
    public void testMessageStoreHotDeployment() throws Exception {
        String messageStoreName = "HotDeploymentTestMessageStore";
        String messageStoreFile = SERVER_DEPLOYMENT_DIR + "message-stores" + File.separator + messageStoreFileName;

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isMessageStoreHotDeployed(messageStoreName));
        assertTrue(checkMessageStoreExistence(messageStoreName), "Message Store Deployment failed");

        carbonLogReader.clearLogs();
        FileUtils.touch(new File(messageStoreFile));
        log.info(messageStoreFileName + " has been updated and waiting for redeployment");

        assertTrue(carbonLogReader.checkForLog("HotDeploymentTestMessageStore has been updated from the file", DEFAULT_TIMEOUT),
                   "Message Store deployment failed on updating file. Log message not found");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isMessageStoreHotDeployed(messageStoreName));
        assertTrue(checkMessageStoreExistence(messageStoreName),
                          "Message Store Deployment failed on updating file");

        Awaitility.await().atMost(20, SECONDS).until(fileDelete(messageStoreFile));
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isMessageStoreUnDeployed(messageStoreName));
        assertFalse(checkMessageStoreExistence(messageStoreName),
                           "Message Store Undeployment failed");
    }

    @Test(groups = "wso2.esb",
          description = "Carbon Application Hot Deployment")
    public void testCAppHotDeployment() throws Exception {
        String cAppName = "HotDeploymentCompositeApplication";
        String cAppFile = CAPP_DEPLOYMENT_DIR + File.separator + cAppFileName;

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isCAppHotDeployed(cAppName));
        assertTrue(checkCarbonAppExistence(cAppName), "Carbon Application Deployment failed");

        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isProxyHotDeployed("hotDeploymentCAppTestingProxy"));
        assertTrue(checkProxyServiceExistence("hotDeploymentCAppTestingProxy"),
                          "transform Proxy service deployment failed");

        Awaitility.await().atMost(20, SECONDS).until(fileDelete(cAppFile));
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                                                                           TimeUnit.MILLISECONDS).until(
                isCAppUnDeployed(cAppName));
        assertFalse(checkCarbonAppExistence(cAppName),
                           "Carbon Application Undeployment failed");
    }

    @AfterClass(alwaysRun = true)
    public void unDeployService() throws Exception {
        super.cleanup();
        serverConfigurationManager.restoreToLastConfiguration();
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
