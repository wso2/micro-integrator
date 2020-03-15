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

/**
 * Constant class.
 */
public class ConstantUtil {

    //task table Name
    public static final String TABLE_NAME = "COORDINATED_TASK_TABLE";

    // Task table columns
    public static final String TASK_NAME = "TASK_NAME";
    public static final String DESTINED_NODE_ID = "DESTINED_NODE_ID";
    public static final String TASK_STATE = "TASK_STATE";

    public static final String TASK_SERVER_COUNT_PROPERTY = "task_server_count";

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private ConstantUtil() {
        throw new AssertionError("Instantiating constants class...");
    }
}
