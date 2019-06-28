/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediator.test.classMediator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;
import javax.xml.namespace.QName;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PropertyPersistenceDeletingTestCase extends ESBIntegrationTest {

    private static final String CLASS_JAR_FIVE_PROPERTIES = "org.wso2.carbon.test.mediator.stockmediator-v1.0.jar";
    private static final String CLASS_JAR_THREE_PROPERTIES = "org.wso2.carbon.test.mediator.stockmediator-v1.0.1.jar";
    private static final String JAR_LOCATION = "/artifacts/ESB/jar";

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.copyToComponentLib(
                new File(getClass().getResource(JAR_LOCATION + File.separator + CLASS_JAR_FIVE_PROPERTIES).toURI()));
        OMElement class_five_properties = AXIOMUtil.stringToOM(FileUtils.readFileToString(
                new File(getESBResourceLocation() + File.separator + "mediatorconfig" + File.separator +
                        "class" + File.separator + "class_property_persistence_five_properties.xml")));
        Utils.deploySynapseConfiguration(class_five_properties, "class_property_persistence_five_properties",
                "proxy-services", true);
        super.init();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb", "localOnly" }, description = "Class Mediator "
            + " -Class mediator property persistence -deleting properties")
    public void testMediationPropertyPersistenceDeleting() throws Exception {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp(
                "class_property_persistence_five_properties"),
                getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");

        String lastPrice = response.getFirstElement()
                .getFirstChildWithName(new QName("http://services.samples/xsd", "last")).getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        String symbol = response.getFirstElement()
                .getFirstChildWithName(new QName("http://services.samples/xsd", "symbol")).getText();
        assertEquals(symbol, "WSO2", "Fault: value 'symbol' mismatched");

        //TODO Log Assertion
        /*
        INFO - StockQuoteMediator Starting Mediation -ClassMediator
        INFO - StockQuoteMediator Initialized with User:[esb user]
        INFO - StockQuoteMediator E-MAIL:[user@wso2.org]
        INFO - StockQuoteMediator Massage Id:
        INFO - StockQuoteMediator Original price:
        INFO - StockQuoteMediator Discounted price:
        INFO - StockQuoteMediator After taxed price:
        INFO - StockQuoteMediator Logged....

        refer: https://wso2.org/jira/browse/TA-532           param 5
         */

        serverConfigurationManager.removeFromComponentLib(CLASS_JAR_FIVE_PROPERTIES);
        Utils.undeploySynapseConfiguration("class_property_persistence_five_properties", "proxy-services", false);
        serverConfigurationManager.copyToComponentLib(
                new File(getClass().getResource(JAR_LOCATION + File.separator + CLASS_JAR_THREE_PROPERTIES).toURI()));
        OMElement class_three_properties = AXIOMUtil.stringToOM(FileUtils.readFileToString(
                new File(getESBResourceLocation() + File.separator + "mediatorconfig" + File.separator +
                        "class" + File.separator + "class_property_persistence_three_properties.xml")));
        Utils.deploySynapseConfiguration(class_three_properties, "class_property_persistence_three_properties",
                "proxy-services", true);
        /* waiting for the new config file to be written to the disk */
        Thread.sleep(10000);

        super.init();

        response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp(
                "class_property_persistence_three_properties"),
                getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "IBM");

        lastPrice = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "last"))
                .getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        symbol = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "symbol"))
                .getText();
        assertEquals(symbol, "IBM", "Fault: value 'symbol' mismatched");

        //TODO Log Assertion
        /*
        INFO - StockQuoteMediator Starting Mediation -ClassMediator
        INFO - StockQuoteMediator Massage Id:
        INFO - StockQuoteMediator Original price:
        INFO - StockQuoteMediator Discounted price:
        INFO - StockQuoteMediator After taxed price:
        INFO - StockQuoteMediator Logged....

        User and email removed     refer: https://wso2.org/jira/browse/TA-532           param 3
         */

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        Utils.undeploySynapseConfiguration("class_property_persistence_three_properties", "proxy-services");
        serverConfigurationManager.removeFromComponentLib(CLASS_JAR_THREE_PROPERTIES);
        serverConfigurationManager = null;
    }
}
