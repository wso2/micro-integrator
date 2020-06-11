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

package org.wso2.micro.integrator.initializer.logging.updater.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.initializer.logging.updater.exception.LoggingUpdaterException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class LoggingUpdaterUtils {

    private static final Log LOG = LogFactory.getLog(LoggingUpdaterUtils.class);
    public static final String PAX_LOGGING_CONFIGURATION_TOPIC = "org/ops4j/pax/logging/Configuration";
    public static final String EXCEPTIONS_PROPERTY = "exception";

    private static PropertiesConfigurationLayout layout;

    private LoggingUpdaterUtils() {
        // utils is not to be be initialized
    }

    public static FileTime readModifiedTime() throws LoggingUpdaterException {

        File log4j2File = getLog4j2File();
        try {
            BasicFileAttributes attributes = Files.getFileAttributeView(log4j2File.toPath(),
                                                                        BasicFileAttributeView.class).readAttributes();
            return attributes.lastModifiedTime();
        } catch (IOException e) {
            throw new LoggingUpdaterException("Error while reading log4j2.properties", e);
        }
    }

    private static File getLog4j2File() {

        String carbonConfigDirPath = MicroIntegratorBaseUtils.getCarbonConfigDirPath();
        return new File(carbonConfigDirPath + File.separator + "log4j2.properties");
    }

    public static void backUpConfigs() throws LoggingUpdaterException {

        try {
            layout = new PropertiesConfigurationLayout(new PropertiesConfiguration());
            layout.load(new InputStreamReader(new FileInputStream(getLog4j2File())));
        } catch (FileNotFoundException | ConfigurationException ex) {
            throw new LoggingUpdaterException(ex);
        }
    }

    public static void reloadBackUp(Exception ex) throws LoggingUpdaterException {

        try {
            layout.save(new FileWriter(getLog4j2File(), false));
            LOG.info("Switched backed to previous configs due to " + ex.getMessage());
        } catch (ConfigurationException | IOException e) {
            throw new LoggingUpdaterException(e);
        }
    }

}
