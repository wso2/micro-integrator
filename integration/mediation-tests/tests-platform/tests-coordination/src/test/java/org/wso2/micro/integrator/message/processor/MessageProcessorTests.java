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

import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
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
import static org.wso2.micro.integrator.TestUtils.MANAGEMENT_API_PORT;
import static org.wso2.micro.integrator.TestUtils.deployArtifacts;
import static org.wso2.micro.integrator.TestUtils.deploymentLog;
import static org.wso2.micro.integrator.TestUtils.getNode;
import static org.wso2.micro.integrator.TestUtils.msgProcessorPauseLog;
import static org.wso2.micro.integrator.TestUtils.msgProcessorResumeLog;

/**
 * Test cases to verify message processor behavior when clustered.
 */
public class MessageProcessorTests extends ESBIntegrationTest {

    private MultipleServersManager serverManager;
    private CarbonLogReader reader1;
    private CarbonLogReader reader2;
    private LogReaderManager logManager;
    private boolean taskScheduledInNode1 = false;
    private static final String MP_1 = "ScheduleMessageForwardingProcessor-1";
    private static final String MP_2 = "ScheduleMessageForwardingProcessor-2";
    private static final String MP_3 = "ScheduleMessageForwardingProcessor-3";
    private static final String STORE_PROXY = "storeProxy";
    private static final String MP_PREFIX = "MSMP_";
    private static final String UNDER_SCORE = "_";
    private static final String DEFAULT_SUFFIX = UNDER_SCORE + "0";
    private static final String MP_1_TASK_NAME = MP_PREFIX + MP_1 + DEFAULT_SUFFIX;
    private static final String MP_3_TASK_NAME = MP_PREFIX + MP_3 + DEFAULT_SUFFIX;
    private static final String PAYLOAD = "{\"test_payload\":\"message_processor_cluster_tests\"}";
    private static final int NODE_1_OFFSET = 50;
    private static final int NODE_2_OFFSET = 60;
    private static final int NODE_3_OFFSET = 70;

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
        deployArtifacts(serverManager.getDeploymentDirectory(), ArtifactType.MESSAGE_PROCESSOR, MP_1, MP_2, MP_3);
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
    void testMpDeploymentOfInitiallyDeactivatedMp() throws Exception {

        if (taskScheduledInNode1) {
            /*
             * In synapse level, we first deploy and deactivate immediately, hence if we have a task scheduled log
             * we should have deactivation log as well (The task is first added to task store and then retrieved from
             * there to schedule. If the status is updated as paused before retrieval, scheduling log won't be there.
             */
            if (reader1.checkForLog(deploymentLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT) && !reader1.checkForLog(
                    msgProcessorPauseLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT)) {
                Assert.fail("Inactive message Processor " + MP_3 + " was not paused after deployment.");
            }
        } else {
            if (reader2.checkForLog(deploymentLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT) && !reader2.checkForLog(
                    msgProcessorPauseLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT)) {
                Assert.fail("Inactive message Processor " + MP_3 + " was not paused after deployment.");
            }
        }
    }

    @Test(dependsOnMethods = { "testMpScheduling", "testMpDeploymentOfInitiallyDeactivatedMp" })
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
        } else {
            sendRequest(NODE_2_OFFSET);
            if (!reader2.checkForLog(storeProxyLog, LOG_READ_TIMEOUT)) {
                Assert.fail("Message hasn't reached " + STORE_PROXY);
            }
        }
        if (!reader1.checkForLog(PAYLOAD, LOG_READ_TIMEOUT)) {
            Assert.fail("Message Processor execution failed.");
        }
    }

    @Test(dependsOnMethods = { "testMpExecution" })
    void testMpDeactivationViaRunningNode() throws Exception {

        logManager.clearAll();
        if (taskScheduledInNode1) {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_1_OFFSET, false);
            if (!reader1.checkForLog(msgProcessorPauseLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Deactivation of message Processor via management api failed for " + MP_1);
            }
        } else {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_2_OFFSET, false);
            if (!reader2.checkForLog(msgProcessorPauseLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Deactivation of message Processor via management api failed for " + MP_1);
            }
        }
    }

    @Test(dependsOnMethods = { "testMpDeactivationViaRunningNode" })
    void testMpActivationViaRunningNode() throws Exception {

        logManager.clearAll();
        if (taskScheduledInNode1) {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_1_OFFSET, true);
            if (!reader1.checkForLog(msgProcessorResumeLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Activation of message Processor via management api failed for " + MP_1);
            }
        } else {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_2_OFFSET, true);
            if (!reader2.checkForLog(msgProcessorResumeLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Activation of message Processor via management api failed for " + MP_1);
            }
        }
    }

    @Test(dependsOnMethods = { "testMpActivationViaRunningNode" })
    void testMpDeactivationViaPassiveNode() throws Exception {

        logManager.clearAll();
        if (taskScheduledInNode1) {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_2_OFFSET, false);
            if (!reader1.checkForLog(msgProcessorPauseLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Deactivation of message Processor via management api failed for " + MP_1);
            }
            String mpStatus = getMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_2_OFFSET);
            Assert.assertEquals("Message processor state is not rendered properly when queried via passive node.",
                                "inactive", mpStatus);
        } else {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_1_OFFSET, false);
            if (!reader2.checkForLog(msgProcessorPauseLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Deactivation of message Processor via management api failed for " + MP_1);
            }
            String mpStatus = getMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_1_OFFSET);
            Assert.assertEquals("Message processor state is not rendered properly when queried via passive node.",
                                "inactive", mpStatus);
        }
    }

    @Test(dependsOnMethods = { "testMpDeactivationViaPassiveNode" })
    void testMpActivationViaPassiveNode() throws Exception {

        logManager.clearAll();
        if (taskScheduledInNode1) {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_2_OFFSET, true);
            if (!reader1.checkForLog(msgProcessorResumeLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Activation of message Processor via management api failed for " + MP_1);
            }
            String mpStatus = getMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_2_OFFSET);
            Assert.assertEquals("Message processor state is not rendered properly when queried via passive node.",
                                "active", mpStatus);
        } else {
            changeMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_1_OFFSET, true);
            if (!reader2.checkForLog(msgProcessorResumeLog(MP_1_TASK_NAME), LOG_READ_TIMEOUT)) {
                Assert.fail("Activation of message Processor via management api failed for " + MP_1);
            }
            String mpStatus = getMpStatus(MP_1, MANAGEMENT_API_PORT + NODE_1_OFFSET);
            Assert.assertEquals("Message processor state is not rendered properly when queried via passive node.",
                                "active", mpStatus);
        }
    }

    @Test(dependsOnMethods = { "testMpActivationViaPassiveNode" })
    void testMpStateUponRedeployment() throws Exception {

        logManager.clearAll();
        /*
         * Activate MP_3 which was deployed in in active state.
         */
        changeMpStatus(MP_3, MANAGEMENT_API_PORT + NODE_1_OFFSET, true);
        if (taskScheduledInNode1) {
            if (!reader1.checkForLog(deploymentLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT)) {
                Assert.fail(MP_3 + " didn't get scheduled upon activation.");
            }
        } else {
            if (!reader2.checkForLog(deploymentLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT)) {
                Assert.fail(MP_3 + " didn't get scheduled upon activation.");
            }
        }
        logManager.clearAll();
        CarbonTestServerManager node3 = getNode(NODE_3_OFFSET);
        serverManager.startServersWithDepSync(true, node3);
        if (taskScheduledInNode1) {
            if (reader1.checkForLog(msgProcessorPauseLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT)) {
                Assert.fail("Running message processor got paused when a new member joined the cluster.");
            }
        } else {
            if (reader2.checkForLog(msgProcessorPauseLog(MP_3_TASK_NAME), CLUSTER_DEP_TIMEOUT)) {
                Assert.fail("Running message processor got paused when a new member joined the cluster.");
            }
        }
    }

    private void changeMpStatus(String mpName, int managementApiPort, boolean isActivate) throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-type", "application/json");
        JsonObject payload = new JsonObject();
        payload.addProperty("name", mpName);
        payload.addProperty("status", isActivate ? "active" : "inactive");
        String url = "https://localhost:" + managementApiPort + "/management/message-processors";
        HttpRequestUtil.doPost(new URL(url), payload.toString(), headers);
    }

    private String getMpStatus(String mpName, int managementApiPort) throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String url = "https://localhost:" + managementApiPort + "/management/message-processors?name=" + mpName;
        HttpResponse response = HttpRequestUtil.doGet(url, headers);
        JSONObject jsonObject = new JSONObject(response.getData());
        return jsonObject.getString("status");
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
