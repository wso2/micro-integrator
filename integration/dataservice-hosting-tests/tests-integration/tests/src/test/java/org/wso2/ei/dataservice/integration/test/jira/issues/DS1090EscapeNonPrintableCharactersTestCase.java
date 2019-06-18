package org.wso2.ei.dataservice.integration.test.jira.issues;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import javax.xml.xpath.XPathExpressionException;

/**
 * This test case is to verify https://wso2.org/jira/browse/DS-1090,
 * to validate escape non printable characters with null values
 */

public class DS1090EscapeNonPrintableCharactersTestCase extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(DS1090EscapeNonPrintableCharactersTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
    }

    @Test(groups = { "wso2.dss" })
    public void testForNullResultSet() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("select_all_Customers_operation", omNs);
        OMElement result = null;
        try {
            String serviceName = "EscapeNonPrintableCharactersTest";
            result = new AxisServiceClient()
                    .sendReceive(payload, getServiceUrlHttp(serviceName), "select_all_Customers_operation");
        } catch (XPathExpressionException e) {
            log.info("EscapeNonPrintableCharactersTestCase failed ", e);
        }
        Assert.assertNotNull(result, "Response message null ");
    }

}
