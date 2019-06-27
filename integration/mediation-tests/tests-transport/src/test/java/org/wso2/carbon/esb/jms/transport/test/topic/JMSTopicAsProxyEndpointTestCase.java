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
package org.wso2.carbon.esb.jms.transport.test.topic;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSTopicMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

public class JMSTopicAsProxyEndpointTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
    }

    @Test(groups = { "wso2.esb" }, description = "Test proxy service with jms transport")
    public void testJMSProxy() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        int messageCount = 5;

        JMSTopicMessageConsumer consumer = new JMSTopicMessageConsumer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.subscribe("TestTopic");
            for (int i = 0; i < messageCount; i++) {
                client.sendRobust(Utils.getStockQuoteRequest("JMSTopicTest"),
                        getProxyServiceURLHttp("StockQuoteProxyToJMSTopic"), "getQuote");
            }

            for (int i = 0; i < 30; i++) {
                if (consumer.getMessages().size() == messageCount) {
                    break;
                }
                Thread.sleep(1000);
            }
        } finally {
            consumer.stopConsuming();
        }

        Assert.assertEquals(consumer.getMessages().size(), messageCount,
                "Message count mismatched in Topic." + " All the message sent to proxy not reached to topic");
        for (String message : consumer.getMessages()) {
            Assert.assertTrue(message.contains("JMSTopicTest"),
                    "Message Content received from topic" + " mismatched than the original message");
        }

    }
}
