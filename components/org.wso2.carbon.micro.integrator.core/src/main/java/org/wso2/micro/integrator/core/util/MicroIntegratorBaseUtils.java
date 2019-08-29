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

package org.wso2.micro.integrator.core.util;

import org.apache.axis2.Constants;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementPermission;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Boolean.TRUE;

public class MicroIntegratorBaseUtils {

    private static final String REPOSITORY = "repository";
    private static boolean isServerConfigInitialized;

    public static String getServerXml() {
        String carbonXML = System.getProperty(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH);
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
        String carbonConfigDirPath = System.getProperty(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH);
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System.getenv(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH_ENV);
            if (carbonConfigDirPath == null) {
                return getCarbonHome() + File.separator + "repository" + File.separator + "conf";
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

    /**
     * Returns a copy of the provided array. Same as the JDK 1.6 Arrays.copyOf() method
     *
     * @param original The original array
     * @param <T>      Type of objects in the original array
     * @return Copy of the provided array
     */
    public static <T> T[] arrayCopyOf(T[] original) {
        if (original == null) {
            return null;
        }
        Class newType = original.getClass();
        int newLength = original.length;
        T[] copy = (newType == Object[].class) ?
                (T[]) new Object[newLength] :
                (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0, newLength);
        return copy;
    }

    /**
     * Check if this is an Instance started by a Java exec
     *
     * @return true if this is an instance started by Java exec
     */
    public static boolean isChildNode() {
        return TRUE.equals(System.getProperty("instance"));
    }

    /**
     * Check whther the specified Strin corresponds to a URL
     *
     * @param location The String to be checked
     * @return true - if <code>location</code> is a URL, false - otherwise
     */
    public static boolean isURL(String location) {
        try {
            new URL(location);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String getAxis2Xml() {
        String axis2XML = CarbonServerConfigurationService.getInstance().
                getFirstProperty("Axis2Config.ConfigurationFile");
        if (axis2XML == null) {
            axis2XML = System.getProperty(Constants.AXIS2_CONF);
        }
        return axis2XML;
    }

    /**
     * Check whether this is the multiple Instance scenario- means started the server with -n arg
     *
     * @return true if the server started with -n argument
     */
    public static boolean isMultipleInstanceCase() {
        return System.getProperty("instances.value") != null;
    }

    public static String getComponentsRepo() {
        String componentsRepo = System.getProperty(ServerConstants.COMPONENT_REP0);
        if (componentsRepo == null) {
            componentsRepo = System.getenv(CarbonConstants.COMPONENT_REP0_ENV);
            if (componentsRepo == null) {
                return getCarbonHome() + File.separator + REPOSITORY + File.separator + "components" + File.separator
                        + "plugins";
            }
        }
        return componentsRepo;
    }

    /**
     * Reads the AAR services dir from the Axis config. if it is null, returns the default value
     * used in Carbon
     *
     * @param axisConfig - AxisConfiguration instance
     * @return - services dir name
     */
    public static String getAxis2ServicesDir(AxisConfiguration axisConfig) {
        String servicesDir = "axis2services";
        String serviceDirPara = (String) axisConfig.getParameterValue(DeploymentConstants.SERVICE_DIR_PATH);
        if (serviceDirPara != null) {
            servicesDir = serviceDirPara;
        }
        return servicesDir;
    }

    public static String getAxis2Repo() {
        String axis2Repo = System.getProperty(ServerConstants.AXIS2_REPO);
        if (axis2Repo == null) {
            axis2Repo = System.getenv(CarbonConstants.AXIS2_REPO_ENV);
        }
        return axis2Repo;
    }

    public static String getCarbonRepository() {
        ServerConfiguration serverConfig = getServerConfiguration();
        return serverConfig
                .getFirstProperty("Axis2Config.RepositoryLocation"); //TODO: Change to Carbon.Repository in carbon.xml
    }

    public static ServerConfiguration getServerConfiguration() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        if (!isServerConfigInitialized) {
            String serverXml = MicroIntegratorBaseUtils.getServerXml();
            File carbonXML = new File(serverXml);
            InputStream inSXml = null;
            try {
                inSXml = new FileInputStream(carbonXML);
                serverConfig.init(inSXml);
                isServerConfigInitialized = true;
            } catch (Exception e) {
                //log.error("Cannot read file " + serverXml, e);
            } finally {
                if (inSXml != null) {
                    try {
                        inSXml.close();
                    } catch (IOException e) {
                        //log.warn("Cannot close file " + serverXml, e);
                    }
                }
            }
        }
        return serverConfig;
    }

}
