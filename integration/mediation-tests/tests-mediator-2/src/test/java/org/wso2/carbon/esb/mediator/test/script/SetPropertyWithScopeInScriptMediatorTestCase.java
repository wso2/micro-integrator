package org.wso2.carbon.esb.mediator.test.script;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.stockquoteclient.StockQuoteClient;

public class SetPropertyWithScopeInScriptMediatorTestCase extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }

    @Test(groups = "wso2.esb", description = "Set a property with axis2 scope in script mediator")
    public void testSetPropertyWithAxis2ScopeInScript() throws Exception {
        carbonLogReader.clearLogs();
        StockQuoteClient axis2Client1 = new StockQuoteClient();
        axis2Client1.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("scriptMediatorSetPropertyWithScopeTestProxy"),
                null, "WSO2");

        boolean setPropertyInLog = Utils.checkForLog(carbonLogReader, "Axis2_Property = AXIS2_PROPERTY", 10);
        Assert.assertTrue(setPropertyInLog, " The property with axis2 scope is not set ");
        boolean removePropertyInLog = Utils.checkForLog(carbonLogReader, "Axis2_Property_After_Remove = null", 10);
        Assert.assertTrue(removePropertyInLog, " The property with axis2 scope is not remove ");
    }

    @Test(groups = "wso2.esb", description = "Set a property with transport scope in script mediator")
    public void testSetPropertyWithTransportScopeInScript() throws Exception {
        carbonLogReader.clearLogs();
        StockQuoteClient axis2Client1 = new StockQuoteClient();
        axis2Client1.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("scriptMediatorSetPropertyWithScopeTestProxy"),
                null, "WSO2");
        boolean setPropertyInLog = Utils.checkForLog(carbonLogReader, "Transport_Property = TRANSPORT_PROPERTY", 10);
        Assert.assertTrue(setPropertyInLog, " The property with transport scope is not set ");
        boolean removePropertyInLog = Utils.checkForLog(carbonLogReader, "Transport_Property_After_Remove = null", 10);
        Assert.assertTrue(removePropertyInLog, " The property with axis2 transport is not remove ");
    }

    @Test(groups = "wso2.esb", description = "Set a property with operation scope in script mediator")
    public void testSetPropertyWithOperationScopeInScript() throws Exception {
        carbonLogReader.clearLogs();
        StockQuoteClient axis2Client1 = new StockQuoteClient();
        axis2Client1.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("scriptMediatorSetPropertyWithScopeTestProxy"),
                null, "WSO2");

        boolean setPropertyInLog = Utils.checkForLog(carbonLogReader, "Operation_Property = OPERATION_PROPERTY", 10);
        Assert.assertTrue(setPropertyInLog, " The property with operation scope is not set ");

        boolean removePropertyInLog = Utils.checkForLog(carbonLogReader, "Operation_Property_After_Remove = null", 10);
        Assert.assertTrue(removePropertyInLog, " The property with operation scope is not remove ");
    }
}
