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
import org.wso2.mi.registry.migration.exception.ArchiveException;
import org.wso2.mi.registry.migration.exception.RegistryMigrationException;
import org.wso2.mi.registry.migration.utils.FileUtils;
import org.wso2.mi.registry.migration.utils.MigrationClientUtils;
import org.wso2.mi.registry.migration.utils.resources.Artifacts;
import org.wso2.mi.registry.migration.utils.resources.Collection;
import org.wso2.mi.registry.migration.utils.resources.Dependency;
import org.wso2.mi.registry.migration.utils.resources.RegistryInfo;
import org.wso2.mi.registry.migration.utils.resources.ResourceArtifact;
import org.wso2.mi.registry.migration.utils.resources.ResourceItem;
import org.wso2.mi.registry.migration.utils.resources.ResourceProperties;
import org.wso2.mi.registry.migration.utils.resources.RootArtifact;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CARExporter extends RegistryExporter {

    private static final Logger LOGGER = LogManager.getLogger(CARExporter.class);

    private String carName;
    private String carVersion;
    private String exportDestination;
    private String archiveDirectory;
    private List<Dependency> dependencies = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param carName           name of the CAR
     * @param carVersion        version of the CAR
     * @param exportDestination export destination of the CAR
     */
    CARExporter(String carName, String carVersion, String exportDestination) {
        this.carName = carName;
        this.carVersion = carVersion;
        this.exportDestination = exportDestination.replace(File.separator, MigrationClientUtils.URL_SEPARATOR);
        this.archiveDirectory = this.exportDestination + MigrationClientUtils.URL_SEPARATOR + carName + "_" + carVersion;
        this.summaryTable = new ArrayList<>();
    }

    /**
     * Export the registry as a CAR file.
     *
     * @param registryResources list of the registry resources retrieved from the server
     */
    @Override
    public void exportRegistry(List<RegistryResource> registryResources)
            throws RegistryMigrationException {
        for (RegistryResource registryResource : registryResources) {
            if (registryResource.canExport()) {
                addRegistryElementToCAR(registryResource);
            }
        }
        if (!this.summaryTable.isEmpty()) {
            // Generate the summary including the export information.
            generateSummaryReport(exportDestination, this.summaryTable);
        }

        if (!dependencies.isEmpty()) {
            try {
                // Create the artifacts.xml at car root.
                createArtifactsXMLFile();
                // Create the .car archive
                FileUtils.zipFolder(Paths.get(archiveDirectory).toString(),
                                    Paths.get(archiveDirectory + ".car").toString());
                LOGGER.info("Carbon Application is successfully generated at {}", Paths.get(archiveDirectory + ".car"));

            } catch (ArchiveException e) {
                throw new RegistryMigrationException("Failed to create the Carbon Application (.car).", e);
            } finally {
                // Delete the archive directory if exists.
                new File(archiveDirectory).deleteOnExit();
            }
        } else {
            // Delete the archive directory if exists.
            new File(archiveDirectory).deleteOnExit();
            throw new RegistryMigrationException("Could not export any resources to the Carbon Application (.car).");
        }
    }

    /**
     * Create dedicated folder for a registry resource and the related artifacts inside this folder. Finally, there will
     * be three files called the registry resource file, artifact.xml, and registry-info.xml inside this folder.
     *
     * @param registryResource path of the registry resource
     */
    private void addRegistryElementToCAR(RegistryResource registryResource) {
        String targetFile = registryResource.getResourceName();
        String resourceFQN = registryResource.getFullQualifiedResourceName();
        String path = registryResource.getResourcePath();

        // Generate per resource folder path inside archive directory
        String resourceFolderPath = archiveDirectory + MigrationClientUtils.URL_SEPARATOR + resourceFQN + "_" + carVersion;
        File resourceFolder = new File(resourceFolderPath);

        // Check whether the resource folder already exists.
        // If the folder exists, the particular registry resource will not be exported.
        if (resourceFolder.exists()) {
            LOGGER.error("{} already exists. Hence, unable to export the registry resource: {}", resourceFolder, path);
            summaryTable.add(new String[]{path, MigrationClientUtils.EXPORT_FAILURE_MESSAGE,
                    resourceFolder + " already exists. Hence, unable to export the registry resource: " + path});
            return;
        }

        // Generate registry resource path inside archive directory.
        String resourceFilePath =
                resourceFolderPath + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.RESOURCES_FOLDER + MigrationClientUtils.URL_SEPARATOR
                        + targetFile;
        try {
            ResourceProperties resourceProperties = new ResourceProperties(registryResource.getProperties());
            RegistryInfo registryInfo;
            if (registryResource instanceof RegistryCollection) {
                // Registry resource is a collection.
                if (!new File(resourceFilePath).mkdirs()) {
                    throw new ArchiveException(
                            "Directory for the collection could not be created at " + resourceFilePath);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Successfully created the collection at {}", resourceFilePath);
                }

                // Create registry-info details.
                Collection collection = new Collection(targetFile, path, resourceProperties);
                registryInfo = new RegistryInfo(collection);
            } else {
                // Registry resource is an item.
                new File(resourceFilePath).getParentFile().mkdirs();

                // Write the resource content to the target file.
                FileUtils.write(resourceFilePath, ((RegistryItem) registryResource).getResourceContent());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Successfully created the registry item at {}", resourceFilePath);
                }

                // Create registry-info details.
                ResourceItem resourceItem = new ResourceItem(targetFile, registryResource.getParentPath(),
                                                             ((RegistryItem) registryResource).getMediaType(),
                                                             resourceProperties);
                registryInfo = new RegistryInfo(resourceItem);
            }

            // Create per registry-info.xml file.
            String registryInfoFilePath = resourceFolderPath + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.REGISTRY_INFO_XML;
            createXmlFile(registryInfo, registryInfoFilePath);

            // Create per artifact.xml file.
            ResourceArtifact artifact = new ResourceArtifact(resourceFQN, carVersion);
            String registryArtifactFilePath = resourceFolderPath + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.ARTIFACT_XML;
            createXmlFile(artifact, registryArtifactFilePath);

            // Create and add the dependency information to be added to the artifacts.xml file.
            Dependency dependency = new Dependency(registryResource.getFullQualifiedResourceName());
            dependencies.add(dependency);

            // Update the summary report
            summaryTable.add(new String[]{path, MigrationClientUtils.EXPORT_SUCCESS_MESSAGE, "-"});

        } catch (Exception e) {
            LOGGER.error("Error in writing the resource artifacts. ", e);

            // Delete the created resource directory
            resourceFolder.deleteOnExit();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleted the resource folder {}", resourceFolder.getPath());
            }

            // Update the summary report
            summaryTable.add(new String[]{path, MigrationClientUtils.EXPORT_FAILURE_MESSAGE, e.getMessage()});
        }
    }

    /**
     * Create the artifacts.xml file.
     *
     * @throws ArchiveException if something goes when creating the artifacts.xml file
     */
    private void createArtifactsXMLFile() throws ArchiveException {
        RootArtifact rootArtifact = new RootArtifact(carName, carVersion, dependencies);
        Artifacts artifacts = new Artifacts(rootArtifact);
        String artifactsFilePath = archiveDirectory + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.ARTIFACTS_XML;

        try {
            createXmlFile(artifacts, artifactsFilePath);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully created the artifacts.xml file at {}", artifactsFilePath);
            }
        } catch (Exception e) {
            throw new ArchiveException("Error occurred while creating the artifacts.xml file", e);
        }
    }
}
