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
package org.wso2.micro.integrator.dataservices.core.engine;

import org.apache.axis2.description.AxisResource;
import org.apache.axis2.description.AxisResourceMap;
import org.apache.axis2.description.AxisResourceParameter;
import org.apache.axis2.description.AxisResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;
import org.wso2.micro.integrator.dataservices.common.DBConstants.ResultTypes;
import org.wso2.micro.integrator.dataservices.common.DBConstants.ServiceStatusValues;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.DataServiceUser;
import org.wso2.micro.integrator.dataservices.core.auth.AuthorizationProvider;
import org.wso2.micro.integrator.dataservices.core.description.config.Config;
import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;
import org.wso2.micro.integrator.dataservices.core.description.operation.Operation;
import org.wso2.micro.integrator.dataservices.core.description.operation.OperationFactory;
import org.wso2.micro.integrator.dataservices.core.description.query.Query;
import org.wso2.micro.integrator.dataservices.core.description.resource.Resource;
import org.wso2.micro.integrator.dataservices.core.description.xa.DSSXATransactionManager;
import org.wso2.securevault.SecretResolver;

import javax.transaction.TransactionManager;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.List;

/**
 * This class is the logical representation of a data service, and is the
 * location of all the queries, operations and configurations associated with a specific DS.
 */
public class DataService implements AxisResources {

    private static final Log log = LogFactory.getLog(DataService.class);

    /**
     * Name of the data service
     */
    private String name;

    /**
     * Service namespace
     */
    private String serviceNamespace;

    /**
     * All the requests that are handled by the dataservice, i.e. operations, resources
     */
    private Map<String, CallableRequest> callableRequests;

    /**
     * Operations which belongs to the dataservice
     */
    private Map<String, Operation> operations;

    /**
     * Resources which belongs to the dataservice, this maps a resource id to an resource,
     * where a resource id contains a path and a HTTP method
     */
    private Map<Resource.ResourceID, Resource> resourceMap;

    /**
     * Data source configurations that are contained in the dataservice
     */
    private Map<String, Config> configs;

    /**
     * Queries defined in this dataservice
     */
    private Map<String, Query> queries;

    /**
     * Event triggers used in this dataservice, i.e. input/output event-triggers
     */
    private Map<String, EventTrigger> eventTriggers;

    /**
     * Description of the dataservice
     */
    private String description;

    /**
     * password manager configuration of the data service
     */
    private SecretResolver secretResolver;

    /**
     * The default namespace to be used, when the user doesn't explicitly mention the namespace
     * to be used in the result of the dataservice. The default namepace is given the
     * "baseURI" attribute, in the dataservice document element.
     */
    private String defaultNamespace;

    /**
     * The physical file path of the dataservice, if known
     */
    private String dsLocation;

    /**
     * Relative file path of the dataservice, if known
     */
    private String dsRelativeLocation;

    /**
     * The service status of the dataservices, can be either "active" or "inactive",
     * mainly used in WIP services.
     */
    private String serviceStatus;

    /**
     * States if batch requests are enabled, if so, the batch operations are also
     * created in the WSDL
     */
    private boolean batchRequestsEnabled;

    /**
     * States if boxcarring is enabled, if so, boxcarring related operations are
     * also created
     * This is also used to identify requestBox requests
     */
    private boolean boxcarringEnabled;

    /**
     * The current user who is sending requests
     */
    private static ThreadLocal<DataServiceUser> currentUser = new ThreadLocal<DataServiceUser>();

    /**
     * the JNDI name of the app server transaction manager
     */
    private String containerUserTxName;

    /**
     * the DSS XA transaction manager
     */
    private DSSXATransactionManager txManager;

    /**
     * flag to check if streaming is disabled
     */
    private boolean disableStreaming;

    /**
     * flag to check if boxcarring legacy mode is disabled
     */
    private boolean disableLegacyBoxcarringMode;

    /**
     * The tenant to which this service belongs to.
     */
    private int tenantId;

    /**
     * Authorization Provider which is used to retrieve user name and roles in configuration time and runtime(to be
     * used in role based filtering)
     */
    private AuthorizationProvider authorizationProvider;

    /**
     * transport settings
     */
    private List<String> transports;

    /**
     * Swagger resource location in the registry.
     */
    private String swaggerResourcePath;

	public DataService(String name, String description,
                       String defaultNamespace, String dsLocation, String serviceStatus,
                       boolean batchRequestsEnabled, boolean boxcarringEnabled,
                       String containerUserTxName) throws DataServiceFault {
        this.name = name;
        this.callableRequests = new HashMap<String, CallableRequest>();
        this.operations = new HashMap<String, Operation>();
        this.resourceMap = new LinkedHashMap<Resource.ResourceID, Resource>();
        this.configs = new HashMap<String, Config>();
        this.eventTriggers = new HashMap<String, EventTrigger>();
        this.queries = new HashMap<String, Query>();
        this.transports = new ArrayList<String>();
        this.description = description;
        this.defaultNamespace = defaultNamespace;
        this.dsLocation = dsLocation;
        this.setRelativeDsLocation(this.dsLocation);
        this.serviceStatus = serviceStatus;
        this.batchRequestsEnabled = batchRequestsEnabled;
        this.boxcarringEnabled = boxcarringEnabled;
        this.containerUserTxName = containerUserTxName;

        /* initialize transaction manager */
        initXA();

        /* set tenant id */
        this.tenantId = DBUtils.getCurrentTenantId();
    }

    private void initXA() throws DataServiceFault {
        TransactionManager txManager = DBUtils.getContainerTransactionManager(
                this.getContainerUserTransactionName());
        this.txManager = new DSSXATransactionManager(txManager);
    }

    private void initBoxcarring() throws DataServiceFault {
        /* add empty query, begin_boxcar, abort_boxcar */
        this.addQuery(new Query(this, DBConstants.EMPTY_QUERY_ID,
                                new ArrayList<QueryParam>(), null, null, null, null, null, this.getDefaultNamespace()) {
            public Object runPreQuery(InternalParamCollection params, int queryLevel) {
                return null;
            }

            @Override
            public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                                     InternalParamCollection params, int queryLevel) throws DataServiceFault {

            }
        });
        /* empty query for end_boxcar */
        Result endBoxcarResult = new Result("dummy", "dummy",
                                            DBConstants.WSO2_DS_NAMESPACE, null, ResultTypes.XML);
        endBoxcarResult.setXsAny(true);
        endBoxcarResult.setDefaultElementGroup(new OutputElementGroup(null, null, null, null));
        this.addQuery(new Query(this, DBConstants.EMPTY_END_BOXCAR_QUERY_ID,
                                new ArrayList<QueryParam>(), endBoxcarResult, null, null, null, null,
                                this.getDefaultNamespace()) {
            public Object runPreQuery(InternalParamCollection params, int queryLevel) {
                return null;
            }

            @Override
            public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                                     InternalParamCollection params, int queryLevel) throws DataServiceFault {

            }
        });
        /* operations */
        this.addOperation(OperationFactory.createBeginBoxcarOperation(this));
        this.addOperation(OperationFactory.createEndBoxcarOperation(this));
        this.addOperation(OperationFactory.createAbortBoxcarOperation(this));
    }

    /**
     * Helper method to initialise request box.
     *
     * @throws DataServiceFault
     */
    public void initRequestBox() throws DataServiceFault {
        initRequestBoxForOperation();
    }

    /**
     * Helper method to initialise request box operation, (if there are no operations already, then it will return
     * without doing anything)
     *
     * @throws DataServiceFault
     */
    private void initRequestBoxForOperation() throws DataServiceFault {
        if (this.getOperationNames().isEmpty() && this.getResourceIds().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("There are no Operations available in data service, So request box won't be generated for Operations");
            }
            return;
        }
        addEmptyQueryForRequestBox();
        /* operation */
        this.addOperation(OperationFactory.createRequestBoxOperation(this));
    }

    /**
     * Helper method to add empty query to be used for request box operation and resource.
     *
     * @throws DataServiceFault
     */
    private void addEmptyQueryForRequestBox() throws DataServiceFault {
        if (this.getQuery(DBConstants.EMPTY_END_BOXCAR_QUERY_ID) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Empty query already exist, returning without trying to add it again");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Empty query does not exist, so adding it for used in request box operation/resource");
        }
        /* empty query for requestBox */
        Result endRequestBoxResult = new Result("dummy", "dummy",
                                                DBConstants.WSO2_DS_NAMESPACE, null, ResultTypes.XML);
        endRequestBoxResult.setXsAny(true);
        endRequestBoxResult.setDefaultElementGroup(new OutputElementGroup(null, null, null, null));
        this.addQuery(new Query(this, DBConstants.EMPTY_END_BOXCAR_QUERY_ID,
                                new ArrayList<QueryParam>(), endRequestBoxResult, null, null, null, null,
                                this.getDefaultNamespace()) {
            public Object runPreQuery(InternalParamCollection params, int queryLevel) {
                return null;
            }

            @Override
            public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                                     InternalParamCollection params, int queryLevel) throws DataServiceFault {

            }
        });

    }

    /**
     * Initializes the data service object.
     */
    public void init() throws DataServiceFault {
         /* add operations related to boxcarring and request Box */
        if (this.isBoxcarringEnabled() && this.disableLegacyBoxcarringMode) {
            initRequestBox();
        } else if (this.isBoxcarringEnabled()) {
            initBoxcarring();
            initRequestBox();
        }
        /* init callable requests */
        for (CallableRequest callableRequest : this.getCallableRequests().values()) {
            callableRequest.getCallQuery().init();
        }
        /* init queries */
        for (Query query : this.getQueries().values()) {
            if (query.hasResult()) {
                query.getResult().getDefaultElementGroup().init();
            }
        }
    }

    public int getTenantId() {
		return tenantId;
	}

    public boolean isDisableStreaming() {
        return disableStreaming;
    }

    public void setDisableStreaming(boolean disableStreaming) {
        this.disableStreaming = disableStreaming;
    }

    public boolean isDisableLegacyBoxcarringMode() {
        return this.disableLegacyBoxcarringMode;
    }

    public void setDisableLegacyBoxcarringMode(boolean disableLegacyBoxcarringMode) {
        this.disableLegacyBoxcarringMode = disableLegacyBoxcarringMode;
    }

    public DSSXATransactionManager getDSSTxManager() {
        return txManager;
    }

    public String getContainerUserTransactionName() {
        return containerUserTxName;
    }

    /**
     * Cleanup operations done when undeploying the data service.
     */
    public void cleanup() throws DataServiceFault {
//        if (log.isDebugEnabled()) {
//            log.debug("Data Service '" + this.getName() + "' cleanup start..");
//        }
//        /* remove event subscriptions */
//        EventBroker eventBroker =
//                DataServicesDSComponent.getEventBroker();
//        if (eventBroker != null) {
//            this.clearDataServicesEventSubscriptions(eventBroker);
//        }
//        /* cleanup configs */
//        for (Config config : this.getConfigs().values()) {
//        	config.close();
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("Data Service '" + this.getName() + "' cleanup end.");
//        }
    }

//    private void clearDataServicesEventSubscriptions(
//            EventBroker eventBroker) throws DataServiceFault {
//        try {
//            String dsName;
//            for (Subscription subs : eventBroker.getAllSubscriptions(null)) {
//                dsName = subs.getProperties().get(DBConstants.DATA_SERVICE_NAME);
//                if (dsName != null && this.getName().equals(dsName)) {
//                    eventBroker.unsubscribe(subs.getId());
//                }
//            }
//        } catch (EventBrokerException e) {
//            throw new DataServiceFault(e);
//        }
//    }

    public String getServiceNamespace() {
        return serviceNamespace;
    }

    public void setServiceNamespace(String serviceNamespace) {
        this.serviceNamespace = serviceNamespace;
    }

    public Map<String, EventTrigger> getEventTriggers() {
        return eventTriggers;
    }

    public EventTrigger getEventTrigger(String triggerId) {
        return this.getEventTriggers().get(triggerId);
    }

    public void addEventTrigger(EventTrigger eventTrigger) {
        this.getEventTriggers().put(eventTrigger.getTriggerId(), eventTrigger);
    }

    public boolean isBatchRequestsEnabled() {
        return batchRequestsEnabled;
    }

    public boolean isBoxcarringEnabled() {
        return boxcarringEnabled;
    }

    public static DataServiceUser getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(DataServiceUser user) {
        currentUser.set(user);
    }

    public String getDsLocation() {
        return dsLocation;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public Map<String, CallableRequest> getCallableRequests() {
        return callableRequests;
    }

    public CallableRequest getCallableRequest(String requestName) {
        return this.getCallableRequests().get(requestName);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    private Map<String, Operation> getOperations() {
        return operations;
    }

    public Set<String> getOperationNames() {
        return this.getOperations().keySet();
    }

    private Map<Resource.ResourceID, Resource> getResourceMap() {
        return resourceMap;
    }

    public Set<Resource.ResourceID> getResourceIds() {
        return this.getResourceMap().keySet();
    }

    public Map<String, Config> getConfigs() {
        return configs;
    }

    public Config getConfig(String configId) {
        return this.getConfigs().get(configId);
    }

    public void addConfig(Config config) {
        this.getConfigs().put(config.getConfigId(), config);
    }

    public Operation getOperation(String opName) {
        return this.getOperations().get(opName);
    }

    public Resource getResource(Resource.ResourceID resourceId) {
        return this.getResourceMap().get(resourceId);
    }

    private void addCallableRequest(CallableRequest callableRequest) {
        this.getCallableRequests().put(callableRequest.getRequestName(), callableRequest);
    }

    public void addOperation(Operation operation) {
        this.getOperations().put(operation.getName(), operation);
        this.addCallableRequest(operation);
    }

    public void addResource(Resource resource) {
        this.getResourceMap().put(resource.getResourceId(), resource);
        this.addCallableRequest(resource);
    }

    public Map<String, Query> getQueries() {
        return queries;
    }

    public Query getQuery(String queryId) {
        return this.getQueries().get(queryId);
    }

    public void addQuery(Query query) {
        this.getQueries().put(query.getQueryId(), query);
    }

    public SecretResolver getSecretResolver() {
        return secretResolver;
    }

    public void setSecretResolver(SecretResolver secretResolver) {
        this.secretResolver = secretResolver;
    }

    public String getRelativeDsLocation() {
        return this.dsRelativeLocation;
    }

    private void setRelativeDsLocation(String location) {
        if (location != null && !"".equals(location)) {
            String[] dsPathContents = location.trim().split("dataservices");
            this.dsRelativeLocation = dsPathContents[dsPathContents.length-1];
        }
    }

    /**
     * Getter method for Authorization Provider.
     *
     * @return authorizationProvider instance.
     */
    public AuthorizationProvider getAuthorizationProvider() {
        return authorizationProvider;
    }

    /**
     * Setter method for Authorization Provider.
     *
     * @param authorizationProvider instance.
     */
    public void setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        this.authorizationProvider = authorizationProvider;
    }

    /**
     * Getter method for Transports
     * @return a list of transports
     */
    public List<String> getTransports() {
        return transports;
    }

    /**
     * Setter method for Transports
     * @param transports List of transports
     */
    public void setTransports(List<String> transports) {
        this.transports = transports;
    }

    public String getSwaggerResourcePath() {
        return swaggerResourcePath;
    }

    public void setSwaggerResourcePath(String swaggerResourcePath) {
        this.swaggerResourcePath = swaggerResourcePath;
    }

    /**
     * Instructs the data service to run the request with the given name
     * with the given parameters.
     *
     * @param xmlWriter      XMLStreamWriter used to write the result
     * @param requestName    The service request name
     * @param params         The parameters to be used for the service call
     * @throws DataServiceFault Thrown if a problem occurs in service dispatching
     */
    public void invoke(XMLStreamWriter xmlWriter,
                                   String requestName, Map<String, ParamValue> params)
            throws DataServiceFault {
        try {
            this.getCallableRequest(requestName).execute(xmlWriter,
            		this.extractParams(params));
        } catch (DataServiceFault e) {
            this.fillInDataServiceFault(e, requestName, params);
            throw e;
        } catch (Exception e) {
            DataServiceFault dsf = new DataServiceFault(e);
            this.fillInDataServiceFault(dsf, requestName, params);
            throw dsf;
        }
    }

    private void fillInDataServiceFault(DataServiceFault dsf, String requestName,
                                        Map<String, ParamValue> params) {
        dsf.setSourceDataService(this);
        dsf.setCurrentRequestName(requestName);
        dsf.setCurrentParams(params);
    }

    /**
     * Convert the parameters passed in to a collection of ExternalParam objects.
     * An ExternalParam is a value that is passed into "call queries".
     */
    private ExternalParamCollection extractParams(Map<String, ParamValue> params) {
        ExternalParamCollection epc = new ExternalParamCollection();
        for (Entry<String, ParamValue> entry : params.entrySet()) {
            /* 'toLowerCase' - workaround for different character case issues in column names.
                * This is because, some DBMSs like H2, the results they give, the column names
                * will not match the column names they actually return. For example,
                *    ....
                *     <query id="select_query_count">
                *        <sql>SELECT COUNT(*) as orderDetailsCount FROM OrderDetails</sql>
                *        <result element="Orders" rowName="OrderDetails">
                *          <element name="orderDetailsCount" column="orderDetailsCount" xsdType="integer" />
                *        </result>
                *     </query>
                *      ....
                *      The above query, the column that should be returned should be "orderDetailsCount",
                *      to be matched by the result's column entry, mentioning, that it's expecting a
                *      column value "orderDetailsCount". But H2 doesn't return this name.
                *      So to overcome this, all the parameter names (the result itself is a parameter
                *      for output elements(static elements, call queries)), are lower cased before passed in.
                */
            epc.addParam(new ExternalParam(entry.getKey().toLowerCase(), entry.getValue(),
                                           DBSFields.QUERY_PARAM));
        }
        return epc;
    }

    public String getResultWrapperForRequest(String requestName) {
        return this.getCallableRequest(requestName).getCallQuery().getResultWrapper();
    }

    /**
     * Returns the namespace for the given request name.
     */
    public String getNamespaceForRequest(String requestName) {
        CallQuery callQuery = this.getCallableRequest(requestName).getCallQuery();
        return callQuery.getNamespace();
    }

    public boolean hasResultForRequest(String requestName) {
        return this.getCallableRequest(requestName).getCallQuery().isHasResult();
    }

    public boolean isReturningRequestStatus(String requestName) {
        return this.getCallableRequest(requestName).isReturnRequestStatus();
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public boolean isServiceInactive() {
        return this.getServiceStatus() != null &&
                this.getServiceStatus().equals(ServiceStatusValues.INACTIVE);
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Name: " + this.getName() + "\n");
        buff.append("Location: " + this.getRelativeDsLocation() + "\n");
        buff.append("Description: " + (this.getDescription() != null ?
                this.getDescription() : "N/A") + "\n");
        buff.append("Default Namespace: " + this.getDefaultNamespace() + "\n");
        return buff.toString();
    }
    
    public boolean isInDTX() {
        return this.getDSSTxManager().isInDTX();
    }

    /**
     * Implement the AxisResources interface to return the API definition as an object.
     *
     * @return AxisResourceMap API as an AxisResourceMap
     */
    @Override
    public AxisResourceMap getAxisResourceMap() {
        AxisResourceMap axisResourceMap = new AxisResourceMap();
        for (Map.Entry<Resource.ResourceID, Resource> entry : resourceMap.entrySet()) {
            String resourcePath = entry.getKey().getPath();
            String method = entry.getKey().getMethod();

            AxisResource existingResource = axisResourceMap.getResources().get(resourcePath);
            if (existingResource != null) {
                // resource path same but different method
                if (!existingResource.getMethods().contains(method)) {
                    existingResource.setMethod(method);
                    createResourceParameter(existingResource, entry.getValue(), method, resourcePath);
                }
            } else {
                AxisResource axisResource = new AxisResource();
                createResourceParameter(axisResource, entry.getValue(), method, resourcePath);
                axisResource.setMethod(method);
                axisResourceMap.addNewResource(resourcePath, axisResource);
            }
        }
        return axisResourceMap;
    }

    // helper method to get the matching query param for a given with-param.
    private QueryParam checkWithParamInQueryParams(ArrayList<QueryParam> paramList, String withParamName) {
        for (QueryParam queryParam : paramList) {
            if (queryParam.getName().equals(withParamName)) {
                return queryParam;
            }
        }
        return null;
    }

    // given a DSS data type this method will return the corresponding swagger data type.
    private String dataTypeMapper(String dataServiceDataType) {
        switch (dataServiceDataType) {
            case DBConstants.DataTypes.BIT:
                return DBConstants.SwaggerDataTypes.BOOLEAN;
            case DBConstants.DataTypes.INTEGER:
            case DBConstants.DataTypes.BIGINT:
            case DBConstants.DataTypes.TINYINT:
            case DBConstants.DataTypes.SMALLINT:
            case DBConstants.DataTypes.DECIMAL:
                return DBConstants.SwaggerDataTypes.INTEGER;
            case DBConstants.DataTypes.DOUBLE:
            case DBConstants.DataTypes.FLOAT:
            case DBConstants.DataTypes.LONG:
                return DBConstants.SwaggerDataTypes.NUMBER;
            default:
                return DBConstants.SwaggerDataTypes.STRING;
        }
    }

    // this method will generate query and url parameters for a given resource in the API.
    private void createResourceParameter(AxisResource axisResource, Resource resource, String method,
                                         String resourcePath) {
        ArrayList<QueryParam> queryParamsList =
                new ArrayList<>(resource.getCallQuery().getQuery().getQueryParams());
        Map<String, CallQuery.WithParam> withParamMap = resource.getCallQuery().getWithParams();
        ArrayList<AxisResourceParameter> resourceParameterList = new ArrayList<>();

        for (Map.Entry<String, CallQuery.WithParam> withParam : withParamMap.entrySet()) {
            String queryParam = withParam.getValue().getOriginalName();

            if (queryParam != null && !queryParam.isEmpty()) {
                String urlParameter = "{" + queryParam + "}";
                AxisResourceParameter resourceParameter = new AxisResourceParameter();
                resourceParameter.setParameterName(queryParam);
                // setting the type of the parameter - query param or url param.
                if (resourcePath.contains(urlParameter)) {
                    resourceParameter.setParameterType(AxisResourceParameter.ParameterType.URL_PARAMETER);
                } else {
                    resourceParameter.setParameterType(AxisResourceParameter.ParameterType.QUERY_PARAMETER);
                }

                // setting the data type of the parameter.
                QueryParam param = checkWithParamInQueryParams(queryParamsList, withParam.getValue().getName());
                if (param != null) {
                    resourceParameter.setParameterDataType(dataTypeMapper(param.getSqlType()));
                } else {
                    // If no entry found, add the default data type.
                    resourceParameter.setParameterDataType(DBConstants.SwaggerDataTypes.STRING);
                }
                resourceParameterList.add(resourceParameter);
            }
        }
        axisResource.addResourceParameter(method, resourceParameterList);
    }
}
