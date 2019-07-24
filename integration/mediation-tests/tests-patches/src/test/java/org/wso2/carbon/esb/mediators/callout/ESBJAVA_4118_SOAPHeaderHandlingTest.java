/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.wso2.carbon.esb.mediators.callout;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.io.File;

import static org.testng.Assert.assertFalse;

public class ESBJAVA_4118_SOAPHeaderHandlingTest extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        super.init();
        verifyProxyServiceExistence("TestCalloutSoapHeader");
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = "wso2.esb", description = "Test whether the callout mediator successfully handle SOAP messages "
            + "Having SOAP header")
    public void testSOAPHeaderHandling() throws Exception {
        String endpoint = "http://localhost:8480/services/TestCalloutSoapHeader";
        String soapRequest =
                TestConfigurationProvider.getResourceLocation() + "artifacts" + File.separator + "ESB" + File.separator
                        + "mediatorconfig" + File.separator + "callout" + File.separator + "SOAPRequestWithHeader.xml";
        File input = new File(soapRequest);
        PostMethod post = new PostMethod(endpoint);
        RequestEntity entity = new FileRequestEntity(input, "text/xml");
        post.setRequestEntity(entity);
        post.setRequestHeader("SOAPAction", "getQuote");
        HttpClient httpClient = new HttpClient();
        boolean errorLog = false;

        try {
            int result = httpClient.executeMethod(post);
            String responseBody = post.getResponseBodyAsString();
            log.info("Response Status: " + result);
            log.info("Response Body: " + responseBody);

            errorLog = carbonLogReader.checkForLog("Unable to convert to SoapHeader Block", DEFAULT_TIMEOUT);
            carbonLogReader.stop();
        } finally {
            post.releaseConnection();
        }
        assertFalse(errorLog, "Mediator Hasn't invoked successfully.");
    }

}
