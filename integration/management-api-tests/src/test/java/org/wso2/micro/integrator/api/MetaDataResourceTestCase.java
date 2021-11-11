/*
 *Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.micro.integrator.api;

import org.awaitility.Awaitility;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonTestServerManager;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Test case to test server resource of the management api.
 */
public class MetaDataResourceTestCase extends ESBIntegrationTest {

    private String carbonHome;
    private int offset;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        offset = portOffset + 10;
    }

    @Test(groups = { "wso2.esb" }, description = "Test restart operation")
    public void testRestart() throws IOException, InterruptedException, AutomationFrameworkException {

        startNewServer();

        CarbonLogReader carbonLogReader = new CarbonLogReader(getCarbonHome());
        carbonLogReader.start();

        sendPatchRequest("restart");
        Assert.assertTrue(carbonLogReader.checkForLog("Restarting WSO2 Micro Integrator", 120));
        Assert.assertTrue(carbonLogReader.checkForLog("WSO2 Micro Integrator started in", 120));
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).
                until(isManagementApiAvailable());
    }

    @Test(groups = { "wso2.esb" }, dependsOnMethods = {"testRestart"}, description = "Test shutdown operation")
    public void testShutdown() throws IOException, InterruptedException, AutomationFrameworkException {

        CarbonLogReader carbonLogReader = new CarbonLogReader(getCarbonHome());
        carbonLogReader.start();

        sendPatchRequest("shutdown");

        Assert.assertTrue(carbonLogReader.checkForLog("Shutting down the task manager", 120));
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).
                until(isManagementApiUnAvailable());
    }

    @Test(groups = { "wso2.esb" }, dependsOnMethods = {"testShutdown"},
          description = "Test gracefully shutdown operation")
    public void testRestartGracefully() throws IOException, InterruptedException, AutomationFrameworkException {

        startNewServer();

        CarbonLogReader carbonLogReader = new CarbonLogReader(getCarbonHome());
        carbonLogReader.start();

        sendPatchRequest("restartGracefully");

        Assert.assertTrue(carbonLogReader.checkForLog("Gracefully restarting WSO2 Micro Integrator", 120));
        Assert.assertTrue(carbonLogReader.checkForLog(
                "Starting a new Carbon instance. Current instance will be shutdown", 120));
        Assert.assertTrue(carbonLogReader.checkForLog("WSO2 Micro Integrator started in", 120));
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).
                until(isManagementApiAvailable());
    }

    @Test(groups = { "wso2.esb" }, dependsOnMethods = {"testRestartGracefully"},
          description = "Test gracefully shutdown operation")
    public void testShutdownGracefully() throws IOException, InterruptedException {

        CarbonLogReader carbonLogReader = new CarbonLogReader(getCarbonHome());
        carbonLogReader.start();

        sendPatchRequest("shutdownGracefully");

        Assert.assertTrue(carbonLogReader.checkForLog("Gracefully shutting down WSO2 Micro Integrator", 120));
        Assert.assertTrue(carbonLogReader.checkForLog("Shutting down the task manager", 120));
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).
                until(isManagementApiUnAvailable());
    }

    /**
     * Return the the location of the server instance.
     * @return carbon home
     */
    private String getCarbonHome() {
        return carbonHome;
    }

    /**
     * Start a new server.
     * @throws IOException
     * @throws AutomationFrameworkException
     */
    private void startNewServer() throws IOException, AutomationFrameworkException {
        HashMap<String, String> startupParameterMap = new HashMap<>();
        startupParameterMap.put("-DportOffset", String.valueOf(offset));
        startupParameterMap.put("startupScript", "micro-integrator");

        CarbonTestServerManager server =
                new CarbonTestServerManager(context, System.getProperty("carbon.zip"), startupParameterMap);
        server.startServer();
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).
                until(isManagementApiAvailable());
        carbonHome = server.getCarbonHome();
        Assert.assertNotNull(carbonHome);
    }

    /**
     * Return if the management api is available.
     * @return
     */
    public Callable<Boolean> isManagementApiAvailable() {
        return this::checkIfManagementApiAvailable;
    }

    /**
     * return if the management api is unavailable.
     * @return
     */
    public Callable<Boolean> isManagementApiUnAvailable() {
        return () -> !checkIfManagementApiAvailable();
    }

    /**
     * Check if the management api is available
     * @return
     */
    private boolean checkIfManagementApiAvailable() {
        try (Socket socket = new Socket(hostName, DEFAULT_INTERNAL_API_HTTPS_PORT + offset)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Send patch request to change the server status.
     * @param status - server status
     * @throws IOException
     */
    private void sendPatchRequest(String status) throws IOException {
        String accessToken = TokenUtil.getAccessToken(hostName, offset);
        Assert.assertNotNull(accessToken);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + offset) + "/management/"
                          + "server";

        JSONObject payload = new JSONObject();
        payload.put("status", status);

        SimpleHttpClient client = new SimpleHttpClient();
        client.doPatch(endpoint, headers, payload.toString(), "application/json");
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
        // revert the portOffset changes
        System.setProperty("port.offset", String.valueOf(portOffset));
    }
}
