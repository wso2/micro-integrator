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

public class CliCompositeappTestCase {

    private static final String CLI_TEST_HELLO_CAR = "hello-worldCompositeApplication";
    private static final String CLI_TEST_MEDIATOR_CAR = "MediatorCApp";

    /**
     * Get information about all the carbon applications
     */
    @Test
    public void miShowCarbonappAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.COMPOSITAPP, Constants.SHOW);
        String artifactName_capp_1[] = TestUtils.getArtifactList(outputForCLICommand).get(0).split(" ", 2);
        String artifactName_capp_2[] = TestUtils.getArtifactList(outputForCLICommand).get(1).split(" ", 2);

        Assert.assertEquals(artifactName_capp_1[0], CLI_TEST_MEDIATOR_CAR);
        Assert.assertEquals(artifactName_capp_2[0], CLI_TEST_HELLO_CAR);

    }

    /**
     * Get information about single carbon applications
     */
    @Test
    public void miShowCarbonappTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.COMPOSITAPP, Constants.SHOW, CLI_TEST_HELLO_CAR);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - hello-worldCompositeApplication");
    }

    /**
     * Test un-deployed Carbon application
     */
    @Test
    public void miShowCappNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.COMPOSITAPP, Constants.SHOW, "TestCapp");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of the Carbon App 404 Not Found");
    }
}
