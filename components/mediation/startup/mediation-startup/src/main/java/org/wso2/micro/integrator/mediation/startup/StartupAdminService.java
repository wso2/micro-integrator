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

package org.wso2.micro.integrator.mediation.startup;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskDescriptionSerializer;
import org.apache.synapse.task.service.TaskManagementService;
import org.wso2.micro.integrator.initializer.AbstractServiceBusAdmin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Provide Task based Startup management
 */
public class StartupAdminService extends AbstractServiceBusAdmin implements TaskManagementService {

	private static Log log = LogFactory.getLog(StartupAdminService.class);

	public StartupAdminService() {
	}

	public void addTaskDescription(TaskDescription taskDescription) {
		final Lock lock = getLock();
		try {
			lock.lock();
			validateTaskDescription(taskDescription);
			OMElement taskElement = TaskDescriptionSerializer
					.serializeTaskDescription(SynapseConstants.SYNAPSE_OMNAMESPACE, taskDescription);
			validateTaskElement(taskElement);

			StartupUtils.addStartup(taskElement, getSynapseConfiguration(), getSynapseEnvironment());
		} finally {
			lock.unlock();
		}
	}

	public void deleteTaskDescription(String taskName) {
		final Lock lock = getLock();
		try {
			lock.lock();
			validateName(taskName);
			StartupUtils.deleteStartup(taskName, getSynapseConfiguration());
		} finally {
			lock.unlock();
		}
	}

	public void editTaskDescription(TaskDescription taskDescription) {
		final Lock lock = getLock();
		try {
			lock.lock();
			validateTaskDescription(taskDescription);
			OMElement taskElement = TaskDescriptionSerializer
					.serializeTaskDescription(SynapseConstants.SYNAPSE_OMNAMESPACE, taskDescription);
			validateTaskElement(taskElement);

			StartupUtils.updateStartup(taskDescription.getName(), taskElement, getSynapseConfiguration(),
			                           getSynapseEnvironment());
		} finally {
			lock.unlock();
		}
	}

	public List<TaskDescription> getAllTaskDescriptions() {
		final Lock lock = getLock();
		try {
			lock.lock();
			List<TaskDescription> taskDescriptions = new ArrayList<TaskDescription>();

			Iterator<TaskDescription> iterator = StartupUtils.getAllTaskDescriptions(getSynapseEnvironment());
			while (iterator.hasNext()) {
				TaskDescription taskDescription = iterator.next();
				if (taskDescription != null) {
					taskDescriptions.add(taskDescription);
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("All available Task based Startup " + taskDescriptions);
			}

			return taskDescriptions;
		} finally {
			lock.unlock();
		}
	}

	public TaskDescription getTaskDescription(String taskName) {
		final Lock lock = getLock();
		try {
			lock.lock();
			validateName(taskName);
			return StartupUtils.getTaskDescription(taskName, getSynapseEnvironment());
		} finally {
			lock.unlock();
		}
	}

	public boolean isContains(String taskName) {
		final Lock lock = getLock();
		try {
			lock.lock();
			validateName(taskName);
			return StartupUtils.isContains(taskName, getSynapseEnvironment());
		} finally {
			lock.unlock();
		}
	}

	public List<String> getPropertyNames(String taskClass) {
		final Lock lock = getLock();
		List<String> names = new ArrayList<String>();
		try {
			lock.lock();
			Class clazz = Class.forName(taskClass.trim());
			Method[] methods = clazz.getMethods();
			Field[] fields = clazz.getDeclaredFields();
			for (Method method : methods) {
				if (method == null) {
					continue;
				}
				String methodName = method.getName();
				if (methodName == null) {
					continue;
				}

				if (methodName.startsWith("set") && !"setTraceState".equals(methodName)) {
					for (Field field : fields) {

						if (field.getName().equalsIgnoreCase(methodName.substring(3))) {
							names.add(field.getName());
							break;
						}
					}
				}

			}
		} catch (ClassNotFoundException e) {
			handleException("Class " + taskClass + " not found in the path", e);
		} finally {
			lock.unlock();
		}
		if (log.isDebugEnabled()) {
			log.debug("Task class '" + taskClass + "' contains property Names : " + names);
		}
		return names;
	}

	private static void validateTaskDescription(TaskDescription description) {
		if (description == null) {
			handleException("Task Description can not be found.");
		}
	}

	private static void validateTaskElement(OMElement taskElement) {
		if (taskElement == null) {
			handleException("Task Description OMElement can not be found.");
		}
	}

	private static void validateName(String name) {
		if (name == null || "".equals(name)) {
			handleException("Name is null or empty");
		}
	}

	private static void handleException(String msg) {
		log.error(msg);
		throw new IllegalArgumentException(msg);
	}

	private static void handleException(String msg, Throwable throwable) {
		log.error(msg, throwable);
		throw new RuntimeException(msg);
	}
}
