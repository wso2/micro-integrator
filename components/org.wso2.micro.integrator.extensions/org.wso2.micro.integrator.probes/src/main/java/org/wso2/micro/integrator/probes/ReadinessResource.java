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

package org.wso2.micro.integrator.probes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.micro.integrator.initializer.deployment.application.deployer.CappDeployer;

/**
 * API resource of the readiness probe API.
 */
public class ReadinessResource extends APIResource {

    private static Log log = LogFactory.getLog(ReadinessResource.class);
    private static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    private static final String HTTP_SC = "HTTP_SC";
    private static String CACHED_RESPONSE = "";
    private static int CACHED_RESPONSE_CODE;

    /**
     * Constructor for creating an API Resource.
     *
     * @param urlTemplate the url template of the Resource
     */
    public ReadinessResource(String urlTemplate) {
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
     * Returns 500 if faulty apps are detected.
     */
    @Override
    public boolean invoke(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axisCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
        axisCtx.setProperty(Constants.Configuration.CONTENT_TYPE, "application/json");
        // Sending a response body for a GET request
        axisCtx.removeProperty(NO_ENTITY_BODY);

        String response = "";

        if (CACHED_RESPONSE.isEmpty()) {
            // appending MI server version number to the response
            CarbonServerConfigurationService serverConfig = CarbonServerConfigurationService.getInstance();
            String miVersion = serverConfig.getServerVersion();
            response = "{\"version\":\"" + miVersion + "\",";

            ArrayList<String> faultyCapps = new ArrayList<>(CappDeployer.getFaultyCapps());
            if (faultyCapps.size() > 0) {
                String faultyList = String.join("\",\"", faultyCapps);
                response += "\"status\": \"not ready, faulty CAPPs detected\", \"Faulty CAPPs\" : [\"" + faultyList +
                        "\"]}";
                CACHED_RESPONSE_CODE = 500;
            } else {
                response += "\"status\" : \"ready\"}";
                CACHED_RESPONSE_CODE = 200;
            }
            // update cached response
            CACHED_RESPONSE = response;
        }

        axisCtx.setProperty(HTTP_SC, CACHED_RESPONSE_CODE);

        if (MicroIntegratorBaseUtils.isHotDeploymentEnabled()) {
            log.warn("Hot Deployment and Readiness Probe configurations are both enabled in your server! Note that "
                             + "the readiness probe will not identify faulty artifacts that are hot deployed. Be sure "
                             + "to disable hot deployment if the readiness probe is enabled.");
        }

        try {
            JsonUtil.getNewJsonPayload(axisCtx, CACHED_RESPONSE, true, true);
        } catch (AxisFault axisFault) {
            log.error("Error occurred while generating health-check response", axisFault);
            // sending 500 without a response payload
            axisCtx.setProperty(NO_ENTITY_BODY, true);
            axisCtx.setProperty(HTTP_SC, 500);
        }
        // return true always since return false = return 404
        return true;
    }
}
