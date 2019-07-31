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

package org.wso2.carbon.esb.mediator.test.clone;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/*
 * This tests tests endpoints from governors registry and configuration registry
 * for the clone mediator
 */

public class CloneFunctionalContextTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(groups = "wso2.esb")
    public void setEnvironment() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
    }

    @Test(groups = "wso2.esb", description = "Tests SEQUENCES from  the governance registry and configuration registry")
    public void testSequence() throws Exception {
        carbonLogReader.start();

        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("CloneFunctionalContextTestProxy"), null, "IBM");
        Assert.assertNotNull(response);

        Assert.assertTrue(carbonLogReader.checkForLog("REQUEST PARAM VALUE", DEFAULT_TIMEOUT));
        carbonLogReader.stop();
    }
}
