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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.core.description.config.TabularDataBasedConfig.CustomSQLDataSource;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.datasource.CustomQueryDataSourceReader;
import org.wso2.micro.integrator.dataservices.core.datasource.CustomTabularDataSourceReader;
import org.wso2.micro.integrator.dataservices.core.datasource.TabularDataBasedDS;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;
import org.wso2.micro.integrator.dataservices.core.odata.RDBMSDataHandler;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceService;
import org.wso2.micro.integrator.ndatasource.rdbms.RDBMSDataSourceConstants;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents an SQL based Carbon Data Source data source configuration.
 */
public class SQLCarbonDataSourceConfig extends SQLConfig {

	private static final Log log = LogFactory.getLog(
            SQLCarbonDataSourceConfig.class);
	
	private DataSource dataSource;
	
	private String dataSourceName;

	public SQLCarbonDataSourceConfig(DataService dataService, String configId, Map<String, String> properties,
                                     boolean odataEnable) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.CARBON, properties, odataEnable);
		this.dataSourceName = properties.get(DBConstants.CarbonDatasource.NAME);
        this.dataSource = initDataSource();
        if (!dataService.isServiceInactive()) {
            try {
                this.initSQLDataSource();
            } catch (SQLException e) {
                throw new DataServiceFault(e, DBConstants.FaultCodes.CONNECTION_UNAVAILABLE_ERROR,
                                           e.getMessage());
            }
        }
	}

	public SQLCarbonDataSourceConfig(DataService dataService, String configId, Map<String, String> properties)
            throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.CARBON, properties, false);
        this.dataSourceName = properties.get(DBConstants.CarbonDatasource.NAME);
        this.dataSource = initDataSource();
        if (!dataService.isServiceInactive()) {
            try {
                this.initSQLDataSource();
            } catch (SQLException e) {
                throw new DataServiceFault(e, DBConstants.FaultCodes.CONNECTION_UNAVAILABLE_ERROR,
                                           e.getMessage());
            }
        }
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public String getDataSourceName() {
		return dataSourceName;
	}

	private DataSource initDataSource() throws DataServiceFault {
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
			if (RDBMSDataSourceConstants.RDBMS_DATASOURCE_TYPE.equals(dsType)) {
			    Object result = cds.getDSObject();
			    if (!(result instanceof DataSource)) {
				    throw new DataServiceFault("The data source '" + this.getDataSourceName() +
						    "' is not of type RDBMS");
			    }
			    return (DataSource) result;
			} else if (CustomTabularDataSourceReader.DATA_SOURCE_TYPE.equals(dsType)) {
				return new CustomSQLDataSource((TabularDataBasedDS) cds.getDSObject());
			} else {
				throw new DataServiceFault("The type '" + dsType + "' of data source '" +
						this.getDataSourceName() + "' is not supported in SQLCarbonDataSourceConfig");
			}
		} catch (DataSourceException e) {
			throw new DataServiceFault(e, "Error in retrieving data source: " + e.getMessage());
		}        
	}
	
	public static List<String> getCarbonDataSourceNames() {
		DataSourceService dataSourceService = DataServicesDSComponent.getDataSourceService();
		if (dataSourceService == null) {
			log.error("CarbonDataSourceConfig.getCarbonDataSourceNames(): " +
                    "Carbon data source service is not available, returning empty list");
			return new ArrayList<String>();
		}
		try {
		    List<CarbonDataSource> dsList = dataSourceService.getAllDataSourcesForType(
				    RDBMSDataSourceConstants.RDBMS_DATASOURCE_TYPE);
		    dsList.addAll(dataSourceService.getAllDataSourcesForType(
                    CustomTabularDataSourceReader.DATA_SOURCE_TYPE));
		    dsList.addAll(dataSourceService.getAllDataSourcesForType(
                    CustomQueryDataSourceReader.DATA_SOURCE_TYPE));
		    List<String> result = new ArrayList<String>(dsList.size());
		    for (CarbonDataSource cds : dsList) {
		    	result.add(cds.getDSMInfo().getName());
		    }
		    return result;
		} catch (Exception e) {
			log.error("Error retrieving data source list, returning empty list: " + 
		            e.getMessage(), e);
			return new ArrayList<String>();
		}
	}
	
	public static List<String> getCarbonDataSourceNamesForType(String[] types) {
		DataSourceService dataSourceService = DataServicesDSComponent.getDataSourceService();
		if (dataSourceService == null) {
			log.error("CarbonDataSourceConfig.getCarbonDataSourceNames(): " +
                    "Carbon data source service is not available, returning empty list");
			return new ArrayList<String>();
		}
		try {
			List<CarbonDataSource> dsList = new ArrayList<CarbonDataSource>();
			for (String type : types) {
				dsList.addAll(dataSourceService.getAllDataSourcesForType(type));
			}
		    List<String> result = new ArrayList<String>(dsList.size());
		    for (CarbonDataSource cds : dsList) {
		    	result.add(cds.getDSMInfo().getName());
		    }
		    return result;
		} catch (Exception e) {
			log.error("Error retrieving data source list, returning empty list: " + 
		            e.getMessage(), e);
			return new ArrayList<String>();
		}
	}
	
	public static String getCarbonDataSourceType(String dsName) {
		DataSourceService dataSourceService = DataServicesDSComponent.getDataSourceService();
		if (dataSourceService == null) {
			log.error("CarbonDataSourceConfig.getCarbonDataSourceNames(): " +
                    "Carbon data source service is not available, returning empty type");
			return null;
		}
		try {
            CarbonDataSource carbonDataSource = dataSourceService.getDataSource(dsName);
            if (carbonDataSource == null) {
                log.error("Carbon datasource [" + dsName + "] is null");
                return null;
            }
            return carbonDataSource.getDSMInfo().getDefinition().getType();
		} catch (Exception e) {
			log.error("Error retrieving data source type, returning empty type: " + 
		            e.getMessage(), e);
			return null;
		}
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
	public boolean isStatsAvailable() {
		return false;
	}

	public void close() {
		/* nothing to close */
	}

	@Override
	public ODataDataHandler createODataHandler() throws ODataServiceFault {
		return new RDBMSDataHandler(getDataSource(), getConfigId());
	}
}

