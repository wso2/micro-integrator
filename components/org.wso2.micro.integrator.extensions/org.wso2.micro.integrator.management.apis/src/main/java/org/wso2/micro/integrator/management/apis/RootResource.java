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

package org.wso2.micro.integrator.management.apis;

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

import java.util.HashSet;
import java.util.Set;

/**
 * Root resource of Management Api.
 */
public class RootResource implements MiApiResource {

    private Set<String> methods;

    RootResource() {
        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext msgCtx, org.apache.axis2.context.MessageContext axisMsgCtx,
                          SynapseConfiguration synapseConfiguration) {

        CarbonServerConfigurationService serverConfig = CarbonServerConfigurationService.getInstance();
        String miVersion = serverConfig.getServerVersion();
        String response = "{ \n \"WSO2 Micro Integrator\" : \"" + miVersion + "\", \n \"ManagementApi\" : "
                + "\"Active\"\n}";
        Utils.setJsonPayLoad(axisMsgCtx, response);
        return true;
    }
}
