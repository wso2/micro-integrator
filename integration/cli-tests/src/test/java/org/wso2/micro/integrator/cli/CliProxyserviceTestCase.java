/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.cli;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.List;
import util.TestUtils;

public class CliProxyserviceTestCase {

    private static final String CLI_ADDRESS_PROXY = "cliAddressProxy";
    private static final String CLI_TEST_PROXY = "cliTestProxy";

    /**
     * Get information about all the Proxy services
     */
    @Test
    public void miShowProxyAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.PROXYSERVICE, Constants.SHOW);
        String artifactName_proxy_1[] = TestUtils.getArtifactList(outputForCLICommand).get(0).split(" ", 2);
        String artifactName_proxy_2[] = TestUtils.getArtifactList(outputForCLICommand).get(1).split(" ", 2);

        Assert.assertEquals(artifactName_proxy_1[0], CLI_ADDRESS_PROXY);
        Assert.assertEquals(artifactName_proxy_2[0], CLI_TEST_PROXY);
    }

    /**
     * Get information about single proxy service
     */
    @Test
    public void miShowProxyTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.PROXYSERVICE, Constants.SHOW, CLI_TEST_PROXY);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - cliTestProxy");
    }

    /**
     * Test un-deployed proxy service
     */
    @Test
    public void miShowProxyNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.PROXYSERVICE, Constants.SHOW, "CliTestProxy");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of ProxyService 404 Not Found");
    }
}
