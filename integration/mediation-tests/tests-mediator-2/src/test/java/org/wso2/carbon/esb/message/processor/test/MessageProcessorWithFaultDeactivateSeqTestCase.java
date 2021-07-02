/*
 *Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.esb.message.processor.test;

import org.apache.http.HttpResponse;
import org.apache.synapse.SynapseException;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MessageProcessorWithFaultDeactivateSeqTestCase extends ESBIntegrationTest {

    private final String PROCESSOR_NAME = "mspDeactivateIfDeactivateSeqFailsMessageProcessor";
    private final String PROXY_NAME = "mspDeactivateIfDeactivateSeqFailsProxy";

    private String proxyURL;

    @BeforeClass(alwaysRun = true, description = "Test Car with Mediator deployment")
    protected void setup() throws Exception {
        super.init();
        proxyURL = getProxyServiceURLHttp(PROXY_NAME);
    }

    /**
     * Verify de-activation of Message Processor when exception is thrown inside deactivate sequence.
     *
     * @throws Exception
     */
    @Test(groups = {"wso2.esb"}, description = "Test de-activation of Message Processor when exception is thrown " +
            "inside deactivate sequence.")
    public void testMPGettingDeactivatedIfDeactivateSeqFails() throws Exception {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doGet(proxyURL, null);
        Thread.sleep(10000);
        checkMessageProcessorState();
    }

    private void checkMessageProcessorState() {
        if (!isManagementApiAvailable) {
            Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).
                    until(isManagementApiAvailable());
        }
        try {
            SimpleHttpClient client = new SimpleHttpClient();
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");

            String endpoint = "https://" + hostName + ":" + (DEFAULT_INTERNAL_API_HTTPS_PORT + portOffset)
                    + "/management/message-processors?name=" + PROCESSOR_NAME;

            HttpResponse response = client.doGet(endpoint, headers);
            Assert.assertTrue(client.getResponsePayload(response).contains("\"status\":\"inactive\""),
                    "Message processor should be inactive when exception is thrown inside deactivate sequence.");
        } catch (IOException e) {
            throw new SynapseException("Error retrieving details of the message processor " + PROCESSOR_NAME
                    + " using Management API", e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanState() throws Exception {
        super.cleanup();
    }
}
