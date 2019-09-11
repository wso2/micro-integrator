package org.wso2.carbon.esb.jms.transport.test;

import org.testng.Assert;
import org.awaitility.Awaitility;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class JMSEndpointTestCase extends ESBIntegrationTest {
    private int NUM_OF_MESSAGES = 3;

    @BeforeClass(alwaysRun = true)
    public void deployeService() throws Exception {
        super.init();
    }

    @Test(groups = { "wso2.esb" }, description = "Test JMS to JMS ")
    public void testJMSProxy() throws Exception {

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
            sender.connect("JMSEndpointTestCaseProxy");
            for (int i = 0; i < NUM_OF_MESSAGES; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect("SimpleStockQuoteServiceJMSEndpointTestCase");
            Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(300, TimeUnit.SECONDS)
                    .until(isMessagesConsumed(consumer));
            for (int i = 0; i < NUM_OF_MESSAGES; i++) {
                if (consumer.popMessage() == null) {
                    Assert.fail("Message not received at SimpleStockQuoteService");
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    private Callable<Boolean> isMessagesConsumed(final JMSQueueMessageConsumer consumer) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return consumer.getMessages().size() == NUM_OF_MESSAGES;
            }
        };
    }
}
