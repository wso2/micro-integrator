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
package org.wso2.micro.integrator.ntask.core.impl.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.task.TaskDescription;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.ntask.common.TaskException;
import org.wso2.micro.integrator.ntask.coordination.TaskCoordinationException;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;
import org.wso2.micro.integrator.ntask.coordination.task.store.TaskStore;
import org.wso2.micro.integrator.ntask.core.TaskInfo;
import org.wso2.micro.integrator.ntask.core.TaskRepository;
import org.wso2.micro.integrator.ntask.core.TaskUtils;
import org.wso2.micro.integrator.ntask.core.impl.AbstractQuartzTaskManager;
import org.wso2.micro.integrator.ntask.core.internal.DataHolder;
import org.wso2.micro.integrator.ntask.core.internal.TasksDSComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for handling / scheduling all tasks in Micro Integrator.
 */
public class ScheduledTaskManager extends AbstractQuartzTaskManager {

    private static Log log = LogFactory.getLog(ScheduledTaskManager.class);
    /**
     * The list which holds the list of coordinated tasks deployed in this node.
     */
    private List<String> deployedCoordinatedTasks = new ArrayList<>();

    /**
     * The list of tasks for which the addition failed.
     */
    private List<String> additionFailedTasks = new ArrayList<>();

    private List<String> deactivationFailedTasks = new ArrayList<>();

    private List<String> activationFailedTasks = new ArrayList<>();

    /**
     * The tasks which were stopped in this node (can be due to pause or node becoming un responsive)
     */
    private List<String> stoppedTasks = new ArrayList<>();
    private SynapseEnvironment synapseEnvironment = null;
    private TaskStore taskStore;

    ScheduledTaskManager(TaskRepository taskRepository, TaskStore taskStore) throws TaskException {

        super(taskRepository, taskStore);
        this.taskStore = taskStore;
    }

    @Override
    public void initStartupTasks() throws TaskException {
        this.scheduleAllTasks();
    }

    private boolean isMyTaskTypeRegistered() {
        return TasksDSComponent.getTaskService().getRegisteredTaskTypes().contains(this.getTaskType());
    }

    /**
     * Handles the task with given name. Schedule if its not coordinated else update the task data base.
     *
     * @param taskName The name of the task
     * @throws TaskException - Exception
     */
    public void handleTask(String taskName) throws TaskException {

        if (isCoordinatedTask(taskName)) {
            if (log.isDebugEnabled()) {
                log.debug("Adding task [" + taskName + "] to the data base since this is a coordinated task.");
            }
            deployedCoordinatedTasks.add(taskName);
            try {
                taskStore.addTaskIfNotExist(taskName);
            } catch (TaskCoordinationException ex) {
                additionFailedTasks.add(taskName);
                throw new TaskException("Error adding task : " + taskName, TaskException.Code.DATABASE_ERROR, ex);
            }
            return;
        }
        scheduleTask(taskName);
    }

    public List<String> getAdditionFailedTasks() {
        return additionFailedTasks;
    }

    /**
     * Checks whether the particular task needs coordination or not.
     *
     * @param taskName - Name of the task.
     * @return - Needs coordination or not.
     */
    private boolean isCoordinatedTask(String taskName) {

        if (isTaskPinned(taskName)) {
            // if task is pinned it shouldn't be coordinated as it belongs to this node.
            return false;
        }
        DataHolder dataHolder = DataHolder.getInstance();
        return dataHolder.isCoordinationEnabledGlobally();
    }

    /**
     * Check whether pinned server is enabled for this task.
     *
     * @param taskName - The name of the task.
     * @return - whether pinned server enabled.
     */
    private boolean isTaskPinned(String taskName) {

        if (synapseEnvironment == null) {
            synapseEnvironment = MicroIntegratorBaseUtils.getSynapseEnvironment();
            if (synapseEnvironment == null) {
                return false;
            }
        }
        TaskDescription taskDescription =
                synapseEnvironment.getTaskManager().getTaskDescriptionRepository().getTaskDescription(taskName);
        if (taskDescription != null) { // would be null for MPs
            List pinnedServers = taskDescription.getPinnedServers();
            if (pinnedServers != null && !pinnedServers.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Pinned server enabled for task " + taskName);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Schedules the coordinated tasks.
     *
     * @param taskName The name of the task to be scheduled.
     * @throws TaskException Exception
     */
    public void scheduleCoordinatedTask(String taskName) throws TaskException {

        if (!stoppedTasks.contains(taskName)) {
            scheduleTask(taskName);
        } else {
            resumeLocalTask(taskName);
        }
        try {
            taskStore.updateTaskState(Collections.singletonList(taskName), CoordinatedTask.States.RUNNING);
        } catch (TaskCoordinationException e) {
            // stopping since the db write failed.
            stopExecution(taskName);
            throw new TaskException(
                    "Exception occurred while updating the state of the task : " + taskName + " to :" + " "
                            + CoordinatedTask.States.RUNNING, TaskException.Code.DATABASE_ERROR);
        }
    }

    public List<String> getAllCoordinatedTasksDeployed() {
        return deployedCoordinatedTasks;
    }

    /**
     * Stops the execution of the task.
     *
     * @param taskName - Name of the task.
     * @throws TaskException - Exception.
     */
    public void stopExecution(String taskName) throws TaskException {
        this.pauseLocalTask(taskName);
        stoppedTasks.add(taskName);
    }

    @Override
    public void scheduleTask(String taskName) throws TaskException {
        if (this.isMyTaskTypeRegistered()) {
            this.scheduleLocalTask(taskName);
        } else {
            throw new TaskException(
                    "Task type: '" + this.getTaskType() + "' is not registered in the current task node",
                    TaskException.Code.TASK_NODE_NOT_AVAILABLE);
        }
    }

    @Override
    public boolean deleteTask(String taskName) throws TaskException {

        boolean result = this.deleteLocalTask(taskName);
        // dis regard of results, if it is a coordinated task we need to remove as it can be in a completed state and
        // result can be false.
        if (deployedCoordinatedTasks.contains(taskName)) {
            try {
                taskStore.deleteTasks(Collections.singletonList(taskName));
            } catch (TaskCoordinationException ex) {
                log.error("Error while removing tasks.", ex);
            }
            deployedCoordinatedTasks.remove(taskName);
            stoppedTasks.remove(taskName);
        }
        return result;
    }

    public void handleTaskPause(String taskName) throws TaskException {

        if (isDeactivated(taskName)) {
            log.info("Task [" + taskName + "] is already in paused state.");
            return;
        }

        if (deployedCoordinatedTasks.contains(taskName)) {
            try {
                taskStore.deactivateTask(taskName);
                log.info("Deactivated task [" + taskName + "]");
            } catch (TaskCoordinationException e) {
                deactivationFailedTasks.add(taskName); // todo
                log.error("Error occurred while deactivating task [" + taskName + "]", e);
            }
            return;
        }
        pauseTask(taskName);
    }

    private void pauseTask(String taskName) throws TaskException {
        this.pauseLocalTask(taskName);
        TaskUtils.setTaskPaused(this.getTaskRepository(), taskName, true);
    }

    @Override
    public void registerTask(TaskInfo taskInfo) throws TaskException {
        this.registerLocalTask(taskInfo);
    }

    @Override
    public boolean isDeactivated(String taskName) throws TaskException {

        if (deployedCoordinatedTasks.contains(taskName)) {
            boolean isDeactivated = false;
            try {
                CoordinatedTask.States state = taskStore.getTaskState(taskName);
                if (Arrays.asList(CoordinatedTask.States.COMPLETED, CoordinatedTask.States.DEACTIVATED,
                                  CoordinatedTask.States.PAUSED).contains(state)) {
                    isDeactivated = true;
                }
            } catch (TaskCoordinationException e) {
                log.error("Error retrieving task [" + taskName + "]", e);
            }
            if (log.isDebugEnabled()) {
                log.debug("Task [" + taskName + "] is " + (isDeactivated ? "" : "not") + " in deactivated state.");
            }
            return isDeactivated;
        }
        return getTaskState(taskName).equals(TaskState.PAUSED);
    }

    @Override
    public TaskState getTaskState(String taskName) throws TaskException {
        return this.getLocalTaskState(taskName);
    }

    @Override
    public TaskInfo getTask(String taskName) throws TaskException {
        return this.getTaskRepository().getTask(taskName);
    }

    @Override
    public List<TaskInfo> getAllTasks() throws TaskException {
        return this.getTaskRepository().getAllTasks();
    }

    @Override
    public void rescheduleTask(String taskName) throws TaskException {
        if (this.isMyTaskTypeRegistered()) {
            this.rescheduleLocalTask(taskName);
        } else {
            throw new TaskException(
                    "Task type: '" + this.getTaskType() + "' is not registered in the current task node",
                    TaskException.Code.TASK_NODE_NOT_AVAILABLE);
        }
    }

    @Override
    public void handleTaskResume(String taskName) throws TaskException {

        if (deployedCoordinatedTasks.contains(taskName) && isDeactivated(taskName)) {
            resumeCoordinatedTask(taskName);
            return;
        }
        resumeTask(taskName);
    }

    private void resumeCoordinatedTask(String taskName) {
        try {
            taskStore.activateTask(taskName);
            log.info("Activated task [" + taskName + "]");
        } catch (TaskCoordinationException e) {
            activationFailedTasks.add(taskName); // todo
            log.error("Error while resuming task [" + taskName + "]", e);
        }
    }

    private void resumeTask(String taskName) throws TaskException {
        this.resumeLocalTask(taskName);
        TaskUtils.setTaskPaused(this.getTaskRepository(), taskName, false);
    }

}
