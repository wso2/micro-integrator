/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.task.TaskDescription;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskResource extends APIResource {

    private Utils utils = new Utils();

    public TaskResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
//        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = utils.getQueryParameters(axis2MessageContext);

        // if query params exists retrieve data about specific task
        if (null != queryParameter) {
            for (NameValuePair nvPair : queryParameter) {
                if (nvPair.getName().equals("taskName")) {
                    populateTaskData(messageContext, nvPair.getValue());
                }
            }
        } else {
            populateTasksList(messageContext);
        }

        axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        return true;
    }

    private void populateTasksList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        String[] taskNames = configuration.getTaskManager().getTaskNames();

        JSONObject jsonBody = new JSONObject();
        JSONArray taskList = new JSONArray();
        jsonBody.put("count", taskNames.length);
        jsonBody.put("list", taskList);

        for (String taskName : taskNames) {

            JSONObject taskObject = getTaskByName(messageContext, taskName);
            taskList.put(taskObject);
        }
        utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateTaskData(MessageContext messageContext, String taskName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getTaskByName(messageContext, taskName);

        if (null != jsonBody) {
            utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private JSONObject getTaskByName(MessageContext messageContext, String taskName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();

        String []taskNames = configuration.getTaskManager().getTaskNames();
        for (String task : taskNames) {
            if (task.equals(taskName)) {
                return convertTaskToOMElement(configuration.getTaskManager().getTask(taskName));
            }
        }
        return null;
    }

    private JSONObject convertTaskToOMElement(TaskDescription task) {

        if (null == task) {
            return null;
        }

        JSONObject taskObject = new JSONObject();

        taskObject.put("name", task.getName());

        String triggerType = "cron";

        if (null == task.getCronExpression()) {
            triggerType = "simple";
        }

        taskObject.put("triggerType", triggerType);

        taskObject.put("triggerCount", String.valueOf(task.getCount()));
        taskObject.put("triggerInterval", String.valueOf(task.getInterval()));
        taskObject.put("triggerCron", task.getCronExpression());

        return taskObject;
    }
}
