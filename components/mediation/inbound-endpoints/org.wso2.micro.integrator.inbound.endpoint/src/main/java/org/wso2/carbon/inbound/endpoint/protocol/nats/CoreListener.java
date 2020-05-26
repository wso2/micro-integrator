/*
 * Copyright 2020 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * Core listener class which uses core NATS connection to receive messages.
 */
public class CoreListener implements NatsMessageListener {

    private static final Log log = LogFactory.getLog(CoreListener.class.getName());
    private String subject;
    private NatsInjectHandler injectHandler;
    private Properties natsProperties;
    private Connection connection;

    public CoreListener(String subject, NatsInjectHandler injectHandler, Properties natsProperties) {
        this.subject = subject;
        this.injectHandler = injectHandler;
        this.natsProperties = natsProperties;
    }

    /**
     * Create the connection to the Core NATS server.
     *
     * @return boolean value whether connection is created.
     */
    @Override public boolean createConnection() throws IOException, InterruptedException {
        if (connection == null) {
            connection = getNatsConnection();
        }
        return true;
    }

    /**
     * Create and return the Core NATS connection.
     *
     * @return the Core NATS connection.
     */
    public Connection getNatsConnection() throws IOException, InterruptedException {
        String bufferSize = natsProperties.getProperty(NatsConstants.BUFFER_SIZE);
        String turnOnAdvancedStats = natsProperties.getProperty(NatsConstants.TURN_ON_ADVANCED_STATS);
        String traceConnection = natsProperties.getProperty(NatsConstants.TRACE_CONNECTION);
        String tlsProtocol = validateParameter(natsProperties.getProperty(NatsConstants.TLS_PROTOCOL));
        String tlsKeyStoreType = validateParameter(natsProperties.getProperty(NatsConstants.TLS_KEYSTORE_TYPE));
        String tlsKeyStoreLocation = validateParameter(natsProperties.getProperty(NatsConstants.TLS_KEYSTORE_LOCATION));
        String tlsKeyStorePassword = validateParameter(natsProperties.getProperty(NatsConstants.TLS_KEYSTORE_PASSWORD));
        String tlsTrustStoreType = validateParameter(natsProperties.getProperty(NatsConstants.TLS_TRUSTSTORE_TYPE));
        String tlsTrustStoreLocation = validateParameter(
                natsProperties.getProperty(NatsConstants.TLS_TRUSTSTORE_LOCATION));
        String tlsTrustStorePassword = validateParameter(
                natsProperties.getProperty(NatsConstants.TLS_TRUSTSTORE_PASSWORD));
        String tlsKeyManagerAlgorithm = validateParameter(
                natsProperties.getProperty(NatsConstants.TLS_KEY_MANAGER_ALGORITHM));
        String tlsTrustManagerAlgorithm = validateParameter(
                natsProperties.getProperty(NatsConstants.TLS_TRUST_MANAGER_ALGORITHM));

        Options.Builder builder = new Options.Builder(natsProperties);

        if (StringUtils.isNotEmpty(bufferSize)) {
            builder.bufferSize(Integer.parseInt(bufferSize));
        }

        if (Boolean.parseBoolean(turnOnAdvancedStats)) {
            builder.turnOnAdvancedStats();
        }

        if (Boolean.parseBoolean(traceConnection)) {
            builder.traceConnection();
        }

        if (StringUtils.isNotEmpty(
                tlsProtocol + tlsTrustStoreType + tlsTrustStoreLocation + tlsTrustStorePassword + tlsKeyStoreType
                        + tlsKeyStoreLocation + tlsKeyStorePassword + tlsKeyManagerAlgorithm
                        + tlsTrustManagerAlgorithm)) {
            SSLContext sslContext = createSSLContext(
                    new TLSConnection(tlsProtocol, tlsTrustStoreType, tlsTrustStoreLocation, tlsTrustStorePassword,
                            tlsKeyStoreType, tlsKeyStoreLocation, tlsKeyStorePassword, tlsKeyManagerAlgorithm,
                            tlsTrustManagerAlgorithm));
            if (sslContext != null) {
                builder.sslContext(sslContext);
            }
        }
        return Nats.connect(builder.build());
    }

    /**
     * Consume the message received and inject into the sequence.
     *
     * @param sequenceName the sequence to inject the message to.
     */
    @Override public void initializeConsumer(String sequenceName) throws IOException, InterruptedException {
        if (createConnection()) {
            Dispatcher dispatcher;
            dispatcher = connection.createDispatcher(natsMessage -> {
                if (natsMessage != null) {
                    String message = new String(natsMessage.getData(), StandardCharsets.UTF_8);
                    if (log.isDebugEnabled()) {
                        log.debug("Message Received to NATS Inbound EP: " + message);
                    }
                    injectHandler.invoke(message.getBytes(), sequenceName, natsMessage.getReplyTo(), connection);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Message is null.");
                    }
                }
            });

            String queueGroup = natsProperties.getProperty(NatsConstants.QUEUE_GROUP);
            if (StringUtils.isNotEmpty(queueGroup)) {
                dispatcher.subscribe(subject, queueGroup);
            } else {
                dispatcher.subscribe(subject);
            }
        }
    }

    /**
     * Close the connection to NATS server and set connection to null.
     */
    @Override public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (InterruptedException e) {
            log.error("An error occurred while closing the connection. ", e);
        }
        connection = null;
    }

    /**
     * Validate null parameter.
     *
     * @param parameter the parameter to validate.
     * @return the updated parameter.
     */
    private String validateParameter(String parameter) {
        return StringUtils.isEmpty(parameter) ? "" : parameter;
    }

    /**
     * Create the SSLContext to establish connection with TLS.
     *
     * @param tlsConnection the TLS connection object.
     * @return the SSLContext or null if any exceptions.
     */
    private static SSLContext createSSLContext(TLSConnection tlsConnection) {
        try {
            KeyManagerFactory keyManagerFactory = null;
            if (StringUtils.isNotEmpty(tlsConnection.getKeyStoreLocation())) {
                KeyStore keyStore = loadKeyStore(tlsConnection.getKeyStoreType(), tlsConnection.getKeyStoreLocation(),
                        tlsConnection.getTrustStorePassword());
                keyManagerFactory = KeyManagerFactory.getInstance(tlsConnection.getKeyManagerAlgorithm().equals("") ?
                        NatsConstants.DEFAULT_TLS_ALGORITHM :
                        tlsConnection.getKeyManagerAlgorithm());
                keyManagerFactory.init(keyStore, tlsConnection.getKeyStorePassword().toCharArray());
            }

            KeyStore trustStore = loadKeyStore(tlsConnection.getTrustStoreType(), tlsConnection.getTrustStoreLocation(),
                    tlsConnection.getTrustStorePassword());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    tlsConnection.getTrustManagerAlgorithm().equals("") ?
                            NatsConstants.DEFAULT_TLS_ALGORITHM :
                            tlsConnection.getTrustManagerAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance(tlsConnection.getProtocol().equals("") ?
                    Options.DEFAULT_SSL_PROTOCOL :
                    tlsConnection.getProtocol());
            sslContext.init(keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
            log.error("Invalid TLS parameters. Establishing connection without TLS if possible.", e);
            return null;
        }
    }

    /**
     * Load key store and trust store.
     *
     * @param storeType          the type of store file.
     * @param storeLocation      the location of store file.
     * @param trustStorePassword the password of trust store file.
     * @return the store.
     */
    private static KeyStore loadKeyStore(String storeType, String storeLocation, String trustStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore store = KeyStore.getInstance(storeType.equals("") ? NatsConstants.DEFAULT_STORE_TYPE : storeType);
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(storeLocation))) {
            store.load(in, trustStorePassword.toCharArray());
        }
        return store;
    }
}

/**
 * Set the TLS connection properties to connect to the server with TLS.
 */
class TLSConnection {
    private String protocol;
    private String trustStoreType;
    private String trustStoreLocation;
    private String trustStorePassword;
    private String keyStoreType;
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyManagerAlgorithm;
    private String trustManagerAlgorithm;

    TLSConnection(String protocol, String trustStoreType, String trustStoreLocation, String trustStorePassword,
            String keyStoreType, String keyStoreLocation, String keyStorePassword, String keyManagerAlgorithm,
            String trustManagerAlgorithm) {
        this.protocol = protocol;
        this.trustStoreType = trustStoreType;
        this.trustStoreLocation = trustStoreLocation;
        this.trustStorePassword = trustStorePassword;
        this.keyStoreType = keyStoreType;
        this.keyStoreLocation = keyStoreLocation;
        this.keyStorePassword = keyStorePassword;
        this.keyManagerAlgorithm = keyManagerAlgorithm;
        this.trustManagerAlgorithm = trustManagerAlgorithm;
    }

    String getProtocol() {
        return protocol;
    }

    String getTrustStoreType() {
        return trustStoreType;
    }

    String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    String getTrustStorePassword() {
        return trustStorePassword;
    }

    String getKeyStoreType() {
        return keyStoreType;
    }

    String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    String getKeyStorePassword() {
        return keyStorePassword;
    }

    String getKeyManagerAlgorithm() {
        return keyManagerAlgorithm;
    }

    String getTrustManagerAlgorithm() {
        return trustManagerAlgorithm;
    }
}