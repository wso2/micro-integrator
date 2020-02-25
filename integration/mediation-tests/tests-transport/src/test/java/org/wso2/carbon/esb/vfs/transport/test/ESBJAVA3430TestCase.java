package org.wso2.carbon.esb.vfs.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.ftpserver.FTPServerManager;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;

/**
 * Related to https://wso2.org/jira/browse/ESBJAVA-3430 This class tests whether
 * the null check for replyFile.getParent() in VFSTransportSender is available
 */
public class ESBJAVA3430TestCase extends ESBIntegrationTest {

    private FTPServerManager ftpServerManager;
    private String FTPUsername;
    private String FTPPassword;
    private File FTPFolder;
    private File inputFolder;
    private LogViewerClient logViewerClient;
    private String pathToFtpDir;
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void runFTPServer() throws Exception {

        // Username password for the FTP server to be started
        FTPUsername = "admin";
        FTPPassword = "admin";
        String inputFolderName = "in";
        int FTPPort = 8085;

        pathToFtpDir = getClass().getResource("/artifacts/ESB/synapseconfig/vfsTransport/").getPath();

        // Local folder of the FTP server root
        FTPFolder = new File(pathToFtpDir + "FTP_Location" + File.separator);

        // create FTP server root folder if not exists
        if (FTPFolder.exists()) {
            FileUtils.deleteDirectory(FTPFolder);
        }
        Assert.assertTrue(FTPFolder.mkdir(), "FTP root file folder not created");

        // create a directory under FTP server root
        inputFolder = new File(FTPFolder.getAbsolutePath() + File.separator + inputFolderName);

        if (inputFolder.exists()) {
            FileUtils.deleteDirectory(inputFolder);
        }
        Assert.assertTrue(inputFolder.mkdir(), "FTP data /in folder not created");

        // start-up FTP server
        ftpServerManager = new FTPServerManager(FTPPort, FTPFolder.getAbsolutePath(), FTPUsername, FTPPassword);
        ftpServerManager.startFtpServer();

        super.init();
        carbonLogReader = new CarbonLogReader();
    }

    @AfterClass(alwaysRun = true)
    public void stopFTPServer() throws Exception {
        ftpServerManager.stop();
        log.info("FTP Server stopped successfully");
        carbonLogReader.stop();
    }

    @Test(groups = "wso2.esb", description = "VFS NPE in Creating a File in FTP directly in root directory")
    public void TestCreateFileInRoot() throws Exception {

        // To check the timed out exception happened
        boolean timeout = false;
        carbonLogReader.start();

        try {
            OMElement response = axis2Client
                    .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("VFSProxyFileCreateInRoot"), null, "WSO2");
        } catch (AxisFault axisFault) {
            if (axisFault.getLocalizedMessage().contains("Read timed out")) {
                timeout = true;
            }
        }

        // To check whether the NPE happened
        boolean isError = carbonLogReader.checkForLog("Error creating file under the FTP root", DEFAULT_TIMEOUT);
        carbonLogReader.clearLogs();
        Assert.assertFalse(isError && timeout,
                " The null check for the replyFile.getParent() in VFSTransportSender is not available");
    }

    @Test(groups = "wso2.esb", description = "VFS NPE in Creating a File in FTP, in a directory under root")
    public void TestCreateFileInDirectoryUnderRoot() throws Exception {

        // To check the timed out exception happened
        boolean timeout = false;

        try {
            OMElement response = axis2Client
                    .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("VFSProxyFileCreateInDirectory"), null, "WSO2");
        } catch (AxisFault axisFault) {
            if (axisFault.getLocalizedMessage().contains("Read timed out")) {
                timeout = true;
            }
        }

        // To check whether the NPE happened
        boolean isError = carbonLogReader.checkForLog("Error creating file under the FTP root", DEFAULT_TIMEOUT);
        Assert.assertFalse(isError && timeout,
                " The null check for the replyFile.getParent() in VFSTransportSender is not available");
    }
}
