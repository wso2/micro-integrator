/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.esb.rest.test.api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * https://wso2.org/jira/browse/ESBJAVA-4852 This test cass will test the URITemplate dispatcher when HTTP method having
 * complete url with query parameters.
 */

public class ESBJAVA4852URITemplateWithCompleteURLTestCase extends ESBIntegrationTest {

    private CarbonLogReader logReader;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        logReader = new CarbonLogReader();
    }

    @Test(groups = { "wso2.esb" }, description = "Sending complete URL to API and for dispatching")
    public void testCompleteURLWithHTTPMethod() throws Exception {
        logReader.start();

        DeleteMethod delete = new DeleteMethod(getApiInvocationURL(
                "myApi1/order/21441/item/17440079" + "?message_id=41ec2ec4-e629-4e04-9fdf-c32e97b35bd1"));
        HttpClient httpClient = new HttpClient();

        try {
            httpClient.executeMethod(delete);
            Assert.assertEquals(delete.getStatusLine().getStatusCode(), 202, "Response code mismatched");
        } finally {
            delete.releaseConnection();
        }

        Assert.assertTrue(logReader.checkForLog("order API INVOKED", DEFAULT_TIMEOUT),
                "Request Not Dispatched to API when HTTP method having full url");

        logReader.stop();
    }
}
