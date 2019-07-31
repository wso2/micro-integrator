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
package org.wso2.carbon.esb.proxyservice.test.wsdlBasedProxy;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.Utils;

import javax.xml.namespace.QName;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ProxyWithPublishSameServiceContractTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        OMElement publishSameServiceContractTestProxy = AXIOMUtil.stringToOM(FileUtils.readFileToString(
                new File(getESBResourceLocation() + File.separator + "proxyconfig" + File.separator +
                        "proxy" + File.separator + "wsdlBasedProxy" + File.separator +
                        "proxy_service_with_publish_same_service_contract.xml")));
        Utils.deploySynapseConfiguration(publishSameServiceContractTestProxy, "publishSameServiceContractTestProxy",
                "proxy-services", true);
    }

    @Test(groups = "wso2.esb", description = "- WSDL based proxy"
            + "- Proxy service with 'Publish Same Service Contract' ")
    public void testWSDLBasedProxy() throws Exception {

        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("publishSameServiceContractTestProxy"),
                        getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE) + "?wsdl",
                        "WSO2");

        String lastPrice = response.getFirstElement()
                .getFirstChildWithName(new QName("http://services.samples/xsd", "last")).getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        String symbol = response.getFirstElement()
                .getFirstChildWithName(new QName("http://services.samples/xsd", "symbol")).getText();
        assertEquals(symbol, "WSO2", "Fault: value 'symbol' mismatched");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        Utils.undeploySynapseConfiguration("publishSameServiceContractTestProxy", "proxy-services", true);
    }

}
