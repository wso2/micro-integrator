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

public class ProxyServiceResourceTestCase extends ManagementAPITest {

    private static String resourcePath = "proxy-services";

    @Test(groups = {"wso2.esb"}, description = "Test get Proxy Service resource")
    public void retrieveProxyServices() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath);
        verifyResourceCount(jsonResponse, 3);
        verifyResourceInfo(jsonResponse, new String[]{"AbcProxyService", "HelloProxyService", "testProxy"});
    }

    @Test(groups = {"wso2.esb"}, description = "Test get Proxy Service resource for search key")
    public void retrieveSearchedProxyServices() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath.concat("?searchKey=HelloProxy"));
        verifyResourceCount(jsonResponse, 1);
        verifyResourceInfo(jsonResponse, new String[]{"HelloProxyService"});
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }

}
