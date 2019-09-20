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

import java.util.List;
import java.util.Map;

import org.wso2.micro.integrator.dataservices.core.datasource.TabularDataBasedDS.FilterOperator;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This represents a data table in a custom data source.
 */
public interface DataTable {

	/**
	 * Returns all the data table columns available.
	 * @return The list of data columns
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	List<DataColumn> getDataColumns() throws DataServiceFault;
	
	/**
	 * Returns all the data in the table.
	 * @param start The 0-based starting index of the results to be returned
	 * @param length The length of the result set size to be limited to, -1 if unlimited
	 * @return A map of data rows, keyed by a unique value to identify the row
	 * @throws DatListaServiceFault Thrown if any error occurs 
	 */
	Map<Long, DataRow> getData(long start, long length) throws DataServiceFault;
	
	/**
	 * Updates the data table with the given values.
	 * @param values The rows to be updated
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	void updateData(Map<Long, DataRow> values) throws DataServiceFault;
	
	/**
	 * Inserts a new row in to the data table.
	 * @param values The values of the new data row
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	void insertData(DataRow... values) throws DataServiceFault;
	
	/**
	 * Deletes rows from the data table.
	 * @param ids The row ids of the rows to be deleted
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	void deleteData(long... ids) throws DataServiceFault;
	
	/**
	 * Returns filtered data according to the given criteria. 
	 * @param column The column the filtering to be based on
	 * @param value The value used to evaluate the criteria
	 * @param operator The operator used to do the evaluation
	 * @return The filtered data
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	Map<Long, DataRow> filterData(String column, Object value, FilterOperator operator)
            throws DataServiceFault;
	
}
