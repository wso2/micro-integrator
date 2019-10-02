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

import static org.wso2.micro.integrator.cli.util.TestUtils.getMIBuildPath;
import static org.wso2.micro.integrator.cli.util.TestUtils.runCommandWithArgs;

public abstract class AbstractCliTest {
    private static final Log LOG = LogFactory.getLog(AbstractCliTest.class);

    List<String> login() throws IOException {
        LOG.info("Logging in to Micro Integrator Management API");
        // mi remote login admin admin
        String[] arguments = new String[]{getMIBuildPath(), Constants.REMOTE, Constants.LOGIN,
                Constants.DEFAULT_USERNAME, Constants.DEFAULT_PASSWORD};
        return runCommandWithArgs(arguments);
    }

    List<String> logout() throws IOException {
        LOG.info("Logging out of Micro Integrator Management API");
        // mi remote logout
        String[] arguments = new String[]{getMIBuildPath(), Constants.REMOTE, Constants.LOGOUT};
        return runCommandWithArgs(arguments);
    }
}
