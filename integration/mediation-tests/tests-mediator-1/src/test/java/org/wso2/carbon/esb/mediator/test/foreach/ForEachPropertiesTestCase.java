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
public class ForEachPropertiesTestCase extends ESBIntegrationTest {
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

    @Test(groups = "wso2.esb", description = "Test foreach properties in a single foreach construct" )
    public void testSingleForEachProperties() throws Exception {
        carbonLogReader.clearLogs();
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
                        + "<soap:Header/><soap:Body><m0:getQuote>"
                        + "<m0:group>Group1</m0:group>"
                        + "<m0:request><m0:code>IBM</m0:code></m0:request>"
                        + "<m0:request><m0:code>WSO2</m0:code></m0:request>"
                        + "<m0:request><m0:code>MSFT</m0:code></m0:request></m0:getQuote>"
                        + "</soap:Body></soap:Envelope>";

        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("foreachSinglePropertyTestProxy"),
                headers, request, "application/xml;charset=UTF-8");

        if (carbonLogReader.checkForLog("fe_originalpayload", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("in_originalpayload", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("out_originalpayload", DEFAULT_TIMEOUT)) {
            //fe : original payload while in foreach
            //in : original payload outside foreach
            String payload = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");
            if (matchFound) {
                int start = matcher.start();
                int end = matcher.end();
                String quote = payload.substring(start, end);

                assertTrue(carbonLogReader.getLogs().contains(
                        "<m0:getQuote><m0:group>Group1</m0:group>" + "<m0:request><m0:code>IBM</m0:code></m0:request>"
                                + "<m0:request><m0:code>WSO2</m0:code></m0:request>"
                                + "<m0:request><m0:code>MSFT</m0:code></m0:request></m0:getQuote>"),
                           "original payload is incorrect");
            }
        }

        if (carbonLogReader.checkForLog("fe_group", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("in_group", DEFAULT_TIMEOUT)) {
            //group in insequence and foreach sequence
            assertTrue(carbonLogReader.getLogs().contains("Group1"), "Group mismatch, expected Group1 found = " +
                    carbonLogReader.getLogs());
        }

        if (carbonLogReader.checkForLog("in_count", DEFAULT_TIMEOUT)) {
            //counter at the end of foreach in insequence
            assertTrue(carbonLogReader.getLogs().contains("in_count = " + 3),
                    "Final counter mismatch, expected 3 found = " + carbonLogReader.getLogs());
        }
        //final payload in insequence
        String payload = carbonLogReader.getLogs();
        String search = "<m0:getQuote>(.*)</m0:getQuote>";
        Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(payload);
        boolean matchFound = matcher.find();

        assertTrue(matchFound, "getQuote element not found");
        int start = matcher.start();
        int end = matcher.end();
        String quote = payload.substring(start, end);
        assertTrue(quote.contains("<m0:group>Group1</m0:group>"), "Group Element not found in : " + quote);
        assertTrue(quote.contains("<m0:symbol>Group1_IBM</m0:symbol>"), "IBM Element not found in : " + quote);
        assertTrue(quote.contains("<m0:symbol>Group1_WSO2</m0:symbol>"), "WSO2 Element not found in : " + quote);
        assertTrue(quote.contains("<m0:symbol>Group1_MSFT</m0:symbol>"), "MSTF Element not found in : " + quote);
    }

    @Test(groups = "wso2.esb", description = "Test foreach properties in a multiple foreach constructs without id specified")
    public void testMultipleForEachPropertiesWithoutID() throws Exception {
        carbonLogReader.clearLogs();
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
                        + "<soap:Header/><soap:Body><m0:getQuote>" + "<m0:group>Group1</m0:group>"
                        + "<m0:request><m0:code>IBM</m0:code></m0:request>"
                        + "<m0:request><m0:code>WSO2</m0:code></m0:request>"
                        + "<m0:request><m0:code>MSFT</m0:code></m0:request></m0:getQuote>"
                        + "</soap:Body></soap:Envelope>";

        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("foreachMultiplePropertyWithoutIDTestProxy"), headers,
                request, "application/xml;charset=UTF-8");

        //*** MESSAGES FOR FOREACH 1 ****
        if (carbonLogReader.checkForLog("1_fe_originalpayload", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("1_in_originalpayload", DEFAULT_TIMEOUT)) {
            //fe : original payload while in foreach
            //in : original payload outside foreach
            String payload = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");
            if (matchFound) {
                int start = matcher.start();
                int end = matcher.end();
                String quote = payload.substring(start, end);

                assertTrue(quote.contains(
                        "<m0:getQuote><m0:group>Group1</m0:group>" + "<m0:request><m0:code>IBM</m0:code></m0:request>"
                                + "<m0:request><m0:code>WSO2</m0:code></m0:request>"
                                + "<m0:request><m0:code>MSFT</m0:code></m0:request></m0:getQuote>"),
                           "original payload is incorrect");
            }
        }

        if (carbonLogReader.checkForLog("1_fe_group", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("1_in_group", DEFAULT_TIMEOUT)) {
            //group in insequence and foreach sequence
            assertTrue(carbonLogReader.getLogs().contains("Group1"),
                    "Group mismatch, expected Group1 found = " + carbonLogReader.getLogs());
        }

        if (carbonLogReader.checkForLog("1_in_count", DEFAULT_TIMEOUT)) {
            //counter at the end of foreach in insequence
            assertTrue(carbonLogReader.getLogs().contains("in_count = " + 3),
                    "Final counter mismatch, expected 3 found = " + carbonLogReader.getLogs());
        }

        if (carbonLogReader.checkForLog("1_in_payload", DEFAULT_TIMEOUT)) {
            //final payload in insequence and payload in outsequence
            String payload = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");
            if (matchFound) {
                int start = matcher.start();
                int end = matcher.end();
                String quote = payload.substring(start, end);
                assertTrue(quote.contains("<m0:group>Group1</m0:group>"), "Group Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_IBM</m0:symbol>"), "IBM Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_WSO2</m0:symbol>"),
                           "WSO2 Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_MSFT</m0:symbol>"),
                           "MSTF Element not found in : " + quote);
            }
        }

        foreachAssert(carbonLogReader.getLogs());

        if (carbonLogReader.checkForLog("2_fe_group", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("2_in_group", DEFAULT_TIMEOUT)) {
            //group in insequence and foreach sequence
            assertTrue(carbonLogReader.getLogs().contains("Group2"), "Group mismatch, expected Group1 found = " +
                    carbonLogReader.getLogs());
        }
        foreachAssert(carbonLogReader.getLogs());
    }

    @Test(groups = "wso2.esb", description = "Test foreach properties in a multiple foreach constructs with id specified")
    public void testMultipleForEachPropertiesWithID() throws Exception {
        carbonLogReader.clearLogs();
        String request =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:m0=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
                        + "<soap:Header/><soap:Body><m0:getQuote>" + "<m0:group>Group1</m0:group>"
                        + "<m0:request><m0:code>IBM</m0:code></m0:request>"
                        + "<m0:request><m0:code>WSO2</m0:code></m0:request>"
                        + "<m0:request><m0:code>MSFT</m0:code></m0:request></m0:getQuote>"
                        + "</soap:Body></soap:Envelope>";

        simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doPost(getProxyServiceURLHttp("foreachMultiplePropertyWithIDTestProxy"), headers,
                request, "application/xml;charset=UTF-8");


        //*** MESSAGES FOR FOREACH 1 ****
        if (carbonLogReader.checkForLog("1_fe_originalpayload", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("1_in_originalpayload", DEFAULT_TIMEOUT)) {
            //fe : original payload while in foreach
            //in : original payload outside foreach
            String payload = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");
            if (matchFound) {
                int start = matcher.start();
                int end = matcher.end();
                String quote = payload.substring(start, end);

                assertTrue(quote.contains(
                        "<m0:getQuote><m0:group>Group1</m0:group>" + "<m0:request><m0:code>IBM</m0:code></m0:request>"
                                + "<m0:request><m0:code>WSO2</m0:code></m0:request>"
                                + "<m0:request><m0:code>MSFT</m0:code></m0:request></m0:getQuote>"),
                           "original payload is incorrect");
            }
        }

        if (carbonLogReader.checkForLog("1_fe_group", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("1_in_group", DEFAULT_TIMEOUT)) {
            //group in insequence and foreach sequence
            assertTrue(carbonLogReader.getLogs().contains("Group1"), "Group mismatch, expected Group1 found = " +
                    carbonLogReader.getLogs());
        }

        if (carbonLogReader.checkForLog("1_in_count", DEFAULT_TIMEOUT)) {
            //counter at the end of foreach in insequence
            assertTrue(carbonLogReader.getLogs().contains("in_count = " + 3),
                    "Final counter mismatch, expected 3 found = " + carbonLogReader.getLogs());
        }

        if (carbonLogReader.checkForLog("1_in_payload", DEFAULT_TIMEOUT)) {
            //final payload in insequence and payload in outsequence
            String payload = carbonLogReader.getLogs();
            String search = "<m0:getQuote>(.*)</m0:getQuote>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "getQuote element not found");
            if (matchFound) {
                int start = matcher.start();
                int end = matcher.end();
                String quote = payload.substring(start, end);
                assertTrue(quote.contains("<m0:group>Group1</m0:group>"), "Group Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_IBM</m0:symbol>"), "IBM Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_WSO2</m0:symbol>"),
                           "WSO2 Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_MSFT</m0:symbol>"),
                           "MSTF Element not found in : " + quote);
            }
        }

        if (carbonLogReader.checkForLog("2_fe_group", 200) ||
                carbonLogReader.checkForLog("2_in_group", 200)) {
            //group in insequence and foreach sequence
            assertTrue(carbonLogReader.getLogs().contains("Group2"), "Group mismatch, expected Group1 found = " +
                    carbonLogReader.getLogs());
        }

        if (carbonLogReader.checkForLog("2_in_count", DEFAULT_TIMEOUT)) {
            //counter at the end of foreach in insequence
            assertTrue(carbonLogReader.getLogs().contains("in_count = " + 4),
                    "Final counter mismatch, expected 4 found = " + carbonLogReader.getLogs());
        }

        if (carbonLogReader.checkForLog("2_in_payload", DEFAULT_TIMEOUT)) {
            //final payload in insequence and payload in outsequence
            String payload = carbonLogReader.getLogs();
            String search = "<m0:checkPrice(.*)</m0:checkPrice>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "checkPrice element not found. Instead found : " + payload);

            if (matchFound) {
                int start = matcher.start();
                int end = matcher.end();
                String quote = payload.substring(start, end);
                assertTrue(quote.contains("<m0:group>Group2</m0:group>"), "Group Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_Group2_IBM</m0:symbol>"),
                           "IBM Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_Group2_WSO2</m0:symbol>"),
                           "WSO2 Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_Group2_MSFT</m0:symbol>"),
                           "MSTF Element not found in : " + quote);
                assertTrue(quote.contains("<m0:symbol>Group1_Group2_SUN</m0:symbol>"),
                           "SUN Element not found in : " + quote);
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }

    private void foreachAssert(String message) throws Exception {
        //*** MESSAGES FOR FOREACH 2 ***

        if (carbonLogReader.checkForLog("2_fe_originalpayload", DEFAULT_TIMEOUT) ||
                carbonLogReader.checkForLog("2_in_originalpayload", DEFAULT_TIMEOUT)) {
            //fe : original payload while in foreach
            //in : original payload outside foreach
            String payload = message;
            String search = "<m0:checkPrice(.*)</m0:checkPrice>";
            Pattern pattern = Pattern.compile(search, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(payload);
            boolean matchFound = matcher.find();

            assertTrue(matchFound, "checkPrice element not found. Instead found : " + payload);

            if (matchFound) {
                int start = matcher.start();
                int end = matcher.end();
                String quote = payload.substring(start, end);
                assertTrue(quote.contains("<m0:group>Group2</m0:group>"), "Group Element not found in : " + quote);
                assertTrue(quote.contains("<m0:code>IBM</m0:code>"), "IBM Element not found in : " + quote);
                assertTrue(quote.contains("<m0:code>WSO2</m0:code>"), "WSO2 Element not found in : " + quote);
                assertTrue(quote.contains("<m0:code>MSFT</m0:code>"), "MSTF Element not found in : " + quote);
                assertTrue(quote.contains("<m0:code>SUN</m0:code>"), "SUN Element not found in : " + quote);
            }
        }
    }
}
