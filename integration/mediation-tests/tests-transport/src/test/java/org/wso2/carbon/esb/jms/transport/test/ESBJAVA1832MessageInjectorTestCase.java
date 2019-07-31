/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.esb.jms.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * https://wso2.org/jira/browse/ESBJAVA-1832
 * Unable to store messages with out setting axis2 service instance in a JMS message store.
 */

public class ESBJAVA1832MessageInjectorTestCase extends ESBIntegrationTest {

    private JMSQueueMessageConsumer consumer;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        OMElement omElement = AXIOMUtil.stringToOM(FileUtils.readFileToString(new File(getESBResourceLocation()
                + File.separator + "jms" + File.separator + "transport" + File.separator + "msgInjection" + File.separator
                + "ESBJAVA1832MessageInjectorTestTask.xml")));
        Utils.deploySynapseConfiguration(omElement, "ESBJAVA1832MessageInjectorTestTask", "tasks", true);
    }

    @Test(groups = { "wso2.esb" }, description = "Test proxy service with jms transport")
    public void testMessageInjection() throws Exception {
        String queueName = "jmsQueue";
        int numberOfMsgToExpect = 10;
        TimeUnit.SECONDS.sleep(15);
        try {
            consumer.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                if (consumer.popMessage(javax.jms.Message.class) == null) {
                    Assert.fail("Unable to pop the expected number of message in the queue " + queueName);
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        Utils.undeploySynapseConfiguration("ESBJAVA1832MessageInjectorTestTask", "tasks");
    }
}
