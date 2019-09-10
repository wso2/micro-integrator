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

package org.wso2.carbon.esb.usage;

import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.*;
import org.apache.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import Util.*;

public class CliUsageTests {
    protected Log log = LogFactory.getLog(CliUsageTests.class);
    public static String pomVersion;
    BufferedReader reader = null;
    String line = null;
    String cliTestApi = "cliTesApi";
    String cliTestMediatorApi = "cliTestIterateMediatorApi";
    String cliTestEp = "cliTestEndpoint";
    String cliTestHtmlEp = "cliTesthtmlEndpoint";
    String cliTestProxy = "cliAddressProxy";
    String cliTestBmProxy = "cliTestBMProxy";
    String cliTestHelloCar = "hello-worldCompositeApplication";
    String cliTestEsbCar = "esb-artifacts-car";
    /**
     * setup the environment to run the tests
     */
    @BeforeClass
    public void setupEnv() throws IOException, XmlPullParserException, InterruptedException {

        TestUtils testUtils = new TestUtils();
        pomVersion = testUtils.getPomVerion();
        Process process;

        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        String[] setup = { "sh", "../src/test/java/EnvSetup.sh"};
        process = Runtime.getRuntime().exec(setup);
        process.waitFor();
//        Process process = new ProcessBuilder("../src/test/java/EnvSetup.sh").start();

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
    public void miShowAllApiTest() throws IOException {
        try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "api", "show");
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestApi)),cliTestApi+" API not found");
            log.info(cliTestApi + " API Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestMediatorApi)),cliTestMediatorApi+" API not found");
            log.info(cliTestMediatorApi + " API Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about single API's
     */
    @Test
    public void miShowApiTest() throws IOException {

        try {
            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "api" , "show", cliTestApi);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestApi)),cliTestApi +" API not found");
            log.info(cliTestApi + " API Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about all the Endpoints
     */
    @Test
    public void miShowEndpointAllTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "show" , "endpoint");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestEp)),cliTestEp +" Endpoint not found");
            log.info(cliTestEp + " Endpoint Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestHtmlEp)),cliTestHtmlEp + "Endpoint not found");
            log.info(cliTestHtmlEp + " Endpoint Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about single Endpoint
     */

    @Test
    public void miShowEndpointTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "show" , "endpoint", cliTestEp);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestEp)),cliTestEp +" Endpoint not found");
            log.info(cliTestEp + " Endpoint Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about all the Proxy services
     */

    @Test
    public void miShowProxyAllTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "show" , "proxyservice");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestBmProxy)),cliTestBmProxy + " Proxy service not found");
            log.info(cliTestBmProxy + " Proxy service Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestProxy)),cliTestProxy + " Proxy service not found");
            log.info(cliTestProxy + " Proxy service Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about single proxy service
     */

    @Test
    public void miShowProxyTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "show" , "proxyservice" , cliTestProxy);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestProxy)),cliTestProxy + " Proxy service not found");
            log.info(cliTestProxy + " Proxy service Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about all the carbon applications
     */

    @Test
    public void miShowCarbonappAllTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "show" , "carbonapp");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestHelloCar)),cliTestHelloCar +" Carbon application not found");
            log.info(cliTestHelloCar + " Carbon application Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestEsbCar)),cliTestEsbCar + " Carbon application not found");
            log.info(cliTestEsbCar + " Carbon application Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about single carbon applications
     */

    @Test
    public void miShowCarbonappTest() throws IOException {

        try {
            ProcessBuilder builder = new ProcessBuilder("../../../cmd/build/wso2mi-cli-"+pomVersion+"/bin/mi", "show", "carbonapp" , cliTestHelloCar);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestHelloCar)), cliTestHelloCar+ " Carbon application not found");
            log.info(cliTestHelloCar + " Carbon application Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }
}
