package org.wso2.carbon.esb.synapse.common.resolvers;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This test case is to validate correctly resolving the file property variables
 */
public class FilePropertyResolverTestCase extends ESBIntegrationTest {

    private static final String targetApiName = "filePropertyTestAPI";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

    }

    @Test(groups = "wso2.esb" , description = "This method is used to verify the resolved variable from file property")
    public void testFilePropertyResolveVariable() throws IOException {

        String contentType = "text/xml";
        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <soapenv:Header/>\n" +
                "<soapenv:Body>\n" +
                "\t<m0:getQuote xmlns:m0=\"http://services.samples\">\n" +
                "        <m0:request>\n" +
                "            <m0:symbol>WSO2</m0:symbol>\n" +
                "        </m0:request>\n" +
                "     </m0:getQuote>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String url = getApiInvocationURL(targetApiName);

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("Content-Type", contentType);
        headers.put("SOAPAction", "urn:mediate");

        SimpleHttpClient httpClient = new SimpleHttpClient();
        HttpResponse response = httpClient.doPost(url, headers, payload, contentType);
        String responsePayload = httpClient.getResponsePayload(response);
        boolean ResponseContainsWSO2Info = responsePayload.contains("getQuoteResponse") && responsePayload.contains("WSO2");
        Assert.assertTrue(ResponseContainsWSO2Info);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}