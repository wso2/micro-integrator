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

package org.wso2.micro.integrator.coordination;


import org.wso2.micro.integrator.coordination.exception.ClusterCoordinationException;
import org.wso2.micro.integrator.coordination.node.NodeDetail;

import java.util.List;
import java.util.Map;

/**
 * This interface is common for all the coordination strategies. All the coordination strategy classes
 * should implement this interface.
 */
public interface CoordinationStrategy {

    /**
     * Get the node details of the current cluster group.
     *
     * @return the node details of the current group
     */
    List<NodeDetail> getAllNodeDetails() throws ClusterCoordinationException;

    /**
     * Get the leader ID of the current cluster group.
     *
     * @return the  leader node ID of the current cluster group
     */
    NodeDetail getLeaderNode();

    /**
     * Check if this node is the leader of the given group Id
     *
     * @return true if node is leader
     */
    boolean isLeaderNode();

    /**
     * Register an event listener as an instance of the MemberEventListener class. Therefore the node
     * events can be notified via the listener class.
     *
     * @param memberEventListener The class instance for listening member events
     */
    void registerEventListener(MemberEventListener memberEventListener);

    /**
     * Join the configured group.
     *
     */
    void joinGroup();

}

