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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.api.API;
import org.wso2.carbon.mediation.commons.rest.api.swagger.SwaggerConstants;
import org.wso2.micro.core.transports.CarbonHttpRequest;
import org.wso2.micro.core.transports.CarbonHttpResponse;
import org.wso2.micro.core.transports.HttpGetRequestProcessor;
import org.wso2.micro.integrator.transport.handlers.utils.SwaggerException;
import org.wso2.micro.integrator.transport.handlers.utils.SwaggerProcessorConstants;
import org.wso2.micro.integrator.transport.handlers.utils.SwaggerUtils;

/**
 * Provides Swagger definition for the API in JSON format.
 */
public class SwaggerJsonProcessor extends SwaggerGenerator implements HttpGetRequestProcessor {

    /**
     * Process incoming GET request and update the response with the swagger definition for the requested API
     * @param request              The CarbonHttpRequest contains request information.
     * @param response             The CarbonHttpResponse which will be updated with response information.
     * @param configurationContext The system ConfigurationContext.
     * @throws AxisFault    Error occurred while fetching the host name.
     * @throws SwaggerException Error occurred while fetching the resources from the registry.
     */
    @Override
    public void process(CarbonHttpRequest request, CarbonHttpResponse response, ConfigurationContext configurationContext)
            throws AxisFault, SwaggerException {
        API api = getAPIFromSynapseConfig(request);
        String responseString = null;
        if (api != null) {
            responseString = SwaggerUtils.getAPISwagger(api,true);
        } else if (request.getContextPath().contains("/" + SwaggerProcessorConstants.SERVICES_PREFIX)) {
            responseString = SwaggerUtils.getDataServiceSwagger(request.getRequestURI(), configurationContext, true);
        } else {
            handleException(request.getRequestURI());
        }

        if (StringUtils.isNotEmpty(responseString)) {
            updateResponse(response, responseString, SwaggerConstants.CONTENT_TYPE_JSON);
        } else {
            handleException(request.getRequestURI());
        }
    }
}
