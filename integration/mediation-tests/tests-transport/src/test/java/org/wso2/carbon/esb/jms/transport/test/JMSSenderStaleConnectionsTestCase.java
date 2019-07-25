/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.esb.integration.common.extensions.jmsserver.ActiveMQServerExtension;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

/**
 * This class tests the JMS sender side stale connections handling in unexpected broker shutdowns, when the connection
 * caching is unabled.
 * Initially 15 messages will be sent, which can fill the cached connection map (10) and after a broker restart another
 * message will be sent. If this message get into default fault sequence, the test will be failed.
 */
public class JMSSenderStaleConnectionsTestCase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader = new CarbonLogReader();
    /* this will be printed on default fault sequence when the exception is thrown */
    private final String exceptedErrorLog = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/"
            + "envelope/\"><soapenv:Body><ns:getQuote xmlns:ns=\"http://services.samples\"><ns:request><ns:symbol>"
            + "JMS</ns:symbol></ns:request></ns:getQuote></soapenv:Body></soapenv:Envelope>";

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Test for JMS sender side stale connections handling")
    public void staleConnectionsTestJMSProxy() throws Exception {
        AxisServiceClient client = new AxisServiceClient();

        for (int i = 0; i < 15; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("JMS"),
                    getProxyServiceURLHttp("JMSSenderStaleConnectionsTestProxy"), "getQuote");
        }

        /* restart the JMS broker */
        JMSBrokerController activeMQServer = ActiveMQServerExtension.getJMSBrokerController();
        activeMQServer.stop();
        activeMQServer.start();

        carbonLogReader.clearLogs();
        /* send another message after broker restart */
        client.sendRobust(Utils.getStockQuoteRequest("JMS"),
                getProxyServiceURLHttp("JMSSenderStaleConnectionsTestProxy"), "getQuote");
        String logs = carbonLogReader.getLogs();
        Assert.assertFalse(logs.contains(exceptedErrorLog),
                "Sender Side Stale connections handling test failed");
        carbonLogReader.stop();
    }

}