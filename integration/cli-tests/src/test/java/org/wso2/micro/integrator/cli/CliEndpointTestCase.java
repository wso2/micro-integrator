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

public class CliEndpointTestCase {

    private static final String CLI_TEST_EP = "SimpleEP";
    private static final String CLI_STOCK_EP = "SimpleStockQuoteServiceEndpoint";

    /**
     * Get information about all the Endpoints
     */
    @Test
    public void miShowEndpointAllTest() {

        List<String> lines =  TestUtils.getOutputForCLICommand("endpoint" , "show");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_EP)), CLI_TEST_EP +" Endpoint not found");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_STOCK_EP)), CLI_STOCK_EP + "Endpoint not found");
    }

    /**
     * Get information about single Endpoint
     */

    @Test
    public void miShowEndpointTest() {

        List<String> lines =  TestUtils.getOutputForCLICommandArtifactName("endpoint" , "show", CLI_TEST_EP);
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_EP)), CLI_TEST_EP +" Endpoint not found");
    }

    /**
     * Test un-deployed Endpoint
     */
    @Test
    public void miShowEndpointNotFoundTest() {

        List<String> lines = TestUtils.getOutputForCLICommandArtifactName("endpoint", "show", "CLITestEP");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("Endpoint 404 Not Found")), "Endpoint 404 Not Found");
    }

}
