/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.esb.mediator.test.smooks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import org.apache.axiom.om.util.AXIOMUtil;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.esb.mediator.test.smooks.utils.FileUtils;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

/**
 * This test case is for verifying the functionality of processing relatively large files(>16K TCP buffer size) with
 * smooks mediator
 */
public class SmooksLargeFileProcessingTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        addSmooksProxy();
    }


    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a Large File To Smooks Mediator")
    public void testSendingToSmooks() throws Exception {
        String smooksResourceDirstr = getClass().getResource("/artifacts/ESB/synapseconfig/smooks/").getFile();
        File fileSmook = new File(smooksResourceDirstr);
        String smooksResourceDir = fileSmook.getAbsolutePath();
        Path source = Paths.get(smooksResourceDir, "person.csv");
        Path destination = Paths.get(smooksResourceDir, "test", "in", "person.csv");

        Files.createDirectories(Paths.get(smooksResourceDir, "test", "in"));
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        /*
         * The polling interval of the VFS proxy is 1s. Therefore atmost 10s waiting time was added to provide
         * enough time for the processing
         */
        Path outPutFilePath = Paths.get(smooksResourceDir, "test", "out", "Out.xml");

        Awaitility.await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).until(
                FileUtils.checkFileExistence(outPutFilePath));
        Assert.assertTrue(Files.exists(outPutFilePath), "Output file has not been created");

        String smooksOut = new String(Files.readAllBytes(outPutFilePath));
        Assert.assertTrue(smooksOut.contains(
                "<csv-record number=\"160\"><firstname>Andun</firstname><lastname>Sameera</lastname>"
                        + "<gender>Male</gender><age>4</age><country>SriLanka</country></csv-record>"),
                "Large file " + "transformation may not have completed as expected");

    }

    private void addSmooksProxy() throws Exception {

        Utils.deploySynapseConfiguration(AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"SmooksProxy\" transports=\"vfs\" "
                + "startOnLoad=\"true\">\n" + "    <target>\n" + "    <inSequence>\n" + "    <log level=\"full\"/>\n"
                + "    <smooks config-key=\"conf:/smooks_config.xml\">\n" + "        <input type=\"text\"/>\n"
                + "        <output type=\"xml\"/>\n" + "    </smooks>\n"
                + "    <property name=\"OUT_ONLY\" value=\"true\"/>\n" + "    <send>\n"
                + "        <endpoint name=\"FileEpr\">\n" + "            <address uri=\"vfs:file://" + getClass()
                .getResource("/artifacts/ESB/synapseconfig/smooks/").getPath()
                + "test/out/Out.xml\" format=\"soap11\"/>\n" + "        </endpoint>\n" + "    </send>\n"
                + "    <log level=\"full\"/>\n" + "    </inSequence>\n" + "    </target>\n"
                + "    <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "    <parameter name=\"transport.vfs.FileURI\">file://" + getClass()
                .getResource("/artifacts/ESB/synapseconfig/smooks/").getPath() + "test/in/</parameter>\n"
                + "    <parameter name=\"transport.vfs.FileNamePattern\">.*\\.csv</parameter>\n"
                + "    <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n" + "</proxy>"), "SmooksProxy", "proxy-services", true);
    }

}

