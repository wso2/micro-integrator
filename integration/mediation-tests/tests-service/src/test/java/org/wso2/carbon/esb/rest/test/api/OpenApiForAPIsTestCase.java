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
package org.wso2.carbon.esb.rest.test.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;


/**
 * This class will test the OAS 3.0 support in synapse APIs.
 */
public class OpenApiForAPIsTestCase  extends ESBIntegrationTest {

    private Map<String, String> requestHeader = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        requestHeader.put("Accept", "application/json");
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Get the API definition in JSON format")
    public void getOASJsonTest() throws Exception {
        String restURL = "http://localhost:8480/SwaggerPetstore:v1.0.5?swagger.json";
        HttpResponse response = HttpRequestUtil.doGet(restURL, requestHeader);
        String responseString = response.getData();
        assertNotNull(responseString, "Failed to get the swagger response.");
        ClassLoader classLoader = getClass().getClassLoader();
        // Reading the expected payload from a file since its too large.
        InputStream inputStream = classLoader.getResourceAsStream("Responses/SwaggerResponse.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement output = jsonParser.parse(responseString);
        JsonElement expected = jsonParser.parse(IOUtils.toString(inputStream));
        Assert.assertEquals(output.toString(), expected.toString(), "Response mismatch");
    }

    @Test(groups = "wso2.esb", description = "Get the API definition in YAML format")
    public void getOASYamlTest() throws Exception {
        String restURL = "http://localhost:8480/SwaggerPetstore:v1.0.5?swagger.yaml";
        HttpResponse response = HttpRequestUtil.doGet(restURL, requestHeader);
        String responseString = response.getData();
        assertNotNull(responseString, "Failed to get the swagger response.");
        ClassLoader classLoader = getClass().getClassLoader();
        // Reading the expected payload from a file since its too large.
        InputStream inputStream = classLoader.getResourceAsStream("Responses/SwaggerResponse.yaml");
        String fileContent = IOUtils.toString(inputStream);
        Assert.assertEquals(responseString, fileContent, "Response mismatch");
    }
}
