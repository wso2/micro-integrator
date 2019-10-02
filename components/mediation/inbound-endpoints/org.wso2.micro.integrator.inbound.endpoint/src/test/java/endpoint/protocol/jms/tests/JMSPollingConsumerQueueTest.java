/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package endpoint.protocol.jms.tests;

import endpoint.protocol.jms.JMSBrokerController;
import endpoint.protocol.jms.JMSTestsUtils;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSPollingConsumer;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSTask;

import java.util.Properties;
import javax.jms.Message;

public class JMSPollingConsumerQueueTest extends TestCase {

    private static final String PROVIDER_URL = "tcp://127.0.0.1:61616";
    private static final String INBOUND_EP_NAME = "testPolling";
    private static final long INTERVAL = 1000;
    private static final String SEND_MSG = "<?xml version='1.0' encoding='UTF-8'?>"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
            + " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
            + "  <soapenv:Header/>" + "  <soapenv:Body>" + "    <ser:getQuote> " + "      <ser:request>"
            + "        <xsd:symbol>IBM</xsd:symbol>" + "      </ser:request>" + "    </ser:getQuote>"
            + "  </soapenv:Body>" + "</soapenv:Envelope>";

    /**
     * Test Run Inbound Task to poll messages from Queue
     *
     * @throws Exception
     */
    @Test
    public void testPollingOnQueue() throws Exception {
        String queueName = "testQueue1";
        boolean isQueueExist = false;
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
        InboundTask task = new JMSTask(jmsPollingConsumer, INTERVAL);
        Assert.assertEquals(task.getInboundProperties().getProperty(JMSConstants.PROVIDER_URL), PROVIDER_URL);
        try {
            brokerController.startProcess();
            task.execute();
            ActiveMQDestination[] activeMQDestination = brokerController.getBrokerService().getRegionBroker().
                    getDestinations();
            for (ActiveMQDestination destination : activeMQDestination) {
                if (destination.isQueue() && queueName.equals(destination.getPhysicalName())) {
                    isQueueExist = true;
                }
            }
            Assert.assertTrue("Queue is not added as a subscription", isQueueExist);
        } finally {
            task.destroy();
            brokerController.stopProcess();
        }
    }

    /**
     * Test Polling Messages From Queue when the JMS Spec Version is 1.1
     *
     * @throws Exception
     */
    @Test
    public void testPollingMessageFromQueue() throws Exception {
        String queueName = "testQueue1";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            brokerController.connect(queueName, true);
            brokerController.pushMessage(SEND_MSG);
            JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
            Message receivedMsg = JMSTestsUtils.pollMessagesFromDestination(jmsPollingConsumer);
            Assert.assertNotNull("Received message is null", receivedMsg);
            Assert.assertEquals("The send message is not received.", SEND_MSG,
                                ((ActiveMQTextMessage) receivedMsg).getText());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test Polling Messages From Queue when the JMS Spec Version is 2.0
     *
     * @throws Exception
     */
    @Test
    public void testPollingMessageFromQueueSpecV20() throws Exception {
        String queueName = "testQueue1v20";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        jmsProperties.put(JMSConstants.PARAM_JMS_SPEC_VER, JMSConstants.JMS_SPEC_VERSION_2_0);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            brokerController.connect(queueName, true);
            brokerController.pushMessage(SEND_MSG);
            JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
            Message receivedMsg = JMSTestsUtils.pollMessagesFromDestination(jmsPollingConsumer);
            Assert.assertNotNull("Received message is null", receivedMsg);
            Assert.assertEquals("The send message is not received.", SEND_MSG,
                                ((ActiveMQTextMessage) receivedMsg).getText());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test Polling Messages From Queue when the JMS Spec Version is 1.0
     *
     * @throws Exception
     */
    @Test
    public void testPollingMessageFromQueueSpecV10() throws Exception {
        String queueName = "testQueue1v20";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        jmsProperties.put(JMSConstants.PARAM_JMS_SPEC_VER, JMSConstants.JMS_SPEC_VERSION_1_0);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            brokerController.connect(queueName, true);
            brokerController.pushMessage(SEND_MSG);
            JMSPollingConsumer jmsPollingConsumer = new JMSPollingConsumer(jmsProperties, INTERVAL, INBOUND_EP_NAME);
            Message receivedMsg = JMSTestsUtils.pollMessagesFromDestination(jmsPollingConsumer);
            Assert.assertNotNull("Received message is null", receivedMsg);
            Assert.assertEquals("The send message is not received.", SEND_MSG,
                                ((ActiveMQTextMessage) receivedMsg).getText());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

}
