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

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.IOException;

import static org.wso2.micro.integrator.api.Constants.COUNT;
import static org.wso2.micro.integrator.api.Constants.LIST;

public class SequenceResourceTestCase extends ESBIntegrationTest {

    private String accessToken;
    private String endpoint;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        accessToken = TokenUtil.getAccessToken(hostName, portOffset);
        endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) + "/management/sequences";
    }

    @Test(groups = {"wso2.esb"}, description = "Test get Sequences resource")
    public void retrieveSequences() throws IOException {

        String responsePayload = sendHttpRequestAndGetPayload(endpoint, accessToken);
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get(COUNT), 5, "Assert Failed due to the mismatch of " +
                "actual vs expected resource count");
        Assert.assertTrue(jsonResponse.get(LIST).toString().contains("AbcSequence"), "Assert failed " +
                "since expected resource name not found in the list");
        Assert.assertTrue(jsonResponse.get(LIST).toString().contains("fault"), "Assert failed " +
                "since expected resource name not found in the list");
        Assert.assertTrue(jsonResponse.get(LIST).toString().contains("main"), "Assert failed " +
                "since expected resource name not found in the list");
    }

    @Test(groups = {"wso2.esb"}, description = "Test get Sequences resource for search key")
    public void retrieveSearchedSequences() throws IOException {
        String responsePayload = sendHttpRequestAndGetPayload(endpoint.concat("?searchKey=ABC"), accessToken);
        JSONObject jsonResponse = new JSONObject(responsePayload);
        Assert.assertEquals(jsonResponse.get(COUNT), 1, "Assert Failed due to the mismatch of " +
                "actual vs expected resource count");
        Assert.assertTrue(jsonResponse.get(LIST).toString().contains("AbcSequence"), "Assert failed " +
                "since expected resource name not found in the list");
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }
}
