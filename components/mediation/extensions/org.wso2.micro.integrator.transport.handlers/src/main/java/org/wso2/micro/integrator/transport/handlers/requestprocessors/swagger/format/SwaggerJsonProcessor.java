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

import net.minidev.json.JSONObject;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.API;
import org.wso2.carbon.mediation.commons.rest.api.swagger.GenericApiObjectDefinition;
import org.wso2.carbon.mediation.commons.rest.api.swagger.SwaggerConstants;
import org.wso2.micro.core.transports.CarbonHttpRequest;
import org.wso2.micro.core.transports.CarbonHttpResponse;
import org.wso2.micro.core.transports.HttpGetRequestProcessor;

/**
 * Provides Swagger definition for the API in JSON format.
 */
public class SwaggerJsonProcessor extends SwaggerGenerator implements HttpGetRequestProcessor {
    Log log = LogFactory.getLog(SwaggerJsonProcessor.class);
    /**
     * Process incoming GET request and update the response with the swagger definition for the requested API
     *
     * @param request              CarbonHttpRequest contains request information
     * @param response             CarbonHttpResponse which will be updated with response information
     * @param configurationContext axis2 configuration context
     * @throws Exception if any exception occurred during definition generation
     */
    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws AxisFault {
        API api = getAPIFromSynapseConfig(request);

        if (api == null) {
            handleException(request.getRequestURI());
        } else {
            String responseString = retrieveFromRegistry(api, request);
            if (responseString == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Generating swagger definition for: " + api.getName());
                }
                JSONObject jsonDefinition =
                        new JSONObject(new GenericApiObjectDefinition(api, new MIServerConfig()).getDefinitionMap());
                responseString = jsonDefinition.toString();
            }

            updateResponse(response, responseString, SwaggerConstants.CONTENT_TYPE_JSON);
        }
    }
}
