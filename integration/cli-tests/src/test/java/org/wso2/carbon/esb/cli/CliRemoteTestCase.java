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

public class CliRemoteTestCase {

    protected Log log = LogFactory.getLog(CliRemoteTestCase.class);
    BufferedReader reader = null;
    ProcessBuilder builder = null;
    ProcessBuilder builderShow = null;
    String line = null;
    File file;
    String filePath = new File(this.getClass().getSimpleName()).getAbsolutePath();

    String cliTestRemoteServer = "TestServer";
    String remoteHost = "192.168.1.15";
    String getRemoteHostUpdate = "192.168.1.17";
    String remotePort = "9164";

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
     * Test to add a Micro Integrator server
     */
    @Test(priority=1)
    public void miAddRemoteServer() throws IOException {

        try{
            builder = new ProcessBuilder(file.getCanonicalPath(), "remote" , "add", cliTestRemoteServer, remoteHost, remotePort);
            builder.start();
            builderShow = new ProcessBuilder(file.getCanonicalPath(), "remote" , "show");
            Process processRemote = builderShow.start();

            reader = new BufferedReader(new InputStreamReader(processRemote.getInputStream()));
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(cliTestRemoteServer))," - Fail to add remote server -"+cliTestRemoteServer);
            log.info("Successfully added remote server - "+cliTestRemoteServer);

        }
        catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        }
        finally {
            reader.close();
        }
    }

    /**
     * Test to Update the hostname of Micro Integrator server
     */
    @Test(priority=2)
    public void miUpdateRemoteServer() throws IOException {

        try{
            builder = new ProcessBuilder(file.getCanonicalPath(), "remote" , "update", cliTestRemoteServer, getRemoteHostUpdate, remotePort);
            builder.start();
            builderShow = new ProcessBuilder(file.getCanonicalPath(), "remote" , "show");
            Process processRemote = builderShow.start();

            reader = new BufferedReader(new InputStreamReader(processRemote.getInputStream()));
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains(getRemoteHostUpdate)),"Fail to Update host of the remote server of "+cliTestRemoteServer);
            log.info("Successfully update host of the remote server of " +cliTestRemoteServer);

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Test to select the current Micro Integrator server
     */
    @Test(priority=3)
    public void miSelectRemoteServer() throws IOException {

        try{
            builder = new ProcessBuilder(file.getCanonicalPath(), "remote" , "select", cliTestRemoteServer);
            builder.start();
            builderShow = new ProcessBuilder(file.getCanonicalPath(), "remote" , "show");
            Process processRemote = builderShow.start();

            reader = new BufferedReader(new InputStreamReader(processRemote.getInputStream()));
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertTrue(lines.stream().anyMatch(str -> str.trim().contains("current_server: "+cliTestRemoteServer)),"Fail to select "+cliTestRemoteServer+" as current remote server");
            log.info("Successfully select "+cliTestRemoteServer+" as current remote server");

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    /**
     * Test to remove a Micro Integrator server
     */
    @Test(priority=4)
    public void miRemoveRemoteServer() throws IOException {

        try{
            builder = new ProcessBuilder(file.getCanonicalPath(), "remote" , "remove", cliTestRemoteServer);
            builder.start();
            builderShow = new ProcessBuilder(file.getCanonicalPath(), "remote" , "show");
            Process processRemote = builderShow.start();

            reader = new BufferedReader(new InputStreamReader(processRemote.getInputStream()));
            List<String> lines = new ArrayList();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Assert.assertNotEquals(lines.stream().anyMatch(str -> str.trim().contains(cliTestRemoteServer)),"Fail to remove "+cliTestRemoteServer);
            log.info("Successfully remove "+cliTestRemoteServer);

        } catch (IOException e) {
            log.info("Exception = " + e.getMessage());
        } finally {
            reader.close();
        }
    }
}
