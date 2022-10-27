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
package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * This test class is to check the presence of the charset in the Content-Type header
 * of multipart/form-data when "setCharacterEncoding" property is enabled and
 * disabled.
 */
public class MultipartFormDataWithCharacterEncodingPropertyTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"}, description = "Test for setCharacterEncoding property for multipart/form-data")
    public void testReturnContentType() throws Exception {

        String jsonPayload = "{\"action\":\"ping\"}";

        SimpleHttpClient httpClient = new SimpleHttpClient();

        HttpResponse response = httpClient.doPost(getApiInvocationURL("testMultipartFormData/withSetCharacterEncodingPropertyEnabled"), null, jsonPayload, HTTPConstants.MEDIA_TYPE_APPLICATION_JSON);
        String contentTypeData = response.getEntity().getContentType().getValue();

        Assert.assertTrue(contentTypeData.contains("charset"), "The default charset should present in the Content-Type header");

        response = httpClient.doPost(getApiInvocationURL("testMultipartFormData/withSetCharacterEncodingPropertyDisabled"), null, jsonPayload, HTTPConstants.MEDIA_TYPE_APPLICATION_JSON);
        contentTypeData = response.getEntity().getContentType().getValue();

        Assert.assertFalse(contentTypeData.contains("charset"), "Charset should not be present in the Content-Type header when 'setCharacterEncoding' is set to false");

    }

    @AfterClass(alwaysRun = true)
    private void destroy() throws Exception {
        super.cleanup();
    }
}
