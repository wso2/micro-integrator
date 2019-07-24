/*
 *Copyright (c) 2005, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class ESBJAVA3340QueryParamHttpEndpointTestCase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = {
            "wso2.esb"}, description = "Sending a Message Via REST to test query param works with space character")
    public void testPassParamsToEndpoint() throws InterruptedException {
        String requestString = "/context?queryParam=some%20value";
        boolean isSpaceCharacterEscaped;
        try {
            HttpRequestUtil.sendGetRequest(getApiInvocationURL("passParamsToEPTest") + requestString, null);
        } catch (Exception timeout) {
            //a timeout is expected
        }
        isSpaceCharacterEscaped = carbonLogReader.checkForLog("queryParam = some%20value", DEFAULT_TIMEOUT);
        carbonLogReader.stop();

        Assert.assertTrue(isSpaceCharacterEscaped,
                "Fail to send a message via REST when query parameter consist of space character");
    }

}
