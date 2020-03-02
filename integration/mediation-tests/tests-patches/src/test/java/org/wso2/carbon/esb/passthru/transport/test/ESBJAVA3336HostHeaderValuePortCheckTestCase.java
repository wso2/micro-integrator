/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.passthru.transport.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;

import static org.testng.Assert.assertFalse;

public class ESBJAVA3336HostHeaderValuePortCheckTestCase extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        init();
        // Set HTTP wire logs to DEBUG level
        configureHTTPWireLogs("DEBUG");
        carbonLogReader = new CarbonLogReader();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.esb",
            description = "Test wrong port(80) attached with the HOST_HEADERS for https backend")
    public void testHOST_HEADERPropertyTest() throws Exception {

        carbonLogReader.start();
        try {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("ESBJAVA3336httpsBackendProxyService"), null,
                                                    "WSO2");
        } catch (Exception e) {

        }
        boolean errorLogFound = false;
        if (!carbonLogReader.checkForLog("Host: google.com", DEFAULT_TIMEOUT) || carbonLogReader
                .checkForLog("Host: google.com:80", 1)) {
            errorLogFound = true;
        }
        assertFalse(errorLogFound, "Port 80 should not append to the Host header");
    }

    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {
        carbonLogReader.stop();
        // Disable HTTP wire logs
        configureHTTPWireLogs("OFF");
    }

}
