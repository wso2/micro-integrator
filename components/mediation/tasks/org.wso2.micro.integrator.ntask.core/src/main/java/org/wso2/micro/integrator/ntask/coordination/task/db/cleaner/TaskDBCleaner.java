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

package org.wso2.micro.integrator.ntask.coordination.task.db.cleaner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.coordination.ClusterCoordinator;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;
import org.wso2.micro.integrator.ntask.coordination.task.TaskDataBase;
import org.wso2.micro.integrator.ntask.core.impl.standalone.ScheduledTaskManager;
import org.wso2.micro.integrator.ntask.core.internal.DataHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class which is responsible for cleaning the task data base. This will remove the tasks if they are invalid and
 * will remove the node assignments if they are no more present in the cluster.
 */
public class TaskDBCleaner {

    private static final Log LOG = LogFactory.getLog(TaskDBCleaner.class);

    private DataHolder dataHolder = DataHolder.getInstance();
    private ClusterCoordinator clusterCoordinator = dataHolder.getClusterCoordinator();
    private TaskDataBase taskDataBase;

    /**
     * Constructor.
     *
     * @param taskDataBase - Task database.
     */
    public TaskDBCleaner(TaskDataBase taskDataBase) {
        this.taskDataBase = taskDataBase;
    }

    /**
     * Cleans the task db. Removes the invalid tasks and invalid nodes ( nodes that are no more in cluster ).
     *
     * @throws SQLException When something goes wrong while connecting to the data base.
     */
    public void clean() throws SQLException {

        LOG.debug("Starting task db cleaning.");
        List<String> allTasks = taskDataBase.getAllTasksInDB();
        if (allTasks.isEmpty()) {
            LOG.debug("No tasks found in task database.");
            return;
        }
        removeInvalidTasksFromDB(allTasks);
        validateDestinedNodeAndUpdateDB();
        LOG.debug("Completed task db cleaning.");
    }

    /**
     * Checks whether the destined node is valid ( i.e it is still present in the cluster) and remove it if it is not.
     *
     * @throws SQLException - When something goes connecting to the data base.
     */
    private void validateDestinedNodeAndUpdateDB() throws SQLException {

        List<CoordinatedTask> assignedIncompleteTasks = taskDataBase.getAllAssignedInCompleteTasks();
        List<CoordinatedTask> taskNodesToBeCleaned = new ArrayList<>();
        List<String> allNodesAvailableInCluster = clusterCoordinator.getAllNodeIds();
        if (allNodesAvailableInCluster.isEmpty()) {
            LOG.warn("No nodes are registered to the cluster successfully yet.");
            return;
        }
        for (CoordinatedTask task : assignedIncompleteTasks) {
            // check whether the node assigned is still valid
            String nodeId = task.getDestinedNodeId();
            if (!allNodesAvailableInCluster.contains(nodeId)) {
                // changing running task to none since node is invalid
                CoordinatedTask.States state = task.getTaskState();
                if (CoordinatedTask.States.RUNNING.equals(state)) {
                    task.setTaskState(CoordinatedTask.States.NONE);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The node [" + nodeId + "] of task [" + task.getTaskName() + "] is not found in cluster"
                                      + ". Hence the node assignment will be removed and the state will be set to ["
                                      + state + "].");
                }
                taskNodesToBeCleaned.add(task);
            }
        }
        taskDataBase.cleanInvalidNodesAndUpdateState(taskNodesToBeCleaned);
    }

    /**
     * From the list of tasks provided removes the invalid tasks entries in the data base ( i.e tasks that are not
     * deployed as coordinated task.
     *
     * @param tasksList - The list of tasks to be checked.
     * @throws SQLException - When something goes wrong while updating the task data base.
     */
    private void removeInvalidTasksFromDB(List<String> tasksList) throws SQLException {

        ScheduledTaskManager taskManager = (ScheduledTaskManager) dataHolder.getTaskManager();
        if (taskManager == null) {
            LOG.info("Task manager is not yet initialized, hence cancelling removing invalid tasks.");
            return;
        }
        List<String> deployedCoordinatedTasks = taskManager.getAllCoordinatedTasksDeployed();
        if (LOG.isDebugEnabled()) {
            for (String deployedCoordinatedTask : deployedCoordinatedTasks) {
                LOG.debug("List of deployed coordinated tasks [" + deployedCoordinatedTask + "].");
            }
        }
        // we first add to list and then to db while deploying. So all the tasks retrieved from the db should be in
        // the list, if not they are invalid entries.
        tasksList.removeAll(deployedCoordinatedTasks);
        taskDataBase.removeTasksFromDB(tasksList);
        if (LOG.isDebugEnabled()) {
            for (String removedTask : tasksList) {
                LOG.debug("Removed invalid task :" + removedTask);
            }
        }
    }

}
