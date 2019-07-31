/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.samples.test.mediation;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.samples.test.util.ESBSampleIntegrationTest;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Sample 18: Transforming a Message Using ForEachMediator
 */
public class Sample18TestCase extends ESBSampleIntegrationTest {
    CarbonLogReader carbonLogReader = new CarbonLogReader();
    private String endpoint;

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        endpoint = getProxyServiceURLHttp("Sample18TestCaseProxy");
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Transforming a Message Using ForEachMediator")
    public void testTransformWithForEachMediator() throws Exception {
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n"
                        + "    <soap:Header/>\n" + "    <soap:Body>\n" + "        <m0:getQuote>\n"
                        + "            <m0:request><m0:symbol>IBM</m0:symbol></m0:request>\n"
                        + "            <m0:request><m0:symbol>WSO2</m0:symbol></m0:request>\n"
                        + "            <m0:request><m0:symbol>MSFT</m0:symbol></m0:request>\n"
                        + "        </m0:getQuote>\n" + "    </soap:Body>\n" + "</soap:Envelope>\n";

        SimpleHttpClient client = new SimpleHttpClient();
        String charset = "UTF-8";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Charset", charset);

        client.doPost(endpoint, headers, request, "application/xml;charset=" + charset);

        if (carbonLogReader.checkForLog("<m0:getQuote>", DEFAULT_TIMEOUT)) {
            String logs = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(logs);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");
            int start = matcher.start();
            int end = matcher.end();
            String quote = logs.substring(start, end);

            assertTrue(quote.contains("<m0:checkPriceRequest><m0:code>IBM</m0:code></m0:checkPriceRequest>"),
                    "IBM Element not found");
            assertTrue(quote.contains("<m0:checkPriceRequest><m0:code>WSO2</m0:code></m0:checkPriceRequest>"),
                    "WSO2 Element not found");
            assertTrue(quote.contains("<m0:checkPriceRequest><m0:code>MSFT</m0:code></m0:checkPriceRequest>"),
                    "MSTF Element not found");

        } else {
            fail("Expected logs not found.");
        }
        carbonLogReader.stop();
    }

}
