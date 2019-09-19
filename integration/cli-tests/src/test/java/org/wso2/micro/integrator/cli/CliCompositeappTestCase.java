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

public class CliCompositeappTestCase {

    private static final String CLI_TEST_HELLO_CAR = "hello-worldCompositeApplication";
    private static final String CLI_TEST_MEDIATOR_CAR = "MediatorCApp";

    /**
     * Get information about all the carbon applications
     */

    @Test
    public void miShowCarbonappAllTest() {

        List<String> lines =  TestUtils.getOutputForCLICommand("compositeapp" , "show");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_HELLO_CAR)), CLI_TEST_HELLO_CAR +" Carbon application not found");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_MEDIATOR_CAR)), CLI_TEST_MEDIATOR_CAR + " Carbon application not found");
    }

    /**
     * Get information about single carbon applications
     */

    @Test
    public void miShowCarbonappTest() {

        List<String> lines =  TestUtils.getOutputForCLICommandArtifactName("compositeapp" , "show", CLI_TEST_HELLO_CAR);
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_HELLO_CAR)), CLI_TEST_HELLO_CAR +" Carbon application not Found");
    }

    /**
     * Test un-deployed Carbon application
     */
    @Test
    public void miShowCappNotFoundTest() {

        List<String> lines =  TestUtils.getOutputForCLICommandArtifactName("compositeapp" , "show", "TestCapp");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("Carbon App 404 Not Found")),"Carbon App 404 Not Found");
    }
}
