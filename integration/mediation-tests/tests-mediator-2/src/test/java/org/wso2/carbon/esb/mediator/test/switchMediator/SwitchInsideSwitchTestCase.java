/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.mediator.test.switchMediator;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;

public class SwitchInsideSwitchTestCase extends ESBIntegrationTest {
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Switch Mediator: Testing Switch inside Switch Scenario")
    public void testSample2() throws Exception {
        axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("switchMediatorSwitchInsideSwitchTestProxy"),
                        getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "IBM");
        // Assert Test property of INFO log for "This is Get Quote service"
        // & symbol property of INFO log for "Great stock - IBM"
        Assert.assertTrue(carbonLogReader.checkForLog("This is Get Quote service", DEFAULT_TIMEOUT),
                "Test Property not set");
        Assert.assertTrue(carbonLogReader.checkForLog("Great stock - IBM", DEFAULT_TIMEOUT),
                "Symbol property not set");
        carbonLogReader.stop();
    }
}
