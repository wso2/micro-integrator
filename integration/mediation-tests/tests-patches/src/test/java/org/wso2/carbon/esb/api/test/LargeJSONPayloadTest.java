/**
 Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.

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

package org.wso2.carbon.esb.api.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonServerExtension;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.FileManager;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;
import org.apache.http.HttpResponse;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.File;
import java.nio.file.Paths;

/**
 * Test api invocation with large payload when force json validation is set to true.
 */
public class LargeJSONPayloadTest extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    private void initialize() throws Exception {
        super.init();
        serverConfigurationManager = new ServerConfigurationManager(
                new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
    }

    @Test(groups = { "wso2.esb" },
            description = "Testing api invocation with large json payload")
    public void largeJsonPayloadTest() throws Exception {
        // Copy the passthru property file to the conf folder.
        String passthruFileNameTarget = "passthru-http.properties";
        String passthruFileNameSource = "passthru-http-with-force-json-validation.properties";

        // Get conf folder
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String conf = Paths.get(carbonHome, "conf").toString();

        String targetFilePath = conf +  File.separator + passthruFileNameTarget;
        String sourceFilePath = "/artifacts/ESB/config/" + passthruFileNameSource;
        File targetFile = new File(targetFilePath);
        boolean fileExists = false;
        if (targetFile.exists()) {
            fileExists = true;
            serverConfigurationManager.applyConfigurationWithoutRestart(
                    new File(getClass().getResource(sourceFilePath).toURI()), new File(targetFilePath), true);
        } else {
            serverConfigurationManager.applyConfigurationWithoutRestart(
                    new File(getClass().getResource(sourceFilePath).toURI()), new File(targetFilePath), false);
        }
        CarbonServerExtension.restartServer();
        String jsonLargePayloadPath = "/artifacts/ESB/json/LargePayload.json";
        String jsonLargePayload = FileManager.readFile(getClass().getResource(jsonLargePayloadPath).getPath());
        String serviceURL = getApiInvocationURL("large/json");
        SimpleHttpClient httpClient = new SimpleHttpClient();
        HttpResponse response = httpClient.doPost(serviceURL, null, jsonLargePayload,"application/json");
        String responseString = httpClient.getResponsePayload(response);
        Assert.assertEquals(responseString, jsonLargePayload);
        if (fileExists) {
            serverConfigurationManager.restoreToLastConfiguration(true);
        } else {
            FileManager.deleteFile(targetFilePath);
        }
        CarbonServerExtension.restartServer();
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {
        super.cleanup();
    }
}
