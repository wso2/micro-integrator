/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.proxyservice.test.proxyservices;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import static org.testng.Assert.assertTrue;

/**
 * This class contains test cases to verify the correctness of XML responses received by the system for Proxy
 * Services when handling multipart/form-data.
 */
public class MultipartFormDataProxyTestCase extends ESBIntegrationTest {
    
    private final String formDataProxyFilePath = TestConfigurationProvider.getResourceLocation(
            ESBTestConstant.ESB_PRODUCT_GROUP) + "/proxyconfig/proxy/proxyservice/";
    private final String formDataProxyName = "formDataProxy";
    private final String xopNamespace = "xmlns:xop=\"http://www.w3.org/2004/08/xop/include\"";
    private final String synapseNamespace = "xmlns=\"http://ws.apache.org/ns/synapse\"";
    private final String anprNamespace = "xmlns:ns2=\"http://sogei.it/ANPR/6001certificazione\"";
    private final String errorLogMessage0 = "Response doesn't contain the expected namespaces";
    private final String errorLogMessage1 = "Fails to handle multiple namespaces in the same element";
    private final String contentTypeWithBoundary =
            "multipart/form-data boundary=MIMEBoundary_698d069bc4fe41437cf1497e126c77667239b5d36d8b5f8f";
    public static final String SPACE_SEPARATOR = " ";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb",
            description = "This test case verifies that a multipart/form-data response includes the expected namespaces")
    public void testMultipartFormDataResponseContainsExpectedNamespaces() throws Exception {

        deployProxyService(formDataProxyName, formDataProxyFilePath);
        Thread.sleep(15000);
        String payload = "<Risposta6001 xmlns=\"http://ws.apache.org/ns/synapse\" "
                + "xmlns:ns2=\"http://sogei.it/ANPR/6001certificazione\">\n"
                + "    <ns2:Risposta6001OK>\n"
                + "        <file>\n"
                + "            <certificatoPdf>\n"
                + "                <xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" "
                + "href=\"cid:2a9f5df2-1719-401b-a4c8-a6df5f0301de@apache.org\"/>\n"
                + "            </certificatoPdf>\n"
                + "        </file>\n"
                + "    </ns2:Risposta6001OK>\n"
                + "</Risposta6001>";

        StringReader requestBody = new StringReader(payload);
        StringWriter postResponseData = new StringWriter();
        URL postRestURL = new URL(getProxyServiceURLHttp(formDataProxyName));
        HttpURLConnectionClient.sendPostRequest(requestBody, postRestURL, postResponseData, contentTypeWithBoundary);
        String postResponse = postResponseData.toString();

        assertTrue(postResponse.contains(xopNamespace) && postResponse.contains(synapseNamespace), errorLogMessage0);
        assertTrue(postResponse.contains(synapseNamespace + SPACE_SEPARATOR + anprNamespace), errorLogMessage1);
    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        super.cleanup();
    }

}
