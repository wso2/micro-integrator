/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.HashMap;
import java.util.Map;

public class LivenessProbeTestCase extends ESBIntegrationTest {

    private static final String LIVENESS_URL = "http://localhost:9391/liveness";
    private final String expectedResponse = "{\"server\" : \"started\"}";

    private SimpleHttpClient client;
    private Map<String, String> headers;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        client = new SimpleHttpClient();
        headers = new HashMap<>();
        headers.put("Accept", "application/json");
    }

    @Test(groups = { "wso2.esb" },
            description = "Test Liveness probe")
    public void testLivenessProbe() throws Exception {

        HttpResponse response = client.doGet(LIVENESS_URL, headers);
        String responsePayload = client.getResponsePayload(response);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                "Liveness probe should return 200.");
        Assert.assertEquals(responsePayload, expectedResponse, "Liveness probe failed");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        super.cleanup();
    }
}
