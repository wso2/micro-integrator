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
package org.wso2.micro.integrator.transport.handlers.requestprocessors.swagger.format;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.axiom.ext.io.StreamCopyException;
import org.apache.axiom.util.blob.BlobOutputStream;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.api.API;
import org.apache.synapse.config.SynapseConfigUtils;
import org.wso2.carbon.mediation.commons.rest.api.swagger.SwaggerConstants;
import org.wso2.micro.core.Constants;
import org.wso2.micro.core.transports.CarbonHttpRequest;
import org.wso2.micro.core.transports.CarbonHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * This is the base class used by swagger formatter classes(JSON and YAML) and contains generic functions.
 */
public class SwaggerGenerator {
    private static final Log log = LogFactory.getLog(SwaggerGenerator.class);
    /**
     * Registry path prefixes
     */

    /**
     * Update the response with provided response string.
     *
     * @param response       CarbonHttpResponse which will be updated
     * @param responseString String response to be updated in response
     * @param contentType    Content type of the response to be updated in response headaers
     * @throws Exception Any exception occured during the update
     */
    protected void updateResponse(CarbonHttpResponse response, String responseString, String contentType) throws
            AxisFault {
        String updatesResponseString = getOpenAPIJsonString(responseString, contentType);
        try {
            if (updatesResponseString == null) {
                // Parsing to OpenAPI model failed. Return the original response.
                // This can happen when user save a custom swagger in registry.
                updatesResponseString = responseString;
            }
            byte[] responseStringBytes = updatesResponseString.getBytes(SwaggerConstants.DEFAULT_ENCODING);
            ((BlobOutputStream) response.getOutputStream()).getBlob()
                    .readFrom(new ByteArrayInputStream(responseStringBytes), responseStringBytes.length);
        } catch (StreamCopyException streamCopyException) {
            handleException("Error in generating Swagger definition : failed to copy data to response ",
                    streamCopyException);
        } catch (UnsupportedEncodingException encodingException) {
            handleException("Error in generating Swagger definition : exception in encoding ", encodingException);
        }
        response.setStatus(SwaggerConstants.HTTP_OK);
        response.getHeaders().put(HTTP.CONTENT_TYPE, contentType);
    }

    /**
     * Returns API instance related to the URI in provided request.
     *
     * @param request CarbonHttpRequest which contains the request URI info
     * @return API instance with respect to the request
     */
    protected API getAPIFromSynapseConfig(CarbonHttpRequest request) {
        String apiName = getApiNameFromRequestUri(request.getRequestURI());
        return SynapseConfigUtils.getSynapseConfiguration(Constants.SUPER_TENANT_DOMAIN_NAME).getAPI(apiName);
    }

    /**
     * Extract API Name from the given URI.
     *
     * @param requestUri URI String of the request
     * @return API Name extracted from the URI provided
     */
    protected String getApiNameFromRequestUri(String requestUri) {
        return requestUri.substring(1);
    }

    /**
     * Logs exceptions occured in formatters and throws.
     *
     * @param errorMsg  String message which contains error information
     * @param exception Actual Thowable instance with exception details
     * @throws AxisFault throws with error information
     */
    private void handleException(String errorMsg, Exception exception) throws AxisFault {
        log.error(errorMsg);
        throw new AxisFault(errorMsg, exception);
    }

    /**
     * Logs the error message and throws New AxisFault.
     *
     * @param errorMsg String message which contains error information
     * @throws AxisFault throws with error information
     */
    protected void handleException(String errorMsg) throws AxisFault {
        log.error(errorMsg);
        throw new AxisFault(errorMsg);
    }

    /**
     * Ignore example:null from the response
     *
     * @param responseString String response to be updated in response
     * @param contentType    Content type of the response to be updated in response headers
     */
    public static String getOpenAPIJsonString(String responseString, String contentType) {
        if (contentType.contains(SwaggerConstants.CONTENT_TYPE_YAML)) {
            try {
                JsonNode yamlMapper = new ObjectMapper(new YAMLFactory()).readTree(responseString);
                responseString = Json.mapper().writeValueAsString(yamlMapper);
            } catch (JsonProcessingException e) {
                log.error("Error while converting a yaml definition to a json " + e.getMessage());
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // this is to ignore "example" and "exampleSetFlag" objects when generating the OpenAPI
        mapper.addMixIn(Parameter.class, MediaTypeMixin.class);
        mapper.addMixIn(MediaType.class, MediaTypeMixin.class);
        mapper.addMixIn(Schema.class, MediaTypeMixin.class);
        try {
            OpenAPI openAPI = mapper.readValue(responseString, OpenAPI.class);
            String updatedResponseString = mapper.writeValueAsString(openAPI);
            if (contentType.contains(SwaggerConstants.CONTENT_TYPE_JSON)) {
                return updatedResponseString;
            }
            JsonNode jsonNodeTree = new ObjectMapper().readTree(updatedResponseString);
            return Yaml.mapper().writeValueAsString(jsonNodeTree);
        } catch (JsonProcessingException e) {
            log.error("Error while generating Swagger JSON from model", e);
            return null;
        }
    }
}
