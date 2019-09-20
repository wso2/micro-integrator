/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.esb.mediator.test.log;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * THis test case verifies different levels and categories in log mediator
 */
public class LogMediatorLevelsAndCategoryTestCase extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        context = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyMIConfiguration(new File(getESBResourceLocation() + "/other/" + "log4j2.properties"));
        serverConfigurationManager.restartMicroIntegrator();
        init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
        //allow time for log reader to start
        Thread.sleep(1000);

        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("LogMediatorLevelAndCategoryTestProxy"), null,
                        "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2"), "Did not receive the expected response");
    }

    @Test(groups = "wso2.esb", description = "Test debug level log")
    public void testDebugLevelLogs() throws InterruptedException {
        Boolean isDebugLogAvailable = isLogAvailable("*****LOGGING IN DEBUG CATEGORY - CUSTOM LEVEL*****");
        Assert.assertTrue(isDebugLogAvailable, "DEBUG Log not found. Debug logs not working properly");
    }

    /**
     * This test case disabled due to issue reported as "[EI 620][Integration Profile] Trace logs not going to
     * indicate via UI" (https://github.com/wso2/product-ei/issues/1071)
     */
    @Test(groups = "wso2.esb", description = "Test trace level log", enabled = false)
    public void testTraceLevelLogs() throws InterruptedException {
        Boolean isTraceLogAvailable = isLogAvailable("*****LOGGING IN TRACE CATEGORY - CUSTOM LEVEL*****");
        Assert.assertTrue(isTraceLogAvailable, "TRACE Log not found. Trace logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test info level log")
    public void testInfoLevelLogs() throws InterruptedException {
        Boolean isInfoLogAvailable = isLogAvailable("*****LOGGING IN INFO CATEGORY - CUSTOM LEVEL*****");
        Assert.assertTrue(isInfoLogAvailable, "INFO Log not found. Info logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test warn level log")
    public void testWarnLevelLogs() throws InterruptedException {
        Boolean isWarnLogAvailable = isLogAvailable("*****LOGGING IN WARN CATEGORY - CUSTOM LEVEL*****");
        Assert.assertTrue(isWarnLogAvailable, "Warn Log not found. Warn logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test error level log")
    public void testErrorLevelLogs() throws InterruptedException {
        Boolean isErrorLogAvailable = isLogAvailable("*****LOGGING IN ERROR CATEGORY - CUSTOM LEVEL*****");
        Assert.assertTrue(isErrorLogAvailable, "Error Log not found. Error logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test fatal level log")
    public void testFatalLevelLogs() throws InterruptedException{
        Boolean isFatalLogAvailable = isLogAvailable("*****LOGGING IN FATAL CATEGORY - CUSTOM LEVEL*****");
        Assert.assertTrue(isFatalLogAvailable, "Fatal Log not found. Fatal logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test full level log")
    public void testFullLevelLogs() throws  InterruptedException {
        Boolean isFullLogAvailable = isLogAvailable("*****LOGGING AT FULL LEVEL*****");
        Assert.assertTrue(isFullLogAvailable, "Full level logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test Header level log")
    public void testHeaderLevelLogs() throws InterruptedException {
        Boolean isHeaderLogAvailable = isLogAvailable("*****LOGGING AT HEADER LEVEL*****");
        Assert.assertTrue(isHeaderLogAvailable, "Header level logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test simple level log")
    public void testSimpleLevelLogs() throws InterruptedException {
        Boolean isSimpleLogAvailable = isLogAvailable("*****LOGGING AT SIMPLE LEVEL*****");
        Assert.assertTrue(isSimpleLogAvailable, "Simple level logs not working properly");
    }

    @Test(groups = "wso2.esb", description = "Test custom level log")
    public void testCustomLevelLogs() throws InterruptedException{
        Boolean isCustomLogAvailable = isLogAvailable("*****LOGGING AT CUSTOM LEVEL*****");
        Assert.assertTrue(isCustomLogAvailable, "Custom level logs not working properly");
    }

    private boolean isLogAvailable(String validateLog) throws InterruptedException {
        if (carbonLogReader.checkForLog(validateLog, DEFAULT_TIMEOUT)) {
            return true;
        }
        return false;
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
        serverConfigurationManager.restoreToLastMIConfiguration();
    }
}