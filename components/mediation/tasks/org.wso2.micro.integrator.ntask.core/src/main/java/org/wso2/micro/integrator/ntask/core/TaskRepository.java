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
package org.wso2.micro.integrator.ntask.core;

import org.wso2.micro.integrator.ntask.common.TaskException;

import java.util.List;

/**
 * This interface represents a task repository, which can be used to store and load tasks.
 */
public interface TaskRepository {

    /**
     * Returns all the task information data in the repository.
     *
     * @return A list of TaskInfo objects
     * @throws TaskException
     */
    public List<TaskInfo> getAllTasks() throws TaskException;

    /**
     * Returns task information of a given task name
     *
     * @param taskName The task name
     * @return The task information object
     * @throws TaskException if the task cannot be found
     */
    public TaskInfo getTask(String taskName) throws TaskException;

    /**
     * Adds given task information to the repository.
     *
     * @param taskInfo The task information object
     * @throws TaskException
     */
    public void addTask(TaskInfo taskInfo) throws TaskException;

    /**
     * Deletes existing task information from the repository.
     *
     * @param taskName The task name
     * @return true if the task was found and deleted
     * @throws TaskException
     */
    public boolean deleteTask(String taskName) throws TaskException;

    /**
     * Returns the type of the tasks represented by this task manager.
     *
     * @return The type of the tasks
     */
    public String getTasksType();

    /**
     * Returns the tenant domain of the tasks represented by this task manager.
     *
     * @return The tenant domain of the tasks
     */
    public int getTenantId();

    /**
     * Sets a task metadata property to a given task name with a given property key.
     *
     * @param taskName The name of the task the metadata property to be assigned to
     * @param key      The key of the metadata property
     * @param value    The value of the metadata property
     * @throws TaskException
     */
    public void setTaskMetadataProp(String taskName, String key, String value) throws TaskException;

    /**
     * Returns the task metadata property value, if the task does not exist, this will return null.
     *
     * @param taskName The name of the task to retrieve the metadata property from
     * @param key      The key of the metadata propery
     * @return The metadata property value
     * @throws TaskException
     */
    public String getTaskMetadataProp(String taskName, String key) throws TaskException;

}
