/*
 *Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ei.dataservice.integration.test.dssCallMediator;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class DSSCallWithInboundEPTestCase extends ESBIntegrationTest {

    CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "DSS Call Mediator is used inside an Inbound EP with Source Type as Body")
    public void SourceTypeBodyWithInboundEPTestCase() throws Exception {
        carbonLogReader.clearLogs();
        // invoking Inbound EP
        axis2Client.sendSimpleStockQuoteRequest("http://localhost:8081", null, "IBM");

        Assert.assertTrue(carbonLogReader.checkForLog(
                "Employee entry added to the database successfully", DEFAULT_TIMEOUT));
    }

    @AfterTest(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }
}
