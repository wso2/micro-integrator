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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.cli.util.TestUtils;

import static org.wso2.micro.integrator.cli.util.TestUtils.getMIBuildPath;

public class LoginLogoutTest extends AbstractCliTest {
    private static final Log LOG = LogFactory.getLog(LoginLogoutTest.class);

    @Test
    public void loginLogoutTest() throws IOException {
        List<String> loginOutput = super.login();
        Assert.assertTrue(loginOutput.get(0).contains("Login successful for remote"));

        List<String> logoutOutput = super.logout();
        Assert.assertTrue(logoutOutput.get(0).contains("Successfully logged out of the current remote"));

        LOG.info("Accessing a protected resource after logging out");
        List<String> outputForCLICommand = TestUtils.runCommandWithArgs(new String[]{
                getMIBuildPath(), Constants.API, Constants.SHOW});
        Assert.assertTrue(outputForCLICommand.get(0).contains("User not logged in or session timed out"));
    }
}
