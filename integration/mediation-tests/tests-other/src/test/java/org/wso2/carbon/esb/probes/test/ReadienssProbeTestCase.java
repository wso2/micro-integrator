/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.probes.test;

import org.apache.http.HttpResponse;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReadienssProbeTestCase extends ESBIntegrationTest {

    private static final String FAULTY_CAPP_NAME = "invalidCompositeApplication_1.0.0.car";
    private static final String READINESS_URL = "http://localhost:9391/healthz";

    private SimpleHttpClient client;
    private Map<String, String> headers;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        client = new SimpleHttpClient();
        headers = new HashMap<>();
        headers.put("Accept", "application/json");
    }

    @Test(groups = {"wso2.esb"}, description = "Test Readiness probe with a faulty CAPP")
    public void testReadinessWithFaultyCapps() throws Exception {

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).
                until(isManagementApiAvailable());

        HttpResponse response = client.doGet(READINESS_URL, headers);
        String responsePayload = client.getResponsePayload(response);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 500, "Readiness should fail due to faulty CAPPs");
        Assert.assertFalse(responsePayload.isEmpty(), "Readiness response should not be empty");
    }

    @Test(groups = {"wso2.esb"}, description = "Test Readiness probe without faulty CAPPs")
    public void testReadinessWithoutFaultyCapps() throws Exception {

        // undeploy the faulty CAPP and restart the server
        Utils.undeployCarbonApplication(FAULTY_CAPP_NAME, true);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).
                until(isManagementApiAvailable());

        HttpResponse response = client.doGet(READINESS_URL, headers);
        String responsePayload = client.getResponsePayload(response);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Readiness should be successful");
        Assert.assertFalse(responsePayload.isEmpty(), "Readiness response should not be empty");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        super.cleanup();
    }
}
