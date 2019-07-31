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
package org.wso2.carbon.esb.proxyservice.test.customProxy;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;

import static org.testng.Assert.assertEquals;

public class InvokingNonExistingProxyTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

    }

    @Test(groups = "wso2.esb", description = "Invoking Non existing proxy service")
    public void testNonExistingProxyInvocation() {
        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp(
                    "NonExistingProxyService"), null, "WSO2");
            Assert.assertNull(response.getFirstElement());
        } catch (AxisFault fault) {
            assertEquals(fault.getMessage(), ESBTestConstant.NOT_FOUND_ERROR, "Error Message Mismatched");
        }

    }

}
