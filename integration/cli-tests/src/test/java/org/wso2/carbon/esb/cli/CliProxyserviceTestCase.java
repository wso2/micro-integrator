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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.*;
import org.apache.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;
import Util.*;

public class CliProxyserviceTestCase {

    protected Log log = LogFactory.getLog(CliProxyserviceTestCase.class);
    BufferedReader reader = null;
    String line = null;
    File file;
    String filePath = new File(this.getClass().getSimpleName()).getAbsolutePath();
    String cliTestProxy = "cliAddressProxy";
    String cliTestBmProxy = "cliTestBMProxy";

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
     * Get information about all the Proxy services
     */

    @Test
    public void miShowProxyAllTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "proxyservice", "show");
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

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
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "proxyservice" , "show" , cliTestBmProxy);
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestBmProxy)),cliTestBmProxy + " Proxy service not found");
            log.info(cliTestBmProxy + " Proxy service Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Test un-deployed proxy service
     */

    @Test
    public void miShowProxyNotFoundTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "proxyservice" , "show" , "CliTestProxy");
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("ProxyService 404 Not Found")),"ProxyService 404 Not Found");
            log.info("ProxyService 404 Not Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }
}
