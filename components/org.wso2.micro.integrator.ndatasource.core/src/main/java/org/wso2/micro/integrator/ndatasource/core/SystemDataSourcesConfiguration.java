/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.micro.integrator.ndatasource.core;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the system data sources configuration.
 */
@XmlRootElement (name = "datasources-configuration")
public class SystemDataSourcesConfiguration {

	private List<DataSourceMetaInfo> dataSources;
	
	private List<String> providers;

	@XmlElementWrapper (name = "providers")
	@XmlElement (name = "provider", nillable = false)
	public List<String> getProviders() {
		return providers;
	}

	public void setProviders(List<String> providers) {
		this.providers = providers;
	}

	@XmlElementWrapper (name = "datasources")
	@XmlElement (name = "datasource", nillable = false)
	public List<DataSourceMetaInfo> getDataSources() {
		return dataSources;
	}

	public void setDataSources(List<DataSourceMetaInfo> dataSources) {
		this.dataSources = dataSources;
	}
	
}
