/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediator.test.foreach;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

/**
 * Test foreach mediator with json payload.
 */
public class ForEachJSONPayloadTestCase extends ESBIntegrationTest {
    private CarbonLogReader carbonLogReader;
    private SimpleHttpClient simpleHttpClient;
    private Map<String, String> headers;

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        verifyProxyServiceExistence("foreachJSONTestProxy");
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
        headers = new HashMap<>();
        headers.put("Accept-Charset", "UTF-8");
    }

    @Test(groups = {"wso2.esb"}, description = "Test ForEach mediator with JSON payload")
    public void testForEachMediatorWithJSONPayload() throws Exception {
        String request = "{\"getQuote\":{\"request\":[{\"symbol\":\"IBM\"},{\"symbol\":\"WSO2\"},{\"symbol\":\"MSFT\"}]}}";

        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("foreachJSONTestProxy"), headers,
                request, "application/json;charset=UTF-8");

        //boolean reachedEnd = false;
        String logs = carbonLogReader.getSubstringBetweenStrings("<jsonObject>", "</jsonObject>", 6);
        assertTrue(logs.contains(
                "<checkPriceRequest xmlns=\"http://ws.apache.org/ns/synapse\"><code>IBM</code></checkPriceRequest>"),
                "IBM Element not found");
        assertTrue(logs.contains(
                "<checkPriceRequest xmlns=\"http://ws.apache.org/ns/synapse\"><code>WSO2</code></checkPriceRequest>"),
                "WSO2 Element not found");
        assertTrue(logs.contains(
                "<checkPriceRequest xmlns=\"http://ws.apache.org/ns/synapse\"><code>MSFT</code></checkPriceRequest>"),
                "MSTF Element not found");

        carbonLogReader.stop();
    }

}
