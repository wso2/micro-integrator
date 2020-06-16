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

package org.wso2.micro.integrator.tasks;

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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.wso2.micro.integrator.TestUtils.CLUSTER_DEP_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.CLUSTER_TASK_RESCHEDULE_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.deployArtifacts;
import static org.wso2.micro.integrator.TestUtils.deploymentLog;
import static org.wso2.micro.integrator.TestUtils.getNode;
import static org.wso2.micro.integrator.TestUtils.isTaskExistInStore;

/**
 * Tests to verify tasks scheduling in 2 node cluster.
 */
public class TaskTests extends ESBIntegrationTest {

    private MultipleServersManager serverManager;
    private CarbonTestServerManager node1;
    private CarbonTestServerManager node2;
    private CarbonLogReader reader1;
    private CarbonLogReader reader2;
    private LogReaderManager logManager;
    private boolean taskScheduledInNode1 = false;
    private static final String TASK_1 = "task-1";
    private static final String TASK_2 = "task-2";
    private static final String TASK_COMPLETE = "completed-task";
    private static final String TASK_PINNED = "pinned-task";
    private static final String logCompleted = "completed-task-execution-cluster";

    @BeforeClass
    void initialize() throws Exception {

        context = new AutomationContext();
        serverManager = new MultipleServersManager();
        logManager = new LogReaderManager();
        HashMap<String, String> startupParameters = new HashMap<>();
        startupParameters.put("-DSynapseServerName", "pinnedServerCluster");
        startupParameters.put("-DnodeId", "node-1");
        node1 = getNode(30, startupParameters);
        startupParameters.clear();
        startupParameters.put("-DnodeId", "node-2");
        node2 = getNode(40, startupParameters);
        serverManager.startServersWithDepSync(true, node1, node2);
        reader1 = new CarbonLogReader(node1.getCarbonHome());
        reader2 = new CarbonLogReader(node2.getCarbonHome());
        logManager.start(reader1, reader2);
        deployArtifacts(serverManager.getDeploymentDirectory(), Utils.ArtifactType.TASK, TASK_1, TASK_2, TASK_COMPLETE,
                        TASK_PINNED);
    }

    @Test
    void testTaskScheduling() throws Exception {

        taskScheduledInNode1 = reader1.checkForLog(deploymentLog(TASK_1), CLUSTER_DEP_TIMEOUT);
        log.info(TASK_1 + " is" + (taskScheduledInNode1 ? " " : " not ") + "scheduled in node 1.");
        if (taskScheduledInNode1 == reader2.checkForLog(deploymentLog(TASK_1), CLUSTER_DEP_TIMEOUT)) {
            Assert.fail("Deployment failed for " + TASK_1);
        }
    }

    @Test
    void testPinnedTaskScheduling() throws Exception {

        if (!reader1.checkForLog(deploymentLog(TASK_PINNED), CLUSTER_DEP_TIMEOUT)) {
            Assert.fail("Pinned task is not deployed in node 1");
        }
        if (!reader2.checkForLog("Server name not in pinned servers list. Not starting Task : " + TASK_PINNED,
                                 CLUSTER_DEP_TIMEOUT)) {
            Assert.fail("Pinned task is not skipped in node 2");
        }
        if (isTaskExistInStore(TASK_PINNED)) {
            Assert.fail("Pinned task is not supposed to be added to task store.");
        }
    }

    @Test(dependsOnMethods = { "testTaskScheduling" },
            description = "check whether all tasks are scheduled in single node since default is active passive "
                    + "resolver")
    void testTaskNode() throws Exception {

        if (taskScheduledInNode1) {
            if (!reader1.checkForLog(deploymentLog(TASK_2), DEFAULT_TIMEOUT)) {
                Assert.fail("Task 2 is not scheduled in node 1 though task 1 is scheduled in it.");
            }
        } else {
            if (!reader2.checkForLog(deploymentLog(TASK_2), DEFAULT_TIMEOUT)) {
                Assert.fail("Task 2 is not scheduled in node 2 though task 1 is scheduled in it.");
            }
        }
    }

    @Test(dependsOnMethods = { "testTaskNode" })
    void testTaskExecution() throws Exception {

        if (taskScheduledInNode1) {
            if (!reader1.checkForLog(logCompleted, 1)) {
                Assert.fail(TASK_COMPLETE + " didn't run in node 1");
            }
        } else {
            if (!reader2.checkForLog(logCompleted, 1)) {
                Assert.fail(TASK_COMPLETE + " didn't run in node 2");
            }
        }
    }

    @Test(dependsOnMethods = { "testTaskExecution" })
    void testTaskExecutionCount() {

        if (taskScheduledInNode1) {
            int count = reader1.getNumberOfOccurencesForLog(logCompleted);
            if (count != 1) {
                Assert.fail(TASK_COMPLETE + " has ran " + count + " no of times.");
            }
        } else {
            int count = reader2.getNumberOfOccurencesForLog(logCompleted);
            if (count != 1) {
                Assert.fail(TASK_COMPLETE + " has ran " + count + " no of times.");
            }
        }
    }

    @Test(dependsOnMethods = { "testTaskExecution", "testTaskExecutionCount", "testPinnedTaskScheduling" })
    void testTaskReSchedulingOfCompletedTask() throws Exception {

        // For the task node to update completed status to task store since we are going to stop it
        TimeUnit.SECONDS.sleep(30);
        if (taskScheduledInNode1) {
            reader2.clearLogs();
            node1.stopServer();
            if (reader2.checkForLog(deploymentLog(TASK_COMPLETE), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Completed task is rescheduled in node 2 when node 1 left the cluster");
            }
        } else {
            reader1.clearLogs();
            node2.stopServer();
            if (reader1.checkForLog(deploymentLog(TASK_COMPLETE), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Completed task is rescheduled in node 1 when node 2 left the cluster");
            }
        }
    }

    @Test(dependsOnMethods = { "testTaskReSchedulingOfCompletedTask" })
    void testCompletedTaskExistenceUponMemberRemoval() throws Exception {
        if (!isTaskExistInStore(TASK_COMPLETE)) {
            Assert.fail("Completed task is removed from task store when the running node left the cluster.");
        }
    }

    @Test(dependsOnMethods = { "testTaskReSchedulingOfCompletedTask" })
    void testTaskReScheduling() throws Exception {

        if (taskScheduledInNode1) {
            if (!reader2.checkForLog(deploymentLog(TASK_1), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Task 1 reschedule failed in node 2 ");
            }
            if (!reader2.checkForLog(deploymentLog(TASK_2), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Task 2 reschedule failed in node 2 ");
            }
        } else {
            if (!reader1.checkForLog(deploymentLog(TASK_1), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Task 1 reschedule failed in node 1");
            }
            if (!reader1.checkForLog(deploymentLog(TASK_2), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Task 2 reschedule failed in node 1");
            }
        }
    }

    @Test(dependsOnMethods = { "testTaskReScheduling" },
            description = "https://github.com/wso2/micro-integrator/issues/1658")
    void testDeletionOfCompletedTask() throws Exception {

        serverManager.stopAllServers();
        if (taskScheduledInNode1) {
            reader1.clearLogs();
            node1.startServer();
            if (reader1.checkForLog(deploymentLog(TASK_COMPLETE), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Completed task got scheduled when server is restarted.");
            }
        } else {
            reader2.clearLogs();
            node2.startServer();
            if (reader2.checkForLog(deploymentLog(TASK_COMPLETE), CLUSTER_TASK_RESCHEDULE_TIMEOUT)) {
                Assert.fail("Completed task got scheduled when server is restarted.");
            }
        }
    }

    @AfterClass
    void stop() throws Exception {
        logManager.stopAll();
        serverManager.stopAllServers();
    }
}
