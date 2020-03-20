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

package org.wso2.micro.integrator.ntask.coordination.task.scehduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.coordination.ClusterCoordinator;
import org.wso2.micro.integrator.ntask.common.TaskException;
import org.wso2.micro.integrator.ntask.coordination.TaskCoordinationException;
import org.wso2.micro.integrator.ntask.coordination.task.ClusterCommunicator;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;
import org.wso2.micro.integrator.ntask.coordination.task.resolver.TaskLocationResolver;
import org.wso2.micro.integrator.ntask.coordination.task.store.TaskStore;
import org.wso2.micro.integrator.ntask.coordination.task.store.cleaner.TaskStoreCleaner;
import org.wso2.micro.integrator.ntask.core.impl.standalone.ScheduledTaskManager;
import org.wso2.micro.integrator.ntask.core.internal.DataHolder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Scheduler class, which runs periodically to retrieve all the scheduled tasks assigned to the node and schedule
 * them locally. Also if the running node is leader, it cleans up the task store and resolve the un assigned nodes.
 */
public class CoordinatedTaskScheduler implements Runnable {

    private static final Log LOG = LogFactory.getLog(CoordinatedTaskScheduler.class);

    private DataHolder dataHolder = DataHolder.getInstance();
    private TaskLocationResolver taskLocationResolver;
    private ClusterCoordinator clusterCoordinator = dataHolder.getClusterCoordinator();
    private TaskStore taskStore;
    private TaskStoreCleaner taskStoreCleaner;
    private int resolvingFrequency;
    private int resolveCount = 0;
    private ClusterCommunicator clusterCommunicator;

    public CoordinatedTaskScheduler(TaskStore taskStore, TaskLocationResolver taskLocationResolver,
                                    ClusterCommunicator connector) {
        this(taskStore, taskLocationResolver, connector, null, 1);
    }

    public CoordinatedTaskScheduler(TaskStore taskStore, TaskLocationResolver taskLocationResolver,
                                    ClusterCommunicator connector, TaskStoreCleaner cleaner, int frequency) {
        this.taskStore = taskStore;
        this.taskLocationResolver = taskLocationResolver;
        this.clusterCommunicator = connector;
        this.taskStoreCleaner = cleaner;
        this.resolvingFrequency = frequency;
    }

    @Override
    public void run() {

        try {
            if (clusterCoordinator.isLeader()) {
                // cleaning will run for each n times resolving frequency . ( n = 0,1,2 ... ).
                if (resolveCount % resolvingFrequency == 0) {
                    LOG.debug("This node is leader hence cleaning task store.");
                    taskStoreCleaner.clean();
                    resolveCount = 0;
                }
                LOG.debug("This node is leader hence resolving unassigned tasks.");
                addFailedTasks();
                resolveCount++;
                resolveUnassignedNotCompletedTasksAndUpdateDB();
            } else {
                LOG.debug("This node is not leader. Hence not cleaning task store or resolving un assigned tasks.");
            }
            // schedule all tasks assigned to this node and in state none
            scheduleAllTasksAssignedToThisNode();
        } catch (Throwable throwable) { // catching throwable to prohibit permanent stopping of the executor service.
            LOG.fatal("Unexpected error occurred while trying to schedule tasks.", throwable);
        }
    }

    /**
     * Add failed tasks to the store.
     *
     * @throws TaskCoordinationException - When something goes wrong while retrieving all the tasks from store.
     */
    private void addFailedTasks() throws TaskCoordinationException {

        ScheduledTaskManager taskManager = (ScheduledTaskManager) dataHolder.getTaskManager();
        List<String> failedTasks = taskManager.getAdditionFailedTasks();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Following list of tasks were found in the failed list.");
            failedTasks.forEach(LOG::debug);
        }
        Iterator<String> iter = failedTasks.iterator();
        while (iter.hasNext()) {
            String task = iter.next();
            taskStore.addTaskIfNotExist(task);
            iter.remove();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully added the failed task [" + task + "]");
            }
        }
    }

    /**
     * Schedules all tasks assigned to this node.
     *
     * @throws TaskCoordinationException - When something goes wrong while retrieving all the assigned tasks.
     */
    private void scheduleAllTasksAssignedToThisNode() throws TaskCoordinationException {

        LOG.debug("Retrieving tasks assigned to this node and to be scheduled.");
        List<String> tasksOfThisNode = taskStore.retrieveTaskNames(clusterCoordinator.getThisNodeId(),
                                                                   CoordinatedTask.States.NONE);
        if (tasksOfThisNode.isEmpty()) {
            LOG.debug("No tasks assigned to this node to be scheduled.");
            return;
        }
        ScheduledTaskManager coordinatedTaskManager = (ScheduledTaskManager) dataHolder.getTaskManager();
        List<String> deployedCoordinatedTasks = coordinatedTaskManager.getAllCoordinatedTasksDeployed();
        tasksOfThisNode.forEach(taskName -> {
            if (deployedCoordinatedTasks.contains(taskName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Submitting retrieved task [" + taskName + "] to the task manager.");
                }
                try {
                    coordinatedTaskManager.scheduleCoordinatedTask(taskName);
                } catch (TaskException e) {
                    LOG.error("Exception occurred while scheduling coordinated task : " + taskName, e);
                }
            } else {
                LOG.info("The task [" + taskName + "] retrieved is not a deployed coordinated task "
                                 + "in this node or an invalid entry, hence ignoring it. It will be eventually "
                                 + "cleaned or deployed.");
            }
        });
    }

    /**
     * Resolves the un assigned tasks and update the task store.
     * Synchronized since this will be triggered in leader periodically and upon member addition.
     *
     * @throws TaskCoordinationException when something goes wrong connecting to the store
     */
    public synchronized void resolveUnassignedNotCompletedTasksAndUpdateDB() throws TaskCoordinationException {

        List<String> unAssignedTasks = taskStore.retrieveAllUnAssignedAndIncompleteTasks();
        if (unAssignedTasks.isEmpty()) {
            LOG.debug("No un assigned tasks found.");
            return;
        }
        Map<String, String> tasksToBeUpdated = new HashMap<>();
        unAssignedTasks.forEach(taskName -> {
            String destinedNode = taskLocationResolver.getTaskNodeLocation(clusterCommunicator, taskName);
            if (destinedNode != null) { // can't resolve all of the time
                tasksToBeUpdated.put(taskName, destinedNode);
            }
        });
        taskStore.updateAssignmentAndRunningStateToNone(tasksToBeUpdated);
    }
}
