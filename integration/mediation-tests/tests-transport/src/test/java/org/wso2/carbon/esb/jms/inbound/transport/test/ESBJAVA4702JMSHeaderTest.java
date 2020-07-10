/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.esb.jms.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import javax.xml.stream.XMLStreamException;

/**
 * Test whether JMS properties are propagated through inbound endpoints.
 * https://wso2.org/jira/browse/ESBJAVA-4702
 */
public class ESBJAVA4702JMSHeaderTest extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
    }

    @Test(groups = { "wso2.esb" },
            description = "Test JMS Headers : ESBJAVA-4702")
    public void JMSInboundEndpointHeaderTest() throws Exception {

        CarbonLogReader logViewerClient = new CarbonLogReader();
        logViewerClient.start();
        Utils.deploySynapseConfiguration(addEndpoint(), "JMSIE", Utils.ArtifactType.INBOUND_ENDPOINT, false);
        axis2Client.sendRobust(getProxyServiceURLHttp("jmsHeaderInboundEpTestProxy"), null, null,
                               AXIOMUtil.stringToOM("<body/>"));
        boolean isHeaderSet = Utils.checkForLog(logViewerClient, "Producer_Log = MDM", 60);
        Assert.assertTrue(isHeaderSet, "Log for transport header is not present in carbon log");
        Utils.undeploySynapseConfiguration("JMSIE", Utils.ArtifactType.INBOUND_ENDPOINT, false);
        logViewerClient.stop();
    }

    private OMElement addEndpoint() throws XMLStreamException {

        return AXIOMUtil.stringToOM(
                "<inboundEndpoint xmlns=\"http://ws.apache.org/ns/synapse\"\n" + "                 name=\"JMSIE\"\n"
                        + "                 sequence=\"jmsHeaderInboundEpTestLogSequence\"\n"
                        + "                 onError=\"inFault\"\n" + "                 protocol=\"jms\"\n"
                        + "                 suspend=\"false\">\n" + "    <parameters>\n"
                        + "        <parameter name=\"interval\">1000</parameter>\n"
                        + "        <parameter name=\"transport.jms.Destination\">testqueue</parameter>\n"
                        + "        <parameter name=\"transport.jms.CacheLevel\">0</parameter>\n"
                        + "        <parameter name=\"transport.jms"
                        + ".ConnectionFactoryJNDIName\">QueueConnectionFactory</parameter>\n"
                        + "        <parameter name=\"java.naming.factory.initial\">org.apache.activemq.jndi.ActiveMQInitialContextFactory</parameter>\n"
                        + "        <parameter name=\"java.naming.provider.url\">tcp://localhost:61616</parameter>\n"
                        + "        <parameter name=\"transport.jms.SessionAcknowledgement\">AUTO_ACKNOWLEDGE</parameter>\n"
                        + "        <parameter name=\"transport.jms.SessionTransacted\">false</parameter>\n"
                        + "        <parameter name=\"transport.jms.ConnectionFactoryType\">queue</parameter>\n"
                        + "    </parameters>\n" + "</inboundEndpoint>");
    }

}
