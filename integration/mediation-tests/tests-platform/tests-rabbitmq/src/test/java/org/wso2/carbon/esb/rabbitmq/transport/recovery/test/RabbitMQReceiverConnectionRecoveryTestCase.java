/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.rabbitmq.transport.recovery.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.rabbitmq.utils.RabbitMQTestUtils;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.clients.rabbitmqclient.RabbitMQConsumerClient;
import org.wso2.esb.integration.common.utils.clients.rabbitmqclient.RabbitMQProducerClient;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.io.File;
import java.io.IOException;

import static org.wso2.carbon.esb.rabbitmq.TestConstants.SIMPLE_CONSUMER_QUEUE;

public class RabbitMQReceiverConnectionRecoveryTestCase extends ESBIntegrationTest {

    private CarbonLogReader logReader;
    private RabbitMQProducerClient sender;
    private RabbitMQConsumerClient consumer;
    private final String PROXY_NAME = "RabbitMQConsumerProxy";
    private final String SOURCE_DIR =
            TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP) + File.separator
            + "server" + File.separator + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "synapse"
            + "-configs" + File.separator + "default" + File.separator + "proxy-services" + File.separator;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        sender = new RabbitMQProducerClient("localhost", 5672, "guest", "guest");
        logReader = new CarbonLogReader();
    }

    @Test(groups = {
            "wso2.esb"}, description = "Test ESB as a RabbitMQ Consumer with connection recovery when the rabbit MQ "
                                       + "server goes down while messaging")
    public void testRabbitMQConsumerRecoverySuccess() throws Exception {
        logReader.start();

        //publish 10 messages to broker and wait for ESB to pick up the messages
        publishMessages(10);
        Thread.sleep(30000);

        //Stop rabbitmq server
        RabbitMQTestUtils.stopRabbitMq();

        //Recovery time is 10000(retry interval) * 5 (retry count) ms. Therefore continue within recovery time.
        Thread.sleep(30000);

        //Restart the server
        RabbitMQTestUtils.startRabbitMq();
        Thread.sleep(10000);

        //publish another 10 messages to broker and wait for ESB to pick up the messages
        sender = new RabbitMQProducerClient("localhost", 5672, "guest", "guest");
        publishMessages(10);
        Thread.sleep(30000);
        logReader.stop();

        Assert.assertEquals(logReader.getNumberOfOccurencesForLog("received = true"),
                            20, "All messages are not received from queue");

        //All 20 messages (10 messages before restarting and 10 messages after restarting)
        consumer = new RabbitMQConsumerClient("localhost");
        consumer.declareAndConnect("exchange2", SIMPLE_CONSUMER_QUEUE);
        Assert.assertEquals(consumer.popAllMessages().size(), 0, "All messages are not received from ESB");
    }

    @Test(groups = {
            "wso2.esb"}, description = "Test ESB as a RabbitMQ Consumer with connection recovery when the RabbitMQ "
                                       + "server is down when the proxy service is deployed and it doesn't come up "
                                       + "before the recovery interval")
    public void testRabbitMQFailureConnectionRetry() throws Exception {
        logReader.start();

        //Stop rabbitmq server
        RabbitMQTestUtils.stopRabbitMq();

        //Re-deploy the proxy service
        undeployProxyService(PROXY_NAME);
        deployProxyService(PROXY_NAME, SOURCE_DIR);

        //Recovery time is 10000(retry interval) * 5 (retry count) = 50000 ms. Therefore the RabbitMQ server starts
        // after the recovery time.
        Thread.sleep(70000);

        //Start rabbitmq server
        RabbitMQTestUtils.startRabbitMq();
        Thread.sleep(10000);
        logReader.stop();
        String retryLog = "Attempting to create connection to RabbitMQ Broker in 10000 ms";

        //Assert connection delay and retry attempts
        Assert.assertTrue(logReader.checkForLog(retryLog, 5), "The connection retry delay is incorrect");
        Assert.assertEquals(
                logReader.getNumberOfOccurencesForLog(retryLog), 5, "The connection retry count is incorrect");
    }

    @Test(groups = {
            "wso2.esb"}, description = "Test ESB as a RabbitMQ Consumer with connection recovery when the RabbitMQ "
                                       + "server is down when the proxy service is deployed and it does come up "
                                       + "before the recovery interval")
    public void testRabbitMQSuccessfulConnectionRetry() throws Exception {
        logReader.start();

        //Stop rabbitmq server
        RabbitMQTestUtils.stopRabbitMq();

        //Re-deploy the proxy service
        undeployProxyService(PROXY_NAME);
        deployProxyService(PROXY_NAME, SOURCE_DIR);

        //Recovery time is 10000(retry interval) * 5 (retry count) ms. Therefore continue within recovery time.
        Thread.sleep(20000);

        //Restart rabbitmq server
        RabbitMQTestUtils.startRabbitMq();
        Thread.sleep(20000);

        logReader.stop();
        String retryLog = "Attempting to create connection to RabbitMQ Broker in 10000 ms";
        Assert.assertTrue(logReader.checkForLog(retryLog, 5), "The connection retry delay is incorrect");
        Assert.assertTrue(logReader.checkForLog("Successfully connected to RabbitMQ Broker", 5),
                          "Unable to establish the connection with the broker");
        logReader.clearLogs();
        logReader.start();
        //publish another 10 messages to broker and wait for ESB to pick up the messages
        sender = new RabbitMQProducerClient("localhost", 5672, "guest", "guest");
        publishMessages(10);
        Thread.sleep(30000);
        logReader.stop();

        Assert.assertEquals(logReader.getNumberOfOccurencesForLog("received = true"),
                            10, "All messages are not received from queue");

        //All 20 messages (10 messages before restarting and 10 messages after restarting)
        consumer = new RabbitMQConsumerClient("localhost");
        consumer.declareAndConnect("exchange2", SIMPLE_CONSUMER_QUEUE);
        Assert.assertEquals(consumer.popAllMessages().size(), 0, "All messages are not received from ESB");
    }

    /**
     * Publish messages to broker
     *
     * @param messageCount number of messages to publish
     */
    private void publishMessages(int messageCount) {
        try {
            sender.declareAndConnect("exchange2", SIMPLE_CONSUMER_QUEUE);
            for (int i = 0; i < messageCount; i++) {
                String message = "<ser:placeOrder xmlns:ser=\"http://services.samples\">\n" + "<ser:order>\n"
                                 + "<ser:price>100</ser:price>\n" + "<ser:quantity>2000</ser:quantity>\n"
                                 + "<ser:symbol>RMQ</ser:symbol>\n" + "</ser:order>\n" + "</ser:placeOrder>";
                sender.sendMessage(message, "text/plain");
            }
        } catch (IOException e) {
            Assert.fail("Could not connect to RabbitMQ broker");
        } finally {
            sender.disconnect();
        }
    }

    @AfterClass(alwaysRun = true)
    public void end() throws Exception {
        super.cleanup();
        sender = null;
        consumer = null;
    }
}
