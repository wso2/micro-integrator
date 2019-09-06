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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskDescriptionSerializer;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.namespace.QName;

public class TaskResource extends APIResource {

    public TaskResource(String urlTemplate) {

        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String param = Utils.getQueryParameter(messageContext, "taskName");

        if (Objects.nonNull(param)) {
            populateTaskData(messageContext, param);
        } else {
            populateTasksList(messageContext);
        }

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateTasksList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<Startup> tasks = configuration.getStartups();
        JSONObject jsonBody = Utils.createJSONList(tasks.size());
        for (Startup task : tasks) {
            JSONObject taskObject = new JSONObject();
            taskObject.put(Constants.NAME, task.getName());
            jsonBody.getJSONArray(Constants.LIST).put(taskObject);
        }

        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateTaskData(MessageContext messageContext, String taskName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        SynapseConfiguration configuration = messageContext.getConfiguration();
        Startup task = configuration.getStartup(taskName);
        SynapseEnvironment synapseEnvironment =
                getSynapseEnvironment(axis2MessageContext.getConfigurationContext().getAxisConfiguration());
        TaskDescription description =
                synapseEnvironment.getTaskManager().getTaskDescriptionRepository().getTaskDescription(task.getName());
        JSONObject jsonBody = getTaskAsJson(description);

        if (Objects.nonNull(jsonBody)) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        }
    }

    /**
     * Returns the json representation of a given scheduled task.
     *
     * @param task Scheduled task
     * @return json representation of atsk
     */
    private JSONObject getTaskAsJson(TaskDescription task) {

        JSONObject taskObject = new JSONObject();

        taskObject.put(Constants.NAME, task.getName());
        taskObject.put("taskGroup", task.getTaskGroup());
        taskObject.put("implementation", task.getTaskImplClassName());
        String triggerType = "simple";

        if (task.getCronExpression() != null) {
            triggerType = "cron";
            taskObject.put("cronExpression", task.getCronExpression());
        } else {
            taskObject.put("triggerCount", String.valueOf(task.getCount()));
            taskObject.put("triggerInterval", String.valueOf(task.getInterval()));
        }
        taskObject.put("triggerType", triggerType);
        taskObject.put("properties", getProperties(task.getXmlProperties()));
        taskObject.put(Constants.SYNAPSE_CONFIGURATION, TaskDescriptionSerializer.serializeTaskDescription(null, task));

        return taskObject;
    }

    /**
     * Returns a String map of properties of the task.
     *
     * @param xmlProperties xml property set
     * @return Map
     */
    private Map getProperties(Set xmlProperties) {
        Map<String, String> properties = new HashMap<>();
        Iterator<OMElement> propertiesItr = xmlProperties.iterator();

        while (propertiesItr.hasNext()) {
            OMElement propertyElem = propertiesItr.next();
            String propertyName = propertyElem.getAttributeValue(new QName("name"));
            OMAttribute valueAttr = propertyElem.getAttribute(new QName("value"));

            String value;
            if (valueAttr != null) {
                value = valueAttr.getAttributeValue();
            } else {
                value = propertyElem.getFirstElement().toString();
            }
            properties.put(propertyName, value);
        }
        return properties;
    }

    /**
     * Returns the Synapse environment from the axis configuration.
     *
     * @param axisCfg Axis configuration
     * @return SynapseEnvironment
     */
    private SynapseEnvironment getSynapseEnvironment(AxisConfiguration axisCfg) {

        return (SynapseEnvironment) axisCfg.getParameter(SynapseConstants.SYNAPSE_ENV).getValue();
    }
}
