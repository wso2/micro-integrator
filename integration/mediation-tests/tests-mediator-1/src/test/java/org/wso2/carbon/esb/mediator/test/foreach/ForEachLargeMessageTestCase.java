/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediator.test.foreach;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.FixedSizeSymbolGenerator;

/**
 * Tests sending different number of large messages through foreach mediator
 */

public class ForEachLargeMessageTestCase extends ESBIntegrationTest {

    private String symbol;
    private CarbonLogReader carbonLogReader;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        symbol = FixedSizeSymbolGenerator.generateMessageMB(1);
        carbonLogReader = new CarbonLogReader();
    }

    @Test(groups = "wso2.esb", description = "Tests large message in small number 5")
    public void testSmallNumbers() throws Exception {
        carbonLogReader.start();
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = axis2Client.sendCustomQuoteRequest(getProxyServiceURLHttp("foreachLargeMessageTestProxy"), null,
                    "IBM" + symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("IBM"), "Incorrect symbol in response");
            response = null;
        }
        carbonLogReader.stop();

        String logs = carbonLogReader.getLogs();
        if (logs.contains("foreach = in")) {
            if (!logs.contains("IBM")) {
                Assert.fail("Incorrect message entered ForEach scope. Could not find symbol IBM ..");
            }
        }

        String[] splitElements = logs.split("IBM");
        Assert.assertEquals(splitElements.length - 1, 5, "Count of messages entered ForEach scope is incorrect");

    }

    @Test(groups = "wso2.esb", description = "Tests large message in large number 10")
    public void testLargeNumbers() throws Exception {
        carbonLogReader.start();

        OMElement response;
        for (int i = 0; i < 10; i++) {
            response = axis2Client.sendCustomQuoteRequest(getProxyServiceURLHttp("foreachLargeMessageTestProxy"), null,
                    "SUN" + symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("SUN"), "Incorrect symbol in response");
        }
        carbonLogReader.stop();

        String logs = carbonLogReader.getLogs();

        if (logs.contains("foreach = in")) {
            if (!logs.contains("SUN")) {
                Assert.fail("Incorrect message entered ForEach scope. Could not find symbol SUN ..");
            }
        }

        String[] splitElements = logs.split("SUN");
        Assert.assertEquals(splitElements.length - 1, 10, "Count of messages entered ForEach scope is incorrect");
    }

    @Test(groups = "wso2.esb", description = "Tests large message 3MB")
    public void testLargeMessage() throws Exception {
        carbonLogReader.start();

        String symbol2 = FixedSizeSymbolGenerator.generateMessageMB(3);
        OMElement response;

        response = axis2Client
                .sendCustomQuoteRequest(getProxyServiceURLHttp("foreachLargeMessageTestProxy"), null, "MSFT" + symbol2);

        carbonLogReader.stop();
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("MSFT"), "Incorrect symbol in response");

        String logs = carbonLogReader.getLogs();

        if (logs.contains("foreach = in")) {
            if (!logs.contains("MSFT")) {
                Assert.fail("Incorrect message entered ForEach scope. Could not find symbol MSFT ..");
            }
        }
        String[] splitElements = logs.split("MSFT");
        Assert.assertEquals(splitElements.length - 1, 1, "Count of messages entered ForEach scope is incorrect");
    }

}
