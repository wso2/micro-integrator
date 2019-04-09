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

package org.wso2.micro.integrator.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
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

    @BeforeSuite
    public void startMicroIntegrator() throws IOException, InterruptedException {
        String miDistributionZipLocation = readDistributionZipLocation();

        String miHome = prepareMicroIntegrator(miDistributionZipLocation);

        LOGGER.info("Initializing micro integrator runtime .........");

        int indexOfZip = miDistributionZipLocation.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(miDistributionZipLocation + " is not a zip file");
        }

        ServerLogReader inputStreamHandler = StartServerInAOsProcess(miHome, getStartupCommand(miHome));

        // wait until server startup is completed
        long time = System.currentTimeMillis() + 60 * 1000;
        while (!inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE) &&
                System.currentTimeMillis() < time) {
            TimeUnit.MILLISECONDS.sleep(1);
        }

        if (!inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE)) {
            throw new RuntimeException("Server initialization failed");
        }
        LOGGER.info("Server started successfully.");
    }

    private String readDistributionZipLocation() {
        String miDistributionZipLocation = System.getProperty(SYSTEM_PROPERTY_DISTRIBUTION_ZIP_LOCATION);

        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if (fileSeparator.equals("\\")) {
            miDistributionZipLocation = miDistributionZipLocation.replace("/", "\\");
        }

        return miDistributionZipLocation;
    }

    private ServerLogReader StartServerInAOsProcess(String miHome, String[] cmdArray) throws IOException {
        File commandDir = new File(miHome);
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        processBuilder.directory(commandDir);
        Process tempProcess = processBuilder.start();

        ServerLogReader errorStreamHandler = new ServerLogReader("errorStream", tempProcess.getErrorStream());
        ServerLogReader inputStreamHandler = new ServerLogReader("inputStream", tempProcess.getInputStream());
        // start the stream readers
        inputStreamHandler.start();
        errorStreamHandler.start();
        return inputStreamHandler;
    }

    private String[] getStartupCommand(String miHome) {
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        String[] cmdArray;

        String startScriptBasename = "micro-integrator";

        if (operatingSystem.contains("windows")) {
            cmdArray = new String[] {
                    "cmd.exe", "/c", miHome + File.separator + "bin" + File.separator + startScriptBasename + ".bat"
            };
        } else {
            cmdArray = new String[] {
                    "sh", miHome + File.separator + "bin" + File.separator + startScriptBasename + ".sh"
            };
        }
        return cmdArray;
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
