package org.wso2.carbon.esb.http.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class HttpInboundDispatchTestCase extends ESBIntegrationTest {

    private static final String REQUEST_PAYLOAD = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' >"
            + "<soapenv:Body xmlns:ser='http://services.samples' xmlns:xsd='http://services.samples/xsd'> "
            + "<ser:getQuote> <ser:request> <xsd:symbol>WSO2</xsd:symbol> </ser:request> </ser:getQuote> "
            + "</soapenv:Body></soapenv:Envelope> ";

    private String urlContext = "";

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

        OMElement inbound1 = AXIOMUtil.stringToOM(FileUtils.readFileToString(new File(getESBResourceLocation()
                + File.separator + "http.inbound.transport" + File.separator + "inbound1.xml")));
        Utils.deploySynapseConfiguration(inbound1, "inbound1", "inbound-endpoints", false);

        OMElement inbound2 = AXIOMUtil.stringToOM(FileUtils.readFileToString(new File(getESBResourceLocation()
                + File.separator + "http.inbound.transport" + File.separator + "inbound2.xml")));
        Utils.deploySynapseConfiguration(inbound2, "inbound2", "inbound-endpoints", true);

        urlContext = "http://" + getHostname() + ":" + "9090" + "/";
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();

    }

    @Test(groups = "wso2.esb", description = "Inbound HTTP Super Tenant Sequence Dispatch")
    public void inboundHttpSuperSequenceTest() throws Exception {

        HttpRequestUtil.doPost(new URL(urlContext), REQUEST_PAYLOAD, new HashMap<>());
        //this case matches with the regex but there is no api or proxy so dispatch to  super tenant main sequence
        Assert.assertTrue(carbonLogReader.checkForLog("main sequence executed for call to non-existent = /", DEFAULT_TIMEOUT));
        carbonLogReader.clearLogs();
    }

    @Test(groups = "wso2.esb", description = "Inbound HTTP Super Tenant API Dispatch")
    public void inboundHttpSuperAPITest() throws Exception {
        axis2Client.sendSimpleStockQuoteRequest(urlContext + "foo", null, "WSO2");
        Assert.assertTrue(carbonLogReader.checkForLog("FOO", DEFAULT_TIMEOUT));
        axis2Client.sendSimpleStockQuoteRequest(urlContext + "boo", null, "WSO2");
        Assert.assertTrue(carbonLogReader.checkForLog("BOO", DEFAULT_TIMEOUT));

        /**
         * Test API dispatch to non existent API - this should trigger super tenant main sequence.
         * since this matches with inbound regex but no api or proxy found to be dispatched
         */
        carbonLogReader.clearLogs();
        HttpRequestUtil.doPost(new URL(urlContext + "idontexist"), REQUEST_PAYLOAD, new HashMap<>());
        Assert.assertTrue(carbonLogReader.checkForLog(
                "main sequence executed for call to non-existent = /idontexist", DEFAULT_TIMEOUT));
        carbonLogReader.clearLogs();
    }

    @Test(groups = "wso2.esb", description = "Inbound HTTP Super Tenant Default Main Sequence Dispatch")
    public void inboundHttpSuperDefaultMainTest() throws Exception {
        HttpRequestUtil.doPost(new URL("http://" + getHostname() + ":9091/"), REQUEST_PAYLOAD, new HashMap<>());
        Assert.assertTrue(carbonLogReader.checkForLog("main sequence executed for call to non-existent = /", DEFAULT_TIMEOUT));
        carbonLogReader.clearLogs();
    }

    @Test(groups = "wso2.esb", description = "Inbound HTTP Super Tenant Proxy Dispatch")
    public void inboundHttpSuperProxyDispatchTest() throws Exception {
        axis2Client.sendSimpleStockQuoteRequest(urlContext + "services/HttpInboundDispatchTestProxy", null, "WSO2");
        Assert.assertTrue(carbonLogReader.checkForLog("PROXY_HIT", DEFAULT_TIMEOUT));
        carbonLogReader.clearLogs();
    }

    @AfterTest(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
        Utils.undeploySynapseConfiguration("inbound1", "inbound-endpoints", false);
        Utils.undeploySynapseConfiguration("inbound2", "inbound-endpoints", true);
    }
}
