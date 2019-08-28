/*
 * Copyright (c)2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class RegistryMigrator {

    private static final Logger logger = Logger.getLogger(RegistryMigrator.class.getName());
    private static final String DEFAULT_MEDIA_TYPE = "text/plain";
    private static final String METADATA_DIR_NAME = ".metadata";
    private static final String METADATA_FILE_SUFFIX = ".meta";
    private static final String METADATA_KEY_MEDIA_TYPE = "mediaType";
    private static Map<String, String> fileExtensionMediaTypeMap;

    public static void main(String[] args) {

        if (args.length < 1) {
            logger.severe("Error: file path to MI HOME is not provided as an argument");
            return;
        }
        File miHome = new File(args[0]);
        if (!miHome.exists() || !miHome.isDirectory()) {
            logger.severe("Error: File path provided for MI HOME is incorrect");
            return;
        }
        File registryRoot = new File(miHome, "registry");
        if (!miHome.exists() || !miHome.isDirectory()) {
            logger.severe("Error: Cannot find registry");
            return;
        }

        fileExtensionMediaTypeMap = createFileExtensionsMap();

        try (Stream<Path> paths = Files.walk(Paths.get(registryRoot.getPath()))) {
            paths.filter(RegistryMigrator::doFilter).forEach(RegistryMigrator::writeMetadata);
        } catch (IOException e) {
            logger.severe("Error occurred walking in registry: " + e);
        }
    }

    /**
     * Identifies which files should be processed as registry resources.
     *
     * @param path path of file
     * @return whether the file should be processed as a registry resource or not
     */
    private static boolean doFilter(Path path) {

        return !path.toFile().isDirectory()
               && !path.getParent().getFileName().toString().equals(METADATA_DIR_NAME)
               && !path.getFileName().toString().equals(".DS_Store");
    }

    /**
     * Writes metadata for the given registry resource.
     *
     * @param path path of registry resource
     */
    private static void writeMetadata(Path path) {

        Properties metadata = new Properties();
        metadata.setProperty(METADATA_KEY_MEDIA_TYPE, lookUpFileMediaType(path.toString()));

        File metadataDir = new File(path.getParent().toFile(), METADATA_DIR_NAME);
        if (!metadataDir.exists() && !metadataDir.mkdirs()) {
            logger.severe("Unable to create metadata directory: " + metadataDir.getPath());
            return;
        }
        File newMetadataFile = new File(metadataDir, path.getFileName() + METADATA_FILE_SUFFIX);
        try (BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(newMetadataFile))) {
            metadata.store(metadataWriter, null);
            logger.info("Successfully written metadata to file: " + newMetadataFile.getPath());
        } catch (IOException e) {
            logger.severe("Couldn't write to metadata file: " + newMetadataFile.getPath() + " Error: " + e);
        }
    }

    /**
     * Fetch the media type of the given resource using file extension of the resource.
     *
     * @param fileUrl file url of the registry resource
     * @return media type
     */
    private static String lookUpFileMediaType(String fileUrl) {

        String extension = "";
        int indexOfExtensionDot = fileUrl.lastIndexOf('.');
        if (indexOfExtensionDot > 0) {
            extension = fileUrl.substring(indexOfExtensionDot);
        }
        String mediaType = DEFAULT_MEDIA_TYPE;
        if (fileExtensionMediaTypeMap.containsKey(extension)) {
            mediaType = fileExtensionMediaTypeMap.get(extension);
        }
        return mediaType;
    }

    /**
     * Populate file extension to media type mapping.
     *
     * @return Map which contains file extension to media type mapping
     */
    private static Map<String, String> createFileExtensionsMap() {

        Map<String, String> map = new HashMap<>();

        map.put(".xml", "application/xml");
        map.put(".js", "application/javascript");
        map.put(".css", "text/css");
        map.put(".html", "text/html");
        map.put(".sql", "text/plain");
        map.put(".xsd", "Schema");
        map.put(".xsl", "application/xsl+xml");
        map.put(".xslt", "application/xslt+xml");
        map.put(".zip", "application/zip");
        map.put(".wsdl", "WSDL");
        map.put(".xqy", "XQuery");

        return map;
    }
}
