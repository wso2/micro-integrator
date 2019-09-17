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
package org.wso2.micro.integrator.dataservices.core.admin;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.micro.core.AbstractAdmin;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DBDeployer;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.description.config.GSpreadConfig;
import org.wso2.micro.integrator.dataservices.core.description.config.SQLCarbonDataSourceConfig;
import org.wso2.micro.integrator.dataservices.core.description.query.QueryFactory;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.DataServiceSerializer;
import org.wso2.micro.integrator.dataservices.core.script.DSGenerator;
import org.wso2.micro.integrator.dataservices.core.script.PaginatedTableInfo;
import org.wso2.micro.integrator.dataservices.core.sqlparser.SQLParserUtil;
import org.wso2.micro.core.util.Pageable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Data Services admin service class, for the basic functions.
 */
public class DataServiceAdminMi extends AbstractAdmin {

	private static final Log log = LogFactory.getLog(DataServiceAdminMi.class);

	public DataServiceAdminMi() {
	}

	/**
	 * Returns data service content as a String.
	 *
	 * @param serviceId
	 *            The data service name
	 * @return The data service configuration data
	 * @throws AxisFault
	 */
	public String getDataServiceContentAsString(String serviceId) throws AxisFault {
		AxisService axisService = getAxisConfig().getServiceForActivation(serviceId);
		StringBuffer fileContents = new StringBuffer();
		String filePath;

		// This is a workaround to fix DS-1075. The proper fix should be in kernel but it could break
		// existing functionality
		if (serviceId.contains("/")) {
			String[] splitArray = serviceId.split("\\/");
			if (splitArray.length >= 1) {
				serviceId = splitArray[splitArray.length - 1];
			}
		}

		// construct data service configuration file path-
		if (axisService != null) {
			filePath = ((DataService) axisService.getParameter(DBConstants.DATA_SERVICE_OBJECT)
					.getValue()).getDsLocation();
		} else {
			// Service could be a fault one. Loading contents directly from
			// repository
			URL repositoryURL = getAxisConfig().getRepository();
            String  repositoryURLPath = repositoryURL.getPath();
            if (repositoryURLPath != null && !repositoryURLPath.endsWith("/")) {
                repositoryURLPath = repositoryURLPath + "/";
            }
			filePath = repositoryURLPath + DBDeployer.DEPLOYMENT_FOLDER_NAME + File.separator
					+ serviceId + "." + DBConstants.DBS_FILE_EXTENSION;
		}

		// load file content into a string buffer
		if (filePath != null) {
			/*
			    Security Comment :
			    This path is trustworthy, constructed file path cannot be access by the user.
			*/
			File config = new File(filePath);
			try {
				FileReader fileReader = new FileReader(config);
				BufferedReader in = new BufferedReader(fileReader);
				String str;
				while ((str = in.readLine()) != null) {
					fileContents.append(str + "\n");
				}
				in.close();
			} catch (IOException e) {
				throw new AxisFault(
						"Error while reading the contents from the service config file for service '"
								+ serviceId + "'", e);
			}
		}
		return fileContents.toString();
	}

	protected String getDataServiceFileExtension() {
		ConfigurationContext configCtx = this.getConfigContext();
		String fileExtension = (String) configCtx.getProperty(DBConstants.DB_SERVICE_EXTENSION);
		return fileExtension;
	}

	/**
	 * Saves the data service in service repository.
	 * @param serviceName The name of the data service to be saved
	 * @param serviceHierarchy The hierarchical path of the service
	 * @param serviceContents The content of the service
	 * @throws AxisFault
	 */
	public void saveDataService(String serviceName, String serviceHierarchy,
			String serviceContents) throws AxisFault {
		String dataServiceFilePath;
		ConfigurationContext configCtx = this.getConfigContext();
		AxisConfiguration axisConfig = configCtx.getAxisConfiguration();

		AxisService axisService = DBUtils.getActiveAxisServiceAccordingToDataServiceGroup(axisConfig, serviceName);

		// This is a workaround to fix DS-1075. The proper fix should be in kernel but it could break
		// existing functionality
		if (serviceName.contains("/")) {
			String[] splitArray = serviceName.split("\\/");
			if (splitArray.length >= 1) {
				String fullServiceName = serviceName;
				serviceName = splitArray[splitArray.length - 1];
				serviceHierarchy = splitArray[splitArray.length - 2];
				serviceContents = serviceContents.replace(fullServiceName,serviceName);
			}
		}

		if (serviceHierarchy == null) {
			serviceHierarchy = "";
		}

		if (axisService == null) {
			/* new service */
			String axis2RepoDirectory = axisConfig.getRepository().getPath();
			String repoDirectory = (String) configCtx.getProperty(DBConstants.DB_SERVICE_REPO);
			String fileExtension = this.getDataServiceFileExtension();

			String dataServiceDirectory = axis2RepoDirectory + File.separator + repoDirectory
					+ File.separator + serviceHierarchy;
			dataServiceFilePath = dataServiceDirectory + File.separator + serviceName + "."
					+ fileExtension;

			/* create the directory, if it does not exist */
			/*
			    Security Comment :
			    This dataServiceDirectory path is trustworthy, constructed dataServiceDirectory path cannot be access by the user.
			*/
			File directory = new File(dataServiceDirectory);
			if (!directory.exists() && !directory.mkdirs()) {
				throw new AxisFault("Cannot create directory: " + directory.getAbsolutePath());
			}
		} else {
			dataServiceFilePath = ((DataService) axisService.getParameter(
					DBConstants.DATA_SERVICE_OBJECT).getValue()).getDsLocation();
			AxisServiceGroup axisServiceGroup = axisService.getAxisServiceGroup();
			axisServiceGroup.addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, Boolean.TRUE.toString());
			axisServiceGroup.addParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, Boolean.TRUE.toString());
			axisService.addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, Boolean.TRUE.toString());
			axisService.addParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, Boolean.TRUE.toString());
		}

		serviceContents = DBUtils.prettifyXML(serviceContents);

		/* save contents to .dbs file */
		try {
			/*
			    Security Comment :
			    This path is trustworthy, constructed file path cannot be access by the user.
			*/
			BufferedWriter out = new BufferedWriter(new FileWriter(dataServiceFilePath));
			out.write(serviceContents);
			out.close();
		} catch (IOException e) {
			log.error("Error while saving " + serviceName, e);
			throw new AxisFault(
					"Error occurred while writing the contents for the service config file for the new service "
							+ serviceName, e);
		}
	}

	/**
	 * This will test a connection to a given database. If connection can be
	 * made this method will return the status as String, if not, faliour String
	 * will be return.
	 *
	 * @param driverClass
	 *            Driver class
	 * @param jdbcURL
	 *            JDBC URL
	 * @param username
	 *            User name
	 * @param password
	 *            Pass word
	 * @return String; state
	 */
	public String testJDBCConnection(String driverClass, String jdbcURL, String username,
			String password, String passwordAlias) {
//		int tenantId =
//				PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
		Connection connection = null;
		try {
//			PrivilegedCarbonContext.startTenantFlow();
//			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

			String resolvePwd;
			if (driverClass == null || driverClass.length() == 0) {
				String message = "Driver class is missing";
				if (log.isDebugEnabled()) {
					log.debug(message);
				}
				return message;
			}
			if (jdbcURL == null || jdbcURL.length() == 0) {
				String message = "Driver connection URL is missing";
				if (log.isDebugEnabled()) {
					log.debug(message);
				}
				return message;
			}

			if (null != passwordAlias && !passwordAlias.isEmpty()) {
				resolvePwd = DBUtils.loadFromSecureVault(passwordAlias);
			} else {
				resolvePwd = password;
			}

			Class.forName(driverClass.trim());
			String message;
			if (null != username && !username.isEmpty()) {
				connection = DriverManager.getConnection(jdbcURL, username, resolvePwd);
				message = "Database connection is successful with driver class " + driverClass + " , jdbc url " +
				          jdbcURL + " and user name " + username;
			} else {
				connection = DriverManager.getConnection(jdbcURL);
				message = "Database connection is successful with driver class " + driverClass + " , jdbc url " +
				          jdbcURL;
			}
			if (log.isDebugEnabled()) {
				log.debug(message);
			}
			return message;
		} catch (SQLException e) {
			String message;
			if (null != username && !username.isEmpty()) {
				message = "Could not connect to database " + jdbcURL + " with username " + username;
			} else {
				message = "Could not connect to database " + jdbcURL;
			}
			log.error(message, e);
			return message;
		} catch (ClassNotFoundException e) {
			String message = "Driver class " + driverClass + " can not be loaded";
			log.error(message, e);
			return message;
		} catch (Exception e) {
            String message = "Could not connect to database " + jdbcURL + ", Error message - " + e.getMessage();
            return message;
        } finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
//			PrivilegedCarbonContext.endTenantFlow();
		}
	}

    /**
     * This will test the connection(retrieve CallFeed) of a Google spreadsheet
     * document. If connection can be made this method will return the status as
     * String, if not, failure String will be return.
     *
     * @param user
     *            - user name
     * @param password
     *            - password
     * @param visibility
     *            - Whether its private or public
     * @param documentURL
     *            - Google spreadsheet URL
     * @return string State
     */

    /**
     * This will test the connection(retrieve CallFeed) of a Google spreadsheet
     * document. If connection can be made this method will return the status as
     * String, if not, failure String will be return.
     *
     * @param clientId from developer console
     * @param clientSecret from developer console
     * @param refreshToken generated refresh token
     * @param visibility Whether its private or public
     * @param documentURL Google spreadsheet URL
     * @return string State
     */
    public String testGSpreadConnection(String clientId, String clientSecret, String refreshToken, String visibility,
                                        String documentURL) {
        if (DBUtils.isEmptyString(documentURL)) {
            String message = "Document URL is empty";
            log.debug(message);
            return message;
        }
        String key;
        SpreadsheetService service = new SpreadsheetService("GSpread Connection Service");
        try {
            key = GSpreadConfig.extractKey(documentURL);
        } catch (DataServiceFault e) {
            String message = "Invalid documentURL:" + documentURL;
            log.error(message, e);
            return message;
        }

        if (!visibility.equals("public")) {
            if (DBUtils.isEmptyString(clientId)) {
                String message = "clientId field is empty";
                log.error(message);
                return message;
            }
            if (DBUtils.isEmptyString(clientSecret)) {
                String message = "clientSecret field is empty";
                log.error(message);
                return message;
            }
            if (DBUtils.isEmptyString(refreshToken)) {
                String message = "refreshToken field is empty";
                log.error(message);
                return message;
            }
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jsonFactory = new JacksonFactory();
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setClientSecrets(clientId, clientSecret)
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .build();
            credential.setRefreshToken(refreshToken);
            try {
                credential.refreshToken();
            } catch (IOException e) {
                String message = "Google spreadsheet connection failed, Error refreshing the token ";
                log.debug(message);
                return message;
            }
            service.setOAuth2Credentials(credential);
        }

        String worksheetFeedURL = GSpreadConfig.BASE_WORKSHEET_URL + key + "/" + visibility
                                  + "/basic";
        try {
            URL url = new URL(worksheetFeedURL);
            try {
                service.getFeed(url,  CellFeed.class);
                String message = "Google spreadsheet connection is successfull ";
                log.debug(message);
                return message;
            } catch (AuthenticationException e) {
                String message = "Invalid Credentials";
                log.error(message,e);
                return message;
            } catch (IOException e) {
                String message = "URL Not found:" + documentURL;
                log.error(message,e);
                return message;
            } catch (ServiceException e) {
                String message = "URL Not found:" + documentURL;
                log.error(message,e);
                return message;
            }
        } catch (MalformedURLException e) {
            String message = "Invalid documentURL:" + documentURL;
            log.error(message,e);
            return message;
        }

    }

	/**
	 * Return data services
	 *
	 * @return names of the data services
	 * @throws AxisFault AxisFault
	 */
	public String[] getAvailableDS() throws AxisFault {
		return DBUtils.getAvailableDS(getAxisConfig());
	}

	/**
	 * This method check whether data service name is already available.
	 *
	 * @param dataService Data Service Name
	 * @return boolean value
	 * @throws AxisFault
	 */
	public boolean isDSNameAvailable(String dataService) throws AxisFault {
		return DBUtils.isAvailableDS(getAxisConfig(), dataService);
	}

	public String[] getCarbonDataSourceNames() {
		List<String> list = SQLCarbonDataSourceConfig.getCarbonDataSourceNames();
		return list.toArray(new String[list.size()]);
	}
	
	public String[] getCarbonDataSourceNamesForTypes(String[] types) {
		List<String> list = SQLCarbonDataSourceConfig.getCarbonDataSourceNamesForType(types);
		return list.toArray(new String[list.size()]);
	}
	
	public String getCarbonDataSourceType(String dsName) {
		return SQLCarbonDataSourceConfig.getCarbonDataSourceType(dsName);
	}

	public String[] getOutputColumnNames(String sql) throws Exception {
		try {
            List<String> columns = SQLParserUtil.extractOutputColumns(sql);
            return columns.toArray(new String[columns.size()]);
		} catch (Exception e) {
			throw new AxisFault("Error occurred while generating response for the query " + sql +
                    ".", e);
		}
    }

    public String[] getInputMappingNames(String sql) throws Exception {
		try {
			List<String> inputMappings = SQLParserUtil.extractInputMappingNames(sql);
            return inputMappings.toArray(new String[inputMappings.size()]);
		} catch (Exception e) {
			throw new AxisFault("Error occurred while generating input mappings for the query " +
                    sql + ".", e);
		}
    }

	public String[] getdbSchemaList(String datasourceId) throws Exception {
		return DSGenerator.getSchemas(datasourceId);
	}

    public PaginatedTableInfo getPaginatedSchemaInfo(int pageNumber, String datasourceId)
            throws Exception {
        List<String> temp = new ArrayList<String>();
        Collections.addAll(temp, getdbSchemaList(datasourceId));
        // Pagination
        PaginatedTableInfo paginatedTableInfo = new PaginatedTableInfo();
        doPaging(pageNumber, temp, paginatedTableInfo);
        return paginatedTableInfo;
    }

	public String[] getTableList(String datasourceId, String dbName, String[] schemas) throws AxisFault {
		try {
		    return DSGenerator.getTableList(datasourceId, dbName, schemas);
		} catch (Exception e) {
			throw new AxisFault("Error in retrieving table list: " + e.getMessage(), e);
		}
	}

    public PaginatedTableInfo getPaginatedTableInfo(int pageNumber, String datasourceId,
                                                    String dbName, String[] schemas) throws Exception {
        List<String> tableInfoList = Arrays.asList(getTableList(datasourceId, dbName, schemas));

        // Pagination
        PaginatedTableInfo paginatedTableInfo = new PaginatedTableInfo();
        doPaging(pageNumber, tableInfoList, paginatedTableInfo);
        return paginatedTableInfo;
    }



	/**
	 * Return the generated services name list
	 */
	public String[] getDSServiceList(String dataSourceId, String dbName, String[] schemas,
			String[] tableNames, boolean singleService,String serviceNamespace) throws Exception {
		DSGenerator generator = new DSGenerator(dataSourceId, dbName, schemas, tableNames, false, serviceNamespace, "");
		List<String> serviceNames = new ArrayList<String>();
		List<DataService> dsList = generator.getGeneratedServiceList();
		for (DataService ds : dsList) {
			OMElement element = DataServiceSerializer.serializeDataService(ds);
			this.saveDataService(ds.getName(), null, element.toString());
			serviceNames.add(ds.getName());
		}
		return serviceNames.toArray(new String[serviceNames.size()]);
	}

	/**
	 * Return the generated service name
	 */
	public String getDSService(String dataSourceId, String dbName, String[] schemas,
			String[] tableNames, boolean singleService,String serviceName,String serviceNamespace) throws Exception {
		DSGenerator generator = new DSGenerator(dataSourceId, dbName, schemas, tableNames, true, serviceNamespace, serviceName);
		DataService dataservice = generator.getGeneratedService();
		OMElement element = DataServiceSerializer.serializeDataService(dataservice);
		this.saveDataService(dataservice.getName(),	null, element.toString());
		return generator.getGeneratedService().getName();
	}

     /**
     * A reusable generic method for doing item paging
     *
     * @param pageNumber The page required. Page number starts with 0.
     * @param sourceList The original list of items
     * @param pageable          The type of Pageable item
     * @return Returned page
     */
    private static <C> List<C> doPaging(int pageNumber, List<C> sourceList, Pageable pageable) {
        if (pageNumber < 0 || pageNumber == Integer.MAX_VALUE) {
            pageNumber = 0;
        }
        if (sourceList.size() == 0) {
            return sourceList;
        }
        if (pageNumber < 0){
            throw new RuntimeException("Page number should be a positive integer. " +
                                       "Page numbers begin at 0.");
        }
        int itemsPerPageInt = 60; // the default number of item per page
        int numberOfPages = (int) Math.ceil((double) sourceList.size() / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        List<C> returnList = new ArrayList<C>();
        for (int i = startIndex; i < endIndex && i < sourceList.size(); i++) {
            returnList.add(sourceList.get(i));
        }
        pageable.setNumberOfPages(numberOfPages);
        pageable.set(returnList);
        return returnList;
    }
    
    public String validateJSONMapping(String jsonMapping) {
        try {
            QueryFactory.getJSONResultFromText(jsonMapping);
            return "";
        } catch (DataServiceFault e) {
            return e.getDsFaultMessage();
        }
    }

    /**
     * Service method  to get all the user roles for use in role based filtering when creating data services.
     *
     * @param authProviderConfig xml config of the authentication provider
     * @return String array of roles
     * @throws AxisFault
     */
    public String[] getAllRoles(String authProviderConfig) throws AxisFault {
        try {
            return DBUtils.getAllRolesUsingAuthorizationProvider(authProviderConfig);
        } catch (DataServiceFault e) {
            throw new AxisFault("Error in retrieving role list: " + e.getMessage(), e);
        }
    }
    
}
