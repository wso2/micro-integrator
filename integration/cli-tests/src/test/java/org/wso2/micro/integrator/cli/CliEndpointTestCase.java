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

public class CliEndpointTestCase extends AbstractCliTest {

    private static final String CLI_SIMPLE_EP = "SimpleEP";
    private static final String CLI_STOCK_EP = "SimpleStockQuoteServiceEndpoint";

    @BeforeClass
    public void loginBeforeClass() throws IOException {
        super.login();
    }

    /**
     * Get information about all the Endpoints
     */
    @Test
    public void miShowEndpointAllTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.ENDPOINT, Constants.SHOW);

        Assert.assertEquals(outputForCLICommand.size(), 3);
        // 3: Table heading, SimpleEP, SimpleStockQuoteServiceEndpoint

        final String TABLE_HEADING = "NAME                              TYPE      Active";
        final String SIMPLE_EP_TABLE_ROW = "SimpleEP                          address   true";
        final String STOCK_EP_TABLE_ROW = "SimpleStockQuoteServiceEndpoint   address   true";

        Assert.assertEquals(outputForCLICommand.get(0), TABLE_HEADING);
        Assert.assertTrue(outputForCLICommand.contains(SIMPLE_EP_TABLE_ROW));
        Assert.assertTrue(outputForCLICommand.contains(STOCK_EP_TABLE_ROW));
    }


    /**
     * Get information about single Endpoint
     */

    @Test
    public void miShowEndpointTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.ENDPOINT,
                Constants.SHOW, CLI_SIMPLE_EP);
        Assert.assertEquals(outputForCLICommand.get(0), "Name - " + CLI_SIMPLE_EP);
    }

    /**
     * Test un-deployed Endpoint
     */
    @Test
    public void miShowEndpointNotFoundTest() throws IOException {

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommandArtifactName(Constants.ENDPOINT,
                Constants.SHOW, "UndefinedEndpoint");
        Assert.assertEquals(outputForCLICommand.get(0), "[ERROR] Getting Information of Endpoint 404 Not Found");
    }

    @AfterClass
    public void logoutAfterClass() throws IOException {
        super.logout();
    }

}
