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

import org.wso2.micro.integrator.coordination.ClusterCoordinator;

import java.util.List;

/**
 * The class which provides the cluster node details. No additional controlling methods should be added to this un
 * necessarily as this is exposed to task location resolver. Task location will be resolved only in leader nodes.
 *
 * @see org.wso2.micro.integrator.ntask.coordination.task.resolver.TaskLocationResolver
 */
public class ClusterCommunicator {

    private ClusterCoordinator clusterCoordinator;

    public ClusterCommunicator(ClusterCoordinator coordinator) {
        this.clusterCoordinator = coordinator;
    }

    /**
     * Provides all the ids of the nodes in the cluster.
     *
     * @return - List of nodes ids.
     */
    public List<String> getAllNodeIds() {
        return clusterCoordinator.getAllNodeIds();
    }

    /**
     * Provides the id of this node.
     *
     * @return - Id of this node.
     */
    public String getThisNodeId() {
        return clusterCoordinator.getThisNodeId();
    }

}
