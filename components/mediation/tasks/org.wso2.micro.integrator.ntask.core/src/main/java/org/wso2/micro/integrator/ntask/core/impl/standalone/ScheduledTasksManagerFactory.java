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
package org.wso2.micro.integrator.ntask.core.impl.standalone;

import org.wso2.micro.integrator.ntask.common.TaskException;
import org.wso2.micro.integrator.ntask.coordination.task.store.TaskStore;
import org.wso2.micro.integrator.ntask.core.TaskManager;
import org.wso2.micro.integrator.ntask.core.TaskManagerFactory;
import org.wso2.micro.integrator.ntask.core.TaskManagerId;
import org.wso2.micro.integrator.ntask.core.TaskRepository;
import org.wso2.micro.integrator.ntask.core.impl.FileBasedTaskRepository;
import org.wso2.micro.integrator.ntask.core.internal.DataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * This represent the task manager factory of Micro Integrator.
 */
public class ScheduledTasksManagerFactory implements TaskManagerFactory {

    @Override
    public TaskManager getTaskManager(TaskManagerId tmId, TaskStore taskStore) throws TaskException {

        return this.createTaskManager(tmId, taskStore);
    }

    private TaskManager createTaskManager(TaskManagerId tmId, TaskStore taskStore) throws TaskException {

        TaskRepository taskRepo = new FileBasedTaskRepository(tmId.getTenantId(), tmId.getTaskType());
        ScheduledTaskManager scheduledTaskManager = new ScheduledTaskManager(taskRepo, taskStore);
        DataHolder.getInstance().setTaskManager(scheduledTaskManager);
        return scheduledTaskManager;
    }

    @Override
    public List<TaskManager> getStartupSchedulingTaskManagersForType(String taskType, TaskStore taskStore)
            throws TaskException {
        return getTaskManagers(taskType, taskStore);
    }

    private List<TaskManager> getTaskManagers(String taskType, TaskStore taskStore) throws TaskException {

        List<TaskManagerId> tmIds = FileBasedTaskRepository.getAllTenantTaskManagersForType(taskType);
        List<TaskManager> result = new ArrayList<>();
        for (TaskManagerId tmId : tmIds) {
            result.add(this.createTaskManager(tmId, taskStore));
        }
        return result;
    }

}
