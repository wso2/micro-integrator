/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.jms.transport.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

public class JMSOutOnlyTestCase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Test proxy service with out-only jms transport")
    public void testJMSProxy() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        String payload = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
                + "   <soapenv:Header/>" + "   <soapenv:Body>" + "      <ser:placeOrder>" + "         <ser:order>"
                + "            <xsd:price>100</xsd:price>" + "            <xsd:quantity>2000</xsd:quantity>"
                + "            <xsd:symbol>JMSTransport</xsd:symbol>" + "         </ser:order>"
                + "      </ser:placeOrder>" + "   </soapenv:Body>" + "</soapenv:Envelope>";

        client.sendRobust(AXIOMUtil.stringToOM(payload), contextUrls.getServiceUrl() + "/MainProxy", "placeOrder");
        client.sendRobust(AXIOMUtil.stringToOM(payload), contextUrls.getServiceUrl() + "/MainProxy", "placeOrder");
        client.sendRobust(AXIOMUtil.stringToOM(payload), contextUrls.getServiceUrl() + "/MainProxy", "placeOrder");

        Thread.sleep(60000); //wait until all message received to jms proxy
        client.sendRobust(AXIOMUtil.stringToOM(payload), contextUrls.getServiceUrl() + "/EndLogProxy", "placeOrder");

        String logMessage = carbonLogReader.getSubstringBetweenStrings("Expiring message ID",
                "dropping message after global timeout of : 120 seconds", 60);

        Assert.assertFalse(!logMessage.isEmpty(),
                "Unnecessary Call Back Registered. Log message found > " + logMessage);

        carbonLogReader.stop();
    }
}
