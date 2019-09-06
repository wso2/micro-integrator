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
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Encapsulate a RabbitMQ AMQP Connection factory definition within an inbound configuration
 * <p/>
 * Connection Factory definitions, allows service level parameters to be defined,
 * and re-used by each service that binds to it
 */
public class RabbitMQConnectionFactory {

    private static final Log log = LogFactory.getLog(RabbitMQConnectionFactory.class);

    private ConnectionFactory connectionFactory;
    private String name;
    private Hashtable<String, String> parameters = new Hashtable<String, String>();
    private int retryInterval = RabbitMQConstants.DEFAULT_RETRY_INTERVAL;
    private int retryCount = RabbitMQConstants.DEFAULT_RETRY_COUNT;
    private Address[] addresses;

    /**
     * Digest a AMQP CF definition from the configuration and construct
     */
    public RabbitMQConnectionFactory(Properties properties) {

        this.name = properties.getProperty(RabbitMQConstants.RABBITMQ_CON_FAC);

        for (final String name : properties.stringPropertyNames())
            parameters.put(name, properties.getProperty(name));
        initConnectionFactory();

    }

    /**
     * Create a connection factory based on given parameters
     *
     * @param name       Name of the connection factory
     * @param parameters parameters containing the required to create the connection factory
     */
    public RabbitMQConnectionFactory(String name, Hashtable<String, String> parameters) {
        this.name = name;
        this.parameters = parameters;
        initConnectionFactory();
        log.info("RabbitMQ ConnectionFactory : " + name + " initialized");
    }

    /**
     * get connection factory name
     *
     * @return connection factory name
     */
    public String getName() {
        return name;
    }

    /**
     * Set connection factory name
     *
     * @param name name to set for the connection Factory
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return parameters.get(RabbitMQConstants.CONTENT_TYPE);
    }

    /**
     * Catch an exception an throw a AxisRabbitMQException with message
     *
     * @param msg message to set for the exception
     * @param e   throwable to set
     */
    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new RabbitMQException(msg, e);
    }

    /**
     * Catch an exception an throw a RabbitMQException with message
     *
     * @param msg message to set for the exception
     */
    private void handleException(String msg) {
        log.error(msg);
        throw new RabbitMQException(msg);
    }

    /**
     * Get all rabbit mq parameters
     *
     * @return a map of parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Initialize connection factory
     */
    public void initConnectionFactory() {
        connectionFactory = new ConnectionFactory();
        String hostName = parameters.get(RabbitMQConstants.SERVER_HOST_NAME);
        String portValue = parameters.get(RabbitMQConstants.SERVER_PORT);
        String serverRetryIntervalS = parameters.get(RabbitMQConstants.SERVER_RETRY_INTERVAL);
        String retryIntervalS = parameters.get(RabbitMQConstants.RETRY_INTERVAL);
        String retryCountS = parameters.get(RabbitMQConstants.RETRY_COUNT);
        String heartbeat = parameters.get(RabbitMQConstants.HEARTBEAT);
        String connectionTimeout = parameters.get(RabbitMQConstants.CONNECTION_TIMEOUT);
        String sslEnabledS = parameters.get(RabbitMQConstants.SSL_ENABLED);
        String userName = parameters.get(RabbitMQConstants.SERVER_USER_NAME);
        String password = parameters.get(RabbitMQConstants.SERVER_PASSWORD);
        String virtualHost = parameters.get(RabbitMQConstants.SERVER_VIRTUAL_HOST);

        if (!StringUtils.isEmpty(heartbeat)) {
            try {
                int heartbeatValue = Integer.parseInt(heartbeat);
                connectionFactory.setRequestedHeartbeat(heartbeatValue);
            } catch (NumberFormatException e) {
                //proceeding with rabbitmq default value
                log.warn("Number format error in reading heartbeat value. Proceeding with default");
            }
        }
        if (!StringUtils.isEmpty(connectionTimeout)) {
            try {
                int connectionTimeoutValue = Integer.parseInt(connectionTimeout);
                connectionFactory.setConnectionTimeout(connectionTimeoutValue);
            } catch (NumberFormatException e) {
                //proceeding with rabbitmq default value
                log.warn("Number format error in reading connection timeout value. Proceeding with default");
            }
        }

        if (!StringUtils.isEmpty(sslEnabledS)) {
            try {
                boolean sslEnabled = Boolean.parseBoolean(sslEnabledS);
                if (sslEnabled) {
                    String keyStoreLocation = parameters.get(RabbitMQConstants.SSL_KEYSTORE_LOCATION);
                    String keyStoreType = parameters.get(RabbitMQConstants.SSL_KEYSTORE_TYPE);
                    String keyStorePassword = parameters.get(RabbitMQConstants.SSL_KEYSTORE_PASSWORD);
                    String trustStoreLocation = parameters.get(RabbitMQConstants.SSL_TRUSTSTORE_LOCATION);
                    String trustStoreType = parameters.get(RabbitMQConstants.SSL_TRUSTSTORE_TYPE);
                    String trustStorePassword = parameters.get(RabbitMQConstants.SSL_TRUSTSTORE_PASSWORD);
                    String sslVersion = parameters.get(RabbitMQConstants.SSL_VERSION);

                    if (StringUtils.isEmpty(keyStoreLocation) || StringUtils.isEmpty(keyStoreType) || StringUtils
                            .isEmpty(keyStorePassword) || StringUtils.isEmpty(trustStoreLocation) || StringUtils
                            .isEmpty(trustStoreType) || StringUtils.isEmpty(trustStorePassword)) {
                        log.warn(
                                "Truststore and keystore information is not provided correctly. Proceeding with default SSL configuration");
                        connectionFactory.useSslProtocol();
                    } else {
                        char[] keyPassphrase = keyStorePassword.toCharArray();
                        KeyStore ks = KeyStore.getInstance(keyStoreType);
                        ks.load(new FileInputStream(keyStoreLocation), keyPassphrase);

                        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                        kmf.init(ks, keyPassphrase);

                        char[] trustPassphrase = trustStorePassword.toCharArray();
                        KeyStore tks = KeyStore.getInstance(trustStoreType);
                        tks.load(new FileInputStream(trustStoreLocation), trustPassphrase);

                        TrustManagerFactory tmf = TrustManagerFactory
                                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
                        tmf.init(tks);

                        SSLContext c = SSLContext.getInstance(sslVersion);
                        c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                        connectionFactory.useSslProtocol(c);
                    }
                }
            } catch (Exception e) {
                log.warn("Format error in SSL enabled value. Proceeding without enabling SSL", e);
            }
        }

        if (!StringUtils.isEmpty(retryCountS)) {
            try {
                retryCount = Integer.parseInt(retryCountS);
            } catch (NumberFormatException e) {
                log.warn("Number format error in reading retry count value. Proceeding with default value (3)", e);
            }
        }

        // Resolving hostname(s) and port(s)
        if (!StringUtils.isEmpty(hostName) && !StringUtils.isEmpty(portValue)) {
            String[] hostNames = hostName.split(",");
            String[] portValues = portValue.split(",");
            if (hostNames.length == portValues.length) {
                addresses = new Address[hostNames.length];
                for (int i = 0; i < hostNames.length; i++) {
                    if (!hostNames[i].isEmpty() && !portValues[i].isEmpty()) {
                        try {
                            addresses[i] = new Address(hostNames[i].trim(), Integer.parseInt(portValues[i].trim()));
                        } catch (NumberFormatException e) {
                            handleException("Number format error in port number", e);
                        }
                    }
                }
            }
        } else {
            handleException("Host name(s) and port(s) are not correctly defined");
        }

        if (!StringUtils.isEmpty(userName)) {
            connectionFactory.setUsername(userName);
        }

        if (!StringUtils.isEmpty(password)) {
            connectionFactory.setPassword(password);
        }

        if (!StringUtils.isEmpty(virtualHost)) {
            connectionFactory.setVirtualHost(virtualHost);
        }

        if (!StringUtils.isEmpty(retryIntervalS)) {
            try {
                retryInterval = Integer.parseInt(retryIntervalS);
            } catch (NumberFormatException e) {
                log.warn("Number format error in reading retry interval value. Proceeding with default value (30000ms)",
                         e);
            }
        }

        if (!StringUtils.isEmpty(serverRetryIntervalS)) {
            try {
                int serverRetryInterval = Integer.parseInt(serverRetryIntervalS);
                connectionFactory.setNetworkRecoveryInterval(serverRetryInterval);
            } catch (NumberFormatException e) {
                log.warn("Number format error in reading server retry interval value. Proceeding with default value",
                         e);
            }
        }

        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setTopologyRecoveryEnabled(false);
    }

    /**
     * Create a rabbit mq connection
     *
     * @return a connection to the server
     */
    public Connection createConnection() throws IOException {
        Connection connection = null;
        try {
            connection = RabbitMQUtils.createConnection(connectionFactory, addresses);
            log.info("[" + name + "] Successfully connected to RabbitMQ Broker");
        } catch (IOException e) {
            log.error("[" + name + "] Error creating connection to RabbitMQ Broker. Reattempting to connect.", e);
            int retryC = 0;
            while ((connection == null) && ((retryCount == -1) || (retryC < retryCount))) {
                retryC++;
                log.info("[" + name + "] Attempting to create connection to RabbitMQ Broker" + " in " + retryInterval
                                 + " ms");
                try {
                    Thread.sleep(retryInterval);
                    connection = RabbitMQUtils.createConnection(connectionFactory, addresses);
                    log.info("[" + name + "] Successfully connected to RabbitMQ Broker");
                } catch (InterruptedException e1) {
                    log.error("[" + name + "] Error while trying to reconnect to RabbitMQ Broker", e1);
                } catch (IOException e2) {
                    log.error("[" + name + "] Error while trying to reconnect to RabbitMQ Broker", e2);
                }
            }
            if (connection == null) {
                handleException("[" + name + "] Could not connect to RabbitMQ Broker. Error while creating connection",
                                e);
            }
        }
        return connection;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public int getRetryCount() {
        return retryCount;
    }

}