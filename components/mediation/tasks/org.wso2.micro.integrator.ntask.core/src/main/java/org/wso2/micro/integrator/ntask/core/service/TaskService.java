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
package org.wso2.micro.integrator.ntask.core.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.micro.integrator.ntask.core.TaskManager;

/**
 * This interface represents the task OSGi service.
 */
public interface TaskService {

    /**
     * Returns a task manager for the current tenant's task type. 
     * @param taskType The type of the tasks, e.g. DSS, ESB, MS
     * @return The created / looked-up task manager
     * @throws TaskException	 
     */
    public org.wso2.micro.integrator.ntask.core.TaskManager getTaskManager(String taskType) throws TaskException;

    /**
     * Returns all the tenant task managers for a specific task type
     * @param taskType The task type
     * @return The list of task managers
     * @throws TaskException
     */
    public List<TaskManager> getAllTenantTaskManagersForType(String taskType) throws TaskException;

    /**
     * This method registers a task type in the server,
     * this must be done for the task managers for the current tenant
     * to be started up immediately.
     * @param taskType The task type
     * @throws TaskException 
     */
    public void registerTaskType(String taskType) throws TaskException;

    /**
     * Retrieves all the registered task types.
     * @return The task types
     */
    public Set<String> getRegisteredTaskTypes();

    /**
     * Notifies the task service implementation that the server is fully initialized.
     */
    public void serverInitialized();
    
    /**
     * Checks whether the task server is full initialized.
     * @return true if initialized
     */
    public boolean isServerInit();

    /**
     * Returns the current task server configuration.
     * @return The task server configuration
     */
    public TaskServiceConfiguration getServerConfiguration();
    
    /**
     * This is called after the current task service is registered.
     */
    public void runAfterRegistrationActions() throws TaskException;

    public static interface TaskServiceConfiguration {

        TaskServerMode getTaskServerMode();

        int getTaskServerCount();

        String getTaskClientDispatchAddress();

        String getRemoteServerAddress();

        String getRemoteServerUsername();

        String getRemoteServerPassword();

        String getLocationResolverClass();

        String getTaskRepositoryClass();
        
        Map<String, String> getLocationResolverProperties();

    }

    public static enum TaskServerMode {
        STANDALONE, CLUSTERED, REMOTE, AUTO
    }

}
