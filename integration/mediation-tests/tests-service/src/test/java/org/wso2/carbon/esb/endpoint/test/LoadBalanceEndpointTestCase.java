/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.endpoint.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.LoadbalanceFailoverClient;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

import java.io.IOException;

public class LoadBalanceEndpointTestCase extends ESBIntegrationTest {

    private SampleAxis2Server axis2Server1;
    private SampleAxis2Server axis2Server2;
    private SampleAxis2Server axis2Server3;
    private LoadbalanceFailoverClient lbClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();

        axis2Server1 = new SampleAxis2Server("test_axis2_server_9001.xml");
        axis2Server2 = new SampleAxis2Server("test_axis2_server_9002.xml");
        axis2Server3 = new SampleAxis2Server("test_axis2_server_9003.xml");

        axis2Server1.deployService(SampleAxis2Server.LB_SERVICE_1);
        axis2Server2.deployService(SampleAxis2Server.LB_SERVICE_2);
        axis2Server3.deployService(SampleAxis2Server.LB_SERVICE_3);

        axis2Server1.start();
        axis2Server2.start();
        axis2Server3.start();

        lbClient = new LoadbalanceFailoverClient();
    }

    @AfterClass(alwaysRun = true)
    public void close() {
        log.info("Tests Are Completed");
        if (axis2Server1.isStarted()) {
            axis2Server1.stop();
        }
        if (axis2Server2.isStarted()) {
            axis2Server2.stop();
        }
        if (axis2Server3.isStarted()) {
            axis2Server3.stop();
        }

        axis2Server1 = null;
        axis2Server2 = null;
        axis2Server3 = null;
        lbClient = null;
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a Message to a loadbalancing endpoint")
    public void testSendingToLoaBalancingEndpoint()
            throws IOException {

        String response = lbClient.sendLoadBalanceRequest(getProxyServiceURLHttp("loadbalancingEndPoint"), null);
        Assert.assertNotNull(response, "Response from server 1 is null");
        Assert.assertTrue(response.contains("Response from server: Server_1"), "Expected response not in server 1 response");

        response = lbClient.sendLoadBalanceRequest(getProxyServiceURLHttp("loadbalancingEndPoint"), null);
        Assert.assertNotNull(response, "Response from server 2 is null");
        Assert.assertTrue(response.contains("Response from server: Server_2"), "Expected response not in server 2 response");

        response = lbClient.sendLoadBalanceRequest(getProxyServiceURLHttp("loadbalancingEndPoint"), null);
        Assert.assertNotNull(response, "Response from server 3 is null");
        Assert.assertTrue(response.contains("Response from server: Server_3"), "Expected response not in server 3 response");

    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a Message to a loadbalancing endpoint in Config Reg")
    public void testSendingToLoaBalancingEndpoint_ConfigReg()
            throws IOException {

        String response = lbClient
                .sendLoadBalanceRequest(getProxyServiceURLHttp("loadbalancingEndPoint_Config_Reg"), null);
        Assert.assertNotNull(response, "Response from server 1 is null");
        Assert.assertTrue(response.contains("Response from server: Server_1"), "Expected response not in server 1 response");

        response = lbClient.sendLoadBalanceRequest(getProxyServiceURLHttp("loadbalancingEndPoint_Config_Reg"), null);
        Assert.assertNotNull(response, "Response from server 2 is null");
        Assert.assertTrue(response.contains("Response from server: Server_2"), "Expected response not in server 2 response");

        response = lbClient.sendLoadBalanceRequest(getProxyServiceURLHttp("loadbalancingEndPoint_Config_Reg"), null);
        Assert.assertNotNull(response, "Response from server 3 is null");
        Assert.assertTrue(response.contains("Response from server: Server_3"), "Expected response not in server 3 response");

    }
}
