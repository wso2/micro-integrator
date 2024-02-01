/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.getETag;
import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.sendDELETE;
import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.sendGET;
import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.sendPATCH;
import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.sendPOST;
import static org.wso2.ei.dataservice.integration.test.odata.ODataTestUtils.sendPUT;

/**
 * This class contains OData specific test cases. to verify the functionality of odata e-tags.
 */
public class ODataETagTestCase extends DSSIntegrationTest {
    private final String serviceName = "ODataETagSampleService";
    private final String configId = "default";
    private String webAppUrl;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        webAppUrl = dssContext.getContextUrls().getWebAppURL();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

    @Test(groups = "wso2.dss", description = "e tag retrieval test")
    public void validateETagRetrievalTestCase() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES";
        String content = "{\"FILENAME\": \"WSO2PROD\" ,\"TYPE\" : \"dss\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response1 = sendPOST(endpoint, content, headers);
        Assert.assertEquals(response1[0], ODataTestUtils.CREATED);
        endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2PROD')";
        Object[] response2 = sendGET(endpoint, headers);
        Assert.assertEquals(response2[0], ODataTestUtils.OK);
        String etag = getETag(response2[1].toString());
        headers.put("If-Match", "1212122");
        Object[] response3 = sendGET(endpoint, headers);
        Assert.assertEquals(response3[0], ODataTestUtils.PRE_CONDITION_FAILED);
        headers.remove("If-Match");
        headers.put("If-None-Match", etag);
        Object[] response4 = sendGET(endpoint, headers);
        Assert.assertEquals(response4[0], ODataTestUtils.PRE_CONDITION_FAILED);
        headers.remove("If-None-Match");
        headers.put("If-Match", etag);
        response4 = sendGET(endpoint, headers);
        Assert.assertEquals(response4[0], ODataTestUtils.OK);
    }

    @Test(groups = "wso2.dss", description = "etag generation test", dependsOnMethods = "validateETagRetrievalTestCase")
    public void validateETagGenerationTestCase() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES";
        String content = "{\"FILENAME\": \"WSO2\" ,\"TYPE\" : \"bam\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response1 = sendPOST(endpoint, content, headers);
        Assert.assertEquals(response1[0], ODataTestUtils.CREATED);
        endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2\')";
        Object[] response2 = sendGET(endpoint, headers);
        Assert.assertEquals(response2[0], ODataTestUtils.OK);
        String etag = getETag(response2[1].toString());
        endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2\')";
        content = "{\"TYPE\" : \"USJP\"}";
        int responseCode = sendPUT(endpoint, content, headers);
        Assert.assertEquals(responseCode, ODataTestUtils.NO_CONTENT);
        Object[] response3 = sendGET(endpoint, headers);
        Assert.assertEquals(response3[0], ODataTestUtils.OK);
        String tempETag = getETag(response3[1].toString());
        Assert.assertNotEquals(etag, tempETag);
    }

    @Test(groups = "wso2.dss", description = "etag concurrent handling with put method test", dependsOnMethods = "validateETagGenerationTestCase")
    public void validateETagConcurrentHandlingTestCaseForPutMethod() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2PROD\')";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response1 = sendGET(endpoint, headers);
        Assert.assertEquals(response1[0], ODataTestUtils.OK);
        String etag1 = getETag(response1[1].toString());
        //modifying data - E-Tag should be changed after processing the below request
        headers.put("If-Match", etag1);
        String content1 = "{\"TYPE\" : \"ml\"}";

        int responseCode1 = sendPUT(endpoint, content1, headers);
        Assert.assertEquals(responseCode1, ODataTestUtils.NO_CONTENT);
        // Data has been modified therefore E-Tag has been changed, Then If-None-Match should be worked with previous E-Tag
        headers.remove("If-Match");
        headers.put("If-None-Match", etag1);
        String content2 = "{\"TYPE\" : \"test\"}";
        int responseCode2 = sendPUT(endpoint, content2, headers);
        Assert.assertEquals(responseCode2, ODataTestUtils.NO_CONTENT);

        //testing concurrent test with put method
        // get the E-Tag
        Object[] response2 = sendGET(endpoint, headers);
        Assert.assertEquals(response2[0], ODataTestUtils.OK);
        String etag2 = getETag(response2[1].toString());
        String content3 = "{\"TYPE\" : \"SriLanka\"}";
        headers.remove("If-None-Match");
        ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content3, headers, endpoint);
        threadExecutor.run();
        Thread.sleep(1000);
        Object[] response3 = sendGET(endpoint, headers);
        Assert.assertEquals(response3[0], ODataTestUtils.OK);
        String tempETag = getETag(response3[1].toString());
        Assert.assertNotEquals(etag2, tempETag);
        headers.put("If-Match", etag2);
        String content4 = "{\"TYPE\" : \"MB\"}";
        int responseCode3 = sendPUT(endpoint, content4, headers);
        Assert.assertEquals(responseCode3, ODataTestUtils.PRE_CONDITION_FAILED);
        headers.put("If-Match", tempETag);
        Object[] response4 = sendGET(endpoint, headers);
        Assert.assertEquals(response4[0], ODataTestUtils.OK);
        // Data validation
        Assert.assertFalse(response4[1].toString().contains("MB"), "E-Tag with put method failed");
        Assert.assertTrue(response4[1].toString().contains("SriLanka"), "E-Tag with put method failed");

        //testing concurrent test with delete method
        // get the E-Tag
        Object[] response5 = sendGET(endpoint, headers);
        Assert.assertEquals(response5[0], ODataTestUtils.OK);
        headers.remove("If-Match");
        threadExecutor = new ODataRequestThreadExecutor("DELETE", null, headers, endpoint);
        threadExecutor.run();
        Thread.sleep(1000);
        headers.put("If-Match", etag2);
        String content5 = "{\"TYPE\" : \"MB\"}";
        int responseCode4 = sendPUT(endpoint, content5, headers);
        Assert.assertEquals(responseCode4, ODataTestUtils.NOT_FOUND);
    }

    @Test(groups = "wso2.dss", description = "etag concurrent handling with patch method test", dependsOnMethods = "validateETagConcurrentHandlingTestCaseForPutMethod")
    public void validateETagConcurrentHandlingTestCaseForPatchMethod() throws Exception {
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2\')";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response1 = sendGET(endpoint, headers);
        Assert.assertEquals(response1[0], ODataTestUtils.OK);
        String etag1 = getETag(response1[1].toString());
        //modifying data - E-Tag should be changed after processing the below request
        headers.put("If-Match", etag1);
        String content1 = "{\"TYPE\" : \"ml\"}";
        int responseCode1 = sendPATCH(endpoint, content1, headers);
        Assert.assertEquals(responseCode1, ODataTestUtils.NO_CONTENT);
        // Data has been modified therefore E-Tag has been changed, Then If-None-Match should be worked with previous E-Tag
        headers.remove("If-Match");
        headers.put("If-None-Match", etag1);
        String content2 = "{\"TYPE\" : \"test\"}";
        int responseCode2 = sendPATCH(endpoint, content2, headers);
        Assert.assertEquals(responseCode2, ODataTestUtils.NO_CONTENT);

        //testing concurrent test with put method
        // get the E-Tag
        Object[] response2 = sendGET(endpoint, headers);
        Assert.assertEquals(response2[0], ODataTestUtils.OK);
        String etag2 = getETag(response2[1].toString());
        String content3 = "{\"TYPE\" : \"SriLanka\"}";
        headers.remove("If-None-Match");
        ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content3, headers, endpoint);
        threadExecutor.run();
        Thread.sleep(1000);
        Object[] response3 = sendGET(endpoint, headers);
        Assert.assertEquals(response3[0], ODataTestUtils.OK);
        String tempETag = getETag(response3[1].toString());
        Assert.assertNotEquals(etag2, tempETag);
        headers.put("If-Match", etag2);
        String content4 = "{\"TYPE\" : \"MB\"}";
        int responseCode3 = sendPATCH(endpoint, content4, headers);
        Assert.assertEquals(responseCode3, ODataTestUtils.PRE_CONDITION_FAILED);
        headers.put("If-Match", tempETag);
        Object[] response4 = sendGET(endpoint, headers);
        Assert.assertEquals(response4[0], ODataTestUtils.OK);
        // Data validation
        Assert.assertFalse(response4[1].toString().contains("MB"), "E-Tag with put method failed");
        Assert.assertTrue(response4[1].toString().contains("SriLanka"), "E-Tag with put method failed");

        //testing concurrent test with delete method
        // get the E-Tag
        Object[] response5 = sendGET(endpoint, headers);
        Assert.assertEquals(response5[0], ODataTestUtils.OK);
        headers.remove("If-Match");
        threadExecutor = new ODataRequestThreadExecutor("DELETE", null, headers, endpoint);
        threadExecutor.run();
        Thread.sleep(1000);
        headers.put("If-Match", etag2);
        String content5 = "{\"TYPE\" : \"MB\"}";
        int responseCode4 = sendPATCH(endpoint, content5, headers);
        Assert.assertEquals(responseCode4, ODataTestUtils.NOT_FOUND);
    }

    @Test(groups = "wso2.dss", description = "etag concurrent handling with delete method test", dependsOnMethods = "validateETagConcurrentHandlingTestCaseForPatchMethod")
    public void validateETagConcurrentHandlingTestCaseForDeleteMethod() throws Exception {
        String endpoint1 = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES";
        String content1 = "{\"FILENAME\": \"WSO2PROD\" ,\"TYPE\" : \"dss\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response1 = sendPOST(endpoint1, content1, headers);
        Assert.assertEquals(response1[0], ODataTestUtils.CREATED);
        String endpoint2 = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2PROD\')";
        Object[] response2 = sendGET(endpoint2, headers);
        Assert.assertEquals(response2[0], ODataTestUtils.OK);
        String etag1 = ODataTestUtils.getETag(response2[1].toString());
        headers.put("If-None-Match", etag1);
        int responseCode1 = sendDELETE(endpoint2, headers);
        Assert.assertEquals(responseCode1, ODataTestUtils.PRE_CONDITION_FAILED);
        headers.remove("If-None-Match");
        headers.put("If-Match", etag1);
        int responseCode2 = sendDELETE(endpoint2, headers);
        Assert.assertEquals(responseCode2, ODataTestUtils.NO_CONTENT);
        int responseCode3 = sendDELETE(endpoint2, headers);
        Assert.assertEquals(responseCode3, ODataTestUtils.NOT_FOUND);

        // To insert values
        validateETagRetrievalTestCase();

        //testing concurrent test with put method
        // get the E-Tag
        Object[] response3 = sendGET(endpoint2, headers);
        Assert.assertEquals(response3[0], ODataTestUtils.OK);
        String etag2 = getETag(response3[1].toString());
        String content2 = "{\"TYPE\" : \"SriLanka\"}";
        headers.remove("If-Match");
        ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content2, headers, endpoint2);
        threadExecutor.run();
        Thread.sleep(1000);
        Object[] response4 = sendGET(endpoint2, headers);
        Assert.assertEquals(response4[0], ODataTestUtils.OK);
        String tempETag = getETag(response4[1].toString());
        Assert.assertNotEquals(etag2, tempETag);
        headers.put("If-Match", etag2);
        int responseCode4 = sendDELETE(endpoint2, headers);
        Assert.assertEquals(responseCode4, ODataTestUtils.PRE_CONDITION_FAILED);
        headers.put("If-Match", tempETag);
        int responseCode5 = sendDELETE(endpoint2, headers);
        Assert.assertEquals(responseCode5, ODataTestUtils.NO_CONTENT);
        int responseCode6 = sendDELETE(endpoint2, headers);
        Assert.assertEquals(responseCode6, 404);
    }

    @Test(groups = "wso2.dss", description = "property modification using etag concurrent handling with put method test", dependsOnMethods = "validateETagConcurrentHandlingTestCaseForDeleteMethod")
    public void validateETagConcurrentHandlingTestCaseForUpdatePropertyWithPutMethod() throws Exception {
        // To insert values
        String endpoint1 = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES";
        String content1 = "{\"FILENAME\": \"WSO2PROD\" ,\"TYPE\" : \"dss\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response1 = sendPOST(endpoint1, content1, headers);
        Assert.assertEquals(response1[0], ODataTestUtils.CREATED);
        String entityEndpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2PROD\')";
        Object[] response2 = sendGET(entityEndpoint, headers);
        Assert.assertEquals(response2[0], ODataTestUtils.OK);
        String etag1 = getETag(response2[1].toString());
        String endpoint2 = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2PROD\')/TYPE";
        String content2 = "{\"value\" : \"Jayasooriya\"}";
        headers.put("If-None-Match", etag1);
        int responseCode1 = sendPUT(endpoint2, content2, headers);
        Assert.assertEquals(responseCode1, ODataTestUtils.PRE_CONDITION_FAILED);
        headers.remove("If-None-Match");
        headers.put("If-Match", etag1);
        int responseCode2 = sendPUT(endpoint2, content2, headers);
        Assert.assertEquals(responseCode2, ODataTestUtils.NO_CONTENT);

        //testing concurrent test with put method
        // get the E-Tag
        headers.remove("If-Match");
        Object[] response3 = sendGET(entityEndpoint, headers);
        Assert.assertEquals(response3[0], ODataTestUtils.OK);
        String etag2 = getETag(response3[1].toString());
        String content3 = "{\"value\" : \"SriLanka\"}";
        ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content3, headers, endpoint2);
        threadExecutor.run();
        Thread.sleep(1000);
        Object[] response4 = sendGET(entityEndpoint, headers);
        Assert.assertEquals(response4[0], ODataTestUtils.OK);
        String tempETag = getETag(response4[1].toString());
        Assert.assertNotEquals(etag2, tempETag);
        headers.put("If-Match", etag2);
        String content4 = "{\"value\" : \"DSS Server\"}";
        int responseCode3 = sendPUT(endpoint2, content4, headers);
        Assert.assertEquals(responseCode3, ODataTestUtils.PRE_CONDITION_FAILED);
        // Data validation
        headers.remove("If-Match");
        Object[] response5 = sendGET(endpoint2, headers);
        Assert.assertEquals(response5[0], ODataTestUtils.OK);
        Assert.assertFalse(response5[1].toString().contains("DSS Server"), "E-Tag with put method failed");
        Assert.assertTrue(response5[1].toString().contains("SriLanka"), "E-Tag with put method failed");
        headers.put("If-Match", tempETag);
        int responseCode4 = sendPUT(endpoint2, content4, headers);
        Assert.assertEquals(responseCode4, ODataTestUtils.NO_CONTENT);

    }

    @Test(groups = "wso2.dss", description = "property modification using etag concurrent handling with patch method test", dependsOnMethods = "validateETagConcurrentHandlingTestCaseForUpdatePropertyWithPutMethod")
    public void validateETagConcurrentHandlingTestCaseForUpdatePropertyWithPatchMethod() throws Exception {
        String entityEndpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2PROD\')";
        String endpoint = webAppUrl + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2PROD\')/TYPE";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response1 = sendGET(entityEndpoint, headers);
        Assert.assertEquals(response1[0], ODataTestUtils.OK);
        String etag1 = getETag(response1[1].toString());
        String content1 = "{\"value\" : \"Jayasooriya\"}";
        headers.put("If-None-Match", etag1);
        int responseCode1 = sendPATCH(endpoint, content1, headers);
        Assert.assertEquals(responseCode1, ODataTestUtils.PRE_CONDITION_FAILED);
        headers.remove("If-None-Match");
        headers.put("If-Match", etag1);
        int responseCode2 = sendPATCH(endpoint, content1, headers);
        Assert.assertEquals(responseCode2, ODataTestUtils.NO_CONTENT);

        //testing concurrent test with put method
        // get the E-Tag
        headers.remove("If-Match");
        Object[] response2 = sendGET(entityEndpoint, headers);
        Assert.assertEquals(response2[0], ODataTestUtils.OK);
        String etag2 = getETag(response2[1].toString());
        String content2 = "{\"value\" : \"SriLanka\"}";
        ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content2, headers, endpoint);
        threadExecutor.run();
        Thread.sleep(1000);
        Object[] response3 = sendGET(entityEndpoint, headers);
        Assert.assertEquals(response3[0], ODataTestUtils.OK);
        String tempETag = getETag(response3[1].toString());
        Assert.assertNotEquals(etag2, tempETag);
        headers.put("If-Match", etag2);
        String content3 = "{\"value\" : \"DSS Server\"}";
        int responseCode3 = sendPATCH(endpoint, content3, headers);
        Assert.assertEquals(responseCode3, ODataTestUtils.PRE_CONDITION_FAILED);
        // Data validation
        headers.remove("If-Match");
        Object[] response4 = sendGET(entityEndpoint, headers);
        Assert.assertEquals(response4[0], ODataTestUtils.OK);
        Assert.assertFalse(response4[1].toString().contains("DSS Server"), "E-Tag with patch method failed");
        Assert.assertTrue(response4[1].toString().contains("SriLanka"), "E-Tag with patch method failed");
        headers.put("If-Match", tempETag);
        int responseCode4 = sendPATCH(endpoint, content3, headers);
        Assert.assertEquals(responseCode4, ODataTestUtils.NO_CONTENT);

    }
}
