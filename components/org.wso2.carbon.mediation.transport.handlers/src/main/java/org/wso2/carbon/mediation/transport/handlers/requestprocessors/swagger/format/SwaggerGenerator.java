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
package org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger.format;

import org.apache.axiom.ext.io.StreamCopyException;
import org.apache.axiom.util.blob.BlobOutputStream;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.rest.API;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.mediation.transport.handlers.requestprocessors.swagger.SwaggerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * This is the base class used by swagger formatter classes(JSON and YAML) and contains generic functions.
 */
public class SwaggerGenerator {
    private static final Log log = LogFactory.getLog(SwaggerGenerator.class);

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
        try {
            ((BlobOutputStream) response.getOutputStream()).getBlob().readFrom(new ByteArrayInputStream
                    (responseString.getBytes(SwaggerConstants.DEFAULT_ENCODING)), responseString.length());
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
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(request.getRequestURI());
        String apiName = getApiNameFromRequestUri(request.getRequestURI());

        return SynapseConfigUtils.getSynapseConfiguration((tenantDomain != null) ? tenantDomain
                : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME).getAPI(apiName);
    }

    /**
     * Extract API Name from the given URI.
     *
     * @param requestUri URI String of the request
     * @return API Name extracted from the URI provided
     */
    protected String getApiNameFromRequestUri(String requestUri) {
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        String pathSeparator = "/";
        String apiName = null;

        if (requestUri.contains(pathSeparator + MultitenantConstants.TENANT_AWARE_URL_PREFIX + pathSeparator)) {
            String[] paths = requestUri.split(pathSeparator);
            boolean foundTenantDelimiter = false;
            boolean foundNewTenant = false;

            for (String pathString : paths) {
                if (!foundTenantDelimiter && MultitenantConstants.TENANT_AWARE_URL_PREFIX.equals(pathString)) {
                    foundTenantDelimiter = true;
                } else if (foundTenantDelimiter && MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    tenantDomain = pathString;
                    foundNewTenant = true;
                } else if (foundNewTenant) {
                    apiName = pathString;
                    break;
                }
            }
        } else {
            apiName = requestUri.substring(1);
        }
        return apiName;
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
}
