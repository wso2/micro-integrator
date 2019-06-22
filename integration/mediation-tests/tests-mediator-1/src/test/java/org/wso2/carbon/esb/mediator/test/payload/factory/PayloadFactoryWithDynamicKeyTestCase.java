package org.wso2.carbon.esb.mediator.test.payload.factory;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.xml.namespace.QName;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

public class PayloadFactoryWithDynamicKeyTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Payload Factory invocation with a key which its format can be saved as a local entry or registry resource")
    public void testInvokeAScriptWithDynamicKey() throws Exception {

        OMElement response = axis2Client
                .sendSimpleQuoteRequest(getProxyServiceURLHttp("PayloadFactoryWithDynamicKeyTestCaseProxy"), null,
                        "WSO2");

        assertEquals(response.getFirstElement().getText(), "IBM", "Fault value mismatched");
        assertNotEquals(response.getFirstElement().getText(), "WSO2", "Fault value mismatched");

        assertNotNull(response.getFirstChildWithName(new QName("http://services.samples/xsd", "Price")),
                "Fault response : doesn't contain element \'Price\'");
        assertNotNull(response.getFirstChildWithName(new QName("http://services.samples/xsd", "Code")),
                "Fault response : doesn't contain element \'Code\'");
    }

}

