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

package org.wso2.micro.integrator.mediation.ntask.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.integrator.ntask.core.service.TaskService;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;
import org.wso2.micro.integrator.mediation.ntask.TaskServiceObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
@Component(
        name = "org.wso2.micro.integrator.mediation.ntask.internal.NtaskService",
        immediate = true)
public class NtaskService {

    private static final Log logger = LogFactory.getLog(NtaskService.class);

    private static final List<TaskServiceObserver> observers = new ArrayList<TaskServiceObserver>();

    private static TaskService taskService;

    private static Axis2ConfigurationContextService ccServiceInstance;

    @Activate
    protected void activate(ComponentContext context) {

        try {

            logger.debug("ntask-integration bundle is activated.");
        } catch (Throwable e) {
            logger.error("Could not activate NTaskService. Error: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        logger.debug("ntask-integration bundle is deactivated.");
    }

    @Reference(
            name = "tasks.component",
            service = TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {

        if (logger.isDebugEnabled()) {
            logger.debug("Setting the Task Service [" + taskService + "].");
        }
        NtaskService.taskService = taskService;
        updateAndCleanupObservers();
    }

    protected void unsetTaskService(TaskService taskService) {

        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting the Task Service [" + taskService + "]");
        }
        NtaskService.taskService = null;
    }

    @Reference(
            name = "config.context.service",
            service = Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(Axis2ConfigurationContextService contextService) {

        if (logger.isDebugEnabled()) {
            logger.debug("Setting Configuration Context Service [" + contextService + "]");
        }
        NtaskService.ccServiceInstance = contextService;
        updateAndCleanupObservers();
    }

    private void updateAndCleanupObservers() {

        Iterator<TaskServiceObserver> i = observers.iterator();
        while (i.hasNext()) {
            TaskServiceObserver observer = i.next();
            if (observer.update(null)) {
                i.remove();
            }
        }
    }

    protected void unsetConfigurationContextService(Axis2ConfigurationContextService contextService) {

        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting Configuration Context Service [" + contextService + "]");
        }
        NtaskService.ccServiceInstance = null;
    }

    public static Axis2ConfigurationContextService getCcServiceInstance() {

        return NtaskService.ccServiceInstance;
    }

    public static void addObserver(TaskServiceObserver o) {

        if (observers.contains(o)) {
            return;
        }
        observers.add(o);
    }

    public static TaskService getTaskService() {

        return NtaskService.taskService;
    }

}

