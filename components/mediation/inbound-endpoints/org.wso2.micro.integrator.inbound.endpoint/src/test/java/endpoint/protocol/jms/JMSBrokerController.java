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
package endpoint.protocol.jms;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSBrokerController {

    private static final Log log = LogFactory.getLog(JMSBrokerController.class);

    private String providerURL;
    private BrokerService broker;
    private Connection connection = null;
    private Session session = null;
    private MessageProducer producer = null;
    private QueueConnectionFactory connectionFactory = null;

    public JMSBrokerController(String providerURL, Properties properties) {
        this.providerURL = providerURL;
        InitialContext ctx;
        try {
            ctx = new InitialContext(properties);
            this.connectionFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");
        } catch (NamingException e) {
            log.info("Error while creating connection factory");
        }
    }

    public boolean startProcess() {
        try {
            //using embedded jms broker
            broker = new BrokerService();
            // configure the broker
            broker.setBrokerName("inboundSampleBroker");
            broker.addConnector(providerURL);
            broker.setPersistent(true);
            broker.start();
            log.info("JMSBrokerController: ActiveMQ Broker is Successfully started. continuing tests");
            return true;
        } catch (Exception e) {
            log.error("There was an error starting JMS broker for providerURL : " + providerURL, e);
            return false;
        }
    }

    public boolean stopProcess() {
        try {
            broker.stop();
            return true;
        } catch (Exception e) {
            log.error("Error while shutting down the broker", e);
            return false;
        }
    }

    public Queue connect(String queueName, boolean persistMessage) throws JMSException {
        this.connection = this.connectionFactory.createConnection();
        this.connection.start();
        this.session = this.connection.createSession(false, 1);
        Queue destination = this.session.createQueue(queueName);
        this.producer = this.session.createProducer(destination);
        if (persistMessage) {
            this.producer.setDeliveryMode(2);
        } else {
            this.producer.setDeliveryMode(1);
        }
        return destination;
    }

    public void disconnect() {
        if (this.producer != null) {
            try {
                this.producer.close();
            } catch (JMSException e) {
                log.error("Error while sending message", e);
                Assert.fail();
            }
        }

        if (this.session != null) {
            try {
                this.session.close();
            } catch (JMSException e) {
                log.error("Error while sending message", e);
                Assert.fail();
            }
        }

        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (JMSException e) {
                log.error("Error while sending message", e);
                Assert.fail();
            }
        }

    }

    public TextMessage pushMessage(String messageContent) {
        if (this.producer == null) {
            log.error("The producer is null");
            Assert.fail();
            return null;
        } else {
            TextMessage message = null;
            try {
                message = this.session.createTextMessage(messageContent);
                this.producer.send(message);
            } catch (JMSException e) {
                log.error("Error while sending message", e);
                Assert.fail();
            }
            return message;
        }
    }

    public BytesMessage createBytesMessage(byte[] payload) {
        BytesMessage bm = null;
        try {
            bm = this.session.createBytesMessage();
            bm.writeBytes(payload);
        } catch (JMSException e) {
            log.error("Error while sending message", e);
            Assert.fail();
        }
        return bm;
    }

    public Message receiveMessage(Destination destination) throws JMSException, InterruptedException {
        this.connection = this.connectionFactory.createConnection();
        this.connection.start();
        this.session = this.connection.createSession(false, 1);
        MessageConsumer consumer = this.session.createConsumer(destination);
        Message receivedMsg = consumer.receive(1);
        int count = 0;
        while (receivedMsg == null) {
            count++;
            if (count == 10) {
                // return null to avoid hanging
                return null;
            }
            Thread.sleep(10);
            receivedMsg = consumer.receive(1);
        }
        return receivedMsg;
    }

    public MapMessage createMapMessage() {
        MapMessage mapMessage = null;
        try {
            mapMessage = this.session.createMapMessage();
        } catch (JMSException e) {
            log.error("Error while creating message", e);
            Assert.fail();
        }
        return mapMessage;
    }

    public StreamMessage createStreamMessage() {
        StreamMessage streamMessage = null;
        try {
            streamMessage = this.session.createStreamMessage();
        } catch (JMSException e) {
            log.error("Error while creating message", e);
            Assert.fail();
        }
        return streamMessage;
    }

    public ObjectMessage createObjectMessage() {
        ObjectMessage objMessage = null;
        try {
            objMessage = this.session.createObjectMessage();
        } catch (JMSException e) {
            log.error("Error while creating message", e);
            Assert.fail();
        }
        return objMessage;
    }

    public BrokerService getBrokerService() {
        return broker;
    }

}
