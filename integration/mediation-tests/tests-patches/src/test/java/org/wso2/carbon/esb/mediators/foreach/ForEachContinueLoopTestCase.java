/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.mediators.foreach;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * Tests ForEach mediator with continue loop improvement
 * Public issue https://github.com/wso2/micro-integrator/issues/3318
 */
public class ForEachContinueLoopTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Tests ForEach mediator with continue loop improvement for JSON payload")
    public void testForEachMediatorContinueLoopWithJSON() throws Exception {

        URL jsonUrl = new URL(getApiInvocationURL("ForEachTestAPI") + "/json");

        String jsonPayload = "{  \n" +
                "   \"info\":[  \n" +
                "      {  \n" +
                "         \"id\":\"IDABC1\",\n" +
                "         \"classid\":1,\n" +
                "         \"name\":\"ABC\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"id\":\"IDEFG2\",\n" +
                "         \"classid\":2,\n" +
                "         \"name\":\"EFG\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"id\":\"IDHIJ3\",\n" +
                "         \"classid\":3,\n" +
                "         \"name\":\"HIJ\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";

        // JSON request
        Map<String, String> jsonHeaders = new HashMap<>();
        jsonHeaders.put("Content-Type", "application/json");
        HttpResponse response = HttpRequestUtil.doPost(jsonUrl, jsonPayload, jsonHeaders);
        Assert.assertEquals(response.getResponseCode(), 200, "Expected response didn't receive");

        assertTrue(Utils.checkForLog(carbonLogReader, "Error occurred while mediating the sequence for the " +
                        "foreach mediator, Sequence named Value {name ='null', keyValue ='asdf'} cannot be found. " +
                        "Continuing with the remaining", 60),
                "Continue loop didn't work as expected for JSON payload");
    }

    @Test(groups = "wso2.esb", description = "Tests ForEach mediator with continue loop improvement for XML payload")
    public void testForEachMediatorContinueLoopWithXML() throws Exception {

        URL xmlUrl = new URL(getApiInvocationURL("ForEachTestAPI") + "/xml");

        String xmlPayload = "<root>\n" +
                "\t<info>\n" +
                "\t\t<id>IDABC1</id>\n" +
                "\t\t<classid>1</classid>\n" +
                "\t\t<name>ABC</name>\n" +
                "\t</info>\n" +
                "\t<info>\n" +
                "\t\t<id>IDEFG2</id>\n" +
                "\t\t<classid>2</classid>\n" +
                "\t\t<name>EFG</name>\n" +
                "\t</info>\n" +
                "\t<info>\n" +
                "\t\t<id>IDHIJ3</id>\n" +
                "\t\t<classid>3</classid>\n" +
                "\t\t<name>HIJ</name>\n" +
                "\t</info>\n" +
                "</root>";

        // XML request
        Map<String, String> xmlHeaders = new HashMap<>();
        xmlHeaders.put("Content-Type", "application/xml");
        HttpResponse response = HttpRequestUtil.doPost(xmlUrl, xmlPayload, xmlHeaders);
        Assert.assertEquals(response.getResponseCode(), 200, "Expected response didn't receive");

        assertTrue(Utils.checkForLog(carbonLogReader, "Error occurred while mediating the sequence for the " +
                        "foreach mediator, Sequence named Value {name ='null', keyValue ='ffff'} cannot be found." +
                        " Continuing with the remaining.", 60),
                "Continue loop didn't work as expected for XML payload");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }
}
