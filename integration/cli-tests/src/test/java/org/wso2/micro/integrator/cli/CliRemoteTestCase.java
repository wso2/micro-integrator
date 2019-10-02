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
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.cli.util.TestUtils;

public class CliRemoteTestCase {

    ProcessBuilder builder = null;
    private static final String CLI_TEST_REMOTE_SERVER = "TestServer";
    private static final String REMOTE_HOST = "192.168.1.15";
    private static final String GET_REMOTE_HOST_UPDATE = "192.168.1.17";
    private static final String REMOTE_PORT = "9164";

    /**
     * Test to add a Micro Integrator server
     */
    @Test( priority = 1 )
    public void miAddRemoteServer() throws IOException, InterruptedException {

        builder = new ProcessBuilder(TestUtils.getMIBuildPath(), Constants.REMOTE, Constants.REMOTE_ADD,
                CLI_TEST_REMOTE_SERVER, REMOTE_HOST, REMOTE_PORT);
        builder.start();
        TimeUnit.MILLISECONDS.sleep(3000);

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.REMOTE, Constants.SHOW);

        Assert.assertEquals(outputForCLICommand.get(0), "remotes:");
        Assert.assertEquals(outputForCLICommand.get(1), CLI_TEST_REMOTE_SERVER + ":");
        Assert.assertEquals(outputForCLICommand.get(2), "remote_address: " + REMOTE_HOST);
        Assert.assertEquals(outputForCLICommand.get(3), "remote_port: \"" + REMOTE_PORT + "\"");

    }

    /**
     * Test to Update the hostname of Micro Integrator server
     */
    @Test( priority = 2 )
    public void miUpdateRemoteServer() throws IOException, InterruptedException {

        builder = new ProcessBuilder(TestUtils.getMIBuildPath(), Constants.REMOTE, Constants.REMOTE_UPDATE,
                CLI_TEST_REMOTE_SERVER, GET_REMOTE_HOST_UPDATE, REMOTE_PORT);
        builder.start();
        TimeUnit.MILLISECONDS.sleep(3000);

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.REMOTE, Constants.SHOW);

        Assert.assertTrue(outputForCLICommand.stream().anyMatch(str -> str.trim().contains(GET_REMOTE_HOST_UPDATE)),
                "Fail to Update host of the remote server of " + CLI_TEST_REMOTE_SERVER);

    }

    /**
     * Test to remove a Micro Integrator server
     */
    @Test( priority = 4 )
    public void miRemoveRemoteServer() throws IOException, InterruptedException {

        builder = new ProcessBuilder(TestUtils.getMIBuildPath(), Constants.REMOTE, Constants.REMOTE_REMOVE,
                CLI_TEST_REMOTE_SERVER);
        builder.start();
        TimeUnit.MILLISECONDS.sleep(3000);

        List<String> outputForCLICommand = TestUtils.getOutputForCLICommand(Constants.REMOTE, Constants.SHOW);
        Assert.assertNotEquals(outputForCLICommand.stream().anyMatch(str -> str.trim().contentEquals(CLI_TEST_REMOTE_SERVER)),
                "Fail to remove " + CLI_TEST_REMOTE_SERVER);
    }
}
