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
package org.wso2.micro.integrator.dataservices.core.description.xa;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.XADataSource;

import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;

/**
 * Represents an RDBMS-XADataSource in a data service.
 */
public class XADataSourceInfo {

	private XADataSource xaDataSource;
		
	private String className;
	
	private Map<String, String>	properties;
	
	private DataService dataService;
	
	public XADataSourceInfo(DataService dataService, String className,
			Map<String, String> properties) throws DataServiceFault {
		this.className = className;
		this.properties = properties;
		this.dataService = dataService;
		if (!this.dataService.isServiceInactive()) {
			this.initXA();
		}
	}
	
	private void initXA() throws DataServiceFault {
		try {
			this.xaDataSource = (XADataSource) Class.forName(this.getClassName()).newInstance();
			this.assignProps(this.getXADataSource(), this.getProperties());
		} catch (Exception e) {
			throw new DataServiceFault(e,
					"Cannot create XADataSource instance using the class: "
							+ this.getClassName());
		}
	}
	
	/**
	 * Assigns the given values to the object as Java bean properties.
	 * @param obj The object to be used to assign the properties
	 * @param props A map of properties to be assigned
	 * @throws DataServiceFault
	 */
	private void assignProps(Object obj, Map<String, String> props) throws DataServiceFault {
		Method method;
		for (Entry<String, String> prop : props.entrySet()) {
			method = getSetterMethod(obj, this.getSetterMethodNameFromPropName(prop.getKey()));
			if (method == null) {
				throw new DataServiceFault("Setter method for property '" + prop.getKey()
						+ "' cannot be found");
			}
			try {
			    method.invoke(obj, convertStringToGivenType(prop.getValue(), 
			    		method.getParameterTypes()[0]));
			} catch (Exception e) {
				throw new DataServiceFault(e, "Cannot invoke setter for property '" +
						prop.getKey() + "'");
			}
		}
	}
	
	private Object convertStringToGivenType(String value, Class<?> type) throws DataServiceFault {
		if (String.class.equals(type)) {
			return value;
		}
		if (boolean.class.equals(type) || Boolean.class.equals(type)) {
			return Boolean.parseBoolean(value);
		}
		if (int.class.equals(type) || Integer.class.equals(type)) {
			return Integer.parseInt(value);
		}
		if (short.class.equals(type) || Short.class.equals(type)) {
			return Short.parseShort(value);
		}
		if (byte.class.equals(type) || Byte.class.equals(type)) {
			return Byte.parseByte(value);
		}
		if (long.class.equals(type) || Long.class.equals(type)) {
			return Long.parseLong(value);
		}
		if (float.class.equals(type) || Float.class.equals(type)) {
			return Float.parseFloat(value);
		}
		if (double.class.equals(type) || Double.class.equals(type)) {
			return Double.parseDouble(value);
		}		
		throw new DataServiceFault("Cannot convert value: '" +
				value + "' to type: '" + type.getName() + "'");
	}
	
	private String getSetterMethodNameFromPropName(String propName) throws RuntimeException {
		if (DBUtils.isEmptyString(propName)) {
			throw new RuntimeException("Invalid property name");
		}
		return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
	}
	
	private Method getSetterMethod(Object obj, String name) {
		Method[] methods = obj.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals(name)
					&& method.getReturnType().equals(void.class)
					&& method.getParameterTypes().length == 1) {
				return method;
			}
		}
		return null;
	}

	public XADataSource getXADataSource() {
		return xaDataSource;
	}
	
	public String getClassName() {
		return className;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public DataService getDataService() {
		return dataService;
	}
	
}
