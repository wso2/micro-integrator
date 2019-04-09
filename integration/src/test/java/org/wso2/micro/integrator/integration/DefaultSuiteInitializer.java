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
    private static final String SYSTEM_PROPERTY_CARBON_ZIP_LOCATION = "carbon.zip";
    private static final String SERVER_STARTUP_MESSAGE = "WSO2 Micro Integrator started";

    @BeforeSuite
    public void startMicriIntegrator() throws IOException, InterruptedException {
        String miDistributionZipLocation = System.getProperty(SYSTEM_PROPERTY_CARBON_ZIP_LOCATION);

        String extractDir = "mitmp" + System.currentTimeMillis();
        String baseDir = (System.getProperty("basedir", ".")) + File.separator + "target";

        String miHome = baseDir + File.separator + extractDir;
        LOGGER.info("Extracting {} to {}", miDistributionZipLocation, miHome);

        extractFile(miDistributionZipLocation, miHome);

        LOGGER.info("Initializing micro integrator runtime .........");
        LOGGER.info("User home is {}", System.getProperty("user.home"));

        String operatingSystem = System.getProperty("os.name").toLowerCase();
        if (operatingSystem.contains("windows")) {

        } else {
            int indexOfZip = miDistributionZipLocation.lastIndexOf(".zip");
            if (indexOfZip == -1) {
                throw new IllegalArgumentException(miDistributionZipLocation + " is not a zip file");
            }
            String extractedCarbonDir =
                    miDistributionZipLocation.substring(miDistributionZipLocation.lastIndexOf(File.separator) + 1,
                                                  indexOfZip);
            String[] cmdArray = new String[] {
                    "sh", extractedCarbonDir + File.separator + "bin" + File.separator + "micro-integrator.sh"};
            File commandDir = new File(miHome);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
            processBuilder.directory(commandDir);
            Process tempProcess = processBuilder.start();

            ServerLogReader errorStreamHandler = new ServerLogReader("errorStream", tempProcess.getErrorStream());
            ServerLogReader inputStreamHandler = new ServerLogReader("inputStream", tempProcess.getInputStream());
            // start the stream readers
            inputStreamHandler.start();
            errorStreamHandler.start();

            //wait until Mgt console url printed.
            long time = System.currentTimeMillis() + 60 * 1000;
            while (!inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE) &&
                    System.currentTimeMillis() < time) {
                // wait until server startup is completed
                TimeUnit.MILLISECONDS.sleep(1);
            }

            if (!inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE)) {
                throw new RuntimeException("Server initialization failed");
            }
            LOGGER.info("Server started successfully.");

        }
    }

    private static void extractFile(String sourceFilePath, String extractedDir) throws IOException {
        FileOutputStream fileoutputstream = null;
        String fileDestination = extractedDir + File.separator;
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;

        try {
            zipinputstream = new ZipInputStream(new FileInputStream(sourceFilePath));
            ZipEntry zipentry = zipinputstream.getNextEntry();

            while(true) {
                while(true) {
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
                            while((n = zipinputstream.read(buf, 0, 1024)) > -1) {
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
