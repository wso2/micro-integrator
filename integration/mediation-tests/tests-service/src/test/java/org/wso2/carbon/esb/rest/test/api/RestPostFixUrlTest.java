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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * Related to Patch Automation https://wso2.org/jira/browse/ESBJAVA-3260 This
 * class test Target URL not appending the Context URL in REST_URL_POSTFIX.
 */

public class RestPostFixUrlTest extends ESBIntegrationTest {

    private CarbonLogReader logReader;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        logReader = new CarbonLogReader();
        logReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Sending a Message Via REST with additional resource")
    public void testRESTURITemplateWithContextURL() throws Exception {

        /** To check whether the Context URL part "anotherParam" available.
         *  sending request from Client API with additional resource
         * "anotherParam" services/client/anotherParam
         */
        logReader.clearLogs();

        HttpRequestUtil.sendGetRequest(getApiInvocationURL("services/client/anotherParam"), null);

        Assert.assertFalse(logReader.checkForLog("anotherParam", DEFAULT_TIMEOUT),
                " Target URL is wrong. It appends the context URL part also.");

    }

    @Test(groups = { "wso2.esb" }, description = "Sending a Message Via REST with additional resource")
    public void testRESTURITemplateWithAdditionalParam() throws Exception {

        /** To check whether the Context URL part "anotherParam" available
         *  sending request from Client API with additional resource - "anotherParam"
         *  & prameter - "foo"
         *  services/client/anotherParam/foo
         */
        logReader.clearLogs();

        HttpRequestUtil.sendGetRequest(getApiInvocationURL("services/client/anotherParam/foo"), null);
        Assert.assertTrue(logReader.checkForLog("/services/testAPI/foo", DEFAULT_TIMEOUT),
                " Target URL is wrong. expected /services/testAPI/foo ");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        logReader.stop();
    }
}
