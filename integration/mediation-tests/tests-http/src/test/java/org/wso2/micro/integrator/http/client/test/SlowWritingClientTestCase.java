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
import org.wso2.micro.integrator.http.utils.HttpRequestWithExpectedHTTPSC;
import org.wso2.micro.integrator.http.utils.RequestMethods;

import java.io.PrintStream;

import static org.wso2.micro.integrator.http.utils.Constants.CRLF;
import static org.wso2.micro.integrator.http.utils.Constants.HTTPCORE_API_CONTEXT;

/**
 * Test case tests for MI behaviour(specifically CPU usage) when a slow writing client sends a request.
 */
public class SlowWritingClientTestCase extends HTTPCoreClientTest {

    @Test(groups = {"wso2.esb"}, description = "Test for MI behaviour when a slow writing client sends a request.",
            dataProvider = "httpRequestsWith200OK", dataProviderClass = Constants.class)
    public void testSlowWritingClient(HttpRequestWithExpectedHTTPSC httpRequest) throws Exception {

        invokeHTTPCoreTestAPI(httpRequest);
    }

    @Override
    protected void sendHTTPRequest(PrintStream printWriter, RequestMethods method, String payload) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(HTTPCORE_API_CONTEXT).append(" HTTP/1.1").append(CRLF);
        sb.append("Content-Type: application/json" + CRLF);
        sb.append("Accept: application/json" + CRLF);
        sb.append("Connection: keep-alive" + CRLF);
        if (StringUtils.isNotBlank(payload)) {
            sb.append("Content-Length: ").append(payload.getBytes().length).append(CRLF);
        }
        sb.append(CRLF);
        if (StringUtils.isNotBlank(payload)) {
            sb.append(payload);
        }
        for (int i = 0; i < sb.length(); ++i) {
            printWriter.print(sb.charAt(i));
            printWriter.flush();
            if (i % 100 == 0) {
                Thread.sleep(20);
            }
        }
        printWriter.flush();
    }
}
