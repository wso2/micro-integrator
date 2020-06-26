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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.ntask.coordination.task.ClusterCommunicator;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This resolver selects a random node from the available nodes in the cluster and resolves all the tasks to it. The
 * tasks will be resolved to some other node only if the first node leaves the cluster.
 */
public class ActivePassiveResolver implements TaskLocationResolver {

    private static final Log log = LogFactory.getLog(ActivePassiveResolver.class);

    private String destinedNode = null;

    @Override
    public void init(Map<String, String> properties) {
        throw new UnsupportedOperationException(
                "Properties are not supported in " + ActivePassiveResolver.class.getSimpleName());
    }

    /**
     * Provides the node id in which the task need to scheduled.
     *
     * @param taskName The name of the task.
     * @return Node id fot the task.
     */
    @Override
    public String getTaskNodeLocation(ClusterCommunicator clusterCommunicator, String taskName) {

        List<String> allNodesAvailableInCluster = clusterCommunicator.getAllNodeIds();
        int noOfNodesInCluster = allNodesAvailableInCluster.size();
        if (noOfNodesInCluster == 0) {
            log.warn("No nodes are registered to the cluster successfully yet.");
            return null;
        }
        if (destinedNode == null || !allNodesAvailableInCluster.contains(destinedNode)) {
            int location = new Random().nextInt();
            destinedNode = allNodesAvailableInCluster.get(Math.abs(location) % noOfNodesInCluster);
        }
        if (log.isDebugEnabled()) {
            log.debug("The task : " + taskName + ", is resolved to node with id : " + destinedNode);
        }
        return destinedNode;
    }
}
