/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.api;

import org.apache.http.HttpResponse;
import org.awaitility.Awaitility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.skyscreamer.jsonassert.JSONAssert;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import static org.wso2.micro.integrator.api.ManagementAPITest.LIST;

public class RegistryResourcesTestCase extends ESBIntegrationTest {

    private String accessToken;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"}, priority = 2, description = "Test adding registry content (text)")
    public void testRegistryPostText() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath
                + "&mediaType=testMediaType";
        File file = new File(TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP)
                + "/registry-resources/test-initial.txt");

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPostWithMultipart(endpoint + queryParameters, file, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully added the registry resource",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = {"wso2.esb"}, priority = 2, description = "Test adding registry content (text) for test delete")
    public void testRegistryPostTextExtraTextFile() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text-delete.txt";
        String queryParameters = "?path=" + registryPath
                + "&mediaType=testMediaType";
        File file = new File(TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP)
                + "/registry-resources/test-initial.txt");

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPostWithMultipart(endpoint + queryParameters, file, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully added the registry resource",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 2, description = "Test adding registry content (json)")
    public void testRegistryPostJson() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-json.json";
        String queryParameters = "?path=" + registryPath;
        File file = new File(TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP)
                + "/registry-resources/test-json.json");

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPostWithMultipart(endpoint + queryParameters, file, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully added the registry resource",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 2, description = "Test adding registry content (xml)")
    public void testRegistryPostXml() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/testSubFolder/test-xml.xml";
        String queryParameters = "?path=" + registryPath + "&mediaType=application/xml";
        String contentType = "application/xml";
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<endpoint name=\"RegResourceExampleEP\" xmlns=\"http://ws.apache.org/ns/synapse\">\n"
                + "    <address uri=\"http://localhost:9000/services/SimpleStockQuoteService\">\n"
                + "        <suspendOnFailure>\n"
                + "            <initialDuration>-1</initialDuration>\n"
                + "            <progressionFactor>1</progressionFactor>\n"
                + "        </suspendOnFailure>\n"
                + "        <markForSuspension>\n"
                + "            <retriesBeforeSuspension>5</retriesBeforeSuspension>\n"
                + "        </markForSuspension>\n"
                + "    </address>\n"
                + "</endpoint>";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPost(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully added the registry resource",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 2, description = "Test adding new properties with empty content file")
    public void testRegistryPostNewProperties() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath;
        String contentType = "application/json";
        String payload = "[\n"
                        + "    {\n"
                        + "        \"name\":\"prop-1\",\n"
                        + "        \"value\":\"val-1\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "        \"name\":\"prop-2\",\n"
                        + "        \"value\":\"val-2\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "        \"name\":\"prop-3\",\n"
                        + "        \"value\":\"val-3\"\n"
                        + "    }\n"
                        + "]\n";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPost(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully added the registry property",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry directory without expanding")
    public void testRegistryGetDirectory() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";
        String registryPath = "registry/config/testFolder";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\"count\":5,\n"
                + "\"list\":[\n"
                + "    {\"name\":\"test-json.json\",\"mediaType\":\"text/plain\",\"properties\":[]},\n"
                + "    {\"name\":\"test-text-delete.txt\",\"mediaType\":\"testMediaType\",\"properties\":[]},\n"
                + "    {\"name\":\"test-text.txt\",\"mediaType\":\"testMediaType\",\"properties\":[]},\n"
                + "    {\"name\":\"test-empty.txt\",\"mediaType\":\"text/plain\",\"properties\":[\n"
                + "        {\"name\":\"prop-3\",\"value\":\"val-3\"},\n"
                + "        {\"name\":\"prop-2\",\"value\":\"val-2\"},\n"
                + "        {\"name\":\"prop-1\",\"value\":\"val-1\"}]},\n"
                + "    {\"name\":\"testSubFolder\",\"mediaType\":\"directory\",\"properties\":[]}]}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry directory with expanding")
    public void testRegistryGetDirectoryExpand() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";
        String registryPath = "registry/config/testFolder";
        String queryParameters = "?path=" + registryPath + "&expand=true";
        String expected = "{\"name\":\"testFolder\",\n"
                + "\"files\":[\n"
                + "    {\"name\":\"test-json.json\",\"files\":[],\"type\":\"text/plain\"},\n"
                + "    {\"name\":\"test-text-delete.txt\",\"files\":[],\"type\":\"testMediaType\"},\n"
                + "    {\"name\":\"test-text.txt\",\"files\":[],\"type\":\"testMediaType\"},\n"
                + "    {\"name\":\"test-empty.txt\",\"files\":[],\"type\":\"text/plain\"},\n"
                + "    {\"name\":\"testSubFolder\",\"files\":[\n"
                + "        {\"name\":\"test-xml.xml\",\"files\":[],\"type\":\"application/xml\"}],\n"
                + "    \"type\":\"directory\"}],\n"
                + "\"type\":\"directory\"}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.get(LIST).toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry directory with expanding for searchKey")
    public void testRegistryGetNestedFileSearch() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";
        String registryPath = "registry";
        String queryParameters = "?path=" + registryPath + "&searchKey=test-text";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload).getJSONObject(LIST);
        Assert.assertTrue(jsonResponse.get("name").toString().contains("registry"));
        JSONArray filesArray = jsonResponse.getJSONArray("files");
        Assert.assertTrue(filesArray.getJSONObject(0).get("name").toString().contains("config"));
        Assert.assertTrue(filesArray.getJSONObject(0).getJSONArray("files").getJSONObject(0)
                .get("name").toString().contains("testFolder"));
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry metadata")
    public void testRegistryGetMetadata() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/metadata";
        String registryPath = "registry/config/testFolder/testSubFolder/test-xml.xml";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\"name\":\"test-xml.xml\",\"mediaType\":\"application/xml\"}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry content")
    public void testRegistryGetContent() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String expected = "Initial\n" + "content\n" + "of the\n" + "\n" + "test file\n" + "../12356";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response).replaceAll("\r\n", "\n");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        Assert.assertEquals(responsePayload, expected, "Invalid registry content received " + responsePayload);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry content from a .json file")
    public void testRegistryGetContentJson() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-json.json";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\n" + "    \"$schema\" : \"http://wso2.org/json-schema/wso2-data-mapper-v5.0.0/schema#\",\n"
                + "    \"inputType\" : \"CSV\",\n" + "    \"id\" : \"http://wso2jsonschema.org\",\n"
                + "    \"title\" : \"root\",\n" + "    \"type\" : \"array\",\n" + "    \"items\" : [ {\n"
                + "      \"properties\" : {\n" + "        \"Name\" : {\n"
                + "          \"id\" : \"http://wso2jsonschema.org/0/Name\",\n" + "          \"type\" : \"string\"\n"
                + "        },\n" + "        \"Age\" : {\n" + "          \"id\" : \"http://wso2jsonschema.org/0/Age\",\n"
                + "          \"type\" : \"string\"\n" + "        },\n" + "        \"Company\" : {\n"
                + "          \"id\" : \"http://wso2jsonschema.org/0/Company\",\n" + "          \"type\" : \"string\"\n"
                + "        }\n" + "      },\n" + "      \"id\" : \"http://wso2jsonschema.org/0\",\n"
                + "      \"type\" : \"object\"\n" + "    } ]\n" + "  }";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response).replaceAll("\r\n", "\n");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        Assert.assertEquals(responsePayload, expected, "Invalid registry content received " + responsePayload);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry content from a .xml file")
    public void testRegistryGetContentXml() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/testSubFolder/test-xml.xml";
        String queryParameters = "?path=" + registryPath;
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<endpoint name=\"RegResourceExampleEP\" xmlns=\"http://ws.apache.org/ns/synapse\">\n"
                + "    <address uri=\"http://localhost:9000/services/SimpleStockQuoteService\">\n"
                + "        <suspendOnFailure>\n"
                + "            <initialDuration>-1</initialDuration>\n"
                + "            <progressionFactor>1</progressionFactor>\n"
                + "        </suspendOnFailure>\n"
                + "        <markForSuspension>\n"
                + "            <retriesBeforeSuspension>5</retriesBeforeSuspension>\n"
                + "        </markForSuspension>\n"
                + "    </address>\n"
                + "</endpoint>";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response).replaceAll("\r\n", "\n");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        Assert.assertEquals(responsePayload, expected, "Invalid registry content received " + responsePayload);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry properties as a list")
    public void testRegistryGetPropertiesList() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\"count\":3,\n"
                + "\"list\":[\n"
                + "    {\"name\":\"prop-3\",\"value\":\"val-3\"},\n"
                + "    {\"name\":\"prop-2\",\"value\":\"val-2\"},\n"
                + "    {\"name\":\"prop-1\",\"value\":\"val-1\"}]}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test fetching registry property with a given name")
    public void testRegistryGetProperty() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath + "&name=prop-1";
        String expected = "{\"prop-1\":\"val-1\"}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test error - fetching non existing registry property")
    public void testRegistryGetPropertyError() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath + "&name=propNotFound";
        String expected = "{\"Error\":\"Property named propNotFound does not exist\"}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(jsonResponse.toString(), expected, false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test error - adding existing registry content")
    public void testRegistryPostContentErrorExistingRegistry() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\"Error\":\"Registry already exists. Can not POST an existing registry\"}";
        File file = new File(TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP)
                + "/registry-resources/test-initial.txt");

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPostWithMultipart(endpoint + queryParameters, file, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test error - deleting non existing registry property")
    public void testRegistryDeletePropertyErrorNonExistingProperty() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath + "&name=propNotFound";
        String expected = "{\"Error\":\"Property named propNotFound does not exist\"}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doDelete(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 3, description = "Test error - property name missing in the request")
    public void testRegistryDeletePropertyErrorPropertyNameMissing() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\"Error\":\"Property name not found in the request\"}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doDelete(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 4, description = "Test adding new properties to existing content file")
    public void testRegistryPostNewPropertiesToExistingContent() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String contentType = "application/json";
        String payload = "[\n"
                        + "    {\n"
                        + "        \"name\":\"prop-1\",\n"
                        + "        \"value\":\"val-1\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "        \"name\":\"prop-2\",\n"
                        + "        \"value\":\"val-2\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "        \"name\":\"prop-3\",\n"
                        + "        \"value\":\"val-3\"\n"
                        + "    }\n"
                        + "]\n";


        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPost(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully added the registry property",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 4, description = "Test modifying registry content")
    public void testRegistryPutText() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        File file = new File(TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP)
                + "/registry-resources/test-update.txt");

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPutWithMultipart(endpoint + queryParameters, file, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully modified the registry resource",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 4, description = "Test deleting registry content")
    public void testRegistryDelete() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text-delete.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doDelete(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully deleted the registry resource",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 4, description = "Test adding new and existing properties")
    public void testRegistryPostExistingProperties() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath;
        String contentType = "application/json";
        String payload = "[\n"
                + "    {\n"
                + "        \"name\":\"prop-1\",\n"
                + "        \"value\":\"updatedVal-1\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"name\":\"prop-5\",\n"
                + "        \"value\":\"val-5\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"name\":\"prop-6\",\n"
                + "        \"value\":\"val-6\"\n"
                + "    }\n"
                + "]";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPost(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("message"), "Successfully added the registry property",
                "Invalid response received " + jsonResponse.get("message"));
    }

    @Test(groups = { "wso2.esb" }, priority = 4, description = "Test deleting registry property")
    public void testRegistryDeleteProperty() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath + "&name=prop-3";
        String expected = "{\"message\":\"Successfully deleted the registry property\"}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doDelete(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 5, description = "Test fetching properties list to verify the changes")
    public void testRegistryGetPropertiesListVerifyEditedProperties() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\"count\":4,\n"
                + "\"list\":[\n"
                + "    {\"name\":\"prop-6\",\"value\":\"val-6\"},\n"
                + "    {\"name\":\"prop-5\",\"value\":\"val-5\"},\n"
                + "    {\"name\":\"prop-2\",\"value\":\"val-2\"},\n"
                + "    {\"name\":\"prop-1\",\"value\":\"updatedVal-1\"}]}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 5, description = "Test fetching properties list to verify new properties")
    public void testRegistryGetPropertiesListVerifyNewProperties() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String expected = "{\"count\":3,\n"
                + "\"list\":[\n"
                + "    {\"name\":\"prop-3\",\"value\":\"val-3\"},\n"
                + "    {\"name\":\"prop-2\",\"value\":\"val-2\"},\n"
                + "    {\"name\":\"prop-1\",\"value\":\"val-1\"}]}";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        JSONAssert.assertEquals(expected, jsonResponse.toString(), false);
    }

    @Test(groups = { "wso2.esb" }, priority = 5, description = "Test fetching registry content to verify changes")
    public void testRegistryGetContentToVerifyChanges() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String expected = "Updated\n" + "content\n" + "\n" + "of the\n" + "\n" + "test file\n" + "..../123";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response).replaceAll("\r\n", "\n");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        Assert.assertEquals(responsePayload, expected, "Invalid registry content received " + responsePayload);
    }

    @Test(groups = {
            "wso2.esb" }, priority = 5, description = "Test fetching registry directory with expanding to verify")
    public void testRegistryGetDirectoryExpandToVerifyChanges() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";
        String registryPath = "registry/config/testFolder";
        String queryParameters = "?path=" + registryPath + "&expand=true";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload).getJSONObject(LIST);
        Assert.assertTrue(jsonResponse.get("name").toString().contains("testFolder"));
        JSONArray filesArray = jsonResponse.getJSONArray("files");
        Assert.assertEquals(filesArray.length(), 4, "Assert Failed due to the mismatch of " +
                "actual vs expected resource count");
        for (int i = 0; i < filesArray.length(); i++) {
            log.info("printing fileArray element " + i + ": " + filesArray.getJSONObject(i).get("name").toString());
        }
        log.info("filesArray.getJSONObject(0).get(\"name\")::: " + filesArray.getJSONObject(0).get("name"));
        Assert.assertTrue(filesArray.getJSONObject(0).get("name").toString().contains("test-json.json"));
        Assert.assertTrue(filesArray.getJSONObject(1).get("name").toString().contains("test-text.txt"));
    }

    @Test(groups = { "wso2.esb" }, description = "Test error - deleting non existing registry")
    public void testRegistryDeleteContentErrorNonExistingRegistry() throws IOException {

        if (!isManagementApiAvailable) {
            Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .until(isManagementApiAvailable());
        }
        accessToken = TokenUtil.getAccessToken(hostName, portOffset);
        Assert.assertNotNull(accessToken);

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doDelete(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Registry does not exists in the path:"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - fetching directory from non existing path")
    public void testRegistryGetDirectoryErrorNonExistingPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";
        String registryPath = "registry/config/testFolder";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Invalid registry path"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1,
            description = "Test error - fetching expanded directory from non existing path")
    public void testRegistryGetDirectoryExpandErrorNonExistingPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";
        String registryPath = "registry/config/testFolder";
        String queryParameters = "?path=" + registryPath + "&expand=true";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Invalid registry path"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - illegal path")
    public void testRegistryGetDirectoryErrorIllegalPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";
        String registryPath = "registry/../repository";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error").toString(), "The registry path  'registry/../repository' "
                + "is illegal", "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - query parameter not found")
    public void testRegistryGetDirectoryErrorPathMissing() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint , getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error").toString(), "Registry path not found in the request",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - fetching from non existing registry")
    public void testRegistryGetMetadataErrorNonExistingRegistry() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/metadata";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Can not find the registry:"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - query parameter not found")
    public void testRegistryGetMetadataErrorPathMissing() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/metadata";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Registry path not found in the request"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - fetching from a illegal path")
    public void testRegistryGetMetadataErrorIllegalPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/metadata";
        String registryPath = "registry/../repository/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("The registry path  'registry/../repository/test-text.txt' is illegal"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - fetching from non existing registry")
    public void testRegistryGetContentErrorNonExistingRegistry() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Can not find the registry:"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - query parameter not found")
    public void testRegistryGetContentErrorPathMissing() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Registry path not found in the request"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - fetching from a illegal path")
    public void testRegistryGetContentErrorIllegalPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/../repository/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("The registry path  'registry/../repository/test-text.txt' is illegal"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - modifying non existing registry")
    public void testRegistryPutContentErrorNonExistingRegistry() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String contentType = "text/plain";
        String payload = "Updated\n" + "content\n" + "of the\n" + "\n" + "test file\n" + "../12356";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPut(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error"), "Registry does not exists in the path: registry/config/testFolder/test-text.txt",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1,
            description = "Test error - modifying registry artifacts outside default registry directories")
    public void testRegistryPutContentErrorOutsideRecommendedPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String contentType = "text/plain";
        String payload = "Updated\n" + "content\n" + "of the\n" + "\n" + "test file\n" + "../12356";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPut(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error"), "Registry does not exists in the path: registry/testFolder/test-text.txt",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1,
            description = "Test error - deleting registry artifacts outside default registry directories")
    public void testRegistryDeleteContentErrorOutsideRecommendedPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doDelete(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error"), "Registry does not exists in the path: registry/testFolder/test-text.txt",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - fetching from non existing registry")
    public void testRegistryGetPropertiesErrorNonExistingRegistry() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Can not find the registry:"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - query parameter not found")
    public void testRegistryGetPropertiesErrorPathMissing() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("Registry path not found"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - fetching from a illegal path")
    public void testRegistryGetPropertiesErrorIllegalPath() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/../repository/test-text.txt";
        String queryParameters = "?path=" + registryPath;

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertTrue(jsonResponse.get("Error").toString().contains("The registry path  'registry/../repository/test-text.txt' is illegal"),
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1,
            description = "Test error - deleting properties from non existing registry")
    public void testRegistryDeletePropertiesErrorNonExistingRegistry() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-empty.txt";
        String queryParameters = "?path=" + registryPath + "&name=prop-1";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doDelete(endpoint + queryParameters, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error"), "Cannot find a property file in the path: registry/config/testFolder/test-empty.txt",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = {
            "wso2.esb" }, priority = 1,
            description = "Test error - adding registry artifacts outside default registry directories")
    public void testRegistryPostContentErrorOutsideRecommendedDirectory() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/content";
        String registryPath = "registry/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath + "&mediaType=testMediaType";
        File file = new File(TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP)
                + "/registry-resources/test-initial.txt");

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPostWithMultipart(endpoint + queryParameters, file, getHeaderMap());
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error"), "Invalid registry path: registry/testFolder/test-text.txt",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1,
            description = "Test error - adding registry artifacts outside config, governance, local registry directories")
    public void testRegistryPostPropertiesErrorOutsideRecommendedDirectory() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String contentType = "application/json";
        String payload = "[\n"
                + "    {\n"
                + "        \"name\":\"prop-1\",\n"
                + "        \"value\":\"val-1\"\n"
                + "    }\n"
                + "]\n";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPost(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error"), "Invalid registry path: registry/testFolder/test-text.txt",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @Test(groups = { "wso2.esb" }, priority = 1, description = "Test error - Invalid payload for properties")
    public void testRegistryPostPropertiesErrorInvalidPayload() throws IOException {

        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/"
                + "registry-resources/properties";
        String registryPath = "registry/config/testFolder/test-text.txt";
        String queryParameters = "?path=" + registryPath;
        String contentType = "application/json";
        String payload = "[\n"
                + "    {\n"
                + "        \"property_name\":\"prop-1\",\n"
                + "        \"property_value\":\"val-1\"\n"
                + "    }\n"
                + "]\n";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doPost(endpoint + queryParameters, getHeaderMap(), payload, contentType);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 400, "Invalid response status " +
                response.getStatusLine().getStatusCode() + " returned.");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("Error"), "Invalid payload for properties",
                "Invalid error message received " + jsonResponse.get("Error").toString());
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }

    private Map<String, String> getHeaderMap() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer ".concat(accessToken));
        return headers;
    }
}
