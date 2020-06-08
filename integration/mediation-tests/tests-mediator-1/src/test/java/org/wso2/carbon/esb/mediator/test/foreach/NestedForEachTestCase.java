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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.mediator.test.iterate.IterateClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

/**
 * Test that a nested foreach will transform the payload.
 */
public class NestedForEachTestCase extends ESBIntegrationTest {
    private IterateClient client;
    private CarbonLogReader carbonLogReader;
    private SimpleHttpClient simpleHttpClient;
    private Map<String, String> headers;

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        client = new IterateClient();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = {"wso2.esb"}, description = "Transforming a Message Using a Nested ForEach Construct")
    public void testNestedForEach() throws Exception {
        carbonLogReader.clearLogs();
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
                        + "<soap:Header/><soap:Body><m0:getQuote><m0:request><m0:symbol>IBM</m0:symbol></m0:request>"
                        + "<m0:request><m0:symbol>WSO2</m0:symbol></m0:request><m0:request><m0:symbol>MSFT</m0:symbol></m0:request>"
                        + "</m0:getQuote></soap:Body></soap:Envelope>";
        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("foreachNestedTestProxy"),
                headers, request, "application/xml;charset=UTF-8");

        if (carbonLogReader.checkForLog("foreach = after", DEFAULT_TIMEOUT)) {
            String logs = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(logs);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");

            int start = matcher.start();
            int end = matcher.end();
            String quote = logs.substring(start, end);

            assertTrue(quote.contains(
                    "<m0:checkPriceRequest><m0:symbol>IBM-1</m0:symbol><m0:symbol>IBM-2</m0:symbol></m0:checkPriceRequest>"),
                    "IBM Element not found");
            assertTrue(quote.contains(
                    "<m0:checkPriceRequest><m0:symbol>WSO2-1</m0:symbol><m0:symbol>WSO2-2</m0:symbol></m0:checkPriceRequest>"),
                    "WSO2 Element not found");
            assertTrue(quote.contains(
                    "<m0:checkPriceRequest><m0:symbol>MSFT-1</m0:symbol><m0:symbol>MSFT-2</m0:symbol></m0:checkPriceRequest>"),
                    "MSFT Element not found");

        }
    }

    @Test(groups = "wso2.esb", description = "Transforming a Message Using a Nested ForEach Construct with Iterate/Aggregate Sending Payload to backend")
    public void testNestedForEachMediatorWithIterate() throws Exception {
        String response = client.send(getProxyServiceURLHttp("nested_foreach_iterate"), createMultipleSymbolPayLoad(10), "urn:getQuote");
        Assert.assertNotNull(response);

        for (int i = 0; i < 10; i++) {
            if (carbonLogReader.checkForLog("foreach = outer", DEFAULT_TIMEOUT)) {
                if (!carbonLogReader.getLogs().contains("SYM" + i)) {
                    Assert.fail("Incorrect message entered outer ForEach scope. Could not find symbol SYM"
                            + i + " Found : " + carbonLogReader.getLogs());
                }
            } else if (carbonLogReader.checkForLog("foreach = inner", DEFAULT_TIMEOUT)) {
                if (!carbonLogReader.getLogs().contains("SYM" + i)) {
                    Assert.fail("Incorrect message entered inner ForEach scope. Could not find symbol SYM"
                            + i + " Found : " + carbonLogReader.getLogs());
                }
            }
        }
        Assert.assertTrue(carbonLogReader.checkForLog("foreach = outer", DEFAULT_TIMEOUT, 10),
                "Count of messages entered outer ForEach scope is incorrect. " +
                        "Found " + carbonLogReader.getNumberOfOccurencesForLog("foreach = outer") + " occurrences");
        Assert.assertTrue(carbonLogReader.checkForLog("foreach = inner", DEFAULT_TIMEOUT, 10),
                "Count of messages entered inner ForEach scope is incorrect. " +
                        "Found " + carbonLogReader.getNumberOfOccurencesForLog("foreach = inner") + " occurrences");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }

    private OMElement createMultipleSymbolPayLoad(int iterations) {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getQuote", omNs);

        for (int i = 0; i < iterations; i++) {
            OMElement chkPrice = fac.createOMElement("CheckPriceRequest", omNs);
            OMElement code = fac.createOMElement("Code", omNs);
            chkPrice.addChild(code);
            code.setText("SYM" + i);
            method.addChild(chkPrice);
        }
        return method;
    }
}
