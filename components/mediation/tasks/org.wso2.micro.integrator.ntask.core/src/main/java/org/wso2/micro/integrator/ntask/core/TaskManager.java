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

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.micro.integrator.ntask.core.impl.LocalTaskActionListener;

import java.util.List;

/**
 * This interface represents the task manager functionalities.
 */
public interface TaskManager {

    /**
     * Initialize the startup tasks.
     * @throws TaskException
     */
    public void initStartupTasks() throws TaskException;

    /**
     * Starts a task with the given name.
     * @param taskName The name of the task
     * @throws TaskException
     */
    public void scheduleTask(String taskName) throws TaskException;

    /**
     * Reschedules a task with the given name, only the trigger information will be updated in the
     * reschedule. 
     * @param taskName The task to be rescheduled
     * @throws TaskException
     */
    public void rescheduleTask(String taskName) throws TaskException;

    /**
     * Stops and deletes a task with the given name.
     * @param taskName The name of the task
     * @return true if the task was found and deleted
     * @throws TaskException
     */
    public boolean deleteTask(String taskName) throws TaskException;

    /**
     * Pauses a task with the given name.
     * @param taskName The name of the task
     * @throws TaskException
     */
    public void pauseTask(String taskName) throws TaskException;

    /**
     * Resumes a paused task with the given name.
     * @param taskName The name of the task
     * @throws TaskException
     */
    public void resumeTask(String taskName) throws TaskException;

    /**
     * Registers a new task or updates if one already exists.
     * @param taskInfo The task information 
     * @throws TaskException
     */
    public void registerTask(org.wso2.micro.integrator.ntask.core.TaskInfo taskInfo) throws TaskException;

    /**
     * Gets tasks state information
     * @param taskName The name of the task
     * @return State of the task
     * @throws TaskException
     */
    public TaskState getTaskState(String taskName) throws TaskException;

    /**
     * Get task information.
     * @param taskName The name of the task
     * @return The task information 
     * @throws TaskException if the task cannot be found
     */
    public org.wso2.micro.integrator.ntask.core.TaskInfo getTask(String taskName) throws TaskException;

    /**
     * Get all task information.
     * @return Task information list
     * @throws TaskException
     */
    public List<TaskInfo> getAllTasks() throws TaskException;

    /**
     * Checks if the given task is already scheduled.
     * @param taskName The task name
     * @return true if already scheduled
     * @throws TaskException
     */
    public boolean isTaskScheduled(String taskName) throws TaskException;

    /**
     * Task states.
     */
    public enum TaskState {
        NORMAL, PAUSED, ERROR, FINISHED, NONE, BLOCKED, UNKNOWN
    }

    /**
     * Registers a listener to be notified when an action is performed on a task.
     *
     * @param listener the listener to be notified
     * @param taskName the name of the task for which the listener should be registered
     */
    void registerLocalTaskActionListener(LocalTaskActionListener listener, String taskName);

}
