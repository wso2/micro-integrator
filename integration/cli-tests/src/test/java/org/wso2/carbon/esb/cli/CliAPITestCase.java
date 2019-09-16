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

package org.wso2.carbon.esb.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import util.TestUtils;

public class CliAPITestCase {

    protected Log log = LogFactory.getLog(CliAPITestCase.class);
    String line = null;
    TestUtils testUtils = new TestUtils();
    private static final String cliSampleApi_1 = "cliSampleApi_1";
    private static final String cliSampleApi_2 = "cliSampleApi_2";

    /**
     * setup the environment to run the tests
     */
    @BeforeClass
    public void setupEnv() throws IOException {

        Process process;
        String[] setup = { "sh", ".."+ File.separator +"src"+ File.separator +"test"+ File.separator +"java"+ File.separator +"EnvSetup.sh"};
        process = Runtime.getRuntime().exec(setup);

        try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = bufferedreader.readLine()) != null) {
                log.info(line);
            }
        }
    }

    /**
     * Get information about all the API's
     */
    @Test
    public void miShowAllApiTest() {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommand(
                testUtils.getMIBuildPath(),"api" ,"show").getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliSampleApi_1)),cliSampleApi_1+" API not found");
            log.info(cliSampleApi_1 + " API Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliSampleApi_2)),cliSampleApi_2+" API not found");
            log.info(cliSampleApi_2 + " API Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }


        /**
         * Get information about single API's
         */
    @Test
    public void miShowApiTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommandWithArtifact(
                testUtils.getMIBuildPath(), "api", "show", cliSampleApi_1).getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliSampleApi_1)),cliSampleApi_1 +" API not found");
            log.info(cliSampleApi_1 + " API Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }

    /**
     * Test Un-deployed API
     */
    @Test
    public void miShowApiNotFoundTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommandWithArtifact(
                testUtils.getMIBuildPath(), "api", "show", "TestAPI").getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("API 404 Not Found")),"API 404 Not Found");
        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }
}
