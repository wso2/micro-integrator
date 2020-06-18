/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.mqtt.inbound.transport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.esb.jms.utils.JMSBroker;
import org.wso2.carbon.esb.mqtt.utils.MQTTTestClient;
import org.wso2.carbon.esb.mqtt.utils.QualityOfService;
import org.wso2.esb.integration.common.extensions.jmsserver.ActiveMQServerExtension;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;

/**
 * This test case is to check if Inbound MQTT Transport receives MQTT messages once deployed
 * 1. Start ActiveMQ server (MQTT Provider)
 * 2. Deploy MQTT transport configuration
 * 3. Send a sample MQTT message
 * 4. Inspect logs and check if message is consumed
 */
public class MQTTInboundMessagePollingTestCase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader = new CarbonLogReader();
    private JMSBroker activeMQServer;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        ActiveMQServerExtension.stopMQServer();
        activeMQServer = new JMSBroker("MQTTBroker",
                JMSBrokerConfigurationProvider.getInstance().getTransportConnectors());
        activeMQServer.start();
        carbonLogReader.start();
        File mqttInbound = new File(
                getESBResourceLocation() + File.separator + "mqtt" + File.separator + "inbound" + File.separator
                        + "transport" + File.separator + "MQTT_Test_Inbound_EP.xml");
        Utils.deploySynapseConfiguration(mqttInbound, Utils.ArtifactType.INBOUND_ENDPOINT);
    }

    @Test(groups = { "wso2.esb" }, description = "Check if Inbound MQTT Transport receives messages without issue")
    public void testMQTTInboundEndpointMessagePolling() throws Exception {
        //connect to broker and publish a message
        String brokerURL = "tcp://localhost:1883";
        String userName = "admin";
        char[] password = "admin".toCharArray();
        String publisherClientId = "publisher1";
        String topic = "esb.test1";
        String messageToSend = "<msg><a>Testing123</a></msg>";
        byte[] payload = messageToSend.getBytes();
        MQTTTestClient mqttPublisherClient = null;
        Assert.assertTrue(carbonLogReader.checkForLog("MQTT_Test_Inbound_EP connected to the broker",
                                                      60), "MQTT_Test_Inbound_EP could not connect to the broker");
        try {
            mqttPublisherClient = new MQTTTestClient(brokerURL, userName, password, publisherClientId);
            mqttPublisherClient.publishMessage(topic, payload, QualityOfService.LEAST_ONCE.getValue(), false);
        } finally {
            if (mqttPublisherClient != null) {
                mqttPublisherClient.disconnect();
            }
        }

        //check EI log to see if message is consumed
        boolean result = carbonLogReader.checkForLog(messageToSend, DEFAULT_TIMEOUT);
        Assert.assertTrue(result, "Message is not found in log. Expected : " + messageToSend);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        activeMQServer.stop();
        ActiveMQServerExtension.startMQServer();
        Utils.undeploySynapseConfiguration("MQTT_Test_Inbound_EP", "inbound-endpoints", false);
        carbonLogReader.stop();
    }

}
