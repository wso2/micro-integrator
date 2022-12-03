/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.esb.vfs.transport.test.connection.failure;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLStreamException;

/**
 * Integration test for https://github.com/wso2/product-ei/issues/5456
 */
public class SMB2ConnectionGrowthTestCaseAfterConnectingToNEDirectory extends ESBIntegrationTest {

    private static final Log LOGGER = LogFactory.getLog(SMB2ConnectionGrowthTestCaseAfterConnectingToNEDirectory.class);

    private File inputFolder;
    private File outputFolder;
    private File originalFolder;
    private ServerConfigurationManager serverConfigurationManager;
    private String inputFolderName = "in";
    private String outputFolderName = "out";
    private String originalFolderName = "original";

    @BeforeClass(alwaysRun = true)
    public void serverSetUp() throws Exception {

        String pathToSMB2root =Utils.getSMB2Root();
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);

        // Local folder of the SMB2 server root
        File SMB2RootFolder = new File(pathToSMB2root);
        Assert.assertTrue(SMB2RootFolder.exists(), "SMB2 root folder hasn't been created");

        inputFolder = new File(SMB2RootFolder.getAbsolutePath() + File.separator + inputFolderName);
        outputFolder = new File(SMB2RootFolder.getAbsolutePath() + File.separator + outputFolderName);
        originalFolder = new File(SMB2RootFolder.getAbsolutePath() + File.separator + originalFolderName);

        Utils.deleteDirectory(inputFolder);
        Utils.deleteDirectory(outputFolder);
        Utils.deleteDirectory(originalFolder);

        log.info("Creating outputFolder " + outputFolder.getAbsolutePath());
        outputFolder.mkdir();
        log.info("Creating originalFolder " + originalFolder.getAbsolutePath());
        originalFolder.mkdir();

        Assert.assertTrue(outputFolder.exists(), "SMB2 /out folder not created");


        super.init();
        log.info("The used host is: " + getHostname());
        File jcifFile = new File(getClass().getResource("/artifacts/ESB/synapseconfig/vfsTransport"
                + "/jcifs-1.3.17.jar").getPath());
        File destinationJcif = Paths.get(carbonHome,"lib","jcifs-1.3.17.jar").toFile();

        //copy jcifFile to lib
        copyFile(jcifFile, destinationJcif);

        // replace the axis2.xml enabled vfs transfer and restart the ESB server gracefully.
        serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyConfiguration(
                new File(getClass().getResource("/artifacts/ESB/synapseconfig/"
                        + "vfsTransport/ESBJAVA4770/axis2.xml").getPath()));
        super.init();
    }


    @Test(groups = "wso2.esb", description = "SMB2 connection growth test after connecting to NE directory")
    public void connectionGrowthTestAfterConnectingToNEDir()
            throws XMLStreamException, IOException, InterruptedException {

        String smb2Password = Utils.getSMB2Password();
        String smb2User = Utils.getSMB2User();
        //create VFS transport SMB2 listener proxy
        String proxy = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<proxy xmlns=\"http://ws.apache.org/ns/synapse\"\n" +
                "       name=\"Polling_Test\"\n" +
                "       transports=\"vfs\"\n" +
                "       startOnLoad=\"true\">\n" +
                "   <target>\n" +
                "      <inSequence>\n" +
                "         <log level=\"custom\">\n" +
                "            <property name=\"Polling_Test\"\n" +
                "                      expression=\"fn:concat($ctx:proxy.name,' file ',$trp:FILE_NAME,' received from file system')\"/>\n" +
                "         </log>\n" +
                "      </inSequence>\n" +
                "      <faultSequence><!-- set $trp:ERROR_CODE to keep source untouched -->\n" +
                "         <property name=\"ERROR_CODE\" value=\"1\" scope=\"transport\"/>\n" +
                "         <!-- -->\n" +
                "         <log level=\"custom\">\n" +
                "            <property name=\"ERROR in proxy \"\n" +
                "                      value=\"Polling_Test\"/>\n" +
                "            <property name=\"code\" expression=\"$ctx:ERROR_CODE\"/>\n" +
                "            <property name=\"detail\" expression=\"$ctx:ERROR_DETAIL\"/>\n" +
                "            <property name=\"exception\" expression=\"$ctx:ERROR_EXCEPTION\"/>\n" +
                "         </log>\n" +
                "         <!-- -->\n" +
                "         <property name=\"status\" value=\"Error\"/>\n" +
                "         <property name=\"errorMessage\" value=\"unable to handle file transfer. Rollback!\"/>\n" +
                "      </faultSequence>\n" +
                "   </target>\n" +
                "   <parameter name=\"transport.PollInterval\">1</parameter>\n" +
                "   <parameter name=\"transport.vfs.Maxfilesize\">10000000</parameter>\n" +
                "   <parameter name=\"transport.vfs.FileURI\">smb2://" + smb2User + ":" + smb2Password + "@" + getHostname() +
                "/share/in</parameter>\n" +
                "   <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n" +
                "   <parameter name=\"transport.vfs.MoveAfterProcess\">smb2://" + smb2User + ":" + smb2Password + "@" + getHostname() +
                "/share/out</parameter> \n" +
                "    <parameter name=\"transport.vfs.MoveAfterFailure\">smb2://" + smb2User + ":" + smb2Password +
                "@" + getHostname() + "/share/original</parameter>\n" +
                "    <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n" +
                "    <parameter name=\"transport.vfs.ActionAfterFailure\">MOVE</parameter>\n" +
                "   <parameter name=\"transport.vfs.ClusterAware\">false</parameter>\n" +
                "   <parameter name=\"transport.vfs.FileNamePattern\">.*\\.txt</parameter>\n" +
                "   <parameter name=\"transport.vfs.Locking\">disable</parameter>\n" +
                "</proxy>";

        OMElement proxyOM = AXIOMUtil.stringToOM(proxy);

        //create VFS transport listener proxy
        try {
            org.wso2.esb.integration.common.utils.Utils.deploySynapseConfiguration(proxyOM, "Polling_Test", "proxy-services", true);
        } catch (Exception e) {
            log.error("Error while updating the Synapse config", e);
        }
        Thread.sleep(30000);
        LOGGER.info("Synapse config updated");


        //connection count before
        int startingConnectionCount = 0;
        try {
            startingConnectionCount = Utils.getNumberOfConnectionsToSambaServer();
            log.info("Successfully got the initial connection count to samba server: " + startingConnectionCount);
        } catch (Exception e) {
            Assert.fail("Test failed since getting connections to samba server failed", e);
        }

        // Give time
        Thread.sleep(30000);

        //See whether connections increased
        Awaitility.await().atMost(60, TimeUnit.SECONDS).until(checkWhetherConnectionsIncreased(startingConnectionCount));

    }

    /**
     * Copy the given source file to the given destination
     *
     * @param sourceFile source file
     * @param destFile   destination file
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            fileOutputStream = new FileOutputStream(destFile);

            FileChannel source = fileInputStream.getChannel();
            FileChannel destination = fileOutputStream.getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    /*
     * This is needed when other test cases are added since we need to copy files in after class method
     * */
    /**
     * Copy the given source directory to the given destination
     *
     * @param sourceDir source directory
     * @param destDir   destination directory
     * @throws IOException
     */
    public static void copyDirectory(File sourceDir, File destDir) throws IOException {
        FileUtils.copyDirectory(sourceDir, destDir);
    }

    /*
     * Check whether all the files have been copied from in to out
     * */
    private Callable<Boolean> checkForOutputFile(final File outputFolder) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                File[] files = outputFolder.listFiles();
                return files != null && files.length == 100;
            }
        };
    }

    /*
     * Check whether all the files have been copied from in to out
     * */
    private Callable<Boolean> checkWhetherPollingStarted(final File inputFolder) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                int numberOfFiles = Utils.getFileCount(inputFolder);
                return numberOfFiles < 500;
            }
        };
    }

    /*
     * Check whether polling is resumed
     * */
    private Callable<Boolean> checkWhetherPollingResumed(final int previousCount) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                int numberOfFiles = Utils.getFileCount(inputFolder);
                return numberOfFiles == 0 || numberOfFiles < previousCount;
            }
        };
    }

    /*
     * Check whether connections increased
     * */
    private Callable<Boolean> checkWhetherConnectionsIncreased(final int previousCount) {

        return new Callable<Boolean>() {
            @Override
            public Boolean call() {

                int numberOfConnections = 0;
                try {
                    numberOfConnections = Utils.getNumberOfConnectionsToSambaServer();
                    log.info("Successfully got the  connection count to samba server after resuming: " +
                            numberOfConnections);
                } catch (Exception e) {
                    Assert.fail("Test failed since getting connections to samba server failed", e);
                }
                return numberOfConnections <= (3 * (previousCount + 1));
            }
        };
    }

    /*
     * Check whether samba server stopped
     * */
    private Callable<Boolean> checkWhetherSambaServerStopped() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !Utils.getStatusSambaServer();
            }
        };
    }

    /*
     * Check whether samba server stopped
     * */
    private Callable<Boolean> checkWhetherSambaServerStarted() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Utils.getStatusSambaServer();
            }
        };
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() throws Exception {
        super.cleanup();
        serverConfigurationManager.restoreToLastMIConfiguration();
        Utils.startSambaServer();
    }
}