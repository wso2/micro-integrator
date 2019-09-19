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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.List;
import util.TestUtils;

public class CliRemoteTestCase {

    protected Log log = LogFactory.getLog(CliRemoteTestCase.class);
    ProcessBuilder builder = null;
    private static final String CLI_TEST_REMOTE_SERVER = "TestServer";
    private static final String REMOTE_HOST = "192.168.1.15";
    private static final String GET_REMOTE_HOST_UPDATE = "192.168.1.17";
    private static final String REMOTE_PORT = "9164";


    /**
     * Test to add a Micro Integrator server
     */
    @Test(priority=1)
    public void miAddRemoteServer() throws IOException {

        builder = new ProcessBuilder(TestUtils.getMIBuildPath(), "remote" , "add", CLI_TEST_REMOTE_SERVER, REMOTE_HOST, REMOTE_PORT);
        builder.start();

        List<String> lines =  TestUtils.getOutputForCLICommand("remote" , "show");
        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_REMOTE_SERVER))," - Fail to add remote server - "+ CLI_TEST_REMOTE_SERVER);
    }

    /**
     * Test to Update the hostname of Micro Integrator server
     */
    @Test(priority=2)
    public void miUpdateRemoteServer() throws IOException {

        builder = new ProcessBuilder(TestUtils.getMIBuildPath(), "remote" , "update", CLI_TEST_REMOTE_SERVER, GET_REMOTE_HOST_UPDATE, REMOTE_PORT);
        builder.start();

        List<String> lines =  TestUtils.getOutputForCLICommand("remote" , "show");

        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(GET_REMOTE_HOST_UPDATE)),"Fail to Update host of the remote server of "+ CLI_TEST_REMOTE_SERVER);
        log.info("Successfully update host of the remote server of " + CLI_TEST_REMOTE_SERVER);
    }

    /**
     * Test to select the current Micro Integrator server
     */
    @Test(priority=3)
    public void miSelectRemoteServer() throws IOException {

        builder = new ProcessBuilder(TestUtils.getMIBuildPath(), "remote" , "select", CLI_TEST_REMOTE_SERVER);
        builder.start();

        List<String> lines =  TestUtils.getOutputForCLICommand("remote" , "show");

        Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("current_server: "+ CLI_TEST_REMOTE_SERVER)),"Fail to select "+ CLI_TEST_REMOTE_SERVER +" as current remote server");
        log.info("Successfully select "+ CLI_TEST_REMOTE_SERVER +" as current remote server");
    }

    /**
     * Test to remove a Micro Integrator server
     */
    @Test(priority=4)
    public void miRemoveRemoteServer() throws IOException {

        builder = new ProcessBuilder(TestUtils.getMIBuildPath(), "remote" , "remove", CLI_TEST_REMOTE_SERVER);
        builder.start();

        List<String> lines =  TestUtils.getOutputForCLICommand("remote" , "show");
        Assert.assertNotEquals(lines.stream().anyMatch(str -> str.trim().contains(CLI_TEST_REMOTE_SERVER)),"Fail to remove "+ CLI_TEST_REMOTE_SERVER);
        log.info("Successfully remove "+ CLI_TEST_REMOTE_SERVER);
    }
}
