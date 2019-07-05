/*
 *Copyright 2005, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.mediator.test.log;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class LogMediatorLevelTest extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Tests level log")
    public void testSendingToDefinedEndpoint() throws Exception {
        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(
                        getProxyServiceURLHttp("logMediatorLevelTestProxy"), null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2"));
        log.info(response);
        Thread.sleep(2000);
    }

    @Test(groups = "wso2.esb", description = "Tests System Logs")
    public void testSystemLogs() throws Exception {
        CarbonLogReader carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();

        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(
                        getProxyServiceURLHttp("logMediatorLevelTestProxy"), null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2"));
        log.info(response);
        boolean logFound = carbonLogReader
                .checkForLog("*****TEST CUSTOM LOGGING MESSAGE TO SYSTEM LOGS TEST*****", DEFAULT_TIMEOUT);
        Assert.assertTrue(logFound, "System Log not found. LogViewer Admin service not working properly");

        carbonLogReader.stop();
    }
}