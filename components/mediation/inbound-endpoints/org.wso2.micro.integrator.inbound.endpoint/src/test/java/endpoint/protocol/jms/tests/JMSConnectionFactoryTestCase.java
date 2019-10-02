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
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.factory.CachedJMSConnectionFactory;
import org.wso2.carbon.inbound.endpoint.protocol.jms.factory.JMSConnectionFactory;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.TopicConnection;

public class JMSConnectionFactoryTestCase extends TestCase {

    private static final String PROVIDER_URL = "tcp://127.0.0.1:61616";

    /**
     * Test cached connection when the transport.jms.CacheLevel is 1
     *
     * @throws Exception
     */
    @Test
    public void testCacheLevelOne() throws Exception {
        String queueName = "testCaching1";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "1");
        try {
            brokerController.startProcess();
            Queue queue = brokerController.connect(queueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection1 = cachedJMSConnectionFactory.getConnection(null, null);
            String clientID1 = connection1.getClientID();
            Session session1 = cachedJMSConnectionFactory.getSession(connection1);
            MessageConsumer consumer1 = cachedJMSConnectionFactory.getMessageConsumer(session1, queue);
            Connection connection2 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session2 = cachedJMSConnectionFactory.getSession(connection2);
            MessageConsumer consumer2 = cachedJMSConnectionFactory.getMessageConsumer(session2, queue);
            Assert.assertEquals("Connection should be cached", clientID1, connection2.getClientID());
            Assert.assertNotSame("Session should not be cached", session1, session2);
            Assert.assertNotSame("Message Consumer should not be cached", ((ActiveMQMessageConsumer) consumer1).
                    getConsumerId().toString(), ((ActiveMQMessageConsumer) consumer2).getConsumerId().toString());
            cachedJMSConnectionFactory.closeConnection();
            Connection connection3 = cachedJMSConnectionFactory.getConnection(null, null);
            Assert.assertNotSame("Connection should be closed", clientID1, connection3.getClientID());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test cached session when the transport.jms.CacheLevel is 2
     *
     * @throws Exception
     */
    @Test
    public void testCacheLevelTwo() throws Exception {
        String queueName = "testCaching2";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "2");
        try {
            brokerController.startProcess();
            Queue queue = brokerController.connect(queueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection1 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session1 = cachedJMSConnectionFactory.getSession(connection1);
            MessageConsumer consumer1 = cachedJMSConnectionFactory.getMessageConsumer(session1, queue);
            Connection connection2 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session2 = cachedJMSConnectionFactory.getSession(connection2);
            MessageConsumer consumer2 = cachedJMSConnectionFactory.getMessageConsumer(session2, queue);
            Assert.assertEquals("Connection should be cached", connection1.getClientID(), connection2.getClientID());
            Assert.assertEquals("Session should be cached", session1, session2);
            Assert.assertNotSame("Message Consumer should not be cached", ((ActiveMQMessageConsumer) consumer1).
                    getConsumerId().toString(), ((ActiveMQMessageConsumer) consumer2).getConsumerId().toString());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test cached message consumer when the transport.jms.CacheLevel is 3 when the JMS Spec Version is 2.0
     *
     * @throws Exception
     */
    @Test
    public void testCacheLevelThreeV2() throws Exception {
        String queueName = "testCaching3V2";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        jmsProperties.put(JMSConstants.PARAM_JMS_SPEC_VER, JMSConstants.JMS_SPEC_VERSION_2_0);
        try {
            brokerController.startProcess();
            Queue queue = brokerController.connect(queueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection1 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session1 = cachedJMSConnectionFactory.getSession(connection1);
            MessageConsumer consumer1 = cachedJMSConnectionFactory.getMessageConsumer(session1, queue);
            Connection connection2 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session2 = cachedJMSConnectionFactory.getSession(connection2);
            MessageConsumer consumer2 = cachedJMSConnectionFactory.getMessageConsumer(session2, queue);
            Assert.assertEquals("Connection should be cached", connection1.getClientID(), connection2.getClientID());
            Assert.assertEquals("Session should be cached", session1, session2);
            Assert.assertEquals("Message Consumer should be cached", ((ActiveMQMessageConsumer) consumer1).
                    getConsumerId().toString(), ((ActiveMQMessageConsumer) consumer2).getConsumerId().toString());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test cached message consumer when the transport.jms.CacheLevel is 3
     *
     * @throws Exception
     */
    @Test
    public void testCacheLevelThree() throws Exception {
        String queueName = "testCaching3";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        try {
            brokerController.startProcess();
            Queue queue = brokerController.connect(queueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection1 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session1 = cachedJMSConnectionFactory.getSession(connection1);
            MessageConsumer consumer1 = cachedJMSConnectionFactory.getMessageConsumer(session1, queue);
            Connection connection2 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session2 = cachedJMSConnectionFactory.getSession(connection2);
            MessageConsumer consumer2 = cachedJMSConnectionFactory.getMessageConsumer(session2, queue);
            Assert.assertEquals("Connection should be cached", connection1.getClientID(), connection2.getClientID());
            Assert.assertEquals("Session should be cached", session1, session2);
            Assert.assertEquals("Message Consumer should be cached", ((ActiveMQMessageConsumer) consumer1).
                    getConsumerId().toString(), ((ActiveMQMessageConsumer) consumer2).getConsumerId().toString());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test close cached connection, session, message consumer when the transport.jms.CacheLevel is 3 not forcefully
     *
     * @throws Exception
     */
    @Test
    public void testCloseCached() throws Exception {
        String queueName = "testCloseCache";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        try {
            brokerController.startProcess();
            Queue queue = brokerController.connect(queueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection1 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session1 = cachedJMSConnectionFactory.getSession(connection1);
            MessageConsumer consumer1 = cachedJMSConnectionFactory.getMessageConsumer(session1, queue);
            cachedJMSConnectionFactory.closeConnection(connection1);
            cachedJMSConnectionFactory.closeSession(session1);
            cachedJMSConnectionFactory.closeConsumer(consumer1);
            Connection connection2 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session2 = cachedJMSConnectionFactory.getSession(connection2);
            MessageConsumer consumer2 = cachedJMSConnectionFactory.getMessageConsumer(session2, queue);
            Assert.assertEquals("Connection should be cached", connection1.getClientID(), connection2.getClientID());
            Assert.assertEquals("Session should be cached", session1, session2);
            Assert.assertEquals("Message Consumer should be cached", ((ActiveMQMessageConsumer) consumer1).
                    getConsumerId().toString(), ((ActiveMQMessageConsumer) consumer2).getConsumerId().toString());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test creating CachedConnectionFactory using cached connection
     *
     * @throws Exception
     */
    @Test
    public void testCachedConnection() throws Exception {
        String queueName = "testCaching3";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        try {
            brokerController.startProcess();
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection1 = cachedJMSConnectionFactory.getConnection(null, null);
            CachedJMSConnectionFactory cachedJMSConnectionFactory2 = new CachedJMSConnectionFactory(jmsProperties,
                                                                                                    connection1);
            Connection connection2 = cachedJMSConnectionFactory2.getConnection(null, null);
            Assert.assertEquals("Connection should be cached", connection1.getClientID(), connection2.getClientID());
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test check destination type
     *
     * @throws Exception
     */
    @Test
    public void testCheckDestinationType() throws Exception {
        String queueName = "testCaching3";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        try {
            brokerController.startProcess();
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            JMSConstants.JMSDestinationType type = cachedJMSConnectionFactory.getDestinationType();
            Assert.assertEquals("The destination type should be matched", JMSConstants.JMSDestinationType.QUEUE, type);
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test get connection null if broker is unreachable
     *
     * @throws Exception
     */
    @Test
    public void testFailToConnectToBroker() throws Exception {
        String queueName = "testConnectionFail";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        try {
            brokerController.startProcess();
            Queue queue = brokerController.connect(queueName, true);
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection1 = cachedJMSConnectionFactory.getConnection(null, null);
            Session session1 = cachedJMSConnectionFactory.getSession(connection1);
            cachedJMSConnectionFactory.getMessageConsumer(session1, queue);
            Assert.assertNotNull("The connection should be created", connection1);
            brokerController.disconnect();
            brokerController.stopProcess();
            Connection connection2 = cachedJMSConnectionFactory.getConnection(null, null);
            Assert.assertNull("The connection should be null", connection2);
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test create connection with credentials
     *
     * @throws Exception
     */
    @Test
    public void testCreateConnectionWithCredentials() throws Exception {
        String queueName = "testWithCred";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        try {
            brokerController.startProcess();
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection = cachedJMSConnectionFactory.getConnection("admin", "admin");
            Assert.assertNotNull("The connection should be created", connection);
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test create connection with credentials when the JMS Spec Version is 2.0
     *
     * @throws Exception
     */
    @Test
    public void testCreateConnectionWithCredentialsV2() throws Exception {
        String queueName = "testWithCredV2";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        jmsProperties.put(JMSConstants.PARAM_JMS_SPEC_VER, JMSConstants.JMS_SPEC_VERSION_2_0);
        jmsProperties.put(JMSConstants.PARAM_CACHE_LEVEL, "3");
        try {
            brokerController.startProcess();
            CachedJMSConnectionFactory cachedJMSConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
            Connection connection = cachedJMSConnectionFactory.getConnection("admin", "admin");
            Assert.assertNotNull("The connection should be created", connection);
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test check JMS properties of the connection factory
     *
     * @throws Exception
     */
    @Test

    public void testCheckJMSProperties() throws Exception {
        String queueName = "testJMSProperties";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        jmsProperties.put(JMSConstants.SESSION_ACK, "CLIENT_ACKNOWLEDGE");
        jmsProperties.put(JMSConstants.SESSION_TRANSACTED, "true");
        JMSConnectionFactory connectionFactory = new JMSConnectionFactory(jmsProperties);
        Assert.assertEquals("The JMS connection factory string mismatch",
                            jmsProperties.getProperty(JMSConstants.CONNECTION_FACTORY_JNDI_NAME),
                            connectionFactory.getConnectionFactoryString());
        Assert.assertEquals("The session acknowledgement mismatch", 2, connectionFactory.getSessionAckMode());
        Assert.assertEquals("The destination type mismatch", JMSConstants.JMSDestinationType.QUEUE,
                            connectionFactory.getDestinationType());
        Assert.assertTrue("The transacted property mismatch", connectionFactory.isTransactedSession());
    }

    /**
     * Test stop/Close connection
     *
     * @throws Exception
     */
    @Test

    public void testStopConnection() throws Exception {
        String queueName = "testStopCon";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            JMSConnectionFactory connectionFactory = new JMSConnectionFactory(jmsProperties);
            Connection connection = connectionFactory.getConnection();
            connectionFactory.start(connection);
            Assert.assertTrue("The connection should be started", ((ActiveMQConnection) connection).isStarted());
            connectionFactory.stop(connection);
            Assert.assertFalse("The connection should be stopped", ((ActiveMQConnection) connection).isStarted());
            boolean isClosed = connectionFactory.closeConnection(connection);
            Assert.assertTrue("The connection should be closed", isClosed);
            //Exception should be thrown and caught thrown when trying to start/stop/close closed connection
            connectionFactory.start(connection);
            connectionFactory.stop(connection);
            ;
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test create queue/topic connection fail scenario - the broker is not started
     *
     * @throws Exception
     */
    @Test
    public void testCreateQueueAndTopicConnectionsFail() throws Exception {
        String queueName = "testCreateConQ";
        Properties jmsPropertiesQ = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSConnectionFactory connectionFactoryQ = new JMSConnectionFactory(jmsPropertiesQ);
        String topicName = "testCreateConT";
        Properties jmsPropertiesT = JMSTestsUtils.getJMSPropertiesForDestination(topicName, PROVIDER_URL, true);
        JMSConnectionFactory connectionFactoryT = new JMSConnectionFactory(jmsPropertiesT);
        Assert.assertNull("The create queue connection should fail", connectionFactoryQ.createQueueConnection());
        Assert.assertNull("The create topic connection should fail", connectionFactoryT.createTopicConnection());
        Assert.assertNull("The create queue connection should fail", connectionFactoryQ.
                createQueueConnection("admin", "admin"));
        Assert.assertNull("The create queue connection should fail", connectionFactoryT.
                createTopicConnection("admin", "admin"));
    }

    /**
     * Test create queue connection
     *
     * @throws Exception
     */
    @Test
    public void testCreateQueueConnections() throws Exception {
        String queueName = "testCreateConQ";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(queueName, PROVIDER_URL, true);
        JMSConnectionFactory connectionFactory = new JMSConnectionFactory(jmsProperties);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            QueueConnection queueConnection = connectionFactory.createQueueConnection();
            Assert.assertTrue("The queue connection is created", ((ActiveMQConnection) queueConnection).
                    getTransport().getRemoteAddress().contains("61616"));
            QueueConnection queueConnectionWithCred = connectionFactory.createQueueConnection("admin", "admin");
            Assert.assertTrue("The queue connection is created", ((ActiveMQConnection) queueConnectionWithCred).
                    getTransport().getRemoteAddress().contains("61616"));
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }

    /**
     * Test create topic connection
     *
     * @throws Exception
     */
    @Test
    public void testCreateTopicConnections() throws Exception {
        String topicName = "testCreateConT";
        Properties jmsProperties = JMSTestsUtils.getJMSPropertiesForDestination(topicName, PROVIDER_URL, true);
        JMSConnectionFactory connectionFactory = new JMSConnectionFactory(jmsProperties);
        JMSBrokerController brokerController = new JMSBrokerController(PROVIDER_URL, jmsProperties);
        try {
            brokerController.startProcess();
            TopicConnection topicConnection = connectionFactory.createTopicConnection();
            Assert.assertTrue("The queue connection is created", ((ActiveMQConnection) topicConnection).
                    getTransport().getRemoteAddress().contains("61616"));
            TopicConnection topicConnectionWithCred = connectionFactory.createTopicConnection("admin", "admin");
            Assert.assertTrue("The queue connection is created", ((ActiveMQConnection) topicConnectionWithCred).
                    getTransport().getRemoteAddress().contains("61616"));
        } finally {
            brokerController.disconnect();
            brokerController.stopProcess();
        }
    }
}
