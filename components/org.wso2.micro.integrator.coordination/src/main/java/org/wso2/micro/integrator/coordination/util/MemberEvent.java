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

package org.wso2.micro.integrator.coordination.util;


/**
 * This class represents a specific nodeId event type for a specific nodeId.
 */
public class MemberEvent {
    /**
     * The node Id for which the membership event was triggered.
     */
    private String nodeId;

    /**
     * The group Id for which the membership event was triggered.
     */
    private String groupId;

    /**
     * Membership event type.
     */
    private MemberEventType type;

    /**
     * Constructor.
     *
     * @param type   Membership event type
     * @param nodeId The members node ID
     * @param groupId The members group ID
     */
    public MemberEvent(MemberEventType type, String nodeId, String groupId) {
        this.type = type;
        this.nodeId = nodeId;
        this.groupId = groupId;
    }

    /**
     * Retrieve the node id of the nodeId for which the membership event is triggered.
     *
     * @return node id of the nodeId
     */
    public String getTargetNodeId() {
        return this.nodeId;
    }

    /**
     * Retrieve the group id of the nodeId for which the membership event is triggered.
     *
     * @return group id of the nodeId
     */
    public String getTargetGroupId() {
        return this.groupId;
    }

    /**
     * Retrieve the type of the membership event.
     *
     * @return membership event type.
     */
    public MemberEventType getMembershipEventType() {
        return type;
    }
}
