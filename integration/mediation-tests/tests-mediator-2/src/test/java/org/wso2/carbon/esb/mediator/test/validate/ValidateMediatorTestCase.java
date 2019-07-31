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
package org.wso2.carbon.esb.mediator.test.validate;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;

public class ValidateMediatorTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        // Initialize ESBMediatorTest
        super.init();
    }

    /*https://wso2.org/jira/browse/STRATOS-2297*/
    @Test(groups = "wso2.esb")
    public void validateMediatorDynamicSchemaTest() throws Exception {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                getProxyServiceURLHttp("validateMediatorDynamicSchemaFromConfigRegTestProxy"),
                getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");

        Assert.assertTrue(response.toString().contains("GetQuoteResponse"), "GetQuoteResponse not found in response");
        Assert.assertTrue(response.toString().contains("WSO2 Company"), "GetQuoteResponse not found in response");
    }
}
