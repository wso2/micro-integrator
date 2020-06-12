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

package org.wso2.carbon.inbound.endpoint.protocol.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Helper class to support AMQP inbound endpoint related functions
 */
public class RabbitMQUtils {

    private static final Log log = LogFactory.getLog(RabbitMQUtils.class);

    /**
     * Create a connection from given connection factory and address array
     *
     * @param factory   a {@link ConnectionFactory} object
     * @param addresses a {@link Address} object
     * @return a {@link Connection} object
     * @throws IOException
     */
    public static Connection createConnection(ConnectionFactory factory, Address[] addresses) throws IOException {
        Connection connection = null;
        try {
            connection = factory.newConnection(addresses);
        } catch (TimeoutException e) {
            log.error("Error occurred while creating a connection", e);
        }
        return connection;
    }

    /**
     * Get transport headers from the rabbitmq message
     *
     * @param properties the AMQP basic properties
     * @return a map of headers
     */
    public static Map<String, String> getTransportHeaders(AMQP.BasicProperties properties) {
        Map<String, String> map = new HashMap<>();

        // correlation ID
        if (properties.getCorrelationId() != null) {
            map.put(RabbitMQConstants.CORRELATION_ID, properties.getCorrelationId());
        }

        // if a AMQP message ID is found
        if (properties.getMessageId() != null) {
            map.put(RabbitMQConstants.MESSAGE_ID, properties.getMessageId());
        }

        // replyto destination name
        if (properties.getReplyTo() != null) {
            map.put(RabbitMQConstants.RABBITMQ_REPLY_TO, properties.getReplyTo());
        }

        // any other transport properties / headers
        Map<String, Object> headers = properties.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (String headerName : headers.keySet()) {
                String value = headers.get(headerName).toString();
                map.put(headerName, value);
            }
        }

        return map;
    }

    public static boolean isDurableQueue(Map<String, String> properties) {
        return BooleanUtils
                .toBoolean(BooleanUtils.toBooleanObject(properties.get(RabbitMQConstants.QUEUE_DURABLE)));
    }

    public static boolean isExclusiveQueue(Map<String, String> properties) {
        return BooleanUtils
                .toBoolean(BooleanUtils.toBooleanObject(properties.get(RabbitMQConstants.QUEUE_EXCLUSIVE)));
    }

    public static boolean isAutoDeleteQueue(Map<String, String> properties) {
        return BooleanUtils
                .toBoolean(BooleanUtils.toBooleanObject(properties.get(RabbitMQConstants.QUEUE_AUTO_DELETE)));
    }

    public static boolean isDurableExchange(Map<String, String> properties) {
        return BooleanUtils
                .toBoolean(BooleanUtils.toBooleanObject(properties.get(RabbitMQConstants.EXCHANGE_DURABLE)));
    }

    public static boolean isAutoDeleteExchange(Map<String, String> properties) {
        return BooleanUtils
                .toBoolean(BooleanUtils.toBooleanObject(properties.get(RabbitMQConstants.EXCHANGE_AUTO_DELETE)));
    }

    /**
     * Sets optional arguments that can be defined at the queue declaration
     *
     * @param properties amqp properties
     * @return map of optional arguments
     */
    private static Map<String, Object> setQueueOptionalArguments(Map<String, String> properties) {
        Map<String, Object> optionalArgs = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propertyKey = entry.getKey();
            if (propertyKey.startsWith(RabbitMQConstants.QUEUE_OPTIONAL_ARG_PREFIX)) {
                String optionalArgName = propertyKey.substring(RabbitMQConstants.QUEUE_OPTIONAL_ARG_PREFIX.length());
                String optionalArgValue = entry.getValue();
                //check whether a boolean argument
                if ("true".equals(optionalArgValue) || "false".equals(optionalArgValue)) {
                    optionalArgs.put(optionalArgName, Boolean.parseBoolean(optionalArgValue));
                } else {
                    try {
                        //check whether a integer argument
                        optionalArgs.put(optionalArgName, Integer.parseInt(optionalArgValue));
                    } catch (NumberFormatException e) {
                        optionalArgs.put(optionalArgName, optionalArgValue);
                    }
                }
            }
        }
        return optionalArgs.size() == 0 ? null : optionalArgs;
    }

    /**
     * Sets optional arguments that can be defined at the exchange declaration
     *
     * @param properties amqp properties
     * @return map of optional arguments
     */
    private static Map<String, Object> setExchangeOptionalArguments(Map<String, String> properties) {
        Map<String, Object> optionalArgs = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propertyKey = entry.getKey();
            if (propertyKey.startsWith(RabbitMQConstants.EXCHANGE_OPTIONAL_ARG_PREFIX)) {
                String optionalArgName = propertyKey.substring(RabbitMQConstants.EXCHANGE_OPTIONAL_ARG_PREFIX.length());
                String optionalArgValue = entry.getValue();
                //check whether a boolean argument
                if ("true".equals(optionalArgValue) || "false".equals(optionalArgValue)) {
                    optionalArgs.put(optionalArgName, Boolean.parseBoolean(optionalArgValue));
                } else {
                    try {
                        //check whether a integer argument
                        optionalArgs.put(optionalArgName, Integer.parseInt(optionalArgValue));
                    } catch (NumberFormatException e) {
                        optionalArgs.put(optionalArgName, optionalArgValue);
                    }
                }
            }
        }
        return optionalArgs.size() == 0 ? null : optionalArgs;
    }

    /**
     * Helper method to declare queue when direct channel is given
     *
     * @param channel    a rabbitmq channel
     * @param queueName  a name of the queue to declare
     * @param properties queue declaration properties
     * @throws IOException
     */
    public static void declareQueue(Channel channel, String queueName,
                                    Map<String, String> properties) throws IOException {
        if (StringUtils.isNotEmpty(queueName)) {
            channel.queueDeclare(queueName, isDurableQueue(properties), isExclusiveQueue(properties),
                                 isAutoDeleteQueue(properties), setQueueOptionalArguments(properties));
        }
    }

    /**
     * Helper method to declare exchange when direct channel is given
     *
     * @param channel      {@link Channel} object
     * @param exchangeName the exchange exchangeName
     * @param properties   RabbitMQ properties
     */
    public static void declareExchange(Channel channel, String exchangeName, Map<String, String> properties)
            throws IOException {
        String type = properties.get(RabbitMQConstants.EXCHANGE_TYPE);
        String queueName = properties.get(RabbitMQConstants.QUEUE_NAME);
        String routingKey = properties.get(RabbitMQConstants.QUEUE_ROUTING_KEY);
        if (StringUtils.isNotEmpty(exchangeName)) {
            // declare the exchange
            if (!exchangeName.startsWith(RabbitMQConstants.AMQ_PREFIX)) {
                if (StringUtils.isNotEmpty(type)) {
                    channel.exchangeDeclare(exchangeName, type, isDurableExchange(properties),
                                            isAutoDeleteExchange(properties), setExchangeOptionalArguments(properties));
                } else {
                    channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, isDurableExchange(properties),
                                            isAutoDeleteExchange(properties), setExchangeOptionalArguments(properties));
                }
            }
            // bind the queue and exchange with routing key
            if (StringUtils.isNotEmpty(queueName) && StringUtils.isNotEmpty(routingKey)) {
                channel.queueBind(queueName, exchangeName, routingKey);
            } else if (StringUtils.isNotEmpty(queueName) && StringUtils.isEmpty(routingKey)) {
                if (log.isDebugEnabled()) {
                    log.debug("No routing key specified. The queue name is using as the routing key.");
                }
                routingKey = queueName;
                channel.queueBind(queueName, exchangeName, routingKey);
            }
        }
    }

    /**
     * Build SOAP envelop from AMQP properties and byte body
     *
     * @param properties the AMQP basic properties
     * @param body       the message body
     * @param msgContext the message context
     * @return content-type used to build the soap message
     * @throws AxisFault
     */
    public static String buildMessage(AMQP.BasicProperties properties, byte[] body, MessageContext msgContext)
            throws AxisFault {
        // set correlation id to the message context
        String amqpCorrelationID = properties.getCorrelationId();
        if (amqpCorrelationID != null && amqpCorrelationID.length() > 0) {
            msgContext.setProperty(RabbitMQConstants.CORRELATION_ID, amqpCorrelationID);
        } else {
            msgContext.setProperty(RabbitMQConstants.CORRELATION_ID, properties.getMessageId());
        }
        // set content-type to the message context
        String contentType = properties.getContentType();
        if (contentType == null) {
            contentType = RabbitMQConstants.DEFAULT_CONTENT_TYPE;
        }
        msgContext.setProperty(RabbitMQConstants.CONTENT_TYPE, contentType);
        // set content encoding to the message context
        if (properties.getContentEncoding() != null) {
            msgContext.setProperty(RabbitMQConstants.CONTENT_ENCODING, properties.getContentEncoding());
        }

        int index = contentType.indexOf(';');
        String type = index > 0 ? contentType.substring(0, index) : contentType;
        Builder builder = BuilderUtil.getBuilderFromSelector(type, msgContext);
        if (builder == null) {
            if (log.isDebugEnabled()) {
                log.debug("No message builder found for type '" + type + "'. Falling back to SOAP.");
            }
            builder = new SOAPBuilder();
        }

        OMElement documentElement;
        String charSetEnc = null;
        try {
            charSetEnc = new ContentType(contentType).getParameter("charset");
        } catch (ParseException ex) {
            log.error("Parse error", ex);
        }
        msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

        documentElement = builder.processDocument(
                new ByteArrayInputStream(body), contentType,
                msgContext);

        msgContext.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));

        return contentType;
    }
}
