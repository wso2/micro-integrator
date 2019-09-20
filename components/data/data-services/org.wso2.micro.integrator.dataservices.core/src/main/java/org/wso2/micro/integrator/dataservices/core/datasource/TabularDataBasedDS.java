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
import java.util.Set;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This interface represents a tabular data based custom data source 
 * implementation in data services.
 */
public interface TabularDataBasedDS extends CustomDataSourceBase {
	
	/**
	 * Returns all the table names of the custom data source.
	 * @return The table names
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	Set<String> getDataTableNames() throws DataServiceFault;
	
	/**
	 * Returns the data table with the given name.
	 * @param name The name of the data table
	 * @return The data table object
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	DataTable getDataTable(String name) throws DataServiceFault;
	
	/**
	 * Creates a data table with the given name and the column information.
	 * @param name The name of the data table to be created
	 * @param columns A list of data column definitions
	 */
	void createDataTable(String name, List<DataColumn> columns);
	
	/**
	 * Drops the data table with the given name.
	 * @param name The name of the data table to be deleted
	 */
	void dropDataTable(String name);
	
	public static enum FilterOperator {
		EQUALS,
		LESS_THAN,
		GREATER_THAN
	}
	
}
