/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediators.rule;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class ESBJAVA2506RuleFetchFromRegistryFailsForTheFirstTime extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    protected void uploadCarFileTest() throws Exception {
        super.init();
    }

    // TODO re-attend to this test case when https://github.com/wso2/micro-integrator/issues/388 is fixed.
    @Test(groups = "wso2.esb", description = "Test whether proxy which has Rule "
            + "mediator which fetch custom rules from registry in sequence get deployed through capp", enabled = false)
    public void testRuleMediatorProxyDeployed() throws Exception {
        org.testng.Assert
                .assertTrue(checkProxyServiceExistence("proxyService2"),
                        "ERROR - ProxyServiceDeployer ProxyService Deployment from the file : "
                                + "esb-artifacts-rule-mediator-car_1.0.0.car/proxyService2_1.0.0/proxyService2-1.0.0.xml "
                                + ": Failed");
    }

    @AfterTest(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {
        super.cleanup();
    }
}
