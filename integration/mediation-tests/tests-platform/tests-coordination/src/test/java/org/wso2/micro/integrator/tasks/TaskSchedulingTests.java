/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.tasks;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonTestServerManager;
import org.wso2.esb.integration.common.extensions.carbonserver.MultipleServersManager;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.LogReaderManager;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.wso2.micro.integrator.TestUtils.deployArtifacts;
import static org.wso2.micro.integrator.TestUtils.deploymentLog;
import static org.wso2.micro.integrator.TestUtils.getNode;

/**
 * Tests to verify same tasks is not scheduling in 2 node cluster.
 */
public class TaskSchedulingTests extends ESBIntegrationTest {

    private MultipleServersManager serverManager;
    private CarbonLogReader reader1;
    private CarbonLogReader reader2;
    private LogReaderManager logManagerNode1;
    private LogReaderManager logManagerNode2;
    private static final String TASK_1 = "task-1";
    private static final String COORDINATOR_LOG = "Current node state changed from: MEMBER to: COORDINATOR";
    private static final int DEPLOYMENT_TIMEOUT = 20;
    private static final int SERVER_STARTUP_TIMEOUT = 180;

    @BeforeClass
    void initialize() throws Exception {

        context = new AutomationContext();
        serverManager = new MultipleServersManager();
        logManagerNode1 = new LogReaderManager();
        logManagerNode2 = new LogReaderManager();
        HashMap<String, String> startupParameters = new HashMap<>();
        startupParameters.put("-DnodeId", "node-1");
        CarbonTestServerManager node1 = getNode(30, startupParameters);
        startupParameters.clear();
        startupParameters.put("-DnodeId", "node-2");
        CarbonTestServerManager node2 = getNode(40, startupParameters);
        serverManager.startServers(node1);
        reader1 = new CarbonLogReader(node1.getCarbonHome());
        logManagerNode1.start(reader1);
        Awaitility.await().atMost(SERVER_STARTUP_TIMEOUT, SECONDS).until(() -> checkCoordinationLog(reader1));
        serverManager.startServers(node2);
        reader2 = new CarbonLogReader(node2.getCarbonHome());
        logManagerNode2.start(reader2);
    }

    /**
     * Tests to verify same tasks is not scheduling in 2 node cluster.
     * Starts two nodes as coordinator and member. Schedule the task-1 in the member node.
     * Then schedule the task-1 in coordinator node and checks task is scheduling in both nodes.
     *
     * @throws Exception throws exception
     */
    @Test
    void testSameTaskSchedulingInBothNodes() throws Exception {

        // deploy the task in non-coordinator node
        deployArtifacts(getServerDeploymentDirectory(serverManager.getServerHomes().get(1)),
                Utils.ArtifactType.TASK, TASK_1);
        boolean taskScheduledInNode2 = reader2.checkForLog(deploymentLog(TASK_1), DEPLOYMENT_TIMEOUT);
        log.info(TASK_1 + " is" + (taskScheduledInNode2 ? " " : " not ") + "scheduled in node 2.");

        TimeUnit.SECONDS.sleep(30); // creating deployment delay artificially
        // deploy the task in coordinator node
        deployArtifacts(getServerDeploymentDirectory(serverManager.getServerHomes().get(0)),
                Utils.ArtifactType.TASK, TASK_1);
        boolean taskScheduledInNode1 = reader1.checkForLog(deploymentLog(TASK_1), DEPLOYMENT_TIMEOUT);
        log.info(TASK_1 + " is" + (taskScheduledInNode1 ? " " : " not ") + "scheduled in node 1.");

        log.info("node 1" + taskScheduledInNode1 + " node2 " + taskScheduledInNode2);
        if (taskScheduledInNode1 == taskScheduledInNode2) {
            Assert.fail(TASK_1 + " is scheduled in both nodes or none.");
        }
    }

    private String getServerDeploymentDirectory(String carbonHome) {
        return String.join(File.separator, carbonHome, "repository", "deployment");
    }

    private boolean checkCoordinationLog(CarbonLogReader reader1) throws Exception {
        return reader1.checkForLog(COORDINATOR_LOG, SERVER_STARTUP_TIMEOUT);
    }

    @AfterClass
    void stop() throws Exception {
        logManagerNode1.stopAll();
        logManagerNode2.stopAll();
        serverManager.stopAllServers();
    }
}
