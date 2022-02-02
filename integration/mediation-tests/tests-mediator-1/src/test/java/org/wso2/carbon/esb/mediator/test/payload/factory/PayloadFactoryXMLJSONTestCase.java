/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.esb.mediator.test.payload.factory;

import org.apache.http.HttpResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * PayloadFactoryXMLJSONTestCase class contains test cases for testing the payload factory mediator
 * for XML and JSON conversions with Special characters.
 */
public class PayloadFactoryXMLJSONTestCase extends ESBIntegrationTest {
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test JsonToXML With $ Mark And Literal true")
    public void testJsonToXMLWithDollarMarkAndLiteralTrue() throws Exception {

        String jsonPayloadWithDollarMarkAndQuotes = "{\"test\": \"test \\\"$\\\"\"}";
        String responseString =
                postJSONPayload("services/pfJSONtoXMLWithDollarMarkProxy", jsonPayloadWithDollarMarkAndQuotes);
        assertEquals(responseString, "<root xmlns=\"http://ws.apache.org/ns/synapse\">" +
                        "<ticketinformation>{\"test\": \"test \\\"$\\\"\"}</ticketinformation></root>",
                "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test JsonToXML With Ampersand And Literal true")
    public void testJsonToXMLWithAmpersand() throws Exception {

        String jsonPayloadWithAmpersand = "{\"test\":\"error &\"}";
        String responseString =
                postJSONPayload("pfJSONtoXMLWithAmpersandAPI", jsonPayloadWithAmpersand);
        assertEquals(responseString, "<mediate><entity_document>{\"test\":\"error &amp;\"}" +
                        "</entity_document></mediate>",
                "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test JsonToXML With BackSlashes")
    public void testJsonToXMLWithBackSlashes() throws Exception {

        String responseString = postJSONPayload("pfJSONtoXMLWithBackSlashesAPI", "");
        assertEquals(responseString, "<text xmlns=\"http://ws.apache.org/commons/ns/payload\">" +
                "XYZ[\\]\\\\^_</text>", "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test convert JSON stream to XML")
    public void testConvertJsonStreamToXML() throws Exception {

        String jsonPayload = "{\"location\":\"\",\"parent\":\"\",\"priority\":\"3\",\"state\":\"1\"," +
                "\"number\":\"MN32432\",\"impact\":\"2\",\"description\":\"print issue\"}";
        String responseString =
                postJSONPayload("pfConvertJsonStreamToXML", jsonPayload);
        assertEquals(responseString, "<root><information>{\"location\":\"\",\"parent\":\"\"," +
                        "\"priority\":\"3\",\"state\":\"1\",\"number\":\"MN32432\",\"impact\":\"2\"," +
                        "\"description\":\"print issue\"}</information></root>",
                "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test String With Curly Brackets")
    public void testXMLStringWithCurlyBracketsProxy() throws Exception {

        String jsonPayloadRandom = "{\"test\": \"test \\\"$\\\"\"}";
        String responseString =
                postJSONPayload("services/pfXMLStringWithCurlyBracketsProxy", jsonPayloadRandom);
        assertEquals(responseString, "<root xmlns=\"http://ws.apache.org/ns/synapse\">" +
                        "<apikey>{string starts with curly braces}</apikey></root>",
                "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test String With Square Brackets")
    public void testXMLStringWithSquareBracketsProxy() throws Exception {

        String jsonPayloadRandom = "{ \"data\": {\n" +
                "\t\t\t\"address1\":\"[yyyy]\",\n" +
                "\t\t\t\"address2\":\"xxxx\"\n" +
                "}\t\n" +
                "}\n";
        String responseString =
                postJSONPayload("services/pfXMLStringWithSquareBracketsProxy", jsonPayloadRandom);
        assertEquals(responseString, "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        "xmlns:hub=\"http://schemas.datacontract.org/2004/07/HubWebAPI\" " +
                        "xmlns:tem=\"http://tempuri.org/\"><soapenv:Header/><soapenv:Body><tem:UpdatePlayerAddress>" +
                        "<tem:address1>[yyyy]</tem:address1></tem:UpdatePlayerAddress>" +
                        "</soapenv:Body></soapenv:Envelope>",
                "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test complex JSON stream to XML")
    public void testComplexJSONToXML() throws Exception {

        String jsonPayloadComplex = "{\n" +
                "    \"testroot\": {\n" +
                "        \"caller\": \"Alice\",\n" +
                "        \"callerRef\": \"Test\",\n" +
                "        \"personNumbers\": [\n" +
                "            \"860\"\n" +
                "        ],\n" +
                "        \"notifyServices\":[\"Pager\"],\n" +
                "        \"uuns\": []\n" +
                "         \n" +
                "    }}";
        String responseString =
                postJSONPayload("services/pfConvertComplexJsonStreamToXML", jsonPayloadComplex);
        assertEquals(responseString, "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Body><testroot xmlns=\"http://www.w3.org/2005/Atom\"><event>Test</event>" +
                "<personNumber>[\"860\"]</personNumber><notifyServices>[\"Pager\"]</notifyServices>" +
                "</testroot></soapenv:Body></soapenv:Envelope>", "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test JSON to JSON")
    public void testJSONToJSONConversion() throws Exception {

        String jsonPayloadString = "{\"test\":\"Hello World\"}";
        String responseString =
                postJSONPayload("pfJSONtoJSONAPI", jsonPayloadString);
        assertEquals(responseString, "{\"Hello\":[{\"test\":\"Hello World\"}]}",
                "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test JSON to JSON With Carriage Return")
    public void testJSONToJSONConversionWithCarriageReturn() throws Exception {

        String jsonPayloadStringWithCarriageString = "{\n" +
                "\t\"CaseExchange\": \"workorder\"\n" +
                "}";
        String responseString =
                postJSONPayload("pfJSONtoJSONWithCarriageReturnAPI", jsonPayloadStringWithCarriageString);
        assertEquals(responseString, "{\n" +
                        "                    \"message\":{\n" +
                        "                    \"workorderjson\": \"{\\n\\t\\\"CaseExchange\\\": \\\"workorder\\\"\\n}\"\n" +
                        "                    }\n" +
                        "                    }",
                "PayloadFactory did not return proper response");
    }

    @Test(groups = "wso2.esb", description = "Payloadfactory mediator - test JSON to JSON With Array As Literal")
    public void testJSONToJSONConversionWithArrayAsLiteral() throws Exception {

        String jsonPayloadStringWithArray = "{ \"Activities\":{ \"Activity\":[ { \"EARLY_FINISH\":\"30-12-2021\", " +
                "\"SUB_PROJECT_ID\":\"401\", \"ACTIVITY_SHORT_NAME\":\"1038678.401.85\", " +
                "\"ACTIVITY_DESCRIPTION\":\"\\\"blah (blah) & blah\\\"\", \"ACTIVITY_NO\":\"85\", " +
                "\"ACTIVITY_SEQ\":\"10139822145\", \"EARLY_START\":\"27-12-2020\", \"ROWKEY\":\"1111111111\", " +
                "\"ROWSTATE\":\"Released\", \"PROJECT_ID\":\"10328678\" } ] } }";
        String responseString =
                postJSONPayload("/services/pfJSONtoJSONWithArrayLiteralProxy", jsonPayloadStringWithArray);
        assertEquals(responseString, "{\n" +
                        "                    \"_postInsertProjectsIntoSQLDB\":{\n" +
                        "                    \"json\": \"{ \\\"Activities\\\":{ \\\"Activity\\\":[ " +
                        "{ \\\"EARLY_FINISH\\\":\\\"30-12-2021\\\", \\\"SUB_PROJECT_ID\\\":\\\"401\\\", " +
                        "\\\"ACTIVITY_SHORT_NAME\\\":\\\"1038678.401.85\\\", " +
                        "\\\"ACTIVITY_DESCRIPTION\\\":\\\"\\\\\\\"blah (blah) & blah\\\\\\\"\\\", " +
                        "\\\"ACTIVITY_NO\\\":\\\"85\\\", \\\"ACTIVITY_SEQ\\\":\\\"10139822145\\\", " +
                        "\\\"EARLY_START\\\":\\\"27-12-2020\\\", \\\"ROWKEY\\\":\\\"1111111111\\\", " +
                        "\\\"ROWSTATE\\\":\\\"Released\\\", \\\"PROJECT_ID\\\":\\\"10328678\\\" } ] } }\"\n" +
                        "                    }\n" +
                        "                    }",
                "PayloadFactory did not return proper response");
    }

    private String postJSONPayload(String service, String payload) throws Exception {

        SimpleHttpClient httpClient = new SimpleHttpClient();
        String url = getMainSequenceURL() + service;
        String contentType = "application/json";
        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("Content-Type", contentType);
        HttpResponse httpResponse = httpClient.doPost(url, headers, payload, contentType);
        return httpClient.getResponsePayload(httpResponse);
    }
}
