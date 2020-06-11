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

package org.wso2.micro.integrator.initializer.logging.updater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.wso2.micro.integrator.initializer.logging.updater.exception.LoggingUpdaterException;
import org.wso2.micro.integrator.initializer.logging.updater.internal.DataHolder;
import org.wso2.micro.integrator.initializer.logging.updater.utils.LoggingUpdaterUtils;

import java.io.IOException;
import java.nio.file.attribute.FileTime;

/**
 * Logging configuration updater implementation to check and update pax-logging configuration realtime.
 */
public class LogConfigUpdater {

    private static final Log LOG = LogFactory.getLog(LogConfigUpdater.class);

    // Constant on pax logging
    private static final String PAX_LOGGING_CONFIGURATION_PID = "org.ops4j.pax.logging";

    private FileTime lastModifiedTime = null;

    public void update() {

        try {
            FileTime modifiedTime = LoggingUpdaterUtils.readModifiedTime();
            if (lastModifiedTime != null && modifiedTime.compareTo(lastModifiedTime) > 0) {
                updateLoggingConfiguration();
                lastModifiedTime = modifiedTime;
            }
        } catch (LoggingUpdaterException e) {
            LOG.error("Error while reading modified Time", e);
        } catch (IOException e) {
            LOG.error("Error while updating logging configuration", e);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while updating logging configuration", e);
        }
    }

    public void setLastModifiedTime(FileTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * Method to update pax-logging configuration.
     *
     * @throws IOException Exception when reading log4j2 file.
     */
    public static void updateLoggingConfiguration() throws IOException {

        ConfigurationAdmin configurationAdmin = DataHolder.getInstance().getConfigurationAdmin();
        Configuration configuration = configurationAdmin.getConfiguration(PAX_LOGGING_CONFIGURATION_PID, "?");
        configuration.update();
    }
}
