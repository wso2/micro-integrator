/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.ei.dataservice.integration.test.samples;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import java.rmi.RemoteException;

public class CSVSampleTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(CSVSampleTestCase.class);
    private final String serviceName = "CSVSampleService";
    private String serverEpr;
    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");

    @Factory(dataProvider = "userModeDataProvider")
    public CSVSampleTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        serverEpr = getServiceUrlHttp(serviceName);
        log.info(serviceName + " uploaded");
    }

    @Test(groups = { "wso2.dss" })
    public void testGetProducts() throws RemoteException {
        log.info("Running CSVSampleServiceTestCase#testGetProducts");
        OMElement payload = fac.createOMElement("getProducts", omNs);
        OMElement result = new AxisServiceClient().sendReceive(payload, serverEpr, "getProducts");
        Assert.assertTrue((result.toString().indexOf("Products") == 1),
                "Expected Result not found on response message");
        Assert.assertTrue(result.toString().contains("<Product>"), "Expected Result not found on response message");
        Assert.assertTrue(result.toString().contains("<ID>"), "Expected Result not found on response message");
        Assert.assertTrue(result.toString().contains("<Category>"), "Expected Result not found on response message");
        Assert.assertTrue(result.toString().contains("<Price>"), "Expected Result not found on response message");
        Assert.assertTrue(result.toString().contains("<Name>"), "Expected Result not found on response message");
    }

    @AfterClass(alwaysRun = true, groups = "wso2.dss", description = "delete service")
    public void deleteFaultyService() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

}
