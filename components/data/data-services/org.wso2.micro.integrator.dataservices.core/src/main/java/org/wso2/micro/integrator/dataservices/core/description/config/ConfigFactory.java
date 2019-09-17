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

import org.apache.axiom.om.OMElement;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.CSV;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.common.DBConstants.Excel;
import org.wso2.micro.integrator.dataservices.common.DBConstants.GSpread;
import org.wso2.micro.integrator.dataservices.common.DBConstants.MongoDB;
import org.wso2.micro.integrator.dataservices.common.DBConstants.RDBMS;
import org.wso2.micro.integrator.dataservices.common.DBConstants.RDBMS_OLD;
import org.wso2.micro.integrator.dataservices.common.DBConstants.RDF;
import org.wso2.micro.integrator.dataservices.common.DBConstants.SPARQL;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.datasource.CustomQueryDataSourceReader;
import org.wso2.micro.integrator.dataservices.core.datasource.CustomTabularDataSourceReader;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceService;
import org.wso2.micro.integrator.ndatasource.rdbms.RDBMSDataSourceConstants;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * A factory class to create config objects to the given data.
 */
public class ConfigFactory {

	private ConfigFactory() { }
	
	public static Config createConfig(DataService dataService, OMElement configEl)
            throws DataServiceFault {
		Map<String, String> properties = DBUtils.extractProperties(configEl);
		String configId = getConfigId(configEl);
		String configType = getConfigType(properties);
		boolean odataEnable = isODataEnable(configEl);
		if (DataSourceTypes.RDBMS.equals(configType)) {
			return getRDBMSConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.JNDI.equals(configType)) {
			return getJNDIConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.MONGODB.equals(configType)) {
			return getMongoConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.EXCEL.equals(configType)) {
			return getExcelConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.RDF.equals(configType)) {
			return getRDFConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.SPARQL.equals(configType)) {
			return getSparqlEndpointConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.CSV.equals(configType)) {
			return getCSVConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.GDATA_SPREADSHEET.equals(configType)) {
			return getGSpreadConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.CARBON.equals(configType)) {
			return getCarbonDataSourceConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.WEB.equals(configType)) {
			return getWebConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.CUSTOM_TABULAR.equals(configType)) {
			return getCustomTabularConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.CUSTOM_QUERY.equals(configType)) {
			return getCustomQueryConfig(dataService, configId, properties, odataEnable);
		} else if (DataSourceTypes.CASSANDRA.equals(configType)) {
			return getCassandraConfig(dataService, configId, properties, odataEnable);
		}
		
		return null;
	}

	private static RDBMSConfig getRDBMSConfig(DataService dataService, String configId, Map<String, String> properties,
                                              boolean odataEnable) throws DataServiceFault {
		RDBMSConfig config = new RDBMSConfig(dataService, configId, properties, odataEnable);
		return config;
	}

	private static JNDIConfig getJNDIConfig(DataService dataService, String configId, Map<String, String> properties,
                                            boolean odataEnable) throws DataServiceFault {
		JNDIConfig config = new JNDIConfig(dataService, configId, properties, odataEnable);
		return config;
	}

	private static MongoConfig getMongoConfig(DataService dataService, String configId, Map<String, String> properties,
                                              boolean odataEnable) throws DataServiceFault {
		MongoConfig config = new MongoConfig(dataService, configId, properties, odataEnable);
		return config;
	}

	private static ExcelConfig getExcelConfig(DataService dataService, String configId, Map<String, String> properties,
                                              boolean odataEnable) throws DataServiceFault {
		ExcelConfig config = new ExcelConfig(dataService, configId, properties, odataEnable);
		return config;
	}

	private static RDFConfig getRDFConfig(DataService dataService, String configId, Map<String, String> properties,
                                          boolean odataEnable) throws DataServiceFault {
		RDFConfig config = new RDFConfig(dataService, configId, properties, odataEnable);
		return config;
	}

    private static SparqlEndpointConfig getSparqlEndpointConfig(DataService dataService, String configId,
                                                                Map<String, String> properties, boolean odataEnable) throws
                                                                                                                     DataServiceFault {
		SparqlEndpointConfig config = new SparqlEndpointConfig(dataService, configId, properties, odataEnable);
		return config;
	}

	private static CSVConfig getCSVConfig(DataService dataService, String configId,
                                          Map<String, String> properties, boolean odataEnable) throws DataServiceFault {
		CSVConfig config = new CSVConfig(dataService, configId, properties, odataEnable);
		return config;
	}

	private static CassandraConfig getCassandraConfig(DataService dataService, String configId,
                                                      Map<String, String> properties, boolean odataEnable) throws
                                                                                                           DataServiceFault {
		CassandraConfig config = new CassandraConfig(dataService, configId, properties, odataEnable);
		return config;
	}

    private static WebConfig getWebConfig(DataService dataService, String configId,
                                          Map<String, String> properties, boolean odataEnable) throws DataServiceFault {
        WebConfig config = new WebConfig(dataService, configId, properties, odataEnable);
        return config;
    }

    private static TabularDataBasedConfig getCustomTabularConfig(DataService dataService, String configId,
                                                                 Map<String, String> properties, boolean odataEnable) throws
                                                                                                                      DataServiceFault {
        TabularDataBasedConfig config = new TabularDataBasedConfig(dataService, configId, properties, odataEnable);
        return config;
    }

    private static InlineCustomQueryBasedDSConfig getCustomQueryConfig(DataService dataService, String configId,
                                                                       Map<String, String> properties, boolean odataEnable) throws
                                                                                                                            DataServiceFault {
    	InlineCustomQueryBasedDSConfig config = new InlineCustomQueryBasedDSConfig(dataService, configId, properties, odataEnable);
        return config;
    }

	private static GSpreadConfig getGSpreadConfig(DataService dataService, String configId,
                                                  Map<String, String> properties, boolean odataEnable) throws
                                                                                                       DataServiceFault {
		GSpreadConfig config = new GSpreadConfig(dataService, configId, properties, odataEnable);
		return config;
	}
	
	private static Config getCarbonDataSourceConfig(DataService dataService,
                                                    String configId, Map<String, String> properties, boolean odataEnable) throws
                                                                                                                          DataServiceFault {
		DataSourceService dataSourceService = DataServicesDSComponent.getDataSourceService();
		try {
			String name = properties.get(DBConstants.CarbonDatasource.NAME);
		    CarbonDataSource cds = dataSourceService.getDataSource(name);
		    if (cds == null) {
		    	throw new DataServiceFault("The Carbon data source '" + name + "' cannot be found");
		    }
		    String dsType = cds.getDSMInfo().getDefinition().getType();
		    if (RDBMSDataSourceConstants.RDBMS_DATASOURCE_TYPE.equals(dsType) ||
		    		CustomTabularDataSourceReader.DATA_SOURCE_TYPE.equals(dsType)) {
			    return new SQLCarbonDataSourceConfig(dataService, configId, properties, odataEnable);
		    } else if (CustomQueryDataSourceReader.DATA_SOURCE_TYPE.equals(dsType)) {
		    	return new CustomQueryCarbonDataSourceConfig(dataService, configId, properties, odataEnable);
		    } else {
		    	throw new DataServiceFault("Unsupported Carbon data source type '" + dsType + "'");
		    }
		} catch (DataServiceFault e) {
			throw e;
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in creating Carbon data source: " +
					e.getMessage());
		}
	}
	
	private static String getConfigId(OMElement configEl) {
		String configId = configEl.getAttributeValue(new QName(DBSFields.ID));
		if (configId == null) {
			configId = DBConstants.DEFAULT_CONFIG_ID;
		}
		return configId;
	}

	private static boolean isODataEnable(OMElement configEl) {
		String odataConfig = configEl.getAttributeValue(new QName(DBSFields.ENABLE_ODATA));
		if (odataConfig == null) {
			return false;
		} else {
			return Boolean.valueOf(odataConfig);
		}
	}

	private static String getConfigType(Map<String, String> properties) throws DataServiceFault {
		if ((properties.get(RDBMS.DRIVER_CLASSNAME) != null) || (properties.get(RDBMS_OLD.DRIVER) != null) ||
				(properties.get(RDBMS.DATASOURCE_CLASSNAME) != null) || 
				(properties.get(RDBMS_OLD.XA_DATASOURCE_CLASS) != null)) {
		    return DataSourceTypes.RDBMS;
		} else if (properties.get(Excel.DATASOURCE) != null) {
		    return DataSourceTypes.EXCEL;
		} else if  (properties.get(RDF.DATASOURCE) != null) {
		    return DataSourceTypes.RDF;
		} else if (properties.get(SPARQL.DATASOURCE) != null) {
            return DataSourceTypes.SPARQL;
        } else if (properties.get(CSV.DATASOURCE) != null) {
		    return DataSourceTypes.CSV;
		} else if (properties.get(DBConstants.JNDI.RESOURCE_NAME) != null) {
		    return DataSourceTypes.JNDI;
		} else if (properties.get(MongoDB.SERVERS) != null) {
            return DataSourceTypes.MONGODB;
        } else if (properties.get(GSpread.DATASOURCE) != null) {
		    return DataSourceTypes.GDATA_SPREADSHEET;
		} else if (properties.get(DBConstants.CarbonDatasource.NAME) != null) {
		    return DataSourceTypes.CARBON;
		} else if (properties.get(DBConstants.WebDatasource.WEB_CONFIG) != null) {
            return DataSourceTypes.WEB;
        } else if (properties.get(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS) != null) {
            return DataSourceTypes.CUSTOM_TABULAR;
        } else if (properties.get(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS) != null) {
            return DataSourceTypes.CUSTOM_QUERY;
        } else if (properties.get(DBConstants.Cassandra.CASSANDRA_SERVERS) != null) {
            return DataSourceTypes.CASSANDRA;
        } 
		throw new DataServiceFault("Cannot create config with properties: " + properties);
	}
	
}
