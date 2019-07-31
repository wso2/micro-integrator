/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.rest.test.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import static org.testng.Assert.assertTrue;

public class APIHeadMethod extends ESBIntegrationTest {
    CarbonLogReader logReader = new CarbonLogReader();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        logReader.start();
    }

    @Test(groups = "wso2.esb", description = "API HTTP HEAD Method")
    public void apiHTTPHeadMethodTest() throws Exception {
        String restURL = "http://localhost:8480/headTest";
        DefaultHttpClient httpclient = new DefaultHttpClient();



        HttpHead httpHead = new HttpHead(restURL);
        HttpResponse response = httpclient.execute(httpHead);

        Assert.assertTrue(logReader.checkForLog("API_HIT", DEFAULT_TIMEOUT));

        // http head method should return a 200 OK
        assertTrue(response.getStatusLine().getStatusCode() == 200);
        // it should not contain a message body
        assertTrue(response.getEntity() == null);

        logReader.stop();
    }
}
