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
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import static org.wso2.micro.integrator.TestUtils.LOG_READ_TIMEOUT;
import static org.wso2.micro.integrator.TestUtils.getNode;

public class StartupTests extends ESBIntegrationTest {

    private CarbonTestServerManager testServer;
    private CarbonLogReader logReader;

    @BeforeClass
    public void initialize() throws Exception {

        context = new AutomationContext();
        testServer = getNode(10);
        testServer.startServer();
        logReader = new CarbonLogReader(false, testServer.getCarbonHome());
        logReader.start();
    }

    @Test
    public void testStartup() throws Exception {
        Assert.assertTrue("Server startup log was not located",
                          logReader.checkForLog("WSO2 Micro Integrator started in", LOG_READ_TIMEOUT));
    }

    @AfterClass
    public void clean() throws Exception {
        logReader.stop();
        if (testServer != null) {
            testServer.stopServer();
        }
    }

}
