/**
 Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.wso2.carbon.esb.car.deployment.test;

import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.data.xsd.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public class RegistryResourceDeploymentTestCase extends ESBIntegrationTest {

    private LogViewerClient logViewer;
    @BeforeClass(alwaysRun = true)
    protected void uploadRegistryResourceCarFileTest() throws Exception {
        super.init();
        deployCar();
        logViewer = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }
    @Test(groups = "wso2.esb", enabled = true, description = "Test registry resources with properties are deployed " +
            "with a CAPP")
    public void testRegistryResourcePropertiesDeployed() throws Exception {
        logViewer.clearLogs();
        String proxyHttpUrl = getProxyServiceURLHttp("readRegistryProperty");
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        deployCar();
        Awaitility.await()
                .pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(60, TimeUnit.SECONDS)
                .until(isProxyExist());
        HttpResponse httpResponse = simpleHttpClient.doGet(proxyHttpUrl, headers);
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        LogEvent[] logs = logViewer.getAllRemoteSystemLogs();
        int afterLogSize = logs.length;
        boolean proxyhostEntryFound = false;
        for (int i = 0; i < afterLogSize; i++) {
            if (logs[i].getMessage().contains("regValue")) {
                proxyhostEntryFound = true;
                break;
            }
        }
        assertTrue(proxyhostEntryFound);
    }

    private Callable<Boolean> isProxyExist() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return esbUtils.isProxyServiceExist(contextUrls.getBackEndUrl(), sessionCookie, "readRegistryProperty");
            }
        };
    }

    @AfterTest(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {
        super.cleanup();
    }

    private void deployCar() throws Exception {
        String cAppPath =
                Paths.get(getESBResourceLocation(), "car", "regresCompositeExporter_1.0.0-SNAPSHOT.car").toString();
        uploadCapp("regresCompositeExporter_1.0.0-SNAPSHOT.car",
                new DataHandler(new FileDataSource(new File( cAppPath))));
    }
}
