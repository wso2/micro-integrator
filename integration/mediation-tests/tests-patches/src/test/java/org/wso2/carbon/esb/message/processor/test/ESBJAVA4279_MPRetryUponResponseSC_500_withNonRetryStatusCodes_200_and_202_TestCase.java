/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.esb.message.processor.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * The Message processor is configured with 200,202 as non retry status codes.
 * If the back end returns 500 SC, then the MP keeps on retrying till the max
 * delivery attempt is reached. This test case is used to verify that behavior.
 */
public class ESBJAVA4279_MPRetryUponResponseSC_500_withNonRetryStatusCodes_200_and_202_TestCase
        extends ESBIntegrationTest {
    private static final String PROXY_SERVICE_NAME = "NonRetrySCProxy";
    private static final String EXPECTED_ERROR_MESSAGE =
            "Message processor [Processor1] received a response with HTTP_SC: 500 from backend EP. Message forwarding failed.";
    private static final String EXPECTED_MP_DEACTIVATION_MSG =
            "Successfully deactivated the message processor [Processor1]";
    private static final int RETRY_COUNT = 4;

    @BeforeClass(alwaysRun = true)
    public void deployeService() throws Exception {
        super.init();
    }

    @Test(groups = {
            "wso2.esb" }, description = "Test whether a Message Processor retries sending the message to the EP when the response status code is 500 and MP is configured with 200,202 as non-retry status codes.")
    public void testMPRetryUponHTTP_SC_500_response_with_200_And_202_AsNonRetrySCs()
            throws RemoteException, InterruptedException {

        final String proxyUrl = getProxyServiceURLHttp(PROXY_SERVICE_NAME);
        AxisServiceClient client = new AxisServiceClient();
        CarbonLogReader carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
        client.sendRobust(createPlaceOrderRequest(3.141593E0, 4, "IBM"), proxyUrl, "placeOrder");

        // Wait till the log appears
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(20, TimeUnit.SECONDS).
                until(isMPDeactivationMessageExists(carbonLogReader));
        boolean isRetriedUpon_500_response = carbonLogReader.checkForLog(EXPECTED_ERROR_MESSAGE, DEFAULT_TIMEOUT);
        Assert.assertTrue(isRetriedUpon_500_response, "Message processor didn't retry upon failure");
        boolean isRetryCompleted = carbonLogReader.checkForLog(EXPECTED_ERROR_MESSAGE, DEFAULT_TIMEOUT, RETRY_COUNT);
        Assert.assertTrue(isRetryCompleted, "Message processor didn't retry " + RETRY_COUNT + " times upon failure");
        boolean isMpDeactivated = carbonLogReader.checkForLog(EXPECTED_MP_DEACTIVATION_MSG, DEFAULT_TIMEOUT);
        Assert.assertTrue(isMpDeactivated, "Message processor hasn't been deactivated upon retries.");
        carbonLogReader.stop();
    }

    /*
     * This method will create a request required for place orders
     */
    private OMElement createPlaceOrderRequest(double purchPrice, int qty, String symbol) {
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

    private Callable<Boolean> isMPDeactivationMessageExists(CarbonLogReader carbonLogReader) {
        return () -> carbonLogReader.checkForLog(EXPECTED_MP_DEACTIVATION_MSG, DEFAULT_TIMEOUT);
    }
}
