/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.rabbitmq.dual.channel;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

public class RabbitMQDualChannelTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"}, description = "Test dual channel scenario with RabbitMQ")
    public void testRabbitMQDualChannel() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            OMElement response = client.sendReceive(Utils.getStockQuoteRequest("RMQ"),
                                                    getProxyServiceURLHttp("RabbitMQDualChannelTestSenderProxy"),
                                                    "urn:mediate");
            String expectedResponse = "<status>Dual Channel Response Received</status>";
            Assert.assertEquals(response.toString(), expectedResponse);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Test dual channel scenario with RabbitMQ while the end point is down "
                                               + "and the consumer proxy responds with a custom error message.")
    public void testRabbitMQDualChannelWithBackEndDown() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        OMElement response = client.sendReceive(Utils.getStockQuoteRequest("RMQ"),
                                                getProxyServiceURLHttp(
                                                        "RabbitMQDualChannelWithBackEndDownTestSenderProxy"),
                                                "urn:mediate");
        String expectedResponse = "<status><Error>Error connecting to the back end</Error></status>";
        Assert.assertEquals(response.toString(), expectedResponse);
    }

    @Test(groups = {"wso2.esb"}, description = "Test dual channel scenario with RabbitMQ while the end point is down "
                                               + "and the consumer proxy drops the error message.")
    public void testRabbitMQDualChannelWithBackEndDownWithNoResponse() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        OMElement response = client.sendReceive(Utils.getStockQuoteRequest("RMQ"),
                                                getProxyServiceURLHttp(
                                                        "RabbitMQDualChannelNoResponseWithBackEndDownTestSenderProxy"),
                                                "urn:mediate");
        String expectedResponse = "<status>Dual-channel fault invoked</status>";
        Assert.assertEquals(response.toString(), expectedResponse);
    }

    @AfterClass(alwaysRun = true)
    public void end() throws Exception {
        super.cleanup();
    }
}
