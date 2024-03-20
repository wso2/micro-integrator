/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.esb.mediators.enrich;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests Enrich mediator behaviour when invalid JSON request is proceeded with a valid JSON request
 * Public issue https://github.com/wso2/micro-integrator/issues/3272
 */
public class EnrichInvalidAndValidJSONTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
    }

    @Test(groups = "wso2.esb", description = "Tests enrich mediator with invalid JSON proceeded with valid JSON")
    public void testEnrichInvalidAndValidJSON() throws Exception {

        URL serviceUrl = new URL(getProxyServiceURLHttp("EnrichInvalidJSONProxy"));
        String invalidPayload = "{\n" +
                "    \"minAmount\": [\n" +
                "        \"3000.00\"\n" +
                "    ],\n" +
                "    \"payPlan\": []\n" +
                "}";

        String validPayload = "{\n" +
                "    \"minAmount\": [\n" +
                "        \"3000.00\"\n" +
                "    ],\n" +
                "    \"payPlan\": [\n" +
                "        \"3\"\n" +
                "    ]\n" +
                "}";

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Send invalid JSON payload
        HttpResponse response = HttpRequestUtil.doPost(serviceUrl, invalidPayload, headers);
        Assert.assertEquals(response.getResponseCode(), 202, "Expected response code didn't match for invalid JSON payload");

        // Send valid JSON payload
        HttpResponse response2 = HttpRequestUtil.doPost(serviceUrl, validPayload, headers);
        Assert.assertEquals(response2.getResponseCode(), 200, "Expected response code didn't match for valid JSON payload");
    }
}
