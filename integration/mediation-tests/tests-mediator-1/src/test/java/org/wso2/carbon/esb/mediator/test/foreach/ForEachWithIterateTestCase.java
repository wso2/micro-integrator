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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.mediator.test.iterate.IterateClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 * Test a complete flow of modifying payload using a payloadfactorymediator within foreach mediator
 * and passing on to an iterate mediator followed by aggregate mediator
 */
public class ForEachWithIterateTestCase extends ESBIntegrationTest {

    private IterateClient client;
    private CarbonLogReader carbonLogReader;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        client = new IterateClient();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Test foreach inline sequence to transform payload, passed to endpoint using iterate and aggregate mediators")
    public void testForEachInlineSequenceWithIterateEndpoint() throws Exception {
        carbonLogReader.clearLogs();
        String response = client.getMultipleCustomResponse(getProxyServiceURLHttp("foreachWithIteration"), "IBM", 2);
        Assert.assertNotNull(response);

        if (carbonLogReader.checkForLog("foreachWithIteration = in", DEFAULT_TIMEOUT)) {
            if (!carbonLogReader.getLogs().contains("IBM")) {
                Assert.fail("Incorrect message entered ForEach scope");
            }
        }
        Assert.assertTrue(carbonLogReader.checkForLog("foreachWithIteration = in", DEFAULT_TIMEOUT, 2),
                          "Count of messages entered ForEach scope is incorrect");
        OMElement envelope = client.toOMElement(response);
        OMElement soapBody = envelope.getFirstElement();
        Iterator iterator = soapBody.getChildrenWithName(new QName("http://services.samples", "getQuoteResponse"));
        int i = 0;
        while (iterator.hasNext()) {
            i++;
            OMElement getQuote = (OMElement) iterator.next();
            Assert.assertTrue(getQuote.toString().contains("IBM"));
        }
        Assert.assertEquals(i, 2, "Message count mismatched in response");
    }

    @Test(groups = "wso2.esb", description = "Test foreach sequence ref to transform payload, passed to endpoint using iterate and aggregate mediators")
    public void testForEachSequenceRefWithIterateEndpoint() throws Exception {
        carbonLogReader.clearLogs();
        String response = client
                .getMultipleCustomResponse(getProxyServiceURLHttp("foreach_simple_sequenceref"), "IBM", 2);
        Assert.assertNotNull(response);
        if (carbonLogReader.checkForLog("foreach = in", DEFAULT_TIMEOUT)) {
            if (!carbonLogReader.getLogs().contains("IBM")) {
                Assert.fail("Incorrect message entered ForEach scope");
            }
        }
        Assert.assertTrue(carbonLogReader.checkForLog("foreach = in", DEFAULT_TIMEOUT, 2),
                "Count of messages entered ForEach scope is incorrect");
        OMElement envelope = client.toOMElement(response);
        OMElement soapBody = envelope.getFirstElement();
        Iterator iterator = soapBody.getChildrenWithName(new QName("http://services.samples", "getQuoteResponse"));
        int i = 0;
        while (iterator.hasNext()) {
            i++;
            OMElement getQuote = (OMElement) iterator.next();
            Assert.assertTrue(getQuote.toString().contains("IBM"));
        }
        Assert.assertEquals(i, 2, "Message count mismatched in response");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }
}
