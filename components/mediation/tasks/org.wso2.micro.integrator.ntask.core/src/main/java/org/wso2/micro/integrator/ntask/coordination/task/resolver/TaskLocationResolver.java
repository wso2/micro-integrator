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
package org.wso2.micro.integrator.ntask.coordination.task.resolver;

import org.wso2.micro.integrator.ntask.common.TaskException;
import org.wso2.micro.integrator.ntask.coordination.task.ClusterCommunicator;

import java.util.Map;

/**
 * This interface represents the contract that must be implemented to retrieve
 * the location that a given task should be scheduled.
 */
public interface TaskLocationResolver {

    /**
     * Initializes the task location resolver with the given properties.
     *
     * @param properties The property map
     * @throws TaskException Exception
     */
    void init(Map<String, String> properties) throws TaskException;

    /**
     * Returns the node id in which the task needs to be scheduled.
     *
     * @param clusterCommunicator - The cluster clusterCommunicator instance.
     * @param taskName            - Name of the task
     * @return - Node Id .
     */
    String getTaskNodeLocation(ClusterCommunicator clusterCommunicator, String taskName);

}
