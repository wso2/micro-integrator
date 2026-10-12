/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.esb.resource.test.proxy;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * Tests externalizing the startOnLoad attribute of proxy services with configurable properties.
 * ConfigurableStartProxyA uses ${configs.proxyStartA} and ConfigurableStartProxyB uses ${configs.proxyStartB}.
 */
public class ProxyWithConfigurableStartOnLoadTestCase extends ESBIntegrationTest {

    private static final String CAPP_NAME = "ProxyStartOnLoadTest_1.0.0";
    private static final String PROXY_A = "ConfigurableStartProxyA";
    private static final String PROXY_B = "ConfigurableStartProxyB";

    private ServerConfigurationManager serverConfigurationManager;
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
        serverConfigurationManager = new ServerConfigurationManager(context);
        File capp = new File(getESBResourceLocation() + File.separator + "config.var" + File.separator +
                CAPP_NAME + ".car");
        serverConfigurationManager.copyToCarbonapps(capp);
        assertTrue(Utils.checkForLog(carbonLogReader, "Successfully Deployed Carbon Application : " + CAPP_NAME,
                20), "Did not receive the expected info log");
    }

    @Test(groups = {"wso2.esb"}, description = "startOnLoad resolved from file.properties", priority = 1)
    public void testStartOnLoadWithFileProperty() throws Exception {
        // file.properties: proxyStartA=true, proxyStartB=false
        assertProxyStopped(PROXY_B);
        assertProxyStarted(PROXY_A);
        Assert.assertFalse(carbonLogReader.checkForLog(stoppedLog(PROXY_A), 1),
                PROXY_A + " should not be stopped");
    }

    // Restart commands accumulate across the suite, so this runs with the other --env-file tests (priority 3)
    // to avoid leaking the env file into the system property tests of the other configurable test cases.
    @Test(groups = {"wso2.esb"}, description = "startOnLoad resolved from environment variables", priority = 3)
    public void testStartOnLoadWithEnvVariable() throws Exception {
        // test.env: proxyStartA=false, proxyStartB=true, which overrides file.properties
        carbonLogReader.clearLogs();
        Map<String, String> commands = new HashMap<>();
        commands.put("--env-file", FrameworkPathUtil.getSystemResourceLocation() + "test.env");
        serverConfigurationManager.restartMicroIntegrator(commands);
        assertTrue(Utils.checkForLog(carbonLogReader, "Successfully Deployed Carbon Application : " + CAPP_NAME,
                60), "Carbon application was not redeployed after restart");
        assertProxyStopped(PROXY_A);
        assertProxyStarted(PROXY_B);
        Assert.assertFalse(carbonLogReader.checkForLog(stoppedLog(PROXY_B), 1),
                PROXY_B + " should not be stopped");
    }

    @Test(groups = {"wso2.esb"}, description = "Proxy deactivated via the management API stays inactive after a "
            + "restart even though startOnLoad resolves to true", priority = 4)
    public void testDeactivatedProxyStateIsPreservedOnRestart() throws Exception {
        // startOnLoad of ConfigurableStartProxyB resolves to true from test.env
        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String managementApiUrl = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset)
                + "/management/proxy-services";
        org.apache.http.HttpResponse deactivateResponse = client.doPost(managementApiUrl, headers,
                "{\"name\": \"" + PROXY_B + "\", \"status\": \"inactive\"}", "application/json");
        Assert.assertEquals(deactivateResponse.getStatusLine().getStatusCode(), 200);
        Assert.assertTrue(client.getResponsePayload(deactivateResponse).contains("stopped successfully"));

        carbonLogReader.clearLogs();
        serverConfigurationManager.restartMicroIntegrator();
        assertTrue(Utils.checkForLog(carbonLogReader, "Successfully Deployed Carbon Application : " + CAPP_NAME,
                60), "Carbon application was not redeployed after restart");

        Map<String, String> invokeHeaders = new HashMap<>();
        invokeHeaders.put("Content-Type", "application/json");
        org.apache.http.HttpResponse invokeResponse = client.doPost(getProxyServiceURLHttp(PROXY_B), invokeHeaders,
                "{}", "application/json");
        Assert.assertNotEquals(invokeResponse.getStatusLine().getStatusCode(), 200,
                PROXY_B + " should remain inactive after restart");
        assertTrue(carbonLogReader.checkForLog("attempting to access an inactive service: " + PROXY_B, 10),
                PROXY_B + " should remain inactive after restart");
    }

    private void assertProxyStopped(String proxyName) throws InterruptedException {
        assertTrue(carbonLogReader.checkForLog(stoppedLog(proxyName), 20),
                proxyName + " should be stopped as startOnLoad resolves to false");
    }

    private void assertProxyStarted(String proxyName) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpResponse httpResponse = HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp(proxyName)), "{}",
                headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200, proxyName + " should be started");
        String expectedProxy = PROXY_A.equals(proxyName) ? "A" : "B";
        Assert.assertEquals(StringUtils.normalizeSpace(httpResponse.getData()),
                StringUtils.normalizeSpace("{\"proxy\": \"" + expectedProxy + "\", \"status\": \"started\"}"));
    }

    private String stoppedLog(String proxyName) {
        return "ProxyService named '" + proxyName + "' has been stopped as startOnLoad parameter is set to false";
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
        serverConfigurationManager.removeFromCarbonapps(CAPP_NAME + ".car");
        super.cleanup();
    }
}
