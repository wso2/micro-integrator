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
import java.util.List;
import util.TestUtils;

public class CliProxyserviceTestCase {

    private static final String CLI_ADDRESS_PROXY = "cliAddressProxy";
    private static final String CLI_TEST_PROXY = "cliTestProxy";

    /**
     * Get information about all the Proxy services
     */
    @Test
    public void miShowProxyAllTest() {

        List<String> lines =  TestUtils.runCLICommand("proxyservice" ,"show");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_PROXY)), CLI_TEST_PROXY + " Proxy service not found");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_ADDRESS_PROXY)), CLI_ADDRESS_PROXY + " Proxy service not found");
    }

    /**
     * Get information about single proxy service
     */
    @Test
    public void miShowProxyTest() {

        List<String> lines =  TestUtils.runCLICommandWithArtifactName("proxyservice" ,"show", CLI_TEST_PROXY);
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_PROXY)), CLI_TEST_PROXY + " Proxy service not found");
    }

    /**
     * Test un-deployed proxy service
     */
    @Test
    public void miShowProxyNotFoundTest() {

        List<String> lines = TestUtils.runCLICommandWithArtifactName("proxyservice", "show", "CliTestProxy");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("ProxyService 404 Not Found")), "ProxyService 404 Not Found");
    }
}
