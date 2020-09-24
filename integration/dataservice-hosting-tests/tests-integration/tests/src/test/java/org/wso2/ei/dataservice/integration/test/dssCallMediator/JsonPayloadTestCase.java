/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ei.dataservice.integration.test.dssCallMediator;

import org.apache.http.HttpResponse;
import org.apache.synapse.SynapseException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class JsonPayloadTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        verifyProxyServiceExistence("dssCallMediatorSourceTypeBodyProxy");
    }

    @Test(groups = {"wso2.esb"}, description = "Test DSS Call mediator with json single request")
    public void testDSSCallMediatorWithJsonSingleRequest() {
        String payload = "{\n" +
                "  \"addEmployee\": {\n" +
                "    \"EmployeeNumber\": \"123\",\n" +
                "    \"Firstname\": \"Martin\",\n" +
                "    \"LastName\": \"Craig\",\n" +
                "    \"Email\": \"craig@wso2.com\",\n" +
                "    \"Salary\": \"1000\"\n" +
                "  }\n" +
                "}";
        String response = sendPostRequest("dssCallMediatorSourceTypeBodyProxy", payload);
        assertTrue(response.contains("SUCCESSFUL"), "Error adding employee record.");
    }

    @Test(groups = {"wso2.esb"}, description = "Test DSS Call mediator with json batch request")
    public void testDSSCallMediatorWithJsonBatchRequest() {
        String payload = "{\n" +
                "  \"addEmployee_batch_req\": {\n" +
                "    \"addEmployee\": [\n" +
                "      {\n" +
                "        \"EmployeeNumber\": \"345\",\n" +
                "        \"Firstname\": \"Miles\",\n" +
                "        \"LastName\": \"Morale\",\n" +
                "        \"Email\": \"morale@wso2.com\",\n" +
                "        \"Salary\": \"3000\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"EmployeeNumber\": \"567\",\n" +
                "        \"Firstname\": \"Mel\",\n" +
                "        \"LastName\": \"Jesse\",\n" +
                "        \"Email\": \"jesse@wso2.com\",\n" +
                "        \"Salary\": \"2000\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        String response = sendPostRequest("dssCallMediatorSourceTypeBodyProxy", payload);
        assertTrue(response.contains("SUCCESSFUL"), "Error adding employee batch record.");
    }

    @Test(groups = {"wso2.esb"}, description = "Test DSS Call mediator with json request box")
    public void testDSSCallMediatorWithJsonRequestBox() {
        String payload = "{\n" +
                "  \"request_box\": {\n" +
                "    \"addEmployee\": {\n" +
                "      \"EmployeeNumber\": \"789\",\n" +
                "      \"Firstname\": \"Geralt\",\n" +
                "      \"LastName\": \"Tris\",\n" +
                "      \"Email\": \"tris@wso2.com\",\n" +
                "      \"Salary\": \"2500\"\n" +
                "    },\n" +
                "    \"getEmployeeByNumber\": { \"EmployeeNumber\": \"789\" }\n" +
                "  }\n" +
                "}";
        String response = sendPostRequest("dssCallMediatorSourceTypeBodyProxy", payload);
        assertTrue(response.contains("Tris"), "Last name 'Tris' not found. Error performing request " +
                "box operation.");
    }

    @AfterClass
    public void cleanUp() throws Exception {
        super.cleanup();
    }

    private String sendPostRequest(String serviceName, String payload) {
        try {
            SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
            HttpResponse response = simpleHttpClient.doPost(getProxyServiceURLHttp(serviceName), null, payload, "application/json");
            return simpleHttpClient.getResponsePayload(response);
        } catch (IOException exp) {
            throw new SynapseException("Error performing POST request to service " + serviceName, exp);
        }
    }
}
