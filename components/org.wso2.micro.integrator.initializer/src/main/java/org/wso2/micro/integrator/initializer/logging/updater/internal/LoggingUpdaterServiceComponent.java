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

package org.wso2.micro.integrator.initializer.logging.updater.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.wso2.micro.integrator.initializer.logging.updater.LogConfigUpdater;
import org.wso2.micro.integrator.initializer.logging.updater.exception.LoggingUpdaterException;
import org.wso2.micro.integrator.initializer.logging.updater.utils.LoggingUpdaterUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Logging updater service component
 */
@Component(name = "org.wso2.micro.integrator.logging.updater",
        immediate = true,
        property = EventConstants.EVENT_TOPIC + "=" + LoggingUpdaterUtils.PAX_LOGGING_CONFIGURATION_TOPIC)
@SuppressWarnings({ "UnusedDeclaration" })
public class LoggingUpdaterServiceComponent implements EventHandler {

    private static final Log LOG = LogFactory.getLog(LoggingUpdaterServiceComponent.class);

    @Activate
    public void activate(ComponentContext context) {

        try {
            LogConfigUpdater logConfigUpdater = new LogConfigUpdater();
            logConfigUpdater.setLastModifiedTime(LoggingUpdaterUtils.readModifiedTime());
            LoggingUpdaterUtils.backUpConfigs();
            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("LogConfigUpdater-%d").build();
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                    namedThreadFactory);
            DataHolder.getInstance().setScheduledExecutorService(scheduledExecutorService);
            scheduledExecutorService.scheduleAtFixedRate(logConfigUpdater::update, 5000L, 5000L, TimeUnit.MILLISECONDS);
        } catch (LoggingUpdaterException e) {
            LOG.error("Error while Activating LoggingUpdater component", e);
        }
    }

    @Reference(name = "osgi.configadmin.service",
            service = ConfigurationAdmin.class,
            unbind = "unsetConfigAdminService",
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC)
    public void setConfigAdminService(ConfigurationAdmin configurationAdmin) {

        DataHolder.getInstance().setConfigurationAdmin(configurationAdmin);
    }

    public void unsetConfigAdminService(ConfigurationAdmin configurationAdmin) {

        DataHolder.getInstance().setConfigurationAdmin(null);
    }

    @Deactivate
    public void deactivate() {

        ScheduledExecutorService executorService = DataHolder.getInstance().getScheduledExecutorService();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void handleEvent(Event event) {

        if (event.containsProperty(LoggingUpdaterUtils.EXCEPTIONS_PROPERTY)) {
            Object property = event.getProperty(LoggingUpdaterUtils.EXCEPTIONS_PROPERTY);
            Exception exception = (Exception) property;
            try {
                LoggingUpdaterUtils.reloadBackUp(exception);
            } catch (LoggingUpdaterException ex) {
                LOG.fatal("Error while loading from the backup configs", ex);
            }
        } else {
            try {
                LoggingUpdaterUtils.backUpConfigs();
                LOG.info("Logging configurations updated successfully");
            } catch (LoggingUpdaterException e) {
                LOG.error("Error while taking a backup of current logging configs ", e);
            }
        }
    }
}
