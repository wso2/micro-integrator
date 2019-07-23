/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediator.test.foreach;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * Test that foreach will process the payload sequentially. Verify the request payload order against processed order.
 */
public class ForEachPropertyMediatorTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;
    private SimpleHttpClient simpleHttpClient;
    private Map<String, String> headers;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
        headers = new HashMap<>();
        headers.put("Accept-Charset", "UTF-8");
    }

    @Test(groups = "wso2.esb", description = "Test multiple foreach constructs with property mediator in flow")
    public void testForEachPropertyMediator() throws Exception {
        carbonLogReader.clearLogs();
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n"
                        + "    <soap:Header/>\n" + "    <soap:Body>\n" + "        <m0:getQuote>\n"
                        + "            <m0:request><m0:code>IBM</m0:code></m0:request>\n"
                        + "            <m0:request><m0:code>WSO2</m0:code></m0:request>\n"
                        + "            <m0:request><m0:code>MSFT</m0:code></m0:request>\n" + "        </m0:getQuote>\n"
                        + "    </soap:Body>\n" + "</soap:Envelope>\n";

        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("foreachPropertyTestProxy"),
                headers, request, "application/xml;charset=UTF-8");

        if (carbonLogReader.checkForLog("fe_1_verify_in_1", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_1_verify_in_1 = first property insequence"));
        }
        if (carbonLogReader.checkForLog("in_2_verify_fe_1", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_2_verify_fe_1 = property in first foreach"));
        }
        if (carbonLogReader.checkForLog("fe_2_verify_in_1", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_2_verify_in_1 = first property insequence"));
        }
        if (carbonLogReader.checkForLog("fe_2_verify_fe_1", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_2_verify_fe_1 = property in first foreach"));
        }
        if (carbonLogReader.checkForLog("fe_2_verify_in_2", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_2_verify_in_2 = second property insequence"));
        }
        if (carbonLogReader.checkForLog("in_3_verify_fe_2", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_3_verify_fe_2 = property in second foreach"));
        }
        if (carbonLogReader.checkForLog("in_3_verify_in_1", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_3_verify_in_1 = first property insequence"));
        }
        if (carbonLogReader.checkForLog("in_3_verify_fe_1", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_3_verify_fe_1 = property in first foreach"));
        }
        if (carbonLogReader.checkForLog("in_3_verify_in_2", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_3_verify_in_2 = second property insequence"));
        }
    }

    @Test(groups = "wso2.esb", description = "Test nested foreach constructs with property mediator in flow")
    public void testNestedForEachPropertiesWithID() throws Exception {
        carbonLogReader.clearLogs();
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n"
                        + "    <soap:Header/>\n" + "    <soap:Body>\n" + "        <m0:getQuote>\n"
                        + "            <m0:request><m0:symbol>IBM</m0:symbol></m0:request>\n"
                        + "            <m0:request><m0:symbol>WSO2</m0:symbol></m0:request>\n"
                        + "            <m0:request><m0:symbol>MSFT</m0:symbol></m0:request>\n"
                        + "        </m0:getQuote>\n" + "    </soap:Body>\n" + "</soap:Envelope>\n";
        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("NestedForEachPropertiesWithID"),
                headers, request, "application/xml;charset=UTF-8");

        if (carbonLogReader.checkForLog("fe_outer_verify_in", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_outer_verify_in = property insequence"));
        }
        if (carbonLogReader.checkForLog("fe_inner_verify_in", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_inner_verify_in = property insequence"));
        }
        if (carbonLogReader.checkForLog("fe_inner_verify_fe_outer", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_inner_verify_fe_outer = property outer foreach"));
        }
        if (carbonLogReader.checkForLog("fe_outer_verify_fe_outer", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_outer_verify_fe_outer = property outer foreach"));
        }
        if (carbonLogReader.checkForLog("fe_outer_fe_inner", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("fe_outer_fe_inner = property inner foreach"));
        }
        if (carbonLogReader.checkForLog("in_verify_in", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_verify_in = property insequence"));
        }
        if (carbonLogReader.checkForLog("in_fe_outer", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_fe_outer = property outer foreach"));
        }
        if (carbonLogReader.checkForLog("in_fe_inner", DEFAULT_TIMEOUT)) {
            assertTrue(carbonLogReader.getLogs().contains("in_fe_inner = property inner foreach"));
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }
}
