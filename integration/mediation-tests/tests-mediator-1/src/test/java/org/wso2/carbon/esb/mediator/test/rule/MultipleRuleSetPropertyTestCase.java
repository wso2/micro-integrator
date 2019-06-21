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
package org.wso2.carbon.esb.mediator.test.rule;

import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class MultipleRuleSetPropertyTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "scenario with multiple rules- Invoke IBM rule")
    public void testInvokeIBMRule() throws AxisFault {
        OMElement response = sendRequest("IBM");

        String lastPrice = response.getFirstElement()
                .getFirstChildWithName(new QName("http://services.samples/xsd", "last")).getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        String symbol = response.getFirstElement()
                .getFirstChildWithName(new QName("http://services.samples/xsd", "symbol")).getText();
        assertEquals(symbol, "IBM", "Fault: value 'symbol' mismatched");
    }

    @Test(groups = "wso2.esb", description = "scenario with multiple rules- Invoke SUN rule ")
    public void testInvokeSUNRule() throws Exception {
        OMElement response = sendRequest("SUN");
        assertTrue(response.toString().contains("WRONG_RULE"), "Fault: value 'responseText' mismatched");
    }

    @Test(groups = "wso2.esb", description = "scenario with multiple rules- Invoke MFST rule ")
    public void testInvokeMSFTRule() throws Exception {

        OMElement response = sendRequest("MFST");
        assertTrue(response.toString().contains("WRONG_RULE"), "Fault: value 'responseText' mismatched");
    }

    @Test(groups = "wso2.esb", description = "scenario with multiple rules- Invoke an invalid rule ")
    public void testInvokeInvalidRule() throws Exception {
        try {
            sendRequest("Invalid");
            fail();
        } catch (AxisFault axisFault) {
            assertEquals(axisFault.getMessage(), ESBTestConstant.INCOMING_MESSAGE_IS_NULL,
                    "Fault: value mismatched, should be 'The input stream for an incoming message is null.'");
        }
    }

    private OMElement sendRequest(String symbol) throws AxisFault {
        return axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("MultipleRuleSetPropertyTestProxy"),
                "http://localhost:9000/services/SimpleStockQuoteService", symbol);
    }
}
