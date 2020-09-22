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

public class SourceTypeBodyTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        verifyProxyServiceExistence("dssCallMediatorSourceTypeBodyProxy");
    }

    @Test(groups = {"wso2.esb"}, description = "Test DSS Call mediator with single request when " +
            "source type is configured to body")
    public void testDSSCallMediatorSingleRequest() {
        String requestPayload = "<addEmployee>\n" +
                "    <EmployeeNumber>555</EmployeeNumber>\n" +
                "    <Firstname>Abby</Firstname>\n" +
                "    <LastName>Anderson</LastName>\n" +
                "    <Email>anderson@wso2.com</Email>\n" +
                "    <Salary>5000</Salary>\n" +
                "</addEmployee>\n";
        String responsePayload = sendPostRequest(requestPayload);
        assertTrue(responsePayload.contains("SUCCESSFUL"), "Error adding employee record.");
    }

    @Test(groups = {"wso2.esb"}, description = "Test DSS Call mediator with batch request when " +
            "source type is configured to body")
    public void testDSSCallMediatorBatchRequests() {
        String requestPayload = "<addEmployee_batch_req>\n" +
                "    <addEmployee>\n" +
                "        <EmployeeNumber>666</EmployeeNumber>\n" +
                "        <Firstname>Lev</Firstname>\n" +
                "        <LastName>Yara</LastName>\n" +
                "        <Email>yara@wso2.com</Email>\n" +
                "        <Salary>6000</Salary>\n" +
                "    </addEmployee>\n" +
                "    <addEmployee>\n" +
                "        <EmployeeNumber>777</EmployeeNumber>\n" +
                "        <Firstname>John</Firstname>\n" +
                "        <LastName>Matthew</LastName>\n" +
                "        <Email>matthew@wso2.com</Email>\n" +
                "        <Salary>7000</Salary>\n" +
                "    </addEmployee>\n" +
                "</addEmployee_batch_req>\n";
        String responsePayload = sendPostRequest(requestPayload);
        assertTrue(responsePayload.contains("SUCCESSFUL"), "Error adding employee batch record.");
    }

    @Test(groups = {"wso2.esb"}, description = "Test DSS Call mediator with request box when " +
            "source type is configured to body")
    public void testDSSCallMediatorRequestBox() {
        String requestPayload = "<request_box>\n" +
                "   <addEmployee>\n" +
                "      <EmployeeNumber>888</EmployeeNumber>\n" +
                "      <Firstname>William</Firstname>\n" +
                "      <LastName>John</LastName>\n" +
                "      <Email>william@wso2.com</Email>\n" +
                "      <Salary>8000</Salary>\n" +
                "   </addEmployee>\n" +
                "   <getEmployeeByNumber>\n" +
                "      <EmployeeNumber>888</EmployeeNumber>\n" +
                "   </getEmployeeByNumber>\n" +
                "</request_box>\n";
        String responsePayload = sendPostRequest(requestPayload);
        assertTrue(responsePayload.contains("William"), "Error performing request box operation. " +
                "First name William not found.");
    }

    @AfterClass
    public void cleanUp() throws Exception {
        super.cleanup();
    }

    private String sendPostRequest(String payload) {
        try {
            SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
            HttpResponse response = simpleHttpClient.doPost(getProxyServiceURLHttp("dssCallMediatorSourceTypeBodyProxy"), null, payload, "application/xml");
            return simpleHttpClient.getResponsePayload(response);
        } catch (IOException exp) {
            throw new SynapseException("Error performing POST request to service 'sourceTypeBodyProxy'", exp);
        }
    }
}
