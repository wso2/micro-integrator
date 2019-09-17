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
package org.wso2.micro.integrator.dataservices.sql.driver.processor.reader;

import java.sql.Connection;
import java.sql.SQLException;

import org.wso2.micro.integrator.dataservices.sql.driver.TCustomConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.TCustomConnection.CustomDataSource;

/**
 * This class represents a data reader class for a custom data source.
 */
public class CustomDataReader implements DataReader {

	private CustomDataSource dataSource;
	
	public CustomDataReader(Connection connection) throws SQLException {
		this.dataSource = ((TCustomConnection) connection).getDataSource();
	}

	public CustomDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public void populateData() throws SQLException {		
	}

	@Override
	public DataTable getDataTable(String name) throws SQLException {
		return this.getDataSource().getDataTable(name);
	}
	
}
