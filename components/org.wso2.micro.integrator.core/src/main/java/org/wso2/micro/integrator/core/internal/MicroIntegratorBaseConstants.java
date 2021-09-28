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

package org.wso2.micro.integrator.core.internal;

public class MicroIntegratorBaseConstants {
    public static final String CARBON_CONFIG_DIR_PATH = "carbon.config.dir.path";
    public static final String CARBON_SERVICEPACKS_DIR_PATH = "carbon.servicepacks.dir.path";
    public static final String CARBON_DROPINS_DIR_PATH = "carbon.dropins.dir.path";
    public static final String CARBON_EXTERNAL_LIB_DIR_PATH = "carbon.external.lib.dir.path"; // components/lib
    public static final String CARBON_EXTENSIONS_DIR_PATH = "carbon.extensions.dir.path";
    public static final String CARBON_COMPONENTS_DIR_PATH = "carbon.components.dir.path";
    public static final String CARBON_PATCHES_DIR_PATH = "carbon.patches.dir.path";
    public static final String CARBON_INTERNAL_LIB_DIR_PATH = "carbon.internal.lib.dir.path"; //lib normally internal
    // tomcat
    public static final String CARBON_CONFIG_DIR_PATH_ENV = "CARBON_CONFIG_DIR_PATH";
    public static final String CARBON_HOME = "carbon.home";
    public static final String CARBON_HOME_ENV = "CARBON_HOME";
    public static final String AXIS2_CONFIG_REPO_LOCATION = "Axis2Config.RepositoryLocation";

    /**
     * This is the key of the System property which indicates whether the server is running
     * is standalone mode.
     */
    public static final String STANDALONE_MODE = "wso2.server.standalone";
    public static final String FILE_RESOURCE_MAP = "file.resource.map";
    public static final String WORK_DIR = "WORK_DIR";
    public static final String CARBON_INSTANCE = "local_WSO2_WSAS";
    public static final String LOCAL_IP_ADDRESS = "carbon.local.ip";

    public static final String COMPONENT_REP0_ENV = "COMPONENTS_REPO";
    public static final String AXIS2_REPO_ENV = "AXIS2_REPO";
    public static final String UPDATE_LEVEL = "UPDATE_LEVEL";

    /**
     * Remove default constructor and make it not available to initialize.
     */

    private MicroIntegratorBaseConstants() {
        throw new AssertionError("Instantiating utility class...");
    }
}
