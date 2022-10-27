/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ei.dataservice.integration.test.odata;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.sendGET;
import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.sendPUT;

/**
 * This class contains OData specific test cases. to verify the functionality of entity streaming.
 */
public class ODataEntityStreamingTestCase extends DSSIntegrationTest {
    private final String serviceName = "ODataBatchRequestSampleService";
    private final String configId = "default";
    private String webAppUrl;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        webAppUrl = dssContext.getContextUrls().getWebAppURL();
    }

    @Test(groups = "wso2.dss",
            description = "Read a single entity with JSON content type")
    public void validateJsonResponse() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS(103)?";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response = sendGET(endpoint, headers);
        Assert.assertEquals(response[0], ODataTestUtils.OK);
        Assert.assertTrue(isValidJson(response[1].toString()));
    }

    @Test(groups = "wso2.dss",
            description = "Read a single entity with XML content type")
    public void validateXmlResponse() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS(103)?";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/xml");
        Object[] response = sendGET(endpoint, headers);
        Assert.assertEquals(response[0], ODataTestUtils.OK);
        Assert.assertTrue(isValidXml(response[1].toString()));
    }

    @Test(groups = "wso2.dss",
            description = "Read entity count")
    public void validateCountQuery() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS/$count";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response = sendGET(endpoint, headers);
        Assert.assertEquals(response[0], ODataTestUtils.OK);
        Assert.assertTrue(response[1].toString().contains("122"));
    }

    @Test(groups = "wso2.dss",
            description = "Read a non-existing table test")
    public void validateTableNotFoundResponse() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/CUSTOMER(103)?";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response = sendGET(endpoint, headers);
        Assert.assertEquals(response[0], ODataTestUtils.NOT_FOUND);
    }

    @Test(groups = "wso2.dss",
            description = "Read an non-existing entity test")
    public void validateEntityNotFoundResponse() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS(150)?";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response = sendGET(endpoint, headers);
        Assert.assertEquals(response[0], ODataTestUtils.NOT_FOUND);
    }

    /**
     * Check for invalid JSON content.
     *
     * @param json JSON content in string format
     * @return true if the content is a valid JSON. Otherwise, return false.
     */
    public boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    /**
     * Check for invalid XML content.
     *
     * @param xml XML content in string format
     * @return true if the content is a valid XML. Otherwise, return false.
     */
    public boolean isValidXml(String xml) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            dBuilder.parse(is);
            return true;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return false;
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }
}
