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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.servers.SimpleHTTPServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class PreserveXmlProcessingInstructionsPropertyTestCase extends ESBIntegrationTest {

    private static final String TARGET_API_CONTEXT = "testPreserveXmlProcessingInstructionProperty";
    private static final String XML_PROCESSING_INSTRUCTION_1 = "<?xml-stylesheet type=\"text/xsl\" " +
            "href=\"C:\\abc.xslt\"?>";
    private static final String XML_PROCESSING_INSTRUCTION_2 = "<?xml-stylesheet type=\"text/xsl\" " +
            "href=\"ftp://xyz.com/abc.xslt\"?>";
    private SimpleHTTPServer simpleHTTPServer;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

        //Prepare and start mock back end server
        int port = 8089;
        simpleHTTPServer = new SimpleHTTPServer(port);
        simpleHTTPServer.createContext("/testBE", new XmlResponseBEHandler());
        simpleHTTPServer.start();//start server
    }

    @Test(groups = "wso2.esb", description = "Test without PRESERVE_XML_PROCESSING_INSTRUCTIONS Property")
    public void testWithoutXmlProcessingInstructionsProperty() throws Exception {
        SimpleHttpClient httpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();

        // Test whether the xml processing instructions are removed when the WRITE_XML_DECLARATION property is not
        // configured.
        String url = getApiInvocationURL(TARGET_API_CONTEXT) + "/withoutProperty";

        HttpResponse response = httpClient.doGet(url, headers);
        String responsePayload = httpClient.getResponsePayload(response);

        Assert.assertNotNull(responsePayload, "Error occurred while retrieving response payload: entity null");
        Assert.assertFalse(responsePayload.contains(XML_PROCESSING_INSTRUCTION_1) ||
                responsePayload.contains(XML_PROCESSING_INSTRUCTION_2), "XML processing instructions are " +
                "preserved without the PRESERVE_XML_PROCESSING_INSTRUCTIONS property");
    }

    @Test(groups = "wso2.esb", description = "Test with PRESERVE_XML_PROCESSING_INSTRUCTIONS Property")
    public void testWithXmlProcessingInstructionsProperty() throws Exception {
        SimpleHttpClient httpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();

        // Test whether the xml processing instructions are present when the WRITE_XML_DECLARATION property is
        // configured true.
        String url = getApiInvocationURL(TARGET_API_CONTEXT) + "/withProperty";

        HttpResponse response = httpClient.doGet(url, headers);
        String responsePayload = httpClient.getResponsePayload(response);

        Assert.assertNotNull(responsePayload, "Error occurred while retrieving response payload: entity null");
        Assert.assertTrue(responsePayload.contains(XML_PROCESSING_INSTRUCTION_1) &&
                responsePayload.contains(XML_PROCESSING_INSTRUCTION_2), "XML processing instructions are not " +
                "preserved even though the WRITE_XML_DECLARATION property is configured true ");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        simpleHTTPServer.stop();//shutdown mock BE server
        super.cleanup();
    }

    /**
     * HttpHandler implementation to handle request
     */
    private static class XmlResponseBEHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Content-Type", "application/xml");

            //response payload
            String response = XML_PROCESSING_INSTRUCTION_1 + "\n" +
                    XML_PROCESSING_INSTRUCTION_2 + "\n" +
                    "<Hello>World</Hello>";

            httpExchange.sendResponseHeaders(200, response.length());
            try (OutputStream responseStream = httpExchange.getResponseBody()) {
                responseStream.write(response.getBytes(Charset.defaultCharset()));
            }
        }
    }
}
