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

import java.io.IOException;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.cli.util.TestUtils;

public class CliAPITestCase extends AbstractCliTest {

    private static final String CLI_SAMPLE_API_1 = "cliSampleApi_1";
    private static final String CLI_SAMPLE_API_2 = "cliSampleApi_2";
    private static final String HELLO_WORLD_API = "HelloWorld";

    @BeforeClass
    public void loginBeforeClass() throws IOException {
        super.login();
    }

    /**
     * Get information about all the APIs
     */
    @Test
    public void miShowAllApiTest() throws Exception {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.API, Constants.SHOW);

        Assert.assertEquals(outputForCLICommand.size(), 4);
        // 4:  Table Heading, cliSampleApi_1, cliSampleApi_2, HelloWorld (from the CAR file)

        String tableHeading = "NAME             URL";
        String sampleApi1TableRow = CLI_SAMPLE_API_1 + "   http://localhost:8290/cliSampleApi_1";
        String sampleApi2TableRow = CLI_SAMPLE_API_2 + "   http://localhost:8290/cliSampleApi_2";
        String helloWorldApiTableRow = HELLO_WORLD_API + "       http://localhost:8290/hello-world";

        Assert.assertEquals(outputForCLICommand.get(0), tableHeading);
        Assert.assertTrue(outputForCLICommand.contains(sampleApi1TableRow));
        Assert.assertTrue(outputForCLICommand.contains(sampleApi2TableRow));
        Assert.assertTrue(outputForCLICommand.contains(helloWorldApiTableRow));
    }

    /**
     * Get information about single APIs
     */
    @Test
    public void miShowApiTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.API,
                Constants.SHOW, CLI_SAMPLE_API_1);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - " + CLI_SAMPLE_API_1);
    }

    /**
     * Test Un-deployed API
     */
    @Test
    public void miShowApiNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.API,
                Constants.SHOW, "UndefinedAPI");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of the API 404 Not Found");
    }

    @AfterClass
    public void logoutAfterClass() throws IOException {
        super.logout();
    }
}
