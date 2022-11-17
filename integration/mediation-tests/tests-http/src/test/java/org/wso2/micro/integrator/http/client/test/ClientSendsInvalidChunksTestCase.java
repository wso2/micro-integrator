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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HttpRequestWithExpectedHTTPSC;
import org.wso2.micro.integrator.http.utils.RequestMethods;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Test case tests for MI behaviour(specifically CPU usage) when the client sends invalid chunks.
 */
public class ClientSendsInvalidChunksTestCase extends HTTPCoreClientTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when a client sends invalid chunks.", dataProvider = "httpRequestsWith200OK", dataProviderClass = Constants.class)
    public void testClientSendsInvalidChunks(HttpRequestWithExpectedHTTPSC httpRequest) throws Exception {

        invokeHTTPCoreTestAPI(httpRequest);
    }

    @Override
    protected void sendHTTPRequest(PrintStream printWriter, RequestMethods method, String payload) throws Exception {

        printWriter.print(method + " " + Constants.HTTPCORE_API_CONTEXT + " HTTP/1.1" + Constants.CRLF);
        printWriter.print("Content-Type: application/json" + Constants.CRLF);
        printWriter.print("Accept: application/json" + Constants.CRLF);
        printWriter.print("Connection: keep-alive" + Constants.CRLF);
        printWriter.print("Transfer-Encoding: chunked" + Constants.CRLF);
        printWriter.print(Constants.CRLF);

        InputStream payloadStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));

        int chunkSize = 100;
        int count;
        byte[] buffer = new byte[chunkSize];

        while ((count = payloadStream.read(buffer)) > 0) {
            printWriter.print(count + Constants.CRLF);
            printWriter.write(buffer, 0, count);
            printWriter.print(Constants.CRLF);
            printWriter.flush();
        }

        printWriter.print("0" + Constants.CRLF);
        printWriter.flush();
    }

    @Override
    protected void readHTTPResponse(BufferedReader reader, HttpRequestWithExpectedHTTPSC httpRequest) throws Exception {

        // When a entity enclosing request (POST) is sent, HTTPCore will close the client connection due to the
        // Malformed Chunk exception. Since the body is not processed by HTTPCore for GET request, the client will be
        // receiving a response.
        if (httpRequest.getMethod() == RequestMethods.GET) {
            Assert.assertTrue(reader.readLine().contains(httpRequest.getExpectedHTTPSC()),
                    "A " + httpRequest.getExpectedHTTPSC() + " HTTP Status");
        }
    }
}
