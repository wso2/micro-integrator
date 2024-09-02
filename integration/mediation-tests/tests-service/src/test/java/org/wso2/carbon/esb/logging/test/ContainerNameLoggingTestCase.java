/*
 *Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.esb.logging.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.io.File;

public class ContainerNameLoggingTestCase extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private CarbonLogReader carbonLogReader;
    private final String SOURCE_DIR =
            TestConfigurationProvider.getResourceLocation(ESBTestConstant.ESB_PRODUCT_GROUP) + File.separator
                    + "car" + File.separator;

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

        String cAppFile = SOURCE_DIR + "HealthCareCompositeExporter_1.0.0.car";
        File file = new File(cAppFile);
        deployCarbonApplication(file);
    }

    @Test(groups = {"wso2.esb"}, description = "Test whether the artifact container name exists in logs")
    public void containerNameLogging() throws Exception {

        boolean isLogExists = carbonLogReader.checkForLog("Deployed From Artifact Container: " +
                    "HealthCareCompositeExporter", DEFAULT_TIMEOUT);
        Assert.assertTrue(isLogExists, "The artifact container name is not available in logs");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
        serverConfigurationManager.restoreToLastMIConfiguration();
    }

}
