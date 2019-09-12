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

import Util.TestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CliSequenceTestCase {

    protected Log log = LogFactory.getLog(CliSequenceTestCase.class);
    BufferedReader reader = null;
    String line = null;
    File file;
    String filePath = new File(this.getClass().getSimpleName()).getAbsolutePath();
    String cliTestSeq = "CliTestSequence";
    String cliSampleSeq = "CliSampleSequence";

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
     * Get information about all Sequence
     */
    @Test
    public void miShowEndpointAllTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "sequence" , "show");
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);

            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestSeq)),cliTestSeq +" Sequence not found");
            log.info(cliTestSeq + " Sequence Found");
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliSampleSeq)),cliSampleSeq + "Sequence not found");
            log.info(cliSampleSeq + " Sequence Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Get information about single Sequence
     */

    @Test
    public void miShowEndpointTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "sequence" , "show", cliTestSeq);
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestSeq)),cliTestSeq +" Sequence not found");
            log.info(cliTestSeq + " Sequence Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Test un-deployed Sequence
     */

    @Test
    public void miShowEndpointNotFoundTest() throws IOException {

        try{
            ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "sequence" , "show", "CLITestSequence");
            Process process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("Sequence 404 Not Found")),"Sequence 404 Not Found");
            log.info("Sequence 404 Not Found");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }
}
