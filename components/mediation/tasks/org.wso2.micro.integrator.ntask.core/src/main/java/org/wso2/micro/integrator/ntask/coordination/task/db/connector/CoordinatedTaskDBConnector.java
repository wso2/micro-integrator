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

package org.wso2.micro.integrator.ntask.coordination.task.db.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.ntask.coordination.task.ConstantUtil;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinateTaskRunTimeException;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;
import org.wso2.micro.integrator.ntask.coordination.task.db.TaskQueryHelper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * The connector class which deals with underlying coordinated task table.
 */
public class CoordinatedTaskDBConnector {

    private static final Log LOG = LogFactory.getLog(CoordinatedTaskDBConnector.class);
    private DataSource dataSource;

    /**
     * Constructor.
     *
     * @param dataSource - The datasource config to initiate the connection.
     * @throws SQLException - Exception.
     */
    public CoordinatedTaskDBConnector(DataSource dataSource) throws SQLException {

        this.dataSource = dataSource;
        try (Connection connection = getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseType = metaData.getDatabaseProductName();
            if (!"MySQL".equals(databaseType)) {
                throw new CoordinateTaskRunTimeException(
                        "Not supported data base type found : " + databaseType + " . Only MySql is supported.");
            }
        }
    }

    /**
     * Removes the node id of the task and update the task state.
     *
     * @param coordinatedTasks - List of coordinated tasks which needs to be updated.
     */
    public void cleanInvalidNodesAndUpdateState(List<CoordinatedTask> coordinatedTasks) throws SQLException {

        if (coordinatedTasks.isEmpty()) {
            return;
        }
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.CLEAN_INVALID_NODES)) {
            for (CoordinatedTask task : coordinatedTasks) {
                String taskName = task.getTaskName();
                preparedStatement.setString(1, task.getTaskState().name());
                preparedStatement.setString(2, taskName);
                preparedStatement.addBatch();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing the node assignment of task [" + taskName + "].");
                }
            }
            preparedStatement.executeBatch();
        }
    }

    /**
     * For all the tasks which has this destined node id , sets it to null and update the task state to none if it was
     * in a running state.
     *
     * @param nodeId - Node Id.
     */
    public void cleanTasksOfNode(String nodeId) throws SQLException {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.CLEAN_TASKS_OF_NODE)) {
            preparedStatement.setString(1, nodeId);
            LOG.debug("Cleaning the tasks of node [" + nodeId + "].");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cleaning the tasks of node [" + nodeId + "].");
            }
            preparedStatement.executeUpdate();
        }
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

        List<String> taskList = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.RETRIEVE_TASKS_OF_PARTICULAR_NODE)) {
            preparedStatement.setString(1, nodeID);
            preparedStatement.setString(2, state.name());
            populateCoordinatedTaskNames(preparedStatement, taskList);
        }
        debugLogs(taskList);
        return taskList;
    }

    private void debugLogs(List<String> tasks) {
        if (LOG.isDebugEnabled()) {
            debugLogs(tasks, "Following list of tasks were retrieved from db.");
        }
    }

    private void debugLogs(List<String> tasks, String msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(msg);
            for (String task : tasks) {
                LOG.debug(" Task Name [" + task + "].");
            }
        }
    }

    private void printDebugLogs(List<CoordinatedTask> tasks) {
        if (LOG.isDebugEnabled()) {
            printDebugLogs(tasks, "Following list of tasks were retrieved from db.");
        }
    }

    private void printDebugLogs(List<CoordinatedTask> tasks, String msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(msg);
            for (CoordinatedTask task : tasks) {
                LOG.debug(" Task Name [" + task.getTaskName() + "]  destined node Id [" + task.getDestinedNodeId()
                                  + "] state [" + task.getTaskState() + "].");
            }
        }
    }

    /**
     * Removes all the tasks assigned to the node.
     *
     * @param nodeId - The node id.
     */
    public void removeTasksOfNode(String nodeId) throws SQLException {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.REMOVE_TASKS_OF_NODE)) {
            preparedStatement.setString(1, nodeId);
            preparedStatement.executeUpdate();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removed all the tasks of node [" + nodeId + "].");
        }
    }

    /**
     * Remove the task entry from the db.
     *
     * @param coordinatedTasks - List of tasks to be removed.
     */
    public void removeTasksFromDB(List<String> coordinatedTasks) throws SQLException {

        if (coordinatedTasks.isEmpty()) {
            return;
        }
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.DELETE_TASK)) {
            for (String task : coordinatedTasks) {
                preparedStatement.setString(1, task);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        debugLogs(coordinatedTasks, "Following list of tasks were deleted from data base.");
    }

    /**
     * Helper method to populate coordinated tasks to the list.
     *
     * @param preparedStatement - Statement to be executed to retrieve the list of tasks.
     * @param taskList          - The list to be populated.
     * @throws SQLException - Exception.
     */
    private void populateCoordinatedTaskNames(PreparedStatement preparedStatement, List<String> taskList)
            throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                taskList.add(resultSet.getString(ConstantUtil.TASK_NAME));
            }
        }
    }

    /**
     * Helper method to populate coordinated tasks to the list.
     *
     * @param preparedStatement - Statement to be executed to retrieve the list of tasks.
     * @param taskList          - The list to be populated.
     * @throws SQLException - Exception.
     */
    private void populateCoordinatedTaskList(PreparedStatement preparedStatement, List<CoordinatedTask> taskList)
            throws SQLException {

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {

                taskList.add(new CoordinatedTask(resultSet.getString(ConstantUtil.TASK_NAME),
                                                 resultSet.getString(ConstantUtil.DESTINED_NODE_ID),
                                                 CoordinatedTask.States
                                                         .valueOf(resultSet.getString(ConstantUtil.TASK_STATE))));
            }
        }
    }

    /**
     * Retrieve all the tasks in the task db.
     *
     * @return - List of available tasks.
     */
    public List<String> getAllTasksInDB() throws SQLException {

        List<String> taskList = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.RETRIEVE_ALL_TASKS)) {
            populateCoordinatedTaskNames(preparedStatement, taskList);
        }
        debugLogs(taskList);
        return taskList;
    }

    /**
     * Retrieve all assigned and in completed tasks in the task db.
     *
     * @return - List of available tasks.
     */
    public List<CoordinatedTask> getAllAssignedInCompleteTasks() throws SQLException {

        List<CoordinatedTask> taskList = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.GET_ALL_ASSIGNED_IN_COMPLETE_TASKS)) {
            populateCoordinatedTaskList(preparedStatement, taskList);
        }
        printDebugLogs(taskList);
        return taskList;
    }

    /**
     * Add the task to the task db.
     *
     * @param coordinatedTask - The coordinated task which needs to be added.
     */
    public void addTaskToDB(CoordinatedTask coordinatedTask) throws SQLException {

        String taskName = coordinatedTask.getTaskName();
        CoordinatedTask.States state = coordinatedTask.getTaskState();
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.ADD_TASK)) {
            preparedStatement.setString(1, taskName);
            preparedStatement.setString(2, state.name());
            preparedStatement.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully added the task [" + taskName + "] with state [" + state
                                  + "] to the task data base" + ".");
            }
        }
    }

    /**
     * Updates the state and node id of the provided tasks in db.
     *
     * @param coordinatedTasks - List of tasks to be updated.
     */
    public void updateTaskDB(List<CoordinatedTask> coordinatedTasks) throws SQLException {

        if (coordinatedTasks.isEmpty()) {
            return;
        }
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.UPDATE_TASK)) {
            for (CoordinatedTask coordinatedTask : coordinatedTasks) {
                String taskName = coordinatedTask.getTaskName();
                String nodeId = coordinatedTask.getDestinedNodeId();
                CoordinatedTask.States state = coordinatedTask.getTaskState();
                preparedStatement.setString(1, nodeId);
                preparedStatement.setString(2, state.name());
                preparedStatement.setString(3, taskName);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            printDebugLogs(coordinatedTasks, "Following list of tasks were successfully updated in data base.");
        }
    }

    /**
     * Updates the stat of a task.
     *
     * @param taskName - Name of the task.
     * @param state    - State to be updated.
     */
    public void updateTaskState(String taskName, CoordinatedTask.States state) throws SQLException {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.UPDATE_TASK_STATE)) {
            preparedStatement.setString(1, state.name());
            preparedStatement.setString(2, taskName);
            preparedStatement.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully updated the state of the the task [" + taskName + "] to [" + state + "].");
            }
        }
    }

    /**
     * Get All unassigned tasks except the completed ones.
     *
     * @return - List of unassigned and in complete tasks.
     */
    public List<CoordinatedTask> retrieveAllUnAssignedAndIncompleteTasks() throws SQLException {

        List<CoordinatedTask> unAssignedTasks = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
                TaskQueryHelper.RETRIEVE_UNASSIGNED_NOT_COMPLETED_TASKS)) {
            populateCoordinatedTaskList(preparedStatement, unAssignedTasks);
        }
        printDebugLogs(unAssignedTasks);
        return unAssignedTasks;
    }

    /**
     * Get connection
     *
     * @return - Connection with auto commit true.
     * @throws SQLException -
     */
    private Connection getConnection() throws SQLException {

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

}
