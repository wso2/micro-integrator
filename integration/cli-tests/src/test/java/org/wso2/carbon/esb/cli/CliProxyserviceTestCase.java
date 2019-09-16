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

import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.*;
import org.apache.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import util.TestUtils;

public class CliProxyserviceTestCase {

    protected Log log = LogFactory.getLog(CliProxyserviceTestCase.class);
    String line = null;
    TestUtils testUtils = new TestUtils();
    private static final String cliAddressProxy = "cliAddressProxy";
    private static final String cliTestProxy = "cliTestProxy";

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
     * Get information about all the Proxy services
     */

    @Test
    public void miShowProxyAllTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommand(
                testUtils.getMIBuildPath(),"proxyservice", "show").getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestProxy)),cliTestProxy + " Proxy service not found");
            log.info(cliTestProxy + " Proxy service Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliAddressProxy)),cliAddressProxy + " Proxy service not found");
            log.info(cliAddressProxy + " Proxy service Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }

    /**
     * Get information about single proxy service
     */

    @Test
    public void miShowProxyTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommandWithArtifact(
                testUtils.getMIBuildPath(), "proxyservice", "show", cliTestProxy).getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestProxy)),cliTestProxy + " Proxy service not found");
            log.info(cliTestProxy + " Proxy service Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }

    /**
     * Test un-deployed proxy service
     */

    @Test
    public void miShowProxyNotFoundTest() throws IOException {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(testUtils.runMiCommandWithArtifact(
                testUtils.getMIBuildPath(), "proxyservice", "show", "CliTestProxy").getInputStream()))) {
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("ProxyService 404 Not Found")),"ProxyService 404 Not Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
    }
}
