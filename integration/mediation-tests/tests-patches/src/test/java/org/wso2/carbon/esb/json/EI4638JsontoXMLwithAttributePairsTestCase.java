/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.json;

import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import static org.testng.Assert.assertEquals;

/**
 * This tests JSON to XML conversion when JSON payload has attribute pairs after key value pairs.
 */
public class EI4638JsontoXMLwithAttributePairsTestCase extends ESBIntegrationTest {

    private SimpleHttpClient httpClient;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        httpClient = new SimpleHttpClient();
    }

    @Test(groups = {"wso2.esb"}, description = "Test JSON to XML conversion when JSON payload has " +
            "attribute pairs after key value pairs")
    public void testJsontoXMLwithAttributePairs() throws Exception {
        String serviceUrl = getProxyServiceURLHttp("JSONtoXMLProxy");
        String requestPayload = "{\"@type\": \"string\", \"content\": \"data\", \"@source\": \"user\"}";
        String expectedResponse = "<jsonObject type=\"string\" source=\"user\"><content>data</content></jsonObject>";
        HttpResponse response = httpClient.doPost(serviceUrl, null, requestPayload,"application/json");
        assertEquals(httpClient.getResponsePayload(response), expectedResponse, "Error in JSON to XML conversion with attribute pairs");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
