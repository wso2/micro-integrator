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
package org.wso2.micro.integrator.dataservices.core.script;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.ResultTypes;
import org.wso2.micro.integrator.dataservices.core.engine.CallQuery.WithParam;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.auth.UserStoreAuthorizationProvider;
import org.wso2.micro.integrator.dataservices.core.description.config.Config;
import org.wso2.micro.integrator.dataservices.core.description.config.SQLCarbonDataSourceConfig;
import org.wso2.micro.integrator.dataservices.core.description.operation.Operation;
import org.wso2.micro.integrator.dataservices.core.description.query.Query;
import org.wso2.micro.integrator.dataservices.core.description.query.QueryFactory;
import org.wso2.micro.integrator.dataservices.core.description.query.SQLQuery;
import org.wso2.micro.integrator.dataservices.core.engine.CallQuery;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.OutputElementGroup;
import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;
import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;
import org.wso2.micro.integrator.dataservices.core.engine.Result;
import org.wso2.micro.integrator.dataservices.core.engine.StaticOutputElement;
import org.wso2.micro.integrator.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.micro.integrator.dataservices.core.validation.Validator;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceService;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Convert whole database into list of data-service objects,data-service object
 * per a each table This data-service object contain the all the basic database
 * operations as follows Insert, Delete, Update, Select and Select all.This
 * class has overload constructors. First constructor for creating DS object per
 * table wise. Second constructor for creating DS object for whole database.
 */
public class DSGenerator {

	private static final String AUTOINCREMENT_COLUMN = "IS_AUTOINCREMENT";

	private static final String IDENTITY_COLUMN = "Identity";

    private static final String IS_AUTOINCREMENT = "YES";

	private static Log log = LogFactory.getLog(DSGenerator.class);

	private DataService generatedService;

	private List<DataService> generatedServiceList;

	private List<String> DSErrorList;

	/**
	 * Constructor creates and initializes the meta data object create DS object
	 * per table
	 * 
	 * @param tableNames
	 *            - String array of database table names ,It can be zero element
	 *            array
	 * @param singleService
	 *            - select the service generation mode true one service for
	 *            whole database when it false services per table wise
	 * @param dataSourceId
	 *            - carbon datasource id it can be null
	 * 
	 *            When create the connection there are 3 away to do it , First
	 *            way is provide the relevant information(url, driver, username,
	 *            password) which need to create connection that time other
	 *            fields(connection, datasourceId) should be null
	 * 
	 *            Second way directly you can provide connection instance
	 * 
	 *            final way is you can provide carbon datasource id(name)
	 * 
	 *            if you wish to use all the tables of database you can provide
	 *            zero element array
	 * 
	 * 
	 */
	public DSGenerator(String dataSourceId, String dbName, String[] schemas, String[] tableNames, boolean singleService,
	                   String nameSpace, String serviceName) throws Exception {
		this.DSErrorList = new ArrayList<String>();
		Connection connection = null;
		try {
			String[] tableNameList = tableNames;
			if (dataSourceId != null) {
				connection = createConnection(dataSourceId);
				DatabaseMetaData metaObject = connection.getMetaData();
				if (tableNameList.length == 0) {
					tableNameList = DSGenerator.getTableList(connection, dbName, schemas);
				}
				if (singleService) {
					this.generatedService = generateService(dataSourceId, dbName, schemas, tableNameList, metaObject,
					                                        nameSpace, serviceName);
				} else {
					this.generatedServiceList = generateServices(dataSourceId, dbName, schemas, tableNameList,
					                                             metaObject, nameSpace);
				}
			}
		} catch (Exception e) {
			throw new Exception("Meta Object initialization failed due to : " + e.getMessage());
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	public List<String> getDSErrorList() {
		return DSErrorList;
	}

	private static Connection createConnection(String dataSourceId)
            throws SQLException, DataServiceFault {
        DataSourceService dataSourceService = DataServicesDSComponent.getDataSourceService();
        CarbonDataSource cds;
		try {
			cds = dataSourceService.getDataSource(dataSourceId);
		} catch (DataSourceException e) {
			throw new DataServiceFault(e, "Error in retrieving data source: " + e.getMessage());
		}
        if (cds == null) {
            throw new DataServiceFault("DataSource '" + dataSourceId + "' is not available.");
        }
        Object ds = cds.getDSObject();
        if (!(ds instanceof DataSource)) {
        	throw new DataServiceFault("DataSource '" + dataSourceId + "' is not an RDBMS data source.");
        }
        return ((DataSource) ds).getConnection();
    }

	private DataService generateService(String datasourceId, String dbName,
                                        String[] schemas, String[] tableNames, DatabaseMetaData metaData, String serviceNamespace, String serviceName)
            throws DataServiceFault, SQLException {
		//String serviceName = dbName + DBConstants.DataServiceGenerator.SERVICE_NAME_SUFFIX;
		DataService dataService = new DataService(serviceName,
                                                  DBConstants.DataServiceGenerator.SINGLE_SERVICE_DESCRIPTION, null, null,
                                                  DBConstants.DataServiceGenerator.ACTIVE, false, false, null);
        /* setting default authorization provider */
        dataService.setAuthorizationProvider(new UserStoreAuthorizationProvider());
		this.setConfig(dataService, datasourceId);
		if (DBUtils.isEmptyString(serviceNamespace)) {
			dataService.setServiceNamespace(DBConstants.WSO2_DS_NAMESPACE);
		} else {
			dataService.setServiceNamespace(serviceNamespace);
		}
		if (schemas.length != 0) {
			for (String schema : schemas) {
				makeService(dbName, tableNames, metaData, dataService, schema);
			}
		} else {
			String schema = null;
			makeService(dbName, tableNames, metaData, dataService, schema);
		}
		return dataService;
	}

	private List<DataService> generateServices(String datasourceId,
                                               String dbName, String[] schemas, String[] tableNames,
                                               DatabaseMetaData metaData, String serviceNamespace) throws SQLException,
                                                                                                          DataServiceFault {
		List<DataService> serviceList = new ArrayList<DataService>();
		if (schemas.length != 0) {
			for (String schema : schemas) {
				makeServices(dbName, tableNames, metaData, serviceList, schema, datasourceId, serviceNamespace);
			}
		} else {
			String schema = null;
			makeServices(dbName, tableNames, metaData, serviceList, schema, datasourceId, serviceNamespace);
		}
		return serviceList;
	}

	private void makeService(String dbName, String[] tableNames,
                             DatabaseMetaData metaData, DataService dataService, String schema)
			throws SQLException {
		for (String tableName : tableNames) {
                        String tablePrimaryKey = "";
                        try {
                                tablePrimaryKey = this.getPrimaryKey(metaData, dbName,
                                        schema, tableName);
                        } catch (SQLException e) {
                                throw new SQLException("Cannot create the service : " + e.getMessage());
                        }
			this.addOperations(dataService, schema, metaData, dbName,
					tableName, tablePrimaryKey);
		}
	}

	private void makeServices(String dbName, String[] tableNames,
			DatabaseMetaData metaData, List<DataService> dataServiceList,
			String schema, String datasourceId,String serviceNamespace) throws SQLException,
                                                                               DataServiceFault {

		for (String tableName : tableNames) {
			String serviceName = tableName + DBConstants.DataServiceGenerator.SERVICE_NAME_SUFFIX;
			DataService dataService = new DataService(serviceName,
                                                      DBConstants.DataServiceGenerator.MUTLIPLE_SERVICE_DESCRIPTION, null, null,
                                                      DBConstants.DataServiceGenerator.ACTIVE, false, false, null);
            /* setting default authorization provider */
            dataService.setAuthorizationProvider(new UserStoreAuthorizationProvider());
			if (DBUtils.isEmptyString(serviceNamespace)) {
				dataService.setServiceNamespace(DBConstants.WSO2_DS_NAMESPACE);
			} else {
				dataService.setServiceNamespace(serviceNamespace);
			}
			/* set transports */
			List<String> transports = new ArrayList<>();
			transports.add(Constants.TRANSPORT_HTTP);
			transports.add(Constants.TRANSPORT_HTTPS);
			dataService.setTransports(transports);
			this.setConfig(dataService, datasourceId);
                        String tablePrimaryKey = "";
                        try {
                                tablePrimaryKey = this.getPrimaryKey(metaData, dbName,
					schema, tableName);
                        } catch (SQLException e) {
                            throw new SQLException("Cannot create the service : " + e.getMessage());
                        }
			this.addOperations(dataService, schema, metaData, dbName,
					tableName, tablePrimaryKey);
			dataServiceList.add(dataService);
		}
	}

	private void addOperations(DataService dataService, String schema,
                               DatabaseMetaData metaData, String dbName, String tableName,
                               String tablePrimaryKey) {
		try {
			/* add Insert Operation & it's Query */
			this.addInsertOperation(dataService, schema, metaData, dbName,
					tableName);
		} catch (Exception e) {
			e.printStackTrace();
			String defError = "Insert operation not created";
			this.addError(defError);
			log.warn(defError, e);
		}
		try {
			/* add Select all Operation & it's Query */
			this.addSelectAllOperation(dataService, schema, metaData,
					dbName, tableName, tablePrimaryKey);
		} catch (Exception e) {
			String defError = "select all operation not created";
			this.addError(defError);
			log.warn(defError, e);
		}
		if (tablePrimaryKey != null) {
			try {
				/* add Update Operation & it's Query */
				this.addUpdateOperation(dataService, schema, metaData, dbName,
						tableName, tablePrimaryKey);
			} catch (Exception e) {
				String defError = "update operation not created";
				this.addError(defError);
				log.warn(defError, e);
			}
			try {
				/* add Delete Operation & it's Query */
				this.addDeleteOperation(dataService, schema, metaData, dbName,
						tableName, tablePrimaryKey);
			} catch (Exception e) {
				String defError = "delete operation not created";
				this.addError(defError);
				log.warn(defError, e);
			}
			try {
				/* add Select using PRY-key Operation & it's Query */
				this.addSelectWithKeyOperation(dataService, schema, metaData,
						dbName, tableName, tablePrimaryKey);
			} catch (Exception e) {
				String defError = "select with key Operation not created";
				this.addError(defError);
				log.warn(defError, e);
			}
		} else {
			String defError = "Primary key does not exists";
			log.warn(defError);
		}
	}

	public static String[] getTableList(String datasourceId, String dbName, String[] schemas) throws DataServiceFault {
		Connection connection = null;
		try {
			if (datasourceId != null) {
				connection = createConnection(datasourceId);
				return DSGenerator.getTableList(connection, dbName, schemas);
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new DataServiceFault("Error in retrieving table list : " + e.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					//ignore
				}
			}
		}
	}

	public static String[] getTableList(String url, String driver, String userName, String password, String dbName,
	                                    String[] schemas) throws DataServiceFault {
		Connection connection = null;
		try {
			if ((url != null) && (driver != null) && (userName != null) && (password != null)) {
				Class.forName(driver);
				if (!userName.isEmpty()) {
					connection = DriverManager.getConnection(url, userName, password);
				} else {
					connection = DriverManager.getConnection(url);
				}
				return DSGenerator.getTableList(connection, dbName, schemas);
			} else {
				return null;
			}
		} catch (ClassNotFoundException e) {
			throw new DataServiceFault(
					"Error in retrieving table list, Error loading Driver class : " + e.getMessage());
		} catch (SQLException e) {
			throw new DataServiceFault("Error in retrieving table list : " + e.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}

	public static String[] getTableList(Connection connection, String dbName,
			String[] schemas) throws SQLException {
		if (connection != null) {
			List<String> tableList = new ArrayList<String>();
			// String dbName = connection.getCatalog();
			DatabaseMetaData mObject = connection.getMetaData();
			if (schemas.length != 0) {
                tableList = getTableNamesList(mObject, dbName, schemas[0]);
                if (tableList.size() == 0) { // for some drivers catalog is not same as the dbname (eg: DB2). In that case search tables only using schema.
                    tableList = getTableNamesList(mObject, null, schemas[0]);
                }
			} else {
               tableList = getTableNamesList(mObject, dbName, null);
			}
			return tableList.toArray(new String[tableList.size()]);
		} else {
			return null;
		}
	}

    public static List<String> getTableNamesList(DatabaseMetaData mObject, String dbName,
                                                 String schema) throws SQLException {
        ResultSet tableNamesList = null;
        try {
            List<String> tableList = new ArrayList<String>();
            tableNamesList = mObject.getTables(dbName, schema, null, null);
            while (tableNamesList.next()) {
                tableList.add(tableNamesList.getString(DBConstants.DataServiceGenerator.TABLE_NAME));
            }
            return tableList;
        } finally {
            if(tableNamesList != null) {
                tableNamesList.close();
            }
        }
    }

	public static String[] getSchemas(String datasourceId) throws Exception {
		Connection connection = null;
		try {
			if (datasourceId != null) {
				connection = createConnection(datasourceId);
				return DSGenerator.getSchemas(connection);
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new DataServiceFault("Error in retrieving schema list : " + e.getMessage());
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	public static String[] getSchemas(Connection connection) throws Exception {
        ResultSet schemas = null;
        try {
            if (connection != null) {
                List<String> schemaList = new ArrayList<String>();
                DatabaseMetaData mObject = connection.getMetaData();
                schemas = mObject.getSchemas();
                while (schemas.next()) {
                    schemaList.add(schemas.getString(DBConstants.DataServiceGenerator.TABLE_SCHEM));
                }
                String str[] = schemaList.toArray(new String[schemaList.size()]);
                return str;
            } else {
                return null;
            }
        } finally {
            if (schemas != null) {
                schemas.close();
            }
        }
    }

	private void setConfig(DataService dataServiceObject, String carbonSourceId)
            throws DataServiceFault {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(DBConstants.CarbonDatasource.NAME, carbonSourceId);
		Config config = new SQLCarbonDataSourceConfig(dataServiceObject,
                                                      DBConstants.DataServiceGenerator.CONFIG_ID, properties);
		dataServiceObject.addConfig(config);
	}

	private void addError(String error) {
		this.DSErrorList.add(error);
	}

	private String getPrimaryKey(DatabaseMetaData meta, String dbName,
			String schema, String tableName) throws SQLException {
		String pKey = null;
        ResultSet resultSet = null;
        try {
            resultSet = meta.getPrimaryKeys(dbName, schema, tableName);

            if (resultSet.next()) {
                resultSet = meta.getPrimaryKeys(dbName, schema, tableName);
            } else {
                try {
                    resultSet = meta.getPrimaryKeys(null, schema, tableName);
                } catch (SQLException e) {
                    throw new SQLException("Failed to extract primary key info");
                }
            }
            while (resultSet.next()) {
                pKey = resultSet.getString(DBConstants.DataServiceGenerator.COLUMN_NAME);
                return pKey;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return pKey;
	}

    private ResultSet getColumnNames (DatabaseMetaData metaData, String schema, String dbName,
			String tableName, String columnNamePattern) throws SQLException {
        ResultSet columnNames = metaData.getColumns(dbName, schema, tableName, columnNamePattern);
        if (columnNames.next()) {
             columnNames = metaData.getColumns(dbName, schema, tableName, columnNamePattern);
        }  else {
             columnNames = metaData.getColumns(null, schema, tableName,columnNamePattern);
        }

        return columnNames;
    }

    private boolean isAutoIncrementField(ResultSet columnNames) throws SQLException {
    	try {
            String autoIncrString = columnNames.getString(AUTOINCREMENT_COLUMN);
			if (IS_AUTOINCREMENT.equalsIgnoreCase(autoIncrString)) {
				return true;
			}
		} catch (SQLException ignore) {
			// ignore
		}
		try {
			Boolean identity = columnNames.getBoolean(IDENTITY_COLUMN);
			if (identity != null) {
				return identity;
			}
		} catch (SQLException ignore) {
			// ignore
		}		
		return false;
    }
    
	/**
	 * Insert Operation.
	 */
	private void addInsertOperation(DataService dataServiceObject,
                                    String schema, DatabaseMetaData metaData, String dbName,
                                    String tableName) throws DataServiceFault, SQLException {
        ResultSet columnNames = null;
		/* do insertion operation */
        try {
            Map<String, WithParam> paramMap = new HashMap<String, WithParam>();
            List<String> paramList = new ArrayList<String>();
            columnNames = getColumnNames(metaData, schema, dbName, tableName, null);

            while (columnNames.next()) {
                if (this.isAutoIncrementField(columnNames)) {
                    continue;
                }
                String name = columnNames.getString(DBConstants.DataServiceGenerator.COLUMN_NAME);
                WithParam withParam = new WithParam(name, name, name, DBConstants.DataServiceGenerator.QUERY_PARAM);
                paramMap.put(name, withParam);
                paramList.add(name);
            }
            Set<String> requiredRoles = new HashSet<String>();
            String queryId = DBConstants.DataServiceGenerator.INSERT_ + tableName + DBConstants.DataServiceGenerator._QUERY;
            String OpName = DBConstants.DataServiceGenerator.INSERT_ + tableName + DBConstants.DataServiceGenerator._OPERATION;
            CallQuery callQuery = new CallQuery(dataServiceObject, queryId,
                                                paramMap, requiredRoles);
            // batchRequest=false
            // parentOperation=null
            Operation op = new Operation(dataServiceObject, OpName, null,
                                         callQuery, false, null, false, false);
            dataServiceObject.addOperation(op);
            dataServiceObject.addQuery(this.getInsertQuery(paramList, queryId,
                    tableName, dataServiceObject, metaData, dbName, schema));
        } finally {
            if (columnNames != null) {
                columnNames.close();
            }
        }

	}

	/**
	 * Update Operation.
	 */
	private void addUpdateOperation(DataService dataServiceObject,
                                    String schema, DatabaseMetaData metaData, String dbName,
                                    String tableName, String pKey) throws SQLException,
                                                                          DataServiceFault {
        ResultSet columnNames = null;
        try {
            Map<String, WithParam> paramMap = new HashMap<String, WithParam>();
            List<String> paramList = new ArrayList<String>();
            columnNames = getColumnNames(metaData, schema, dbName, tableName, null);
            while (columnNames.next()) {
                String name = columnNames.getString(DBConstants.DataServiceGenerator.COLUMN_NAME);
                if (!name.equals(pKey)) {
                    WithParam withParam1 = new WithParam(name, name, name,
                            DBConstants.DataServiceGenerator.QUERY_PARAM);
                    paramMap.put(name, withParam1);
                    paramList.add(name);// add to this @param into @param List
                }
            }
            WithParam withParam2 = new WithParam(pKey, pKey, pKey, DBConstants.DataServiceGenerator.QUERY_PARAM);
            paramMap.put(pKey, withParam2);
            paramList.add(pKey);
            Set<String> requiredRoles = new HashSet<String>();// empty set
            String queryId = DBConstants.DataServiceGenerator.UPDATE_ + tableName + DBConstants.DataServiceGenerator._QUERY;
            String OpName = DBConstants.DataServiceGenerator.UPDATE_ + tableName + DBConstants.DataServiceGenerator._OPERATION;
            CallQuery callQuery = new CallQuery(dataServiceObject, queryId,
                                                paramMap, requiredRoles);
            // batchRequest=false
            // parentOperation=null
            Operation operation = new Operation(dataServiceObject, OpName, null,
                                                callQuery, false, null, false, false);
            dataServiceObject.addOperation(operation);
            dataServiceObject.addQuery(this
                    .getUpdateQuery(paramList, pKey, queryId, tableName,
                            dataServiceObject, metaData, dbName, schema));
        } finally {
            if (columnNames != null) {
                columnNames.close();
            }
        }
    }

	/**
	 * Delete operation.
	 */
	private void addDeleteOperation(DataService dataServiceObject,
                                    String schema, DatabaseMetaData metaData, String dbName,
                                    String tableName, String pKey) throws SQLException,
                                                                          DataServiceFault, NullPointerException {
        ResultSet columnNames = null;
        try {

		/* get the primary key */
            Map<String, WithParam> paramMap = new HashMap<String, WithParam>();
            List<String> paramList = new ArrayList<String>();

            columnNames = getColumnNames(metaData, schema, dbName, tableName, null);
            while (columnNames.next()) {
                String name = columnNames.getString(DBConstants.DataServiceGenerator.COLUMN_NAME);
                if (pKey.equals(name)) {
                    WithParam withParam = new WithParam(pKey, pKey, pKey,
                            DBConstants.DataServiceGenerator.QUERY_PARAM);
                    paramMap.put(pKey, withParam);
                    paramList.add(pKey);
                }
            }
            Set<String> requiredRoles = new HashSet<String>();
            String queryId = DBConstants.DataServiceGenerator.DELETE_ + tableName + DBConstants.DataServiceGenerator._QUERY;
            String OpName = DBConstants.DataServiceGenerator.DELETE_ + tableName + DBConstants.DataServiceGenerator._OPERATION;
            CallQuery callQuery = new CallQuery(dataServiceObject, queryId,
                                                paramMap, requiredRoles);
            // batchRequest=false
            // parentOperation=null
            Operation operation = new Operation(dataServiceObject, OpName, null,
                                                callQuery, false, null, false, false);
            dataServiceObject.addOperation(operation);
            dataServiceObject.addQuery(this
                    .getDeleteQuery(paramList, pKey, queryId, tableName,
                            dataServiceObject, metaData, dbName, schema));
        } finally {
            if (columnNames != null) {
                columnNames.close();
            }
        }
    }

	/**
	 * Create the data-service for select data by given key operation.
	 */
	private void addSelectWithKeyOperation(DataService dataServiceObject,
                                           String schema, DatabaseMetaData metaData, String dbName,
                                           String tableName, String pKey) throws SQLException,
                                                                                 DataServiceFault, NullPointerException {
        ResultSet columnNames = null;
        try {
            Map<String, WithParam> paramMap = new HashMap<String, WithParam>();
            List<String> paramList = new ArrayList<String>();

            columnNames = getColumnNames(metaData, schema, dbName, tableName, null);
            String colomNames = "";
            int i = 0;
            while (columnNames.next()) {
                String name = columnNames.getString(DBConstants.DataServiceGenerator.COLUMN_NAME);
                //get the colomn names for the query
                if (i == 0) {
                    colomNames = " " + name;
                } else {
                    colomNames = colomNames + ", " + name;
                }
                i++;
                if (pKey.equals(name)) {
                    WithParam withParam = new WithParam(pKey, pKey, pKey,
                            DBConstants.DataServiceGenerator.QUERY_PARAM);
                    paramMap.put(pKey, withParam);
                    paramList.add(pKey);
                }
            }
            Set<String> requiredRoles = new HashSet<String>();
            String queryId = DBConstants.DataServiceGenerator.SELECT_WITH_KEY + tableName + DBConstants.DataServiceGenerator._QUERY;
            String OpName = DBConstants.DataServiceGenerator.SELECT_WITH_KEY + tableName + DBConstants.DataServiceGenerator._OPERATION;
            CallQuery callQuery = new CallQuery(dataServiceObject, queryId,
                                                paramMap, requiredRoles);
            // batchRequest=false
            // parentOperation=null
            Operation operation = new Operation(dataServiceObject, OpName, null,
                                                callQuery, false, null, false, false);
            dataServiceObject.addOperation(operation);
            dataServiceObject.addQuery(this
                    .getSelectWithKeyQuery(paramList, pKey, queryId, tableName,
                            dataServiceObject, metaData, dbName, schema, colomNames));
        } finally {
            if (columnNames != null) {
                columnNames.close();
            }
        }
    }

	/**
	 * Select all operation.
	 */
	private void addSelectAllOperation(DataService dataServiceObject,
                                       String schema, DatabaseMetaData metaData, String dbName,
                                       String tableName, String pKey) throws SQLException,
                                                                             DataServiceFault, NullPointerException {
        ResultSet columnNames = null;
        try {
            Map<String, WithParam> paramMap = new HashMap<String, WithParam>();
            List<String> paramList = new ArrayList<String>();
		    /* get the primary key */
            // ResultSet resultSet = this.metaObject.getColumns(this.dbName, null,
            // this.tableName, null);
            columnNames = getColumnNames(metaData, schema, dbName, tableName, null);
            String colomNames = "";
            int i = 0;
            while (columnNames.next()) {
                String name = columnNames.getString(DBConstants.DataServiceGenerator.COLUMN_NAME);
                if (i == 0) {
                    colomNames = " " + name;
                } else {
                    colomNames = colomNames + ", " + name;
                }
                i++;
            }
            paramMap.clear();
            Set<String> requiredRoles = new HashSet<String>();// empty set
            String queryId = new StringBuilder().append(DBConstants.DataServiceGenerator.SELECT_ALL)
                    .append(tableName).append(DBConstants.DataServiceGenerator._QUERY).toString();
            String OpName = new StringBuilder().append(DBConstants.DataServiceGenerator.SELECT_ALL)
                    .append(tableName).append(DBConstants.DataServiceGenerator._OPERATION).toString();
            CallQuery callQuery = new CallQuery(dataServiceObject, queryId,
                                                paramMap, requiredRoles);
            // batchRequest=false
            // parentOperation=null
            Operation operation = new Operation(dataServiceObject, OpName, null,
                                                callQuery, false, null, false, false);
            dataServiceObject.addOperation(operation);
            dataServiceObject.addQuery(this.getSelectAllQuery(paramList, queryId,
                    tableName, dataServiceObject, metaData, dbName, schema, colomNames));
        } finally {
            if (columnNames != null) {
                columnNames.close();
            }
        }

	}

	private Query getUpdateQuery(List<String> pList, String tablePrimaryKey,
                                 String queryId, String tableName, DataService dataServiceObject,
                                 DatabaseMetaData metaData, String dbName, String schema)
			throws IllegalArgumentException, DataServiceFault, SQLException {
		/* get the query */
		DynamicSqlUtils sqlStatement = new DynamicSqlUtils();
		String query = sqlStatement.getUpdateStatement(tableName, schema, pList,
				tablePrimaryKey);
		Result result = null;
		Map<String, String> advanceProp = new HashMap<String, String>();
		return new SQLQuery(dataServiceObject, queryId, DBConstants.DataServiceGenerator.CONFIG_ID, false, false, null,
                            query, this.getQueryParamList(metaData, dbName, schema, tableName, pList), result, null,
                            null, advanceProp, null);
	}

	private Query getDeleteQuery(List<String> pList, String tablePrimaryKey,
                                 String queryId, String tableName, DataService dataServiceObject,
                                 DatabaseMetaData metaData, String dbName, String schema)
			throws IllegalArgumentException, DataServiceFault, SQLException {
		DynamicSqlUtils sqlStatement = new DynamicSqlUtils();
		String query = sqlStatement.getDeleteStatement(tableName, schema,
				tablePrimaryKey);
		Result result = null;
		Map<String, String> advanceProp = new HashMap<String, String>();
		return new SQLQuery(dataServiceObject, queryId, DBConstants.DataServiceGenerator.CONFIG_ID, false, false, null,
                            query, this.getQueryParamList(metaData, dbName, schema, tableName, pList), result, null,
                            null, advanceProp, null);
	}

	private SQLQuery getInsertQuery(List<String> param, String queryId,
                                    String tableName, DataService dataServiceObject,
                                    DatabaseMetaData metaData, String dbName, String schema)
			throws IllegalArgumentException, DataServiceFault, SQLException {
		/* get the query */
		DynamicSqlUtils sqlStatementCreator = new DynamicSqlUtils();
		String query = sqlStatementCreator.getInsertStatement(tableName, schema, param);
		Result result = null;
		Map<String, String> advanceProp = new HashMap<String, String>();
		return new SQLQuery(dataServiceObject, queryId, DBConstants.DataServiceGenerator.CONFIG_ID, false, false, null,
                            query, this.getQueryParamList(metaData, dbName, schema, tableName, param), result, null,
                            null, advanceProp, null);
	}

	private Query getSelectAllQuery(List<String> param, String queryId,
                                    String tableName, DataService dataServiceObject,
                                    DatabaseMetaData metaData, String dbName, String schema, String colomNames)
			throws IllegalArgumentException, DataServiceFault, SQLException {
		DynamicSqlUtils sqlStatement = new DynamicSqlUtils();
		String query = sqlStatement.getSelectAll(tableName, schema, colomNames);
		Result result = this.getResult(dataServiceObject, metaData, dbName,
                                       schema, tableName);
		Map<String, String> advanceProp = new HashMap<String, String>();
		return new SQLQuery(dataServiceObject, queryId, DBConstants.DataServiceGenerator.CONFIG_ID, false, false, null,
                            query, this.getQueryParamList(metaData, dbName, schema, tableName, param), result, null,
                            null, advanceProp, null);
	}

	private Query getSelectWithKeyQuery(List<String> param,
                                        String tablePrimaryKey, String queryId, String tableName,
                                        DataService dataServiceObject, DatabaseMetaData metaData,
                                        String dbName, String schema, String colomNames) throws IllegalArgumentException,
                                                                                                DataServiceFault, SQLException {
		DynamicSqlUtils sqlStatement = new DynamicSqlUtils();
		String query = sqlStatement.getSelectByKey(tableName, schema, tablePrimaryKey,colomNames);
		Result result = this.getResult(dataServiceObject, metaData, dbName,
                                       schema, tableName);
		Map<String, String> advanceProp = new HashMap<String, String>();
		return new SQLQuery(dataServiceObject, queryId, DBConstants.DataServiceGenerator.CONFIG_ID, false, false, null,
                            query, this.getQueryParamList(metaData, dbName, schema, tableName, param), result, null,
                            null, advanceProp, null);
	}

	/**
	 * Create query param list according to table column names
	 * 
	 * @param param
	 *            -List of column names in the database table
	 * @param tableName
	 *            -
	 * @return paramList-List of QueryParam
	 * @throws IllegalArgumentException
	 *             -""
	 * @throws SQLException
	 *             -""
	 * @throws DataServiceFault
	 *             -
	 */
	private List<QueryParam> getQueryParamList(DatabaseMetaData metaData,
                                               String dbName, String schema, String tableName, List<String> param)
			throws IllegalArgumentException, SQLException, DataServiceFault {
		List<QueryParam> paramList = new ArrayList<QueryParam>();
		int ordinal = 1;
		for (String pName : param) {
			/* Get the SQL type of parameter */
			String sqlType = "";
			ResultSet rs = getColumnNames (metaData, schema,dbName, tableName, pName);
			while (rs.next()) {
				int type = rs.getInt(DBConstants.DataServiceGenerator.DATA_TYPE);
				if ((-1 == type) || (-16 == type) || (-15 == type)
						|| (2009 == type) || (1111 == type)) {
					type = 1;
				}
				sqlType = DSSqlTypes.getDefinedTypes().get(type);
			}
			List<Validator> validator = new ArrayList<Validator>();
			QueryParam queryParam = new QueryParam(pName, sqlType, DBConstants.DataServiceGenerator.IN,
                                                   DBConstants.DataServiceGenerator.SCALAR, ordinal, null, null, validator, false, false);
			paramList.add(queryParam);
			ordinal++; // increase the ordinal value one by one
		}
		return paramList;
	}

	/**
	 * This method construct the result object.
	 */
	private Result getResult(DataService dataServiceObject,
                             DatabaseMetaData metaData, String dbName, String schema,
                             String tableName) throws DataServiceFault,
                                                      IllegalArgumentException, SQLException {
        ResultSet columnNames = null;
		try {
		    // row name is equal to table Name
            String rowElementName = tableName + DBConstants.DataServiceGenerator.ROW_ELEMENT_NAME_SUFFIX;
            Result result = new Result(rowElementName, tableName, null,
                                       null, ResultTypes.XML);
            columnNames = getColumnNames(metaData, schema, dbName, tableName, null);
            OutputElementGroup defGroup = new OutputElementGroup(null, null, null, null);
            while (columnNames.next()) {
                String columnName = columnNames.getString(DBConstants.DataServiceGenerator.COLUMN_NAME);
                int typeInt = columnNames.getInt("DATA_TYPE");
                if ((-1 == typeInt) || (-16 == typeInt) || (-15 == typeInt)
                        || (2009 == typeInt) || (1111 == typeInt)) {
                    typeInt = 1;
                }
                String type = DSSqlTypes.getQNameType(typeInt);
                QName qName = QueryFactory.getXsdTypeQName(type);
                Set<String> requiredRoles = new HashSet<String>();// empty set
                StaticOutputElement outputElement = new StaticOutputElement(
                        dataServiceObject, columnName, columnName, columnName,
                        DBConstants.DataServiceGenerator.COLUMN, DBConstants.DataServiceGenerator.ELEMENT,
                        null, qName, requiredRoles, 0, 0, null,
                        ParamValue.PARAM_VALUE_SCALAR, null);
                defGroup.addAttributeEntry(outputElement);
            }
            result.setDefaultElementGroup(defGroup);
            return result;
        } finally {
            if(columnNames != null) {
                columnNames.close();
            }
        }
    }

	public DataService getGeneratedService() {
		return generatedService;
	}

	public List<DataService> getGeneratedServiceList() {
		return generatedServiceList;
	}

}
