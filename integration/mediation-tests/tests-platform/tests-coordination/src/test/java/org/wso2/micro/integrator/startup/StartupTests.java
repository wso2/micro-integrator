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

package org.wso2.micro.integrator.startup;

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

import static org.wso2.micro.integrator.TestUtils.LOG_READ_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.deployArtifacts;
import static org.wso2.micro.integrator.TestUtils.getNode;

public class StartupTests extends ESBIntegrationTest {

    private MultipleServersManager manager = new MultipleServersManager();
    private CarbonTestServerManager node1;
    private CarbonTestServerManager node2;
    private CarbonTestServerManager node3;
    private CarbonLogReader logReader1;
    private CarbonLogReader logReader2;
    private LogReaderManager readerManager;

    @BeforeClass
    public void initialize() throws Exception {

        context = new AutomationContext();
        node1 = getNode(10);
        node2 = getNode(20);
        manager.startServersWithDepSync(true, node1, node2);
        logReader1 = new CarbonLogReader(false, node1.getCarbonHome());
        logReader2 = new CarbonLogReader(false, node2.getCarbonHome());
        readerManager = new LogReaderManager();
        readerManager.start(logReader1, logReader2);
        deployArtifacts(manager.getDeploymentDirectory(), Utils.ArtifactType.TASK, "task-1");
    }

    @Test
    public void testClusterJoin() throws Exception {

        if (!logReader1.checkForLog("Successfully joined the cluster", LOG_READ_TIMEOUT)) {
            Assert.fail("Cluster join failed for node 1.");
        }
        if (!logReader2.checkForLog("Successfully joined the cluster", LOG_READ_TIMEOUT)) {
            Assert.fail("Cluster join failed for node 2.");
        }
    }

    @Test(dependsOnMethods = { "testClusterJoin" })
    public void testStartup() throws Exception {
        if (logReader1.checkForLog("ERROR", LOG_READ_TIMEOUT)) {
            Assert.fail("Node 1 started with errors");
        }
        if (logReader2.checkForLog("ERROR", LOG_READ_TIMEOUT)) {
            Assert.fail("Node 2 started with errors");
        }
    }

    @Test(dependsOnMethods = { "testStartup" })
    public void testMemberAddition() throws Exception {

        boolean additionLog1 = logReader1.checkForLog("Member added", LOG_READ_TIMEOUT);
        boolean additionLog2 = logReader2.checkForLog("Member added", LOG_READ_TIMEOUT);
        if (!additionLog1 && !additionLog2) {
            Assert.fail("Member addition is not detected in any of the nodes");
        }
        if (additionLog1 == additionLog2) {
            Assert.fail("Member addition log is present in both nodes. Since two node cluster, one should have "
                                + "skipped self logging.");
        }
        logReader2.clearLogs();
        node1.stopServer();
    }

    @Test(dependsOnMethods = { "testMemberAddition" })
    public void testMemberRemoval() throws Exception {
        if (!logReader2.checkForLog("Member removed", LOG_READ_TIMEOUT)) {
            Assert.fail("Member removal is not detected in node 2.");
        }
        node3 = getNode(5);
        node3.startServer();
    }

    @Test(dependsOnMethods = { "testMemberRemoval" })
    public void testCoordinator() throws Exception {
        CarbonLogReader logReader3 = new CarbonLogReader(false, node3.getCarbonHome());
        readerManager.start(logReader3);
        node2.stopServer();
        if (!logReader3.checkForLog("Current node state changed from: MEMBER to: COORDINATOR", LOG_READ_TIMEOUT)) {
            Assert.fail("Coordinator change is not detected in node 3.");
        }
    }

    @Test(dependsOnMethods = { "testCoordinator" })
    public void testReStart() throws Exception {
        node3.stopServer();
        node3.startServer();
        CarbonLogReader logReader3 = new CarbonLogReader(false, node3.getCarbonHome());
        if (logReader3.checkForLog("ERROR", 30)) {
            Assert.fail("Node 3 re-started with errors");
        }
    }

    @AfterClass
    public void clean() throws Exception {
        readerManager.stopAll();
        if (node3 != null) {
            node3.stopServer();
        }
    }

}
