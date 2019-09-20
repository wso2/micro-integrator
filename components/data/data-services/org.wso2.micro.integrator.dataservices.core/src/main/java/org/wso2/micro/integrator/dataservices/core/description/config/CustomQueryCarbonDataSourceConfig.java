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
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.datasource.CustomQueryBasedDS;import org.wso2.micro.integrator.dataservices.core.datasource.CustomQueryDataSourceReader;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.internal.DataServicesDSComponent;import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceService;

import java.util.Map;

/**
 * This class represents an custom query based Carbon Data Source data source configuration.
 */
public class CustomQueryCarbonDataSourceConfig extends CustomQueryBasedDSConfig {

	private String dataSourceName;
	
	private CustomQueryBasedDS dataSource;

	public CustomQueryCarbonDataSourceConfig(DataService dataService, String configId, Map<String, String> properties,
	                                         boolean odataEnable) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.CUSTOM_QUERY, properties, odataEnable);
		this.dataSourceName = properties.get(DBConstants.CarbonDatasource.NAME);
		this.dataSource = this.initDataSource();
	}

	@Override
	public CustomQueryBasedDS getDataSource() {
		return dataSource;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}
	
	private CustomQueryBasedDS initDataSource() throws DataServiceFault {
        DataSourceService dataSourceService = DataServicesDSComponent.getDataSourceService();
        if (dataSourceService == null) {
            throw new DataServiceFault("Carbon DataSource Service is not initialized properly");
        }
        CarbonDataSource cds;
		try {
			cds = dataSourceService.getDataSource(this.getDataSourceName());
			if (cds == null) {
				throw new DataServiceFault("Cannot find data source with the name: " +
						this.getDataSourceName());
			}
			String dsType = cds.getDSMInfo().getDefinition().getType();
			if (CustomQueryDataSourceReader.DATA_SOURCE_TYPE.equals(dsType)) {
			    Object result = cds.getDSObject();
			    if (!(result instanceof CustomQueryBasedDS)) {
				    throw new DataServiceFault("The data source '" +
				    		this.getDataSourceName() + "' is not of type '" + 
						    CustomQueryDataSourceReader.DATA_SOURCE_TYPE + "'");
			    }
			    return (CustomQueryBasedDS) result;
			} else {
				throw new DataServiceFault("The type '" + dsType + "' of data source '" +
						this.getDataSourceName() + 
						"' is not supported in CustomQueryCarbonDataSourceConfig");
			}
		} catch (DataSourceException e) {
			throw new DataServiceFault(e, "Error in retrieving data source: " + e.getMessage());
		}
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public ODataDataHandler createODataHandler() throws ODataServiceFault {
		throw new ODataServiceFault("Expose as OData Service feature doesn't support for the " + getConfigId() +
		                           " Datasource.");
	}

	@Override
	public boolean isResultSetFieldsCaseSensitive() {
		return false;
	}
}
