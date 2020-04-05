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

import java.util.concurrent.TimeUnit;

import static org.wso2.micro.integrator.TestUtils.CLUSTER_DEP_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.CLUSTER_TASK_RESCHEDULE_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.deployTasks;
import static org.wso2.micro.integrator.TestUtils.deploymentLog;
import static org.wso2.micro.integrator.TestUtils.getNode;

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
    private static final String logCompleted = "completed-task-execution-cluster";

    @BeforeClass
    void initialize() throws Exception {

        context = new AutomationContext();
        serverManager = new MultipleServersManager();
        logManager = new LogReaderManager();
        node1 = getNode(30);
        node2 = getNode(40);
        serverManager.startServersWithDepSync(true, node1, node2);
        reader1 = new CarbonLogReader(node1.getCarbonHome());
        reader2 = new CarbonLogReader(node2.getCarbonHome());
    }

    @Test
    void testTaskScheduling() throws Exception {

        logManager.start(reader1, reader2);
        deployTasks(serverManager.getDeploymentDirectory(), TASK_1, TASK_2, TASK_COMPLETE);
        taskScheduledInNode1 = reader1.checkForLog(deploymentLog(TASK_1), CLUSTER_DEP_TIMEOUT);
        log.info(TASK_1 + " is" + (taskScheduledInNode1 ? " " : " not ") + "scheduled in node 1.");
        if (taskScheduledInNode1 == reader2.checkForLog(deploymentLog(TASK_1), CLUSTER_DEP_TIMEOUT)) {
            Assert.fail("Deployment failed for " + TASK_1);
        }
    }

    @Test(dependsOnMethods = { "testTaskScheduling" },
            description = "check whether all tasks are scheduled in single node since default is active passive "
                    + "resolver")
    void testTaskNode() throws Exception {

        if (taskScheduledInNode1) {
            if (!reader1.checkForLog(deploymentLog(TASK_2), 1)) {
                Assert.fail("Task 2 is not scheduled in node 1 though task 1 is scheduled in it.");
            }
        } else {
            if (!reader2.checkForLog(deploymentLog(TASK_2), 1)) {
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

    @Test(dependsOnMethods = { "testTaskNode" })
    void testTaskExecutionCount() {

        if (taskScheduledInNode1) {
            if (1 != reader1.getNumberOfOccurencesForLog(logCompleted)) {
                Assert.fail(TASK_COMPLETE + " has ran more than specified time in node 1");
            }
        } else {
            if (1 != reader2.getNumberOfOccurencesForLog(logCompleted)) {
                Assert.fail(TASK_COMPLETE + " has ran more than specified time in node 2");
            }
        }
    }

    @Test(dependsOnMethods = { "testTaskExecution", "testTaskExecutionCount" })
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

    @AfterClass
    void stop() throws Exception {
        logManager.stopAll();
        serverManager.stopAllServers();
    }
}
