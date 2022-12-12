/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.management.apis;

import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.description.config.Config;
import org.wso2.micro.integrator.dataservices.core.description.operation.Operation;
import org.wso2.micro.integrator.dataservices.core.description.query.Query;
import org.wso2.micro.integrator.dataservices.core.description.resource.Resource;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.DataServiceSerializer;
import org.wso2.micro.integrator.management.apis.models.dataServices.DataServiceInfo;
import org.wso2.micro.integrator.management.apis.models.dataServices.DataServiceSummary;
import org.wso2.micro.integrator.management.apis.models.dataServices.DataServicesList;
import org.wso2.micro.integrator.management.apis.models.dataServices.DataSourceInfo;
import org.wso2.micro.integrator.management.apis.models.dataServices.OperationInfo;
import org.wso2.micro.integrator.management.apis.models.dataServices.QuerySummary;
import org.wso2.micro.integrator.management.apis.models.dataServices.ResourceInfo;
import org.wso2.micro.service.mgt.ServiceAdmin;
import org.wso2.micro.service.mgt.ServiceMetaData;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;

import java.util.stream.Collectors;

import static org.wso2.micro.integrator.management.apis.Constants.SEARCH_KEY;

public class DataServiceResource extends APIResource {

    private static final Log log = LogFactory.getLog(DataServiceResource.class);

    private static ServiceAdmin serviceAdmin = null;

    public DataServiceResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        return methods;
    }

    @Override
    public boolean invoke(MessageContext msgCtx) {
        buildMessage(msgCtx);
        if (serviceAdmin == null) {
            serviceAdmin = Utils.getServiceAdmin(msgCtx);
        }
        String param = Utils.getQueryParameter(msgCtx, "dataServiceName");
        String searchKey = Utils.getQueryParameter(msgCtx, SEARCH_KEY);

        try {
            if (param != null) {
                // data-service specified by name
                populateDataServiceByName(msgCtx, param);
            } else if (Objects.nonNull(searchKey) && !searchKey.trim().isEmpty()) {
                populateSearchResults(msgCtx, searchKey.toLowerCase());
            } else {
                // list of all data-services
                populateDataServiceList(msgCtx);
            }
        } catch (Exception exception) {
            log.error("Error while populating service: ", exception);
            msgCtx.setProperty(Constants.HTTP_STATUS_CODE, Constants.INTERNAL_SERVER_ERROR);
        }

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private List<String> getSearchResults(MessageContext messageContext, String searchKey) throws AxisFault {
        SynapseConfiguration configuration = messageContext.getConfiguration();
        AxisConfiguration axisConfiguration = configuration.getAxisConfiguration();
        return Arrays.stream(DBUtils.getAvailableDS(axisConfiguration))
                .filter(serviceName -> serviceName.toLowerCase().contains(searchKey)).collect(Collectors.toList());
    }

    private void populateSearchResults(MessageContext messageContext, String searchKey) throws Exception {
        List<String> resultsList = getSearchResults(messageContext, searchKey);
        setResponseBody(resultsList, messageContext);
    }

    private void setResponseBody(List<String> dataServicesNames, MessageContext messageContext) throws Exception {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        DataServicesList dataServicesList = new DataServicesList(dataServicesNames.size());

        for (String dataServiceName : dataServicesNames) {
            DataService dataService = getDataServiceByName(messageContext, dataServiceName);
            ServiceMetaData serviceMetaData = getServiceMetaData(dataService);
            // initiate summary model
            DataServiceSummary summary = null;
            if (serviceMetaData != null) {
                summary = new DataServiceSummary(serviceMetaData.getName(), serviceMetaData.getWsdlURLs());
            }
            dataServicesList.addServiceSummary(summary);
        }
        String stringPayload = new Gson().toJson(dataServicesList);
        Utils.setJsonPayLoad(axis2MessageContext, new JSONObject(stringPayload));
    }
    private void populateDataServiceList(MessageContext msgCtx) throws Exception {
        SynapseConfiguration configuration = msgCtx.getConfiguration();
        AxisConfiguration axisConfiguration = configuration.getAxisConfiguration();
        List<String> dataServicesNames = Arrays.stream(DBUtils.getAvailableDS(axisConfiguration)).collect(Collectors.toList());
        setResponseBody(dataServicesNames, msgCtx);
    }

    private void populateDataServiceByName(MessageContext msgCtx, String serviceName) throws Exception {
        DataService dataService = getDataServiceByName(msgCtx, serviceName);

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();

        if (dataService == null) {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        } else {
            ServiceMetaData serviceMetaData = getServiceMetaData(dataService);
            DataServiceInfo dataServiceInfo = null;

            if (serviceMetaData != null) {
                dataServiceInfo = new DataServiceInfo(serviceMetaData.getName(), serviceMetaData.getDescription(),
                                                      serviceMetaData.getServiceGroupName(),
                                                      serviceMetaData.getWsdlURLs(), serviceMetaData.getSwaggerUrl());

                Map<String, Query> queries = dataService.getQueries();
                for (Map.Entry<String, Query> stringQuery : queries.entrySet()) {
                    QuerySummary querySummary = new QuerySummary();
                    querySummary.setId(stringQuery.getKey());
                    querySummary.setNamespace(stringQuery.getValue().getNamespace());
                    querySummary.setConfigId(stringQuery.getValue().getConfigId());
                    dataServiceInfo.addQuery(querySummary);
                }
            }
            dataServiceInfo.setDataSources(getDataSources(dataService));
            dataServiceInfo.setResources(getResources(dataService));
            dataServiceInfo.setOperations(getOperations(dataService));
            dataServiceInfo.setConfiguration(DataServiceSerializer.serializeDataService(dataService, true).toString());
            String stringPayload = new Gson().toJson(dataServiceInfo);
            Utils.setJsonPayLoad(axis2MessageContext, new JSONObject(stringPayload));
        }
    }

    private DataService getDataServiceByName(MessageContext msgCtx, String serviceName) {
        AxisService axisService = msgCtx.getConfiguration().
                getAxisConfiguration().getServiceForActivation(serviceName);
        DataService dataService = null;
        if (axisService != null) {
            dataService = (DataService) axisService.getParameter(DBConstants.DATA_SERVICE_OBJECT).getValue();
        } else {
            log.debug("DataService {} is null.");
        }
        return dataService;
    }

    private ServiceMetaData getServiceMetaData(DataService dataService) throws Exception {
        if (dataService != null) {
            return serviceAdmin.getServiceData(dataService.getName());
        } else {
            return null;
        }
    }

    private List<ResourceInfo> getResources(DataService dataService) {

        Set<Resource.ResourceID> resourceIDS = dataService.getResourceIds();
        List<ResourceInfo> resourceList = new ArrayList<>();
        for (Resource.ResourceID id : resourceIDS) {
            ResourceInfo resourceInfo = new ResourceInfo();
            Resource resource = dataService.getResource(id);
            resourceInfo.setResourcePath(id.getPath());
            resourceInfo.setResourceMethod(id.getMethod());
            resourceInfo.setResourceQuery(resource.getCallQuery().getQueryId());
            resourceInfo.setQueryParams(resource.getCallQuery().getQuery().getQueryParams());
            resourceList.add(resourceInfo);
        }
        return resourceList;
    }

    private List<OperationInfo> getOperations(DataService dataService) {

        List<OperationInfo> opertionList = new ArrayList<>();
        Set<String> operationNames = dataService.getOperationNames();
        for (String operationName : operationNames) {
            OperationInfo operationInfo = new OperationInfo();
            Operation operation = dataService.getOperation(operationName);
            operationInfo.setOperationName(operationName);
            operationInfo.setQueryName(operation.getCallQuery().getQueryId());
            operationInfo.setQueryParams(operation.getCallQuery().getQuery().getQueryParams());
            opertionList.add(operationInfo);
        }
        return opertionList;
    }

    private List<DataSourceInfo> getDataSources(DataService dataService) {

        Map<String, Config> configs = dataService.getConfigs();
        List<DataSourceInfo> dataSources = new ArrayList<>();
        configs.forEach((name, config) -> {
            DataSourceInfo dataSource = new DataSourceInfo();
            dataSource.setDataSourceId(config.getConfigId());
            dataSource.setDataSourceType(config.getType());
            dataSource.setDataSourceProperties(config.getProperties());
            dataSources.add(dataSource);
        });
        return dataSources;
    }
}
