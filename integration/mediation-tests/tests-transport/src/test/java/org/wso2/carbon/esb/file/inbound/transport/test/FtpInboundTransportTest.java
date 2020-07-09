/*
 *Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.file.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.extensions.servers.ftpserver.FTPServerManager;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class FtpInboundTransportTest extends ESBIntegrationTest {

    private FTPServerManager ftpServerManager;
    private File FTPFolder;
    private CarbonLogReader logViewerClient;
    private String pathToFtpDir;

    @BeforeClass(alwaysRun = true)
    public void runFTPServerForInboundTest() throws Exception {

        init();
        // Username password for the FTP server to be started
        String FTPUsername = "admin";
        String FTPPassword = "admin";
        String inputFolderName = "ftpin";
        String outputFolderName = "ftpout";
        int FTPPort = 9653;

        pathToFtpDir = getESBResourceLocation() + File.separator + "synapseconfig" + File.separator + "vfsTransport"
                + File.separator;

        // Local folder of the FTP server root
        FTPFolder = new File(pathToFtpDir + "FTP_Location" + File.separator);

        // create FTP server root folder if not exists
        if (FTPFolder.exists()) {
            FileUtils.deleteDirectory(FTPFolder);
        }
        Assert.assertTrue(FTPFolder.mkdir(), "FTP root file folder not created");

        // create 'in' directory under FTP server root
        File inputFolder = new File(FTPFolder.getAbsolutePath() + File.separator + inputFolderName);

        if (inputFolder.exists()) {
            FileUtils.deleteDirectory(inputFolder);
        }
        Assert.assertTrue(inputFolder.mkdir(), "FTP data /in folder not created");
        // create 'out' directory under FTP server root
        File outputFolder = new File(FTPFolder.getAbsolutePath() + File.separator + outputFolderName);

        if (outputFolder.exists()) {
            FileUtils.deleteDirectory(outputFolder);
        }
        Assert.assertTrue(outputFolder.mkdir(), "FTP data /in folder not created");
        Utils.shutdownFailsafe(FTPPort);

        // start-up FTP server
        ftpServerManager = new FTPServerManager(FTPPort, FTPFolder.getAbsolutePath(), FTPUsername, FTPPassword);
        ftpServerManager.startFtpServer();
        logViewerClient = new CarbonLogReader();
        logViewerClient.start();
        log.info("Before Class test method completed successfully");
    }

    @AfterClass(alwaysRun = true)
    public void stopFTPServerForInboundTest() {

        ftpServerManager.stop();
        log.info("FTP Server stopped successfully");
        logViewerClient.stop();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = "wso2.esb",
            dependsOnMethods = { "testInboundEndpointMoveAfterProcessFTP" },
            description = "Inbound endpoint Reading file in FTP Test Case")
    public void testInboundEndpointReadFileInFTP() throws Exception {

        File sourceFile = new File(pathToFtpDir + File.separator + "test.xml");
        File targetFolder = new File(FTPFolder + File.separator + "ftpin");
        File targetFile = new File(targetFolder + File.separator + "test1.xml");
        try {
            FileUtils.copyFile(sourceFile, targetFile);
            logViewerClient.clearLogs();
            Utils.deploySynapseConfiguration(addEndpoint1(), "testFtpFile1", Utils.ArtifactType.INBOUND_ENDPOINT,
                                             false);
            boolean isFileRead = Utils.checkForLog(logViewerClient, "<m0:symbol>WSO2</m0:symbol>", 60);
            Assert.assertTrue(isFileRead, "The XML file is not getting read");
        } finally {
            Utils.undeploySynapseConfiguration("testFtpFile1", Utils.ArtifactType.INBOUND_ENDPOINT, false);
        }
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = "wso2.esb",
            description = "Inbound endpoint move after process in FTP Test Case")
    public void testInboundEndpointMoveAfterProcessFTP() throws Exception {

        logViewerClient.clearLogs();
        File sourceFile = new File(pathToFtpDir + File.separator + "test.xml");
        File targetFile = new File(FTPFolder + File.separator + "ftpin" + File.separator + "test.xml");
        File outFile = new File(FTPFolder + File.separator + "ftpout" + File.separator + "test.xml");
        try {
            FileUtils.copyFile(sourceFile, targetFile);
            Utils.deploySynapseConfiguration(addEndpoint2(), "testFtpFile2", Utils.ArtifactType.INBOUND_ENDPOINT,
                                             false);
            Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).until(
                    isFileExist(outFile));
            Assert.assertTrue(outFile.exists(), "Input file is not moved after processing the file");
            Assert.assertFalse(targetFile.exists(), "Input file is exist after processing the input file");
        } finally {
            Utils.undeploySynapseConfiguration("testFtpFile2", Utils.ArtifactType.INBOUND_ENDPOINT, false);
            Assert.assertTrue(Utils.checkForLog(logViewerClient, "Destroying Inbound Endpoint: testFtpFile2", 60));
            deleteFile(targetFile);
            deleteFile(outFile);
        }
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = "wso2.esb",
            description = "Inbound endpoint invalid FTP username Test Case")
    public void testInboundInvalidFtpUsername() throws Exception {

        File sourceFile = new File(pathToFtpDir + File.separator + "test.xml");
        File targetFile = new File(FTPFolder + File.separator + "ftpin" + File.separator + "test.xml");
        File outFile = new File(FTPFolder + File.separator + "ftpout" + File.separator + "test.xml");
        try {
            FileUtils.copyFile(sourceFile, targetFile);
            logViewerClient.clearLogs();
            Utils.deploySynapseConfiguration(addEndpoint3(), "testFtpFile3", Utils.ArtifactType.INBOUND_ENDPOINT,
                                             false);
            Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).until(
                    isFileNotExist(outFile));
            Assert.assertFalse(outFile.exists());
        } finally {
            Utils.undeploySynapseConfiguration("testFtpFile3", Utils.ArtifactType.INBOUND_ENDPOINT, false);
            if (!deleteFile(targetFile)) {
                Assert.fail("Failed to delete target file");
            }
        }
    }

    private OMElement addEndpoint1() throws Exception {

        return AXIOMUtil.stringToOM("<inboundEndpoint name=\"testFtpFile1\" onError=\"inFault\" protocol=\"file\"\n"
                                            + " sequence=\"requestHandlerSeq\" suspend=\"false\" xmlns=\"http://ws.apache.org/ns/synapse\">\"\n"
                                            + " <parameters>\n" + " <parameter name=\"interval\">1000</parameter>\n"
                                            + " <parameter name=\"coordination\">false</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterErrors\">NONE</parameter>\n"
                                            + " <parameter name=\"transport.vfs.Locking\">disable</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ContentType\">application/xml</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterFailure\">NONE</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterProcess\">NONE</parameter>\n"
                                            + " <parameter name=\"transport.vfs.FileURI\">ftp://admin:admin@localhost:9653/ftpin/test1.xml"
                                            + "</parameter>\n" + " </parameters>\n" + "</inboundEndpoint>\n");
    }

    private OMElement addEndpoint2() throws Exception {

        return AXIOMUtil.stringToOM("<inboundEndpoint name=\"testFtpFile2\" onError=\"inFault\" protocol=\"file\"\n"
                                            + " sequence=\"requestHandlerSeq\" suspend=\"false\" xmlns=\"http://ws.apache.org/ns/synapse\">\"\n"
                                            + " <parameters>\n" + " <parameter name=\"interval\">1000</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterErrors\">NONE</parameter>\n"
                                            + " <parameter name=\"transport.vfs.Locking\">enable</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ContentType\">application/xml</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterFailure\">NONE</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n"
                                            + "<parameter name=\"transport.vfs.MoveAfterProcess\">ftp://admin:admin@localhost:9653/ftpout"
                                            + "</parameter>"
                                            + " <parameter name=\"transport.vfs.FileURI\">ftp://admin:admin@localhost:9653/ftpin"
                                            + "</parameter>\n" + " </parameters>\n" + "</inboundEndpoint>\n");
    }

    private OMElement addEndpoint3() throws Exception {

        return AXIOMUtil.stringToOM("<inboundEndpoint name=\"testFtpFile3\" onError=\"inFault\" protocol=\"file\"\n"
                                            + " sequence=\"requestHandlerSeq\" suspend=\"false\" xmlns=\"http://ws.apache.org/ns/synapse\">\"\n"
                                            + " <parameters>\n" + " <parameter name=\"interval\">1000</parameter>\n"
                                            + " <parameter name=\"coordination\">false</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterErrors\">NONE</parameter>\n"
                                            + " <parameter name=\"transport.vfs.Locking\">enable</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ContentType\">application/xml</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterFailure\">NONE</parameter>\n"
                                            + " <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n"
                                            + "<parameter name=\"transport.vfs.MoveAfterProcess\">ftp://admin:admin@localhost:9653/ftpout/test.xml"
                                            + "</parameter>"
                                            + " <parameter name=\"transport.vfs.FileURI\">ftp://invalid:admin@localhost:9653/ftpin/test.xml"
                                            + "</parameter>\n" + " </parameters>\n" + "</inboundEndpoint>\n");
    }

    private boolean deleteFile(File file) {
        return file.exists() && file.delete();
    }

    private Callable<Boolean> isFileExist(final File file) {
        return () -> file.exists();
    }

    private Callable<Boolean> isFileNotExist(final File file) {
        return () -> !file.exists();
    }
}
