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

package org.wso2.micro.integrator.http.backend.test;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonServerExtension;
import org.wso2.esb.integration.common.utils.CPUMonitor;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.micro.integrator.http.utils.BackendResponse;
import org.wso2.micro.integrator.http.utils.BackendServer;
import org.wso2.micro.integrator.http.utils.HTTPRequestWithBackendResponse;
import org.wso2.micro.integrator.http.utils.MultiThreadedHTTPClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import static org.testng.Assert.assertEquals;
import static org.wso2.micro.integrator.http.utils.Constants.CLIENT_INSTANCES;
import static org.wso2.micro.integrator.http.utils.Constants.HTTPCORE_BE_API_CONTEXT;
import static org.wso2.micro.integrator.http.utils.Constants.HTTPS_BACKEND_PORT;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_BACKEND_PORT;
import static org.wso2.micro.integrator.http.utils.Constants.JAVAX_KEYSTORE_PASSWORD_PROP;
import static org.wso2.micro.integrator.http.utils.Constants.JAVAX_KEYSTORE_PROP;
import static org.wso2.micro.integrator.http.utils.Constants.KEYSTORE_PASS;
import static org.wso2.micro.integrator.http.utils.Constants.KEYSTORE_PATH;
import static org.wso2.micro.integrator.http.utils.Utils.checkCPUUsage;
import static org.wso2.micro.integrator.http.utils.Utils.getPayload;

/**
 * This class provides an abstraction for HTTP Core backend scenario test cases.
 */
public abstract class HTTPCoreBackendTest extends ESBIntegrationTest {

    private static CloseableHttpClient httpclient;
    private static CPUMonitor cpuMonitor;
    private List<BackendServer> backendServerList;

    @BeforeClass
    public void init() throws Exception {

        cpuMonitor = new CPUMonitor();
        startBackendServers();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(CLIENT_INSTANCES);
        cm.setDefaultMaxPerRoute(CLIENT_INSTANCES);

        httpclient = HttpClients.custom().setConnectionManager(cm).build();
        super.init();
    }

    @BeforeMethod
    public void beforeTestCase(Object[] testArgs) throws Exception {

        HTTPRequestWithBackendResponse httpRequestWithBackendResponse = (HTTPRequestWithBackendResponse) testArgs[0];
        CarbonServerExtension.restartServer();
        assertBackendServerStatus();
        setBackendServerParams(getPayload(httpRequestWithBackendResponse.getBackendResponse().getBackendPayloadSize()));
        cpuMonitor.startLogging();
    }

    @AfterMethod
    public void afterTestCase() {

        cpuMonitor.stop();
    }

    @AfterClass
    public void cleanUp() throws Exception {

        stopBackendServers();
        super.cleanup();
    }

    /**
     * This method will invoke the invokeHTTPCoreBETestAPI concurrently using an Executor Service. You can configure the
     * thread pool size and number of client instances using the constants CLIENT_THREAD_POOL_SIZE and
     * CLIENT_INSTANCES respectively.
     *
     * @param httpRequestWithBackendResponse The Mock HTTP request and backend response
     * @throws Exception If an error occurs while executing the client and the backend
     */
    protected void invokeHTTPCoreBETestAPI(HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception {

        String apiInvocationURL = httpRequestWithBackendResponse.getHttpRequest().isSSLEnabled() ?
                getApiInvocationURLHttps(HTTPCORE_BE_API_CONTEXT) : getApiInvocationURL(HTTPCORE_BE_API_CONTEXT);

        apiInvocationURL += populatePathParam(httpRequestWithBackendResponse.getBackendResponse());

        List<Future<Boolean>> list = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(
                org.wso2.micro.integrator.http.utils.Constants.CLIENT_THREAD_POOL_SIZE);
        for (int i = 0; i < CLIENT_INSTANCES; i++) {
            Future<Boolean> future = executorService.submit(new MultiThreadedHTTPClient(httpclient, apiInvocationURL,
                    httpRequestWithBackendResponse) {
                @Override
                protected boolean onResponseReceived(CloseableHttpResponse response,
                                                     HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
                        throws Exception {

                    return validateResponse(response, httpRequestWithBackendResponse);
                }
            });
            list.add(future);
        }
        for (Future<?> future : list) {
            future.get();
        }
        executorService.shutdown();

        assertCPUUsage();
    }

    /**
     * Helper method to start the servers defined in the backend server list.
     */
    private void startBackendServers() throws Exception {

        System.setProperty(JAVAX_KEYSTORE_PROP, KEYSTORE_PATH);
        System.setProperty(JAVAX_KEYSTORE_PASSWORD_PROP, KEYSTORE_PASS);

        backendServerList = getBackEndServers();

        for (BackendServer server : backendServerList) {
            server.start();
        }
    }

    /**
     * Helper method to stop the servers defined in the backend server list.
     */
    private void stopBackendServers() {

        for (BackendServer server : backendServerList) {
            server.shutdown();

            Awaitility.await().pollInterval(100, TimeUnit.MILLISECONDS).
                    atMost(15, TimeUnit.SECONDS).
                    until(hasServerStopped(server));
        }
    }

    /**
     * This method will set properties to the backend servers.
     *
     * @param backendPayload The response payload that the backend server must sent
     */
    private void setBackendServerParams(String backendPayload) {

        for (BackendServer server : backendServerList) {
            server.setPayload(backendPayload);
        }
    }

    /**
     * This method must be implemented by concrete implementations of this class to provide the backend server
     * implementation.
     *
     * @return List of implemented backend servers
     * @throws Exception If an error occurs while creating the mock backends
     */
    protected abstract List<BackendServer> getBackEndServers() throws Exception;

    /**
     * This method must be implemented by concrete implementations of this class to provide the logic to validate the
     * response.
     *
     * @param response                       The HTTP response returned by the client
     * @param httpRequestWithBackendResponse The Mock HTTP request and backend response
     * @throws Exception If an error occurs while validating the response
     */
    protected abstract boolean validateResponse(CloseableHttpResponse response,
                                                HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception;

    /**
     * This method will return a ServerSocket with or without SSL.
     *
     * @param enableSSL Whether the server socket should have SSL or not
     * @return ServerSocket with or without SSL
     */
    protected static ServerSocket getServerSocket(boolean enableSSL) throws IOException {

        ServerSocketFactory ssf;
        if (enableSSL) {
            ssf = SSLServerSocketFactory.getDefault();
            return ssf.createServerSocket(HTTPS_BACKEND_PORT);
        }
        ssf = ServerSocketFactory.getDefault();
        return ssf.createServerSocket(HTTP_BACKEND_PORT);
    }

    /**
     * Asserts whether the HTTP Status code in the response is 200.
     *
     * @param response The HTTP response received from MI
     */
    protected void assertHTTPStatusCodeEquals200(CloseableHttpResponse response) {

        assertHTTPStatusCode(response, 200);
    }

    /**
     * Asserts whether the HTTP Status code in the response is 400.
     *
     * @param response The HTTP response received from MI
     */
    protected void assertHTTPStatusCodeEquals400(CloseableHttpResponse response) {

        assertHTTPStatusCode(response, 400);
    }

    /**
     * Asserts whether the HTTP Status code in the response is 500.
     *
     * @param response The HTTP response received from MI
     */
    protected void assertHTTPStatusCodeEquals500(CloseableHttpResponse response) {

        assertHTTPStatusCode(response, 500);
    }

    /**
     * Asserts the HTTP Status code in the response.
     *
     * @param response       The HTTP response received from MI
     * @param expectedHTTPSC The expected HTTP status code
     */
    private void assertHTTPStatusCode(CloseableHttpResponse response, int expectedHTTPSC) {

        assertEquals(response.getStatusLine().getStatusCode(), expectedHTTPSC, "Invalid HTTP Status code received");
    }

    /**
     * Asserts the CPU usage. This method will add an alias to track the assertion that was called after closing the
     * socket.
     */
    private static void assertCPUUsage() {

        checkCPUUsage(cpuMonitor, "CPU settled after closing the socket by client");
    }

    /**
     * Returns an url string which contains the backend server protocol and port.
     *
     * @param backendResponse The Mock HTTP backend response
     * @return A string which contains the backend server protocol and port
     */
    private static String populatePathParam(BackendResponse backendResponse) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/");
        stringBuilder.append(backendResponse.getProtocol());
        stringBuilder.append("/");
        stringBuilder.append(backendResponse.getPort());
        return stringBuilder.toString();
    }

    /**
     * Asserts whether the Backend Servers are running.
     */
    private void assertBackendServerStatus() {

        for (BackendServer server : backendServerList) {
            Awaitility.await().pollInterval(10, TimeUnit.MILLISECONDS).
                    atMost(5, TimeUnit.SECONDS).
                    until(hasThreadStarted(server));
        }
    }

    private Callable<Boolean> hasServerStopped(BackendServer server) {

        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                return server == null || !server.isAlive();
            }
        };
    }

    private Callable<Boolean> hasThreadStarted(final Thread thread) {

        return thread::isAlive;
    }
}
