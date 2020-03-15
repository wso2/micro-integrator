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

import org.wso2.micro.integrator.ntask.coordination.task.ConstantUtil;
import org.wso2.micro.integrator.ntask.coordination.task.CoordinatedTask;

/**
 * The class which contains all the data base queries for the task database.
 */
public class TaskQueryHelper {

    public static final String ADD_TASK =
            "INSERT INTO " + ConstantUtil.TABLE_NAME + " ( " + ConstantUtil.TASK_NAME + ", "
                    + ConstantUtil.DESTINED_NODE_ID + ", " + ConstantUtil.TASK_STATE + ") " + "VALUES (?,NULL,?)";

    public static final String UPDATE_TASK =
            "UPDATE  " + ConstantUtil.TABLE_NAME + " SET  " + ConstantUtil.DESTINED_NODE_ID + " = ? , "
                    + ConstantUtil.TASK_STATE + " = ?  WHERE " + ConstantUtil.TASK_NAME + " = ?";

    public static final String UPDATE_TASK_STATE =
            "UPDATE  " + ConstantUtil.TABLE_NAME + "  SET " + ConstantUtil.TASK_STATE + " = ? WHERE "
                    + ConstantUtil.TASK_NAME + " =? ";

    public static final String RETRIEVE_ALL_TASKS =
            "SELECT  " + ConstantUtil.TASK_NAME + " FROM " + ConstantUtil.TABLE_NAME;

    public static final String RETRIEVE_UNASSIGNED_NOT_COMPLETED_TASKS =
            "SELECT * FROM " + ConstantUtil.TABLE_NAME + " WHERE  " + ConstantUtil.DESTINED_NODE_ID + " IS NULL AND "
                    + ConstantUtil.TASK_STATE + " !=\"" + CoordinatedTask.States.COMPLETED + "\"";

    public static final String RETRIEVE_TASKS_OF_PARTICULAR_NODE =
            "SELECT " + ConstantUtil.TASK_NAME + " FROM " + ConstantUtil.TABLE_NAME + "  WHERE "
                    + ConstantUtil.DESTINED_NODE_ID + " =? AND " + ConstantUtil.TASK_STATE + " =?";

    public static final String CLEAN_INVALID_NODES =
            "UPDATE " + ConstantUtil.TABLE_NAME + " SET " + ConstantUtil.DESTINED_NODE_ID + " = NULL , "
                    + ConstantUtil.TASK_STATE + " = ? WHERE " + ConstantUtil.TASK_NAME + " =?";

    public static final String REMOVE_TASKS_OF_NODE =
            "DELETE FROM " + ConstantUtil.TABLE_NAME + "  WHERE " + ConstantUtil.DESTINED_NODE_ID + " =?";

    public static final String DELETE_TASK =
            "DELETE FROM " + ConstantUtil.TABLE_NAME + " WHERE " + ConstantUtil.TASK_NAME + " =?";

    public static final String CLEAN_TASKS_OF_NODE =
            "UPDATE " + ConstantUtil.TABLE_NAME + " SET " + ConstantUtil.DESTINED_NODE_ID + " = NULL , "
                    + ConstantUtil.TASK_STATE + " = ( CASE " + ConstantUtil.TASK_STATE + " WHEN \""
                    + CoordinatedTask.States.RUNNING + "\"THEN \"" + CoordinatedTask.States.NONE + "\" ELSE "
                    + ConstantUtil.TASK_STATE + " END ) WHERE " + ConstantUtil.DESTINED_NODE_ID + " = ?";

    public static final String GET_ALL_ASSIGNED_IN_COMPLETE_TASKS =
            "SELECT * FROM " + ConstantUtil.TABLE_NAME + " WHERE " + ConstantUtil.DESTINED_NODE_ID + " IS NOT NULL AND "
                    + ConstantUtil.TASK_STATE + " != \"" + CoordinatedTask.States.COMPLETED + "\"";

    private TaskQueryHelper() throws IllegalAccessException {
        throw new IllegalAccessException("This class not to be initialized.");
    }

}
