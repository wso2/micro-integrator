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
import org.wso2.micro.integrator.http.utils.HttpRequestWithExpectedHTTPSC;
import org.wso2.micro.integrator.http.utils.RequestMethods;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.wso2.micro.integrator.http.utils.Constants.CRLF;
import static org.wso2.micro.integrator.http.utils.Constants.HTTPCORE_API_CONTEXT;

/**
 * Test case tests for MI behaviour(specifically CPU usage) when the client sends chunks.
 */
public class ClientSendsChunksTestCase extends HTTPCoreClientTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when a client sends chunks.", dataProvider = "httpRequestsWith200OK", dataProviderClass = Constants.class)
    public void testClientSendsChunks(HttpRequestWithExpectedHTTPSC httpRequest) throws Exception {

        invokeHTTPCoreTestAPI(httpRequest);
    }

    @Override
    protected void sendHTTPRequest(PrintStream printWriter, RequestMethods method, String payload) throws Exception {

        printWriter.print(method + " " + HTTPCORE_API_CONTEXT + " HTTP/1.1" + CRLF);
        printWriter.print("Content-Type: application/json" + CRLF);
        printWriter.print("Accept: application/json" + CRLF);
        printWriter.print("Connection: keep-alive" + CRLF);
        printWriter.print("Transfer-Encoding: chunked" + CRLF);
        printWriter.print(CRLF);

        InputStream payloadStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));

        int chunkSize = 100;
        int count;
        byte[] buffer = new byte[chunkSize];

        while ((count = payloadStream.read(buffer)) > 0) {
            printWriter.printf("%x" + CRLF, count);
            printWriter.write(buffer, 0, count);
            printWriter.print(CRLF);
            printWriter.flush();
        }

        printWriter.print("0" + CRLF);
        printWriter.print(CRLF);
        printWriter.flush();
    }
}
