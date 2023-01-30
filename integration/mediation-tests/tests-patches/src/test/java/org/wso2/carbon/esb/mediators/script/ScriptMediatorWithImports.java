/*
 * Copyright (c) 2023, WSO2 LLC (http://www.wso2.com).
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

package org.wso2.carbon.esb.mediators.script;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class ScriptMediatorWithImports extends ESBIntegrationTest {

    private final Map<String, String> httpHeaders = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        httpHeaders.put("Content-Type", "application/json");
    }

    @Test(groups = {"wso2.esb"}, description = "Testing the Script Mediator importClass method")
    public void testScriptMediatorWithImports() throws Exception {

        String payload = "{\n" +
                "   \"sessionId\":\"QwWsHJyTPW.1pd0_jXlNKOSU\"\n" +
                "}";

        HttpResponse response = HttpRequestUtil.doPost(
                new URL(getApiInvocationURL("ScriptMediatorWithImportsTestAPI")), payload, httpHeaders);
        assertTrue(response.getData().contains("UUID"), "Fault: Script Mediator failed to response with expected " +
                "payload.");
    }
}
