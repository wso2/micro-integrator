package org.wso2.carbon.esb.registry.caching;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.MicroRegistryManager;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;

/**
 * ESBJAVA-3267
 * When cachableDuration is 0 or not specified in the tag, the resources are cached forever.
 * This needs to be corrected as "no caching" if the entry is 0.
 */

public class CachableDurationTestCase extends ESBIntegrationTest {

    private Log logger = LogFactory.getLog(CachableDurationTestCase.class);
    private MicroRegistryManager registryManager;
    private CarbonLogReader carbonLogReader;

    private static final String NAME = "cache_test";
    private static final String PATH = "conf:/";
    private static final String RESOURCE_PATH = "cache";
    private static final String OLD_VALUE = "123456789";
    private static final String NEW_VALUE = "987654321";

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {

        super.init();
        registryManager = new MicroRegistryManager();
        carbonLogReader = new CarbonLogReader();
        String sourceFile = getESBResourceLocation() + File.separator + "synapseconfig" + File.separator + "registry"
                + File.separator + "caching" + File.separator + "registry.xml";
        String registryConfig = FileUtils.readFileToString(new File(sourceFile));
        Utils.deploySynapseConfiguration(AXIOMUtil.stringToOM(registryConfig), "registry", "", true);
        uploadResourcesToConfigRegistry();
    }

    @Test(groups = "wso2.esb",
            description = "ESBRegistry cachableDuration 0 property test")
    public void testCachableDuration() throws Exception {

        carbonLogReader.start();
        clearLogsAndSendRequest();
        Assert.assertTrue(Utils.checkForLog(carbonLogReader, OLD_VALUE, 10),
                          "Expected value : " + OLD_VALUE + " not found in logs.");
        updateResourcesInConfigRegistry();
        Assert.assertEquals(NEW_VALUE, registryManager.getProperty(PATH, RESOURCE_PATH, NAME));
        clearLogsAndSendRequest();
        Assert.assertTrue(Utils.checkForLog(carbonLogReader, NEW_VALUE, 10),
                          "Expected value : " + NEW_VALUE + " not found in logs.");
    }

    private void clearLogsAndSendRequest() {

        try {
            carbonLogReader.clearLogs();
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("CachableDurationTestCaseProxy"),
                                                    getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                    "IBM");
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

    private void uploadResourcesToConfigRegistry() throws Exception {

        registryManager.addProperty(PATH, RESOURCE_PATH, NAME, OLD_VALUE);
    }

    private void updateResourcesInConfigRegistry() {

        try {
            // Add a resource to cache resource due to limitation as in https://github.com/wso2/micro-integrator/issues/536
            String sampleFile =
                    getESBResourceLocation() + File.separator + "synapseconfig" + File.separator + "registry"
                            + File.separator + "caching" + File.separator + "sample.txt";
            registryManager.addResource(PATH + RESOURCE_PATH, sampleFile);
            registryManager.updateProperty(PATH, RESOURCE_PATH, NAME, NEW_VALUE, true);
            Thread.sleep(5000);

        } catch (Exception e) {
            logger.error("Error while updating the registry property", e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void unDeployService() throws Exception {

        registryManager.restoreOriginalResources();
        // restore to original registry.xml file
        String sourceFile = getESBResourceLocation() + File.separator + "synapseconfig" + File.separator + "registry"
                + File.separator + "caching" + File.separator + "registry_original.xml";
        String registryConfig = FileUtils.readFileToString(new File(sourceFile));
        Utils.deploySynapseConfiguration(AXIOMUtil.stringToOM(registryConfig), "registry", "", true);
        carbonLogReader.stop();
    }

}
