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
package org.wso2.micro.integrator.ndatasource.rdbms;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.common.spi.DataSourceReader;
import org.wso2.micro.core.util.CarbonUtils;

/**
 * This class represents the RDBMS based data source reader implementation.
 */
public class RDBMSDataSourceReader implements DataSourceReader {
	
	@Override
	public String getType() {
		return RDBMSDataSourceConstants.RDBMS_DATASOURCE_TYPE;
	}

	public static RDBMSConfiguration loadConfig(String xmlConfiguration)
			throws DataSourceException {
		try {
            xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
		    JAXBContext ctx = JAXBContext.newInstance(RDBMSConfiguration.class);
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(new StringReader(xmlConfiguration));

		    return (RDBMSConfiguration) ctx.createUnmarshaller().unmarshal(xmlReader);
		} catch (Exception e) {
			throw new DataSourceException("Error in loading RDBMS configuration: " +
		            e.getMessage(), e);
		}
	}

	@Override
	public Object createDataSource(String xmlConfiguration, boolean isDataSourceFactoryReference)
			throws DataSourceException {
		if (isDataSourceFactoryReference) {
			return (new RDBMSDataSource(loadConfig(xmlConfiguration)).getDataSourceFactoryReference());
		} else {
			return (new RDBMSDataSource(loadConfig(xmlConfiguration)).getDataSource());
		}
	}

	@Override
	public boolean testDataSourceConnection(String xmlConfiguration) throws DataSourceException {
		RDBMSConfiguration rdbmsConfiguration = loadConfig(xmlConfiguration);
		DataSource dataSource = new RDBMSDataSource(rdbmsConfiguration).getDataSource();

		Connection connection = null;
		Connection testConnection = null;
		try {
			Class.forName(rdbmsConfiguration.getDriverClassName());
			if (rdbmsConfiguration.getUsername() != null) {
				testConnection = DriverManager
						.getConnection(rdbmsConfiguration.getUrl(), rdbmsConfiguration.getUsername(),
								rdbmsConfiguration.getPassword());
			} else {
				testConnection = DriverManager.getConnection(rdbmsConfiguration.getUrl());
			}
		} catch (ClassNotFoundException e) {
			throw new DataSourceException("Error loading Driver class:" + e.getMessage(), e);
		} catch (SQLException e) {
			if (e.getSQLState().equals("08001")) {
				throw new DataSourceException("The data source URL is not accepted by any of the loaded drivers. "
				                              + e.getMessage(), e);
			} else if (e.getSQLState().equals("28000")) {
				throw new DataSourceException("The user is not associated with a trusted SQL Server connection."
				                              + e.getMessage(), e);
			} else {
				throw new DataSourceException("Error establishing data source connection: " + e.getMessage(), e);
			}
		} finally {
			if (testConnection != null) {
				try {
					testConnection.close();
				} catch (SQLException ignored) {

				}
			}
		}
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			throw new DataSourceException("Error establishing data source connection: " + e.getMessage(), e);
		}
		if (connection != null) {
			String validationQuery = rdbmsConfiguration.getValidationQuery();
			if (validationQuery != null && !"".equals(validationQuery)) {
				try (PreparedStatement ps = connection.prepareStatement(validationQuery.trim())) {
					ps.execute();
				} catch (SQLException e) {
					throw new DataSourceException("Error during executing validation query: " + e.getMessage(), e);
				}
			}
			try {
				connection.close();
			} catch (SQLException ignored) {

			}
		}
		return true;
	}

}
