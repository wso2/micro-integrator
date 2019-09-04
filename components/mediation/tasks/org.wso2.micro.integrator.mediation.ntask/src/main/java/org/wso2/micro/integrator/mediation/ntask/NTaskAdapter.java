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

package org.wso2.micro.integrator.mediation.ntask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.Task;
import org.wso2.micro.integrator.ntask.core.AbstractTask;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.micro.integrator.mediation.ntask.Constants.SUPER_TENANT_ID;

public class NTaskAdapter extends AbstractTask {

    private static final Log logger = LogFactory.getLog(NTaskAdapter.class.getName());

    private static final Map<String, Object> synapseTaskProperties = new HashMap<String, Object>();

    private static final Object lock = new Object();

    private boolean initialized = false;

    private org.apache.synapse.task.Task synapseTask;

    public static boolean addProperty(String name, Object property) {
        if (name == null) {
            return false;
        }
        synchronized (lock) {
            return synapseTaskProperties.put(NTaskTaskManager.tenantId() + name, property) == property;
        }
    }

    public static boolean removeProperty(String name) {
        if (name == null) {
            return false;
        }
        synchronized (lock) {
            return synapseTaskProperties.remove(NTaskTaskManager.tenantId() + name) == null;
        }
    }

    public void init() {
        Map<String, String> properties = getProperties();
        if (properties == null) {
            return;
        }
        String taskName = properties.get("task.name");// taskName = "name::group"
        if (taskName == null) {
            return;
        }
        Object taskInstance;
        synchronized (lock) {
            taskInstance = synapseTaskProperties.get(NTaskTaskManager.tenantId() + taskName);
        }
        if (taskInstance == null) {
            // Nothing to execute.
            return;
        }
        if (!(taskInstance instanceof Task)) {
            return;
        }

        // Add runtimeProperties
        if(taskInstance instanceof org.apache.synapse.startup.tasks.MessageInjector){
            org.apache.synapse.startup.tasks.MessageInjector messageInjectorTask = (org.apache.synapse.startup.tasks.MessageInjector) taskInstance;
            messageInjectorTask.addRuntimeProperty(Constants.TASK_EXECUTING_TENANT_ID, SUPER_TENANT_ID );
        }

        synapseTask = (Task) taskInstance;
        initialized = true;
    }

    public void execute() {

        if (!isInitialized()) {
            return;
        }

        //introduced due to limitation of Ntask.core executing task for single cycle when task
        // count 0
        //trigger count can be null in some scenarios when editing tasks hence null check
        if (getProperties().get("task.count") == null || (getProperties().get("task.count") != null && Integer.parseInt(getProperties().get("task.count")) == 0)) {
            return;
        }

        synapseTask.execute();
    }

    
    private boolean isInitialized() { return initialized; }
}
