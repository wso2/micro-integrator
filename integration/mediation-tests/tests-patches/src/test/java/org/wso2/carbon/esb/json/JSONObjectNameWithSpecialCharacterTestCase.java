/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.esb.json;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

/**
 * This test case is written to verify the fix for https://github.com/wso2/api-manager/issues/2082
 * This test case will verify the JSON object name with special characters
 */
public class JSONObjectNameWithSpecialCharacterTestCase extends ESBIntegrationTest {

    private final SimpleHttpClient httpClient = new SimpleHttpClient();
    private final String expectedPayload = "{\n" +
            "    \"id\": \"001\",\n" +
            "    \"special_data\": {\n" +
            "        \"@odata.nextLink\": \"sample_url\",\n" +
            "        \"value\": [\n" +
            "            {\n" +
            "                \"id\": \"sample_id\",\n" +
            "                \"@removed\": {\n" +
            "                    \"reason\": \"sample_reason\"\n" +
            "                }\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    @BeforeClass()
    public void setEnvironment() throws Exception {

        super.init();
        checkApiExistence("JsonObjectNameSpecialCharacter");
    }

    @Test(groups = "wso2.esb", description = "Test JSONObject name with special character")
    public void testJSONObjectNameWithSpecialCharacter() throws Exception {

        HttpResponse response = httpClient.doGet(getApiInvocationURL("JsonObjectNameSpecialCharacter"), null);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        // Assert the JSON payloads
        Assert.assertEquals(new Gson().fromJson(SimpleHttpClient.responseEntityBodyToString(response), Object.class),
                new Gson().fromJson(expectedPayload, Object.class));
    }

    @AfterClass()
    public void destroy() throws Exception {

        super.cleanup();
    }
}
