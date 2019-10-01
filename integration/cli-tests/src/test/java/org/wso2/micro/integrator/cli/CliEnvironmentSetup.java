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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeSuite;

/**
 * Setup the environment to run the tests
 */

public class CliEnvironmentSetup extends AbstractCliTest {

    private static final Log LOG = LogFactory.getLog(CliEnvironmentSetup.class);

    @BeforeSuite
    public void setupEnv() throws IOException {

        Process process;
        String line;
        String[] setup = {"sh", ".." + File.separator + "src" + File.separator + "test" +
                File.separator + "java" + File.separator + "EnvSetup.sh"};
        process = Runtime.getRuntime().exec(setup);

        try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = bufferedreader.readLine()) != null) {
                LOG.info(line);
            }
        }
    }
}
