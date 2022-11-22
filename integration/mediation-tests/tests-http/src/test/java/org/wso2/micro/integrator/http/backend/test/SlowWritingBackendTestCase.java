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
import org.wso2.micro.integrator.http.utils.MultiThreadedHTTPClient;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.wso2.micro.integrator.http.utils.Constants.CRLF;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_VERSION;
import static org.wso2.micro.integrator.http.utils.Utils.getPayload;

/**
 * Test case for MI behaviour(specifically CPU usage) when a slow writing backend sends a HTTP response.
 */
public class SlowWritingBackendTestCase extends HTTPCoreBackendTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when a slow writing backend sends a HTTP response.",
            dataProvider = "httpRequestResponse", dataProviderClass = Constants.class)
    public void testSlowWritingBackend(HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception {

        invokeHTTPCoreBETestAPI(httpRequestWithBackendResponse);
    }

    @Override
    protected boolean validateResponse(CloseableHttpResponse response,
                                       HTTPRequestWithBackendResponse httpRequestWithBackendResponse) throws Exception {

        assertHTTPStatusCodeEquals200(response);

        assertEquals(MultiThreadedHTTPClient.getResponsePayload(response).getBytes().length,
                getPayload(httpRequestWithBackendResponse.getBackendResponse().getBackendPayloadSize())
                        .getBytes().length,
                "Response size mismatch");
        return true;
    }

    @Override
    protected List<BackendServer> getBackEndServers() throws Exception {

        List<BackendServer> serverList = new ArrayList<>();
        serverList.add(new SlowWritingBackend(getServerSocket(true)));
        serverList.add(new SlowWritingBackend(getServerSocket(false)));

        return serverList;
    }

    private static class SlowWritingBackend extends BackendServer {

        public SlowWritingBackend(ServerSocket serverSocket) {

            super(serverSocket);
        }

        @Override
        protected void writeOutput(Socket socket) throws Exception {

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            StringBuilder sb = new StringBuilder();

            sb.append(HTTP_VERSION + " 200 OK" + CRLF);
            sb.append("Content-Type: application/json" + CRLF);
            if (StringUtils.isNotBlank(payload)) {
                sb.append("Content-Length:  " + payload.getBytes().length + CRLF);
            }
            sb.append("Connection: keep-alive" + CRLF);
            sb.append(CRLF);
            if (StringUtils.isNotBlank(payload)) {
                sb.append(payload);
            }
            for (int i = 0; i < sb.length(); ++i) {
                out.write(sb.charAt(i));
                out.flush();
                if (i % 500 == 0) {
                    Thread.sleep(100);
                }
            }
            out.flush();
            socket.close();
        }
    }
}
