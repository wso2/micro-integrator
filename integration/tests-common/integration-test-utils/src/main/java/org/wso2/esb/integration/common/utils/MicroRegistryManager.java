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

package org.wso2.esb.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.esb.integration.common.utils.common.FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Micro-Integrator registry manager for integration tests
 */
public class MicroRegistryManager {

    private HashMap<String, byte[]> backupResources;
    private static final String CONF_PATH = "conf:";
    private static final String GOV_PATH = "gov:";

    private static final Log log = LogFactory.getLog(MicroRegistryManager.class);

    public MicroRegistryManager() {
        backupResources = new LinkedHashMap<>();
    }

    /**
     * Function to add resource to the Micro Registry
     *
     * @param resourcePath resource path in WSO2 registry path style
     * @param sourcePath source file
     * @throws MicroRegistryManagerException if error occurred while updating resource
     */
    public void addResource(String resourcePath, String sourcePath) throws MicroRegistryManagerException {
        updateResource(resourcePath, sourcePath, false);
    }

    /**
     * Function to add resource to the Micro Registry
     *
     * @param filePath resource path in WSO2 registry path style
     * @param sourcePath source file
     * @param propertyName source file
     * @param value property value
     * @throws MicroRegistryManagerException if error occurred while updating resource
     */
    public void addProperty(String filePath, String sourcePath, String propertyName, String value) throws
            MicroRegistryManagerException {
        updateProperty(filePath, sourcePath, propertyName, value, false);
    }

    /**
     * Function to update existing resource from the Micro Registry
     *
     * @param resourcePath resource path in WSO2 registry path style
     * @param sourcePath source file
     * @param backupOriginal bool to indicate whether to backup the original resource if exists
     * @throws MicroRegistryManagerException if error occurred while updating resource
     */
    public void updateResource(String resourcePath, String sourcePath, boolean backupOriginal) throws MicroRegistryManagerException {

        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(System.getProperty("carbon.home")).append(File.separator).append("registry").append(File.separator);

        if (resourcePath.startsWith(CONF_PATH)) {
            String relativePath = resourcePath.substring(5).replace('/', File.separatorChar);
            pathBuilder.append("config").append(relativePath);
        } else if (resourcePath.startsWith(GOV_PATH)) {
            String relativePath = resourcePath.substring(4).replace('/', File.separatorChar);
            pathBuilder.append("governance").append(relativePath);
        } else {
            throw new MicroRegistryManagerException("Unknown resource path: " + resourcePath);
        }

        String targetPath = pathBuilder.toString();
        String message = " source:" + sourcePath + " to: " + targetPath;

        if (backupOriginal) {
            //backup original file if exists
            try {
                backupFile(targetPath);
            } catch (IOException e) {
                throw new MicroRegistryManagerException("Error occurred while taking backup of file: " + targetPath, e);
            }
        }

        try {
            FileManager.copyFile(Paths.get(sourcePath).toFile(), Paths.get(targetPath).toString());
            log.info("Successfully copied" + message);
        } catch (IOException e) {
            throw new MicroRegistryManagerException("Error occurred while copying" + message, e);
        }
    }

    /**
     * Function is to check if the registry resources are exist in filepath
     *
     * @param sourcePath path to registry resource
     * @param resourcePath path to resource file path from registry resource
     * @param filename registry filename
     */

    public boolean checkResourceExist(String sourcePath, String resourcePath, String filename) throws Exception {
        try {
            String path = sourcePath + resourcePath + filename;
            return path.contains(filename);

        } catch (Exception e) {
            throw new Exception("Exception occurred while checking the registry resource file.", e);
        }
    }

    /**
     * Function to update existing property from the Micro Registry
     *
     * @param collectionPath resource path in WSO2 registry path style
     * @param resourceName resource path in WSO2 registry path style
     * @param propertyName source file
     * @param value property value
     * @param backupOriginal bool to indicate whether to backup the original resource if exists
     * @throws MicroRegistryManagerException if error occurred while updating resource
     */
    public void updateProperty(String collectionPath, String resourceName, String propertyName, String value, boolean
            backupOriginal) throws MicroRegistryManagerException {

        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(System.getProperty("carbon.home")).append(File.separator).append("registry").append(File.separator);
        if (collectionPath.startsWith(CONF_PATH)) {
            String relativePath = collectionPath.substring(5).replace('/', File.separatorChar);
            pathBuilder.append("config").append(relativePath);
        } else if (collectionPath.startsWith(GOV_PATH)) {
            String relativePath = collectionPath.substring(4).replace('/', File.separatorChar);
            pathBuilder.append("governance").append(relativePath);
        } else {
            throw new MicroRegistryManagerException("Unknown resource path: " + collectionPath);
        }

        String targetPath = pathBuilder.toString();
        String propertiesFileName = resourceName.split("/")[0] + ".properties";
        String message = " property name:" + propertyName + " to: " + targetPath;

        boolean isExists = new File(targetPath + resourceName).isFile();
        try {
            if (!isExists) {
                new File(targetPath + resourceName).createNewFile();
                new File(targetPath + propertiesFileName).createNewFile();
            }
            if (backupOriginal) {
                //backup original file if exists
                backupFile(targetPath);
            }
            Properties prop = new Properties();
            if (isExists) {
                InputStream input = new FileInputStream(targetPath + propertiesFileName);
                prop.load(input);
            }
            OutputStream output = new FileOutputStream(targetPath + propertiesFileName);
            prop.setProperty(propertyName, value);
            prop.store(output, null);
        } catch (IOException e) {
            throw new MicroRegistryManagerException("Error occurred while adding property" + message, e);
        }
    }

    /**
     * Function to update existing property from the Micro Registry
     *
     * @param collectionPath resource path in WSO2 registry path style
     * @param resourceName resource path in WSO2 registry path style
     * @param propertyName source file
     * @return property value
     * @throws MicroRegistryManagerException if error occurred while updating resource
     */
    public String getProperty(String collectionPath, String resourceName, String propertyName) throws
            MicroRegistryManagerException {

        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(System.getProperty("carbon.home")).append(File.separator).append("registry").append(File.separator);

        if (collectionPath.startsWith(CONF_PATH)) {
            String relativePath = collectionPath.substring(5).replace('/', File.separatorChar);
            pathBuilder.append("config").append(relativePath);
        } else if (collectionPath.startsWith(GOV_PATH)) {
            String relativePath = collectionPath.substring(4).replace('/', File.separatorChar);
            pathBuilder.append("governance").append(relativePath);
        } else {
            throw new MicroRegistryManagerException("Unknown resource path: " + collectionPath);
        }

        String targetPath = pathBuilder.toString();
        String message = " get property:" + propertyName + " from: " + targetPath;
        try (InputStream input = new FileInputStream(targetPath + resourceName + ".properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty(propertyName);
        } catch (IOException e) {
            throw new MicroRegistryManagerException("Error occurred while getting property" + message, e);
        }
    }

    /**
     * Function to restore backed up resources by this utility
     *
     * @throws MicroRegistryManagerException
     */
    public void restoreOriginalResources() throws MicroRegistryManagerException {
        for (Map.Entry<String, byte[]> backupEntry : backupResources.entrySet()) {
            restoreFile(backupEntry.getKey(), backupEntry.getValue());
            backupResources.remove(backupEntry.getKey());
        }
    }

    private void backupFile(String pathToFile) throws IOException {

        File sourceFile = new File(pathToFile);
        if (sourceFile.exists() && sourceFile.isFile()) {
            backupResources.put(pathToFile, Files.readAllBytes(sourceFile.toPath()));
        }
    }

    private void restoreFile(String path, byte[] data) throws MicroRegistryManagerException {

        try {
            Files.write(Paths.get(path), data, StandardOpenOption.WRITE);
            log.info("Successfully restored file: " + path);
        } catch (IOException e) {
            throw new MicroRegistryManagerException("Error occurred while restoring file : " + path, e);
        }
    }


    /**
     * Custom exception for exceptions related for MicroRegistryManager
     */
    public class MicroRegistryManagerException extends Exception {

        public MicroRegistryManagerException() {

        }

        public MicroRegistryManagerException(String message) {

            super(message);
        }

        public MicroRegistryManagerException(String message, Throwable cause) {

            super(message, cause);
        }

        public MicroRegistryManagerException(Throwable cause) {

            super(cause);
        }

    }
 }
