/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mi.registry.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.mi.registry.migration.exception.RegistryMigrationException;
import org.wso2.mi.registry.migration.utils.MigrationClientUtils;

import java.io.Console;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RegistryMigrationClient {
    private static final Logger LOGGER = LogManager.getLogger(RegistryMigrationClient.class);
    private static List<RegistryResource> registryResources = new ArrayList<>();
    private static LoginAdminServiceClient loginAdminServiceClient = null;

    public static void main(String[] args) {
        RegistryExporter registryExporter;
        try {
            String backEndUrl = getValueFromConsole("Please Enter EI Server URL (https://localhost:9443):  ", false,
                                                    "https://localhost:9443");

            // Set System properties for trustStore, trustStoreType, and trustStorePassword
            setInternalTrustStoreProperties();

            // Authenticate the server
            String session = authenticate(backEndUrl);
            if (session == null) {
                LOGGER.error("Unable to authenticate to the server. Please enter valid credentials.");
                return;
            }

            // Get registry resource path to be exported
            String targetRegistryResourcePath = getResourcePath();

            // Get export option
            String option = getExportOption();

            if ("1".equals(option)) {
                // Export as Registry Resource Project
                registryExporter = createRegistryResourceProjectExporter();

            } else if ("2".equals(option)) {
                // Export as Carbon Application
                registryExporter = createRegistryResourceCARExporter();

            } else {
                // Exit
                LOGGER.info("Bye!");
                return;
            }

            if (Objects.isNull(registryExporter)) {
                return;
            }

            ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backEndUrl, session);
            PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient(backEndUrl,
                                                                                                         session);
            List<String> ignorableRegistryResourcesList = new ArrayList<>(
                    Arrays.asList(MigrationClientUtils.IGNORED_REGISTRY_RESOURCES));

            // Fetch all the registry resources that are specified under the given resource path
            String parent = targetRegistryResourcePath.substring(0, targetRegistryResourcePath.lastIndexOf('/'));
            traverseRegistryTree(resourceAdminServiceClient, propertiesAdminServiceClient, targetRegistryResourcePath,
                                 parent, ignorableRegistryResourcesList);
            loginAdminServiceClient.logOut();

            // Export the registry resources either as a Registry Resource Project or as a Carbon Application
            if (!registryResources.isEmpty()) {
                registryExporter.exportRegistry(registryResources);
            } else {
                LOGGER.error(
                        "Error in retrieving data from the server or there is no data for the given registry path");
            }

        } catch (RegistryMigrationException e) {
            LOGGER.error("Sorry! Failed to export the Registry.", e);
            LOGGER.info("Please see the log file located at {} for more information. ", getLogFileLocation());
        }
    }


    /**
     * Configure the log4j.properties file.
     */
    private static String getLogFileLocation() {

        String logFileLocation = System.getProperty(MigrationClientUtils.LOG_FILE_LOCATION_SYS_PROPERTY);
        if (logFileLocation == null) {
            logFileLocation = new File("registry-export.log").getAbsolutePath();
        }
        return logFileLocation;
    }

    /**
     * Get the resource path to be exported as a user input. Validate the user input against to check whether it starts
     * with valid registry resource path.
     *
     * @return registry resource path to be exported
     * @throws RegistryMigrationException if some error happens while getting the user input
     */
    private static String getResourcePath() throws RegistryMigrationException {
        LOGGER.info("\n\nYou can export multiple resources or only a single resource using the resource path. "
                            + "Resource path should be valid one starting with a '/'"
                            + ".\nExample: "
                            + "\n [/] - for entire registry"
                            + "\n [/_system/config/] - for registry resources stored inside config directory"
                            + "\n [/_system/config/example.txt] - for only the example.txt resource stored inside "
                            + "config directory");

        String targetRegistryResourceLocation = getValueFromConsole(
                "\nPlease enter registry resource path you would like to export (/):  ", false, "/");

        while (!targetRegistryResourceLocation.substring(0, 1).matches("^/")) {
            targetRegistryResourceLocation = getValueFromConsole(
                    "\n\nInvalid input. Please enter registry resource path you would like to export (/):  ", false,
                    "/");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Registry resource path to be exported: {}", targetRegistryResourceLocation);
        }
        return targetRegistryResourceLocation;
    }

    /**
     * Get the export option as a user input.
     *
     * @return export option; 1, 2, or 3
     * @throws RegistryMigrationException if some error happens while getting the user input
     */
    private static String getExportOption() throws RegistryMigrationException {
        LOGGER.info("\nSelect one of the below options");
        LOGGER.info(" [1]  Export as Registry Resource Project\n [2]  Export as Carbon Application\n [3]  "
                            + "Exit");
        String option = getValueFromConsole("Please enter your numeric choice:  ", false, null);

        while (!("1".equals(option) || "2".equals(option) || "3".equals(option))) {
            option = getValueFromConsole("Invalid option. Please enter a valid numeric choice:  ", false, null);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Export option: {}", option);
        }
        return option;
    }

    /**
     * Set internal TrustStore System properties.
     *
     * @throws RegistryMigrationException if some error happens while getting the user input
     */
    private static void setInternalTrustStoreProperties() throws RegistryMigrationException {
        String truststoreLocation = getValueFromConsole("Please Enter Internal Truststore Location of EI Server:  ",
                                                        false, null);
        String truststoreType = getValueFromConsole("Please Enter Internal Truststore Type of EI Server (JKS):  ",
                                                    false, "JKS");
        String truststorePassword = getValueFromConsole("Please Enter Internal Truststore Password of EI Server:  ",
                                                        true, null);

        System.setProperty("javax.net.ssl.trustStore", truststoreLocation);
        System.setProperty("javax.net.ssl.trustStoreType", truststoreType);
        System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TrustStore location: {}, TrustStore type: {}", truststoreLocation, truststoreType);
        }
    }

    /**
     * Initialize the RegistryMigrationClient by authenticating the user and retrieving the Registry details from server
     * via Admin services.
     *
     * @param backEndUrl url of the backend EI server
     * @return true if authentication and data retrieval got successfully executed. false otherwise.
     */
    private static String authenticate(String backEndUrl) throws RegistryMigrationException {

        loginAdminServiceClient = new LoginAdminServiceClient(backEndUrl);
        LOGGER.info("\nPlease enter the following admin credentials of the EI server.");
        String username = getValueFromConsole("Enter username:  ", false, null);
        String password = getValueFromConsole("Enter password:  ", true, null);

        return loginAdminServiceClient.authenticate(username, password, getURL(backEndUrl).getHost());
    }

    /**
     * Get the URL object given the url as a string representation.
     *
     * @param url url as a string
     * @return URL instance
     * @throws RegistryMigrationException when something goes wrong when parsing the string url to an URL object
     */
    private static URL getURL(String url) throws RegistryMigrationException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RegistryMigrationException("Invalid EI Server Url. ", e);
        }
    }

    /**
     * Get user inputs and create Registry Resource Project exporter.
     *
     * @return RegistryResourceProjectExporter instance
     * @throws RegistryMigrationException when something went wrong while getting user inputs
     */
    private static RegistryExporter createRegistryResourceProjectExporter() throws RegistryMigrationException {
        try {
            String integrationProjectName = getValueFromConsole("\nPlease enter Integration Project name:  ", false,
                                                                null);
            String groupId = getValueFromConsole("Please enter Group Id (com.example):  ", false, "com.example");
            String artifactId = getValueFromConsole("Please enter Artifact Id (" + integrationProjectName + "):  ",
                                                    false, integrationProjectName);
            String version = getValueFromConsole("Please enter Version (1.0.0):  ", false, "1.0.0");
            String exportDestination = getValueFromConsole("Please enter export destination:  ", false, null);

            File targetFolder = new File(exportDestination + File.separator + integrationProjectName);
            while (targetFolder.exists()) {
                exportDestination = getValueFromConsole("Target directory " + integrationProjectName
                                                                + " already exists in the destination location"
                                                                + ".\nPlease re-enter "
                                                                + "export destination or type 'exit' to exit: ",
                                                        false, null);
                if ("exit".equalsIgnoreCase(exportDestination)) {
                    LOGGER.info("Bye!");
                    return null;
                }
                targetFolder = new File(exportDestination + File.separator + integrationProjectName);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Creating Registry Resource Project Exporter instance. Integration Project Name: {}, Group "
                                + "Id: {}, Artifact Id: {}, Version: {}, Export Destination: "
                                + exportDestination, integrationProjectName, groupId, artifactId, version);
            }
            return new RegistryResourceProjectExporter(integrationProjectName, groupId, artifactId, version,
                                                       exportDestination);

        } catch (RegistryMigrationException e) {
            throw new RegistryMigrationException("Could not export the Registry Resource Project. " + e.getMessage(),
                                                 e);
        }
    }

    /**
     * Get user inputs and create CAR exporter.
     *
     * @return CARExporter instance
     * @throws RegistryMigrationException when something went wrong while getting user inputs
     */
    private static RegistryExporter createRegistryResourceCARExporter() throws RegistryMigrationException {
        try {
            String carName = getValueFromConsole("Please enter CAR name:  ", false, null);
            String carVersion = getValueFromConsole("Please enter CAR version (1.0.0):  ", false, "1.0.0");
            String exportDestination = getValueFromConsole("Please enter export destination:  ", false, null);

            File targetCarFile = new File(exportDestination + File.separator + carName + ".car");
            while (targetCarFile.exists()) {
                exportDestination = getValueFromConsole("The target " + carName
                                                                + ".car file already exists in the destination "
                                                                + "location.\nPlease "
                                                                + "re-enter export destination or type 'exit' to "
                                                                + "exit: ",
                                                        false, null);
                if ("exit".equalsIgnoreCase(exportDestination)) {
                    LOGGER.info("Bye!");
                    return null;
                }
                targetCarFile = new File(exportDestination + File.separator + carName + ".car");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating CAR Exporter instance. Car Name: {}, Version: {}, Export Destination: {}",
                             carName, carVersion, exportDestination);
            }
            return new CARExporter(carName, carVersion, exportDestination);
        } catch (RegistryMigrationException e) {
            throw new RegistryMigrationException("Could not export the Carbon Application (.car). ", e);
        }
    }

    /**
     * Given the resource path, traverse the registry tree recursively to obtain all the resource details.
     *
     * @param resourceAdminServiceClient     ResourceAdminServiceClient instance
     * @param path                           registry resource path
     * @param parentPath                     path of the registry resource's parent node
     * @param ignorableRegistryResourcesList predefined list of the registry resource that should be neglected
     */
    private static void traverseRegistryTree(ResourceAdminServiceClient resourceAdminServiceClient,
                                             PropertiesAdminServiceClient propertiesAdminServiceClient, String path,
                                             String parentPath, List<String> ignorableRegistryResourcesList) {
        try {
            // Fetch resource tree entry bean information using getResourceTreeEntry() admin operation.
            ResourceTreeEntryBean treeEntryBean = resourceAdminServiceClient.getResourceTreeEntryBean(path);

            if (treeEntryBean.getCollection()) {
                if (!parentPath.isEmpty()) {
                    RegistryCollection collection = new RegistryCollection();
                    collection.setDirectory(path);
                    collection.setResourcePath(path);
                    collection.setParentPath(parentPath);
                    String[] resourcePathArray = path.split(MigrationClientUtils.URL_SEPARATOR);
                    String resourceName = resourcePathArray[resourcePathArray.length - 1];
                    collection.setResourceName(resourceName);

                    PropertiesBean properties = propertiesAdminServiceClient.getProperties(path);
                    if (properties.getProperties() != null) {
                        // If properties are defined in this collection, then mark it as an exportable resource element.
                        collection.setProperties(properties.getProperties());
                        collection.updateExportStatus(true);
                    }
                    registryResources.add(collection);
                }

                boolean isChildrenSpecified = treeEntryBean.isChildrenSpecified();
                if (isChildrenSpecified) {
                    String[] children = treeEntryBean.getChildren();
                    for (String child : children) {
                        // Check whether this particular registry resource is ignorable.
                        // If it is, then stop processing that registry resource path further.
                        if (!ignorableRegistryResourcesList.isEmpty() && isRegistryResourceIgnorable(child,
                                                                                                     ignorableRegistryResourcesList)) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Ignoring the resource: {}", child);
                            }
                            continue;
                        }
                        // Recursively call the traverseRegistryTree function.
                        traverseRegistryTree(resourceAdminServiceClient, propertiesAdminServiceClient, child, path,
                                             ignorableRegistryResourcesList);
                    }
                }
            } else {
                RegistryItem registryItem = new RegistryItem();

                // Fetch resource metadata bean using getMetadata() admin operation.
                MetadataBean resourceMataData = resourceAdminServiceClient.getMetadata(path);
                // Fetch resource text content using getTextContent() admin operation.
                String resourceContent = resourceAdminServiceClient.getTextContent(path);
                // Fetch properties associated to a resource using getProperties() admin operation.
                PropertiesBean properties = propertiesAdminServiceClient.getProperties(path);

                registryItem.setResourceContent(resourceContent);
                registryItem.setProperties(properties.getProperties());
                String mediaType = resourceMataData.getMediaType();
                if (mediaType == null) {
                    mediaType = "";
                }
                registryItem.setMediaType(mediaType);
                registryItem.setParentPath(parentPath);
                registryItem.setResourcePath(path);

                String fullResourcePath = resourceMataData.getActiveResourcePath();
                String[] resourcePathArray = fullResourcePath.split(MigrationClientUtils.URL_SEPARATOR);
                String resourceFileName = resourcePathArray[resourcePathArray.length - 1];
                registryItem.setResourceName(resourceFileName);
                registryItem.updateExportStatus(true);

                registryResources.add(registryItem);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Registry resource: " + path + " is added to the list.");
                }
            }
        } catch (RegistryMigrationException e) {
            LOGGER.error("Error in retrieving data of the registry resource: " + path);
        }

    }

    /**
     * Check whether the given registry resource should be ignored or not.
     *
     * @param fullResourcePath               registry resource path
     * @param ignorableRegistryResourcesList predefined list of the registry resources that should be neglected
     * @return true if the given registry resource should be ignored, false otherwise
     */
    private static boolean isRegistryResourceIgnorable(String fullResourcePath,
                                                       List<String> ignorableRegistryResourcesList) {
        boolean isIgnorable = ignorableRegistryResourcesList.contains(fullResourcePath);
        if (isIgnorable) {
            ignorableRegistryResourcesList.remove(fullResourcePath);
        }
        return isIgnorable;
    }

    /**
     * Retrieve value from command-line.
     *
     * @param message    message to be shown to the user
     * @param isPassword whether the input requires a password or not
     * @return the user input as a string
     * @throws RegistryMigrationException when there is no any console.
     */
    private static String getValueFromConsole(String message, boolean isPassword, String defaultValue)
            throws RegistryMigrationException {
        Console console = System.console();
        if (console != null) {
            if (isPassword) {
                char[] password;
                if ((password = console.readPassword("%s", message)) != null) {
                    return String.valueOf(password);
                }
            } else {
                String value = console.readLine("%s", message);
                if (value.isEmpty() && defaultValue != null) {
                    return defaultValue;
                }
                return value;
            }
        }
        throw new RegistryMigrationException("Can't get input...No console");
    }
}
