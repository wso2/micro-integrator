/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.synapse.common.cors;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Test api functionality when cors enabled. https://github.com/wso2/micro-integrator/issues/1650
 */
public class TestApiWithCorsEnabled extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    private SimpleHttpClient client;
    private Map<String, String> headers;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        client = new SimpleHttpClient();
        headers = new HashMap<>();
        headers.put("Accept", "application/json");
        serverConfigurationManager = new ServerConfigurationManager(
                new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
    }

    @Test(groups = { "wso2.esb" },
            description = "Test api with cors enabled")
    public void testApiWithCorsEnabled() throws Exception {

        // enable cors
        serverConfigurationManager.applyMIConfigurationWithRestart(
                new File(getESBResourceLocation() + File.separator + "cors" + File.separator + "deployment.toml"));
        String payload = "{\"hello\":\"world\"}";
        HttpResponse response = client.doPost(getApiInvocationURL("cors-api") + "/cors-api", headers, payload,
                                              "application/json");
        String responsePayload = client.getResponsePayload(response);
        Assert.assertEquals(responsePayload, payload, "Response not received when cors enabled.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        super.cleanup();
        serverConfigurationManager.restoreToLastConfiguration();
    }

}
