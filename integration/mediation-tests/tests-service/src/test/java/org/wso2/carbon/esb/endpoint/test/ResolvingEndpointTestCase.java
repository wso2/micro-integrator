/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.endpoint.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * Resolving Endpoints test class
 */
public class ResolvingEndpointTestCase extends ESBIntegrationTest {

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = {"wso2.esb"}, description = "Sending a Message to a dynamically resolved endpoint")
    public void testSendingToDynamicallyResolvedEndpoint() throws AxisFault {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                "http://localhost:8480/services/resolvingEndpointTestProxy?myKey=resolvingEP", null, "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
    }
}
