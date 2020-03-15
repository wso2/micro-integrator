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

package org.wso2.micro.integrator.ntask.coordination.task;

import org.wso2.micro.integrator.ntask.coordination.task.db.connector.CoordinatedTaskDBConnector;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * The layer which connects to the task data base.
 */
public class TaskDataBase {

    /**
     * Connector for the data base.
     */
    private CoordinatedTaskDBConnector coordinatedTaskDBConnector;

    public TaskDataBase(DataSource dataSource) throws SQLException {

        this.coordinatedTaskDBConnector = new CoordinatedTaskDBConnector(dataSource);
    }

    /**
     * Removes the node id of the task update the task state.
     *
     * @param coordinatedTasks - List of coordinated tasks which needs to be updated.
     */
    public void cleanInvalidNodesAndUpdateState(List<CoordinatedTask> coordinatedTasks) throws SQLException {

        coordinatedTaskDBConnector.cleanInvalidNodesAndUpdateState(coordinatedTasks);
    }

    /**
     * For all the tasks which has this destined node id , sets it to null and update the task state to none if it was
     * in a running state.
     *
     * @param nodeId - Node Id.
     */
    public void cleanTasksOfNode(String nodeId) throws SQLException {

        coordinatedTaskDBConnector.cleanTasksOfNode(nodeId);
    }

    /**
     * Retrieves the list of tasks of node specified in specified state.
     *
     * @param nodeID - Id of the node, for which the tasks need to be retrieved.
     * @param state  - State of the tasks which need to be retrieved.
     * @return - List of tasks.
     */
    public List<String> retrieveTasksOfParticularNodeInSpecifiedState(String nodeID, CoordinatedTask.States state)
            throws SQLException {

        return coordinatedTaskDBConnector.retrieveTasksOfParticularNodeInSpecifiedState(nodeID, state);
    }

    /**
     * Removes all the tasks assigned to the node.
     *
     * @param nodeId - The node id.
     */
    public void removeTasksOfNode(String nodeId) throws SQLException {

        coordinatedTaskDBConnector.removeTasksOfNode(nodeId);
    }

    /**
     * Remove the task entry from the db.
     *
     * @param coordinatedTasks - List of tasks to be removed.
     */
    public void removeTasksFromDB(List<String> coordinatedTasks) throws SQLException {

        coordinatedTaskDBConnector.removeTasksFromDB(coordinatedTasks);
    }

    /**
     * Retrieve all the tasks in the task db.
     *
     * @return - List of available tasks.
     */
    public List<String> getAllTasksInDB() throws SQLException {

        return coordinatedTaskDBConnector.getAllTasksInDB();
    }

    /**
     * Retrieve all assigned and in completed tasks in the task db.
     *
     * @return - List of available tasks.
     */
    public List<CoordinatedTask> getAllAssignedInCompleteTasks() throws SQLException {

        return coordinatedTaskDBConnector.getAllAssignedInCompleteTasks();
    }

    /**
     * Add the task to the task db.
     *
     * @param coordinatedTask - The coordinated task which needs to be added.
     */
    public void addTaskToDB(CoordinatedTask coordinatedTask) throws SQLException {

        coordinatedTaskDBConnector.addTaskToDB(coordinatedTask);
    }

    /**
     * Updates the state and node id of the provided tasks in db.
     *
     * @param coordinatedTasks - List of tasks to be updated.
     */
    public void updateTaskDB(List<CoordinatedTask> coordinatedTasks) throws SQLException {

        coordinatedTaskDBConnector.updateTaskDB(coordinatedTasks);
    }

    /**
     * Updates the stat of a task.
     *
     * @param taskName - Name of the task.
     * @param state    - State to be updated.
     */
    public void updateTaskState(String taskName, CoordinatedTask.States state) throws SQLException {

        coordinatedTaskDBConnector.updateTaskState(taskName, state);
    }

    /**
     * Get All unassigned tasks except the completed ones.
     *
     * @return - List of unassigned and in complete tasks.
     */
    public List<CoordinatedTask> getAllUnAssignedNotCompletedTasks() throws SQLException {

        return coordinatedTaskDBConnector.retrieveAllUnAssignedAndIncompleteTasks();
    }

}
