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
package org.wso2.micro.integrator.dataservices.core.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.description.config.CSVConfig;
import org.wso2.micro.integrator.dataservices.core.description.config.Config;
import org.wso2.micro.integrator.dataservices.core.description.config.ExcelConfig;
import org.wso2.micro.integrator.dataservices.core.description.config.GSpreadConfig;
import org.wso2.micro.integrator.dataservices.core.description.config.JNDIConfig;
import org.wso2.micro.integrator.dataservices.core.description.config.RDBMSConfig;
import org.wso2.micro.integrator.dataservices.core.description.config.SQLCarbonDataSourceConfig;
import org.wso2.micro.integrator.dataservices.core.description.config.SQLConfig;
import org.wso2.micro.integrator.dataservices.core.description.operation.Operation;
import org.wso2.micro.integrator.dataservices.core.description.query.Query;
import org.wso2.micro.integrator.dataservices.core.description.resource.Resource;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;

/**
 * JMX MBean implementation to represent a data service.
 * @see DataServiceInstanceMBean
 */
public class DataServiceInstance implements DataServiceInstanceMBean {

	private DataService dataService;
	
	public DataServiceInstance(DataService dataService) {
		this.dataService = dataService;
	}
	
	public DataService getDataService() {
		return dataService;
	}
	
	public String getServiceName() {
		return this.getDataService().getName();
	}
	
	public String getDataServiceDescriptorPath() {
		return this.getDataService().getDsLocation();
	}
	
	public String[] getConfigIds() {
		return this.getDataService().getConfigs().keySet().toArray(new String[0]);
	}
	
	public String[] getQueryIds() {
		return this.getDataService().getQueries().keySet().toArray(new String[0]);
	}
	
	public String[] getOperationNames() {
		return this.getDataService().getOperationNames().toArray(new String[0]);
	}
	
	public String[] getResourcePaths() {
		Set<Resource.ResourceID> resourceIds = this.getDataService().getResourceIds();
		List<String> list = new ArrayList<String>();
		for (Resource.ResourceID resourceId : resourceIds) {
			list.add(resourceId.getPath());
		}
		return list.toArray(new String[0]);
	}
	
	public String[] getHTTPMethodsForResourcePath(String resPath) {
		Set<Resource.ResourceID> resourceIds = this.getDataService().getResourceIds();
		List<String> list = new ArrayList<String>();
		for (Resource.ResourceID resourceId : resourceIds) {
			if (resourceId.getPath().equals(resPath)) {
			    list.add(resourceId.getMethod());
			}
		}
		return list.toArray(new String[0]);
	}
	
	public String getQueryIdFromOperationName(String operationName) {
		Operation op = this.getDataService().getOperation(operationName);
		if (op != null) {
			return op.getCallQuery().getQueryId();
		} else {
			return null;
		}
	}
	
	public String getConfigIdFromQueryId(String queryId) {
		Query query = this.getDataService().getQuery(queryId);
		if (query != null) {
			return query.getConfigId();
		} else {
			return null;
		}
	}

	public boolean isConfigActive(String configId) {
		Config config = this.getDataService().getConfig(configId);
		if (config != null) {
			return config.isActive();
		} else {
		    return false;
		}
	}
	
	public String getConfigTypeFromId(String configId) {
		Config config = this.getDataService().getConfig(configId);
		if (config instanceof RDBMSConfig) {
			return DBConstants.DataSourceTypes.RDBMS;
		} else if (config instanceof JNDIConfig) {
			return DBConstants.DataSourceTypes.JNDI;
		} else if (config instanceof SQLCarbonDataSourceConfig) {
			return DBConstants.DataSourceTypes.CARBON;
		} else if (config instanceof ExcelConfig) {
			return DBConstants.DataSourceTypes.EXCEL;
		} else if (config instanceof CSVConfig) {
			return DBConstants.DataSourceTypes.CSV;
		} else if (config instanceof GSpreadConfig) {
			return DBConstants.DataSourceTypes.GDATA_SPREADSHEET;
		} else {
		    return null;
		}
	}
	
	public boolean isDatabaseConnectionStatsAvailable(String configId) {
		Config config = this.getDataService().getConfig(configId);
		if (config instanceof SQLConfig) {
			SQLConfig sqlConfig = (SQLConfig) config;
			try {
			    return sqlConfig.isStatsAvailable();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			return false;
		}
	}
	
	public int getOpenDatabaseConnectionsCount(String configId) {
		Config config = this.getDataService().getConfig(configId);
		if (config instanceof SQLConfig) {
			SQLConfig sqlConfig = (SQLConfig) config;
			try {
			    return sqlConfig.getActiveConnectionCount();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			return -1;
		}
	}
	
}
