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
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSInjectHandler;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSUtils;

import java.util.Properties;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.naming.InitialContext;

public class JMSUtilsTest extends TestCase {

    private final static String PROVIDER_URL = "tcp://127.0.0.1:61616";

    /**
     * Test ConvertJMSMapToXML
     *
     * @throws Exception
     */
    @Test
    public void testConvertJMSMapToXML() throws Exception {
        String queueName = "testHandler";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, false);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            brokerController.connect(queueName, true);
            MapMessage mapMessage = brokerController.createMapMessage();
            mapMessage.setStringProperty("MessageFormat", "Person");
            mapMessage.setString("NAME", queueName);
            mapMessage.setInt("COUNT", 10);
            mapMessage.setDouble("PRICE", 12.00);
            OMElement result = JMSInjectHandler.convertJMSMapToXML(mapMessage);
            Assert.assertEquals("The converted XML is not correct", "10", ((OMElement) result.
                    getChildrenWithLocalName("COUNT").next()).getText());
            Assert.assertEquals("The converted XML is not correct", queueName, ((OMElement) result.
                    getChildrenWithLocalName("NAME").next()).getText());
        } finally {
            brokerController.stopProcess();
        }
    }

    /**
     * Test convertXMLtoJMSMap
     *
     * @throws Exception
     */
    @Test
    public void testConvertXMLtoJMSMap() throws Exception {
        String queueName = "testHandler1";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, false);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            brokerController.connect(queueName, true);
            MapMessage mapMessage = brokerController.createMapMessage();
            OMElement omElement = AXIOMUtil.stringToOM(
                    "<JMSMap xmlns=\"http://axis.apache.org/axis2/java/transports/jms/map-payload\">"
                            + "<PRICE>12.0</PRICE>" + "<COUNT>10</COUNT>" + "<NAME>" + queueName + "</NAME>"
                            + "</JMSMap>");
            JMSUtils.convertXMLtoJMSMap(omElement, mapMessage);
            Assert.assertEquals("The converted JMS Map is not correct", "12.0", ((ActiveMQMapMessage) mapMessage).
                    getContentMap().get("PRICE"));
            Assert.assertEquals("The converted JMS Map is not correct", queueName, ((ActiveMQMapMessage) mapMessage).
                    getContentMap().get("NAME"));
        } finally {
            brokerController.stopProcess();
        }
    }

    /**
     * Test lookupDestination
     *
     * @throws Exception
     */
    @Test
    public void testLookupDestination() throws Exception {
        String queueName = "testQueueExist";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            InitialContext ctx = new InitialContext(jmsProperties);
            brokerController.connect(queueName, true);
            Destination existDest = JMSUtils.lookupDestination(ctx, queueName, JMSConstants.DESTINATION_TYPE_QUEUE);
            Assert.assertEquals("The destination should be exist", queueName,
                                ((ActiveMQQueue) existDest).getPhysicalName());
            Destination nullDest = JMSUtils.lookupDestination(ctx, null, JMSConstants.DESTINATION_TYPE_QUEUE);
            Assert.assertNull("Destination should be null when the destination name is null", nullDest);
            String notExistQueueName = "Not_Exist";
            Destination nonExistDest = JMSUtils
                    .lookupDestination(ctx, notExistQueueName, JMSConstants.DESTINATION_TYPE_QUEUE);
            Assert.assertEquals("The destination should be exist", notExistQueueName,
                                ((ActiveMQQueue) nonExistDest).getPhysicalName());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test inferJMSMessageType
     *
     * @throws Exception
     */
    @Test
    public void testInferJMSMessageType() throws Exception {
        String queueName = "testQueueExist1";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            brokerController.connect(queueName, true);
            Message mapMessage = brokerController.createMapMessage();
            String mapClassName = JMSUtils.inferJMSMessageType(mapMessage);
            Assert.assertEquals("The Class name should be javax.jms.MapMessage", "javax.jms.MapMessage", mapClassName);
            String text = "This is a test";
            Message textMessage = brokerController.pushMessage(text);
            String textClassName = JMSUtils.inferJMSMessageType(textMessage);
            Assert.assertEquals("The Class name should be javax.jms.TextMessage", "javax.jms.TextMessage",
                                textClassName);
            Message byteMessage = brokerController.createBytesMessage(text.getBytes());
            String byteClassName = JMSUtils.inferJMSMessageType(byteMessage);
            Assert.assertEquals("The Class name should be javax.jms.BytesMessage", "javax.jms.BytesMessage",
                                byteClassName);
            Message objMessage = brokerController.createObjectMessage();
            String objClassName = JMSUtils.inferJMSMessageType(objMessage);
            Assert.assertEquals("The Class name should be javax.jms.ObjectMessage", "javax.jms.ObjectMessage",
                                objClassName);
            Message streamMessage = brokerController.createStreamMessage();
            String streamClassName = JMSUtils.inferJMSMessageType(streamMessage);
            Assert.assertEquals("The Class name should be javax.jms.StreamMessage", "javax.jms.StreamMessage",
                                streamClassName);
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

}
