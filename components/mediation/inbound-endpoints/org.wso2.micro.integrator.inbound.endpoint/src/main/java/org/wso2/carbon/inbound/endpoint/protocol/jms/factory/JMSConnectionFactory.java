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
package org.wso2.carbon.inbound.endpoint.protocol.jms.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSUtils;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * use of factory server down and up jms spec transport.jms.MessageSelector
 * isDurable
 */

public class JMSConnectionFactory implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory {
    private static final Log logger = LogFactory.getLog(JMSConnectionFactory.class.getName());

    protected Context ctx;
    protected ConnectionFactory connectionFactory;
    protected String connectionFactoryString;

    protected JMSConstants.JMSDestinationType destinationType;

    private Destination destination;
    protected String destinationName;

    protected boolean transactedSession = false;
    protected int sessionAckMode = 0;

    protected String jmsSpec;
    protected boolean isDurable;
    protected boolean noPubSubLocal;

    protected String clientId;
    protected String subscriptionName;
    protected String messageSelector;
    protected boolean isSharedSubscription;

    public JMSConnectionFactory(Properties properties) {
        try {
            ctx = new InitialContext(properties);
        } catch (NamingException e) {
            logger.error("NamingException while obtaining initial context. " + e.getMessage(), e);
        }

        String connectionFactoryType = properties.getProperty(JMSConstants.CONNECTION_FACTORY_TYPE);
        if ("topic".equals(connectionFactoryType)) {
            this.destinationType = JMSConstants.JMSDestinationType.TOPIC;
        } else {
            this.destinationType = JMSConstants.JMSDestinationType.QUEUE;
        }

        if (properties.getProperty(JMSConstants.PARAM_JMS_SPEC_VER) == null || JMSConstants.JMS_SPEC_VERSION_1_1
                .equals(properties.getProperty(JMSConstants.PARAM_JMS_SPEC_VER))) {
            jmsSpec = JMSConstants.JMS_SPEC_VERSION_1_1;
        } else if (JMSConstants.JMS_SPEC_VERSION_2_0.equals(properties.getProperty(JMSConstants.PARAM_JMS_SPEC_VER))) {
            jmsSpec = JMSConstants.JMS_SPEC_VERSION_2_0;
        } else {
            jmsSpec = JMSConstants.JMS_SPEC_VERSION_1_0;
        }

        if ("true".equalsIgnoreCase(properties.getProperty(JMSConstants.PARAM_IS_SHARED_SUBSCRIPTION))) {
            isSharedSubscription = true;
        } else {
            isSharedSubscription = false;
        }

        noPubSubLocal = Boolean.valueOf(properties.getProperty(JMSConstants.PARAM_PUBSUB_NO_LOCAL));

        clientId = properties.getProperty(JMSConstants.PARAM_DURABLE_SUB_CLIENT_ID);
        subscriptionName = properties.getProperty(JMSConstants.PARAM_DURABLE_SUB_NAME);

        if (isSharedSubscription) {
            if (subscriptionName == null) {
                logger.info("Subscription name is not given. Therefor declaring a non-shared subscription");
                isSharedSubscription = false;
            }
        }

        String subDurable = properties.getProperty(JMSConstants.PARAM_SUB_DURABLE);
        if (subDurable != null) {
            isDurable = Boolean.parseBoolean(subDurable);
        }
        String msgSelector = properties.getProperty(JMSConstants.PARAM_MSG_SELECTOR);
        if (msgSelector != null) {
            messageSelector = msgSelector;
        }
        this.connectionFactoryString = properties.getProperty(JMSConstants.CONNECTION_FACTORY_JNDI_NAME);
        if (connectionFactoryString == null || "".equals(connectionFactoryString)) {
            connectionFactoryString = "QueueConnectionFactory";
        }

        this.destinationName = properties.getProperty(JMSConstants.DESTINATION_NAME);
        if (destinationName == null || "".equals(destinationName)) {
            destinationName = "QUEUE_" + System.currentTimeMillis();
        }

        String strTransactedSession = properties.getProperty(JMSConstants.SESSION_TRANSACTED);
        if (strTransactedSession == null || "".equals(strTransactedSession) || !strTransactedSession.equals("true")) {
            transactedSession = false;
        } else if ("true".equals(strTransactedSession)) {
            transactedSession = true;
            logger.warn(
                    "Usage of transport.jms.SessionTransacted property is deprecated. Please use SESSION_TRANSACTED "
                            + "acknowledge mode to create a transacted session");
        }

        String strSessionAck = properties.getProperty(JMSConstants.SESSION_ACK);
        if (null == strSessionAck) {
            sessionAckMode = 1;
        } else if (strSessionAck.equals("AUTO_ACKNOWLEDGE")) {
            sessionAckMode = Session.AUTO_ACKNOWLEDGE;
        } else if (strSessionAck.equals("CLIENT_ACKNOWLEDGE")) {
            sessionAckMode = Session.CLIENT_ACKNOWLEDGE;
        } else if (strSessionAck.equals("DUPS_OK_ACKNOWLEDGE")) {
            sessionAckMode = Session.DUPS_OK_ACKNOWLEDGE;
        } else if (strSessionAck.equals("SESSION_TRANSACTED")) {
            sessionAckMode = Session.SESSION_TRANSACTED;
            transactedSession = true;
        } else {
            sessionAckMode = 1;
        }

        createConnectionFactory();
    }

    public ConnectionFactory getConnectionFactory() {
        if (this.connectionFactory != null) {
            return this.connectionFactory;
        }

        return createConnectionFactory();
    }

    private ConnectionFactory createConnectionFactory() {
        if (this.connectionFactory != null) {
            return this.connectionFactory;
        }

        if (ctx == null) {
            return null;
        }

        try {
            if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                this.connectionFactory = (QueueConnectionFactory) ctx.lookup(this.connectionFactoryString);
            } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                this.connectionFactory = (TopicConnectionFactory) ctx.lookup(this.connectionFactoryString);
            }
        } catch (NamingException e) {
            logger.error(
                    "Naming exception while obtaining connection factory for '" + this.connectionFactoryString + "'",
                    e);
        }

        return this.connectionFactory;
    }

    public Connection getConnection() {
        return createConnection();
    }

    public Connection createConnection() {
        if (connectionFactory == null) {
            logger.error("Connection cannot be establish to the broker. Please check the broker libs provided.");
            return null;
        }
        Connection connection = null;
        try {
            if ("1.1".equals(jmsSpec)) {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    connection = ((QueueConnectionFactory) (this.connectionFactory)).createQueueConnection();
                } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                    connection = ((TopicConnectionFactory) (this.connectionFactory)).createTopicConnection();
                }
                if (isDurable) {
                    connection.setClientID(clientId);
                }
                return connection;
            } else {
                QueueConnectionFactory qConFac = null;
                TopicConnectionFactory tConFac = null;
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    qConFac = (QueueConnectionFactory) this.connectionFactory;
                } else {
                    tConFac = (TopicConnectionFactory) this.connectionFactory;
                }
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection();
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection();
                }
                if (isDurable && !isSharedSubscription) {
                    connection.setClientID(clientId);
                }
                return connection;
            }
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while creating connection through factory '" + this.connectionFactoryString + "' "
                            + e.getMessage(), e);
            // Need to close the connection in the case if durable subscriptions
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                }
            }
        }

        return null;
    }

    public Connection createConnection(String userName, String password) {
        if (connectionFactory == null) {
            logger.error("Connection cannot be establish to the broker. Please check the broker libs provided.");
            return null;
        }
        Connection connection = null;
        try {
            if (JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec)) {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    connection = ((QueueConnectionFactory) (this.connectionFactory))
                            .createQueueConnection(userName, password);
                } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                    connection = ((TopicConnectionFactory) (this.connectionFactory))
                            .createTopicConnection(userName, password);
                }
                if (isDurable) {
                    connection.setClientID(clientId);
                }
                return connection;
            } else {
                QueueConnectionFactory qConFac = null;
                TopicConnectionFactory tConFac = null;
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    qConFac = (QueueConnectionFactory) this.connectionFactory;
                } else {
                    tConFac = (TopicConnectionFactory) this.connectionFactory;
                }
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection(userName, password);
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection(userName, password);
                }
                if (isDurable && !isSharedSubscription) {
                    connection.setClientID(clientId);
                }
                return connection;
            }
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while creating connection through factory '" + this.connectionFactoryString + "' "
                            + e.getMessage(), e);
            // Need to close the connection in the case if durable subscriptions
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                }
            }
        }

        return null;
    }

    public QueueConnection createQueueConnection() throws JMSException {
        try {
            return ((QueueConnectionFactory) (this.connectionFactory)).createQueueConnection();
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while creating queue connection through factory '" + this.connectionFactoryString
                            + "' " + e.getMessage(), e);
        }
        return null;
    }

    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        try {
            return ((QueueConnectionFactory) (this.connectionFactory)).createQueueConnection(userName, password);
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while creating queue connection through factory '" + this.connectionFactoryString
                            + "' " + e.getMessage(), e);
        }

        return null;
    }

    public TopicConnection createTopicConnection() throws JMSException {
        try {
            return ((TopicConnectionFactory) (this.connectionFactory)).createTopicConnection();
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while creating topic connection through factory '" + this.connectionFactoryString
                            + "' " + e.getMessage(), e);
        }

        return null;
    }

    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        try {
            return ((TopicConnectionFactory) (this.connectionFactory)).createTopicConnection(userName, password);
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while creating topic connection through factory '" + this.connectionFactoryString
                            + "' " + e.getMessage(), e);
        }

        return null;
    }

    public Destination getDestination(Session session) {
        if (this.destination != null) {
            return this.destination;
        }

        return createDestination(session);
    }

    public MessageConsumer createMessageConsumer(Session session, Destination destination) {
        try {
            if (JMSConstants.JMS_SPEC_VERSION_2_0.equals(jmsSpec) && isSharedSubscription) {
                if (isDurable) {
                    return session.createSharedDurableConsumer((Topic) destination, subscriptionName, messageSelector);
                } else {
                    return session.createSharedConsumer((Topic) destination, subscriptionName, messageSelector);
                }
            } else if ((JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec)) || (
                    JMSConstants.JMS_SPEC_VERSION_2_0.equals(jmsSpec) && !isSharedSubscription)) {
                if (isDurable) {
                    return session.createDurableSubscriber((Topic) destination, subscriptionName, messageSelector,
                                                           noPubSubLocal);
                } else {
                    return session.createConsumer(destination, messageSelector);
                }
            } else {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    return ((QueueSession) session).createReceiver((Queue) destination, messageSelector);
                } else {
                    if (isDurable) {
                        return ((TopicSession) session)
                                .createDurableSubscriber((Topic) destination, subscriptionName, messageSelector,
                                                         noPubSubLocal);
                    } else {
                        return ((TopicSession) session).createSubscriber((Topic) destination, messageSelector, false);
                    }
                }
            }
        } catch (JMSException e) {
            logger.error("JMS Exception while creating consumer. " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * This is a JMS spec independent method to create a MessageProducer. Please be cautious when
     * making any changes
     *
     * @param session     JMS session
     * @param destination the Destination
     * @param isQueue     is the Destination a queue?
     * @param jmsSpec11   should we use JMS 1.1 API ?
     * @return a MessageProducer to send messages to the given Destination
     * @throws JMSException on errors, to be handled and logged by the caller
     */
    public MessageProducer createProducer(Session session, Destination destination, Boolean isQueue)
            throws JMSException {
        if ("2.0".equals(jmsSpec) || "1.1".equals(jmsSpec) || isQueue == null) {
            return session.createProducer(destination);
        } else {
            if (isQueue) {
                return ((QueueSession) session).createSender((Queue) destination);
            } else {
                return ((TopicSession) session).createPublisher((Topic) destination);
            }
        }
    }

    private Destination createDestination(Session session) {
        this.destination = createDestination(session, this.destinationName);
        return this.destination;
    }

    public Destination createDestination(Session session, String destinationName) {
        Destination destination = null;
        try {
            if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                destination = JMSUtils.lookupDestination(ctx, destinationName, JMSConstants.DESTINATION_TYPE_QUEUE);
            } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                destination = JMSUtils.lookupDestination(ctx, destinationName, JMSConstants.DESTINATION_TYPE_TOPIC);
            }
        } catch (NameNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find destination '" + destinationName + "' on connection factory for '"
                                     + this.connectionFactoryString + "'. " + e.getMessage());
                logger.debug("Creating destination '" + destinationName + "' on connection factory for '"
                                     + this.connectionFactoryString + ".");
            }
            try {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    destination = (Queue) session.createQueue(destinationName);
                } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                    destination = (Topic) session.createTopic(destinationName);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Created '" + destinationName + "' on connection factory for '"
                                         + this.connectionFactoryString + "'.");
                }
            } catch (JMSException e1) {
                logger.error("Could not find nor create '" + destinationName + "' on connection factory for '"
                                     + this.connectionFactoryString + "'. " + e1.getMessage(), e1);
            }

        } catch (NamingException e) {
            logger.error(
                    "Naming exception while obtaining connection factory for '" + this.connectionFactoryString + "' "
                            + e.getMessage(), e);
        }

        return destination;
    }

    public Session getSession(Connection connection) {
        return createSession(connection);
    }

    protected Session createSession(Connection connection) {
        try {
            if (JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec) || JMSConstants.JMS_SPEC_VERSION_2_0
                    .equals(jmsSpec)) {
                return connection.createSession(transactedSession, sessionAckMode);
            } else {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    return (QueueSession) ((QueueConnection) (connection))
                            .createQueueSession(transactedSession, sessionAckMode);
                } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                    return (TopicSession) ((TopicConnection) (connection))
                            .createTopicSession(transactedSession, sessionAckMode);
                }
            }
        } catch (JMSException e) {
            logger.error("JMS Exception while obtaining session for factory '" + this.connectionFactoryString + "' " + e
                    .getMessage(), e);
        }

        return null;
    }

    public void start(Connection connection) {
        try {
            connection.start();
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while starting connection for factory '" + this.connectionFactoryString + "' " + e
                            .getMessage(), e);
        }
    }

    public void stop(Connection connection) {
        try {
            connection.stop();
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while stopping connection for factory '" + this.connectionFactoryString + "' " + e
                            .getMessage(), e);
        }
    }

    public boolean closeConnection(Connection connection) {
        try {
            connection.close();
            return true;
        } catch (JMSException e) {
            logger.error("JMS Exception while closing the connection.");
        }

        return false;
    }

    public Context getContext() {
        return this.ctx;
    }

    public JMSConstants.JMSDestinationType getDestinationType() {
        return this.destinationType;
    }

    public String getConnectionFactoryString() {
        return connectionFactoryString;
    }

    public boolean isTransactedSession() {
        return transactedSession;
    }

    public int getSessionAckMode() {
        return sessionAckMode;
    }

    public javax.jms.JMSContext createContext() {
        return connectionFactory.createContext();
    }

    public javax.jms.JMSContext createContext(int sessionMode) {
        return connectionFactory.createContext(sessionMode);
    }

    public javax.jms.JMSContext createContext(String userName, String password) {
        return connectionFactory.createContext(userName, password);
    }

    public javax.jms.JMSContext createContext(String userName, String password, int sessionMode) {
        return connectionFactory.createContext(userName, password, sessionMode);
    }

}
