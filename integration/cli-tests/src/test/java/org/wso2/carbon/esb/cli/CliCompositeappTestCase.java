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

public class CliCompositeappTestCase {

    protected Log log = LogFactory.getLog(CliEndpointTestCase.class);
    String line = null;
    TestUtils testUtils = new TestUtils();
    private static final String cliTestHelloCar = "hello-worldCompositeApplication";
    private static final String cliTestMediatorCar = "MediatorCApp";

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
     * Get information about all the carbon applications
     */

    @Test
    public void miShowCarbonappAllTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommand(
                testUtils.getMIBuildPath(),"compositeapp", "show").getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestHelloCar)),cliTestHelloCar +" Carbon application not found");
            log.info(cliTestHelloCar + " Carbon application Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestMediatorCar)),cliTestMediatorCar + " Carbon application not found");
            log.info(cliTestMediatorCar + " Carbon application Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }

    /**
     * Get information about single carbon applications
     */

    @Test
    public void miShowCarbonappTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommandWithArtifact(
                testUtils.getMIBuildPath(), "compositeapp", "show", cliTestHelloCar).getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestHelloCar)),cliTestHelloCar +" Carbon application not Found");
            log.info(cliTestHelloCar + " Carbon application Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }

    /**
     * Test un-deployed Carbon application
     */
    @Test
    public void miShowCappNotFoundTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommandWithArtifact(
                testUtils.getMIBuildPath(), "compositeapp", "show", "TestCapp").getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("Carbon App 404 Not Found")),"Carbon App 404 Not Found");
        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }
}
