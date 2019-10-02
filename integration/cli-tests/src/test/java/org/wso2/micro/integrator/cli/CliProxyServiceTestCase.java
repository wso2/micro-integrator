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

public class CliProxyServiceTestCase extends AbstractCliTest{

    private static final String CLI_TEST_PROXY = "cliTestProxy";
    private static final String CLI_ADDRESS_PROXY = "cliAddressProxy";
    private static final String CLI_MEDIATOR_PROXY = "MediatorTestProxy";

    @BeforeClass
    public void loginBeforeClass() throws IOException {
        super.login();
    }

    /**
     * Get information about all the Proxy services
     */
    @Test
    public void miShowProxyAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.PROXYSERVICE, Constants.SHOW);

        Assert.assertEquals(outputForCLICommand.size(), 4);
        // 4: Table heading, cliTestProxy, cliAddressProxy, MediatorTestProxy

        String tableHeading = "NAME                WSDL 1.1                                                   WSDL 2.0";

        String testProxyTableRow = "MediatorTestProxy   " +
                "http://localhost:8290/services/MediatorTestProxy?wsdl   " +
                "http://localhost:8290/services/MediatorTestProxy?wsdl2";
        String mediatorProxyTableRow = "cliTestProxy        " +
                "http://localhost:8290/services/cliTestProxy?wsdl        " +
                "http://localhost:8290/services/cliTestProxy?wsdl2";
        String addressProxyTableRow = "cliAddressProxy     " +
                "http://localhost:8290/services/cliAddressProxy?wsdl     " +
                "http://localhost:8290/services/cliAddressProxy?wsdl2";


        Assert.assertEquals(outputForCLICommand.get(0), tableHeading);
        Assert.assertTrue(outputForCLICommand.contains(testProxyTableRow));
        Assert.assertTrue(outputForCLICommand.contains(mediatorProxyTableRow));
        Assert.assertTrue(outputForCLICommand.contains(addressProxyTableRow));
    }

    /**
     * Get information about single proxy service
     */
    @Test
    public void miShowProxyTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.PROXYSERVICE,
                Constants.SHOW, CLI_TEST_PROXY);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - " + CLI_TEST_PROXY);
    }

    /**
     * Test un-deployed proxy service
     */
    @Test
    public void miShowProxyNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.PROXYSERVICE,
                Constants.SHOW, "UndefinedProxy");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of ProxyService 404 Not Found");
    }

    @AfterClass
    public void logoutAfterClass() throws IOException {
        super.logout();
    }
}
