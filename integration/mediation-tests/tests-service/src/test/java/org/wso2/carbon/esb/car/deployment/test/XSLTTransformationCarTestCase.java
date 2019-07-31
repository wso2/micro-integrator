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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.MicroRegistryManager;

import java.io.File;

public class XSLTTransformationCarTestCase extends ESBIntegrationTest {

    private MicroRegistryManager registryManager = null;
    String registryResource1 = "transform.xslt";
    String registryResource2 = "transform_back.xslt";

    @BeforeClass(alwaysRun = true)
    protected void uploadCarFileTest() throws Exception {
        super.init();
        registryManager = new MicroRegistryManager();

    }

    @Test(groups = { "wso2.esb" }, description = "test endpoint deployment from car file")
    public void artifactDeploymentAndServiceInvocation() throws Exception {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String sourcePath = carbonHome + File.separator + "registry";

        Assert.assertTrue(registryManager.checkResourceExist(sourcePath,"/config/", registryResource1),"Registry resources not found");
        log.info(registryResource1 + "Registry resources found");
        Assert.assertTrue(registryManager.checkResourceExist(sourcePath,"config/", registryResource2), "Registry resources not found");
        log.info(registryResource2 + "Registry resources found");

    }

}
