package org.wso2.carbon.esb.scenarios.test.healthcare;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import static org.testng.Assert.assertTrue;

public class HealthCareScenarioTestCase extends ESBIntegrationTest {
    AxisServiceClient a2Client = new AxisServiceClient();
    SampleAxis2Server axis2Server1;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        axis2Server1 = new SampleAxis2Server("test_axis2_server_9009.xml");
        axis2Server1.start();
        axis2Server1.deployService("geows");
        axis2Server1.deployService("hcfacilitylocator");
        axis2Server1.deployService("hcinformationservice");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Health Care Scenario Test Case")
    public void testScenario() throws IOException, XMLStreamException {

        OMElement requestXML = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
                + "                          xmlns:heal=\"http://healthcare.wso2\">\n" + "            <soapenv:Body>\n"
                + "                <heal:getHealthcareCenterInfo>\n"
                + "                    <heal:longitude>34.3</heal:longitude>\n"
                + "                    <heal:latitude>-43.2</heal:latitude>\n"
                + "                </heal:getHealthcareCenterInfo>\n" + "            </soapenv:Body>\n"
                + "</soapenv:Envelope>");

        String proxyServiceEP = getProxyServiceURLHttp("HCCProxyService");
        OMElement response = a2Client.sendReceive(requestXML, proxyServiceEP, "getHealthcareCenterInfo");
        assertTrue(response.getLocalName() == "getHCCenterInfoResponse" ? this.countSiblings(response, 1) == 5 : false,
                "Received 5 aggregated HCCenterInfoResponse elements in response.");
        //System.out.println("==== Response: " + response.toString());

    }

    /**
     * This is a recursive function to count the number of siblings.
     * This is a little confusing, but what it does is traverse through the
     * siblings (getNextOMSibling) by checking to see there is a next sibling.
     * If there is, it sets the count to 1, and calls itself again. When it reaches
     * no next sibling, it returns 1 i.e. the last sibling). Then the method stack
     * should return adding up each of the 1's.
     *
     * @param sibling OMNode
     * @param count   initial count (i.e. first sibling, this should always be 1).
     * @return total sibling count.
     */
    private int countSiblings(OMNode sibling, int count) {
        if (sibling.getNextOMSibling() != null) {
            count = 1; // I am a sibling.
            return count + countSiblings(sibling.getNextOMSibling(), 1);
        } else {
            return 1;  // I am the last sibling.
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        a2Client = null;
        axis2Server1.stop();
        super.cleanup();
    }
}
