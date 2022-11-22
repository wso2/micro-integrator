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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.http.utils.BackendServer;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HTTPRequestWithBackendResponse;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BackendClosesConnectionAsSoonAsTheRequestReceivedTestCase extends HTTPCoreBackendTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when a backend closes the socket as soon as a request received.",
            dataProvider = "httpRequestResponse", dataProviderClass = Constants.class)
    public void testBackendClosesConnectionAsSoonAsTheRequestReceived(
            HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception {

        invokeHTTPCoreBETestAPI(httpRequestWithBackendResponse);
    }

    @Override
    protected List<BackendServer> getBackEndServers() throws Exception {

        List<BackendServer> serverList = new ArrayList<>();
        serverList.add(new ClosesConnectionAsSoonAsTheRequestReceivedBackend(getServerSocket(true)));
        serverList.add(new ClosesConnectionAsSoonAsTheRequestReceivedBackend(getServerSocket(false)));

        return serverList;
    }

    @Override
    protected boolean validateResponse(CloseableHttpResponse response,
                                       HTTPRequestWithBackendResponse httpRequestWithBackendResponse) throws Exception {

        assertHTTPStatusCodeEquals500(response);
        return true;
    }

    private static class ClosesConnectionAsSoonAsTheRequestReceivedBackend extends BackendServer {

        public ClosesConnectionAsSoonAsTheRequestReceivedBackend(ServerSocket serverSocket) {

            super(serverSocket);
        }

        @Override
        protected void readInput(Socket socket) throws Exception {
            // close the socket
            socket.close();
        }

        @Override
        protected void writeOutput(Socket socket) throws Exception {

        }
    }
}
