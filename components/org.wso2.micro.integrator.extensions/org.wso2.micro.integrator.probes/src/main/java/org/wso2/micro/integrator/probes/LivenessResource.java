/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.probes;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.Set;

/**
 * API resource of the liveness probe API.
 */
public class LivenessResource extends APIResource {

    private static final Log log = LogFactory.getLog(LivenessResource.class);
    private static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    private static final String HTTP_SC = "HTTP_SC";

    /**
     * Constructor for creating an API Resource.
     *
     * @param urlTemplate the url template of the Resource
     */
    public LivenessResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<>();
        methods.add("GET");
        return methods;
    }


    /**
     * Health-check implementation.
     */
    @Override
    public boolean invoke(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axisCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
        axisCtx.setProperty(Constants.Configuration.CONTENT_TYPE, "application/json");
        // Sending a response body for a GET request
        axisCtx.removeProperty(NO_ENTITY_BODY);

        String response = "{\"server\" : \"started\"}";
        int responseCode = 200;

        try {
            JsonUtil.getNewJsonPayload(axisCtx, response, true, true);
            axisCtx.setProperty(HTTP_SC, responseCode);
        } catch (AxisFault axisFault) {
            log.error("Error occurred while generating server started health-check response", axisFault);
            // sending 500 without a response payload
            axisCtx.setProperty(NO_ENTITY_BODY, true);
            axisCtx.setProperty(HTTP_SC, 500);
        }
        // return true always since return false = return 404
        return true;
    }
}
