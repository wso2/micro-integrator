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

import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.datasource.DataColumn;
import org.wso2.micro.integrator.dataservices.core.datasource.DataRow;
import org.wso2.micro.integrator.dataservices.core.datasource.DataTable;
import org.wso2.micro.integrator.dataservices.core.datasource.FixedDataRow;
import org.wso2.micro.integrator.dataservices.core.datasource.TabularDataBasedDS;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;
import org.wso2.micro.integrator.dataservices.sql.driver.TConnectionFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.TCustomConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataCell;
import org.wso2.micro.integrator.dataservices.sql.driver.query.ColumnInfo;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class represents a data services custom data source.
 */
public class TabularDataBasedConfig extends SQLConfig {
	
	private CustomSQLDataSource dataSource;

	public TabularDataBasedConfig(DataService dataService, String configId, Map<String, String> properties,
                                  boolean odataEnable) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.CUSTOM_TABULAR, properties, odataEnable);
		String dsClass = properties.get(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS);
		try {
			TabularDataBasedDS customDS = (TabularDataBasedDS) Class.forName(dsClass).newInstance();
			String dataSourcePropsString = properties.get(
					DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
			Map<String, String> dsProps;
			if (dataSourcePropsString != null) {
				dsProps = DBUtils.extractProperties(AXIOMUtil.stringToOM(
						dataSourcePropsString));
			} else {
				dsProps = new HashMap<String, String>();
			}
			DBUtils.populateStandardCustomDSProps(dsProps, this.getDataService(), this);
			customDS.init(dsProps);
			this.dataSource = new CustomSQLDataSource(customDS);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in creating custom data source config: " +
					e.getMessage());
		}
	}
	
	@Override
	public DataSource getDataSource() throws DataServiceFault {
		return dataSource;
	}

	@Override
	public boolean isStatsAvailable() throws DataServiceFault {
		return false;
	}

	@Override
	public int getActiveConnectionCount() throws DataServiceFault {
		return -1;
	}

	@Override
	public int getIdleConnectionCount() throws DataServiceFault {
		return -1;
	}

	@Override
	public void close() {
		this.dataSource.close();
	}

	@Override
	public ODataDataHandler createODataHandler() throws ODataServiceFault {
		throw new ODataServiceFault("Expose as OData Service feature doesn't support for the " + getConfigId() +
		                           " Datasource.");
	}

	/**
	 * Custom SQL data source implementation.
	 */
	public static class CustomSQLDataSource implements DataSource {

		private PrintWriter logWriter;
		
		private int loginTimeout;
		
		private SQLParserCustomDataSourceAdapter dataSource;
		
		private Properties customDSProps;
		
		public CustomSQLDataSource(TabularDataBasedDS dataSource) {
			this.dataSource = new SQLParserCustomDataSourceAdapter(dataSource);
			this.customDSProps = new Properties();
			this.customDSProps.put(Constants.DRIVER_PROPERTIES.DATA_SOURCE_TYPE, Constants.CUSTOM);
			this.customDSProps.put(TCustomConnection.CUSTOM_DATASOURCE, this.dataSource);
		}
		
		@Override
		public PrintWriter getLogWriter() throws SQLException {
			return logWriter;
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return loginTimeout;
		}

        @Override //remove the annotation if needs to support this in java 1.6 too(backward compatibility)
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        }

        @Override
		public void setLogWriter(PrintWriter logWriter) throws SQLException {
			this.logWriter = logWriter;
		}

		@Override
		public void setLoginTimeout(int loginTimeout) throws SQLException {
			this.loginTimeout = loginTimeout;
		}

		@Override
		public boolean isWrapperFor(Class<?> arg0) throws SQLException {
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> arg0) throws SQLException {
			return null;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return TConnectionFactory.createConnection(Constants.CUSTOM, this.customDSProps);
		}

		@Override
		public Connection getConnection(String username, String password)
				throws SQLException {
			return this.getConnection();
		}
		
		public void close() {
			this.dataSource.close();
		}
		
		/**
		 * Adapter class to bridge the functionality between the data services custom data source
		 * and the SQL parser custom data source.
		 */
		public class SQLParserCustomDataSourceAdapter implements
				TCustomConnection.CustomDataSource {

			private TabularDataBasedDS customDS;

			public SQLParserCustomDataSourceAdapter(TabularDataBasedDS customDS) {
				this.customDS = customDS;
			}

			@Override
			public void createDataTable(String name, Map<String, Integer> columns)
					throws SQLException {
				List<DataColumn> dataColumns = new ArrayList<DataColumn>();
				for (String key : columns.keySet()) {
					dataColumns.add(new DataColumn(key));
				}
				this.customDS.createDataTable(name, dataColumns);
			}

			@Override
			public void dropDataTable(String name) throws SQLException {
				this.customDS.dropDataTable(name);
			}

			@Override
			public org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable getDataTable(
					String name) throws SQLException {
				try {
					DataTable dataTable = this.customDS.getDataTable(name);
					if (dataTable == null) {
						throw new SQLException("The custom data table '" + name + "' does not exist");
					}
					return new SQLParserDataTableAdapter(name, dataTable);
				} catch (DataServiceFault e) {
					throw new SQLException(e);
				}
			}

			@Override
			public Set<String> getDataTableNames() throws SQLException {
				try {
					return this.customDS.getDataTableNames();
				} catch (DataServiceFault e) {
					throw new SQLException(e);
				}
			}

			@Override
			public void init(Properties props) throws SQLException {
				try {
					Map<String, String> dsProps = new HashMap<String, String>();
					for (Entry<String, String> entry : dsProps.entrySet()) {
						dsProps.put(entry.getKey(), entry.getValue());
					}
					this.customDS.init(dsProps);
				} catch (DataServiceFault e) {
					throw new SQLException(e);
				}
			}

			public void close() {
				this.customDS.close();
			}

			/**
			 * Adapter class for a SQL Parser data table.
			 */
			public class SQLParserDataTableAdapter extends
                    org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable {

				private DataTable customDataTable;

				private String[] columns;

				private int[] types;

				public SQLParserDataTableAdapter(String tableName,
						DataTable customDataTable) throws DataServiceFault {
                    super(tableName, true);
					this.customDataTable = customDataTable;
					/* columns, types pre-populated for performance reasons */
					this.columns = new String[this.customDataTable.getDataColumns().size()];
					this.types = new int[this.columns.length];
					for (int i = 0; i < this.columns.length; i++) {
						this.columns[i] = this.customDataTable.getDataColumns().get(i).getName();
						this.types[i] = Types.VARCHAR;
					}
					this.setHeaders(this.generateHeaders());
				}

                @Override
				public void addRow(org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow row)
				            throws SQLException {
					try {
						this.customDataTable.insertData(this.convertDataRow(row));
					} catch (DataServiceFault e) {
						throw new SQLException(e);
					}
				}

				private DataRow convertDataRow(org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow row) {
					Map<String, String> values = new HashMap<String, String>();
					for (DataCell cell : row.getCells().values()) {
						values.put(this.columns[cell.getColumnId()],
								cell.getCellValue().toString());
					}
					return new FixedDataRow(values);
				}

				private org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow convertDataRow(
						int rowId, DataRow row) {
					org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow result =
						new org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow(rowId);
					for (int i = 0; i < this.types.length; i++) {
                        int colId = i + 1;
						result.addCell(colId, new DataCell(colId, this.types[i], row.getValueAt(columns[i])));
					}
					return result;
				}

				private TabularDataBasedDS.FilterOperator convertOperator(String operator) throws SQLException {
					if (Constants.EQUAL.equals(operator)) {
						return TabularDataBasedDS.FilterOperator.EQUALS;
					} else if (Constants.GREATER_THAN.equals(operator)) {
						return TabularDataBasedDS.FilterOperator.GREATER_THAN;
					} else if (Constants.LESS_THAN.equals(operator)) {
						return TabularDataBasedDS.FilterOperator.LESS_THAN;
					} else {
						throw new SQLException("The operator '" + operator +
								"' is not supported by DS custom data sources");
					}
				}

				@Override
				public Map<Integer, org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow> applyCondition(
						String column, String value, String operator) throws SQLException {
					try {
						TabularDataBasedDS.FilterOperator dsOp = this.convertOperator(operator);
						Map<Integer, org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow> result =
							new HashMap<Integer, org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow>();
					    for (Entry<Long, DataRow> row : this.customDataTable.filterData(
					    		column, value, dsOp).entrySet()) {
						    result.put(row.getKey().intValue(), this.convertDataRow(
						    		row.getKey().intValue(), row.getValue()));
					    }
					    return result;
					} catch (DataServiceFault e) {
						throw new SQLException(e);
					}
				}

				private ColumnInfo[] generateHeaders() throws DataServiceFault {
					ColumnInfo[] headers =
                            new ColumnInfo[this.customDataTable.getDataColumns().size()];
					int i = 1;
					for (DataColumn column : this.customDataTable.getDataColumns()) {
                        headers[i - 1] =
                                new ColumnInfo(i, column.getName(), this.getTableName(),
                                        Types.VARCHAR, i);
                        i++;
					}
					return headers;
				}

				@Override
				public Map<Integer, org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow> getRows()
				            throws SQLException {
					try {
					    Map<Integer, org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow> result =
						    new HashMap<Integer, org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow>();
					    for (Entry<Long, DataRow> row : this.customDataTable.getData(0, -1).entrySet()) {
					        result.put(row.getKey().intValue(), this.convertDataRow(
					    		    row.getKey().intValue(), row.getValue()));
				        }
					    return result;
					} catch (DataServiceFault e) {
						throw new SQLException(e);
					}
				}

				@Override
				public void deleteRows(int... rowIds) throws SQLException {
					try {
						long[] longIds = new long[rowIds.length];
						for (int i = 0; i < rowIds.length; i++) {
							longIds[i] = rowIds[i];
						}
						this.customDataTable.deleteData(longIds);
					} catch (DataServiceFault e) {
						throw new SQLException(e);
					}
				}

				@Override
				public void updateRows(org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow... data)
				        throws SQLException {
					Map<Long, DataRow> updateValues = new HashMap<Long, DataRow>();
					for (org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataRow row : data) {
					    updateValues.put((long) row.getRowId(), this.convertDataRow(row));
					}
					try {
					    this.customDataTable.updateData(updateValues);
					} catch (DataServiceFault e) {
						throw new SQLException(e);
					}
				}
				
			}
			
		}
		
	}

}
