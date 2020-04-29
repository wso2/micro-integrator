/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests sending different number of messages through foreach mediator.
 */
public class ForEachMediatorTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb",
            description = "Test sending messages to for each and assert the count")
    public void testForEach() throws Exception {

        int count = 5;
        clearLogsAndSendRequest(count);
        Assert.assertTrue(carbonLogReader.checkForLog("ForEachMediatorTestCase = in", DEFAULT_TIMEOUT, count),
                          "No of messages entered ForEach scope is incorrect");

        count = 20;
        clearLogsAndSendRequest(count);
        Assert.assertTrue(carbonLogReader.checkForLog("ForEachMediatorTestCase = in", DEFAULT_TIMEOUT, count),
                          "No of messages entered ForEach scope is incorrect");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }

    private void clearLogsAndSendRequest(int count) throws Exception {
        carbonLogReader.clearLogs();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/xml");
        String payload = IntStream.range(0, count).mapToObj(i -> "<foreach>TEST</foreach>").collect(
                Collectors.joining("", "<payload>", "</payload>"));
        HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("foreachLargeMessageTestProxy")), payload, headers);
    }
}
