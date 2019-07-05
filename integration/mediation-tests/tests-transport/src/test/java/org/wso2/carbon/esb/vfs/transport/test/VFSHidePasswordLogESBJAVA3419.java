package org.wso2.carbon.esb.vfs.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.Utils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * This test class related to - https://wso2.org/jira/browse/ESBJAVA-3419
 * This class test whether the password is printed in the log while
 * exception happens in vfs.
 */

public class VFSHidePasswordLogESBJAVA3419 extends ESBIntegrationTest {

    private CarbonLogReader logReader;
    private static String PROXY1_NAME = "HidePasswordListenerProxy";
    private static String PROXY2_NAME = "HidePasswordSenderProxy";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        logReader = new CarbonLogReader();
        logReader.start();
        deployArtifacts();
    }

    @AfterClass(alwaysRun = true)
    public void restoreServerConfiguration() throws Exception {
        logReader.stop();
        Utils.undeploySynapseConfiguration(PROXY1_NAME, Utils.ArtifactType.PROXY, false);
        Utils.undeploySynapseConfiguration(PROXY2_NAME, Utils.ArtifactType.PROXY, true);

    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Checking VFSTransportListener not logs the clear password on error")
    public void testVFSListenerHidePasswordInLog() throws Exception {

        Assert.assertFalse(logReader.checkForLog("ClearPassword", DEFAULT_TIMEOUT),
                " The password is getting printed in the log in the VFSTransportListener.");
        logReader.clearLogs();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Checking VFSTransportSender not logs the clear password on error",
            dependsOnMethods = "testVFSListenerHidePasswordInLog")
    public void testVFSSenderHidePasswordInLog() throws Exception {
        try {
            OMElement response = axis2Client
                    .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("HidePasswordSenderProxy"),
                            getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        } catch (AxisFault e) {
            // Ignore exception
        }

        Assert.assertFalse(logReader.checkForLog("ClearPassword", DEFAULT_TIMEOUT), " The password is getting printed in the log VFSTransportSender.");
    }


    private void deployArtifacts() throws XMLStreamException, IOException {

        OMElement proxy1 = AXIOMUtil.stringToOM(
                "<proxy xmlns=\"http://ws.apache.org/ns/synapse\"\n" + "       name=\"HidePasswordListenerProxy\"\n"
                        + "       transports=\"vfs\"\n" + "       statistics=\"disable\"\n"
                        + "       trace=\"disable\"\n" + "       startOnLoad=\"true\">\n" + "   <target>\n"
                        + "      <outSequence>\n" + "         <property name=\"transport.vfs.ReplyFileName\"\n"
                        + "                   expression=\"fn:concat(fn:substring-after(get-property('MessageID'), 'urn:uuid:'), '.xml')\"\n"
                        + "                   scope=\"transport\"/>\n"
                        + "         <property name=\"OUT_ONLY\" value=\"true\"/>\n" + "         <send>\n"
                        + "            <endpoint>\n"
                        + "               <address uri=\"vfs:smb://username:ClearPassword@localhost/test/out\"/>\n"
                        + "            </endpoint>\n" + "         </send>\n" + "      </outSequence>\n"
                        + "      <endpoint>\n"
                        + "         <address uri=\"http://localhost:9000/services/SimpleStockQuoteService\"\n"
                        + "                  format=\"soap12\"/>\n" + "      </endpoint>\n" + "   </target>\n"
                        + "   <publishWSDL uri=\"file:samples/service-bus/resources/proxy/sample_proxy_1.wsdl\"/>\n"
                        + "   <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n"
                        + "   <parameter name=\"transport.PollInterval\">1</parameter>\n"
                        + "   <parameter name=\"transport.vfs.MoveAfterProcess\">vfs:smb://username:ClearPassword@localhost/test/original</parameter>\n"
                        + "   <parameter name=\"transport.vfs.FileURI\">vfs:smb://username:ClearPassword@localhost/test/out</parameter>\n"
                        + "   <parameter name=\"transport.vfs.MoveAfterFailure\">vfs:smb://username:ClearPassword@localhost/test/original</parameter>\n"
                        + "   <parameter name=\"transport.vfs.FileNamePattern\">.*\\.text</parameter>\n"
                        + "   <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                        + "   <parameter name=\"transport.vfs.ActionAfterFailure\">MOVE</parameter>\n"
                        + "   <parameter name=\"ScenarioID\">scenario1</parameter>\n" + "   <description/>\n"
                        + "</proxy>");

        OMElement proxy2 = AXIOMUtil.stringToOM(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "    <proxy name=\"HidePasswordSenderProxy\"\n"
                        + "           xmlns=\"http://ws.apache.org/ns/synapse\""
                        + "           transports=\"https http\"\n" + "           startOnLoad=\"true\"\n"
                        + "           trace=\"disable\">\n" + "        <target>\n" + "            <inSequence>\n"
                        + "                <header name=\"To\" value=\"vfs:smb://username:ClearPassword@localhost/test/out\"/>"
                        + "                <property name=\"OUT_ONLY\" value=\"true\"/>\n"
                        + "                <property name=\"FORCE_SC_ACCEPTED\" value=\"true\" scope=\"axis2\"/>\n"
                        + "                <send>\n" + "                    <endpoint>\n"
                        + "                        <default trace=\"disable\" format=\"pox\">\n"
                        + "                            <timeout>\n"
                        + "                                <duration>1000</duration>\n"
                        + "                                <responseAction>discard</responseAction>\n"
                        + "                            </timeout>\n"
                        + "                            <suspendOnFailure>\n"
                        + "                                <initialDuration>0</initialDuration>\n"
                        + "                                <progressionFactor>1.0</progressionFactor>\n"
                        + "                                <maximumDuration>0</maximumDuration>\n"
                        + "                            </suspendOnFailure>\n" + "                        </default>\n"
                        + "                    </endpoint>\n" + "                </send>\n"
                        + "            </inSequence>\n" + "            <outSequence>\n" + "                <drop/>\n"
                        + "            </outSequence>\n" + "            <faultSequence/>\n" + "        </target>\n"
                        + "    </proxy>");


        Utils.deploySynapseConfiguration(proxy1, PROXY1_NAME, Utils.ArtifactType.PROXY, false);
        Utils.deploySynapseConfiguration(proxy2, PROXY2_NAME, Utils.ArtifactType.PROXY, true);
    }
}
