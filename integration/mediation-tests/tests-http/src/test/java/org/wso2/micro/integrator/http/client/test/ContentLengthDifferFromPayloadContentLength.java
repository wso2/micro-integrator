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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HttpRequestWithExpectedHTTPSC;
import org.wso2.micro.integrator.http.utils.PayloadSize;
import org.wso2.micro.integrator.http.utils.RequestMethods;

import java.io.PrintStream;

import static org.wso2.micro.integrator.http.utils.Constants.HTTP_SC_200;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_SC_202;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_SC_400;

/**
 * Test case tests for MI behaviour(specifically CPU usage) when a content length header and body content length
 * mismatch HTTP request is received.
 */
public class ContentLengthDifferFromPayloadContentLength extends HTTPCoreClientTest {

    int contentLengthDiff = 0;

    @Test(groups = {"wso2.esb"}, description = "Test for MI behaviour when Content Length is greater than actual Body" +
            " length.", dataProvider = "httpRequestWithGreaterContentLength")
    public void testContentLengthHeaderGreaterFromPayloadContentLength(HttpRequestWithExpectedHTTPSC httpRequest)
            throws Exception {

        contentLengthDiff = 100;
        invokeHTTPCoreTestAPI(httpRequest);
    }

    @Test(groups = {"wso2.esb"}, description = "Test for MI behaviour when Content Length is lower than actual Body" +
            " length.", dataProvider = "httpRequestWithLowerContentLength")
    public void testContentLengthHeaderLowerFromPayloadContentLength(HttpRequestWithExpectedHTTPSC httpRequest)
            throws Exception {

        contentLengthDiff = -100;
        invokeHTTPCoreTestAPI(httpRequest);
    }

    @Override
    protected void sendHTTPRequest(PrintStream printWriter, RequestMethods method, String payload) {

        printWriter.print(method + " " + Constants.HTTPCORE_API_CONTEXT + " HTTP/1.1" + Constants.CRLF);
        printWriter.print("Accept: application/json" + Constants.CRLF);
        printWriter.print("Connection: keep-alive" + Constants.CRLF);
        printWriter.print("Content-Type: application/json" + Constants.CRLF);
        printWriter.print("Content-Length: " + (payload.getBytes().length + contentLengthDiff) + Constants.CRLF);
        printWriter.print(Constants.CRLF);
        printWriter.print(payload);
        printWriter.flush();
    }

    @DataProvider(name = "httpRequestWithLowerContentLength")
    private static Object[][] httpRequestWithLowerContentLength() {

        return new Object[][]{
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.EMPTY, true, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.SMALL, true, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.LARGE, true, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.EMPTY, true, HTTP_SC_400)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.SMALL, true, HTTP_SC_202)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.LARGE, true, HTTP_SC_202)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.EMPTY, false, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.SMALL, false, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.LARGE, false, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.EMPTY, false, HTTP_SC_400)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.SMALL, false, HTTP_SC_202)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.LARGE, false, HTTP_SC_202)}
        };
    }

    @DataProvider(name = "httpRequestWithGreaterContentLength")
    private static Object[][] httpRequestWithGreaterContentLength() {

        return new Object[][]{
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.EMPTY, true, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.SMALL, true, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.LARGE, true, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.EMPTY, true, "")},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.SMALL, true, "")},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.LARGE, true, "")},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.EMPTY, false, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.SMALL, false, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.GET, PayloadSize.LARGE, false, HTTP_SC_200)},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.EMPTY, false, "")},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.SMALL, false, "")},
                {new HttpRequestWithExpectedHTTPSC(RequestMethods.POST, PayloadSize.LARGE, false, "")}
        };
    }
}
