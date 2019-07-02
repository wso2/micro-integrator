/*
 *     Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.carbon.esb.template.endpointTemplate;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class to test TemplateEndpoint suspension after retry attempts
 * Verify fix: https://github.com/wso2/product-ei/issues/1604
 */
public class EndpointTemplateSuspensionTest extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Test invoking EP which cause timeout and verify whether Endpoint "
            + "template has been marked for suspension")
    public void testTemplateEndpointSuspension() throws IOException, InterruptedException {

        String contentType = "text/xml";//Content-Type
        Map<String, String> headers = new HashMap<String, String>();//For HTTP Headers
        headers.put("Content-Type", contentType);
        headers.put("SOAPAction", "urn:mediate");
        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + "   <soapenv:Header/>\n" + "   <soapenv:Body>\n" + "       <test>test payload</test>\n"
                + "   </soapenv:Body>\n" + "</soapenv:Envelope>";

        log.info("Invoke testEndpointTemplateProxy attempt 1");
        SimpleHttpClient httpClient1 = new SimpleHttpClient();
        HttpResponse response1 = httpClient1
                .doPost(getProxyServiceURLHttp("testEndpointTemplateProxy"), headers, payload, contentType);

        //Wait for timeout markForSuspension retry delay get pass
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //ignore
        }

        log.info("Invoke testEndpointTemplateProxy attempt 2");
        SimpleHttpClient httpClient2 = new SimpleHttpClient();
        HttpResponse response2 = httpClient2
                .doPost(getProxyServiceURLHttp("testEndpointTemplateProxy"), headers, payload, contentType);

        //Wait for timeout markForSuspension retry delay get pass
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //ignore
        }

        CarbonLogReader carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();

        log.info("Invoke testEndpointTemplateProxy attempt 3");
        SimpleHttpClient httpClient3 = new SimpleHttpClient();
        HttpResponse response3 = httpClient3
                .doPost(getProxyServiceURLHttp("testEndpointTemplateProxy"), headers, payload, contentType);

        //Check logs to verify endpoint suspension
        boolean epSuspended = Utils.checkForLog(carbonLogReader,
                "testEP1 with address http://localhost:8480/testbe has been marked for SUSPENSION", 10);

        Assert.assertTrue(epSuspended, "template endpoint suspension failed");
        carbonLogReader.stop();

    }

}
