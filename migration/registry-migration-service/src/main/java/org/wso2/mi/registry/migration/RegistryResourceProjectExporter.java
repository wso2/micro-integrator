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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.wso2.mi.registry.migration.exception.ProjectCreationException;
import org.wso2.mi.registry.migration.exception.RegistryMigrationException;
import org.wso2.mi.registry.migration.utils.FileUtils;
import org.wso2.mi.registry.migration.utils.MigrationClientUtils;
import org.wso2.mi.registry.migration.utils.resources.Collection;
import org.wso2.mi.registry.migration.utils.resources.ResourceItem;
import org.wso2.mi.registry.migration.utils.resources.ResourceMetaInfo;
import org.wso2.mi.registry.migration.utils.resources.ResourceProjectArtifact;
import org.wso2.mi.registry.migration.utils.resources.ResourceProjectArtifacts;
import org.wso2.mi.registry.migration.utils.resources.ResourceProperties;
import org.wso2.mi.registry.migration.utils.resources.ResourceProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class RegistryResourceProjectExporter extends RegistryExporter {

    private static final Logger LOGGER = LogManager.getLogger(RegistryResourceProjectExporter.class);

    private String integrationProjectName;
    private String groupId;
    private String artifactId;
    private String version;
    private String exportLocation;
    private String integrationProjectPath;
    private String compositeExporterName;
    private String registryResourceProjectName;

    RegistryResourceProjectExporter(String integrationProjectName, String groupId,
                                    String artifactId, String version, String exportLocation) {
        this.integrationProjectName = integrationProjectName;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.exportLocation = exportLocation.replace(File.separator, MigrationClientUtils.URL_SEPARATOR);
        this.integrationProjectPath = exportLocation + MigrationClientUtils.URL_SEPARATOR + integrationProjectName;
        this.compositeExporterName = integrationProjectName + "CompositeExporter";
        this.registryResourceProjectName = integrationProjectName + "RegistryResources";
        this.summaryTable = new ArrayList<>();
    }

    /**
     * Export the registry as a Registry Resources Project. Create the Integration Project with the CompositeExporter
     * and RegistryResource sub modules.
     *
     * @param registryResources list of the registry resources retrieved from the server
     */
    public void exportRegistry(List<RegistryResource> registryResources) throws RegistryMigrationException {
        try {
            createIntegrationProjectArtifacts();
            createRegistryResourceArtifacts(registryResources);
            createCompositeExporterArtifacts();
            LOGGER.info("Registry Resource Project is successfully created at {}",
                        Paths.get(exportLocation, integrationProjectName));

        } catch (ProjectCreationException e) {
            new File(integrationProjectPath).deleteOnExit();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully deleted the file at {}", Paths.get(integrationProjectPath));
            }

            throw new RegistryMigrationException("Failed to create the Registry Resource Project.", e);
        }
    }

    /**
     * Create artifacts related to Integration Project such as pom.xml and .project files.
     *
     * @throws ProjectCreationException if something goes wrong when creating Integration Project artifacts
     */
    private void createIntegrationProjectArtifacts() throws ProjectCreationException {
        try {
            // Add registry resource and composite modules information.
            List<String> modules = new ArrayList<>();
            modules.add(registryResourceProjectName);
            modules.add(compositeExporterName);

            // Create the project pom file at the project's root.
            createPomFile("/integration-project-pom/integration-project-pom.xml", integrationProjectPath,
                          groupId, artifactId, version, integrationProjectName, integrationProjectName, modules);

            // Create the .project file at the project's root.
            File integrationProjectDescriptionFile = new File(
                    integrationProjectPath + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.PROJECT_FILE);
            OMElement descriptionFile = getResourceAsOMElement(
                    "/integration-project-description/integration-project-description.xml");
            descriptionFile.getFirstChildWithName(new QName("name")).setText(integrationProjectName);

            FileUtils.write(integrationProjectDescriptionFile.getAbsolutePath(), descriptionFile.toString());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully created the Integration Project artifacts at {}",
                             Paths.get(integrationProjectPath));
            }
        } catch (ProjectCreationException | IOException e) {
            throw new ProjectCreationException("Error creating the Integration Project artifacts. " + e.getMessage(),
                                               e);
        }
    }

    /**
     * Create artifacts related to Composite Exporter module such as pom.xml and .project files.
     *
     * @throws ProjectCreationException if something goes wrong when creating the Composite Exporter artifacts
     */
    private void createCompositeExporterArtifacts() throws ProjectCreationException {
        try {
            // Create the project pom file at the composite exporter module's root.
            String compositeExporterDir = integrationProjectPath + MigrationClientUtils.URL_SEPARATOR + compositeExporterName;
            createPomFile("/integration-project-pom/composite-exporter-pom.xml", compositeExporterDir, groupId,
                          compositeExporterName, version, compositeExporterName, compositeExporterName, null);

            // Create the .project file at composite exporter module's root.
            File compositeExporterDescriptionFile = new File(
                    compositeExporterDir + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.PROJECT_FILE);
            OMElement descriptionFile = getResourceAsOMElement(
                    "/integration-project-description/composite-exporter-description.xml");
            descriptionFile.getFirstChildWithName(new QName("name")).setText(compositeExporterName);
            FileUtils.write(compositeExporterDescriptionFile.getAbsolutePath(), descriptionFile.toString());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully created the Composite Exporter at {} ", Paths.get(compositeExporterDir));
            }
        } catch (ProjectCreationException | IOException e) {
            throw new ProjectCreationException("Error creating the Composite Exporter artifacts. " + e.getMessage(), e);
        }
    }

    /**
     * Create artifacts related to Registry Resource module such as pom.xml, .project, .meta files and related registry
     * resources themselves.
     *
     * @throws ProjectCreationException if something goes wrong when creating the Registry Resource artifacts
     */
    private void createRegistryResourceArtifacts(List<RegistryResource> registryResources)
            throws ProjectCreationException {
        try {
            // Create the pom file at registry resource module's root.
            String registryResourceProjectDir =
                    integrationProjectPath + MigrationClientUtils.URL_SEPARATOR + registryResourceProjectName;
            createPomFile("/integration-project-pom/registry-resource-project-pom.xml", registryResourceProjectDir,
                          groupId, registryResourceProjectName, version, registryResourceProjectName,
                          registryResourceProjectName, null);

            // Create the .project file at registry resource module's root.
            File registryResourceProjectDescriptionFile = new File(
                    registryResourceProjectDir + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.PROJECT_FILE);
            OMElement descriptionFile = getResourceAsOMElement(
                    "/integration-project-description/registry-resource-project-description.xml");
            descriptionFile.getFirstChildWithName(new QName("name")).setText(registryResourceProjectName);
            FileUtils.write(registryResourceProjectDescriptionFile.getAbsolutePath(), descriptionFile.toString());

            // Create the .classpath file at registry resource module's root location.
            File registryResourceProjectClasspathFile = new File(
                    registryResourceProjectDir + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.CLASSPATH_FILE);
            OMElement classpathFile = getResourceAsOMElement("/registry_module_classpath.xml");
            FileUtils.write(registryResourceProjectClasspathFile.getAbsolutePath(), classpathFile.toString());

            // Add all resources to the registry resource module.
            addResourcesToProject(registryResourceProjectDir, registryResources);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully created the Registry Resource module at {}",
                             Paths.get(registryResourceProjectDir));
            }
        } catch (ProjectCreationException | IOException e) {
            throw new ProjectCreationException(
                    "Error creating the Registry Resource Project artifacts. " + e.getMessage(), e);
        }
    }

    /**
     * Read the file and create an OMElement.
     *
     * @param resourcePath resource path
     * @return OMElement of the XML resource
     * @throws ProjectCreationException if something goes wrong when reading or parsing the XML stream
     */
    private OMElement getResourceAsOMElement(String resourcePath) throws ProjectCreationException {
        InputStream in = getClass().getResourceAsStream(resourcePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String xmlString = IOUtils.toString(reader);
            return AXIOMUtil.stringToOM(xmlString);
        } catch (IOException e) {
            throw new ProjectCreationException("Error occurred while reading the java resource at " + resourcePath, e);
        } catch (XMLStreamException e) {
            throw new ProjectCreationException("Error occurred while parsing XML stream", e);
        }
    }

    /**
     * Add registry resources into Registry Resource module and save meta data in .meta file.
     *
     * @param registryResourceModulePath path of the Registry Resource module/project
     * @param registryResources          registry resource list
     * @throws ProjectCreationException if something wrong happens when creating the artifact.xml file
     */
    private void addResourcesToProject(String registryResourceModulePath, List<RegistryResource> registryResources)
            throws ProjectCreationException {
        List<ResourceProjectArtifact> artifacts = new ArrayList<>();
        for (RegistryResource registryResource : registryResources) {
            if (!registryResource.canExport()) {
                continue;
            }
            if (registryResource instanceof RegistryCollection) {
                addRegistryCollection((RegistryCollection) registryResource, registryResourceModulePath, artifacts);
            } else {
                addRegistryItem((RegistryItem) registryResource, registryResourceModulePath, artifacts);
            }
        }

        if (!this.summaryTable.isEmpty()) {
            // Generate a summary report including registry resource export details.
            generateSummaryReport(exportLocation, this.summaryTable);
        }

        if (artifacts.isEmpty()) {
            throw new ProjectCreationException("Could not export any resources to the Registry Resource Project.");
        }

        // Create root artifact.xml file.
        ResourceProjectArtifacts resourceProjectArtifacts = new ResourceProjectArtifacts(artifacts);
        String artifactXML = registryResourceModulePath + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.ARTIFACT_XML;
        new File(artifactXML).getParentFile().mkdirs();
        try {
            createXmlFile(resourceProjectArtifacts, artifactXML);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully created the root artifact.xml file.");
            }
        } catch (Exception e) {
            throw new ProjectCreationException("Failed to create root artifact.xml file.", e);
        }
    }

    /**
     * Add a registry collection to the Registry Resource module.
     *
     * @param registryCollection         registry collection
     * @param registryResourceModulePath location of the Registry Resource module
     */
    private void addRegistryCollection(RegistryCollection registryCollection, String registryResourceModulePath,
                                       List<ResourceProjectArtifact> artifacts) {
        String directory = registryCollection.getDirectory();
        String resourceName = registryCollection.getResourceName();
        String resourcePath = registryCollection.getResourcePath();
        String resourceParentPath = registryCollection.getParentPath();

        //create collection inside the registry resource module.
        String collectionFilePath = registryResourceModulePath + MigrationClientUtils.URL_SEPARATOR + directory;
        File collection = new File(collectionFilePath);
        if (!collection.exists() && !collection.mkdirs()) {
            LOGGER.error("Failed to create collection \"" + directory + "\" at " + collection.getAbsolutePath());
            return;
        }
        //create .meta file inside the collection.
        String collectionMetaDataFilePath =
                collectionFilePath + MigrationClientUtils.URL_SEPARATOR + ".meta" + MigrationClientUtils.URL_SEPARATOR + "~.xml";
        File collectionMetaDataFile = new File(collectionMetaDataFilePath);
        collectionMetaDataFile.getParentFile().mkdirs();
        ResourceMetaInfo resourceMetaInfo = new ResourceMetaInfo(resourceName, resourceParentPath, "", "true");

        try {
            createXmlFile(resourceMetaInfo, collectionMetaDataFilePath);
        } catch (Exception e) {
            LOGGER.error(
                    "Error occurred while creating the metadata file at " + collectionMetaDataFile.getAbsolutePath());
        }

        //update the artifact.xml file
        String resourceFQN = registryCollection.getFullQualifiedResourceName();
        List<ResourceProperty> resourcePropertyList = registryCollection.getProperties();

        ResourceProperties resourceProperties = new ResourceProperties(resourcePropertyList);
        Collection resourceCollection = new Collection(directory, resourcePath, resourceProperties);
        ResourceProjectArtifact artifact = new ResourceProjectArtifact(resourceFQN, groupId, version,
                                                                       resourceCollection);
        artifacts.add(artifact);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully created the registry resource at {}", collection.getAbsolutePath());
        }
        // Update the summary report and return the next index
        summaryTable.add(new String[]{resourcePath, MigrationClientUtils.EXPORT_SUCCESS_MESSAGE, "-"});
    }

    /**
     * Add a registry item to the Registry Resource module.
     *
     * @param registryItem               registry item
     * @param registryResourceModulePath location of the Registry Resource module
     */
    private void addRegistryItem(RegistryItem registryItem, String registryResourceModulePath,
                                 List<ResourceProjectArtifact> artifacts) {
        String resourceName = registryItem.getResourceName();
        String resourceParentPath = registryItem.getParentPath();
        String resourcePath = registryItem.getResourcePath();
        String mediaType = registryItem.getMediaType();
        String textContent = registryItem.getResourceContent();
        String resourceFQN = registryItem.getFullQualifiedResourceName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding registry resource {} to the Registry Resource Project.", resourcePath);
        }

        File resourceFile = new File(registryResourceModulePath + resourcePath);
        String resourceMetaFilePath =
                registryResourceModulePath + resourceParentPath + MigrationClientUtils.URL_SEPARATOR + ".meta"
                        + MigrationClientUtils.URL_SEPARATOR + "~" + resourceName + ".xml";
        File resourceMetaFile = new File(resourceMetaFilePath);

        if (resourceFile.exists()) {
            LOGGER.error(
                    "Failed to export the resource {} since a resource with the same name already exists in the "
                            + "Registry Resource Project",
                    resourceFile.getPath());

            // Update the summary report and return the next index
            summaryTable.add(new String[]{resourcePath, MigrationClientUtils.EXPORT_FAILURE_MESSAGE,
                    "A resource with the same name already exists in the Registry Resource Project"});
            return;
        }
        if (!resourceFile.getParentFile().exists() && !resourceFile.getParentFile().mkdirs()) {
            LOGGER.error(
                    "Failed to export the resource {} since some issues occurred while creating the parent directories",
                    resourcePath);

            // Update the summary report and return the next index
            summaryTable.add(new String[]{resourcePath, MigrationClientUtils.EXPORT_FAILURE_MESSAGE,
                    "Unable to create the parent directories"});
            return;
        }
        try {
            // Create the resource file inside the registry resource module.
            FileUtils.write(resourceFile.getAbsolutePath(), textContent);

            // Create the .meta folder and artifact.
            ResourceMetaInfo resourceMetaInfo = new ResourceMetaInfo(resourceName, resourcePath, mediaType,
                                                                     "false");
            createArtifactMetadataFile(resourceMetaFile, resourceMetaInfo);

            // Generate the artifact element to be put in artifact.xml file.
            List<ResourceProperty> resourcePropertyList = registryItem.getProperties();
            ResourceProperties resourceProperties = new ResourceProperties(resourcePropertyList);
            ResourceItem resourceItem = new ResourceItem(resourcePath, resourceParentPath, mediaType,
                                                         resourceProperties);
            ResourceProjectArtifact artifact = new ResourceProjectArtifact(resourceFQN, groupId, version,
                                                                           resourceItem);
            artifacts.add(artifact);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully created the registry resource at {}", resourceFile.getAbsolutePath());
            }

            // Update the summary report and return the next index
            summaryTable.add(new String[]{resourcePath, MigrationClientUtils.EXPORT_SUCCESS_MESSAGE, "-"});
        } catch (Exception e) {
            LOGGER.error("Error in creating the resource artifacts. ", e);

            // Delete resource file on exist.
            resourceFile.deleteOnExit();

            // Delete .metadata folder on exist.
            resourceMetaFile.getParentFile().deleteOnExit();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleted the resource file at {}", resourceFile.getAbsolutePath());
                LOGGER.debug("Deleted the .meta file at {}", resourceMetaFile.getAbsolutePath());
            }

            // Update the summary report and return the next index
            summaryTable.add(new String[]{resourcePath, MigrationClientUtils.EXPORT_FAILURE_MESSAGE, e.getMessage()});
        }
    }

    /**
     * Generate the pom.xml file in the given location.
     *
     * @param sourcePomLocation location of the source pom.xml file
     * @param rootDir           name of the root directory
     * @param groupId           group id
     * @param artifactId        artifact id
     * @param version           version
     * @param name              name
     * @param description       description
     * @param modules           list of modules
     * @throws ProjectCreationException if something goes wrong when parsing the source pom file or writing the pom file
     *                                  to destination location
     */
    private void createPomFile(String sourcePomLocation, String rootDir, String groupId, String artifactId,
                               String version, String name, String description, List<String> modules)
            throws ProjectCreationException {
        File targetPom = new File(rootDir + MigrationClientUtils.URL_SEPARATOR + MigrationClientUtils.POM_XML);
        InputStream in = getClass().getResourceAsStream(sourcePomLocation);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        try {
            MavenProject mavenProject = FileUtils.getMavenProject(reader);
            mavenProject.setGroupId(groupId);
            mavenProject.setArtifactId(artifactId);
            mavenProject.setVersion(version);
            mavenProject.setName(name);
            mavenProject.setDescription(description);
            if (modules != null && !modules.isEmpty()) {
                mavenProject.getModules().addAll(modules);
            }

            FileUtils.saveMavenProject(mavenProject, targetPom);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Successfully created the pom file at {}", targetPom.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new ProjectCreationException(
                    "Error in writing the pom file to destination at " + targetPom.getAbsolutePath());
        } catch (XmlPullParserException e) {
            throw new ProjectCreationException("Error in parsing the pom file at " + sourcePomLocation);
        }
    }

    public void createArtifactMetadataFile(File resourceMetaFile, ResourceMetaInfo resourceMetaInfo) {
        try {
            resourceMetaFile.getParentFile().mkdirs();
            createXmlFile(resourceMetaInfo, resourceMetaFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error(
                    "Error occurred while creating the metadata file at " + resourceMetaFile.getAbsolutePath());
        }
    }
}
