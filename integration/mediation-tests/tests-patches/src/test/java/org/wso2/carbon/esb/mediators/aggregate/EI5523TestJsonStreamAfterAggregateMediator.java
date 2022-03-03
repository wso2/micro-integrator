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
package org.wso2.carbon.esb.mediators.aggregate;

import org.testng.Assert;
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

/**
 * This is to test the JSON Stream after Aggregate Mediator.
 * Public issue https://github.com/wso2/product-ei/issues/5523
 */
public class EI5523TestJsonStreamAfterAggregateMediator extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Tests JSON Stream after Aggregate Mediator")
    public void testJsonStreamAfterAggregateMediator() throws Exception {

        String serviceUrl = getProxyServiceURLHttp("EI5523TestJsonStreamAfterAggregateMediatorProxy");

        String requestPayload = "{\n" +
                "    \"accounts\": [\n" +
                "        {\n" +
                "            \"accountNo\": \"01001\",\n" +
                "            \"customerNo\": \"0001\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"accountNo\": \"01002\",\n" +
                "            \"customerNo\": \"0002\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        Reader data = new StringReader(requestPayload);
        Writer writer = new StringWriter();
        String response = HttpURLConnectionClient
                .sendPostRequestAndReadResponse(data, new URL(serviceUrl), writer, "application/json");
        Assert.assertTrue(response.contains("\"accountNo\":\"01001\"") && response.contains("\"accountNo\":\"01002\""),
                "Error in Json Stream after Aggregate Mediator.");
    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        super.cleanup();
    }
}
