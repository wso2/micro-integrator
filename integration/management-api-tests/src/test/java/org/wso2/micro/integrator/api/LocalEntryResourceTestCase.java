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
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class LocalEntryResourceTestCase extends ManagementAPITest {

    private static String resourcePath = "local-entries";

    @Test(groups = {"wso2.esb"}, description = "Test get Local Entries resource")
    public void retrieveLocalEntries() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath);
        /* (-2) to account for the 2 local entries defined by the server
         * entire count is 4. the count that should be returned should be 4-2 = 2
         */
        verifyResourceCount(jsonResponse, 2);
        verifyResourceInfo(jsonResponse, new String[]{"AbcLocalEntry", "HelloLocalEntry"});
    }

    @Test(groups = {"wso2.esb"}, description = "Test get Local Entries resource for search key")
    public void retrieveSearchedLocalEntries() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath.concat("?searchKey=Hello"));

        /* (-2) to account for the 2 local entries defined by the server
        * entire count is 1. the count that should be returned should be 1-2 = -1
        */
        verifyResourceCount(jsonResponse, -1);
        verifyResourceInfo(jsonResponse, new String[]{"HelloLocalEntry"});
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }
}
