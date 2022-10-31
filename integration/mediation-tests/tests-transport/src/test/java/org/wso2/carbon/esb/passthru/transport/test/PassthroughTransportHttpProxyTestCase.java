/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class PassthroughTransportHttpProxyTestCase extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;
    CarbonLogReader carbonLogReader = new CarbonLogReader();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        serverConfigurationManager = new ServerConfigurationManager(new AutomationContext());
        serverConfigurationManager.applyMIConfigurationWithRestart(new File(
                getESBResourceLocation() + File.separator + "passthru" + File.separator + "transport" + File.separator
                        + "httpproxy" + File.separator + "deployment.toml"));
        super.init();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Passthrough Transport Http.proxy test case")
    public void passthroughTransportHttpProxy() throws Exception {

        try {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("PassthroughTransportHttpTestProxy"), "", "IBM");
        } catch (AxisFault expected) {
            //read timeout expected
        }

        // This test case is supposed to test the pass-through server by sending a request to a pass-through proxy.
        // We need to improve this test to send a message to the pass-through server and validate the request. Since
        // there is a difference in printing, IP address with host, and port  with JDK 17, the host and port resolution
        // differs from the original we need to check both for building in JDK 11 and 17
        assertTrue(carbonLogReader.checkForLog("111.wso2.com", DEFAULT_TIMEOUT) &&
                        carbonLogReader.checkForLog("7777", DEFAULT_TIMEOUT),
                "The log message with http proxy host was not found in passthroughTransportHttpProxy testcase.");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        serverConfigurationManager.restoreToLastMIConfiguration();
        carbonLogReader.stop();
    }
}
