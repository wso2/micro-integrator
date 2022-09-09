/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.jms.transport.test;

import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class JMSBlockingCallWithToHeaderTestCase extends ESBIntegrationTest {
    private final int NUM_OF_MESSAGES = 3;
    CarbonLogReader carbonLogReader = new CarbonLogReader();

    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        super.init();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Test JMS Blocking Call with To Header")
    public void jMSBlockingCallWithToHeaderTestCase() throws Exception {
        String nullPointerExceptionErrorLog =
                "Unexpected error during sending message out java.lang.NullPointerException";
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String message = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">"
                + "  <soapenv:Header/>" + "  <soapenv:Body>" + "   <ser:placeOrder>" + "     <ser:order>"
                + "      <xsd:price>100</xsd:price>" + "      <xsd:quantity>2000</xsd:quantity>"
                + "      <xsd:symbol>JMSTransport</xsd:symbol>" + "     </ser:order>" + "   </ser:placeOrder>"
                + "  </soapenv:Body>" + "</soapenv:Envelope>";
        try {
            sender.connect("JMSBlockingCallWithToHeaderTestCaseProxy");
            carbonLogReader.clearLogs();
            for (int i = 0; i < NUM_OF_MESSAGES; i++) {
                sender.pushMessage(message);
            }
            Assert.assertFalse(carbonLogReader.checkForLog(nullPointerExceptionErrorLog, DEFAULT_TIMEOUT),
                "Unexpected error occurred when sending message out");
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect("SimpleStockQuoteServiceJMSBlockingCallTestCase");
            Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(300, TimeUnit.SECONDS)
                    .until(isMessagesConsumed(consumer));
            for (int i = 0; i < NUM_OF_MESSAGES; i++) {
                if (consumer.popMessage() == null) {
                    Assert.fail("Message has not received at SimpleStockQuoteServiceJMSBlockingCallTestCase");
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    private Callable<Boolean> isMessagesConsumed(final JMSQueueMessageConsumer consumer) {
        return () -> consumer.getMessages().size() == NUM_OF_MESSAGES;
    }
}
