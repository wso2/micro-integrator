/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URLMappingBasedDispatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Generalized object structure for Swagger definitions of APIs. This structure contains set of Maps which compatible
 * with both JSON and YAML formats.
 */
public class GenericApiObjectDefinition {
    private static final Log log = LogFactory.getLog(GenericApiObjectDefinition.class);
    private API api;

    public GenericApiObjectDefinition(API api) {
        this.api = api;
    }

    /**
     * Provides a map which represents the structure of swagger definition.
     *
     * @return Map containing information for swagger definition
     */
    public Map<String, Object> getDefinitionMap() {
        Map<String, Object> apiMap = new LinkedHashMap<>();
        apiMap.put(SwaggerConstants.SWAGGER, SwaggerConstants.SWAGGER_VERSION);
        apiMap.put(SwaggerConstants.INFO, getInfoMap());

        if (api.getHost() != null) {
            apiMap.put(SwaggerConstants.HOST, api.getHost());
        }
        apiMap.put(SwaggerConstants.BASE_PATH, api.getContext());
        apiMap.put(SwaggerConstants.SCHEMES, getSchemes());

        if (getPathMap() != null && !getPathMap().isEmpty()) {
            apiMap.put(SwaggerConstants.PATHS, getPathMap());
        }
        return apiMap;
    }

    /**
     * Provides structure for the "responses" element in swagger definition.
     *
     * @return Map containing information for responses element
     */
    private Map<String, Object> getResponsesMap() {
        Map<String, Object> responsesMap = new LinkedHashMap<>();
        Map<String, Object> responseDetailsMap = new LinkedHashMap<>();
        /* Use a default response since these information is not available in synapse configuration for APIs */
        responseDetailsMap.put(SwaggerConstants.DESCRIPTION, SwaggerConstants.DEFAULT_RESPONSE);
        responsesMap.put(SwaggerConstants.DEFAULT_VALUE, responseDetailsMap);
        if(log.isDebugEnabled()){
            log.debug("Response map created with size " + responsesMap.size());
        }
        return responsesMap;
    }

    /**
     * Provides structure for the "info" element in swagger definition.
     *
     * @return Map containing information for info element
     */
    private Map<String, Object> getInfoMap() {
        Map<String, Object> infoMap = new LinkedHashMap<>();
        infoMap.put(SwaggerConstants.DESCRIPTION, (SwaggerConstants.API_DESC_PREFIX + api.getAPIName()));
        infoMap.put(SwaggerConstants.TITLE, api.getName());
        infoMap.put(SwaggerConstants.VERSION, (api.getVersion() != null && !api.getVersion().equals(""))
                ? api.getVersion() : SwaggerConstants.DEFAULT_API_VERSION);
        if(log.isDebugEnabled()){
            log.debug("Info map created with size " + infoMap.size());
        }
        return infoMap;
    }

    /**
     * Provides structure for the "paths" element in swagger definition.
     *
     * @return Map containing information for paths element
     */
    private Map<String, Object> getPathMap() {
        Map<String, Object> pathsMap = new LinkedHashMap<>();
        for (Resource resource : api.getResources()) {
            Map<String, Object> methodMap = new LinkedHashMap<>();
            DispatcherHelper resourceDispatcherHelper = resource.getDispatcherHelper();

            for (String method : resource.getMethods()) {
                if (method != null) {
                    Map<String, Object> methodInfoMap = new LinkedHashMap<>();
                    methodInfoMap.put(SwaggerConstants.RESPONSES, getResponsesMap());
                    if (resourceDispatcherHelper != null) {
                        Object[] parameters = getResourceParameters(resource);
                        if (parameters.length > 0) {
                            methodInfoMap.put(SwaggerConstants.PARAMETERS, parameters);
                        }

                    }
                    methodMap.put(method.toLowerCase(), methodInfoMap);
                }
            }
            pathsMap.put(getPathFromUrl(resourceDispatcherHelper == null ? SwaggerConstants.PATH_SEPARATOR
                    : resourceDispatcherHelper.getString()), methodMap);
        }
        if(log.isDebugEnabled()){
            log.debug("Paths map created with size " + pathsMap.size());
        }
        return pathsMap;
    }

    /**
     * Provides list of schemas support by the API.
     *
     * @return Array of String containing schemas list
     */
    private String[] getSchemes() {
        String[] protocols;
        switch (api.getProtocol()) {
            case SwaggerConstants.PROTOCOL_HTTP_ONLY:
                protocols = new String[]{SwaggerConstants.PROTOCOL_HTTP};
                break;
            case SwaggerConstants.PROTOCOL_HTTPS_ONLY:
                protocols = new String[]{SwaggerConstants.PROTOCOL_HTTPS};
                break;
            default:
                protocols = new String[]{SwaggerConstants.PROTOCOL_HTTP, SwaggerConstants.PROTOCOL_HTTPS};
                break;
        }
        return protocols;
    }

    /**
     * Generate resource parameters for the given resource.
     *
     * @param resource instance of Resource in the API
     * @return Array of parameter objects supported by the API
     */
    private Object[] getResourceParameters(Resource resource) {
        ArrayList<Map<String, Object>> parameterList = new ArrayList<>();
        String uri = resource.getDispatcherHelper().getString();

        if (resource.getDispatcherHelper() instanceof URLMappingBasedDispatcher) {
            generateParameterList(parameterList, uri, false);
        } else {
            generateParameterList(parameterList, uri, true);
        }
        if(log.isDebugEnabled()){
            log.debug("Parameters processed for the URI + " + uri + " size " + parameterList.size());
        }
        return parameterList.toArray();
    }

    /**
     * Generate URI and Path parameters for the given URI.
     *
     * @param parameterList     List of maps to be populated with parameters
     * @param uriString         URI string to be used to extract parameters
     * @param generateBothTypes Indicates whether to consider both query and uri parameters. True if both to be
     *                          considered.
     */
    private void generateParameterList(ArrayList<Map<String, Object>> parameterList, String uriString, boolean
            generateBothTypes) {
        if (uriString == null) {
            return;
        }
        if (generateBothTypes) {
            String[] params = getQueryStringFromUrl(uriString).split("&");
            for (String parameter : params) {
                if (parameter != null) {
                    int pos = parameter.indexOf('=');
                    if (pos > 0) {
                        parameterList.add(getParametersMap(parameter.substring(0, pos), SwaggerConstants.PARAMETER_IN_QUERY));
                    }
                }
            }
        }
        Matcher matcher = SwaggerConstants.PATH_PARAMETER_PATTERN.matcher(getPathFromUrl(uriString));
        while (matcher.find()) {
            parameterList.add(getParametersMap(matcher.group(1), SwaggerConstants.PARAMETER_IN_PATH));
        }
    }

    /**
     * Create map of parameters from give name and type. Default values are used for other fields since those are not
     * provided by the synapse configuration of the API.
     *
     * @param parameterName Name of the parameter
     * @param parameterType Type of the parameter
     * @return Map containing parameter properties
     */
    private Map<String, Object> getParametersMap(String parameterName, String parameterType) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put(SwaggerConstants.PARAMETER_DESCRIPTION, parameterName);
        parameterMap.put(SwaggerConstants.PARAMETER_IN, parameterType);
        parameterMap.put(SwaggerConstants.PARAMETER_NAME, parameterName);
        parameterMap.put(SwaggerConstants.PARAMETER_REQUIRED, true);
        /* Type will be "string" for all parameters since synapse configuration does
        not contain suc information for APIs. */
        parameterMap.put(SwaggerConstants.PARAMETER_TYPE, SwaggerConstants.PARAMETER_TYPE_STRING);
        return parameterMap;
    }

    /**
     * Get the path portion from the URI.
     *
     * @param uri String URI to be analysed
     * @return String containing the path portion of the URI
     */
    private String getPathFromUrl(String uri) {
        int pos = uri.indexOf("?");
        if (pos > 0) {
            return uri.substring(0, pos);
        }
        return uri;
    }

    /**
     * Get query parameter portion from the URI.
     *
     * @param uri String URI to be analysed
     * @return String containing the URI parameter portion of the URI
     */
    private String getQueryStringFromUrl(String uri) {
        int pos = uri.indexOf("?");
        if (pos > 0) {
            return uri.substring(pos + 1);
        }
        return "";
    }
}
