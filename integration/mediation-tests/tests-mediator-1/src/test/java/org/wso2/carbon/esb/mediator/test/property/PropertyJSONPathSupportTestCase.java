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

package org.wso2.carbon.esb.mediator.test.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * This class will test the jsonPath evaluation against property values
 */
public class PropertyJSONPathSupportTestCase extends ESBIntegrationTest {

    private JsonParser parser;
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        parser = new JsonParser();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = {"wso2.esb"}, description = "Test JSONPath against properties")
    public void testPropertyJSONPathSupport() throws IOException, AutomationFrameworkException, InterruptedException {

        String input = "{ \"a\" : 5 }";
        String expectedOutput = "{\n" +
                "    \"bla\": [\n" +
                "        {\n" +
                "            \"category\": \"fiction\",\n" +
                "            \"author\": \"J. R. R. Tolkien\",\n" +
                "            \"title\": \"The Lord of the Rings\",\n" +
                "            \"isbn\": \"0-395-19395-8\",\n" +
                "            \"price\": 22.99\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        URL endpoint = new URL(getApiInvocationURL("testJsonPath"));

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        carbonLogReader.clearLogs();
        HttpResponse httpResponse = HttpRequestUtil.doPost(endpoint, input, header);
        assertEqualJsonObjects(httpResponse.getData(), expectedOutput);
        Assert.assertTrue(carbonLogReader.checkForLog(
                "bla3 = {\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99}",
                DEFAULT_TIMEOUT));
    }

    private void assertEqualJsonObjects(String json1, String json2) {

        JsonElement element1 = parser.parse(json1);
        JsonElement element2 = parser.parse(json2);
        assertEquals(element1, element2, "Didn't receive the expected output");
    }
}
