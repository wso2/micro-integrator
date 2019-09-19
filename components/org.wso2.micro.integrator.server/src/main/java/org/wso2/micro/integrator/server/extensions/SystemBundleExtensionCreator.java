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
import org.wso2.micro.integrator.server.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * This class will read the extension folder and create extension bundles.
 * User need to drop the classic jars that needed to be extended in ${carbon.home}/extensions
 * folder.
 */
public class SystemBundleExtensionCreator implements MicroIntegratorLaunchExtension {

    private static final String EXTENSIONS_DIR;

    static {
        String extensions = System.getProperty(LauncherConstants.CARBON_EXTENSIONS_DRI_PATH);
        if (extensions == null) {
            String componentsPath = System.getProperty(LauncherConstants.CARBON_COMPONENTS_DIR_PATH);
            if (componentsPath == null) {
                EXTENSIONS_DIR = Paths.get("repository", "components", "extensions").toString();
            } else {
                Path path = Paths.get(componentsPath, "extensions");
                EXTENSIONS_DIR = Paths.get(System.getProperty(LauncherConstants.CARBON_HOME)).relativize(path)
                        .toString();
            }
        } else {
            EXTENSIONS_DIR = Paths.get(System.getProperty(LauncherConstants.CARBON_HOME))
                    .relativize(Paths.get(extensions)).toString();
        }
    }

    private static final String EXTENSION_PREFIX = "org.wso2.micro.integrator.framework.extension.";

    public void perform() {
        String dropinsPath = System.getProperty(LauncherConstants.CARBON_DROPINS_DIR_PATH);
        File dropinsFolder;
        if (dropinsPath == null) {
            dropinsFolder = new File(Utils.getCarbonComponentRepo(), "dropins");
        } else {
            dropinsFolder = new File(dropinsPath);
        }

        File dir = Utils.getBundleDirectory(EXTENSIONS_DIR);
        File[] files = dir.listFiles(new Utils.JarFileFilter());
        if (files != null) {
            for (File file : files) {
                try {
                    Manifest mf = new Manifest();
                    Attributes attribs = mf.getMainAttributes();
                    attribs.putValue(LauncherConstants.FRAGMENT_HOST, "system.bundle; extension:=framework");
                    Utils.createBundle(file, dropinsFolder, mf, EXTENSION_PREFIX);
                } catch (IOException e) {
                    System.err.println(
                            "Cannot create framework extension bundle from jar file " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
    }
}
