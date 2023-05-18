//Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
//WSO2 Inc. licenses this file to you under the Apache License,
//Version 2.0 (the "License"); you may not use this file except
//in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,
//software distributed under the License is distributed on an
//"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//KIND, either express or implied.  See the License for the
//specific language governing permissions and limitations
//under the License.

package org.wso2.carbon.esb.json;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JsonPathTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb")
    public void jsonPathTest() throws Exception {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        String name1 = "jsonpath-test-service-v1";
        String name2 = "jsonpath-test-service-v2";
        String name3 = "jsonpath-test-service-v3";

        String payload1 = "{\"data\":[{\"name\":\"bar\",\"type\":\"A\"},{\"name\":\"foo\",\"type\":\"B\"}]}";
        String payload2 = "{\"result\":{\"finished\":true,\"status\":\"OK\"},\"errors\":[]}";
        String payload3 = "{\"data\":[{\"name\":\"bar\",\"type\":\"A\"},{\"name\":\"bar\",\"type\":\"C\"}]}";

        String url1 = getApiInvocationURL(name1);
        String url2 = getApiInvocationURL(name2);
        String url3 = getApiInvocationURL(name3);

        String response1 = sendPOST(url1, payload1, headers);
        String response2 = sendPOST(url2, payload2, headers);
        String response3 = sendPOST(url3, payload3, headers);

        Assert.assertEquals(response1, payload1, "The expected response is not received");
        Assert.assertEquals(response2, payload2, "The expected response is not received");
        Assert.assertEquals(response3, payload3, "The expected response is not received");
    }

    private String sendPOST(String endpoint, String content, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(endpoint);
        for (String headerType : headers.keySet()) {
            httpPost.setHeader(headerType, headers.get(headerType));
        }
        if (content != null) {
            HttpEntity httpEntity = new ByteArrayEntity(content.getBytes(StandardCharsets.UTF_8));
            if (headers.get("Content-Type") == null) {
                httpPost.setHeader("Content-Type", "application/json");
            }
            httpPost.setEntity(httpEntity);
        }
        HttpResponse httpResponse = httpClient.execute(httpPost);
        if (httpResponse.getEntity() != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            httpClient.close();
            return response.toString();
        } else {
            return "No entity found in response";
        }
    }
}
