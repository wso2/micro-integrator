/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integration.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DefaultSuiteInitializer {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSuiteInitializer.class);

    /**
     * System property name used to lookup the location of the Micro Integrator Distribution file. This is expected to
     * be set by the maven surefire plugin.
     */
    private static final String SYSTEM_PROPERTY_DISTRIBUTION_ZIP_LOCATION = "distribution.zip";

    /**
     * This log message is used to verify the successful initialization of the Micro Integrator runtime.
     */
    private static final String SERVER_STARTUP_MESSAGE = "WSO2 Micro Integrator started";

    /**
     * Store the location of Micro Integrator home directory.
     */
    private String miHome;

    @BeforeSuite
    public void startMicroIntegrator() throws IOException, InterruptedException {

        System.out.println("Test =======================================");
        String miDistributionZipLocation = readDistributionZipLocation();

        miHome = prepareMicroIntegrator(miDistributionZipLocation);

        LOGGER.info("Initializing micro integrator runtime .........");

        int indexOfZip = miDistributionZipLocation.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(miDistributionZipLocation + " is not a zip file");
        }

        ServerLogReader inputStreamHandler = startOsProcess(miHome, getStartScriptCommand());

        // wait until server startup is completed
        waitTill(() -> !inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE), 60, TimeUnit.SECONDS);

        if (!inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE)) {
            throw new RuntimeException("Server initialization failed");
        }
        LOGGER.info("Server started successfully.");
    }

    @Test
    public void test123(){
        LOGGER.info("Server started test successfully.");
    }

    @AfterSuite
    public void stopMicroIntegrator() throws IOException, InterruptedException {
        LOGGER.info("Shutting down server..");

        startProcess(miHome, getStartScriptCommand("stop"));

        waitTill(() -> isRemotePortInUse("localhost", 8290), 60, TimeUnit.SECONDS);
    }

    private void waitTill(BooleanSupplier predicate, int maxWaitTime, TimeUnit timeUnit) throws InterruptedException {
        long time = System.currentTimeMillis() + timeUnit.toMillis(maxWaitTime);
        while (predicate.getAsBoolean() && System.currentTimeMillis() < time) {
            TimeUnit.MILLISECONDS.sleep(1);
        }
    }

    private boolean isRemotePortInUse(String hostName, int portNumber) {
        try {
            // Socket try to open a REMOTE port
            new Socket(hostName, portNumber).close();
            // remote port can be opened, this is a listening port on remote machine
            // this port is in use on the remote machine !
            return true;
        } catch(IOException e) {
            // remote port is closed, nothing is running on
            return false;
        }
    }

    private String readDistributionZipLocation() {
        String miDistributionZipLocation = System.getProperty(SYSTEM_PROPERTY_DISTRIBUTION_ZIP_LOCATION);

        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if (fileSeparator.equals("\\")) {
            miDistributionZipLocation = miDistributionZipLocation.replace("/", "\\");
        }

        return miDistributionZipLocation;
    }

    private ServerLogReader startOsProcess(String miHome, String[] cmdArray) throws IOException {
        Process tempProcess = startProcess(miHome, cmdArray);

        ServerLogReader errorStreamHandler = new ServerLogReader("errorStream", tempProcess.getErrorStream());
        ServerLogReader inputStreamHandler = new ServerLogReader("inputStream", tempProcess.getInputStream());
        // start the stream readers
        inputStreamHandler.start();
        errorStreamHandler.start();
        return inputStreamHandler;
    }

    private Process startProcess(String workingDirectory, String[] cmdArray) throws IOException {
        File commandDir = new File(workingDirectory);
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        processBuilder.directory(commandDir);
        return processBuilder.start();
    }

    private String[] getStartScriptCommand(String ...commands) {
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        String startScriptBasename = "micro-integrator";

        ArrayList<String> commandArray;
        if (operatingSystem.contains("windows")) {
            commandArray = new ArrayList<>(Arrays.asList(
                    "cmd.exe", "/c", miHome + File.separator + "bin" + File.separator + startScriptBasename + ".bat"));
        } else {
            commandArray = new ArrayList<>(Arrays.asList("sh", miHome + File.separator + "bin" + File.separator +
                    startScriptBasename + ".sh"));
        }

        commandArray.addAll(Arrays.asList(commands));
        return commandArray.toArray(new String[0]);
    }

    private String prepareMicroIntegrator(String miDistributionZipLocation) throws IOException {
        String extractDir = "mitmp" + System.currentTimeMillis();
        String baseDir = System.getProperty("basedir", ".") + File.separator + "target";
        String miHome = baseDir + File.separator + extractDir;

        extractFile(miDistributionZipLocation, miHome);

        int indexOfZip = miDistributionZipLocation.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(miDistributionZipLocation + " is not a zip file");
        }
        String extractedMicroIntegratorDirectory = miDistributionZipLocation.substring(
                miDistributionZipLocation.lastIndexOf(File.separator) + 1, indexOfZip);

        return miHome + File.separator + extractedMicroIntegratorDirectory;
    }

    private static void extractFile(String sourceFilePath, String extractedDir) throws IOException {
        FileOutputStream fileoutputstream = null;
        String fileDestination = extractedDir + File.separator;
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;

        try {
            zipinputstream = new ZipInputStream(new FileInputStream(sourceFilePath));
            ZipEntry zipentry = zipinputstream.getNextEntry();

            while (true) {
                while (true) {
                    if (zipentry != null) {
                        String entryName = fileDestination + zipentry.getName();
                        entryName = entryName.replace('/', File.separatorChar);
                        entryName = entryName.replace('\\', File.separatorChar);
                        File newFile = new File(entryName);
                        boolean fileCreated = false;
                        if (zipentry.isDirectory()) {
                            if (!newFile.exists()) {
                                fileCreated = newFile.mkdirs();
                            }

                            zipentry = zipinputstream.getNextEntry();
                            continue;
                        }

                        File resourceFile = new File(entryName.substring(0, entryName.lastIndexOf(File.separator)));
                        if (resourceFile.exists() || resourceFile.mkdirs()) {
                            fileoutputstream = new FileOutputStream(entryName);

                            int n;
                            while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                                fileoutputstream.write(buf, 0, n);
                            }

                            fileoutputstream.close();
                            zipinputstream.closeEntry();
                            zipentry = zipinputstream.getNextEntry();
                            continue;
                        }
                    }

                    zipinputstream.close();
                    return;
                }
            }
        } catch (IOException var16) {
            LOGGER.error("Error on archive extraction ", var16);
            throw new IOException("Error on archive extraction ", var16);
        } finally {
            if (fileoutputstream != null) {
                fileoutputstream.close();
            }

            if (zipinputstream != null) {
                zipinputstream.close();
            }

        }
    }
}
