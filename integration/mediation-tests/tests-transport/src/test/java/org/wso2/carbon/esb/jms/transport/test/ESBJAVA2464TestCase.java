package org.wso2.carbon.esb.jms.transport.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class ESBJAVA2464TestCase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader = new CarbonLogReader();
    private static final String logLine0 = "org.wso2.carbon.proxyadmin.service.ProxyServiceAdmin is not an admin service. Service name ";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        carbonLogReader.start();
    }

    @Test(groups = {
            "wso2.esb" }, description = "Test ESBJAVA2464 proxy service with jms and nonBlockingLocal transport")
    public void testMessageInjection() throws Exception {
        Thread.sleep(7000);

        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(
                JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String message = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:echo=\"http://echo.services.core.carbon.wso2.org\">"
                + "  <soapenv:Header/>" + "  <soapenv:Body>" + "     <echo:echoInt>" + "        <!--Optional:-->"
                + "       <in>1</in>" + "     </echo:echoInt>" + "  </soapenv:Body>" + "</soapenv:Envelope>";

        try {
            sender.connect("ESBJAVA2464TestProxy");
            for (int i = 0; i < 3; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        if (carbonLogReader.checkForLog(logLine0, DEFAULT_TIMEOUT)) {
            Assert.fail(logLine0 + "is in log");
        }
        carbonLogReader.stop();
    }

}
