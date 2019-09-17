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
package org.wso2.micro.integrator.dataservices.core.description.config;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;

import java.util.Map;

/**
 * Represents a config in a data service.
 */
public abstract class Config {

	private String configId;
	
	private String type;
	
	private Map<String, String> properties;
	
	private DataService dataService;

	private boolean oDataEnabled;

	public Config(DataService dataService, String configId, String type, Map<String, String> properties, boolean oDataEnabled) {
		this.dataService = dataService;
		this.configId = configId;
		this.type = type;
		this.properties = properties;
		this.oDataEnabled = oDataEnabled;
	}
	
	public DataService getDataService() {
		return dataService;
	}
	
	public String getProperty(String name) {
		return this.properties.get(name);
	}
	
	public Map<String, String> getProperties() {
		return this.properties;
	}

	public String getConfigId() {
		return configId;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "Type:" + this.getType() + properties.toString();
	}
	
	public abstract boolean isActive();
	
	public abstract void close();

	public abstract ODataDataHandler createODataHandler() throws DataServiceFault;

	public boolean isODataEnabled() {
		return oDataEnabled;
	}

	public abstract boolean isResultSetFieldsCaseSensitive();
}
