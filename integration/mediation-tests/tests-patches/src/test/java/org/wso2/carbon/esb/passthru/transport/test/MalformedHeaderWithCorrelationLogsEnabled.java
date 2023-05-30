/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

public class MalformedHeaderWithCorrelationLogsEnabled extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private final String startUpScript = "micro-integrator.sh";
    private final String invalidTransferEncoder = "incorrect-value";
    private final String apiName = "TestAPI";
    private final String method = "POST";
    private final String path = "/TestAPI";
    private String serviceUrl;
    private final String CRLF = "\r\n";
    private int port;

    @BeforeClass
    public void init() throws Exception {
        super.init();
        File newShFile = new File(
                getESBResourceLocation() + File.separator + "passthru" + File.separator + "transport" + File.separator
                        + "enableCorrelation" + File.separator + startUpScript);
        File oldShFile = new File(
                CarbonBaseUtils.getCarbonHome() + File.separator + "bin" + File.separator + startUpScript);
        serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyConfigurationWithoutRestart(newShFile, oldShFile, true);
        serverConfigurationManager.restartMicroIntegrator();

        serviceUrl = contextUrls.getServiceUrl().replace("service", apiName);
        port = new URL(contextUrls.getServiceUrl()).getPort();
    }

    @Test(groups = "wso2.esb",
            description = "Enable correlation logs and send a HTTP POST request with a malformed header.")
    public void TestMalformedHeaderWithCorrelationLogsEnabled() throws Exception {

        // Send HTTP POST request with malformed Transfer-Encoding Header value
        String headers = HttpHeaders.HOST + ": " + hostName + CRLF + HttpHeaders.TRANSFER_ENCODING + ": "
                + invalidTransferEncoder + CRLF;
        Socket socket = new Socket(hostName, port);
        String request = method + " " + path + " " + HttpVersion.HTTP_1_1 + CRLF + headers + CRLF;
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(request.getBytes());
        outputStream.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        StringBuilder postResponse = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            postResponse.append(line).append("\n");
        }
        reader.close();
        outputStream.close();
        socket.close();

        // Send HTTP GET request
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        HttpResponse response = HttpRequestUtil.doGet(serviceUrl, requestHeaders);

        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_ACCEPTED);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
        serverConfigurationManager.restoreToLastConfiguration();
    }
}