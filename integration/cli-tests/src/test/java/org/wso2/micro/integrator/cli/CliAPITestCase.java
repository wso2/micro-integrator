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

public class CliAPITestCase {

    private static final String CLI_SAMPLE_API_1 = "cliSampleApi_1";
    private static final String CLI_SAMPLE_API_2 = "cliSampleApi_2";

    /**
     * Get information about all the API's
     */
    @Test
    public void miShowAllApiTest() throws Exception {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.API, Constants.SHOW);
        String artifactName_api_1[] = TestUtils.getArtifactList(outputForCLICommand).get(0).split(" ", 2);
        String artifactName_api_2[] = TestUtils.getArtifactList(outputForCLICommand).get(1).split(" ", 2);

        Assert.assertEquals(artifactName_api_1[0], CLI_SAMPLE_API_1);
        Assert.assertEquals(artifactName_api_2[0], CLI_SAMPLE_API_2);

    }

    /**
     * Get information about single API's
     */
    @Test
    public void miShowApiTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.API, Constants.SHOW, CLI_SAMPLE_API_1);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - cliSampleApi_1");
    }

    /**
     * Test Un-deployed API
     */
    @Test
    public void miShowApiNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.API, Constants.SHOW, "TestAPI");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of the API 404 Not Found");
    }
}
