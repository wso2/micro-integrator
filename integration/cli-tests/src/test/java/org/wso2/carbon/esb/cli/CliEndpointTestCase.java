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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import Util.*;

public class CliEndpointTestCase {
    protected Log log = LogFactory.getLog(CliEndpointTestCase.class);
    BufferedReader reader = null;
    String line = null;
    File file;
    String filePath = new File(this.getClass().getSimpleName()).getAbsolutePath();
    String cliTestEp = "SimpleEP";
    String cliStockEp = "SimpleStockQuoteServiceEndpoint";

    {
        try {
            TestUtils testUtils = new TestUtils();
            file = new File(filePath+".."+ File.separator+".."+ File.separator +".."+ File.separator +".."
                    + File.separator +"cmd"+ File.separator+"build"+ File.separator+"wso2mi-cli-"+testUtils.getPomVerion()
                    + File.separator +"bin"+ File.separator+"mi");
        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } catch (XmlPullParserException e) {
            log.info("Exception = " + e.getMessage());
        }
    }

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
     * Get information about all the Endpoints
     */
    @Test
    public void miShowEndpointAllTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "endpoint" , "show");
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);

            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestEp)),cliTestEp +" Endpoint not found");
            log.info(cliTestEp + " Endpoint Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliStockEp)),cliStockEp + "Endpoint not found");
            log.info(cliStockEp + " Endpoint Found");

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
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "endpoint" , "show", cliTestEp);
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

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
     * Test un-deployed Endpoint
     */

    @Test
    public void miShowEndpointNotFoundTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "endpoint" , "show", "CLITestEP");
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("Endpoint 404 Not Found")),"Endpoint 404 Not Found");
            log.info("Endpoint 404 Not Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }
}
