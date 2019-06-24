package org.wso2.carbon.esb.mediators.clone;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * Test clone mediator if it reach error sequence on error.
 * This test case is for fix done for ESBjAVA-4913
 */
public class ESBJAVA4913HandleExceptionTest extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        // Initializing server configuration
        super.init();
        verifyAPIExistence("ESBJAVA4913testapi");
    }

    /**
     * Verifies whether the mediator reach error sequence on error while executing.
     */
    @Test(groups = "wso2.esb", description = "Check if clone mediator reach error sequence on error.")
    public void testExceptionHandlingInCloneMediator() {

        final String expectedErrorMsg = "This is error sequence from sequenceOne";
        CarbonLogReader carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
        // invoking the service through the test api.
        try {
            HttpRequestUtil.sendGetRequest(getApiInvocationURL("clonetest"), "");
        } catch (Exception e) {
            // Ignore read timeout from get request.
        }
        String logs = carbonLogReader.getLogs();
        boolean isExpectedErrorMessageFound = logs.contains(expectedErrorMsg);
        carbonLogReader.stop();
        /*
         * Asserting the results here. If there's no logs from error sequence, then the
         * assertion should fail.
         */
        Assert.assertTrue(isExpectedErrorMessageFound, "Error sequence logs not found in the LOG stream.");
    }

}
