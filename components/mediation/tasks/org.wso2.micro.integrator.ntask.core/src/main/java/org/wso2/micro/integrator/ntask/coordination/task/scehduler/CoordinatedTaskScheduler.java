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
import org.wso2.micro.integrator.ntask.coordination.task.ClusterNodeDetails;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;
import org.wso2.micro.integrator.ntask.coordination.task.TaskDataBase;
import org.wso2.micro.integrator.ntask.coordination.task.db.cleaner.TaskDBCleaner;
import org.wso2.micro.integrator.ntask.coordination.task.resolver.TaskLocationResolver;
import org.wso2.micro.integrator.ntask.core.impl.standalone.ScheduledTaskManager;
import org.wso2.micro.integrator.ntask.core.internal.DataHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduler class, which runs periodically to retrieve all the scheduled tasks assigned to the node and schedule
 * them locally. Also if the running node is leader, it cleans up the task data base and resolve the un assigned nodes.
 */
public class CoordinatedTaskScheduler implements Runnable {

    private static final Log LOG = LogFactory.getLog(CoordinatedTaskScheduler.class);

    private DataHolder dataHolder = DataHolder.getInstance();
    private TaskLocationResolver taskLocationResolver;
    private ClusterCoordinator clusterCoordinator = dataHolder.getClusterCoordinator();
    private TaskDataBase taskDataBase;
    private TaskDBCleaner taskDBCleaner;
    private int resolvingFrequency;
    private int resolveCount = 0;
    private ClusterNodeDetails clusterNodeDetails;

    public CoordinatedTaskScheduler(TaskDataBase taskDataBase, TaskLocationResolver taskLocationResolver,
                                    ClusterNodeDetails connector) {
        this(taskDataBase, taskLocationResolver, connector, null, 1);
    }

    public CoordinatedTaskScheduler(TaskDataBase taskDataBase, TaskLocationResolver taskLocationResolver,
                                    ClusterNodeDetails connector, TaskDBCleaner cleaner, int frequency) {
        this.taskDataBase = taskDataBase;
        this.taskLocationResolver = taskLocationResolver;
        this.clusterNodeDetails = connector;
        this.taskDBCleaner = cleaner;
        this.resolvingFrequency = frequency;
    }

    @Override
    public void run() {

        try {
            if (clusterCoordinator.isLeader()) {
                // cleaning will run for each n times resolving frequency . ( n = 0,1,2 ... ).
                if (resolveCount % resolvingFrequency == 0) {
                    LOG.debug("This node is leader hence cleaning task database.");
                    taskDBCleaner.clean();
                    resolveCount = 0;
                }
                LOG.debug("This node is leader hence resolving unassigned tasks.");
                resolveCount++;
                resolveUnassignedNotCompletedTasksAndUpdateDB();
            } else {
                LOG.debug("This node is not leader. Hence not cleaning data base or resolving un assigned tasks.");
            }
            // schedule all tasks assigned to this node and in state none
            scheduleAllTasksAssignedToThisNode();
        } catch (Throwable throwable) { // catching throwable to prohibit permanent stopping of the executor service.
            LOG.fatal("Unexpected error occurred while trying to schedule tasks.", throwable);
        }
    }

    /**
     * Schedules all tasks assigned to this node.
     *
     * @throws SQLException - When something goes wrong while retrieving all the assigned tasks from data base.
     */
    private void scheduleAllTasksAssignedToThisNode() throws SQLException {

        LOG.debug("Retrieving tasks assigned to this node and to be scheduled.");
        List<String> tasksOfThisNode = taskDataBase.retrieveTasksOfParticularNodeInSpecifiedState(
                clusterCoordinator.getThisNodeId(), CoordinatedTask.States.NONE);
        if (tasksOfThisNode.isEmpty()) {
            LOG.debug("No tasks assigned to this node to be scheduled.");
            return;
        }
        ScheduledTaskManager coordinatedTaskManager = (ScheduledTaskManager) dataHolder.getTaskManager();
        List<String> deployedCoordinatedTasks = coordinatedTaskManager.getAllCoordinatedTasksDeployed();
        for (String taskName : tasksOfThisNode) {
            if (deployedCoordinatedTasks.contains(taskName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Submitting task retrieved from data base [" + taskName + "] to the task manager.");
                }
                try {
                    coordinatedTaskManager.scheduleCoordinatedTask(taskName);
                } catch (TaskException e) {
                    LOG.error("Exception occurred while scheduling coordinated task : " + taskName, e);
                }
            } else {
                LOG.info("The task [" + taskName + "] retrieved from data base is not a deployed coordinated task "
                                 + "in this node or an invalid entry, hence ignoring it. It will be eventually cleaned.");
            }
        }
    }

    /**
     * Resolves the un assigned tasks and update the task database.
     * Synchronized since this will be triggered in leader periodically and upon member addition.
     *
     * @throws SQLException when something goes wrong connecting to the data base
     */
    public synchronized void resolveUnassignedNotCompletedTasksAndUpdateDB() throws SQLException {

        List<CoordinatedTask> unAssignedTasks = taskDataBase.getAllUnAssignedNotCompletedTasks();
        if (unAssignedTasks.isEmpty()) {
            LOG.debug("No un assigned tasks found.");
            return;
        }
        List<CoordinatedTask> taskToBeUpdated = new ArrayList<>();
        for (CoordinatedTask task : unAssignedTasks) {
            String taskNode = taskLocationResolver.getTaskNodeLocation(clusterNodeDetails, task.getTaskName());
            if (taskNode != null) { // can't resolve all of the time
                CoordinatedTask.States taskState = task.getTaskState();
                if (CoordinatedTask.States.RUNNING.equals(taskState)) {
                    taskState = CoordinatedTask.States.NONE;
                }
                taskToBeUpdated.add(new CoordinatedTask(task.getTaskName(), taskNode, taskState));
            }
        }
        taskDataBase.updateTaskDB(taskToBeUpdated);
    }
}
