/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.mediator.test.aggregate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.IOException;

public class OnCompleteSequenceFromRegistryTestCase extends ESBIntegrationTest {

    private AggregatedRequestClient aggregatedRequestClient;
    private final int no_of_requests = 5;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        aggregatedRequestClient = new AggregatedRequestClient();
        aggregatedRequestClient
                .setProxyServiceUrl(getProxyServiceURLHttp("aggregateMediatorOnCompleteFromConfTestProxy"));
        aggregatedRequestClient.setSymbol("IBM");
        aggregatedRequestClient.setNoOfIterations(no_of_requests);

    }

    @Test(groups = {
            "wso2.esb"}, description = "pick up a sequence from registry conf on onComplete action of aggregate mediator")
    public void test() throws IOException {

        String Response = aggregatedRequestClient.getResponse();
        Assert.assertNotNull(Response, "Response message is null");
        Assert.assertTrue(Response.contains("getQuoteResponse"), "payload factory in registry sequence has not run");
        Assert.assertTrue(Response.contains("responseFromPayloadFactory"),
                "payload factory in registry sequence has not run");
        Assert.assertTrue(Response.contains("WSO2"), "payload factory in registry sequence has not run");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            aggregatedRequestClient = null;
        } finally {
            super.cleanup();
        }
    }

}
