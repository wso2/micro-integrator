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
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.JDBCPoolSQLConfig;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;import org.wso2.micro.integrator.dataservices.core.odata.RDBMSDataHandler;

import java.sql.SQLException;
import java.util.Map;

/**
 * This class represents a RDBMS based data source configuration.
 */
public class RDBMSConfig extends JDBCPoolSQLConfig {

	public RDBMSConfig(DataService dataService, String configId, Map<String, String> properties, boolean odataEnable)
            throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.RDBMS, properties, odataEnable);
		if (!dataService.isServiceInactive()) {
			try {
				this.initSQLDataSource();
			} catch (SQLException e) {
				throw new DataServiceFault(e, DBConstants.FaultCodes.CONNECTION_UNAVAILABLE_ERROR, e.getMessage());
			}
		}
	}

	public RDBMSConfig(DataService dataService, String configId, Map<String, String> properties)
			throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.RDBMS, properties, false);
		if (!dataService.isServiceInactive()) {
			try {
				this.initSQLDataSource();
			} catch (SQLException e) {
				throw new DataServiceFault(e, DBConstants.FaultCodes.CONNECTION_UNAVAILABLE_ERROR, e.getMessage());
			}
		}
	}

	@Override
	public ODataDataHandler createODataHandler() throws DataServiceFault {
		return new RDBMSDataHandler(getDataSource(), getConfigId());
	}

}
