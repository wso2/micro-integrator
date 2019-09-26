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

package org.wso2.micro.integrator.management.apis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONObject;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * This class serves metadata related to the server.
 */
public class MetaDataResource implements MiApiResource {

    private static final Log LOG = LogFactory.getLog(MetaDataResource.class);

    // HTTP method types supported by the resource
    Set<String> methods;

    public MetaDataResource() {
        methods = new HashSet<>(1);
        methods.add(Constants.HTTP_GET);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext,
                          org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {

        populateMetaData(axis2MessageContext);
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    /**
     * Populate metadata to the axis2message context.
     *
     * @param axis2MessageContext
     */
    private void populateMetaData(org.apache.axis2.context.MessageContext axis2MessageContext) {

        JSONObject jsonObject = new JSONObject();
        CarbonServerConfigurationService serverConfigurationService = MicroIntegratorBaseUtils.getServerConfiguration();
        jsonObject.put("carbonHome", System.getProperty("carbon.home"));
        jsonObject.put("javaHome", System.getProperty("java.home"));
        jsonObject.put("productName", serverConfigurationService.getFirstProperty("Name"));
        jsonObject.put("productVersion", serverConfigurationService.getFirstProperty("Version"));
        jsonObject.put("workDirectory", serverConfigurationService.getFirstProperty("WorkDirectory"));
        jsonObject.put("repositoryLocation", serverConfigurationService.getFirstProperty("Axis2Config.ClientRepositoryLocation"));
        Utils.setJsonPayLoad(axis2MessageContext, jsonObject);
    }
}
