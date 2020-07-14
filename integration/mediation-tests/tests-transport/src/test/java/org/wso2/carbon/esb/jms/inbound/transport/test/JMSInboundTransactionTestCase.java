/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.jms.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import javax.xml.stream.XMLStreamException;

/**
 * Tests JMS transactions with inbound endpoints.
 */
public class JMSInboundTransactionTestCase extends ESBIntegrationTest {

    private CarbonLogReader logViewerClient = null;
    private String message;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {

        super.init();
        message = String.valueOf(loadResource("message.xml"));
        logViewerClient = new CarbonLogReader();
        logViewerClient.start();
    }

    @Test(groups = { "wso2.esb" },
            description = "Successfully committing the message")
    public void testTransactionCommit() throws Exception {

        String queueName = "testTransactionCommitQueue";
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        logViewerClient.clearLogs();
        try {
            sender.connect(queueName);
            sender.pushMessage(message);
        } finally {
            sender.disconnect();
        }
        Utils.deploySynapseConfiguration(loadResource("jmsInboundTransactionCommitInboundEp.xml"), "TestJMSQueue",
                                         Utils.ArtifactType.INBOUND_ENDPOINT, false);
        boolean committed = Utils.checkForLog(logViewerClient, "Committed", 60);
        Assert.assertTrue(committed, "Did not find the \"Committed\" log");
        Assert.assertTrue(Utils.isQueueEmpty(queueName), "Queue (" + queueName + ") should be empty after commit");
        Utils.undeploySynapseConfiguration("TestJMSQueue", Utils.ArtifactType.INBOUND_ENDPOINT, false);
    }

    @Test(groups = { "wso2.esb" },
            description = "Rolling back the failed message to the queue")
    public void testTransactionRollBack() throws Exception {

        String queueName = "testTransactionRollBackQueue";
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        logViewerClient.clearLogs();
        try {
            sender.connect(queueName);
            sender.pushMessage(message);
        } finally {
            sender.disconnect();
        }
        Utils.deploySynapseConfiguration(loadResource("jmsInboundTransactionRollbackInboundEp.xml"), "TestJMSQueue1",
                                         Utils.ArtifactType.INBOUND_ENDPOINT, false);
        boolean rollBacked = Utils.checkForLog(logViewerClient, "Rollbacked", 60);
        Assert.assertTrue(rollBacked, "Did not find the \"Rollbacked\" log");
        Assert.assertFalse(Utils.isQueueEmpty(queueName),
                           "Queue (" + queueName + ") should not be empty after " + "rollback");
        Utils.undeploySynapseConfiguration("TestJMSQueue1", Utils.ArtifactType.INBOUND_ENDPOINT, false);
    }

    private OMElement loadResource(String resourceName) throws FileNotFoundException, XMLStreamException {

        return esbUtils.loadResource(
                String.join(File.separator, "artifacts", "ESB", "jms", "inbound", "transport", resourceName));
    }
}
