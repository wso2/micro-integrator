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
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.wso2.mi.registry.migration.exception.RegistryMigrationException;
import org.wso2.mi.registry.migration.utils.DataTable;
import org.wso2.mi.registry.migration.utils.MigrationClientUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

abstract class RegistryExporter {

    private static final Logger LOGGER = LogManager.getLogger(RegistryExporter.class);

    List<String[]> summaryTable;

    abstract void exportRegistry(List<RegistryResource> registryResources)
            throws RegistryMigrationException;

    /**
     * Generate the summary report including details to exported registry resources.
     *
     * @param targetLocation target file location of the summary report
     * @param summaryTable data
     */
    void generateSummaryReport(String targetLocation, List<String[]> summaryTable) {
        String outputFileName = "registry_export_summary_" + new Date().getTime() + ".txt";
        File file = new File(targetLocation + File.separator + outputFileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.append("\n");
            writer.append("                                  REGISTRY RESOURCE EXPORT SUMMARY");
            writer.append("\n");
            writer.append(DataTable.getTable(summaryTable));
            writer.append("\n");
            LOGGER.info("\nSummary report is available at {}", file.getPath());
        } catch (IOException e) {
            LOGGER.error("Could not generate the summary report for registry resource export!");
        }
    }

    /**
     * Serialize an object as an Xml file.
     *
     * @param content  Xml content to be written
     * @param filePath target file path
     * @throws Exception if something goes wrong when writing the xml content to the target location
     */
    void createXmlFile(Object content, String filePath) throws Exception {
        File file = new File(filePath);
        Serializer serializer = new Persister(new Format(MigrationClientUtils.XML_DECLARATION));
        serializer.write(content, file);
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("Successfully created the file at {}", filePath);
        }
    }
}
