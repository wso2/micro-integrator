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

package org.wso2.carbon.esb.vfs.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonServerExtension;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This test class in skipped when user mode is tenant because of this release not support vfs transport for tenants
 */
public class VFSTransportTestCase extends ESBIntegrationTest {

    private String pathToVfsDir;
    private File rootFolder;
    private HashMap<String, File> proxyVFSRoots = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        pathToVfsDir = getClass().getResource("/artifacts/ESB/synapseconfig/vfsTransport/").getPath();

        rootFolder = new File(pathToVfsDir + "test" + File.separator);
        File outFolder = new File(pathToVfsDir + "test" + File.separator + "out" + File.separator);
        File inFolder = new File(pathToVfsDir + "test" + File.separator + "in" + File.separator);
        File originalFolder = new File(pathToVfsDir + "test" + File.separator + "original" + File.separator);
        File failureFolder = new File(pathToVfsDir + "test" + File.separator + "failure" + File.separator);

        FileUtils.deleteDirectory(rootFolder);
        Assert.assertTrue(rootFolder.mkdirs(), "file folder not created");
        Assert.assertTrue(outFolder.mkdirs(), "file folder not created");
        Assert.assertTrue(inFolder.mkdirs(), "file folder not created");
        Assert.assertTrue(originalFolder.mkdirs(), "file folder not created");
        Assert.assertTrue(failureFolder.mkdirs(), "file folder not created");
        Assert.assertTrue(outFolder.exists(), "File folder doesn't exists");
        Assert.assertTrue(inFolder.exists(), "File folder doesn't exists");
        Assert.assertTrue(originalFolder.exists(), "File folder doesn't exists");
        Assert.assertTrue(failureFolder.exists(), "File folder doesn't exists");

        addVFSProxyWriteFile();
        addVFSProxy1();
        addVFSProxy2();
        addVFSProxy3();
        addVFSProxy4();
        addVFSProxy5();
        addVFSProxy6();
        addVFSProxy7();
        addVFSProxy8();
        addVFSProxy9();
        addVFSProxy10();
        addVFSProxy11();
        addVFSProxy12();
        addVFSProxy13();
        addVFSProxy14();
        addVFSProxy15();
        addVFSProxy16();
        addVFSProxy17();
        addVFSProxy19();
        addVFSProxy20();
        addVFSProxy21();
        addVFSProxy22();
        addVFSProxy23();
        addVFSProxy24();

        CarbonServerExtension.restartServer();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Writing to a file the content of a xml with content in text element")
    public void testVFSProxyPlainXMLWriter() throws Exception {

        File outfile = new File(proxyVFSRoots.get("salesforce_DAMAS_writeFile") + File.separator +
                "out" + File.separator + "out_reply.xml");

        String request =
                " <ns:text xmlns:ns=\"http://ws.apache.org/commons/ns/payload\"><test>request_value</test></ns:text>";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "text/xml;charset=UTF-8");
        headers.put("Accept-Charset", "UTF-8");
        HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("salesforce_DAMAS_writeFile")), request, headers);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "request_value"), "Sent request body not found");
    }

    private void addVFSProxyWriteFile() throws Exception {

        String proxyName = "salesforce_DAMAS_writeFile";
        OMElement proxy = AXIOMUtil.stringToOM(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\"\n"
                        + "       name=\"salesforce_DAMAS_writeFile\"\n" + "       transports=\"http\"\n"
                        + "       statistics=\"disable\"\n" + "       trace=\"enable\"\n"
                        + "       startOnLoad=\"true\">\n" + "   <target>\n" + "      <inSequence>\n"
                        + "         <property name=\"OUT_ONLY\" value=\"true\" scope=\"default\" type=\"STRING\"/>\n"
                        + "         <property name=\"FORCE_SC_ACCEPTED\" value=\"true\" scope=\"axis2\"/>\n"
                        + "         <property name=\"transport.vfs.ReplyFileName\"\n"
                        + "                   value=\"out_reply.xml\"\n" + "                   scope=\"transport\"\n"
                        + "                   type=\"STRING\"/>\n"
                        + "                           <log level=\"full\"/>\n" + "         <send>\n"
                        + "            <endpoint>\n" + "               <address uri=\"vfs:file://" + pathToVfsDir
                        + "test" + File.separator + proxyName + File.separator +  "out\"/>\n" + "<timeout>\n"
                        + "               <duration>10</duration>\n"
                        + "               <responseAction>discard</responseAction>\n" + "            </timeout>"
                        + "            </endpoint>\n" + "         </send>\n" + "      </inSequence>\n"
                        + "      <outSequence/>\n" + "      <faultSequence/>\n" + "   </target>\n"
                        + "   <parameter name=\"transport.vfs.locking\">disable</parameter>\n"
                        + "   <parameter name=\"redeliveryPolicy.maximumRedeliveries\">0</parameter>\n"
                        + "   <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                        + "   <parameter name=\"redeliveryPolicy.redeliveryDelay\">1</parameter>\n"
                        + "   <description/>\n" + "</proxy>");

        addProxy(proxy, proxyName);
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml")
    public void testVFSProxyFileURI_LinuxPath_ContentType_XML() throws Exception {

        //Related proxy : VFSProxy1
        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy1") + File.separator + "in" + File.separator + "test.xml");
        File outfile = new File(proxyVFSRoots.get("VFSProxy1") + File.separator + "out" + File.separator + "out.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "WSO2 Company"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir" + " transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt")
    public void testVFSProxyFileURI_LinuxPath_ContentType_Plain() throws Exception {

        //Related proxy : VFSProxy2
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy2") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy2") + File.separator + "out" + File.separator + "out.txt");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : " + "transport.vfs.FileURI = "
            + "/home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = *")
    public void testVFSProxyFileURI_LinuxPath_SelectAll_FileNamePattern() throws Exception {

        //Related proxy : VFSProxy3
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy3") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy3") + File.separator + "out" + File.separator + "out.txt");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile,"andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = nothing")
    public void testVFSProxyFileURI_LinuxPath_No_FileNamePattern() throws Exception {

        //Related proxy : VFSProxy4
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy4") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy4") + File.separator + "out" + File.separator + "out.txt");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().
                pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS).until(isFileNotExist(outfile));
        Assert.assertFalse(outfile.exists());
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt, transport.PollInterval=1")
    public void testVFSProxyPollInterval_1() throws Exception {

        //Related proxy : VFSProxy5
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy5") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy5") + File.separator + "out" + File.separator + "out.txt");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt, transport.PollInterval=30")
    public void testVFSProxyPollInterval_30() throws Exception {

        //Related proxy : VFSProxy6
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy6") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy6") + File.separator + "out" + File.separator + "out.txt");

        FileUtils.copyFile(sourceFile, targetFile);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(outfile));
        Assert.assertFalse(outfile.exists());

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport :"
            + " transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt, "
            + "transport.PollInterval=1, transport.vfs.ActionAfterProcess=MOVE")
    public void testVFSProxyActionAfterProcess_Move() throws Exception {

        //Related proxy : VFSProxy7
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy7") + File.separator + "in" + File.separator + "test.txt");
        File targetLockFile = new File(proxyVFSRoots.get("VFSProxy7") + File.separator + "in" + File.separator +
                "test.txt.lock");
        File outfile = new File(proxyVFSRoots.get("VFSProxy7") + File.separator + "out" + File.separator + "out.txt");
        File originalFile = new File(proxyVFSRoots.get("VFSProxy7") + File.separator + "original" + File.separator + "test.txt");

        FileUtils.copyFile(sourceFile, targetLockFile);
        FileUtils.moveFile(targetLockFile, targetFile);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));

        Assert.assertTrue(originalFile.exists());
        Assert.assertTrue(doesFileContain(originalFile, "andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt, transport.PollInterval=1, "
            + "transport.vfs.ActionAfterProcess=DELETE")
    public void testVFSProxyActionAfterProcess_DELETE() throws Exception {

        //Related proxy : VFSProxy8
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy8") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy8") + File.separator + "out" + File.separator + "out.txt");
        File originalFile = new File(proxyVFSRoots.get("VFSProxy8") + File.separator + "original" + File.separator + "test.txt");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));

        Assert.assertFalse(originalFile.exists());
        Assert.assertFalse(targetFile.exists());
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt, " + "transport.PollInterval=1,"
            + " transport.vfs.ReplyFileName = out.txt ")
    public void testVFSProxyReplyFileName_Normal() throws Exception {

        //Related proxy : VFSProxy9
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy9") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy9") + File.separator + "out" + File.separator + "out.txt");

        FileUtils.copyFile(sourceFile, targetFile);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt, " + "transport.PollInterval=1, "
            + "transport.vfs.ReplyFileName = out123@wso2_text.txt ")
    public void testVFSProxyReplyFileName_SpecialChars() throws Exception {

        //Related proxy : VFSProxy10

        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy10") + File.separator + "in" + File.separator + "test.txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy10") + File.separator + "out" + File.separator + "out123@wso2_text.txt");

        FileUtils.copyFile(sourceFile, targetFile);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir " + "transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt, " + "transport.PollInterval=1, "
            + "transport.vfs.ReplyFileName = not specified ")
    public void testVFSProxyReplyFileName_NotSpecified() throws Exception {

        //Related proxy : VFSProxy11
        File sourceFile = new File(pathToVfsDir + File.separator + "test.txt");
        File targetFile = new File(proxyVFSRoots.get("VFSProxy11") + File.separator + "in" + File.separator + "test" +
                ".txt");
        File outfile = new File(proxyVFSRoots.get("VFSProxy11") + File.separator + "out" + File.separator + "response.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "andun@wso2.com"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.ActionAfterFailure=MOVE")
    public void testVFSProxyActionAfterFailure_MOVE() throws Exception {

        //Related proxy : VFSProxy12
        String proxyName = "VFSProxy12";
        File sourceFile = new File(pathToVfsDir + File.separator + "fail.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File originalFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "failure" + File.separator + "fail.xml");

        FileUtils.copyFile(sourceFile, targetFile);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(outfile));
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(originalFile));
        Assert.assertFalse(outfile.exists());

        Assert.assertTrue(originalFile.exists());
        Assert.assertTrue(doesFileContain(originalFile, "andun@wso2.com"));
        Assert.assertFalse(
                new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml.lock").exists(),
                "lock file exists even after moving the failed file");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.ActionAfterFailure=DELETE")
    public void testVFSProxyActionAfterFailure_DELETE() throws Exception {

        //Related proxy : VFSProxy13
        String proxyName = "VFSProxy13";
        File sourceFile = new File(pathToVfsDir + File.separator + "fail.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File originalFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "failure" + File.separator + "fail.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(targetFile));
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(outfile));
        Assert.assertFalse(originalFile.exists());
        Assert.assertFalse(
                new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml.lock").exists(),
                "lock file exists even after moving the failed file");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport :"
            + " transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.ActionAfterFailure=NotSpecified")
    public void testVFSProxyActionAfterFailure_NotSpecified() throws Exception {

        String proxyName = "VFSProxy14";
        File sourceFile = new File(pathToVfsDir + File.separator + "fail.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File originalFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "failure" + File.separator + "fail.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(targetFile));
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(outfile));
        Assert.assertFalse(outfile.exists());
        Assert.assertFalse(originalFile.exists());
        Assert.assertFalse(
                new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml.lock").exists(),
                "lock file exists even after moving the failed file");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Invalid, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml")
    public void testVFSProxyFileURIInvalid() {

        String proxyName = "VFSProxy15";
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(isFileNotExist(outfile));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = Invalid, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.FileURI = Invalid")
    public void testVFSProxyContentType_Invalid() throws Exception {

        String proxyName = "VFSProxy16";
        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "test.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");

        FileUtils.copyFile(sourceFile, targetFile);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));

        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "WSO2 Company"));
    }

    //https://wso2.org/jira/browse/ESBJAVA-2273
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = Not Specified, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.FileURI = Invalid", enabled = false)
    public void testVFSProxyContentType_NotSpecified() throws Exception {

        String proxyName = "VFSProxy17";
        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "test.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).pollInterval(50, TimeUnit.MILLISECONDS).
                atMost(60, TimeUnit.SECONDS).until(isFileNotExist(outfile));

        Assert.assertFalse(outfile.exists());
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.ActionAfterProcess = Invalid")
    public void testVFSProxyActionAfterProcess_Invalid() throws Exception {

        String proxyName = "VFSProxy19";
        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "test.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File originalFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "original" + File.separator + "test.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(targetFile));
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(60, TimeUnit.SECONDS).until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "WSO2 Company"));
        Assert.assertFalse(originalFile.exists());
        Assert.assertFalse(targetFile.exists());
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport :"
            + " transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.ActionAfterFailure = Invalid")
    public void testVFSProxyActionAfterFailure_Invalid() throws Exception {

        String proxyName = "VFSProxy20";

        File sourceFile = new File(pathToVfsDir + File.separator + "fail.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File originalFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "failure" + File.separator + "fail.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(targetFile));
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(isFileNotExist(outfile));
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(isFileNotExist(originalFile));
        Assert.assertFalse(outfile.exists());
        Assert.assertFalse(originalFile.exists());
        Assert.assertFalse(targetFile.exists());
        Assert.assertFalse(
                new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml.lock").exists(),
                "lock file exists even after moving the failed file");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path," + " transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.MoveAfterProcess = processed")
    public void testVFSProxyMoveAfterProcessInvalidFile() throws Exception {

        String proxyName = "VFSProxy21";

        File sourceFile = new File(pathToVfsDir + File.separator + "fail.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml");
        File originalFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "processed" + File.separator + "test.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(targetFile));
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(60, TimeUnit.SECONDS).until(isFileNotExist(outfile));

        Assert.assertFalse(outfile.exists(), "Out put file found");
        Assert.assertFalse(originalFile.exists(), "Input file moved even if file processing is failed");
        Assert.assertFalse(targetFile.exists(), "Input file found after reading the file");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path," + " transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.MoveAfterProcess = processed",
            dependsOnMethods = "testVFSProxyMoveAfterProcessInvalidFile")
    public void testVFSProxyMoveAfterProcess() throws Exception {

        String proxyName = "VFSProxy21";

        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "test.xml");
        File targetLockFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator +
                "test.xml.lock");
        File originalFileAfterProcessed = new File(
                proxyVFSRoots.get(proxyName) + File.separator + "processed" + File.separator + "test.xml");

        FileUtils.copyFile(sourceFile, targetLockFile);
        FileUtils.moveFile(targetLockFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));

        Assert.assertTrue(outfile.exists(), "out put file not found");
        Assert.assertTrue(doesFileContain(outfile, "WSO2 Company"), "Invalid Response message.");
        //input file should be moved to processed directory after processing the input file
        Assert.assertTrue(originalFileAfterProcessed.exists(), "Input file is not moved after processing the file");
        Assert.assertFalse(targetFile.exists(), "Input file is exist after processing the input file");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml " + "transport.vfs.MoveAfterFailure = Invalid")
    public void testVFSProxyMoveAfterFailure() throws Exception {

        String proxyName = "VFSProxy22";

        File sourceFile = new File(pathToVfsDir + "fail.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "fail.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");
        File originalFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "invalid" + File.separator + "fail.xml");
        /*File lockFile = new File(pathToVfsDir + "test" + File.separator + "in" + File.separator +
                                 "fail.xml.lock");*/

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(targetFile));
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(outfile));
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(originalFile));


        Assert.assertFalse(outfile.exists(), "Out put file found");
        Assert.assertTrue(originalFile.exists(),
                "Input file not moved even if failure happens while building message");
        Assert.assertFalse(targetFile.exists(), "input file not found even if it is invalid file");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml, " + "transport.vfs.ReplyFileURI  = Invalid")
    public void testVFSProxyReplyFileURI_Invalid() throws Exception {

        String proxyName = "VFSProxy23";

        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "test.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "invalid" + File.separator + "out.xml");
        deleteFile(outfile); //delete outfile dir if exists
        FileUtils.cleanDirectory(new File(proxyVFSRoots.get(proxyName) + File.separator + "in"));

        FileUtils.copyFile(sourceFile, targetFile);

        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileExist(outfile));
        Assert.assertTrue(outfile.exists());
        Assert.assertTrue(doesFileContain(outfile, "WSO2 Company"));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport :"
            + " transport.vfs.FileURI = Linux Path, " + "transport.vfs.ContentType = text/xml, "
            + "transport.vfs.FileNamePattern = - *\\.xml, " + "transport.vfs.ReplyFileName  = Invalid")
    public void testVFSProxyReplyFileName_Invalid() throws Exception {

        String proxyName = "VFSProxy24";

        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File targetFile = new File(proxyVFSRoots.get(proxyName) + File.separator + "in" + File.separator + "test.xml");
        File outfile = new File(proxyVFSRoots.get(proxyName) + File.separator + "out" + File.separator + "out.xml");

        FileUtils.copyFile(sourceFile, targetFile);
        Awaitility.await().pollInterval(50, TimeUnit.MILLISECONDS).atMost(60, TimeUnit.SECONDS)
                .until(isFileNotExist(targetFile));
        Awaitility.await().pollDelay(2, TimeUnit.SECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(60, TimeUnit.SECONDS).until(isFileNotExist(outfile));
        Assert.assertFalse(outfile.exists());
    }

    private void addVFSProxy1() throws Exception {

        String proxyName = "VFSProxy1";

        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy1\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                           <log level=\"full\"/>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy2() throws Exception {

        String proxyName = "VFSProxy2";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy2\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test" + File.separator + proxyName
                + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.txt\"/>\n"
                + "                               </endpoint>\n" + "                           </send>"
                + "                        </inSequence>" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy3() throws Exception {

        String proxyName = "VFSProxy3";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy3\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.*</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.txt\"/>\n"
                + "                               </endpoint>\n" + "                           </send>"
                + "                        </inSequence>" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy4() throws Exception {

        String proxyName = "VFSProxy4";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy4\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\"></parameter>"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.txt\"/>\n"
                + "                               </endpoint>\n" + "                           </send>"
                + "                        </inSequence>" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy5() throws Exception {

        String proxyName = "VFSProxy5";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy5\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.txt\"/>\n"
                + "                               </endpoint>\n" + "                           </send>"
                + "                        </inSequence>" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy6() throws Exception {

        String proxyName = "VFSProxy6";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy6\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">30</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.txt\"/>\n"
                + "                               </endpoint>\n" + "                           </send>"
                + "                        </inSequence>" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy7() throws Exception {

        String proxyName = "VFSProxy7";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy7\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n"
                + "                <parameter name=\"transport.vfs.MoveAfterProcess\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "original" + File.separator + "</parameter>"
                + "           <target>\n"
                + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.txt\"/>\n"
                + "                               </endpoint>\n" + "                           </send>"
                + "                        </inSequence>" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy8() throws Exception {

        String proxyName = "VFSProxy8";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy8\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterProcess\">DELETE</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.txt\"/>\n"
                + "                               </endpoint>\n" + "                           </send>"
                + "                        </inSequence>" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy9() throws Exception {

        String proxyName = "VFSProxy9";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy9\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property name=\"transport.vfs.ReplyFileName\" value=\"out.txt\" scope=\"transport\"/>"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName+ File.separator + "out" + File.separator + "\"/>\n" + "                               </endpoint>\n"
                + "                           </send>"
                + "                        </inSequence>"
                + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy10() throws Exception {

        String proxyName = "VFSProxy10";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy10\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property name=\"transport.vfs.ReplyFileName\" value=\"out123@wso2_text.txt\" scope=\"transport\"/>"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                                 <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "\"/>\n"
                + "                                 </endpoint>\n"
                + "                           </send>" + "                        </inSequence>"
                + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy11() throws Exception {

        String proxyName = "VFSProxy11";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy11\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <inSequence>\n"
                + "                           <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                           <log level=\"full\"/>\n" + "                           <send>\n"
                + "                               <endpoint name=\"FileEpr\">\n"
                + "                                   <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "\"/>\n" + "</endpoint>\n"
                + "                           </send>" + "                        </inSequence>"
                + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy12() throws Exception {

        String proxyName = "VFSProxy12";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy12\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.MoveAfterFailure\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "failure" + File.separator + "</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterFailure\">MOVE</parameter>"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy13() throws Exception {

        String proxyName = "VFSProxy13";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy13\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterFailure\">DELETE</parameter>"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy14() throws Exception {

        String proxyName = "VFSProxy14";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy14\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy15() throws Exception {

        String proxyName = "VFSProxy15";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy15\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "invalid" + File.separator + "</parameter>"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy16() throws Exception {

        String proxyName = "VFSProxy16";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy16\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">invalid/invalid</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <log level=\"full\"/>"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy17() throws Exception {

        String proxyName = "VFSProxy17";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy17\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy19() throws Exception {

        String proxyName = "VFSProxy19";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy19\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterProcess\">MOVEDD</parameter>\n"
                + "                <parameter name=\"transport.vfs.MoveAfterProcess\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "original" + File.separator + "</parameter>"
                + "                 <target>\n"
                + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                        <log level=\"full\"/>"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy20() throws Exception {

        String proxyName = "VFSProxy20";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy20\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.MoveAfterFailure\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "failure" + File.separator + "</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterFailure\">MOVEDD</parameter>"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy21() throws Exception {

        String proxyName = "VFSProxy21";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy21\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n"
                + "                <parameter name=\"transport.vfs.MoveAfterProcess\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "processed" + File.separator + "</parameter>"
                + "                <parameter name=\"transport.vfs.CreateFolder\">true</parameter>"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                        <log level=\"full\"/>"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy22() throws Exception {

        String proxyName = "VFSProxy22";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy22\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <parameter name=\"transport.vfs.MoveAfterFailure\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "invalid" + File.separator + "</parameter>\n"
                + "                <parameter name=\"transport.vfs.ActionAfterFailure\">MOVE</parameter>"
                + "                <parameter name=\"transport.vfs.CreateFolder\">true</parameter>"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "out.xml\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy23() throws Exception {

        String proxyName = "VFSProxy23";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy23\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                 <log level=\"full\"/>" + "                                <send>\n"
                + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "invalid" + File.separator + "out.xml\"/>"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addVFSProxy24() throws Exception {

        String proxyName = "VFSProxy24";
        OMElement proxy = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy24\" transports=\"vfs\">\n"
                + "                <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "in" + File.separator + "</parameter> <!--CHANGE-->\n"
                + "                <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n"
                + "                <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n"
                + "                <parameter name=\"transport.PollInterval\">1</parameter>\n"
                + "                <target>\n" + "                        <endpoint>\n"
                + "                                <address format=\"soap12\" uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/>\n"
                + "                        </endpoint>\n" + "                        <outSequence>\n"
                + "                                <property name=\"transport.vfs.ReplyFileName\" value=\"out.xml\" scope=\"transport\"/>"
                + "                                <property action=\"set\" name=\"OUT_ONLY\" value=\"true\"/>\n"
                + "                                <send>\n" + "                                        <endpoint>\n"
                + "                                                <address uri=\"vfs:ftpd://" + pathToVfsDir + "test"
                + File.separator + proxyName + File.separator + "out" + File.separator + "\"/> <!--CHANGE-->\n"
                + "                                        </endpoint>\n" + "                                </send>\n"
                + "                        </outSequence>\n" + "                </target>\n" + "        </proxy>");
        addProxy(proxy, proxyName);
    }

    private void addProxy(OMElement proxy, String proxyName) throws IOException {
        createProxyVfsRootDir(proxyName);
        Utils.deploySynapseConfiguration(proxy, proxyName, Utils.ArtifactType.PROXY, false);
    }

    private void createProxyVfsRootDir(String proxyName) {
        // Create folder structure
        File proxyRoot = new File(rootFolder.getPath() + File.separator + proxyName);
        File inDir = new File(rootFolder.getPath() + File.separator + proxyName + File.separator + "in");
        File outDir = new File(rootFolder.getPath() + File.separator + proxyName + File.separator + "out");
        File failureDir = new File(rootFolder.getPath() + File.separator + proxyName + File.separator + "failure");
        File originalDir = new File(rootFolder.getPath() + File.separator + proxyName + File.separator + "original");
        File processedDir = new File(rootFolder.getPath() + File.separator + proxyName + File.separator + "processed");

        Assert.assertTrue(proxyRoot.mkdirs(), "Test VFS Root Directory creation failed for proxy : " + proxyName);
        Assert.assertTrue(inDir.mkdir(), "Test VFS In Directory creation failed for proxy : " + proxyName);
        Assert.assertTrue(outDir.mkdir(), "Test VFS Out Directory creation failed for proxy : " + proxyName);
        Assert.assertTrue(failureDir.mkdir(), "Test VFS Failure Directory creation failed for proxy : " + proxyName);
        Assert.assertTrue(originalDir.mkdir(), "Test VFS Original Directory creation failed for proxy : " + proxyName);
        Assert.assertTrue(processedDir.mkdir(),
                          "Test VFS Processed Directory creation failed for proxy : " + proxyName);
        proxyVFSRoots.put(proxyName, proxyRoot);
    }

    private boolean deleteFile(File file) {
        return file.exists() && file.delete();
    }

    private Callable<Boolean> isFileExist(final File file) {
        return file::exists;
    }

    private Callable<Boolean> isFileNotExist(final File file) {
        return () -> !file.exists();
    }

    private boolean doesFileContain(File file, String message) throws InterruptedException, IOException {
        for (int i = 0; i < 60; i++) {
            if (FileUtils.readFileToString(file).contains(message)) {
                return true;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        return false;
    }
}

