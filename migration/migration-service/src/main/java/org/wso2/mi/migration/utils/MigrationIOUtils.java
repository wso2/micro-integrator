/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mi.migration.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.mi.migration.migrate.MigrationClientException;
import org.wso2.mi.migration.migrate.MigrationConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MigrationIOUtils {

    private static final Log log = LogFactory.getLog(MigrationIOUtils.class);

    private static final String carbonHome = System.getProperty(MigrationConstants.CARBON_HOME);

    public static void createMigrationDirectoryIfNotExists() {

        String migrationDirPath = Paths.get(carbonHome, "migration").toString();
        File migrationDir = new File(migrationDirPath);

        if (!migrationDir.exists()) {
            log.warn("Migration directory does not exists in path " + migrationDirPath);

            if (migrationDir.mkdir()) {
                log.info("Created directory " + migrationDir.getAbsolutePath());
            } else {
                log.error("Could not create directory " + migrationDirPath + "." +
                        " Please create the directory manually and re-run the service");
            }
        }
    }

    /**
     * Returns content of a properties file as a Map.
     *
     * @param path to properties file
     * @return Map
     */
    public static Map<String, String> getProperties(String path) {

        HashMap<String, String> map = new HashMap<>();
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException e) {
            log.error("Error while reading " + path, e);
        }
        for (final String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }
        return map;
    }

    public static void writePropertiesFile(String fileName, Map<String, String> propertyMap) {

        String filePath = Paths.get(carbonHome, MigrationConstants.MIGRATION_DIR, fileName).toString();
        Properties properties = new Properties();
        propertyMap.forEach((alias, decryptedValue) -> {
            properties.setProperty(alias, decryptedValue);
        });

        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            log.error("Error while writing file " + filePath, e);
            throw new MigrationClientException("Error while writing file " + filePath, e);
        }
    }
}
