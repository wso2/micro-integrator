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

package org.wso2.carbon.esb.mediator.test.foreach;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.testng.Assert.assertTrue;

/**
 * Test that foreach will process the payload sequentially. Verify the request payload order against processed order.
 */
public class NestedForEachPropertiesTestCase extends ESBIntegrationTest {
    private SimpleHttpClient simpleHttpClient;
    private Map<String, String> headers;
    CarbonLogReader carbonLogReader;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        headers = new HashMap<>();
        headers.put("Accept-Charset", "UTF-8");
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Test foreach properties in a nested foreach constructs with id specified")
    public void testNestedForEachPropertiesWithID() throws Exception {
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n"
                        + "    <soap:Header/>\n" + "    <soap:Body>\n" + "        <m0:getQuote>\n"
                        + "            <m0:group>Group</m0:group>\n"
                        + "            <m0:request><m0:symbol>IBM</m0:symbol></m0:request>\n"
                        + "            <m0:request><m0:symbol>WSO2</m0:symbol></m0:request>\n"
                        + "            <m0:request><m0:symbol>MSFT</m0:symbol></m0:request>\n"
                        + "        </m0:getQuote>\n" + "    </soap:Body>\n" + "</soap:Envelope>\n";

        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("foreachNestedPropertiesTestProxy"),
                headers, request, "application/xml;charset=UTF-8");

        //*** MESSAGES FOR OUTER FOREACH ****
        if (carbonLogReader.checkForLog("outer_fe_originalpayload", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("outer_in_originalpayload", DEFAULT_TIMEOUT)) {
            //fe : original payload while in foreach
            //in : original payload outside foreach
            String logs = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(logs);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");

            int start = matcher.start();
            int end = matcher.end();
            String quote = logs.substring(start, end);

            assertTrue(quote.contains("<m0:getQuote>" + "            <m0:group>Group</m0:group>"
                    + "            <m0:request><m0:symbol>IBM</m0:symbol></m0:request>"
                    + "            <m0:request><m0:symbol>WSO2</m0:symbol></m0:request>"
                    + "            <m0:request><m0:symbol>MSFT</m0:symbol></m0:request>"
                    + "        </m0:getQuote>"), "original payload is incorrect");
        } else if (carbonLogReader.checkForLog("outer_fe_group", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("outer_in_group", DEFAULT_TIMEOUT)) {
            //group in insequence and foreach sequence
            assertTrue(carbonLogReader.getLogs().contains("Group"), "Group mismatch, expected Group found = " + carbonLogReader.getLogs());
        }

        //*** MESSAGES FOR INNER FOREACH ***

        else if (carbonLogReader.checkForLog("inner_fe_originalpayload", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("inner_in_originalpayload", DEFAULT_TIMEOUT)) {
            //fe : original payload while in foreach
            //in : original payload outside foreach
            String payload = carbonLogReader.getLogs();
            String search = "<m0:checkPrice(.*)</m0:checkPrice>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "checkPrice element not found. Instead found : " + payload);

            int start = matcher.start();
            int end = matcher.end();
            String quote = payload.substring(start, end);

            if (payload.contains("<m0:group>NewGroup0</m0:group>")) {
                assertTrue(quote.contains("<m0:code>IBM-1</m0:code>"), "IBM Element not found");
                assertTrue(quote.contains("<m0:code>IBM-2</m0:code>"), "IBM Element not found");
            } else if (payload.contains("<m0:group>NewGroup1</m0:group>")) {

                assertTrue(quote.contains("<m0:code>WSO2-1</m0:code>"), "WSO2 Element not found");
                assertTrue(quote.contains("<m0:code>WSO2-2</m0:code>"), "WSO2 Element not found");
            } else if (payload.contains("<m0:group>NewGroup2</m0:group>")) {
                assertTrue(quote.contains("<m0:code>MSFT-1</m0:code>"), "MSTF Element not found");
                assertTrue(quote.contains("<m0:code>MSFT-2</m0:code>"), "MSTF Element not found");
            } else {
                assertTrue(false, "Payload not found");
            }
        } else if (carbonLogReader.checkForLog("inner_in_group", DEFAULT_TIMEOUT)) {
            //group in insequence for inner foreach
            assertTrue(carbonLogReader.getLogs().contains("NewGroup2"),
                    "Group mismatch, expected NewGroup2 found = " + carbonLogReader.getLogs());
        } else if (carbonLogReader.checkForLog("inner_fe_end_originalpayload", DEFAULT_TIMEOUT)) {
            //at end of inner foreach
            String logs = carbonLogReader.getLogs();
            String search = "<m0:checkPrice(.*)</m0:checkPrice>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(logs);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "checkPrice element not found. Instead found : " + logs);

            int start = matcher.start();
            int end = matcher.end();
            String quote = logs.substring(start, end);

            if (logs.contains("<m0:group>NewGroup0</m0:group>")) {
                assertTrue(quote.contains("<m0:code>IBM-1</m0:code>"), "IBM Element not found");
                assertTrue(quote.contains("<m0:code>IBM-2</m0:code>"), "IBM Element not found");
            } else if (logs.contains("<m0:group>NewGroup1</m0:group>")) {

                assertTrue(quote.contains("<m0:code>WSO2-1</m0:code>"), "WSO2 Element not found");
                assertTrue(quote.contains("<m0:code>WSO2-2</m0:code>"), "WSO2 Element not found");
            } else if (logs.contains("<m0:group>NewGroup2</m0:group>")) {
                assertTrue(quote.contains("<m0:code>MSFT-1</m0:code>"), "MSTF Element not found");
                assertTrue(quote.contains("<m0:code>MSFT-2</m0:code>"), "MSTF Element not found");
            } else {
                assertTrue(false, "Payload not found");
            }

        } else if (carbonLogReader.checkForLog("inner_fe_end_count", DEFAULT_TIMEOUT)) {
            //counter at the end of foreach in insequence
            assertTrue(carbonLogReader.getLogs().contains("inner_fe_end_count = " + 2),
                    "Final counter mismatch, expected 2 found = " + carbonLogReader.getLogs());
        } else if (carbonLogReader.checkForLog("in_payload", DEFAULT_TIMEOUT)) {
            //final payload in insequence and payload in outsequence
            String logs = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(logs);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "checkPrice element not found. Instead found : " + logs);

            int start = matcher.start();
            int end = matcher.end();
            String quote = logs.substring(start, end);

            assertTrue(quote.contains("<m0:group>Group</m0:group>"), "Group Element not found");
            assertTrue(quote.contains(
                    "<m0:checkPrice><m0:group>NewGroup0</m0:group><m0:symbol>Group_NewGroup0_IBM-1</m0:symbol><m0:symbol>Group_NewGroup0_IBM-2</m0:symbol></m0:checkPrice>"),
                    "IBM Element not found");
            assertTrue(quote.contains(
                    "<m0:checkPrice><m0:group>NewGroup1</m0:group><m0:symbol>Group_NewGroup1_WSO2-1</m0:symbol><m0:symbol>Group_NewGroup1_WSO2-2</m0:symbol></m0:checkPrice>"),
                    "WSO2 Element not found");
            assertTrue(quote.contains(
                    "<m0:checkPrice><m0:group>NewGroup2</m0:group><m0:symbol>Group_NewGroup2_MSFT-1</m0:symbol><m0:symbol>Group_NewGroup2_MSFT-2</m0:symbol></m0:checkPrice>"),
                    "MSTF Element not found");

        }
    }

    @AfterClass
    public void close() throws Exception {
        carbonLogReader.stop();
    }
}
