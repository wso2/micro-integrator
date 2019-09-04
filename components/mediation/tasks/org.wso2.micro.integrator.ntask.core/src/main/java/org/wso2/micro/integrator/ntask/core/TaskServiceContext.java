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
package org.wso2.micro.integrator.ntask.core;

import org.wso2.carbon.ntask.common.TaskException;

import com.hazelcast.core.Member;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * This class represents a runtime context of the task service.
 */
public class TaskServiceContext {

    private TaskRepository taskRepo;

    private List<String> memberIds;
    
    private Map<String, Member> memberMap;

    private static final String LOCAL_MEMBER_IDENTIFIER = "localMemberIdentifier";

    public TaskServiceContext(TaskRepository taskRepo, List<String> memberIds, 
            Map<String, Member> memberMap) {
        this.taskRepo = taskRepo;
        this.memberIds = memberIds;
        this.memberMap = memberMap;
    }

    public int getTenantId() {
        return this.taskRepo.getTenantId();
    }

    public String getTaskType() {
        return this.taskRepo.getTasksType();
    }

    public List<TaskInfo> getTasks() throws TaskException {
        return this.taskRepo.getAllTasks();
    }

    public int getServerCount() {
        return this.memberIds.size();
    }
    
    public InetSocketAddress getServerAddress(int index) {
        String memberId = this.memberIds.get(index);
        Member member = this.memberMap.get(memberId);
        if (member == null) {
            return null;
        }
        return member.getSocketAddress();
    }

    public String getServerIdentifier(int index) {
        String memberId = this.memberIds.get(index);
        Member member = this.memberMap.get(memberId);
        if (member == null) {
            return null;
        }
        return member.getStringAttribute(LOCAL_MEMBER_IDENTIFIER);
    }
}
