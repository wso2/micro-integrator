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

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;
import org.wso2.micro.integrator.ndatasource.core.utils.DataSourceUtils;

/**
 * This class represents data source meta information.
 */
@XmlRootElement (name = "datasource")
@XmlType (propOrder = {"name", "description", "jndiConfig", "definition"})
public class DataSourceMetaInfo {

	private String name;
	
	private String description;
	
	private JNDIConfig jndiConfig;

	private DataSourceDefinition definition;

	@XmlTransient
	private boolean system;

    @XmlTransient
    private boolean carbonApplicationDeployed;

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setJndiConfig(JNDIConfig jndiConfig) {
		this.jndiConfig = jndiConfig;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

    public void setCarbonApplicationDeployed(boolean carbonApplicationDeployed) {
        this.carbonApplicationDeployed = carbonApplicationDeployed;
    }

	@XmlElement (name = "name", required = true, nillable = false)
	public String getName() {
		return name;
	}

	@XmlElement (name = "description")
	public String getDescription() {
		return description;
	}

	@XmlElement (name = "jndiConfig")
	public JNDIConfig getJndiConfig() {
		return jndiConfig;
	}

	@XmlTransient
	public boolean isSystem() {
		return system;
	}

    @XmlTransient
    public boolean isCarbonApplicationDeployed() {
        return  carbonApplicationDeployed;
    }

	@XmlElement (name = "definition", required = true, nillable = false)
	public DataSourceDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(DataSourceDefinition definition) {
		this.definition = definition;
	}

    @XmlTransient
    public boolean isPersistable() {
        return (!system && !carbonApplicationDeployed);
    }

	@XmlRootElement (name = "definition")
	public static class DataSourceDefinition {
		
		private String type;
		
		private Object dsXMLConfiguration;

		@XmlAttribute (name = "type", required = true)
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@XmlAnyElement
		public Object getDsXMLConfiguration() {
			return dsXMLConfiguration;
		}

		public void setDsXMLConfiguration(Object dsXMLConfiguration) {
			this.dsXMLConfiguration = dsXMLConfiguration;
		}
		
		public boolean equals(Object rhs) {
			if (!(rhs instanceof DataSourceDefinition)) {
				return false;
			}
			DataSourceDefinition dsDef = (DataSourceDefinition) rhs;
			if (!DataSourceUtils.nullAllowEquals(dsDef.getType(), this.getType())) {
				return false;
			}
			if (!DataSourceUtils.nullAllowEquals(DataSourceUtils.elementToString(
					(Element) dsDef.getDsXMLConfiguration()),
                                                 DataSourceUtils.elementToString((Element) this.getDsXMLConfiguration()))) {
				return false;
			}
			return true;
		}
		
		@Override
		public int hashCode() {
			assert false : "hashCode() not implemented";
		    return -1;
		}
		
	}
	
	@Override
	public boolean equals(Object rhs) {
		if (!(rhs instanceof DataSourceMetaInfo)) {
			return false;
		}
		DataSourceMetaInfo
                dsmInfo = (DataSourceMetaInfo) rhs;
		if (!DataSourceUtils.nullAllowEquals(dsmInfo.getName(), this.getName())) {
			return false;
		}
		if (!DataSourceUtils.nullAllowEquals(dsmInfo.getDescription(), this.getDescription())) {
			return false;
		}
		if (!DataSourceUtils.nullAllowEquals(dsmInfo.getJndiConfig(), this.getJndiConfig())) {
			return false;
		}
		if (!DataSourceUtils.nullAllowEquals(dsmInfo.getDefinition(), this.getDefinition())) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		assert false : "hashCode() not implemented";
	    return -1;
	}
	
}
