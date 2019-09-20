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

public class CliEndpointTestCase {

    private static final String CLI_TEST_EP = "SimpleEP";
    private static final String CLI_STOCK_EP = "SimpleStockQuoteServiceEndpoint";

    /**
     * Get information about all the Endpoints
     */
    @Test
    public void miShowEndpointAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.ENDPOINT, Constants.SHOW);
        String artifactName_ep_1[] = TestUtils.getArtifactList(outputForCLICommand).get(0).split(" ", 2);
        String artifactName_ep_2[] = TestUtils.getArtifactList(outputForCLICommand).get(1).split(" ", 2);

        Assert.assertEquals(artifactName_ep_1[0], CLI_TEST_EP);
        Assert.assertEquals(artifactName_ep_2[0], CLI_STOCK_EP);

    }


    /**
     * Get information about single Endpoint
     */

    @Test
    public void miShowEndpointTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.ENDPOINT, Constants.SHOW, CLI_TEST_EP);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - SimpleEP");
    }

    /**
     * Test un-deployed Endpoint
     */
    @Test
    public void miShowEndpointNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.ENDPOINT, Constants.SHOW, "CLITestEP");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of Endpoint 404 Not Found");
    }

}
