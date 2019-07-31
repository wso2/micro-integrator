package org.wso2.carbon.esb.mediator.test.property;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import static org.testng.Assert.assertTrue;

/**
 * This test case tests the setting of properties
 * from the registries
 */
public class PropertyIntegrationRegistryValuesTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Set value from config registry (default scope)")
    public void testConfVal() throws Exception {
        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("propertyConfRegistryTestProxy"), null,
                        "Random Symbol");
        assertTrue(response.toString().contains("Config Reg Test String"), "Property Not Set");
    }

    @Test(groups = "wso2.esb", description = "Set value from goverance registry (default scope)")
    public void testGovVal() throws Exception {
        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("propertyGovRegistryTestProxy"), null,
                        "Random Symbol");
        assertTrue(response.toString().contains("Gov Reg Test String"), "Property Not Set");
    }
}
