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
import org.wso2.micro.integrator.ntask.coordination.task.ClusterNodeDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class will resolve tasks to predefined set of nodes ( task nodes ) in a round robin fashion.
 * Predefined node list : "task_nodes", "node-1,node-2 ,node-3,node-4" ( to be defined in toml ).
 */
public class TaskNodeResolver implements TaskLocationResolver {

    private static final Log log = LogFactory.getLog(TaskNodeResolver.class);

    private AtomicInteger taskLocation = new AtomicInteger(0);
    private List<String> definedNodeList = new ArrayList<>();
    private static final String TASK_NODES = "task_nodes";

    @Override
    public void init(Map<String, String> properties) {

        for (Map.Entry<String, String> property : properties.entrySet()) {
            if (TASK_NODES.equals(property.getKey()) && property.getValue() != null && !property.getValue().isEmpty()) {
                String[] nodeIds = property.getValue().trim().split(",");
                for (String nodeId : nodeIds) {
                    definedNodeList.add(nodeId.trim());
                }
            }
        }
        if (definedNodeList.isEmpty()) {
            throw new UnsupportedOperationException(
                    TaskNodeResolver.class.getName() + " is initialized with empty an set of " + TASK_NODES);
        }
    }

    @Override
    public String getTaskNodeLocation(ClusterNodeDetails clusterNodeDetails, String taskName) {

        List<String> tempDefinedList = new ArrayList<>(definedNodeList.subList(0, definedNodeList.size()));
        List<String> allNodesAvailableInCluster = clusterNodeDetails.getAllNodeIds();
        if (allNodesAvailableInCluster.isEmpty()) {
            log.warn("No nodes are registered to the cluster successfully yet.");
            return null;
        }
        String nodeId = null;
        while (!tempDefinedList.isEmpty()) {
            int location = taskLocation.incrementAndGet();
            nodeId = tempDefinedList.get(Math.abs(location) % tempDefinedList.size());
            if (allNodesAvailableInCluster.contains(nodeId)) {
                break;
            } else {
                tempDefinedList.remove(nodeId);
                nodeId = null;
            }
        }
        if (nodeId == null) {
            log.info("No nodes defined in the " + TASK_NODES
                             + " is found in the cluster. Hence can't resolve a location for the task " + taskName);
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("The task : " + taskName + ", is resolved to node with id : " + nodeId);
        }
        return nodeId;
    }

}
