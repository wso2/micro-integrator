/*
 *Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.mediator.test.property;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

/**
 * This class tests the functionality of the XPATHURL property
 */

public class PropertyIntegrationXpathURLPropertyTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Test getting the  URI element of a request URL")
    public void testXpathURLProperty() throws Exception {
        boolean isUri = false;
        HttpRequestUtil.sendGetRequest(getApiInvocationURL("XpathURLPropertyApi") + "/edit?a=wso2&b=2.4", null);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            log.warn("Sleep Inturrupted : logs may not updated with required text");
        }

        String msg = "SYMBOL = wso2, VALUE = 2.4";
        // after sending the message reading the log file
        isUri = carbonLogReader.checkForLog(msg, DEFAULT_TIMEOUT);
        assertTrue(isUri, "Message expected (SYMBOL = wso2, VALUE = 2.4) Not found");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }
}
