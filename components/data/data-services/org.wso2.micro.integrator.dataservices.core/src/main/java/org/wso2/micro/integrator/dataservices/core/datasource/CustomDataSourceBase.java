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

import java.util.Map;import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This interface represents the common functionality for a custom data source 
 * implementation in data services.
 */
public interface CustomDataSourceBase {

	/**
	 * Initialized the custom data source.
	 * @param props The properties used for initialization
	 * @throws DataServiceFault Thrown if any error occurs
	 */
	void init(Map<String, String> props) throws DataServiceFault;

	/**
	 * Closes the data source.
	 */
	void close();
	
}
