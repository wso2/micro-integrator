/*
 *Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.endpoint.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

import java.util.HashMap;
import java.util.Map;

/**
 * Test case for checking invocation of correct fault sequence.
 */
public class FaultSequenceExecutionOrderTestCase extends ESBIntegrationTest {

    private SampleAxis2Server axis2Server;
    CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        axis2Server = new SampleAxis2Server("test_axis2_server_9001.xml");
        axis2Server.deployService(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE + "_timeout");
        axis2Server.start();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = {"wso2.esb"}, description = "Correct Fault Sequence Invoke Test")
    public void testCorrectFaultSequenceExecution() throws Exception {
        String contentType = "application/xml";
        SimpleHttpClient httpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", contentType);
        httpClient.doPost(getMainSequenceURL() + "faultSequenceExecutionOrderTest", headers, "", contentType);
        boolean isImmediateOnly = carbonLogReader.checkForLog("cF = C Fault", DEFAULT_TIMEOUT);
        boolean isSuperSequecefalutExecuted = carbonLogReader.checkForLog("aF = A Fault", DEFAULT_TIMEOUT);
        Assert.assertTrue(isImmediateOnly);
        Assert.assertFalse(isSuperSequecefalutExecuted);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() {
        axis2Server.stop();
        axis2Server = null;
        carbonLogReader.stop();
    }
}
