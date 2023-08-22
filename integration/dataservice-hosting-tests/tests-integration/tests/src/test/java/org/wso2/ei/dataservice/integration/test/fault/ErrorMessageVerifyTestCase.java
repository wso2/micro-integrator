/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ei.dataservice.integration.test.fault;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertTrue;

/**
 * Test case to verify the element ordering of a DSS soap fault.
 */
public class ErrorMessageVerifyTestCase extends DSSIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        cleanup();
    }

    @Test(groups = {"wso2.dss"}, description = "Check element ordering of a DSS soap fault")
    public void verifyDSSErrorMessageTestCase() throws Exception {
        String serviceEndPoint = "http://localhost:8480/services/ResourcesServiceTest/product";

        // cannot use HttpRequestUtil.doPOST() because it fails when response is 500
        URL urlObj = new URL(serviceEndPoint);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/xml");
        connection.setRequestProperty("Accept", "application/xml");

        byte[] postDataBytes = "<a>invalid</a>".getBytes(StandardCharsets.UTF_8);
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(postDataBytes);
        }
        int responseCode = connection.getResponseCode();
        Assert.assertEquals(responseCode, 500, "Response code mismatched");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            int currentParamsIndex = response.indexOf("<axis2ns1:current_params>");
            int requestNameIndex = response.indexOf("<axis2ns1:current_request_name>");
            int nestedExceptionIndex = response.indexOf("<axis2ns1:nested_exception");
            int dataServiceIndex = response.indexOf("<axis2ns1:source_data_service>");
            int locationIndex = response.indexOf("<axis2ns1:location>");
            int namespaceIndex = response.indexOf("<axis2ns1:default_namespace>");
            int descriptionIndex = response.indexOf("<axis2ns1:description>");
            int serviceNameIndex = response.indexOf("<axis2ns1:data_service_name>");
            int dsCodeIndex = response.indexOf("<axis2ns1:ds_code>");
            assertTrue(currentParamsIndex < requestNameIndex,
                    "current_params should be before current_request_name");
            assertTrue(requestNameIndex < nestedExceptionIndex,
                    "current_request_name should be before nested_exception");
            assertTrue(nestedExceptionIndex < dataServiceIndex,
                    "nested_exception should be before source_data_service");
            assertTrue(dataServiceIndex < locationIndex,
                    "source_data_service should be before location");
            assertTrue(locationIndex < namespaceIndex,
                    "location should be before default_namespace");
            assertTrue(namespaceIndex < descriptionIndex,
                    "default_namespace should be before description");
            assertTrue(descriptionIndex < serviceNameIndex,
                    "description should be before data_service_name");
            assertTrue(serviceNameIndex < dsCodeIndex,
                    "data_service_name should be before ds_code");
        }
    }
}
