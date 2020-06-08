/*
 * Copyright (c)2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.rabbitmq.inbound;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.rabbitmq.utils.RabbitMQServerInstance;
import org.wso2.carbon.esb.rabbitmq.utils.RabbitMQTestUtils;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonServerExtension;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.rabbitmqclient.RabbitMQProducerClient;

import java.io.File;

/**
 * Includes a test case which deploys a simple rabbitmq inbound endpoint and tests for message consumption through
 * that.
 */
public class RabbitMQInboundTestCase extends ESBIntegrationTest {

    private CarbonLogReader logReader;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        logReader = new CarbonLogReader();
        super.init();
    }

    /**
     * Publishes 200 messages to the queue 'simple_inbound_endpoint_test' and asserts for the log
     * 'received by inbound endpoint = true' which is logged once a message is picked up by the inbound endpoint.
     *
     * @throws Exception if an error occurs while accessing the logViewer client or while publishing messages.
     */
    @Test(groups = { "wso2.esb" }, description = "Test ESB as a RabbitMQ inbound endpoint ")
    public void testRabbitMQInboundEndpoint() throws Exception {
        RabbitMQProducerClient sender;

        sender = RabbitMQServerInstance.createProducerWithDeclaration("exchange", "simple_inbound_endpoint_test");

        logReader.start();
        int messageCount = 2;

        String message = "<ser:placeOrder xmlns:ser=\"http://services.samples\">\n" + "<ser:order>\n"
                + "<ser:price>100</ser:price>\n" + "<ser:quantity>2000</ser:quantity>\n"
                + "<ser:symbol>RMQ</ser:symbol>\n" + "</ser:order>\n" + "</ser:placeOrder>";
        for (int i = 0; i < messageCount; i++) {
            sender.sendMessage(message, "text/plain");
        }

        RabbitMQTestUtils.waitForLogToGetUpdated();
        logReader.stop();
        int messagesConsumed = logReader.getNumberOfOccurencesForLog("received by inbound endpoint = true");
        Assert.assertEquals(messagesConsumed, messageCount, "All messages are not received from queue");

        sender.disconnect();
    }

    /**
     * Deploys a rabbitmq inbound endpoint with invalid rabbitmq broker configurations and asserts the retries the
     * server makes to re-connect to the broker.
     *
     * @throws Exception if an error occurs while accessing the logViewer client or while deploying the inbound
     *                   endpoint.
     */
    @Test(groups = { "wso2.esb" }, description = "Test ESB RabbitMQ inbound endpoint deployment with incorrect server "
            + "port")
    public void testRabbitMQInboundEndpointDeploymentWithInvalidServerConfigs() throws Exception {
        logReader.start();
        CarbonServerExtension.restartServer();
        Thread.sleep(20000);
        RabbitMQTestUtils.waitForLogToGetUpdated();
        Assert.assertTrue(logReader.checkForLog("Attempting to create connection to RabbitMQ Broker in 500ms",
                                                5), "The connection retry delay is incorrect");
        Assert.assertEquals(
                logReader.getNumberOfOccurencesForLog("Attempting to create connection to RabbitMQ Broker"),
                                                      3, "The connection retry count is incorrect");
        logReader.stop();
    }

    @AfterClass(alwaysRun = true)
    public void end() throws Exception {
        super.cleanup();
    }
}
