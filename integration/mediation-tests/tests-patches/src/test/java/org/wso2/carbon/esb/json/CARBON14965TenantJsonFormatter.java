package org.wso2.carbon.esb.json;

import org.json.JSONException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.JSONClient;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class CARBON14965TenantJsonFormatter extends ESBIntegrationTest {
    private JSONClient jsonClient;
    private String serviceUrl;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        serviceUrl = context.getContextUrls().getServiceUrl() + "/jsonproducer/";
        jsonClient = new JSONClient();
    }

    @Test
    public void testTest() throws IOException, JSONException {
        String payload = "{\"test\":\"\"}";
        String expectedResult = "{\"test\":\"\"}";
        String actualResult = jsonClient.sendUserDefineRequest(serviceUrl, payload).toString();
        assertEquals(actualResult, expectedResult, "Tenant Returned incorrectly formatted JSON response.");
    }
}
