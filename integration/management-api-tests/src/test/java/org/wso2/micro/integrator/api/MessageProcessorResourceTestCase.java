/*
 *Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *WSO2 LLC. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.micro.integrator.api;

import org.apache.http.HttpResponse;
import org.awaitility.Awaitility;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MessageProcessorResourceTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = { "wso2.esb" }, description = "Test get Message Processors resource")
    public void retrieveMessageProcessors() throws IOException {
        
        String responsePayload = sendHttpRequestAndGetPayload(null);
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("count"), 3, "Assert Failed due to the mismatch of " +
                "actual vs expected resource count");
        Assert.assertTrue(jsonResponse.get("list").toString().contains("AbcMessageProcessor"));
        Assert.assertTrue(jsonResponse.get("list").toString().contains("HelloMessageProcessor"));
    }

    @Test(groups = { "wso2.esb" }, description = "Test get Message Processors resource for search key")
    public void retrieveSearchedMessageProcessors() throws IOException {

        String responsePayload = sendHttpRequestAndGetPayload("test");
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get("count"), 1, "Assert Failed due to the mismatch of " +
                "actual vs expected resource count");
        Assert.assertTrue(jsonResponse.get("list").toString().contains("testMessageProcessor"));
    }

    private String sendHttpRequestAndGetPayload(String searchKey) throws IOException {

        if (!isManagementApiAvailable) {
            Awaitility.await().pollInterval(100, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).
                    until(isManagementApiAvailable());
        }
        String accessToken = TokenUtil.getAccessToken(hostName, portOffset);
        Assert.assertNotNull(accessToken);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);
        String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/message-processors";
        if (searchKey != null) {
            endpoint = endpoint.concat("?searchKey=").concat(searchKey);
        }
        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint, headers);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        return responsePayload;
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }

}
