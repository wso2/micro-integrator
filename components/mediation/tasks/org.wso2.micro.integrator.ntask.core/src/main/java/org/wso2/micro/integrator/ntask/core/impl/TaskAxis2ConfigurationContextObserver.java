/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.ntask.core.impl;

import org.wso2.micro.integrator.ntask.core.service.TaskService;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This class represents an configuration context observer, used to load the tasks, when a new
 * tenant arrives.
 */
public class TaskAxis2ConfigurationContextObserver extends
        AbstractAxis2ConfigurationContextObserver {

    private TaskService taskService;

    public TaskAxis2ConfigurationContextObserver(TaskService taskService) {
        this.taskService = taskService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

}
