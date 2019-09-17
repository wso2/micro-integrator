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
package org.wso2.micro.integrator.dataservices.core.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.wso2.micro.integrator.dataservices.common.DBConstants;

import com.hp.hpl.jena.sparql.lib.org.json.JSONArray;
import com.hp.hpl.jena.sparql.lib.org.json.JSONObject;
import com.hp.hpl.jena.sparql.lib.org.json.JSONTokener;
import org.wso2.micro.integrator.dataservices.core.DBUtils;import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * An in-memory custom data source implementation.
 */
public class InMemoryDataSource implements TabularDataBasedDS {

	public static final String IN_MEMORY_DATASOURCE_SCHEMA = "inmemory_datasource_schema";
	
	public static final String IN_MEMORY_DATASOURCE_RECORDS = "inmemory_datasource_records";
	
	private Map<String, InMemoryDataTable> dataTables = new ConcurrentHashMap<String, InMemoryDataTable>();
	
	private String dataSourceId;
	
	public Map<String, InMemoryDataTable> getDataTables() {
		return dataTables;
	}

	@Override
	public void init(Map<String, String> props) throws DataServiceFault {
		this.dataSourceId = props.get(DBConstants.CustomDataSource.DATASOURCE_ID);
		String schemaContents = props.get(IN_MEMORY_DATASOURCE_SCHEMA);
		if (!DBUtils.isEmptyString(schemaContents)) {
			this.createInitialSchema(schemaContents);
		}
		String recordContents = props.get(IN_MEMORY_DATASOURCE_RECORDS);
		if (!DBUtils.isEmptyString(schemaContents)) {
			this.populateInitialData(recordContents);
		}
	}
	
	private void createInitialSchema(String schemaContents) throws DataServiceFault {
		try {
		    JSONObject obj = new JSONObject(new JSONTokener(schemaContents));
		    List<DataColumn> columns;
		    JSONArray colArray;
		    for (String table : JSONObject.getNames(obj)) {
		    	columns = new ArrayList<DataColumn>();
		    	colArray = obj.getJSONArray(table);
		    	for (int i = 0; i < colArray.length(); i++) {
		    		columns.add(new DataColumn(colArray.getString(i)));
		    	}
		    	this.createDataTable(table, columns);
		    }
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in creating initial schema " +
					"for In-Memory data source: " + e.getMessage());
		}
	}

	private void populateInitialData(String recordContents) throws DataServiceFault {
		try {
		    JSONObject obj = new JSONObject(new JSONTokener(recordContents));
		    JSONArray entryArray, recordArray;
		    Map<String, String> rowValues;
		    for (String table : JSONObject.getNames(obj)) {
		    	entryArray = obj.getJSONArray(table);
		    	for (int i = 0; i < entryArray.length(); i++) {
		    		recordArray = entryArray.getJSONArray(i);
		    		rowValues = new HashMap<String, String>();
		    		for (int j = 0; j < recordArray.length(); j++) {
		    			rowValues.put(this.getDataTable(table).getDataColumns().get(j).getName(),
		    					recordArray.get(j).toString());
		    		}
		    		this.getDataTable(table).insertData(new FixedDataRow(rowValues));
		    	}
		    }
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in populating data " +
					"for In-Memory data source: " + e.getMessage());
		}
	}

	public String getDataSourceId() {
		return dataSourceId;
	}

	@Override
	public Set<String> getDataTableNames() throws DataServiceFault {
		return this.getDataTables().keySet();
	}

	@Override
	public DataTable getDataTable(String name) throws DataServiceFault {
		return this.getDataTables().get(name);
	}

	@Override
	public void createDataTable(String name, List<DataColumn> columns) {
		this.getDataTables().put(name, new InMemoryDataTable(columns));
	}

	@Override
	public void dropDataTable(String name) {
		this.getDataTables().remove(name);
	}

	/**
	 * In-memory data table implementation.
	 */
	public class InMemoryDataTable implements DataTable {

		private List<DataColumn> columns;

		private Map<Long, DataRow> data;

		private Lock writeLock = new ReentrantLock();

		private long currentRowId = 0;

		public InMemoryDataTable(List<DataColumn> columns) {
			this.columns = columns;
			this.data = new HashMap<Long, DataRow>();
		}

		@Override
		public List<DataColumn> getDataColumns() {
			return columns;
		}

		public List<DataColumn> getColumns() {
			return columns;
		}

		public Map<Long, DataRow> getData() {
			return data;
		}

		@Override
		public Map<Long, DataRow> getData(long start, long length) throws DataServiceFault {
			/* we are getting a snapshot of the current data by copying the data,
			 * so no need to do any locking of the data set */
			Map<Long, DataRow> result = new HashMap<Long, DataRow>(this.getData());
			int rsize = result.size();
			int endIndex;
			if (length == -1 || length == Integer.MAX_VALUE) {
				endIndex = rsize;
			} else {
			    endIndex = (int) (start + length);
			    if (endIndex > rsize) {
					endIndex = rsize;
				}
			}
			if (start == 0 && endIndex == rsize) {
				return result;
			} else {
			    return this.subDataRowMap(result, start, endIndex);
			}
		}

		private Map<Long, DataRow> subDataRowMap(Map<Long, DataRow> data, long start, long end) {
			List<Long> keys = new ArrayList<Long>(data.keySet());
			Collections.sort(keys);
			Map<Long, DataRow> result = new HashMap<Long, DataRow>();
			long key;
			for (int i = (int) start; i < (int) end; i++) {
				key = keys.get(i);
				result.put(key, data.get(key));
			}
			return result;
		}

		@Override
		public Map<Long, DataRow> filterData(String column, Object value, FilterOperator operator)
		            throws DataServiceFault {
			Map<Long, DataRow> result = new HashMap<Long, DataRow>();
			for (Map.Entry<Long, DataRow> entry : this.data.entrySet()) {
				if (this.evaluateCriteria(entry.getValue(), column, value, operator)) {
					result.put(entry.getKey(), entry.getValue());
				}
			}
			return result;
		}

		private boolean evaluateCriteria(DataRow row, String column, Object value,
				FilterOperator operator) throws DataServiceFault {
			Object colValue;
			colValue = row.getValueAt(column);
			if (colValue == null) {
				return false;
			}
			switch (operator) {
			case EQUALS:
				if (!colValue.equals(value)) {
					return false;
				}
				break;
			case GREATER_THAN:
				double lhs = Double.parseDouble(colValue.toString());
				double rhs = Double.parseDouble(value.toString());
				if (!(lhs > rhs)) {
					return false;
				}
				break;
			case LESS_THAN:
				lhs = Double.parseDouble(colValue.toString());
				rhs = Double.parseDouble(value.toString());
				if (!(lhs < rhs)) {
					return false;
				}
				break;
			default:
				throw new DataServiceFault("Unsupported operator: "
						+ operator.toString());
			}
			return true;
		}

		@Override
		public void updateData(Map<Long, DataRow> values) {
			this.writeLock.lock();
			try {
				this.getData().putAll(values);
			} finally {
				this.writeLock.unlock();
			}
		}

		@Override
		public void insertData(DataRow... values) {
			this.writeLock.lock();
			try {
				for (DataRow value : values) {
				    this.getData().put(this.currentRowId++, value);
				}
			} finally {
				this.writeLock.unlock();
			}
		}

		@Override
		public void deleteData(long... ids) {
			this.writeLock.lock();
			try {
				for (long i : ids) {
					this.getData().remove(i);
				}
			} finally {
				this.writeLock.unlock();
			}
		}
		
	}

	@Override
	public void close() {
	}

}
