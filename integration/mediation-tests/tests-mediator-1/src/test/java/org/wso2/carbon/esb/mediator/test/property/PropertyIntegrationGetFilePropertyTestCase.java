/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.mediator.test.property;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * This test the functionality of retrieving a property from file.properties config file
 */
public class PropertyIntegrationGetFilePropertyTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        verifyProxyServiceExistence("propertyMediatorFilePropertyTestProxy");
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = {"wso2.esb"}, description = "Test retrieving a property from file.properties config file")
   public void testFileProperty() throws IOException, InterruptedException {
        carbonLogReader.clearLogs();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        simpleHttpClient.doGet(getProxyServiceURLHttp("propertyMediatorFilePropertyTestProxy"), null);
        boolean responseStatus = carbonLogReader.checkForLog("testFileProperty = PropertyIntegrationGetFilePropertyTestCaseValue", DEFAULT_TIMEOUT);
        assertTrue(responseStatus, "Error retrieving the property from file.properties config file.");
    }
}
