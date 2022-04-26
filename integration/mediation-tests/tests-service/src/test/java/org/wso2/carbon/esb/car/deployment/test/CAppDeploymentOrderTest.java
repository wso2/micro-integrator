/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.car.deployment.test;

import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CAppDeploymentOrderTest extends ESBIntegrationTest {

    public static final String CONNECTOR_DEPLOYMENT_MESSAGE =
            "Synapse Library named '{org.wso2.carbon.connector}email' has been " +
                    "deployed from file";
    public static final String LOCAL_DEPLOYMENT_MESSAGE =
            "LocalEntry named 'sample-local-entry' has been deployed from file";
    public static final String ENDPOINT_DEPLOYMENT_MESSAGE =
            "Endpoint named 'PineValleyEndpoint' has been deployed from file";

    private static final String SEQ_DEPLOYMENT_MESSAGE =
            "Sequence named 'sample-sequence' has been deployed from file";

    public static final String MSG_STORE_DEPLOYMENT_MESSAGE =
            "Message Store named 'in-memory-message-store' has been deployed from file";

    public static final String TEMPLATE_DEPLOYMENT_MESSAGE =
            "Template named 'sample_seq_template' has been deployed from file";

    public static final String ENDPOINT_TEMPLATE_DEPLOYMENT_MESSAGE =
            "Endpoint Template named 'sample_template' has been deployed from file";

    public static final String PROXY_DEPLOYMENT_MESSAGE =
            "ProxyService named 'StockQuoteProxy' has been deployed from file";

    public static final String TASK_DEPLOYMENT_MESSAGE =
            "StartupTask named 'sample-cron-task' has been deployed from file";

    public static final String MSG_PROCESSOR_DEPLOYMENT_MESSAGE =
            "Message Processor named 'scheduled-msg-processor' has been deployed " +
                    "from file";

    public static final String API_DEPLOYMENT_MESSAGE = "API named 'HealthcareAPI' has been deployed from file";

    private static final String INBOUND_DEPLOYMENT_MESSAGE =
            "Inbound Endpoint named 'httpInboundEP' has been deployed from file";

    private ServerConfigurationManager serverConfigurationManager;
    private CarbonLogReader carbonLogReader;
    private static final int SERVICE_DEPLOYMENT_DELAY = TestConfigurationProvider.getServiceDeploymentDelay();

    private final String SOURCE_DIR =
            TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP) + File.separator
                    + "car" + File.separator;

    @BeforeClass(alwaysRun = true)
    private void initialize() throws Exception {

        super.init();
        carbonLogReader = new CarbonLogReader();
        serverConfigurationManager = new ServerConfigurationManager(new AutomationContext());
        carbonLogReader.start();
        String cAppFile = SOURCE_DIR + "HealthCareCompositeExporter_1.0.0.car";
        File file = new File(cAppFile);
        deployCarbonApplication(file);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAPPDeploymentLogWritten());
    }

    @Test(groups = {"wso2.esb"}, description = "Test whether synapse artifacts get deployed through capp in order")
    public void carReDeploymentTest() {

        String[] logs = carbonLogReader.getLogs().split("\\[");
        Assert.assertTrue(checkDeploymentOrder(logs), "Deployment order isn't correct");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {

        carbonLogReader.stop();
        serverConfigurationManager.restoreToLastMIConfiguration();
    }

    private boolean checkDeploymentOrder(String[] logs) {

        Queue<String> deploymentQueue = new LinkedList<>();
        deploymentQueue.add(CONNECTOR_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(LOCAL_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(ENDPOINT_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(SEQ_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(MSG_STORE_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(TEMPLATE_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(ENDPOINT_TEMPLATE_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(PROXY_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(TASK_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(MSG_PROCESSOR_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(API_DEPLOYMENT_MESSAGE);
        deploymentQueue.add(INBOUND_DEPLOYMENT_MESSAGE);

        for (String log : logs) {
            if (!deploymentQueue.isEmpty() && log.contains(deploymentQueue.peek())) {
                deploymentQueue.poll();
            }
        }
        return deploymentQueue.isEmpty();
    }

    private Callable<Boolean> isCAPPDeploymentLogWritten() {

        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                return carbonLogReader
                        .checkForLog("Successfully Deployed Carbon Application : HealthCareCompositeExporter_1.0.0",
                                DEFAULT_TIMEOUT);
            }
        };
    }
}
