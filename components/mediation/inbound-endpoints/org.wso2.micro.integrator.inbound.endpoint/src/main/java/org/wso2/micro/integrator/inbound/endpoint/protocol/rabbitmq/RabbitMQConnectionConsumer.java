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

package org.wso2.micro.integrator.inbound.endpoint.protocol.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.axiom.om.OMException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class RabbitMQConnectionConsumer {
    private static final Log log = LogFactory.getLog(RabbitMQConnectionConsumer.class);
    private RabbitMQConnectionFactory rabbitMQConnectionFactory;
    private Properties rabbitMQProperties;

    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_SHUTTING_DOWN = 3;
    private static final int STATE_FAILURE = 4;
    private static final int STATE_FAULTY = 5;

    private volatile int workerState = STATE_STOPPED;
    private String inboundName;
    private Connection connection = null;
    private Channel channel = null;
    private boolean autoAck = false;
    private QueueingConsumer queueingConsumer;
    private String queueName, routeKey, exchangeName;
    private Hashtable<String, String> rabbitMQProps = new Hashtable<>();
    private RabbitMQInjectHandler injectHandler;
    private String consumerTagString;

    private volatile boolean connected = false;
    private volatile boolean idle = false;

    public RabbitMQConnectionConsumer(RabbitMQConnectionFactory rabbitMQConnectionFactory,
                                      Properties rabbitMQProperties, RabbitMQInjectHandler injectHandler) {
        this.rabbitMQConnectionFactory = rabbitMQConnectionFactory;
        this.rabbitMQProperties = rabbitMQProperties;
        this.injectHandler = injectHandler;
        for (final String propertyName : rabbitMQProperties.stringPropertyNames()) {
            this.rabbitMQProps.put(propertyName, rabbitMQProperties.getProperty(propertyName));
        }
    }

    public void execute() {

        try {
            workerState = STATE_STARTED;
            initConsumer();

            while (workerState == STATE_STARTED) {
                try {
                    startConsumer();
                } catch (ShutdownSignalException sse) {
                    if (!sse.isInitiatedByApplication()) {
                        log.error("RabbitMQ Listener of the inbound " + inboundName + " was disconnected", sse);
                        waitForConnection();
                    }
                } catch (OMException e) {
                    log.error("Invalid Message Format while consuming the message", e);
                } catch (IOException e) {
                    log.error("RabbitMQ Listener of the inbound " + inboundName + " was disconnected", e);
                    waitForConnection();
                }
            }
        } catch (IOException e) {
            handleException("Error initializing consumer for inbound " + inboundName, e);
        } finally {
            closeConnection();
            workerState = STATE_STOPPED;
        }
    }

    private void waitForConnection() throws IOException {
        int retryInterval = rabbitMQConnectionFactory.getRetryInterval();
        int retryCountMax = rabbitMQConnectionFactory.getRetryCount();
        int retryCount = 0;
        while ((workerState == STATE_STARTED) && !connection.isOpen() && ((retryCountMax == -1) || (retryCount
                < retryCountMax))) {
            retryCount++;
            log.info(
                    "Attempting to reconnect to RabbitMQ Broker for the inbound " + inboundName + " in " + retryInterval
                            + " ms");
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                log.error("Error while trying to reconnect to RabbitMQ Broker for the inbound " + inboundName, e);
            }
        }
        if (connection.isOpen()) {
            log.info("Successfully reconnected to RabbitMQ Broker for the inbound " + inboundName);
            initConsumer();
        } else {
            log.error("Could not reconnect to the RabbitMQ Broker for the inbound " + inboundName
                              + ". Connection is closed.");
            workerState = STATE_FAULTY;
        }
    }

    /**
     * Used to start message consuming messages. This method is called in startup and when
     * connection is re-connected. This method will request for the connection and create
     * channel, queues, exchanges and bind queues to exchanges before consuming messages
     *
     * @throws ShutdownSignalException
     * @throws IOException
     */
    private void startConsumer() throws ShutdownSignalException, IOException {
        connection = getConnection();
        if (channel == null || !channel.isOpen()) {
            channel = connection.createChannel();
            log.debug("Channel is not open. Creating a new channel for inbound " + inboundName);
        }

        //unable to connect to the queue
        if (queueingConsumer == null) {
            workerState = STATE_STOPPED;
            return;
        }

        while (isActive()) {
            try {
                if (!channel.isOpen()) {
                    channel = queueingConsumer.getChannel();
                }
                channel.txSelect();
            } catch (IOException e) {
                log.error("Error while starting transaction", e);
                continue;
            }

            boolean successful = false;
            boolean mediationError = false;

            RabbitMQMessage message = null;
            try {
                message = getConsumerDelivery(queueingConsumer);
            } catch (InterruptedException e) {
                log.error("Error while consuming message", e);
                continue;
            }

            if (message != null) {
                idle = false;
                try {
                    successful = injectHandler.invoke(message, inboundName);
                } catch (Exception e) {         //we need to handle any exception upon injecting to mediation
                    successful = false;
                    mediationError = true;
                    log.error("Error while mediating message", e);
                } finally {
                    if (successful) {
                        try {
                            if (!autoAck) {
                                channel.basicAck(message.getDeliveryTag(), false);
                            }
                            channel.txCommit();
                        } catch (IOException e) {
                            log.error("Error while committing transaction", e);
                        }
                    } else {
                        try {
                            channel.txRollback();
                            // According to the spec, rollback doesn't automatically redeliver unacked messages.
                            // We need to call recover explicitly.
                            channel.basicRecover();
                        } catch (IOException e) {
                            log.error("Error while trying to roll back transaction", e);
                        }
                    }

                    /*
                     * Upon a mediation error, re-try with a fixed delay. Polling messages cannot be given up
                     * as there is no way for the user to start the polling task back
                     */
                    if (mediationError) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } else {
                idle = true;
            }
        }

    }

    /**
     * Create a queue consumer using the properties from inbound listener configuration
     *
     * @throws IOException on error
     */
    private void initConsumer() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing consumer for inbound " + inboundName);
        }
        connection = getConnection();
        channel = connection.createChannel();
        queueName = rabbitMQProperties.getProperty(RabbitMQConstants.QUEUE_NAME);
        routeKey = rabbitMQProperties.getProperty(RabbitMQConstants.QUEUE_ROUTING_KEY);
        exchangeName = rabbitMQProperties.getProperty(RabbitMQConstants.EXCHANGE_NAME);

        String autoAckStringValue = rabbitMQProperties.getProperty(RabbitMQConstants.QUEUE_AUTO_ACK);
        if (autoAckStringValue != null) {
            try {
                autoAck = Boolean.parseBoolean(autoAckStringValue);
            } catch (Exception e) {
                log.debug("Format error in rabbitmq.queue.auto.ack parameter");
            }
        }
        //If no queue name is specified then inbound factory name will be used as queue name
        if (StringUtils.isEmpty(queueName)) {
            queueName = inboundName;
            log.info("No queue name is specified for " + inboundName + ". "
                             + "inbound factory name will be used as queue name");
        }

        if (routeKey == null) {
            log.info("No routing key specified. Using queue name as the " + "routing key.");
            routeKey = queueName;
        }

        if (!StringUtils.isEmpty(queueName)) {
            //declaring queue
            RabbitMQUtils.declareQueue(connection, queueName, rabbitMQProps);
        }

        if (!StringUtils.isEmpty(exchangeName)) {
            //declaring exchange
            RabbitMQUtils.declareExchange(connection, exchangeName, rabbitMQProps);

            if (!channel.isOpen()) {
                channel = connection.createChannel();
                if (log.isDebugEnabled()) {
                    log.debug("Channel is not open. Creating a new channel for inbound " + inboundName);
                }
            }
            channel.queueBind(queueName, exchangeName, routeKey);
            log.debug("Bind queue '" + queueName + "' to exchange '" + exchangeName + "' with route key '" + routeKey
                              + "'");
        }

        if (!channel.isOpen()) {
            channel = connection.createChannel();
            log.debug("Channel is not open. Creating a new channel for inbound " + inboundName);
        }

        // set QoS parameter for the channel before it is assigned to the consumer
        String qos = rabbitMQProperties.getProperty(RabbitMQConstants.CONSUMER_QOS);
        if (qos != null && !qos.isEmpty()) {
            try {
                channel.basicQos(Integer.parseInt(qos));
            } catch (NumberFormatException e) {
                log.warn("Unable to parse given QoS value, " + qos + " as an integer. Therefore using channel "
                                 + "without QoS.");
            }
        }

        queueingConsumer = new QueueingConsumer(channel);

        consumerTagString = rabbitMQProperties.getProperty(RabbitMQConstants.CONSUMER_TAG);
        if (consumerTagString != null) {
            channel.basicConsume(queueName, autoAck, consumerTagString, queueingConsumer);
            log.debug("Start consuming queue '" + queueName + "' with consumer tag '" + consumerTagString
                              + "' for inbound " + inboundName);
        } else {
            consumerTagString = channel.basicConsume(queueName, autoAck, queueingConsumer);
            log.debug("Start consuming queue '" + queueName + "' with consumer tag '" + consumerTagString
                              + "' for inbound " + inboundName);
        }
    }

    /**
     * Returns the delivery from the consumer
     *
     * @param consumer the consumer to get the delivery
     * @return RabbitMQMessage consumed by the consumer
     * @throws InterruptedException on error
     */
    private RabbitMQMessage getConsumerDelivery(QueueingConsumer consumer)
            throws InterruptedException, ShutdownSignalException {
        RabbitMQMessage message = new RabbitMQMessage();
        QueueingConsumer.Delivery delivery = null;
        try {
            log.debug("Waiting for next delivery from queue for inbound " + inboundName);
            delivery = consumer.nextDelivery();
        } catch (ShutdownSignalException e) {
            return null;
        } catch (InterruptedException e) {
            return null;
        } catch (ConsumerCancelledException e) {
            return null;
        }

        if (delivery != null) {
            AMQP.BasicProperties properties = delivery.getProperties();
            Map<String, Object> headers = properties.getHeaders();
            message.setBody(delivery.getBody());
            message.setDeliveryTag(delivery.getEnvelope().getDeliveryTag());
            message.setReplyTo(properties.getReplyTo());
            message.setMessageId(properties.getMessageId());

            // Content type is as set in delivered message. If not, from inbound parameters.
            String contentType = properties.getContentType();
            if (contentType == null) {
                contentType = rabbitMQProperties.getProperty(RabbitMQConstants.CONTENT_TYPE);
            }
            message.setContentType(contentType);

            message.setContentEncoding(properties.getContentEncoding());
            message.setCorrelationId(properties.getCorrelationId());
            if (headers != null) {
                message.setHeaders(headers);
                if (headers.get(RabbitMQConstants.SOAP_ACTION) != null) {
                    message.setSoapAction(headers.get(RabbitMQConstants.SOAP_ACTION).toString());
                }
            }
        } else {
            log.debug("Queue delivery item is null for inbound " + inboundName);
            return null;
        }
        return message;
    }

    private void closeConnection() {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
                log.info("RabbitMQ connection closed for inbound " + inboundName);
            } catch (IOException e) {
                log.error("Error while closing RabbitMQ connection for inbound " + inboundName, e);
            } finally {
                connection = null;
            }
        }
    }

    private Connection createConnection() throws IOException {
        Connection connection = null;
        try {
            connection = rabbitMQConnectionFactory.createConnection();
            log.info("RabbitMQ connection created for inbound " + inboundName);
        } catch (Exception e) {
            handleException("Error while creating RabbitMQ connection for inbound " + inboundName, e);
        }
        return connection;
    }

    private Connection getConnection() throws IOException {
        if (connection == null) {
            connection = createConnection();
            setConnected(true);
        }
        return connection;
    }

    private boolean isActive() {
        return workerState == STATE_STARTED;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getInboundName() {
        return inboundName;
    }

    public void setInboundName(String inboundName) {
        this.inboundName = inboundName;
    }

    protected void requestShutdown() {
        workerState = STATE_SHUTTING_DOWN;
        closeConnection();
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new RabbitMQException(msg, e);
    }

}