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

import org.wso2.micro.integrator.server.util.Utils;

import java.io.File;
import java.io.FilenameFilter;

public class LibraryFragmentBundleCreator extends FragmentBundleCreator {
    @Override
    protected String getFragmentBundleName(File file) {
        return file.getName() + ".config";
    }

    @Override
    protected String getFragmentHostBundleName(File file) {
        return file.getName();
    }

    @Override
    protected File[] getBundleConfigs() {
        File bundleConfigDir = Utils.getBundleConfigDirectory();
        if (bundleConfigDir != null) {
            return bundleConfigDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (dir.isDirectory() && !name.startsWith("."));
                }
            });
        } else {
            return new File[0]; // if bundle directory not found nothing happens..
        }
    }
}
