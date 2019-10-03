/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
/*
package org.wso2.carbon.esb.proxyservice.test.proxyservices;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.esb.integration.common.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

*/
/**
 * This test class will test the Proxy Service deployment  when pinnedService parameter value does
 * not contain current instance name
 * https://wso2.org/jira/browse/ESBJAVA-4540
 *//*

public class ESBJAVA4540PinnedServerParameterTestCase extends ESBIntegrationTest {

    private final String proxyServiceName = "pinnedServerProxy";
    private final String proxyServiceNameEditProxy = "EditProxyWithPinnedServer";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Deploying proxy when the pinnedServer is having another instance name")
    public void deployProxyService() throws Exception {
        CarbonLogReader logReader = new CarbonLogReader();
        logReader.start();

        SimpleHttpClient client = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String endpoint = "https://localhost:9354/management/proxy-services?proxyServiceName=proxyWithPinnedServer";

        HttpResponse response = client.doGet(endpoint, headers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 404, "Proxy service got deployed");

        logReader.stop();
    }

    @Test(groups = "wso2.esb", description = "Editing a proxy service when the pinnedServer is having"
            + " another instance name", enabled = false)
    public void modifyProxyService() throws Exception {
        ProxyServiceAdminClient proxyAdmin = new ProxyServiceAdminClient(contextUrls.getBackEndUrl(),
                getSessionCookie());
        ProxyData proxyData = proxyAdmin.getProxyDetails(proxyServiceNameEditProxy);
        proxyData.setPinnedServers(new String[] { "invalidPinnedServer" });

        LogViewerClient logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
        logViewerClient.clearLogs();

        proxyAdmin.updateProxy(proxyData);

        LogEvent[] logEvents = logViewerClient.getAllRemoteSystemLogs();
        boolean isLogMessageFound = false;

        for (LogEvent log : logEvents) {
            if (log != null && log.getMessage().contains(
                    "not in pinned servers list. Not deploying " + "Proxy service : EditProxyWithPinnedServer")) {
                isLogMessageFound = true;
                break;
            }
        }
        Assert.assertTrue(isLogMessageFound, "Log message not found in the console log");
        //proxy service should not be deployed since the pinnedServer does not contain this server name
        Assert.assertFalse(esbUtils.isProxyDeployed(contextUrls.getBackEndUrl(), getSessionCookie(), proxyServiceName),
                "Proxy service deployed successfully");
    }
}
*/
