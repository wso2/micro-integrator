/**
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 */

package org.wso2.micro.integrator.management.apis;

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.micro.integrator.registry.MicroIntegratorRegistry;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.ERROR_KEY;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.REGISTRY_PATH;
import static org.wso2.micro.integrator.management.apis.Utils.formatPath;
import static org.wso2.micro.integrator.management.apis.Utils.isRegistryExist;
import static org.wso2.micro.integrator.management.apis.Utils.validatePath;

/**
 * This class provides mechanisms to monitor registry metadata.
 */
public class RegistryMetadataResource implements MiApiResource {

    Set<String> methods;

    public RegistryMetadataResource() {

        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
            SynapseConfiguration synapseConfiguration) {

        String registryPath = Utils.getQueryParameter(messageContext, REGISTRY_PATH);
        String validatedPath = validatePath(registryPath, axis2MessageContext, messageContext);

        if (StringUtils.isEmpty(validatedPath)) {
            axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
            return true;
        }
        handleGet(messageContext, axis2MessageContext, registryPath, validatedPath);
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    /**
     * This method handles GET.
     *
     * @param axis2MessageContext AXIS2 message context
     */
    private void handleGet(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
            String registryPath, String validatedPath) {

        MicroIntegratorRegistry microIntegratorRegistry = new MicroIntegratorRegistry();
        if (!isRegistryExist(validatedPath, messageContext)) {
            JSONObject jsonBody = Utils.createJsonError("Can not find the registry: " + registryPath,
                    axis2MessageContext, BAD_REQUEST);
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            populateRegistryMetadata(axis2MessageContext, microIntegratorRegistry, validatedPath);
        }
    }

    /**
     * This method is used to fetch the metadata(media type) of a specified registry file.
     *
     * @param axis2MessageContext     AXIS2 message context
     * @param microIntegratorRegistry Micro integrator registry
     * @param path                    Registry path
     */
    private void populateRegistryMetadata(org.apache.axis2.context.MessageContext axis2MessageContext,
            MicroIntegratorRegistry microIntegratorRegistry, String path) {

        String regRoot = microIntegratorRegistry.getRegRoot();
        String registryPath = formatPath(regRoot + File.separator + path);
        JSONObject jsonBody = microIntegratorRegistry.getRegistryMediaType(registryPath);
        try {
            String error = jsonBody.getString(ERROR_KEY);
            if (Objects.nonNull(error)) {
                jsonBody = Utils.createJsonError(error, axis2MessageContext, INTERNAL_SERVER_ERROR);
            }
        } catch (JSONException ignored) { }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }
}
