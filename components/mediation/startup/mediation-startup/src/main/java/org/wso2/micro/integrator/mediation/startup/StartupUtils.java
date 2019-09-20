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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Startup;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.startup.quartz.SimpleQuartzFactory;
import org.apache.synapse.startup.AbstractStartup;
import org.apache.synapse.task.TaskDescription;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;
import org.wso2.micro.integrator.initializer.ServiceBusUtils;
import org.wso2.micro.integrator.initializer.persistence.MediationPersistenceManager;

import java.util.Iterator;

/**
 * Synapse Startup Management
 */

public class StartupUtils {

	private final static Log log = LogFactory.getLog(StartupUtils.class);

	public StartupUtils() {
	}

	public static void addStartup(OMElement startupElement, SynapseConfiguration synapseConfiguration,
	                              SynapseEnvironment synapseEnvironment) {
		addStartup(startupElement, null, true, synapseConfiguration, synapseEnvironment);
	}

	private static void addStartup(OMElement startupElement, String fileName, boolean generateFileName,
	                               SynapseConfiguration synapseConfiguration, SynapseEnvironment synapseEnvironment) {

		SimpleQuartzFactory factory = new SimpleQuartzFactory();
		Startup startup = factory.createStartup(startupElement);

		if (startup == null) {
			handleException("Startup is null");
		} else {
			if (startup instanceof AbstractStartup) {
				if (generateFileName) {
					fileName = ServiceBusUtils.generateFileName((startup.getName()));
				}
				startup.setFileName(fileName);
			}
			synapseConfiguration.addStartup(startup);
			startup.init(synapseEnvironment);
			persistStartup(startup, synapseConfiguration.getAxisConfiguration());

			if (log.isDebugEnabled()) {
				log.debug("Added Startup : " + startup + " from the configuration");
			}
		}
	}

	public static void updateStartup(String name, OMElement taskElement, SynapseConfiguration synapseConfiguration,
	                                 SynapseEnvironment synapseEnvironment) {

		Startup st = synapseConfiguration.getStartup(name);

		if (st == null) {
			log.warn("Cannot update the startup named: " + name + ", it doesn't exists in the SynapseConfiguration");
			return;
		}

		String fileName = null;
		if (st instanceof AbstractStartup) {
			fileName = st.getFileName();
		}

		deleteStartup(st.getName(), synapseConfiguration);
		addStartup(taskElement, fileName, false, synapseConfiguration, synapseEnvironment);
	}

	public static void deleteStartup(String name, SynapseConfiguration synapseConfiguration) {
		Startup st = synapseConfiguration.getStartup(name);
		if (st != null) {
			st.destroy();
			String fileName = null;
			if (st instanceof AbstractStartup) {
				fileName = st.getFileName();
			}
			synapseConfiguration.removeStartup(name);
			if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
				MediationPersistenceManager pm =
						ServiceBusUtils.getMediationPersistenceManager(synapseConfiguration.getAxisConfiguration());
				pm.deleteItem(name, fileName, ServiceBusConstants.ITEM_TYPE_TASK);
			}

		} else {
			log.warn("Cannot delete the startup named " + name + ", it doesn't exists in the SynapseConfiguration");
		}

		if (log.isDebugEnabled()) {
			log.debug("Deleted Startup : " + name + " from the configuration");
		}
	}

	public static TaskDescription getTaskDescription(String name, SynapseEnvironment synapseEnvironment) {

		if (log.isDebugEnabled()) {
			log.debug("Returning a Startup : " + name + " from the configuration");
		}
		TaskDescription taskDescription = synapseEnvironment.getTaskManager().
				getTaskDescriptionRepository().getTaskDescription(name);
		if (taskDescription != null) {
			if (log.isDebugEnabled()) {
				log.debug("Returning a TaskDescription : " + taskDescription);

			}
			return taskDescription;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("There is no  Startup with name :" + name);
			}
			return null;
		}
	}

	public static Iterator<TaskDescription> getAllTaskDescriptions(SynapseEnvironment synapseEnvironment) {

		if (log.isDebugEnabled()) {
			log.debug("Returning a All Startups from the configuration");
		}
		return synapseEnvironment.getTaskManager().
				getTaskDescriptionRepository().getAllTaskDescriptions();
	}

	public static boolean isContains(String name, SynapseEnvironment synapseEnvironment) {
		return !synapseEnvironment.getTaskManager().
				getTaskDescriptionRepository().isUnique(name);
	}

	public static void shutDown() {
	}

	private static void persistStartup(Startup startup, AxisConfiguration axisCfg) {
		if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
			MediationPersistenceManager pm = ServiceBusUtils.getMediationPersistenceManager(axisCfg);
			pm.saveItem(startup.getName(), ServiceBusConstants.ITEM_TYPE_TASK);
		}
	}

	private static void handleException(String msg) {
		log.error(msg);
		throw new IllegalArgumentException(msg);
	}
}
