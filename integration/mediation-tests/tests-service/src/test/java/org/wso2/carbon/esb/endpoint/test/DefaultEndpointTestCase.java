/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.esb.endpoint.test;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

public class DefaultEndpointTestCase extends ESBIntegrationTest {

    private SampleAxis2Server axis2Server1;
    private String wso2Company = "WSO2 Company";

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();

        axis2Server1 = new SampleAxis2Server("test_axis2_server_9001.xml");
        axis2Server1.deployService(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        axis2Server1.start();

    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        if (axis2Server1.isStarted()) {
            axis2Server1.stop();
        }
        axis2Server1 = null;
        super.cleanup();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = {"wso2.esb"}, description = "Sending a Message to a Default endpoint")
    public void testSendingToDefaultEndpoint() throws XPathExpressionException, AxisFault {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("defaultEndPoint"),
                                                                     getBackEndServiceUrl(
                                                                             ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                                     "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains(wso2Company));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = {"wso2.esb"}, description = "Sending a Message to a Default endpoint in Config Reg")
    public void testSendingToDefaultEndpoint_ConfigReg() throws XPathExpressionException, AxisFault {
        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("defaultEndPoint_Config_Reg"),
                                             getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains(wso2Company));
    }

    //Related to Patch Automation https://wso2.org/jira/browse/CARBON-10551
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = {"wso2.esb"}, description = "Sending a Message to a Default endpoint which have Suspension")
    public void testSendingToDefaultEndpointWithSuspension()
            throws IOException, InterruptedException {

        axis2Server1.stop();
        OMElement response = null;
        OMElement response1 = null;
        OMElement response2 = null;
        try {
            response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("defaultEndPointWithSuspension"),
                                                               "http://localhost:9001/services/SimpleStockQuoteService",
                                                               "WSO2");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof org.apache.axis2.AxisFault);
        }
        Assert.assertNull(response, "Received response: " + response);
        axis2Server1.start();
        try {
            response1 = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("defaultEndPointWithSuspension"),
                                                                "http://localhost:9001/services" +
                                                                        "/SimpleStockQuoteService",
                                                                "WSO2");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof org.apache.axis2.AxisFault);
        }
        Assert.assertNull(response1, "Received response: " + response1);
        //Increasing wait time than suspendDuration value
        Thread.sleep(15000);
        response2 = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("defaultEndPoint_Config_Reg"),
                                                            "http://localhost:9001/services/SimpleStockQuoteService",
                                                            "WSO2");
        Assert.assertNotNull(response2);
        Assert.assertTrue(response2.toString().contains("WSO2 Company"), "Received response:" + response2.toString());
    }
}

