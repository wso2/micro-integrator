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
import org.wso2.esb.integration.common.utils.clients.rabbitmqclient.RabbitMQConsumerClient;
import org.wso2.esb.integration.common.utils.clients.rabbitmqclient.RabbitMQProducerClient;

import java.io.IOException;

import static org.wso2.carbon.esb.rabbitmq.TestConstants.SIMPLE_CONSUMER_QUEUE;

public class RabbitMQReceiverConnectionRecoveryTestCase extends ESBIntegrationTest {

    private CarbonLogReader logReader;
    private RabbitMQProducerClient sender;
    private RabbitMQConsumerClient consumer;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        sender = new RabbitMQProducerClient("localhost", 5672, "guest", "guest");
        logReader = new CarbonLogReader();
    }

    @Test(groups = {
            "wso2.esb"}, description = "Test ESB as a RabbitMQ Consumer with connection recovery - success case")
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
