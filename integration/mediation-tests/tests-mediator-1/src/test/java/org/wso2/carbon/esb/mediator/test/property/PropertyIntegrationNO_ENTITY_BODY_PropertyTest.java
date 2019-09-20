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
package org.wso2.carbon.esb.mediator.test.property;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Property Mediator NO_ENTITY_BODY Property Test
 */

public class PropertyIntegrationNO_ENTITY_BODY_PropertyTest extends ESBIntegrationTest {

    private SimpleHttpClient client;
    private HttpResponse response;

    public HttpResponse sendGet(String endpoint, Map<String, String> headers) throws Exception {

        client = new SimpleHttpClient();
        response = client.doGet(endpoint, headers);
        return response;
    }

    @Test(groups = "wso2.esb", description = "Test-Without No_ENTITY_BODY Property")
    public void testWithoutNoEntityBodyPropertTest() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/plain");
        String endpoint = "http://localhost:8480/services/Axis2ProxyService1";
        response = sendGet(endpoint, headers);
        String responsePayload = client.getResponsePayload(response);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Response code not 200");
        assertNotNull(responsePayload, "Response is null");
        assertEquals(responsePayload, "IBM", "Text does not match");
    }

    @Test(groups = "wso2.esb", description = "Test-With NO_ENTITY_BODY")
    public void testWithNoEntityBodyPropertTest() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/plain");
        String endpoint = "http://localhost:8480/services/Axis2ProxyService2";
        response = sendGet(endpoint, headers);
        String responsePayload = client.getResponsePayload(response);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Response code not 200");
        Assert.assertTrue(responsePayload.isEmpty(), "Expected empty response but found " + responsePayload);
    }
}
