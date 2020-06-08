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

package org.wso2.micro.integrator.ntask.core.internal;

import org.wso2.micro.integrator.coordination.ClusterCoordinator;
import org.wso2.micro.integrator.ntask.core.impl.standalone.ScheduledTaskManager;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Data holder for this component.
 *
 * @see TasksDSComponent
 */
public class DataHolder {

    private static final DataHolder instance = new DataHolder();
    private ClusterCoordinator clusterCoordinator;
    private ScheduledTaskManager taskManager;
    private ScheduledExecutorService taskScheduler;

    private DataHolder() {

    }

    public static DataHolder getInstance() {
        return instance;
    }

    public boolean isCoordinationEnabledGlobally() {
        return clusterCoordinator != null;
    }

    public ClusterCoordinator getClusterCoordinator() {
        return clusterCoordinator;
    }

    void setClusterCoordinator(ClusterCoordinator clusterCoordinator) {
        this.clusterCoordinator = clusterCoordinator;
    }

    public String getLocalNodeId() {
        return clusterCoordinator != null ? clusterCoordinator.getThisNodeId() : null;
    }

    public ScheduledTaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(ScheduledTaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public ScheduledExecutorService getTaskScheduler() {
        return taskScheduler;
    }

    public void setTaskScheduler(ScheduledExecutorService taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

}
