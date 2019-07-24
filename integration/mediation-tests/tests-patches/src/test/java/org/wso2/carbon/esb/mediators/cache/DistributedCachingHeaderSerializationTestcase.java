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
package org.wso2.carbon.esb.mediators.cache;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This test can be used make sure cache mediator works with distributed caching is enabled.
 */
public class DistributedCachingHeaderSerializationTestcase extends ESBIntegrationTest {
    CarbonLogReader carbonLogReader = new CarbonLogReader();

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "cache meditor test enabling axis2 clustering.")
    public void testDistributedCachingHeaderSerialization() throws Exception {
        String requestXml = "<a>ABC</a>";

        SimpleHttpClient httpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/xml;charset=UTF-8");
        HttpResponse response1 = httpClient.doPost(getApiInvocationURL("CachingTest") + "/test", headers, requestXml,
                "application/xml;charset=UTF-8");
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        response1.getEntity().writeTo(baos1);
        String actualValue1 = baos1.toString();

        // this is to populate response from cache mediator
        HttpResponse response2 = httpClient.doPost(getApiInvocationURL("CachingTest") + "/test", headers, requestXml,
                "application/xml;charset=UTF-8");
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        response2.getEntity().writeTo(baos2);
        String actualValue2 = baos2.toString();

        Assert.assertEquals(actualValue1, requestXml);
        Assert.assertEquals(actualValue2, requestXml);

        boolean existInLogs = carbonLogReader.checkForLog("CACHEMATCHEDCACHEMATCHED", DEFAULT_TIMEOUT);
        carbonLogReader.stop();
        Assert.assertTrue(existInLogs);
    }
}
