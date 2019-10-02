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

public class CliCompositeappTestCase extends AbstractCliTest {

    private static final String CLI_TEST_HELLO_CAR = "hello-worldCompositeApplication";
    private static final String CLI_TEST_MEDIATOR_CAR = "MediatorCApp";

    @BeforeClass
    public void loginBeforeClass() throws IOException {
        super.login();
    }

    /**
     * Get information about all the carbon applications
     */
    @Test
    public void miShowCarbonappAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.COMPOSITEAPP, Constants.SHOW);

        Assert.assertEquals(outputForCLICommand.size(), 3);
        // 3: 2 CApps, Table Heading

        final String tableHeading = "NAME                              VERSION";
        final String mediatorCarTableRow = "MediatorCApp                      1.0.0";
        final String helloCarTableRow = "hello-worldCompositeApplication   1.0.0";

        Assert.assertEquals(outputForCLICommand.get(0), tableHeading);
        Assert.assertTrue(outputForCLICommand.contains(mediatorCarTableRow));
        Assert.assertTrue(outputForCLICommand.contains(helloCarTableRow));
    }

    /**
     * Get information about single carbon applications
     */
    @Test
    public void miShowCarbonappTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.COMPOSITEAPP,
                Constants.SHOW, CLI_TEST_HELLO_CAR);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - " + CLI_TEST_HELLO_CAR);
    }

    /**
     * Test un-deployed Carbon application
     */
    @Test
    public void miShowCappNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.COMPOSITEAPP,
                Constants.SHOW, "UndefinedCapp");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of the Carbon App 404 Not Found");
    }

    @AfterClass
    public void logoutAfterClass() throws IOException {
        super.logout();
    }
}
