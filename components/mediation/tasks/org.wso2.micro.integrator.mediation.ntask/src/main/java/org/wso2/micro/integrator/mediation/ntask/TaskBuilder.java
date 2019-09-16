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

import org.apache.synapse.task.TaskDescription;
import org.wso2.micro.integrator.ntask.core.TaskInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class TaskBuilder {
    public static final String REMOTE_TASK_NAME = "__REMOTE_TASK_NAME__";

    public static TaskInfo buildTaskInfo(TaskDescription taskDescription, Map<String, Object> properties) throws Exception {
        return buildTaskInfo(taskDescription, false, properties);
    }

    public static TaskInfo buildTaskInfo(TaskDescription description,
                                   boolean system, Map<String, Object> tmProperties) throws Exception {
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
        String cron = description.getCronExpression();
        if (cron != null && cron.trim().length() == 0) {
            cron = null;
        }
        triggerInfo.setCronExpression(cron);
        if (description.getStartTime() != null) {
            triggerInfo.setStartTime(description.getStartTime().getTime());
        } else if (triggerInfo.getCronExpression() == null) {
            //triggerInfo.setStartTime(new Date());
            //triggerInfo.setMisfirePolicy(TaskConstants.TaskMisfirePolicy.NOW_WITH_REMAINING_COUNT);
        }
        if (description.getEndTime() != null) {
            triggerInfo.setEndTime(description.getEndTime().getTime());
        }
        if (cron == null && !system && description.getInterval() < 1000) {
            throw new Exception("Task interval cannot be less than 1 second for user tasks");
        }
        triggerInfo.setIntervalMillis(description.getInterval());
        triggerInfo.setRepeatCount(description.getCount()
                > 0 ? description.getCount() - 1 : description.getCount());
        triggerInfo.setDisallowConcurrentExecution(true);
        Map<String, String> props = new HashMap<String, String>();
        props.put(REMOTE_TASK_NAME, description.getName());
        // copy the remaining properties
        Map<String, String> properties = description.getProperties();
        Iterator<String> iterator = properties.keySet().iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o != null) {
                props.put((String) o, properties.get(o));
            }
        }
        if (tmProperties != null) {
            for (String name : tmProperties.keySet()) {
                if (tmProperties.get(name) instanceof String) {
                    props.put(name, (String) tmProperties.get(name));
                }
            }
        }
        Object o = description.getResource(TaskDescription.CLASSNAME);
        String taskClassName;
        if (o instanceof String) {
            taskClassName = (String) o;
            props.put(TaskDescription.CLASSNAME, taskClassName);
        } else {
            return null;
        }
        String nameGroup = description.getName() + "::" + description.getTaskGroup();
        props.put("task.name", nameGroup);
        //trigger count cannot be null for a task description hence null check avoided
        props.put("task.count", String.valueOf(description.getCount()));
        Object taskInstance = description.getResource(TaskDescription.INSTANCE);
        if (taskInstance instanceof org.apache.synapse.task.Task) {
            NTaskAdapter.addProperty(nameGroup, taskInstance);
        }
        return new TaskInfo(description.getName(), NTaskAdapter.class.getName(), props, triggerInfo);
    }

    public static TaskDescription buildTaskDescription(TaskInfo taskInfo) {
        TaskDescription taskDescription = new TaskDescription();
        taskDescription.setName(taskInfo.getName());
        Map<String, String> taskProps = taskInfo.getProperties();
        TaskInfo.TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
        taskDescription.setCronExpression(triggerInfo.getCronExpression());
        taskDescription.setStartTime(dateToCal(triggerInfo.getStartTime()));
        taskDescription.setEndTime(dateToCal(triggerInfo.getEndTime()));
        taskDescription.setCount(triggerInfo.getRepeatCount()+1);
        taskDescription.setInterval(triggerInfo.getIntervalMillis());
        taskDescription.setIntervalInMs(true);

        return taskDescription;
    }

    private static Calendar dateToCal(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

}
