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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.micro.integrator.http.utils.Constants.CRLF;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_VERSION;

public class BackendClosesConnectionWhileSendingBodyTestCase extends HTTPCoreBackendTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when a backend closes the socket while sending response body.",
            dataProvider = "httpRequestResponse", dataProviderClass = Constants.class)
    public void testBackendClosesConnectionWhileSendingBody(
            HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception {

        invokeHTTPCoreBETestAPI(httpRequestWithBackendResponse);
    }

    @Override
    protected List<BackendServer> getBackEndServers() throws Exception {

        List<BackendServer> serverList = new ArrayList<>();
        serverList.add(new CloseConnectionWhileSendingBodyBackend(getServerSocket(true)));
        serverList.add(new CloseConnectionWhileSendingBodyBackend(getServerSocket(false)));

        return serverList;
    }

    @Override
    protected boolean validateResponse(CloseableHttpResponse response,
                                       HTTPRequestWithBackendResponse httpRequestWithBackendResponse) throws Exception {

        // Depending on the underlying connections we may or not receive a response.
        // Hence response validation is ignored.
        return false;
    }

    private static class CloseConnectionWhileSendingBodyBackend extends BackendServer {

        public CloseConnectionWhileSendingBodyBackend(ServerSocket serverSocket) {

            super(serverSocket);
        }

        @Override
        protected void writeOutput(Socket socket) throws Exception {

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            out.write(HTTP_VERSION + " 200 OK" + CRLF);
            out.write("Content-Type: application/json" + CRLF);
            if (StringUtils.isNotBlank(payload)) {
                out.write("Content-Length:  " + payload.getBytes().length + CRLF);
            }
            out.write("Connection: keep-alive" + CRLF);
            out.write(CRLF);
            if (StringUtils.isNotBlank(payload)) {
                BufferedReader bufReader = new BufferedReader(new StringReader(payload));
                String line;
                int count = 0;
                while ((line = bufReader.readLine()) != null) {
                    if (count++ == 20) {
                        break;
                    }
                    out.write(line);
                    out.flush();
                }
            }
            out.flush();
            socket.close();
        }
    }
}
