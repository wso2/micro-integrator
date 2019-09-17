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

import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * This class represents a JNDI based data source configuration.
 */
public class JNDIConfig extends SQLConfig {

	private DataSource dataSource;

	public JNDIConfig(DataService dataService, String configId, Map<String, String> properties, boolean odataEnable)
            throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.JNDI, properties, odataEnable);
		if (!dataService.isServiceInactive()) {
			this.validateJNDIConfig();
		    this.dataSource = this.createDataSource();
		    try {
			    this.initSQLDataSource();
		    } catch (SQLException e) {
			    throw new DataServiceFault(e, DBConstants.FaultCodes.CONNECTION_UNAVAILABLE_ERROR,
			    		e.getMessage());
		    }
		}
	}
	
	private void validateJNDIConfig() throws DataServiceFault {
        if (DBUtils.isEmptyString(this.getProperty(DBConstants.JNDI.RESOURCE_NAME))) {
        	throw new DataServiceFault("Resource name cannot be null in config '" +
					this.getConfigId() + "'");
		}
	}
	
	private DataSource createDataSource() throws DataServiceFault {
		Properties properties = new Properties();
		String username = this.getProperty(DBConstants.JNDI.USERNAME);
		String password = DBUtils.resolvePasswordValue(this.getDataService(),
				this.getProperty(DBConstants.JNDI.PASSWORD));
		String factoryClass = this.getProperty(DBConstants.JNDI.INITIAL_CONTEXT_FACTORY);
		String contextUrl = this.getProperty(DBConstants.JNDI.PROVIDER_URL);
		String resourceName = this.getProperty(DBConstants.JNDI.RESOURCE_NAME);
		try {
			if (username != null && username.trim().length() > 0) {
				properties.setProperty(Context.SECURITY_PRINCIPAL, username.trim());
			}
			if (password != null && password.trim().length() > 0) {
				properties.setProperty(Context.SECURITY_CREDENTIALS, password.trim());
			}
			if (factoryClass != null && factoryClass.trim().length() > 0) {
			    properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryClass.trim());
			}
			if (contextUrl != null && contextUrl.trim().length() > 0) {
			    properties.setProperty(Context.PROVIDER_URL, contextUrl.trim());
			}
			if (resourceName != null && resourceName.trim().length() > 0) {
			    InitialContext context = new InitialContext(properties);
			    DataSource dataSource = (DataSource) context.lookup(resourceName);
			    return dataSource;
			} else {
				throw new DataServiceFault("JNDI resource name not specified.");
			}
		} catch (NamingException e) {
			throw new DataServiceFault(e,
					"Naming error occurred while trying to retrieve JDBC Connection from JNDI tree.");
		}
	}

	@Override
	public DataSource getDataSource() throws DataServiceFault {
		if (this.dataSource == null) {
		    synchronized (this) {
		    	if (this.dataSource == null) {
		    	    this.dataSource = this.createDataSource();
		    	}
		    }
		}
		return dataSource;
	}

	@Override
	public boolean isStatsAvailable() {
		return false;
	}

	@Override
	public int getActiveConnectionCount() {
		return -1;
	}

	@Override
	public int getIdleConnectionCount() {
		return -1;
	}

	@Override
	public void close() {
	}

	@Override
	public ODataDataHandler createODataHandler() throws ODataServiceFault {
		throw new ODataServiceFault("Expose as OData Service feature doesn't support for the " + getConfigId() +
		                           " Datasource.");
	}
		
}
