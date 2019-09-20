/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.server.extensions;

import org.wso2.micro.integrator.server.LauncherConstants;
import org.wso2.micro.integrator.server.MicroIntegratorLaunchExtension;
import org.wso2.micro.integrator.server.util.FileUtils;
import org.wso2.micro.integrator.server.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FragmentBundleCreator implements MicroIntegratorLaunchExtension {
    private static final Logger logger = Logger.getLogger(FragmentBundleCreator.class.getName());

    private static String FRAGMENT_BUNDLE_VERSION = "1.0.0";

    public void perform() {
        File[] files = getBundleConfigs();
        if (files.length > 0) {
            for (File file : files) {
                String fragmentHostBundleName = getFragmentHostBundleName(file);
                String fragmentBundleName = getFragmentBundleName(file);

                try {
                    Manifest mf = new Manifest();
                    Attributes attribs = mf.getMainAttributes();
                    attribs.putValue(LauncherConstants.MANIFEST_VERSION, "1.0");
                    attribs.putValue(LauncherConstants.BUNDLE_MANIFEST_VERSION, "2");
                    attribs.putValue(LauncherConstants.BUNDLE_NAME, fragmentBundleName);
                    attribs.putValue(LauncherConstants.BUNDLE_SYMBOLIC_NAME, fragmentBundleName);
                    attribs.putValue(LauncherConstants.BUNDLE_VERSION, FRAGMENT_BUNDLE_VERSION);
                    attribs.putValue(LauncherConstants.FRAGMENT_HOST, fragmentHostBundleName);
                    attribs.putValue(LauncherConstants.BUNDLE_CLASSPATH, ".");
                    String dropinsPath = System.getProperty(LauncherConstants.CARBON_DROPINS_DIR_PATH);
                    File dropinsFolder;
                    if (dropinsPath == null) {
                        dropinsFolder = new File(Utils.getCarbonComponentRepo(), "dropins");
                    } else {
                        dropinsFolder = new File(dropinsPath);
                    }
                    String targetFilePath = dropinsFolder.getAbsolutePath() + File.separator + fragmentBundleName + "_"
                            + FRAGMENT_BUNDLE_VERSION + ".jar";

                    String tempDirPath =
                            Utils.JAR_TO_BUNDLE_DIR + File.separator + System.currentTimeMillis() + Math.random();

                    FileOutputStream mfos = null;
                    try {
                        if (file.isDirectory()) {
                            FileUtils.copyDirectory(file, new File(tempDirPath));
                        } else { // is a single file..
                            Utils.copyFileToDir(file, new File(tempDirPath));
                        }
                        String metaInfPath = tempDirPath + File.separator + "META-INF";
                        if (!new File(metaInfPath).mkdirs()) {
                            throw new IOException("Failed to create the directory: " + metaInfPath);
                        }
                        mfos = new FileOutputStream(metaInfPath + File.separator + "MANIFEST.MF");
                        mf.write(mfos);

                        Utils.archiveDir(targetFilePath, tempDirPath);
                        Utils.deleteDir(new File(tempDirPath));
                    } finally {
                        try {
                            if (mfos != null) {
                                mfos.close();
                            }
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Unable to close the OutputStream " + e.getMessage(), e);
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error occured while creating the log4j prop fragment bundle.", e);
                }
            }
        }
    }

    protected abstract File[] getBundleConfigs();

    protected abstract String getFragmentHostBundleName(File dirName);

    protected abstract String getFragmentBundleName(File dirName);
}
