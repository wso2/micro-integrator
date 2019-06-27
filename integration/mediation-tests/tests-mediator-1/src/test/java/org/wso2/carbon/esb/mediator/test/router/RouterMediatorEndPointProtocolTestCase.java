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

package org.wso2.carbon.esb.mediator.test.router;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * Tests for different types of endpoints http,https.JMS.etc
 */

public class RouterMediatorEndPointProtocolTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init();
    }

    @Test(groups = "wso2.esb", description = "Tests for https")
    public void testHTTPS() throws Exception {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                getProxyServiceURLHttps("RouterMediatorEndpointProtocolProxy"), null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2"));
    }
}
