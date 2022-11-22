/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.http.client.test;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HTTPRequest;
import org.wso2.micro.integrator.http.utils.RequestMethods;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.StringReader;

/**
 * Test case tests for MI behaviour(specifically CPU usage) when the client closes the socket while writing the
 * body.
 */
public class ClosesConnectionWhileSendingRequestBody extends HTTPCoreClientTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when a client closes the socket while sending the " +
                    "body.", dataProvider = "httpRequests", dataProviderClass = Constants.class)
    public void testClosesConnectionWhileSendingRequestBody(HTTPRequest httpRequest)
            throws Exception {

        invokeHTTPCoreTestAPI(httpRequest);
    }

    @Override
    protected void sendHTTPRequest(PrintStream printWriter, RequestMethods method, String payload) throws Exception {

        printWriter.print(method + " " + Constants.HTTPCORE_API_CONTEXT + " HTTP/1.1" + Constants.CRLF);
        printWriter.print("Content-Type: application/json" + Constants.CRLF);
        printWriter.print("Accept: application/json" + Constants.CRLF);
        printWriter.print("Connection: keep-alive" + Constants.CRLF);
        if (StringUtils.isNotBlank(payload)) {
            printWriter.print("Content-Length: " + payload.getBytes().length + Constants.CRLF);
        }
        printWriter.print(Constants.CRLF);
        if (StringUtils.isNotBlank(payload)) {
            BufferedReader bufReader = new BufferedReader(new StringReader(payload));
            String line = null;
            int count = 0;
            while ((line = bufReader.readLine()) != null) {
                if (count++ == 20) {
                    return;
                }
                printWriter.print(line);
                printWriter.flush();
            }
        }
    }
}
