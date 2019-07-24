package org.wso2.carbon.esb.mediators.callout;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.rmi.RemoteException;

public class ESBJAVA_4239_AccessHTTPSCAfterCallout extends ESBIntegrationTest {
    private CarbonLogReader carbonLogReader;

    private static final String PROXY_SERVICE_NAME = "HTTPSCProxy";
    private static final String EXPECTED_LOG_MESSAGE = "Status Code inSequence = 500";

    @BeforeClass(alwaysRun = true)
    public void deployeService() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Test whether an HTTP SC can be retrieved after the callout mediator.")
    public void testFetchHTTP_SC_After_Callout_Mediator() throws RemoteException, InterruptedException {
        final String proxyUrl = getProxyServiceURLHttp(PROXY_SERVICE_NAME);
        AxisServiceClient client = new AxisServiceClient();
        client.sendRobust(createPlaceOrderRequest(3.141593E0, 4, "IBM"), proxyUrl, "placeOrder");

        boolean isScFound = carbonLogReader.checkForLog(EXPECTED_LOG_MESSAGE, DEFAULT_TIMEOUT);
        Assert.assertTrue(isScFound, "The HTTP Status Code was not found in the log.");
        carbonLogReader.stop();
    }

    /*
     * This method will create a request required for place orders
     */
    public static OMElement createPlaceOrderRequest(double purchPrice, int qty, String symbol) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://services.samples", "m0");
        OMElement placeOrder = factory.createOMElement("placeOrder", ns);
        OMElement order = factory.createOMElement("order", ns);
        OMElement price = factory.createOMElement("price", ns);
        OMElement quantity = factory.createOMElement("quantity", ns);
        OMElement symb = factory.createOMElement("symbol", ns);
        price.setText(Double.toString(purchPrice));
        quantity.setText(Integer.toString(qty));
        symb.setText(symbol);
        order.addChild(price);
        order.addChild(quantity);
        order.addChild(symb);
        placeOrder.addChild(order);
        return placeOrder;
    }

}
