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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This resolver distributes the tasks to the all the available nodes in the cluster in a round robin fashion. In
 * addition it waits for the Task Server Count ( no. of nodes to be presented before resolving ) to start resolving.
 */
public class RoundRobinResolver implements TaskLocationResolver {

    private static final Log log = LogFactory.getLog(RoundRobinResolver.class);

    private AtomicInteger taskLocation = new AtomicInteger(0);
    private int taskServerCount;
    private static final String TASK_SERVER_COUNT_PROPERTY = "task_server_count";

    @Override
    public void init(Map<String, String> properties) {

        int serverCount = 1;
        try {
            serverCount = Integer.parseInt(properties.get(TASK_SERVER_COUNT_PROPERTY));
        } catch (NumberFormatException ex) {
            log.error("Specify an int value for " + TASK_SERVER_COUNT_PROPERTY + ". Default "
                              + "value of 1 will be used.", ex);
        }
        this.taskServerCount = serverCount;
    }

    @Override
    public String getTaskNodeLocation(ClusterCommunicator clusterCommunicator, String taskName) {

        int location = taskLocation.incrementAndGet();
        List<String> allNodesAvailableInCluster = clusterCommunicator.getAllNodeIds();
        int availableNoOfNodes = allNodesAvailableInCluster.size();
        if (availableNoOfNodes == 0) {
            log.warn("No nodes are registered to the cluster successfully yet.");
            return null;
        }
        if (availableNoOfNodes < taskServerCount) {
            log.info("Waiting for " + taskServerCount + " nodes to resolve the tasks. Only " + availableNoOfNodes
                             + " node(s) available cluster ...");
            return null;
        }
        String destinedNode = allNodesAvailableInCluster.get(Math.abs(location) % availableNoOfNodes);
        if (log.isDebugEnabled()) {
            log.debug("The task : " + taskName + ", is resolved to node with id : " + destinedNode);
        }
        return destinedNode;
    }

}
