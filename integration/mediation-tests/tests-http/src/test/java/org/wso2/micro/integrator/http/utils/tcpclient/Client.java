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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.http.utils.HTTPRequest;
import org.wso2.micro.integrator.http.utils.HttpRequestWithExpectedHTTPSC;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.concurrent.Callable;

/**
 * This class provides the base implementation for TCP client with or without SSL.
 */
public abstract class Client implements Callable<Boolean> {

    protected static final Log log = LogFactory.getLog(Client.class);
    private final String host;
    private final int port;
    protected HTTPRequest httpRequest;
    protected PrintStream printStream;
    protected BufferedReader bufferedReader;

    public Client(String host, int port, HTTPRequest httpRequest) {

        this.host = host;
        this.port = port;
        this.httpRequest = httpRequest;
    }

    @Override
    public Boolean call() throws Exception {

        connect();
        sendRequest(printStream, httpRequest);
        if (httpRequest instanceof HttpRequestWithExpectedHTTPSC) {
            readResponse(bufferedReader, (HttpRequestWithExpectedHTTPSC) httpRequest);
        }
        disconnect();
        return true;
    }

    protected abstract void readResponse(BufferedReader bufferedReader, HttpRequestWithExpectedHTTPSC httpRequest)
            throws Exception;

    protected abstract void sendRequest(PrintStream printWriter, HTTPRequest httpRequest)
            throws Exception;

    public abstract void connect() throws Exception;

    public abstract void disconnect() throws Exception;

    protected String getHost() {

        return host;
    }

    protected int getPort() {

        return port;
    }
}
