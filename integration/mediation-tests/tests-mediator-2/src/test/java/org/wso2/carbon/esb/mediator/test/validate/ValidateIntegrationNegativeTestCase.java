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
import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class ValidateIntegrationNegativeTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = { "wso2.esb" }, description = "Provide invalid dynamic key as shema location")
    public void TestWithInvalidDynamicKey() throws Exception {
        OMElement response = null;
        try {
            response = axis2Client
                    .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("validateMediatorInvalidDynamicKeyTestProxy"),
                            null, "WSO2");
            fail("This Request must throws a AxisFault");
        } catch (AxisFault axisFault) {
            assertEquals(axisFault.getMessage(), "Invalid custom quote request");
        }
    }

    @Test(groups = { "wso2.esb" }, description = "Create validate mediator and specifying an invalid "
            + "XPath expression using \"source\" attribute "
            + "Check how mediator operates on the elements of SOAP body")
    public void TestWithInvalidXpath() throws Exception {
        final String expectedErrorMsg = "Error occurred while accessing source element: //m0:requestElement/m0:getQuote";
        try {
            axis2Client
                    .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("validateMediatorInvalidXPathTestProxy"), null,
                            "WSO2");
        } catch (AxisFault expected) {
            assertEquals(expected.getMessage(), expectedErrorMsg, "Error Message mismatched");
        }
    }
}


