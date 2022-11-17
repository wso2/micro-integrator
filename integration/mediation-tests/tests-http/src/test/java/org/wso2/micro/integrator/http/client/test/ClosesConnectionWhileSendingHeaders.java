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

import org.testng.annotations.Test;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HTTPRequest;
import org.wso2.micro.integrator.http.utils.RequestMethods;

import java.io.PrintStream;

/**
 * Test case tests for MI behaviour(specifically CPU usage) when the client closes the socket while writing the
 * headers.
 */
public class ClosesConnectionWhileSendingHeaders extends HTTPCoreClientTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when a client closes the socket while sending the " +
                    "headers.", dataProvider = "httpRequests", dataProviderClass = Constants.class)
    public void testClosesConnectionWhileSendingHeaders(HTTPRequest httpRequest) throws Exception {

        invokeHTTPCoreTestAPI(httpRequest);
    }

    @Override
    protected void sendHTTPRequest(PrintStream printWriter, RequestMethods method, String payload) {

        printWriter.print(method + " " + Constants.HTTPCORE_API_CONTEXT + " HTTP/1.1" + Constants.CRLF);
        printWriter.print("Content-Type: application/json" + Constants.CRLF);
        printWriter.print("Accept: application/json" + Constants.CRLF);
        printWriter.print("Connection: keep-alive");
        printWriter.flush();
    }
}
