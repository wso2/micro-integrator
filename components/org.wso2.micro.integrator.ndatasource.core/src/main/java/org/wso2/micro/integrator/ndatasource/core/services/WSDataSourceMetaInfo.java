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
package org.wso2.micro.integrator.ndatasource.core.services;

import org.w3c.dom.Element;
import org.wso2.micro.integrator.ndatasource.core.DataSourceMetaInfo;
import org.wso2.micro.integrator.ndatasource.core.DataSourceMetaInfo.DataSourceDefinition;
import org.wso2.micro.integrator.ndatasource.core.JNDIConfig;
import org.wso2.micro.integrator.ndatasource.core.utils.DataSourceUtils;

/**
 * This is a bean class to contain the DataSourceMetaInfo in a web services context.
 */
public class WSDataSourceMetaInfo {

	private String name;
	
	private String description;
	
	private JNDIConfig jndiConfig;
	
	private boolean system;
	
	private WSDataSourceDefinition definition;
	
	public WSDataSourceMetaInfo() {
	}
	
	public WSDataSourceMetaInfo(DataSourceMetaInfo metaInfo) {
		this.name = metaInfo.getName();
		this.description = metaInfo.getDescription();
		this.jndiConfig = metaInfo.getJndiConfig();
		this.system = !metaInfo.isPersistable();
		this.definition = new WSDataSourceDefinition(metaInfo.getDefinition());
	}
	
	public DataSourceMetaInfo extractDataSourceMetaInfo() {
		DataSourceMetaInfo dsmInfo = new DataSourceMetaInfo();
		dsmInfo.setName(this.getName());
		dsmInfo.setDescription(this.getDescription());
		dsmInfo.setSystem(this.isSystem());
		dsmInfo.setJndiConfig(this.getJndiConfig());
		dsmInfo.setDefinition(this.getDefinition().extractDataSourceDefinition());
		return dsmInfo;
	}
	
	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JNDIConfig getJndiConfig() {
		return jndiConfig;
	}

	public void setJndiConfig(JNDIConfig jndiConfig) {
		this.jndiConfig = jndiConfig;
	}

	public WSDataSourceDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(WSDataSourceDefinition definition) {
		this.definition = definition;
	}

	/**
	 * This is a bean class to contain the DataSourceDefinition in a web services context.
	 */
	public static class WSDataSourceDefinition {
		
        private String type;
		
		private String dsXMLConfiguration;
		
		public WSDataSourceDefinition() {	
		}
		
        public WSDataSourceDefinition(DataSourceDefinition dsDef) {
			this.type = dsDef.getType();
			this.dsXMLConfiguration = DataSourceUtils.elementToString((Element) 
					dsDef.getDsXMLConfiguration());
		}
        
        public DataSourceDefinition extractDataSourceDefinition() {
        	DataSourceDefinition dsDef = new DataSourceDefinition();
        	dsDef.setType(this.getType());
        	dsDef.setDsXMLConfiguration(DataSourceUtils.stringToElement(
        			this.getDsXMLConfiguration()));
        	return dsDef;
        }

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDsXMLConfiguration() {
			return dsXMLConfiguration;
		}

		public void setDsXMLConfiguration(String dsXMLConfiguration) {
			this.dsXMLConfiguration = dsXMLConfiguration;
		}
		
	}
	
}
