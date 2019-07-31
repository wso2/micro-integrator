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

package org.wso2.carbon.esb.car.deployment.test;

import org.awaitility.Awaitility;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.concurrent.TimeUnit;

public class ESBJAVA3611EndpointTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    protected void uploadCarFileTest() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.esb", enabled = true, description = "Test whether Endpoint get deployed in tenant through  capp")
    public void testEndpointDeployed() throws Exception {
        checkEndpointExistence("Axis2ServiceCSEndPoint");
    }



    @AfterTest(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {
        super.cleanup();
    }

}
