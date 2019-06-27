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

package org.wso2.carbon.esb.car.deployment.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import java.util.concurrent.TimeUnit;

public class ClassMediatorCarTestCase extends ESBIntegrationTest {

    private final String car1Name = "MediatorCApp";

    @BeforeClass(alwaysRun = true, description = "Test Car with Mediator deployment")
    protected void uploadCar1Test() throws Exception {
        super.init();
        Assert.assertTrue(checkCarbonAppExistence(car1Name), "Car file deployment failed");
        TimeUnit.SECONDS.sleep(5);
    }

    @Test(groups = { "wso2.esb" }, description = "Test Car with Mediator deployment and invocation")
    public void capp1DeploymentAndServiceInvocation() throws Exception {

        OMElement response = null;
        try {
            response = axis2Client.sendCustomQuoteRequest(getProxyServiceURLHttp("MediatorTestProxy"), null, "WSO2");
        } catch (AxisFault axisFault) {
            throw new Exception("Service Invocation Failed > " + axisFault.getMessage(), axisFault);
        }
        Assert.assertNotNull(response, "Response message null");
        Assert.assertTrue(response.toString().contains("MEDIATOR1"), "MEDIATOR1 element not found in response message");

    }

    /**
     * Not using hot deployment. Remove rest of the methods since those are not valid.
     * */

}
