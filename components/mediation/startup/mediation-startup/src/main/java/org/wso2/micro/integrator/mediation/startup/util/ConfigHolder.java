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

package org.wso2.micro.integrator.mediation.startup.util;

import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.Map;
import java.util.HashMap;

public class ConfigHolder {
	private static final ConfigHolder configHolder = new ConfigHolder();

	private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices =
			new HashMap<Integer, SynapseEnvironmentService>();

	private ConfigHolder() {
	}

	public static ConfigHolder getInstance() {
		return configHolder;
	}

	public SynapseConfiguration getSynapseConfiguration(int id) {
		SynapseEnvironmentService synEnvService = synapseEnvironmentServices.get(id);
		if (synEnvService != null) {
			synEnvService.getSynapseEnvironment().getSynapseConfiguration();
		}

		return null;
	}

	public SynapseEnvironmentService getSynapseEnvironmentService(int id) {
		return synapseEnvironmentServices.get(id);
	}

	public void addSynapseEnvironmentService(int id, SynapseEnvironmentService synapseEnvironmentService) {
		synapseEnvironmentServices.put(id, synapseEnvironmentService);
	}

	public void removeSynapseEnvironmentService(int id) {
		synapseEnvironmentServices.remove(id);
	}

	public Map<Integer, SynapseEnvironmentService> getSynapseEnvironmentServices() {
		return synapseEnvironmentServices;
	}
}
