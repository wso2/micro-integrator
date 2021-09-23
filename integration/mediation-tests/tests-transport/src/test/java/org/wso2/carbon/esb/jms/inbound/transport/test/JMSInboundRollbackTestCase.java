/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

/**
 * This testcase tests whether broker is redelivering messages as expected after rollback
 */
public class JMSInboundRollbackTestCase extends ESBIntegrationTest {

    private static final String QUEUE_NAME = "jmsQueueJMSInboundRollbackTestCase";
    private CarbonLogReader logViewerClient = null;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        init();
        logViewerClient = new CarbonLogReader();
        logViewerClient.start();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" },
            description = "Test whether broker is redelivering messages as expected after rollback")
    public void testJMSRollbackAndRetriesWithInboundEP() throws Exception {
        logViewerClient.clearLogs();
        sendMessage();
        boolean logExists = logViewerClient.checkForLog("** jmsRollbackInboundEPInSequence was called **", 120, 4);
        Assert.assertTrue(logExists, "Message was not redelivered 3 times after Rollback");
        Assert.assertTrue(Utils.isQueueEmpty(QUEUE_NAME), "Queue should be empty after 3 retries");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        logViewerClient.stop();
    }

    private void sendMessage() throws Exception {

        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String message = "{\"Name\" : \"WSO2\"}";
        try {
            sender.connect(QUEUE_NAME);
            sender.pushMessage(message);
        } finally {
            sender.disconnect();
        }
    }
}
