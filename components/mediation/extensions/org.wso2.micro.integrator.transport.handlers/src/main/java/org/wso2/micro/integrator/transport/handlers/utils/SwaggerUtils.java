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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisResource;
import org.apache.axis2.description.AxisResourceMap;
import org.apache.axis2.description.AxisResourceParameter;
import org.apache.axis2.description.AxisResources;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.registry.Registry;
import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.transport.handlers.requestprocessors.swagger.format.MIServerConfig;

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
    public static String takeDataServiceSwagger(String requestURI, ConfigurationContext configurationContext,
                                                boolean isJSON) throws AxisFault {
        String dataServiceName = requestURI.substring(requestURI.lastIndexOf("/") + 1);
        AxisService dataService = configurationContext.getAxisConfiguration().getService(dataServiceName);
        if (dataService != null) {
            MIServerConfig serverConfig = new MIServerConfig();
            Object dataServiceObject =
                    dataService.getParameter(SwaggerProcessorConstants.DATA_SERVICE_OBJECT).getValue();
            if (dataService.getParameter(SwaggerProcessorConstants.SWAGGER_RESOURCE_PATH) != null) {
                String swaggerLocation =
                        (String) dataService.getParameter(SwaggerProcessorConstants.SWAGGER_RESOURCE_PATH).getValue();
                if (swaggerLocation != null && !swaggerLocation.isEmpty()) {
                    return SwaggerUtils.fetchSwaggerFromRegistry(swaggerLocation);
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
            paths.put(entry.getKey(), path);
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
     * @return Swagger definition as string or null if error occurred.
     */
    public static String fetchSwaggerFromRegistry(String resourcePath) {
        String defString = null;
        SynapseConfiguration synapseConfig =
                SynapseConfigUtils.getSynapseConfiguration(Constants.SUPER_TENANT_DOMAIN_NAME);
        Registry registry = synapseConfig.getRegistry();
        OMNode regContent = registry.lookup(resourcePath);

        if (regContent instanceof OMText) {
            defString = ((OMText) regContent).getText();
        }

        return defString;
    }
}
