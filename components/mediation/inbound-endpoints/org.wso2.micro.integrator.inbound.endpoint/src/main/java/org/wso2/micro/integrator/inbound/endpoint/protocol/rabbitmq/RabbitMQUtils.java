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

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQUtils {

    private static final Log log = LogFactory.getLog(RabbitMQUtils.class);

    public static Connection createConnection(ConnectionFactory factory, Address[] addresses) throws IOException {
        Connection connection = null;
        try {
            connection = factory.newConnection(addresses);
        } catch (TimeoutException e) {
            log.error("Error while creating new connection", e);
        }
        return connection;
    }

    public static String getProperty(MessageContext mc, String key) {
        return (String) mc.getProperty(key);
    }

    public static Map getTransportHeaders(RabbitMQMessage message) {
        Map<String, String> map = new HashMap<String, String>();

        // correlation ID
        if (message.getCorrelationId() != null) {
            map.put(RabbitMQConstants.CORRELATION_ID, message.getCorrelationId());
        }

        // if a AMQP message ID is found
        if (message.getMessageId() != null) {
            map.put(RabbitMQConstants.MESSAGE_ID, message.getMessageId());
        }

        // replyto destination name
        if (message.getReplyTo() != null) {
            String dest = message.getReplyTo();
            map.put(RabbitMQConstants.RABBITMQ_REPLY_TO, dest);
        }

        // expiration time
        if (message.getExpiration() != null) {
            String expiration = message.getExpiration();
            map.put(RabbitMQConstants.EXPIRATION, expiration);
        }

        // any other transport properties / headers
        Map<String, Object> headers = message.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (String headerName : headers.keySet()) {
                String value = headers.get(headerName).toString();
                map.put(headerName, value);
            }
        }

        return map;
    }

    public static boolean isDurableQueue(Hashtable<String, String> properties) {
        String durable = properties.get(RabbitMQConstants.QUEUE_DURABLE);
        return durable != null && Boolean.parseBoolean(durable);
    }

    public static boolean isExclusiveQueue(Hashtable<String, String> properties) {
        String exclusive = properties.get(RabbitMQConstants.QUEUE_EXCLUSIVE);
        return exclusive != null && Boolean.parseBoolean(exclusive);
    }

    public static boolean isAutoDeleteQueue(Hashtable<String, String> properties) {
        String autoDelete = properties.get(RabbitMQConstants.QUEUE_AUTO_DELETE);
        return autoDelete != null && Boolean.parseBoolean(autoDelete);
    }

    public static boolean isQueueAvailable(Connection connection, String queueName) throws IOException {
        Channel channel = connection.createChannel();
        try {
            // check availability of the named queue
            // if an error is encountered, including if the queue does not exist and if the
            // queue is exclusively owned by another connection
            channel.queueDeclarePassive(queueName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param connection
     * @param queueName
     * @param isDurable
     * @param isExclusive
     * @param isAutoDelete
     * @throws IOException
     */
    public static void declareQueue(Connection connection, String queueName, boolean isDurable, boolean isExclusive,
                                    boolean isAutoDelete) throws IOException {

        boolean queueAvailable = isQueueAvailable(connection, queueName);
        Channel channel = connection.createChannel();

        if (!queueAvailable) {
            if (log.isDebugEnabled()) {
                log.debug("Queue :" + queueName + " not found or already declared exclusive. Declaring the queue.");
            }
            // Declare the named queue if it does not exists.
            if (!channel.isOpen()) {
                channel = connection.createChannel();
                log.debug("Channel is not open. Creating a new channel.");
            }
            try {
                channel.queueDeclare(queueName, isDurable, isExclusive, isAutoDelete, null);
            } catch (IOException e) {
                handleException("Error while creating queue: " + queueName, e);
            }
        }
    }

    public static void declareQueue(Connection connection, String queueName, Hashtable<String, String> properties)
            throws IOException {
        Boolean queueAvailable = isQueueAvailable(connection, queueName);
        Channel channel = connection.createChannel();

        if (!queueAvailable) {
            // Declare the named queue if it does not exists.
            if (!channel.isOpen()) {
                channel = connection.createChannel();
                log.debug("Channel is not open. Creating a new channel.");
            }
            try {
                channel.queueDeclare(queueName, isDurableQueue(properties), isExclusiveQueue(properties),
                                     isAutoDeleteQueue(properties), null);

            } catch (IOException e) {
                handleException("Error while creating queue: " + queueName, e);
            }
        }
    }

    public static void declareExchange(Connection connection, String exchangeName, Hashtable<String, String> properties)
            throws IOException {
        Boolean exchangeAvailable = false;
        Channel channel = connection.createChannel();
        String exchangeType = properties
                .getOrDefault(RabbitMQConstants.EXCHANGE_TYPE, RabbitMQConstants.EXCHANGE_TYPE_DEFAULT);
        String durable = properties
                .getOrDefault(RabbitMQConstants.EXCHANGE_DURABLE, RabbitMQConstants.EXCHANGE_DURABLE_DEFAULT);
        String autoDelete = properties
                .getOrDefault(RabbitMQConstants.EXCHANGE_AUTODELETE, RabbitMQConstants.EXCHANGE_AUTODELETE_DEFAULT);

        if (exchangeType.isEmpty()) {
            exchangeType = RabbitMQConstants.EXCHANGE_TYPE_DEFAULT;
        }
        if (durable.isEmpty()) {
            durable = RabbitMQConstants.EXCHANGE_DURABLE_DEFAULT;
        }
        if (autoDelete.isEmpty()) {
            autoDelete = RabbitMQConstants.EXCHANGE_AUTODELETE_DEFAULT;
        }

        try {
            // check availability of the named exchange.
            // The server will raise an IOException
            // if the named exchange already exists.
            channel.exchangeDeclarePassive(exchangeName);
            exchangeAvailable = true;
        } catch (IOException e) {
            log.info("Exchange :" + exchangeName + " not found.Declaring exchange.");
        }

        if (!exchangeAvailable) {
            // Declare the named exchange if it does not exists.
            if (!channel.isOpen()) {
                channel = connection.createChannel();
                log.debug("Channel is not open. Creating a new channel.");
            }
            try {
                channel.exchangeDeclare(exchangeName, exchangeType, Boolean.parseBoolean(durable),
                                        Boolean.parseBoolean(autoDelete),
                                        null); // null since passing no extra arguments
            } catch (IOException e) {
                handleException("Error occurred while declaring exchange.", e);
            }
        }
        try {
            channel.close();
        } catch (TimeoutException e) {
            log.error("Error occurred while closing connection.", e);
        }
    }

    public static void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RabbitMQException(message, e);
    }
}
