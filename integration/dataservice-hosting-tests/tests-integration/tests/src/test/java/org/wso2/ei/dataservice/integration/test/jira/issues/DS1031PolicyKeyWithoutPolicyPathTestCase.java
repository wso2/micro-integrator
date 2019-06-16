/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.wso2.ei.dataservice.integration.test.jira.issues;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

/**
 * This test case is written to verify the fix for https://wso2.org/jira/browse/DS-1031
 */

public class DS1031PolicyKeyWithoutPolicyPathTestCase extends DSSIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.dss", description = "Check whether service is listed as a deployed service")
    public void testServiceDeployment() throws Exception {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("select_all_Customers_operation", omNs);
        try {
            String serviceName = "PolicyKeyWithoutPolicyPathTest";
            new AxisServiceClient()
                    .sendReceive(payload, getServiceUrlHttp(serviceName), "select_all_Customers_operation");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getLocalizedMessage().contains("Transport error: 404 Error: Not Found"),
                    "Faulty service got deployed");
        }
    }

}
