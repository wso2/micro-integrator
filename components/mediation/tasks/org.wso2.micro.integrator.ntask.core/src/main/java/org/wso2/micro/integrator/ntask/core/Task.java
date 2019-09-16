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

import java.util.Map;

/**
 * Task interface to be implemented by all tasks.
 */
public interface Task {

    /**
     * This method is called initially to set the task properties.
     *
     * @param properties The task properties
     */
    public void setProperties(Map<String, String> properties);

    /**
     * This method is called once after properties are set in the task, and
     * right before the task is executed.
     */
    public void init();

    /**
     * This method will be called when the task is started, the task logic
     * should go here.
     */
    public void execute();

}
