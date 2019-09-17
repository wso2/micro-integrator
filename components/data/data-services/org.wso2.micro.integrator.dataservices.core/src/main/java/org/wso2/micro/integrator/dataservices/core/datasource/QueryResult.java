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

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This class represent the result for a custom query execution. 
 */
public interface QueryResult {

	/**
	 * Returns all the data table columns available.
	 * @return The list of data columns
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	List<DataColumn> getDataColumns() throws DataServiceFault;
	
	/**
	 * Checks if there are any result records left.
	 * @return True if there are more records to be read
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	boolean hasNext() throws DataServiceFault;
	
	/**
	 * Reads in the next record in the result set.
	 * @return The next available record
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	DataRow next() throws DataServiceFault;
	
}
