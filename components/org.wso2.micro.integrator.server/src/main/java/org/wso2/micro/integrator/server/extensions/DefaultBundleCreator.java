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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Creates regular OSGi bundles out of regular jar files
 */
public class DefaultBundleCreator implements MicroIntegratorLaunchExtension {
    private static final String JARS_DIR;

    static {
        String externalLibPath = System.getProperty(LauncherConstants.CARBON_EXTERNAL_LIB_DIR_PATH);
        if (externalLibPath != null) {
            JARS_DIR = Paths.get(System.getProperty(LauncherConstants.CARBON_HOME))
                    .relativize(Paths.get(externalLibPath)).toString();
        } else {
            String componentPath = System.getProperty(LauncherConstants.CARBON_COMPONENTS_DIR_PATH);
            if (componentPath == null) {
                JARS_DIR = Paths.get("repository", "components", "lib").toString();
            } else {
                Path path = Paths.get(componentPath, "lib");
                JARS_DIR = Paths.get(System.getProperty(LauncherConstants.CARBON_HOME)).relativize(path).toString();
            }
        }
    }

    public void perform() {
        String dropinsPath = System.getProperty(LauncherConstants.CARBON_DROPINS_DIR_PATH);
        File dropinsFolder;
        if (dropinsPath == null) {
            dropinsFolder = new File(Utils.getCarbonComponentRepo(), "dropins");
        } else {
            dropinsFolder = new File(dropinsPath);
        }

        File dir = Utils.getBundleDirectory(JARS_DIR);
        File[] files = dir.listFiles(new Utils.JarFileFilter());
        if (files != null) {
            for (File file : files) {
                try {
                    Manifest mf = new Manifest();
                    Attributes attribs = mf.getMainAttributes();
                    attribs.putValue(LauncherConstants.DYNAMIC_IMPORT_PACKAGE, "*");
                    Utils.createBundle(file, dropinsFolder, mf, "");
                } catch (Throwable e) {
                    System.err.println("Cannot create bundle from jar file " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
    }
}
