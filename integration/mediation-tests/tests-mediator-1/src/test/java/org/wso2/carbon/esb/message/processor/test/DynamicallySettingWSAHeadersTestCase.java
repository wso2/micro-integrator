/*
 *Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.message.processor.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Test case to test whether message processor is able to handle messages when their WSA headers are set dynamically.
 */
public class DynamicallySettingWSAHeadersTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Testing message Processor handling message when setting wsa headers dynamically")
    public void testForwardingWithInMemoryStore()
            throws InterruptedException, MalformedURLException, AutomationFrameworkException {
        CarbonLogReader carbonLogReader = new CarbonLogReader();
        Reader data = new StringReader("<request><element>Test</element></request>");
        Writer writer = new StringWriter();
        carbonLogReader.start();
        HttpURLConnectionClient
                .sendPostRequestAndReadResponse(data, new URL(getProxyServiceURLHttp("MessageProcessorWSATestProxy")),
                        writer, "application/xml");
        Assert.assertTrue(checkForLog(carbonLogReader, "MessageProcessorWSAProxy Request Received", 20),
                "Message processor unable to handle the message!");
    }

    private static boolean checkForLog(CarbonLogReader carbonLogReader, String expected, int timeout)
            throws InterruptedException {
        boolean logExists = false;
        for (int i = 0; i < timeout; i++) {
            TimeUnit.SECONDS.sleep(1);
            if (carbonLogReader.getLogs().contains(expected)) {
                logExists = true;
                break;
            }
        }
        carbonLogReader.stop();
        return logExists;
    }
}