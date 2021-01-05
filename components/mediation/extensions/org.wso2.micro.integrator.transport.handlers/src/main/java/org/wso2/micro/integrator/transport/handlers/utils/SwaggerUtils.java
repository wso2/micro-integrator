/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.transport.handlers.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import io.swagger.models.Info;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import net.minidev.json.JSONObject;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisResource;
import org.apache.axis2.description.AxisResourceMap;
import org.apache.axis2.description.AxisResourceParameter;
import org.apache.axis2.description.AxisResources;
import org.apache.axis2.description.AxisService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.registry.Registry;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.version.DefaultStrategy;
import org.wso2.carbon.integrator.core.json.utils.GSONUtils;
import org.wso2.carbon.mediation.commons.rest.api.swagger.GenericApiObjectDefinition;
import org.wso2.carbon.mediation.commons.rest.api.swagger.SwaggerConstants;
import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.transport.handlers.requestprocessors.swagger.format.MIServerConfig;
import static org.wso2.micro.application.deployer.AppDeployerUtils.createRegistryPath;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class with methods to generate swagger definition and fetch them from the registry.
 */
public final class SwaggerUtils {

    private static Log logger = LogFactory.getLog(SwaggerUtils.class.getName());

    /**
     * Fetch the swagger from registry if available or create one from scratch.
     *
     * @param requestURI           request URI.
     * @param configurationContext Configuration context with details.
     * @param isJSON               result format JSON or YAML.
     * @return Swagger definition as string
     * @throws AxisFault Error occurred while fetching the host details.
     */
    public static String getDataServiceSwagger(String requestURI, ConfigurationContext configurationContext,
                                               boolean isJSON) throws AxisFault, SwaggerException {
        String dataServiceName = requestURI.substring(requestURI.lastIndexOf("/") + 1);
        AxisService dataService = configurationContext.getAxisConfiguration().getService(dataServiceName);
        if (dataService != null) {
            MIServerConfig serverConfig = new MIServerConfig();
            Object dataServiceObject =
                    dataService.getParameter(SwaggerProcessorConstants.DATA_SERVICE_OBJECT).getValue();
            if (dataService.getParameter(SwaggerProcessorConstants.SWAGGER_RESOURCE_PATH) != null) {
                String swaggerLocation =
                        (String) dataService.getParameter(SwaggerProcessorConstants.SWAGGER_RESOURCE_PATH).getValue();
                if (StringUtils.isNotEmpty(swaggerLocation)) {
                    String swaggerFromReg = SwaggerUtils.fetchSwaggerFromRegistry(swaggerLocation);
                    if (StringUtils.isNotEmpty(swaggerFromReg)) {
                        return swaggerFromReg;
                    } else throw new SwaggerException("Could not fetch the swagger definition from registry. Registry" +
                            " path : " + swaggerLocation);
                }
                return null;
            } else {
                List<String> transports = dataService.getExposedTransports();
                if (dataServiceObject instanceof AxisResources) {
                    AxisResourceMap axisResourceMap = ((AxisResources) dataServiceObject).getAxisResourceMap();
                    return SwaggerUtils.createSwaggerFromDefinition(axisResourceMap, dataServiceName, transports,
                            serverConfig, isJSON);
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Create a swagger definition from data-service resource details.
     *
     * @param axisResourceMap AxisResourceMap containing resource details.
     * @param dataServiceName Name of the data service.
     * @param transports      Transports supported from the data-service.
     * @param serverConfig    Server config details.
     * @param isJSON          result format JSON or YAML.
     * @return Swagger definition as string.
     * @throws AxisFault Error occurred while fetching the host address.
     */
    private static String createSwaggerFromDefinition(AxisResourceMap axisResourceMap, String dataServiceName,
                                                      List<String> transports, MIServerConfig serverConfig,
                                                      boolean isJSON)
            throws AxisFault {

        Swagger swaggerDoc = new Swagger();
        addSwaggerInfoSection(dataServiceName, transports, serverConfig, swaggerDoc);

        Map<String, Path> paths = new HashMap<>();

        for (Map.Entry<String, AxisResource> entry : axisResourceMap.getResources().entrySet()) {
            Path path = new Path();
            for (String method : entry.getValue().getMethods()) {
                Operation operation = new Operation();
                List<AxisResourceParameter> parameterList = entry.getValue().getResourceParameterList(method);
                generatePathAndQueryParameters(method, operation, parameterList);
                // Adding a sample request payload for methods except GET.
                generateSampleRequestPayload(method, operation, parameterList);
                Response response = new Response();
                response.description("this is the default response");
                operation.addResponse("default", response);
                switch (method) {
                    case "GET":
                        path.get(operation);
                        break;
                    case "POST":
                        path.post(operation);
                        break;
                    case "DELETE":
                        path.delete(operation);
                        break;
                    case "PUT":
                        path.put(operation);
                        break;
                    default:
                        throw new AxisFault("Invalid method \"" + method + "\" detected in the data-service");
                }
            }
            String contextPath = entry.getKey().startsWith("/") ? entry.getKey() : "/" + entry.getKey();
            paths.put(contextPath, path);
        }
        swaggerDoc.setPaths(paths);
        if (isJSON) return Json.pretty(swaggerDoc);
        try {
            return Yaml.pretty().writeValueAsString(swaggerDoc);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while creating the YAML configuration", e);
            return null;
        }
    }

    /**
     * This method will generate a sample request body for the request.
     *
     * @param method        Rest resource method.
     * @param operation     Swagger operation object.
     * @param parameterList list of parameters.
     */
    private static void generateSampleRequestPayload(String method, Operation operation,
                                                     List<AxisResourceParameter> parameterList) {
        // GET method does not have a body
        if (!"GET".equals(method)) {
            BodyParameter bodyParameter = new BodyParameter();
            bodyParameter.description("Sample Payload");
            bodyParameter.name("payload");
            bodyParameter.setRequired(false);

            ModelImpl modelschema = new ModelImpl();
            modelschema.setType("object");
            Map<String, Property> propertyMap = new HashMap<>(1);
            ObjectProperty objectProperty = new ObjectProperty();
            objectProperty.name("payload");

            Map<String, Property> payloadProperties = new HashMap<>();
            for (AxisResourceParameter resourceParameter : parameterList) {
                switch (resourceParameter.getParameterDataType()) {
                    case SwaggerProcessorConstants.INTEGER:
                        payloadProperties.put(resourceParameter.getParameterName(), new IntegerProperty());
                        break;
                    case SwaggerProcessorConstants.NUMBER:
                        payloadProperties.put(resourceParameter.getParameterName(), new DoubleProperty());
                        break;
                    case SwaggerProcessorConstants.BOOLEAN:
                        payloadProperties.put(resourceParameter.getParameterName(), new BooleanProperty());
                        break;
                    default:
                        payloadProperties.put(resourceParameter.getParameterName(), new StringProperty());
                        break;
                }
            }

            objectProperty.setProperties(payloadProperties);
            propertyMap.put("payload", objectProperty);
            modelschema.setProperties(propertyMap);
            bodyParameter.setSchema(modelschema);
            operation.addParameter(bodyParameter);
        }
    }

    /**
     * This method will generate path and query parameters in the swagger document.
     *
     * @param method        method of API resource.
     * @param operation     swagger operation object.
     * @param parameterList list of parameters.
     */
    private static void generatePathAndQueryParameters(String method, Operation operation,
                                                       List<AxisResourceParameter> parameterList) {

        if (!parameterList.isEmpty()) {
            for (AxisResourceParameter resourceParameter : parameterList) {
                AxisResourceParameter.ParameterType resourceParameterType =
                        resourceParameter.getParameterType();
                if (resourceParameterType.equals(AxisResourceParameter.ParameterType.URL_PARAMETER)) {
                    PathParameter pathParameter = new PathParameter();
                    pathParameter.setName(resourceParameter.getParameterName());
                    pathParameter.setType(resourceParameter.getParameterDataType());
                    pathParameter.required(true);
                    operation.addParameter(pathParameter);
                } else if (resourceParameterType
                        .equals(AxisResourceParameter.ParameterType.QUERY_PARAMETER) && "GET".equals(method)) {
                    //  Currently handling query parameter only for GET requests.
                    QueryParameter queryParameter = new QueryParameter();
                    queryParameter.setName(resourceParameter.getParameterName());
                    queryParameter.setType(resourceParameter.getParameterDataType());
                    queryParameter.required(true);
                    operation.addParameter(queryParameter);
                }
            }
        }
    }

    /**
     * This method will create the info section of the swagger document.
     *
     * @param dataServiceName name of the data-service.
     * @param transports      enabled transports.
     * @param serverConfig    Server config object.
     * @param swaggerDoc      Swagger document object.
     * @throws AxisFault Exception occured while getting the host address from transports.
     */
    private static void addSwaggerInfoSection(String dataServiceName, List<String> transports,
                                              MIServerConfig serverConfig, Swagger swaggerDoc) throws AxisFault {

        swaggerDoc.basePath("/" + SwaggerProcessorConstants.SERVICES_PREFIX + "/" + dataServiceName);

        if (transports.contains("https")) {
            swaggerDoc.addScheme(Scheme.HTTPS);
            swaggerDoc.addScheme(Scheme.HTTP);
            swaggerDoc.setHost(serverConfig.getHost("https"));
        } else {
            swaggerDoc.addScheme(Scheme.HTTP);
            swaggerDoc.setHost(serverConfig.getHost("http"));
        }

        Info info = new Info();
        info.title(dataServiceName);
        info.setVersion("1.0");
        info.description("API Definition of dataservice : " + dataServiceName);
        swaggerDoc.setInfo(info);

        swaggerDoc.addConsumes("application/json");
        swaggerDoc.addConsumes("application/xml");

        swaggerDoc.addProduces("application/json");
        swaggerDoc.addProduces("application/xml");
    }

    /**
     * Util method to fetch a swagger resource from the registry.
     *
     * @param resourcePath registry path to the resource.
     * @return null if no resource found or the swagger as string.
     */
    public static String fetchSwaggerFromRegistry(String resourcePath) {

        String defString = null;
        SynapseConfiguration synapseConfig =
                SynapseConfigUtils.getSynapseConfiguration(Constants.SUPER_TENANT_DOMAIN_NAME);
        Registry registry = synapseConfig.getRegistry();
        OMNode regContent = registry.lookup(createRegistryPath(resourcePath));

        if (regContent instanceof OMText) {
            defString = ((OMText) regContent).getText();
            byte[] decodedBytes = Base64.getDecoder().decode(defString);
            defString = new String(decodedBytes);
        }
        return defString;
    }

    /**
     * Get the swagger definition for a given API.
     * @param api   API object.
     * @param isJSON    response needed in JSON / JAML.
     * @return  Swagger as String.
     * @throws AxisFault Error occurred while fetching the host name for API.
     */
    public static String getAPISwagger(API api, boolean isJSON) throws AxisFault {

        MIServerConfig serverConfig = new MIServerConfig();
        String responseString;

        responseString = retrieveAPISwaggerFromRegistry(api);
        org.yaml.snakeyaml.Yaml yamlDefinition = new org.yaml.snakeyaml.Yaml();
        if (StringUtils.isNotEmpty(responseString)) {
            if (!isJSON) {
                JsonParser jsonParser = new JsonParser();
                responseString =
                        yamlDefinition.dumpAsMap(GSONUtils.gsonJsonObjectToMap(jsonParser.parse(responseString)));
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Generating swagger definition for: " + api.getName());
            }
            GenericApiObjectDefinition objectDefinition = new GenericApiObjectDefinition(api, serverConfig);
            Map<String, Object> definitionMap = objectDefinition.getDefinitionMap();
            if (isJSON) {
                JSONObject jsonDefinition = new JSONObject(definitionMap);
                responseString = jsonDefinition.toString();
            } else {
                responseString = yamlDefinition.dumpAsMap(definitionMap);
            }
        }
        return responseString;
    }

    /**
     * Function to extract swagger definition from the registry.
     *
     * @param api API object.
     * @return null if registry content unavailable or empty, otherwise relevant content.
     */
    private static String retrieveAPISwaggerFromRegistry(API api) {

        String resourcePath = api.getSwaggerResourcePath();

        if (resourcePath == null) {
            //Create resource path in registry
            StringBuilder resourcePathBuilder = new StringBuilder();
            resourcePathBuilder.append(SwaggerProcessorConstants.CONFIG_REG_PREFIX)
                    .append(SwaggerConstants.DEFAULT_SWAGGER_REGISTRY_PATH).append(api.getAPIName());
            if (!(api.getVersionStrategy() instanceof DefaultStrategy)) {
                resourcePathBuilder.append(":v").append(api.getVersion());
            }
            resourcePathBuilder.append("/swagger.json");
            resourcePath = resourcePathBuilder.toString();

        }

        return SwaggerUtils.fetchSwaggerFromRegistry(resourcePath);
    }
}
