/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.message.processor;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonTestServerManager;
import org.wso2.esb.integration.common.extensions.carbonserver.MultipleServersManager;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.LogReaderManager;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.esb.integration.common.utils.Utils.ArtifactType;
import static org.wso2.micro.integrator.TestUtils.CLUSTER_DEP_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.LOG_READ_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.deployArtifacts;
import static org.wso2.micro.integrator.TestUtils.deploymentLog;
import static org.wso2.micro.integrator.TestUtils.getNode;

/**
 * Test cases to verify message processor behavior when clustered.
 */
public class MessageProcessorTests extends ESBIntegrationTest {

    private MultipleServersManager serverManager;
    private CarbonLogReader reader1;
    private CarbonLogReader reader2;
    private LogReaderManager logManager;
    private boolean taskScheduledInNode1 = false;
    private static final String MS_1 = "InMemoryStore-1";
    private static final String MS_2 = "InMemoryStore-2";
    private static final String MP_1 = "ScheduleMessageForwardingProcessor-1";
    private static final String MP_2 = "ScheduleMessageForwardingProcessor-2";
    private static final String STORE_PROXY = "storeProxy";
    private static final String MP_PREFIX = "MSMP_";
    private static final String UNDER_SCORE = "_";
    private static final String DEFAULT_SUFFIX = UNDER_SCORE + "0";
    private static final String MP_1_TASK_NAME = MP_PREFIX + MP_1 + DEFAULT_SUFFIX;
    private static final String PAYLOAD = "{\"test_payload\":\"message_processor_cluster_tests\"}";
    private static final int NODE_1_OFFSET = 50;
    private static final int NODE_2_OFFSET = 60;

    @BeforeClass
    void initialize() throws Exception {

        context = new AutomationContext();
        serverManager = new MultipleServersManager();
        logManager = new LogReaderManager();
        CarbonTestServerManager node1 = getNode(NODE_1_OFFSET);
        CarbonTestServerManager node2 = getNode(NODE_2_OFFSET);
        serverManager.startServersWithDepSync(true, node1, node2);
        reader1 = new CarbonLogReader(node1.getCarbonHome());
        reader2 = new CarbonLogReader(node2.getCarbonHome());
        logManager.start(reader1, reader2);
        deployArtifacts(serverManager.getDeploymentDirectory(), ArtifactType.MESSAGE_STORES, MS_1, MS_2);
        deployArtifacts(serverManager.getDeploymentDirectory(), ArtifactType.MESSAGE_PROCESSOR, MP_1, MP_2);
        deployArtifacts(serverManager.getDeploymentDirectory(), ArtifactType.PROXY, STORE_PROXY);
    }

    @Test
    void testMpScheduling() throws Exception {

        taskScheduledInNode1 = reader1.checkForLog(deploymentLog(MP_1_TASK_NAME), CLUSTER_DEP_TIMEOUT);
        log.info(MP_1 + " is" + (taskScheduledInNode1 ? " " : " not ") + "scheduled in node 1.");
        if (taskScheduledInNode1 == reader2.checkForLog(deploymentLog(MP_1_TASK_NAME), CLUSTER_DEP_TIMEOUT)) {
            Assert.fail("Deployment failed for " + MP_1);
        }
    }

    @Test(dependsOnMethods = { "testMpScheduling" })
    void testMpMemberCount() throws Exception {

        int memberCount = 5;
        boolean result = true;
        if (taskScheduledInNode1) {
            for (int i = 0; i < memberCount; i++) {
                String mpName = MP_PREFIX + MP_2 + UNDER_SCORE + i;
                if (!reader1.checkForLog(deploymentLog(mpName), 1)) {
                    result = false;
                    break;
                }
            }
        } else {
            for (int i = 0; i < memberCount; i++) {
                String mpName = MP_PREFIX + MP_2 + UNDER_SCORE + i;
                if (!reader2.checkForLog(deploymentLog(mpName), 1)) {
                    result = false;
                    break;
                }
            }
        }
        if (!result) {
            Assert.fail(memberCount + " tasks are not scheduled for " + MP_2 + " as specified in the member count.");
        }
    }

    @Test(dependsOnMethods = { "testMpMemberCount" })
    void testMpExecution() throws Exception {

        logManager.clearAll();
        String storeProxyLog = "where = store proxy";
        if (taskScheduledInNode1) {
            sendRequest(NODE_1_OFFSET);
            if (!reader1.checkForLog(storeProxyLog, LOG_READ_TIMEOUT)) {
                Assert.fail("Message hasn't reached " + STORE_PROXY);
            }
            if (!reader1.checkForLog(PAYLOAD, LOG_READ_TIMEOUT)) {
                Assert.fail("Message Processor execution failed.");
            }
        } else {
            sendRequest(NODE_2_OFFSET);
            if (!reader2.checkForLog(storeProxyLog, LOG_READ_TIMEOUT)) {
                Assert.fail("Message hasn't reached " + STORE_PROXY);
            }
            if (!reader2.checkForLog(PAYLOAD, LOG_READ_TIMEOUT)) {
                Assert.fail("Message Processor execution failed.");
            }
        }
    }

    private void sendRequest(int offset) throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        String url = "http://localhost:" + (8280 + offset) + "/services/" + STORE_PROXY;
        HttpRequestUtil.doPost(new URL(url), PAYLOAD, headers);
    }

    @AfterClass
    void stop() throws Exception {
        logManager.stopAll();
        serverManager.stopAllServers();
    }
}
