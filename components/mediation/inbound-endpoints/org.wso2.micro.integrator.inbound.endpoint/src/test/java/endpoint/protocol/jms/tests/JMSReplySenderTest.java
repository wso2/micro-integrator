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
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.protocol.jms.BytesMessageDataSource;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSReplySender;
import org.wso2.carbon.inbound.endpoint.protocol.jms.factory.CachedJMSConnectionFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class JMSReplySenderTest extends TestCase {

    private final static String PROVIDER_URL = "tcp://127.0.0.1:61616";

    /**
     * Test SendBackTextMessages
     *
     * @throws Exception
     */
    @Test
    public void testSendBackTextMessages() throws Exception {
        String replyQueueName = "testQueueReplyTxt";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(replyQueueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            Queue replyQueue = brokerController.connect(replyQueueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            MessageContext messageContext = JMSTestsUtils.createMessageContext();
            String correlationID = UUID.randomUUID().toString();
            this.setSOAPEnvelopWithTextBody(messageContext);
            this.setTransportHeaders(((Axis2MessageContext) messageContext).getAxis2MessageContext(),
                                     JMSConstants.JMS_TEXT_MESSAGE, correlationID);
            messageContext.setProperty(JMSConstants.JMS_COORELATION_ID, correlationID);
            JMSReplySender replySender = new JMSReplySender(replyQueue, cachedJMSConnectionFactory, null, null);
            String soapAction = "urn:test";
            ((Axis2MessageContext) messageContext).getAxis2MessageContext().setServerSide(true);
            ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                    .setProperty(BaseConstants.SOAPACTION, soapAction);
            replySender.sendBack(messageContext);
            Message replyMsg = brokerController.receiveMessage(replyQueue);
            Assert.assertNotNull("The reply message cannot be null", replyMsg);
            Assert.assertEquals("The Message type of received message does not match", JMSConstants.JMS_TEXT_MESSAGE,
                                replyMsg.getJMSType());
            Assert.assertEquals("The Content of received message does not match", "TestSendBack",
                                ((ActiveMQTextMessage) replyMsg).getText());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test SendBackByteMessages
     *
     * @throws Exception
     */
    @Test
    public void testSendBackByteMessages() throws Exception {
        String replyQueueName = "testQueueReplyBinary";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(replyQueueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            Queue replyQueue = brokerController.connect(replyQueueName, true);
            String content = "This is a test";
            BytesMessage message = brokerController.createBytesMessage(content.getBytes());
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            MessageContext messageContext = JMSTestsUtils.createMessageContext();
            String correlationID = UUID.randomUUID().toString();
            this.setSOAPEnvelopWithBinaryBody(messageContext, message);
            this.setTransportHeaders(((Axis2MessageContext) messageContext).getAxis2MessageContext(),
                                     JMSConstants.JMS_BYTE_MESSAGE, correlationID);
            messageContext.setProperty(JMSConstants.JMS_COORELATION_ID, correlationID);
            JMSReplySender replySender = new JMSReplySender(replyQueue, cachedJMSConnectionFactory, null, null);
            String soapAction = "urn:test";
            ((Axis2MessageContext) messageContext).getAxis2MessageContext().getOptions().setAction(soapAction);
            replySender.sendBack(messageContext);
            Message replyMsg = brokerController.receiveMessage(replyQueue);
            Assert.assertNotNull("The reply message cannot be null", replyMsg);
            Assert.assertEquals("The Message type of received message does not match", JMSConstants.JMS_BYTE_MESSAGE,
                                replyMsg.getJMSType());
            Assert.assertEquals("The Content of received message does not match", content,
                                new String(((ActiveMQBytesMessage) replyMsg).getContent().getData()));
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test SendBackMapMessages
     *
     * @throws Exception
     */
    @Test
    public void testSendBackMapMessages() throws Exception {
        String replyQueueName = "testQueueReplyMap";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(replyQueueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            Queue replyQueue = brokerController.connect(replyQueueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            MessageContext messageContext = JMSTestsUtils.createMessageContext();
            String correlationID = UUID.randomUUID().toString();
            this.setSOAPEnvelopWithMapMessageBody(messageContext);
            this.setTransportHeaders(((Axis2MessageContext) messageContext).getAxis2MessageContext(),
                                     JMSConstants.JMS_MAP_MESSAGE, correlationID);
            messageContext.setProperty(JMSConstants.JMS_COORELATION_ID, correlationID);
            JMSReplySender replySender = new JMSReplySender(replyQueue, cachedJMSConnectionFactory, null, null);
            replySender.sendBack(messageContext);
            Message replyMsg = brokerController.receiveMessage(replyQueue);
            Assert.assertNotNull("The reply message cannot be null", replyMsg);
            Assert.assertEquals("The Message type of received message does not match", JMSConstants.JMS_MAP_MESSAGE,
                                replyMsg.getJMSType());
            Assert.assertEquals("The Content of received message does not match", "10",
                                ((ActiveMQMapMessage) replyMsg).getContentMap().get("Price"));
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test SendBackNoPayloadTypeTextMessages
     *
     * @throws Exception
     */
    @Test
    public void testSendBackNoPayloadTypeTextMessages() throws Exception {
        String replyQueueName = "testQueueNoPayloadTypeText";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(replyQueueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            Queue replyQueue = brokerController.connect(replyQueueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            MessageContext messageContext = JMSTestsUtils.createMessageContext();
            String correlationID = UUID.randomUUID().toString();
            this.setSOAPEnvelopWithoutTypeTextMessageBody(messageContext);
            this.setTransportHeaders(((Axis2MessageContext) messageContext).getAxis2MessageContext(),
                                     JMSConstants.JMS_TEXT_MESSAGE, correlationID);
            messageContext.setProperty(JMSConstants.JMS_COORELATION_ID, correlationID);
            JMSReplySender replySender = new JMSReplySender(replyQueue, cachedJMSConnectionFactory, null, null);
            replySender.sendBack(messageContext);
            Message replyMsg = brokerController.receiveMessage(replyQueue);
            Assert.assertNotNull("The reply message cannot be null", replyMsg);
            Assert.assertEquals("The Message type of received message does not match", JMSConstants.JMS_TEXT_MESSAGE,
                                replyMsg.getJMSType());
            Assert.assertTrue("The Content of received message does not match",
                              ((ActiveMQTextMessage) replyMsg).getText().contains("Price"));
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test SendBackNoPayloadTypeByteMessages
     *
     * @throws Exception
     */
    @Test
    public void testSendBackNoPayloadTypeByteMessages() throws Exception {
        String replyQueueName = "testQueueNoPayloadTypeByte";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(replyQueueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            Queue replyQueue = brokerController.connect(replyQueueName, true);
            String content = "This is a test";
            BytesMessage message = brokerController.createBytesMessage(content.getBytes());
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            MessageContext messageContext = JMSTestsUtils.createMessageContext();
            String correlationID = UUID.randomUUID().toString();
            this.setSOAPEnvelopWithoutTypeByteMessageBody(messageContext, message);
            this.setTransportHeaders(((Axis2MessageContext) messageContext).getAxis2MessageContext(),
                                     JMSConstants.JMS_BYTE_MESSAGE, correlationID);
            messageContext.setProperty(JMSConstants.JMS_COORELATION_ID, correlationID);
            JMSReplySender replySender = new JMSReplySender(replyQueue, cachedJMSConnectionFactory, null, null);
            replySender.sendBack(messageContext);
            Message replyMsg = brokerController.receiveMessage(replyQueue);
            Assert.assertNotNull("The reply message cannot be null", replyMsg);
            Assert.assertEquals("The Message type of received message does not match", JMSConstants.JMS_BYTE_MESSAGE,
                                replyMsg.getJMSType());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test SendBackWhenMessageContextNull
     *
     * @throws Exception
     */
    @Test
    public void testSendBackWhenMessageContextNull() throws Exception {
        String replyQueueName = "testQueueNullMsgCtx";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(replyQueueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            Queue replyQueue = brokerController.connect(replyQueueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            MessageContext messageContext = null;
            JMSReplySender replySender = new JMSReplySender(replyQueue, cachedJMSConnectionFactory, null, null);
            replySender.sendBack(messageContext);
            Message replyMsg = brokerController.receiveMessage(replyQueue);
            Assert.assertNull("The message should be null", replyMsg);
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    private void setTransportHeaders(org.apache.axis2.context.MessageContext axis2MsgCxt, String messageType,
                                     String correlationID) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(JMSConstants.JMS_COORELATION_ID, correlationID);
        headerMap.put(JMSConstants.JMS_DELIVERY_MODE, "1");
        headerMap.put(JMSConstants.JMS_EXPIRATION, "10");
        headerMap.put(JMSConstants.JMS_MESSAGE_ID, correlationID);
        headerMap.put(JMSConstants.JMS_PRIORITY, "1");
        headerMap.put(JMSConstants.JMS_TIMESTAMP, "" + new Timestamp(System.currentTimeMillis()).getTime());
        headerMap.put(JMSConstants.JMS_MESSAGE_TYPE, messageType);
        headerMap.put("CUSTOM_HEADER", "HEADER1");
        headerMap.put("Content-Type", "text/xml; charset=\"utf-8\"");
        headerMap.put("Content-Length", axis2MsgCxt.getEnvelope().toString().length());
        axis2MsgCxt.setProperty(JMSConstants.JMS_MESSAGE_TYPE, messageType);
        axis2MsgCxt.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headerMap);
    }

    private void setSOAPEnvelopWithTextBody(MessageContext messageContext) throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement firstEle = fac.createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER);
        firstEle.setText("TestSendBack");
        env.getBody().addChild(firstEle);
        messageContext.setEnvelope(env);
    }

    private void setSOAPEnvelopWithBinaryBody(MessageContext messageContext, BytesMessage message) throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement firstEle = fac.createOMElement(BaseConstants.DEFAULT_BINARY_WRAPPER);
        DataHandler dataHandler = new DataHandler(new BytesMessageDataSource(message));
        OMText textEle = fac.createOMText(dataHandler, true);
        firstEle.addChild(textEle);
        env.getBody().addChild(firstEle);
        messageContext.setEnvelope(env);
    }

    private void setSOAPEnvelopWithMapMessageBody(MessageContext messageContext) throws AxisFault, XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement mapElement1 = fac.createOMElement(new QName("Price"));
        mapElement1.setText("10");
        OMElement mapElement2 = fac.createOMElement(new QName("Name"));
        mapElement2.setText("Queue");
        OMElement firstEle = fac.createOMElement(JMSConstants.JMS_MAP_QNAME);
        firstEle.addChild(mapElement1);
        firstEle.addChild(mapElement2);
        env.getBody().addChild(firstEle);
        messageContext.setEnvelope(env);
    }

    private void setSOAPEnvelopWithoutTypeTextMessageBody(MessageContext messageContext)
            throws AxisFault, XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement mapElement1 = fac.createOMElement(new QName("Price"));
        mapElement1.setText("10");
        OMElement firstEle = fac.createOMElement(new QName("First"));
        firstEle.addChild(mapElement1);
        env.getBody().addChild(firstEle);
        messageContext.setEnvelope(env);
    }

    private void setSOAPEnvelopWithoutTypeByteMessageBody(MessageContext messageContext, BytesMessage message)
            throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement firstEle = fac.createOMElement(new QName("Binary"));
        DataHandler dataHandler = new DataHandler(new BytesMessageDataSource(message));
        OMText textEle = fac.createOMText(dataHandler, true);
        firstEle.addChild(textEle);
        env.getBody().addChild(firstEle);
        messageContext.setEnvelope(env);
    }
}
