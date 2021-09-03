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

package org.wso2.micro.integrator.ntask.coordination.task.store.cleaner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.coordination.ClusterCoordinator;
import org.wso2.micro.integrator.ntask.coordination.TaskCoordinationException;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;
import org.wso2.micro.integrator.ntask.coordination.task.store.TaskStore;
import org.wso2.micro.integrator.ntask.core.impl.standalone.ScheduledTaskManager;
import org.wso2.micro.integrator.ntask.core.internal.DataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class which is responsible for cleaning the task store. This will remove the tasks if they are invalid and
 * will remove the node assignments if they are no more present in the cluster.
 */
public class TaskStoreCleaner {

    private static final Log LOG = LogFactory.getLog(TaskStoreCleaner.class);

    private DataHolder dataHolder = DataHolder.getInstance();
    private ClusterCoordinator clusterCoordinator = dataHolder.getClusterCoordinator();
    private TaskStore taskStore;
    private ScheduledTaskManager taskManager;

    /**
     * Constructor.
     *
     * @param taskStore - Task database.
     */
    public TaskStoreCleaner(ScheduledTaskManager taskManager, TaskStore taskStore) {
        this.taskManager = taskManager;
        this.taskStore = taskStore;
    }

    /**
     * Cleans the task store. Removes the invalid tasks and invalid nodes ( nodes that are no more in cluster ).
     *
     * @throws TaskCoordinationException When something goes wrong while connecting to the store.
     */
    public void clean() throws TaskCoordinationException {

        LOG.debug("Starting task store cleaning.");
        List<CoordinatedTask> allTasks = taskStore.getAllTaskNames();
        List<String> allNodesAvailableInCluster = clusterCoordinator.getAllNodeIds();
        if (allTasks.isEmpty()) {
            LOG.debug("No tasks found in task database.");
            return;
        }
        removeInvalidTasksFromStore(allTasks, allNodesAvailableInCluster);
        validateDestinedNodeAndUpdateStore(allNodesAvailableInCluster);
        LOG.debug("Completed task store cleaning.");
    }

    /**
     * Checks whether the destined node is valid and remove it if it is not.
     *
     * @param allNodesAvailableInCluster - all available nodes in the cluster
     * @throws TaskCoordinationException - When something goes wrong while updating tasks.
     */
    private void validateDestinedNodeAndUpdateStore(List<String> allNodesAvailableInCluster)
            throws TaskCoordinationException {

        List<CoordinatedTask> assignedIncompleteTasks = taskStore.getAllAssignedIncompleteTasks();

        if (allNodesAvailableInCluster.isEmpty()) {
            LOG.warn("No nodes are registered to the cluster successfully yet.");
            return;
        }
        List<String> tasksToBeUpdated = new ArrayList<>();
        assignedIncompleteTasks.forEach(task -> {
            // check whether the node assigned is still valid
            String nodeId = task.getDestinedNodeId();
            if (!allNodesAvailableInCluster.contains(nodeId)) {
                String taskName = task.getTaskName();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The node [" + nodeId + "] of task [" + taskName + "] is not found in cluster"
                                      + ". Hence the node assignment will be removed.");
                }
                tasksToBeUpdated.add(taskName);
            }
        });
        taskStore.unAssignAndUpdateState(tasksToBeUpdated);
    }

    /**
     * From the list of tasks provided removes the invalid tasks entries in the store ( i.e tasks that are not
     * deployed as coordinated task.
     *
     * @param tasksList - The list of tasks to be checked.
     * @param allNodesAvailableInCluster - all available nodes in the cluster
     * @throws TaskCoordinationException - When something goes wrong while updating the store.
     */
    private void removeInvalidTasksFromStore(List<CoordinatedTask> tasksList, List<String> allNodesAvailableInCluster)
            throws TaskCoordinationException {

        List<String> deployedCoordinatedTasks = taskManager.getAllCoordinatedTasksDeployed();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Following list of tasks are found deployed coordinated task list.");
            deployedCoordinatedTasks.forEach(LOG::debug);
        }
        // We first add to list and then to the store  while deploying. So all the tasks retrieved from the store
        // which has valid node ids should be in the list, if not they are invalid entries.
        tasksList.removeIf(task -> allNodesAvailableInCluster.contains(task.getDestinedNodeId()));
        tasksList.removeIf(task -> deployedCoordinatedTasks.contains(task.getTaskName()));
        taskStore.deleteTasks(tasksList.stream().map(CoordinatedTask::getTaskName).collect(Collectors.toList()));
        if (LOG.isDebugEnabled()) {
            tasksList.forEach(removedTask -> LOG.debug("Removed invalid task :" + removedTask));
        }
    }

}
