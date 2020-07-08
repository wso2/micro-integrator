/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.file.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;

/**
 * Tests the invocation of a proxy service via a dynamic sequence registered in the registry upon the receipt
 * of a file using the file inbound.
 */
public class FileInboundWithDynamicSequenceTestCase extends ESBIntegrationTest {

    private File inboundFileFolder;
    private String pathToDir;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        init();
        pathToDir = getESBResourceLocation() + File.separator + "file" + File.separator + "inbound" + File.separator
                + "transport";
        inboundFileFolder = new File(pathToDir + File.separator + "InboundInFileFolder");
        // create InboundFileFolder if not exists
        if (inboundFileFolder.exists()) {
            FileUtils.cleanDirectory(inboundFileFolder);
        } else {
            Assert.assertTrue(inboundFileFolder.mkdir(), "InboundFileFolder not created");
        }
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = "wso2.esb",
            description = "Tests sequences from  the governance registry with inbound endpoint")
    public void testSequence() throws Exception {

        //Copy file to source folder from which the inbound endpoint will pick up the message
        File sourceFile = new File(pathToDir + File.separator + "test.xml");
        File targetFile = new File(inboundFileFolder + File.separator + "test.xml");
        if (!sourceFile.exists() && !sourceFile.createNewFile()) {
            Assert.fail("Failed to created file " + sourceFile);
        }
        FileUtils.copyFile(sourceFile, targetFile);

        CarbonLogReader logReader = new CarbonLogReader();
        logReader.start();
        Utils.deploySynapseConfiguration(createInboundEP(), "testFileInboundWithDynamicSeq",
                                         Utils.ArtifactType.INBOUND_ENDPOINT, false);
        Assert.assertTrue(Utils.checkForLog(logReader, "Proxy invoked by dynamic sequence in file inbound", 120),
                          "The XML file is not getting read");
        logReader.stop();
    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {

        FileUtils.deleteDirectory(inboundFileFolder);
    }

    private OMElement createInboundEP() throws Exception {

        return AXIOMUtil.stringToOM(
                "<inboundEndpoint name=\"testFileInboundWithDynamicSeq\" onError=\"inFault\" " + "protocol=\"file\"\n"
                        + " sequence=\"gov:/fileInboundDynamicSequence.xml\" suspend=\"false\" xmlns=\"http://ws.apache"
                        + ".org/ns/synapse\">\"\n" + " <parameters>\n"
                        + " <parameter name=\"interval\">1000</parameter>\n"
                        + " <parameter name=\"transport.vfs.ActionAfterErrors\">DELETE</parameter>\n"
                        + " <parameter name=\"transport.vfs.Locking\">enable</parameter>\n"
                        + " <parameter name=\"transport.vfs.ContentType\">application/xml</parameter>\n"
                        + " <parameter name=\"transport.vfs.ActionAfterFailure\">DELETE</parameter>\n"
                        + " <parameter name=\"transport.vfs.ActionAfterProcess\">DELETE</parameter>\n"
                        + " <parameter name=\"transport.vfs.FileURI\">file://" + inboundFileFolder + "</parameter>\n"
                        + " </parameters>\n" + "</inboundEndpoint>\n");
    }
}
