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

package org.wso2.micro.integrator.transaction;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.micro.integrator.transaction.TransactionCounterUtils.getTransactionCount;
import static org.wso2.micro.integrator.transaction.TransactionCounterUtils.isTransactionCountUpdated;

/**
 * Tests to check the functionality of Transaction Counter with APIs Proxies & Inbound Endpoints
 */
public class TransactionCounterTest extends ESBIntegrationTest {
    private final String inputJson = "{\"Hello\" : \"World\"}";
    private final String inputXML = "<Hello>World</Hello>";
    private final Map<String, String> headers = new HashMap<String, String>();
    private String transactionCountResource;
    private String transactionReportResource;
    private final int REQUEST_COUNT = 100;
    private SimpleHttpClient client;
    private CarbonLogReader carbonLogReader;

    @BeforeClass
    void initialize() throws Exception {
        super.init();
        transactionCountResource = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) +
                "/management/transactions/count";
        transactionReportResource = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset) +
                "/management/transactions/report";
        client = new SimpleHttpClient();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Transaction Counter Tests for an API")
    void testAPITransactionCount() throws Exception {
        for (int i = 0; i < REQUEST_COUNT; i++) {
            String response = client.getResponsePayload(client.doPost(
                    getApiInvocationURL("echoAPI"), headers, inputJson, "application/json"));
        }
        int expectedRequestCount = REQUEST_COUNT;
        Assert.assertTrue(isTransactionCountUpdated(transactionCountResource, expectedRequestCount),
                String.format("Expected : %d, Actual : %d",
                        expectedRequestCount, getTransactionCount(transactionCountResource)));
    }

    @Test(dependsOnMethods = "testAPITransactionCount", groups = "wso2.esb",
            description = "Transaction Counter Tests for a Proxy Service")
    void testProxyTransactionCount() throws Exception {
        for (int i = 0; i < REQUEST_COUNT; i++) {
            String response = client.getResponsePayload(client.doPost(
                    getProxyServiceURLHttp("echoProxy"), headers, inputXML, "application/xml"));
        }
        int expectedRequestCount = REQUEST_COUNT*2;
        Assert.assertTrue(isTransactionCountUpdated(transactionCountResource, expectedRequestCount),
                String.format("Expected : %d, Actual : %d",
                        expectedRequestCount, getTransactionCount(transactionCountResource)));
    }

    @Test(dependsOnMethods = "testProxyTransactionCount", groups = "wso2.esb",
            description = "Transaction Counter Tests for an HTTP Inbound Endpoint")
    void testInboundEPTransactionCount() throws Exception {
        String inboundEPurl = "http://localhost:8083";
        for (int i = 0; i < REQUEST_COUNT; i++) {
            String response = client.getResponsePayload(client.doPost(
                    inboundEPurl, headers, inputJson, "application/json"));
        }
        int expectedRequestCount = REQUEST_COUNT*3;
        Assert.assertTrue(isTransactionCountUpdated(transactionCountResource, expectedRequestCount),
                String.format("Expected : %d, Actual : %d",
                        expectedRequestCount, getTransactionCount(transactionCountResource)));
    }

    @Test(dependsOnMethods = "testInboundEPTransactionCount", groups = "wso2.esb",
            description = "Transaction Counter Tests for Service Chaining scenario")
    void testServiceChainingTransactionCount() throws Exception {
        for (int i = 0; i < REQUEST_COUNT; i++) {
            String response = client.getResponsePayload(client.doPost(
                    getApiInvocationURL("base"), headers, inputJson, "application/json"));
        }
        int expectedRequestCount = REQUEST_COUNT*5;
        Assert.assertTrue(isTransactionCountUpdated(transactionCountResource, expectedRequestCount),
                String.format("Expected : %d, Actual : %d",
                        expectedRequestCount, getTransactionCount(transactionCountResource)));
    }

    @Test(groups = "wso2.esb", description = "Transaction Counter Report Generation Log")
    void testTransactionReportGeneration() throws Exception {
        carbonLogReader.clearLogs();
        client.doGet(transactionReportResource + "?end=2020-07&start=2020-05", headers);
        carbonLogReader.checkForLog("Transaction count report is created", DEFAULT_TIMEOUT);
    }

    @AfterClass
    void close() throws Exception {
        carbonLogReader.stop();
    }
}
