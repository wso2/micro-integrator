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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * This tests the JSON to XML conversion when JSON payload has unicode characters.
 */
public class TestJSONtoXMLwithUnicodeCharacters extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        verifyProxyServiceExistence("JSONtoXMLProxy");
    }

    @Test(groups = {"wso2.esb"}, description = "Test JSON to XML conversion when JSON has unicode characters")
    public void testJSONtoXMLwithUnicodeCharacters() throws IOException {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        String request = "{\n" +
                "    \"response\":\"\u0011\u0012Test With UTF-8 char at the beginning\"\n" +
                "  }";

        HttpResponse response = simpleHttpClient.doPost(getProxyServiceURLHttp("JSONtoXMLProxy"),
                null, request, "application/json");
        String responsePayload = simpleHttpClient.getResponsePayload(response);
        assertTrue(responsePayload.equals("<jsonObject><response>\\u0011\\u0012Test With UTF-8 char at the" +
                " beginning</response></jsonObject>"), "Error transforming JSON payload with unicode characters to XML.");
    }
}
