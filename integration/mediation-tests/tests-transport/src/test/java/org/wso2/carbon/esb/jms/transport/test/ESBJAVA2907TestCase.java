package org.wso2.carbon.esb.jms.transport.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

/**
 * ESBJAVA-2907 OMElements are not added as properties when saving messages to the MessageStore
 */
public class ESBJAVA2907TestCase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader = new CarbonLogReader();
    private static String GET_QUOTE_REQUEST_BODY = "OM_ELEMENT_PREFIX_ = <ns:getQuote xmlns:ns=\"http://services.samples\"><ns:request><ns:symbol>IBM</ns:symbol></ns:request></ns:getQuote>";

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Test adding OMElements as properties when saving messages to the MessageStore")
    public void testAddingOMElementPropertyToMessageStore() throws Exception {
        AxisServiceClient client = new AxisServiceClient();
        client.sendRobust(Utils.getStockQuoteRequest("IBM"), getProxyServiceURLHttp("testPS"), "getQuote");
        Assert.assertTrue(carbonLogReader.checkForLog(GET_QUOTE_REQUEST_BODY, DEFAULT_TIMEOUT), "OMElement is not saved to the message store");
        carbonLogReader.stop();
    }

}
