/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.api;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class DataSourceResourceTestCase extends ManagementAPITest {

    private static String resourcePath = "data-sources";

    /**
     * This test case verifies if datasource information is retrieved successfully.
     *
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, description = "Test get data source info")
    public void retrieveDataSourceInfo() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath.concat("?name=").concat("MySQLConnection2"));
        String datasourceType = jsonResponse.get("type").toString();
        Assert.assertEquals(datasourceType, "RDBMS");
    }

    @Test(groups = { "wso2.esb"}, description = "Test get data-source resource for search key")
    public void retrieveSearchedDataSources() throws IOException {
        JSONObject jsonResponse = sendHttpRequestAndGetPayload(resourcePath.concat("?searchKey=MYSQL"));
        verifyResourceCount(jsonResponse, 1);
        verifyResourceInfo(jsonResponse, new String[]{"MySQLConnection2"});
    }
    
    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }
}
