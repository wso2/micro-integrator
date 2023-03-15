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
import org.testng.annotations.Test;

import java.io.IOException;


public class CarbonAppResourceTestCase extends ManagementAPITest {

    private static String resourcePath = "applications";
    private final static String ACTIVE_LIST = "activeList";
    private final static String FAULTY_LIST = "faultyList";
    private final static String TOTAL_COUNT = "totalCount";

    @Test(groups = { "wso2.esb" }, description = "Test get carbon applications resource")
    public void retrieveCApps() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath);
        verifyTotalResourceCount(jsonResponse, 4);
        verifyFaultyResourceInfo(jsonResponse, new String[]{"FaultyCAppCompositeExporter"});
        verifyActiveResourceInfo(jsonResponse, new String[]{"hello-worldCompositeExporter"});
    }

    @Test(groups = { "wso2.esb" }, description = "Test get carbon applications resource for search key")
    public void retrieveSearchedCApps() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath.concat("?searchKey=FaultyCApp"));
        verifyTotalResourceCount(jsonResponse, 1);
        verifyFaultyResourceInfo(jsonResponse, new String[]{"FaultyCAppCompositeExporter"});
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }

    protected void verifyTotalResourceCount(JSONObject jsonResponse, int expectedCount) {
        Assert.assertEquals(jsonResponse.get(TOTAL_COUNT), expectedCount, "Assert Failed due to the mismatch of " +
                "actual vs expected resource count");
    }

    protected void verifyActiveResourceInfo(JSONObject jsonResponse, String[] expectedResourceNames) {
        for (String expectedResourceName : expectedResourceNames) {
            Assert.assertTrue(jsonResponse.get(ACTIVE_LIST).toString().contains(expectedResourceName),
                    "Assert failed since expected resource name not found in the list");
        }
    }

    protected void verifyFaultyResourceInfo(JSONObject jsonResponse, String[] expectedResourceNames) {
        for (String expectedResourceName : expectedResourceNames) {
            Assert.assertTrue(jsonResponse.get(FAULTY_LIST).toString().contains(expectedResourceName),
                    "Assert failed since expected resource name not found in the list");
        }
    }
}
