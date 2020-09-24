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

package org.wso2.ei.dataservice.integration.test.dssCallMediator;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class TargetTypePropertyTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        verifyProxyServiceExistence("targetTypePropertyProxy");
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = {"wso2.esb"}, description = "Test DSS Call mediator when target type is set to property")
    public void testDSSCallMediatorTargetProperty() throws Exception {
        carbonLogReader.clearLogs();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/xml");
        simpleHttpClient.doGet(getProxyServiceURLHttp("dssCallMediatorTargetTypePropertyProxy"), headers);

        boolean responseStatus = carbonLogReader.checkForLog("SUCCESSFUL", DEFAULT_TIMEOUT);
        assertTrue(responseStatus, "Response payload not found in property.");
    }

    @AfterClass
    public void cleanUp() throws Exception {
        super.cleanup();
    }
}
