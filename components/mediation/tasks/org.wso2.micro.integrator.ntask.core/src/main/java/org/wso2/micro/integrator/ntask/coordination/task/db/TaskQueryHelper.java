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

package org.wso2.micro.integrator.ntask.coordination.task.db;

import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;

/**
 * The class which contains all the data base queries for the task database.
 */
public class TaskQueryHelper {

    //task table Name
    private static final String TABLE_NAME = "COORDINATED_TASK_TABLE";

    // Task table columns
    public static final String TASK_NAME = "TASK_NAME";
    public static final String DESTINED_NODE_ID = "DESTINED_NODE_ID";
    public static final String TASK_STATE = "TASK_STATE";

    public static final String ADD_TASK_IF_NOT_EXISTS =
            "INSERT IGNORE INTO " + TABLE_NAME + " ( " + TASK_NAME + ", " + DESTINED_NODE_ID + ", " + TASK_STATE + ") "
                    + "VALUES (?,NULL,\"" + CoordinatedTask.States.NONE + "\")";

    public static final String UPDATE_ASSIGNMENT_AND_RUNNING_STATE_TO_NONE =
            "UPDATE  " + TABLE_NAME + " SET  " + DESTINED_NODE_ID + " = ? , " + TASK_STATE + " = ( CASE " + TASK_STATE
                    + " WHEN \"" + CoordinatedTask.States.RUNNING + "\"THEN \"" + CoordinatedTask.States.NONE
                    + "\" ELSE " + TASK_STATE + " END ) WHERE " + TASK_NAME + " = ?";

    public static final String UPDATE_TASK_STATE =
            "UPDATE  " + TABLE_NAME + "  SET " + TASK_STATE + " = ? WHERE " + TASK_NAME + " =? ";

    public static final String RETRIEVE_ALL_TASKS = "SELECT  " + TASK_NAME + " FROM " + TABLE_NAME;

    public static final String RETRIEVE_UNASSIGNED_NOT_COMPLETED_TASKS =
            "SELECT " + TASK_NAME + " FROM " + TABLE_NAME + " WHERE  " + DESTINED_NODE_ID + " IS NULL AND " + TASK_STATE
                    + " !=\"" + CoordinatedTask.States.COMPLETED + "\"";

    public static final String RETRIEVE_TASKS_OF_NODE =
            "SELECT " + TASK_NAME + " FROM " + TABLE_NAME + "  WHERE " + DESTINED_NODE_ID + " =? AND " + TASK_STATE
                    + " =?";

    public static final String REMOVE_ASSIGNMENT_AND_UPDATE_RUNNING_STATE_TO_NONE =
            "UPDATE " + TABLE_NAME + " SET " + DESTINED_NODE_ID + " = NULL , " + TASK_STATE + " = ( CASE " + TASK_STATE
                    + " WHEN \"" + CoordinatedTask.States.RUNNING + "\"THEN \"" + CoordinatedTask.States.NONE
                    + "\" ELSE " + TASK_STATE + " END ) WHERE " + TASK_NAME + " =?";

    public static final String REMOVE_TASKS_OF_NODE =
            "DELETE FROM " + TABLE_NAME + "  WHERE " + DESTINED_NODE_ID + " =?";

    public static final String DELETE_TASK = "DELETE FROM " + TABLE_NAME + " WHERE " + TASK_NAME + " =?";

    public static final String CLEAN_TASKS_OF_NODE =
            "UPDATE " + TABLE_NAME + " SET " + DESTINED_NODE_ID + " = NULL , " + TASK_STATE + " = ( CASE " + TASK_STATE
                    + " WHEN \"" + CoordinatedTask.States.RUNNING + "\"THEN \"" + CoordinatedTask.States.NONE
                    + "\" ELSE " + TASK_STATE + " END ) WHERE " + DESTINED_NODE_ID + " = ?";

    public static final String GET_ALL_ASSIGNED_INCOMPLETE_TASKS =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + DESTINED_NODE_ID + " IS NOT NULL AND " + TASK_STATE + " != \""
                    + CoordinatedTask.States.COMPLETED + "\"";

    private TaskQueryHelper() throws IllegalAccessException {
        throw new IllegalAccessException("This class not to be initialized.");
    }

}
