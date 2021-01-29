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
package org.wso2.ei.dataservice.integration.test.swagger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class will test the Swagger generation feature for data-services.
 */
public class SwaggerGenerationTestCase extends DSSIntegrationTest {
    ClassLoader classLoader = getClass().getClassLoader();

    private Map<String, String> requestHeader = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        requestHeader.put("Accept", "application/json");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        cleanup();
    }

    @Test(groups = {"wso2.dss"}, description = "Check swagger generation feature")
    public void swaggerDataServiceWithResourcesTestCase() throws Exception {

        String serviceEndPoint = "http://localhost:8480/services/ResourcesServiceTest?swagger.json";
        HttpResponse response = HttpRequestUtil.doGet(serviceEndPoint, requestHeader);
        String responseString = response.getData();
        assertNotNull(responseString, "Failed to get the swagger response.");
        JSONObject result = new JSONObject(responseString);
        assertNotNull(result, "Response is null");
        assertTrue(result.has("paths"), "Swagger information should be available on all paths");
        String pathDetails = result.get("paths").toString();
        JsonParser jsonParser = new JsonParser();
        JsonElement output = jsonParser.parse(pathDetails);
        InputStream inputStream = classLoader.getResourceAsStream("Swagger/JSONResponse.json");
        JsonElement expected = jsonParser.parse(IOUtils.toString(inputStream));
        Assert.assertEquals(output.toString(), expected.toString(), "Response mismatch");
    }

    @Test(groups = {"wso2.dss"}, description = "Check swagger generation for dataservice without resources")
    public void swaggerDataServiceWithoutResourcesTestCase() throws Exception {

        String serviceEndPoint = "http://localhost:8480/services/CSVDataService?swagger.json";
        HttpResponse response = HttpRequestUtil.doGet(serviceEndPoint, requestHeader);
        String responseString = response.getData();
        assertNotNull(responseString, "Failed to get the swagger response.");
        JSONObject result = new JSONObject(responseString);
        assertNotNull(result, "Response is null");
        assertTrue(result.has("paths"), "Path section of the swagger definition is missing");
        String pathDetails = result.get("paths").toString();
        Assert.assertEquals(pathDetails, "{}", "Should not contain resource path details");
    }

    @Test(groups = {"wso2.dss"}, description = "Get OpenApi definition in YAML format")
    public void swaggerDataServiceYamlTestCase() throws Exception {
        String serviceEndPoint = "http://localhost:8480/services/ResourcesServiceTest?swagger.yaml";
        HttpResponse response = HttpRequestUtil.doGet(serviceEndPoint, requestHeader);
        String responseString = response.getData();
        assertNotNull(responseString, "Failed to get the yaml response.");
        InputStream inputStream = classLoader.getResourceAsStream("Swagger/YAMLResponse.yaml");
        String expected = IOUtils.toString(inputStream);
        Assert.assertEquals(responseString, expected, "Response mismatch");
    }
}
