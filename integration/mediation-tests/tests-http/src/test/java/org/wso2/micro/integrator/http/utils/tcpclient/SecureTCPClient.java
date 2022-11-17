/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.http.utils.tcpclient;

import org.wso2.micro.integrator.http.utils.HTTPRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * This class extends Client to use a Secure socket for the communications.
 */
public abstract class SecureTCPClient extends Client {

    private final SSLSocketFactory sslSocketFactory;
    private SSLSocket socket;

    public SecureTCPClient(String host, int port, String keyStorePath, String keyStorePassword, String keyPassword,
                           HTTPRequest httpRequest) {

        super(host, port, httpRequest);

        SSLContext sslContext = createSSLContext(keyStorePath, keyStorePassword, keyPassword);
        // Create socket factory
        sslSocketFactory = sslContext.getSocketFactory();
    }

    @Override
    public void connect() throws Exception {
        // Create socket
        socket = (SSLSocket) sslSocketFactory.createSocket(getHost(), getPort());

        // Start handshake
        socket.startHandshake();

        // Get session after the connection is established
        SSLSession sslSession = socket.getSession();

        log.info("SecureTCPClient started with following SSL Session :");
        log.info("\tProtocol : " + sslSession.getProtocol());
        log.info("\tCipher suite : " + sslSession.getCipherSuite());

        printStream = new PrintStream(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void disconnect() throws Exception {

        log.info("SecureTCPClient closed :");
        socket.close();
    }

    // Create the and initialize the SSLContext
    private SSLContext createSSLContext(String keyStorePath, String keyStorePassword, String keyPassword) {

        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());

            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyPassword.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();

            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km, tm, null);

            return sslContext;
        } catch (Exception ex) {
            log.error("Error initializing SSLContext ", ex);
        }
        return null;
    }
}
