/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.passthru.transport.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.JSONClient;
import org.wso2.esb.integration.common.utils.servers.SimpleSocketServer;

/**
 * Tests if an error is thrown when a payload is received with empty content type when building. Also asserts that
 * the property 'message.builder.invoked' is set to true.
 */
public class SetPropertyMessageBuilderInvokedWithEmptyContentTypeTestCase extends ESBIntegrationTest {
    private static final String EXPECTED_ERROR_MESSAGE = "Could not save JSON payload. Invalid input stream found";
    private JSONClient jsonclient;
    private SimpleSocketServer simpleSocketServer;
    CarbonLogReader carbonLogReader = new CarbonLogReader();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        jsonclient = new JSONClient();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Test whether the msg builder invoked property is set when the content"
            + " type is empty")
    public void testMsgBuilderInvokedPropertyWhenContentTypeisEmpty() throws Exception {

        int port = 8090;
        String expectedResponse =
                "HTTP/1.0 200 OK\r\n" + "Server: CERN/3.0 libwww/2.17\r\n" + "Date: Tue, 16 Nov 1994 08:12:31 GMT\r\n"
                        + "\r\n" + "<HTML>\n" + "<!DOCTYPE HTML PUBLIC "
                        + "\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" + "<HEAD>\n"
                        + " <TITLE>Test Server Results</TITLE>\n" + "</HEAD>\n" + "\n" + "<BODY BGCOLOR=\"#FDF5E6\">\n"
                        + "<H1 ALIGN=\"CENTER\"> Results</H1>\n" + "Here is the request line and request headers\n"
                        + "sent by your browser:\n" + "<PRE>";
        simpleSocketServer = new SimpleSocketServer(port, expectedResponse);
        simpleSocketServer.start();

        final String jsonPayload = "{\"album\":\"Hello\",\"singer\":\"Peter\"}";
        String apiEp = getApiInvocationURL("setMessageBuilderInvokedWithEmptyContentType");
        jsonclient.sendUserDefineRequest(apiEp, jsonPayload.trim());

        Assert.assertTrue(carbonLogReader.checkForLog("messageBuilderInvokedValue = true", 20),
                "'message.builder.invoked' value is not set when the content type is empty.");
        Assert.assertFalse(carbonLogReader.checkForLog(EXPECTED_ERROR_MESSAGE, DEFAULT_TIMEOUT), "The error, "
                + EXPECTED_ERROR_MESSAGE + "is found in testMsgBuilderInvokedPropertyWhenContentTypeisEmpty testcase.");

        carbonLogReader.stop();
    }

}
