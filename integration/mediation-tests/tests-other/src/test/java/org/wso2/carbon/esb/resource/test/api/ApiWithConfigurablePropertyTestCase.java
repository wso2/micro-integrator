package org.wso2.carbon.esb.resource.test.api;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ApiWithConfigurablePropertyTestCase extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        serverConfigurationManager = new ServerConfigurationManager(context);
    }

    @Test(groups = {"wso2.esb"}, description = "Configurable property", priority = 1)
    public void testConfigurablePropertyWithFile() throws IOException {
        Map<String, String> headers = new HashMap<>();
        URL endpoint = new URL(getApiInvocationURL("apiConfig/test"));
        HttpResponse httpResponse = HttpRequestUtil.doGet(endpoint.toString(), headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(StringUtils.normalizeSpace(httpResponse.getData()),
                StringUtils.normalizeSpace("{ \"name\": \"file\", \"msg\": \"Gd mng\" }"),
                StringUtils.normalizeSpace(httpResponse.getData()));
    }

    @Test(groups = {"wso2.esb"}, description = "Configurable property", priority = 2)
    public void testConfigurablePropertyWithSystemProperty() throws IOException, AutomationUtilException {
        Map<String, String> commands = new HashMap<>();
        commands.put("-Dname", "sys");
        commands.put("-Dmsg", "Hi");
        serverConfigurationManager.restartMicroIntegrator(commands);
        Map<String, String> headers = new HashMap<>();
        URL endpoint = new URL(getApiInvocationURL("apiConfig/test"));
        HttpResponse httpResponse = HttpRequestUtil.doGet(endpoint.toString(), headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(StringUtils.normalizeSpace(httpResponse.getData()),
                StringUtils.normalizeSpace("{ \"name\": \"sys\", \"msg\": \"Hi\" }"),
                StringUtils.normalizeSpace(httpResponse.getData()));
    }

    @Test(groups = {"wso2.esb"}, description = "Configurable property", priority = 3)
    public void testConfigurablePropertyWithEnvVariable() throws IOException, AutomationUtilException {
        Map<String, String> commands = new HashMap<>();
        commands.put("--env-file", FrameworkPathUtil.getSystemResourceLocation() + ".env");
        serverConfigurationManager.restartMicroIntegrator(commands);
        Map<String, String> headers = new HashMap<>();
        URL endpoint = new URL(getApiInvocationURL("apiConfig/test"));
        HttpResponse httpResponse = HttpRequestUtil.doGet(endpoint.toString(), headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(StringUtils.normalizeSpace(httpResponse.getData()),
                StringUtils.normalizeSpace("{ \"name\": \"env\", \"msg\": \"Hello\" }"),
                StringUtils.normalizeSpace(httpResponse.getData()));
    }

    @Test(groups = {"wso2.esb"}, description = "Configurable property", priority = 4)
    public void testConfigurableProperty() throws IOException, AutomationUtilException {
        Map<String, String> commands = new HashMap<>();
        commands.put("-Dname", "sys");
        commands.put("-Dmsg", "Hi");
        commands.put("--env-file", FrameworkPathUtil.getSystemResourceLocation() + ".env");
        serverConfigurationManager.restartMicroIntegrator(commands);
        Map<String, String> headers = new HashMap<>();
        URL endpoint = new URL(getApiInvocationURL("apiConfig/test"));
        HttpResponse httpResponse = HttpRequestUtil.doGet(endpoint.toString(), headers);
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(StringUtils.normalizeSpace(httpResponse.getData()),
                StringUtils.normalizeSpace("{ \"name\": \"env\", \"msg\": \"Hello\" }"),
                StringUtils.normalizeSpace(httpResponse.getData()));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
