/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.esb.mediators.enrich;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

/**
 * This test case is written to verify the fix for https://github.com/wso2/micro-integrator/issues/2944
 * This test case will verify the Enrich Mediator which contains double quotes in the inline expression
 */
public class EnrichDoubleQuoteTestCase extends ESBIntegrationTest {

    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    @BeforeClass()
    public void setEnvironment() throws Exception {

        super.init();
        checkApiExistence("EnrichDoubleQuoteAPI");
    }

    @Test(groups = "wso2.esb", description = "Test EnrichMediator issue with double quotes")
    public void testEnrichMediatorWithDoubleQuotes() throws Exception {

        String payload = "{\"test\": {\"value\": \"67E 262\"}}";
        String contentType = "application/json";
        HttpResponse response = httpClient.doPost(getApiInvocationURL("EnrichDoubleQuoteAPI"), null,
                payload, contentType);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        // Assert the JSON payloads
        String expectedPayload = "{\"issue\":\"Value with space in double quote\",\"value\":\"67E 262\"}";
        Assert.assertEquals(new Gson().fromJson(SimpleHttpClient.responseEntityBodyToString(response), Object.class),
                new Gson().fromJson(expectedPayload, Object.class));
    }

    @AfterClass()
    public void destroy() throws Exception {

        super.cleanup();
    }

}
