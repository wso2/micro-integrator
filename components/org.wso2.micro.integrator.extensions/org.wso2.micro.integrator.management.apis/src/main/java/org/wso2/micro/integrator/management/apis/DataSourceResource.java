/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.management.apis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;
import org.wso2.micro.integrator.ndatasource.core.DataSourceManager;
import org.wso2.micro.integrator.ndatasource.core.DataSourceMetaInfo;
import org.wso2.micro.integrator.ndatasource.core.DataSourceRepository;
import org.wso2.micro.integrator.ndatasource.core.utils.DataSourceUtils;


import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.wso2.micro.integrator.management.apis.Constants.SEARCH_KEY;

public class DataSourceResource implements MiApiResource {

    private static Log LOG = LogFactory.getLog(DataSourceResource.class);
    Set<String> methods;

    private static final String DESCRIPTION = "description";
    private static final String JNDIConfig = "jndiConfig";
    private static final String TYPE = "type";
    private static final String DRIVER = "driverClass";
    private static final String URL = "url";
    private static final String USER_NAME = "userName";
    private static final String DEFAULT_AUTO_COMMIT = "isDefaultAutoCommit";
    private static final String DEFAULT_READ_ONLY = "isDefaultReadOnly";
    private static final String VALIDATION_QUERY = "validationQuery";
    private static final String CONFIGURATION_PARAMETERS = "configurationParameters";
    private static final String REMOVE_ABANDONED = "removeAbandoned";
    private static final String MAX_ACTIVE = "maxActive";
    private static final String MAX_WAIT = "maxWait";
    private static final String MAX_IDLE = "maxIdle";
    private static final String MAX_AGE = "maxAge";
    private static final String VALIDATION_TIMEOUT = "validationQueryTimeout";

    public DataSourceResource() {

        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
    }

    @Override
    public Set<String> getMethods() {

        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext,
                          org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {

        String httpMethod = axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY) != null
                ? axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY).toString() : "method undefined";

        String datasourceName = Utils.getQueryParameter(messageContext, Constants.NAME);
        String searchKey = Utils.getQueryParameter(messageContext, SEARCH_KEY);

        JSONObject response;
        try {
            DataSourceManager dsManager = DataSourceManager.getInstance();
            DataSourceRepository dataSourceRepository = dsManager.getDataSourceRepository();
            if (Objects.nonNull(datasourceName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Handling " + httpMethod + " request for " + datasourceName);
                }
                response = getDatasourceInformation(axis2MessageContext, dataSourceRepository, datasourceName);
            } else if (Objects.nonNull(searchKey) && !searchKey.trim().isEmpty()) {
                response = populateSearchResults(dataSourceRepository, searchKey.toLowerCase());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Handling " + httpMethod + " request to fetch data source list");
                }
                response = getDatasourceList(dataSourceRepository);
            }
        } catch (DataSourceException e) {
            response = Utils.createJsonError("error while processing request", e, axis2MessageContext, Constants.INTERNAL_SERVER_ERROR);
        }
        Utils.setJsonPayLoad(axis2MessageContext, response);
        return true;
    }

    private List<CarbonDataSource> getSearchResults(DataSourceRepository dataSourceRepository, String searchKey) {
        Collection<CarbonDataSource> dataSources = dataSourceRepository.getAllDataSources();
        return dataSources.stream()
                .filter(artifact -> artifact.getDSMInfo().getName().toLowerCase().contains(searchKey))
                .collect(Collectors.toList());
    }

    /**
     * Reurns the JSON Object containing all available data-sources according to search and pagination parameters.
     *
     * @param dataSourceRepository DataSourceRepository
     * @param searchKey String
     * @return JSONObject response
     */
    private JSONObject populateSearchResults(DataSourceRepository dataSourceRepository, String searchKey) {
        List<CarbonDataSource> resultsList = getSearchResults(dataSourceRepository, searchKey);
        JSONObject datasourceList = Utils.createJSONList(resultsList.size());
        resultsList.forEach((dataSource) -> addToJsonList(dataSource, datasourceList.getJSONArray(Constants.LIST)));
        return datasourceList;
    }

    /**
     * Reurns the JSON Object containing all available data-sources.
     *
     * @param dataSourceRepository DataSourceRepository
     * @return JSONObject response
     */
    private JSONObject getDatasourceList(DataSourceRepository dataSourceRepository) {

        Collection<CarbonDataSource> datasources = dataSourceRepository.getAllDataSources();
        JSONObject datasourceList = Utils.createJSONList(datasources.size());
        datasources.forEach((dataSource) -> addToJsonList(dataSource, datasourceList.getJSONArray(Constants.LIST)));
        return datasourceList;
    }

    /**
     * Adds summary data source object to json Array.
     *
     * @param dataSource     CarbonDataSource
     * @param datasourceList JSONArray
     */
    private void addToJsonList(CarbonDataSource dataSource, JSONArray datasourceList) {

        JSONObject datasourceJsonObject = new JSONObject();
        DataSourceMetaInfo dataSourceMetaInfo = dataSource.getDSMInfo();
        datasourceJsonObject.put(Constants.NAME, dataSourceMetaInfo.getName());
        datasourceJsonObject.put(TYPE, dataSourceMetaInfo.getDefinition().getType());
        datasourceList.put(datasourceJsonObject);
    }

    /**
     * Returns the JSON Object comprising of specified datasource.
     *
     * @param axis2MessageContext  Axis2 Message context
     * @param dataSourceRepository DataSourceRepository
     * @param datasourceName       String
     * @return JSONObject Response
     * @throws DataSourceException
     */
    private JSONObject getDatasourceInformation(org.apache.axis2.context.MessageContext axis2MessageContext,
                                                DataSourceRepository dataSourceRepository,
                                                String datasourceName) throws DataSourceException {

        JSONObject datasourceInformation;
        CarbonDataSource dataSource = dataSourceRepository.getDataSource(datasourceName);
        if (Objects.nonNull(dataSource)) {
            datasourceInformation = new JSONObject();
            DataSourceMetaInfo dataSourceMetaInfo = dataSource.getDSMInfo();
            datasourceInformation.put(Constants.NAME, dataSourceMetaInfo.getName());
            datasourceInformation.put(DESCRIPTION, dataSourceMetaInfo.getDescription());
            datasourceInformation.put(TYPE, dataSourceMetaInfo.getDefinition().getType());
            datasourceInformation.put(Constants.SYNAPSE_CONFIGURATION, DataSourceUtils.
                    elementToStringWithMaskedPasswords(
                            (Element) dataSource.getDSMInfo().getDefinition().getDsXMLConfiguration()));

            if (dataSource.getDSObject() instanceof DataSource) {
                DataSource dataSourceObject = (DataSource) dataSource.getDSObject();
                PoolConfiguration pool = dataSourceObject.getPoolProperties();
                datasourceInformation.put(DRIVER, pool.getDriverClassName());
                datasourceInformation.put(URL, DataSourceUtils.maskURLPassword(pool.getUrl()));
                datasourceInformation.put(USER_NAME, pool.getUsername());
                // set configuration parameters
                JSONObject configParameters = new JSONObject();
                datasourceInformation.put(CONFIGURATION_PARAMETERS, configParameters);
                configParameters.put(DEFAULT_AUTO_COMMIT, pool.isDefaultAutoCommit());
                configParameters.put(DEFAULT_READ_ONLY, pool.isDefaultReadOnly());
                configParameters.put(REMOVE_ABANDONED, pool.isRemoveAbandoned());
                configParameters.put(VALIDATION_QUERY, pool.getValidationQuery());
                configParameters.put(VALIDATION_TIMEOUT, pool.getValidationQueryTimeout());
                configParameters.put(MAX_ACTIVE, pool.getMaxActive());
                configParameters.put(MAX_IDLE, pool.getMaxIdle());
                configParameters.put(MAX_WAIT, pool.getMaxWait());
                configParameters.put(MAX_AGE, pool.getMaxAge());
            }
        } else {
            datasourceInformation = Utils.createJsonError("datasource " + datasourceName + " does not exist",
                    axis2MessageContext, Constants.NOT_FOUND);
        }
        return datasourceInformation;
    }
}
