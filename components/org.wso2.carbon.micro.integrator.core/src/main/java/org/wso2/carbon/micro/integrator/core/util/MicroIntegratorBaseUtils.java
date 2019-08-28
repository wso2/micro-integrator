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

package org.wso2.carbon.micro.integrator.core.util;

import org.wso2.carbon.micro.integrator.core.internal.MicroIntegratorBaseConstants;

import java.io.File;
import java.lang.management.ManagementPermission;

public class MicroIntegratorBaseUtils {
    public static String getServerXml() {
        String carbonXML = System
                .getProperty(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH);
        /*
         * if user set the system property telling where is the configuration
         * directory
         */
        if (carbonXML == null) {
            return getCarbonConfigDirPath() + File.separator + "carbon.xml";
        }
        return carbonXML + File.separator + "carbon.xml";
    }

    public static String getCarbonConfigDirPath() {
        String carbonConfigDirPath = System
                .getProperty(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH);
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System
                    .getenv(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH_ENV);
            if (carbonConfigDirPath == null) {
                return getCarbonHome() + File.separator + "repository"
                       + File.separator + "conf";
            }
        }
        return carbonConfigDirPath;
    }

    public static String getCarbonHome() {
        String carbonHome = System.getProperty(MicroIntegratorBaseConstants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv(MicroIntegratorBaseConstants.CARBON_HOME_ENV);
            System.setProperty(MicroIntegratorBaseConstants.CARBON_HOME, carbonHome);
        }
        return carbonHome;
    }

    /**
     * Method to test whether a given user has permission to execute the given
     * method.
     */
    public static void checkSecurity() {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
    }
}
