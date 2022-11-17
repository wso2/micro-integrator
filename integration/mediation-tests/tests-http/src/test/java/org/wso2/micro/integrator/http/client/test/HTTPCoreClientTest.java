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

package org.wso2.micro.integrator.http.client.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonServerExtension;
import org.wso2.esb.integration.common.utils.CPUMonitor;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HTTPRequest;
import org.wso2.micro.integrator.http.utils.HttpRequestWithExpectedHTTPSC;
import org.wso2.micro.integrator.http.utils.RequestMethods;
import org.wso2.micro.integrator.http.utils.tcpclient.Client;
import org.wso2.micro.integrator.http.utils.tcpclient.SecureTCPClient;
import org.wso2.micro.integrator.http.utils.tcpclient.TCPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.wso2.micro.integrator.http.utils.Utils.checkCPUUsage;
import static org.wso2.micro.integrator.http.utils.Utils.getPayload;

/**
 * This class provides an abstraction for HTTP Core client scenario test cases.
 */
public abstract class HTTPCoreClientTest extends ESBIntegrationTest {

    private static CPUMonitor cpuMonitor;

    /**
     * This method will invoke the HTTPCoreTestAPI concurrently using an Executor Service. You can configure the
     * thread pool size and number of client instances using the constants CLIENT_THREAD_POOL_SIZE and
     * CLIENT_INSTANCES respectively.
     *
     * @param httpRequest The Mock HTTP request
     * @throws Exception If an error occurs while executing the socket client
     */
    protected void invokeHTTPCoreTestAPI(HTTPRequest httpRequest)
            throws Exception {

        List<Future<Boolean>> list = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(Constants.CLIENT_THREAD_POOL_SIZE);
        for (int i = 0; i < Constants.CLIENT_INSTANCES; i++) {
            Future<Boolean> future = executorService.submit(getTCPClient(httpRequest));
            list.add(future);
        }
        for (Future<?> future : list) {
            future.get();
        }
        executorService.shutdown();

        assertCPUUsageAfterClosingSocket();
    }

    @BeforeClass
    public void init() throws Exception {

        cpuMonitor = new CPUMonitor();
        super.init();
    }

    @BeforeMethod
    public void beforeTestCase() throws Exception {

        CarbonServerExtension.restartServer();
        cpuMonitor.startLogging();
    }

    @AfterMethod
    public void afterTestCase() {

        cpuMonitor.stop();
    }

    @AfterClass
    public void cleanUp() throws Exception {

        super.cleanup();
    }

    /**
     * This method must be implemented by concrete implementations of this class to provide the logic to send the
     * HTTP request.
     *
     * @param printWriter The PrintStream returned by the socket
     * @param method      The HTTP method associated with this request
     * @param payload     The Payload associated with this request
     * @throws Exception If an error occurs while writing the request
     */
    protected abstract void sendHTTPRequest(PrintStream printWriter, RequestMethods method, String payload)
            throws Exception;

    /**
     * Reads the response and assert the HTTP status code.
     *
     * @param reader      The BufferedReader returned by the socket
     * @param httpRequest The mock HTTP request with the expected HTTP status code
     * @throws Exception If an error occurs while reading the response
     */
    protected void readHTTPResponse(BufferedReader reader, HttpRequestWithExpectedHTTPSC httpRequest) throws Exception {

        Assert.assertTrue(getResponseAsString(reader).contains(httpRequest.getExpectedHTTPSC()),
                "A " + httpRequest.getExpectedHTTPSC() + " HTTP Status");
    }

    /**
     * Returns the HTTP response as a string.
     *
     * @param bufferedReader The BufferedReader returned by the socket
     * @return A string which contains the complete HTTP response
     * @throws IOException If an I/O error occurs while reading the response
     */
    private String getResponseAsString(BufferedReader bufferedReader) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            if (line.trim().equals("0")) {
                break;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Asserts the CPU usage. This method will add an alias to track the assertion that was called after closing the
     * socket.
     */
    private static void assertCPUUsageAfterClosingSocket() {

        checkCPUUsage(cpuMonitor, "CPU settled after closing the socket by client");
    }

    /**
     * This method will return a TCP client with or without SSL depending on the mock HTTP request.
     *
     * @param httpRequest The Mock HTTP request
     * @return A TCP client with or without SSL
     */
    private Client getTCPClient(HTTPRequest httpRequest) {

        if (httpRequest.isSSLEnabled()) {
            return new SecureTCPClient(
                    Constants.HOST, Constants.HTTPS_PORT, Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASS,
                    Constants.KEYSTORE_PASS, httpRequest) {
                @Override
                protected void sendRequest(PrintStream printWriter, HTTPRequest httpRequest)
                        throws Exception {

                    sendHTTPRequest(printWriter, httpRequest.getMethod(), getPayload(httpRequest.getPayloadSize()));
                }

                @Override
                protected void readResponse(BufferedReader bufferedReader, HttpRequestWithExpectedHTTPSC httpRequest)
                        throws Exception {

                    readHTTPResponse(bufferedReader, httpRequest);
                }
            };
        }
        return new TCPClient(Constants.HOST, Constants.HTTP_PORT, httpRequest) {
            @Override
            protected void sendRequest(PrintStream printWriter, HTTPRequest httpRequest)
                    throws Exception {

                sendHTTPRequest(printWriter, httpRequest.getMethod(), getPayload(httpRequest.getPayloadSize()));
            }

            @Override
            protected void readResponse(BufferedReader bufferedReader, HttpRequestWithExpectedHTTPSC httpRequest)
                    throws Exception {

                readHTTPResponse(bufferedReader, httpRequest);
            }
        };
    }
}
