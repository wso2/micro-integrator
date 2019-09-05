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
package org.wso2.micro.integrator.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.service.TaskManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * ServiceHandler  for the TaskManagement service
 */
public class TaskManagementServiceHandler extends ServiceHanlder {

    private static final Log log = LogFactory.getLog(TaskManager.class);

    public TaskManagementServiceHandler() {
        super();
    }

    /**
     * Adding a TaskDescription
     * Delegates adding responsibility to underlying  each 'TaskManagementService' service
     *
     * @param taskDescription TaskDescription instance
     * @param className       Name of the class of  TaskManagementService implementer
     */
    public void addTaskDescription(TaskDescription taskDescription, String className) {

        TaskManagementService managementService = getTaskManagementServiceImplementer(className);
        if (managementService != null) {
            managementService.addTaskDescription(taskDescription);
        }
    }

    /**
     * Deleting a TaskDescription
     * Delegates deleting responsibility to each 'TaskManagementService' service
     *
     * @param name      Name of the TaskDescription instance  to be deleted
     * @param className Name of the class of  TaskManagementService implementer
     */
    public void deleteTaskDescription(String name, String className) {

        TaskManagementService managementService = getTaskManagementServiceImplementer(className);
        if (managementService != null) {
            managementService.deleteTaskDescription(name);
        }
    }

    /**
     * Editing a TaskDescription
     * Delegates Editing responsibility to each 'TaskManagementService' service
     *
     * @param taskDescription TaskDescription instance
     * @param className       Name of the class of  TaskManagementService implementer
     */
    public void editTaskDescription(TaskDescription taskDescription, String className) {

        TaskManagementService managementService = getTaskManagementServiceImplementer(className);
        if (managementService != null) {
            managementService.editTaskDescription(taskDescription);
        }
    }

    /**
     * Looking up a TaskDescription
     * Delegates Looking up responsibility to each 'TaskManagementService' service
     *
     * @param name      Name of the TaskDescription to be returned
     * @param className Name of the class of  TaskManagementService implementer
     * @return taskDescription TaskDescription instance
     */
    public TaskDescription getTaskDescription(String name, String className) {

        TaskManagementService managementService = getTaskManagementServiceImplementer(className);
        if (managementService != null) {
            return managementService.getTaskDescription(name);
        }
        return null;
    }

    /**
     * Explicit check for a TaskDescription with a particular name
     * Delegates responsibility to each 'TaskManagementService' service until any one find a TaskDescription
     * with the given name
     *
     * @param name      Name of the TaskDescription
     * @param className Name of the class of  TaskManagementService implementer
     * @return Ture if there is a task with given name
     */
    public boolean isContains(String name, String className) {

        TaskManagementService managementService = getTaskManagementServiceImplementer(className);
        return managementService != null && managementService.isContains(name);
    }

    public List<String> getPropertyNames(String taskClass, String className) {


        TaskManagementService managementService = getTaskManagementServiceImplementer(className);
        if (managementService != null) {
            return managementService.getPropertyNames(taskClass);
        }

        if (log.isDebugEnabled()) {
            log.debug("Cannot find a property name list of class : " + taskClass);
        }

        return new ArrayList<String>();
    }

    public TaskManagementService getTaskManagementServiceImplementer(String className) {

        final List<Object> services = getServices();
        if (!assertEmpty(services)) {

            for (Object serviceObject : services) {

                if (serviceObject instanceof TaskManagementService) {
                    String name = serviceObject.getClass().getName();
                    if (name.equals(className)) {
                        return (TaskManagementService) serviceObject;
                    }
                }
            }
        }
        return null;
    }

}
