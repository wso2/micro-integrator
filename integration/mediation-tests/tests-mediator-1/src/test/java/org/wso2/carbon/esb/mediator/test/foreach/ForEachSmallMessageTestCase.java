/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Tests sending different number of small messages through foreach mediator
 */

public class ForEachSmallMessageTestCase extends ESBIntegrationTest {

    private String symbol;
    private CarbonLogReader carbonLogReader;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        symbol = FixedSizeSymbolGenerator.generateMessageKB(5);
        carbonLogReader = new CarbonLogReader();

    }

    @Test(groups = "wso2.esb", description = "Tests small message in small number ~20")
    public void testSmallNumbers() throws Exception {
        carbonLogReader.clearLogs();
        carbonLogReader.start();

        OMElement response = null;
        for (int i = 0; i < 20; i++) {
            response = axis2Client.sendCustomQuoteRequest(getProxyServiceURLHttp("foreachSmallMessageTestProxy"), null,
                    "IBM" + symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("IBM"), "Incorrect symbol in response");
            response = null;
        }
        if (carbonLogReader.checkForLog("foreach = in", DEFAULT_TIMEOUT)) {
            if (!carbonLogReader.checkForLog("IBM", DEFAULT_TIMEOUT)) {
                Assert.fail("Incorrect message entered ForEach scope. Could not find symbol IBM ..");
            }
        }
        Assert.assertTrue(carbonLogReader.checkForLog("foreach = in", DEFAULT_TIMEOUT, 20), "Count of messages entered ForEach scope is incorrect");
        carbonLogReader.stop();
    }

    @Test(groups = "wso2.esb", description = "Tests small message in small number ~100")
    public void testLargeNumbers() throws Exception {
        carbonLogReader.clearLogs();
        carbonLogReader.start();
        OMElement response = null;
        for (int i = 0; i < 100; i++) {
            response = axis2Client.sendCustomQuoteRequest(getProxyServiceURLHttp("foreachSmallMessageTestProxy"), null,
                    "MSFT" + symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("MSFT"),
                    "Incorrect symbol in response. Could not find symbol MSFT ..");
            response = null;
        }
        if (carbonLogReader.checkForLog("foreach = in", DEFAULT_TIMEOUT)) {
            if (!carbonLogReader.checkForLog("MSFT", DEFAULT_TIMEOUT)) {
                Assert.fail("Incorrect message entered ForEach scope");
            }
        }
        Assert.assertTrue(carbonLogReader.checkForLog("foreach = in", DEFAULT_TIMEOUT , 100),
                "Count of messages entered ForEach scope is incorrect");
        carbonLogReader.stop();
    }
}
