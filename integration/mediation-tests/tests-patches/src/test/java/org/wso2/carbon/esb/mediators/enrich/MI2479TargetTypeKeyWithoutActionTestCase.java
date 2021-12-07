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
 *
 */
package org.wso2.carbon.esb.mediators.enrich;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import static org.testng.Assert.assertTrue;

/**
 * This is to test when target type is set to 'key' and no action is specified in Enrich Mediator.
 * If no action is configured it should default to 'replace'
 * Public issue https://github.com/wso2/micro-integrator/issues/2479
 */
public class MI2479TargetTypeKeyWithoutActionTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Tests when target type is set to key and no action is specified")
    public void testEnrichTargetTypeKeyWithoutAction() throws Exception {

        String serviceUrl = getProxyServiceURLHttp("MI2479EnrichTargetTypeKeyWithoutActionProxy");
        String requestPayload = "{\n" +
                "    \"accountNo\": \"1001\",\n" +
                "    \"customerNo\": \"0001\"\n" +
                "}";
        Reader data = new StringReader(requestPayload);
        Writer writer = new StringWriter();
        String response = HttpURLConnectionClient
                .sendPostRequestAndReadResponse(data, new URL(serviceUrl), writer, "application/json");
        assertTrue(response.contains("\"avcId\":\"1001\""), "Did not receive expected response, " +
                "when target type is set to key " + "and no action is specified in Enrich Mediator.");
    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        super.cleanup();
    }
}
