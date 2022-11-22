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

package org.wso2.micro.integrator.http.backend.test;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.http.utils.BackendServer;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HTTPRequestWithBackendResponse;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.micro.integrator.http.utils.Constants.CRLF;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_VERSION;

/**
 * Test case for MI behaviour(specifically CPU usage) when a HTTP response with content length header greater
 * than the actual body size is received.
 */
public class ContentLengthGreaterThanResponseSizeBackendTestCase extends HTTPCoreBackendTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when the content length header is greater than the the size of the backend " +
                    "response.",
            dataProvider = "httpRequestResponse", dataProviderClass = Constants.class)
    public void testContentLengthGreaterThanResponsePayloadSize(
            HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception {

        invokeHTTPCoreBETestAPI(httpRequestWithBackendResponse);
    }

    @Override
    protected List<BackendServer> getBackEndServers() throws Exception {

        List<BackendServer> serverList = new ArrayList<>();
        serverList.add(new ContentLengthGreaterThanBodyBackend(getServerSocket(true)));
        serverList.add(new ContentLengthGreaterThanBodyBackend(getServerSocket(false)));

        return serverList;
    }

    @Override
    protected boolean validateResponse(CloseableHttpResponse response,
                                       HTTPRequestWithBackendResponse httpRequestWithBackendResponse) throws Exception {

        assertHTTPStatusCodeEquals200(response);
        return true;
    }

    private static class ContentLengthGreaterThanBodyBackend extends BackendServer {

        public ContentLengthGreaterThanBodyBackend(ServerSocket serverSocket) {

            super(serverSocket);
        }

        @Override
        protected void writeOutput(Socket socket) throws Exception {

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            out.write(HTTP_VERSION + " 200 OK" + CRLF);
            out.write("Content-Type: application/json" + CRLF);
            if (StringUtils.isNotBlank(payload)) {
                out.write("Content-Length:  " + (payload.getBytes().length + 200) + CRLF);
            }
            out.write("Connection: keep-alive" + CRLF);
            out.write(CRLF);
            if (StringUtils.isNotBlank(payload)) {
                out.write(payload);
            }
            out.flush();
            socket.close();
        }
    }
}
