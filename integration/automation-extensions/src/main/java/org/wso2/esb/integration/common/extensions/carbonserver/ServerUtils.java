/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.esb.integration.common.extensions.carbonserver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

class ServerUtils {

    private static final Log log = LogFactory.getLog(ServerUtils.class);

    private ServerUtils() {
        //
    }

    static void copyResources(String product, String destCarbonHome, String clusterDepDir, String clusterRegDir)
            throws IOException {

        String carbonHome =
                FrameworkPathUtil.getSystemResourceLocation() + File.separator + "artifacts" + File.separator + product
                        + File.separator + "server";
        copyFolders(new File(carbonHome + File.separator + "conf"), new File(destCarbonHome + File.separator + "conf"));
        copyFolders(new File(carbonHome + File.separator + "lib"), new File(destCarbonHome + File.separator + "lib"));
        copyFolders(new File(carbonHome + File.separator + "bin"), new File(destCarbonHome + File.separator + "bin"));

        File destinationDeploymentDirectory = new File(
                String.join(File.separator, destCarbonHome, "repository", "deployment"));
        if (clusterDepDir == null) {
            copyFolders(new File(String.join(File.separator, carbonHome, "repository", "deployment")),
                        destinationDeploymentDirectory);
        } else {
            createSymlink(new File(clusterDepDir), destinationDeploymentDirectory);
        }

        File regDest = new File(destCarbonHome + File.separator + "registry");
        if (clusterRegDir == null) {
            copyFolders(new File(carbonHome + File.separator + "registry"), regDest);
        } else {
            createSymlink(new File(clusterRegDir), regDest);
        }

    }

    private static void copyFolders(File source, File destination) throws IOException {
        if (source.exists() && source.isDirectory()) {
            log.info("Copying " + source.getPath() + " to " + destination.getPath());
            FileUtils.copyDirectory(source, destination, true);
        }
    }

    private static void createSymlink(File source, File destination) throws IOException {

        log.info("Creating sym link from : " + destination.toPath() + " to : " + source.toPath());
        if (destination.exists() && !deleteDirectory(destination)) {
            throw new IOException("Delete failed for : " + destination);
        }
        Files.createSymbolicLink(destination.toPath(), source.toPath());
    }

    private static boolean deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            Arrays.stream(allContents).forEach(ServerUtils::deleteDirectory);
        }
        return directory.delete();
    }

}
