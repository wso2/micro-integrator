/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.mediator.test.property;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Dynamic Properties were introduced with https://github.com/wso2/product-ei/issues/3300
 * This class tests the functionality of dynamic property names
 */
public class PropertyIntegrationDynamicNameTestCase extends ESBIntegrationTest {
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", description = "Test get property evaluation for dynamic property names")
    public void testGetPropertyEvaluation() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        String inputPayload = "{\n" +
                "            \"propertyName\":\"hospitalName\",\n" +
                "                \"propertyValue\":\"Maria Hospital\"\n" +
                "        }";
        String expectedResponse = "{\"hospital\":\"Maria Hospital\"}";
        HttpResponse httpResponse = HttpRequestUtil.doPost(
                new URL(getProxyServiceURLHttp("DynamicPropertiesGetPropertyEval")), inputPayload,
                headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200, "Error occurred while retrieving dynamic " +
                "property value using get property evaluation.");
        Assert.assertEquals(httpResponse.getData(), expectedResponse, "Error occurred while retrieving dynamic " +
                "property value using get property evaluation.");
    }

    @Test(groups = "wso2.esb", description = "Test synapse message context evaluation for dynamic property names")
    public void testSynapseMessageContextEvaluation() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        String inputPayload = "{\n" +
                "            \"propertyName\":\"schoolName\",\n" +
                "                \"propertyValue\":\"ABC\"\n" +
                "        }";
        String expectedResponse = "{\"school\":\"ABC\"}";
        HttpResponse httpResponse = HttpRequestUtil.doPost(
                new URL(getProxyServiceURLHttp("DynamicPropertiesSynapseMessageContextEval")), inputPayload,
                headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200, "Error occurred while retrieving dynamic " +
                "property value using synapse message context.");
        Assert.assertEquals(httpResponse.getData(), expectedResponse, "Error occurred while retrieving dynamic " +
                "property value using synapse message context.");
    }

    @Test(groups = "wso2.esb", description = "Test json evaluation for dynamic property names")
    public void testJsonEvaluation() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        String inputPayload = "{\n" +
                "            \"propertyName\":\"companyName\",\n" +
                "                \"propertyValue\":\"WSO2\"\n" +
                "        }";
        String expectedResponse = "{\"company\":\"WSO2\"}";
        HttpResponse httpResponse = HttpRequestUtil.doPost(
                new URL(getProxyServiceURLHttp("DynamicPropertiesJsonEval")), inputPayload, headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200, "Error occurred while retrieving " +
                "dynamic property value using json evaluation.");
        Assert.assertEquals(httpResponse.getData(), expectedResponse, "Error occurred while retrieving dynamic " +
                "property value using json evaluation.");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        cleanup();
    }
}
