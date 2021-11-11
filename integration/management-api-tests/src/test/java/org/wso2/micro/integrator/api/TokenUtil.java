/*
 *Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
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
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.esb.integration.common.utils.ESBIntegrationTest.DEFAULT_INTERNAL_API_HTTPS_PORT;

/**
 * Util class used to take the access token for invoking management APIs.
 */
public class TokenUtil {

    /**
     * Get the access token from MI managemenet API.
     *
     * @param hostName hostname.
     * @param offset   port offset.
     * @return access token if succeed or null.
     * @throws IOException error occurred while getting the access token.
     */
    public static String getAccessToken(String hostName, int offset) throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic YWRtaW46YWRtaW4=");

        String endpoint =
                "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + offset) + "/management/login";

        SimpleHttpClient client = new SimpleHttpClient();
        HttpResponse response = client.doGet(endpoint, headers);
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        JSONObject jsonResponse = new JSONObject(responsePayload);
        if (jsonResponse.has("AccessToken")) {
            return jsonResponse.get("AccessToken").toString();
        }
        return null;
    }
}
