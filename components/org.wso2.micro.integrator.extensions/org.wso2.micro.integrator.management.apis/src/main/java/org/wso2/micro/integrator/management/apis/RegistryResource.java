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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.micro.integrator.registry.MicroIntegratorRegistry;

import java.io.File;
import java.util.Set;
import java.util.Objects;
import java.util.HashSet;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.EXPAND_PARAM;
import static org.wso2.micro.integrator.management.apis.Constants.REGISTRY_PATH;
import static org.wso2.micro.integrator.management.apis.Constants.SEARCH_KEY;
import static org.wso2.micro.integrator.management.apis.Constants.VALUE_TRUE;
import static org.wso2.micro.integrator.management.apis.Utils.formatPath;
import static org.wso2.micro.integrator.management.apis.Utils.validatePath;

/**
 * This class provides mechanisms to monitor registry directory.
 */
public class RegistryResource implements MiApiResource {

    Set<String> methods;
    public RegistryResource() {

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
        String searchKey = Utils.getQueryParameter(messageContext, SEARCH_KEY);

        if (StringUtils.isEmpty(validatedPath)) {
            axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
            return true;
        }
        if (Objects.nonNull(searchKey)) {
            if (searchKey.trim().isEmpty()) {
                handleGet(messageContext, axis2MessageContext, validatedPath);
            } else {
                populateRegistryResourceJSON(searchKey, axis2MessageContext, (MicroIntegratorRegistry) messageContext.getConfiguration().getRegistry(), validatedPath);
            }
        } else {
            handleGet(messageContext, axis2MessageContext, validatedPath);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    /**
     * This method handles GET.
     *
     * @param messageContext      Synapse message context
     * @param axis2MessageContext AXIS2 message context
     */
    private void handleGet(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
            String validatedPath) {

        String expandedEnabled = Utils.getQueryParameter(messageContext, EXPAND_PARAM);
        MicroIntegratorRegistry microIntegratorRegistry = (MicroIntegratorRegistry) messageContext.getConfiguration().getRegistry();
        if (Objects.nonNull(expandedEnabled) && expandedEnabled.equals(VALUE_TRUE)) {
            populateRegistryResourceJSON("", axis2MessageContext, microIntegratorRegistry, validatedPath);
        } else {
            populateImmediateChildren(axis2MessageContext, microIntegratorRegistry, validatedPath);
        }
    }

    /**
     * This method is used to get the <MI-HOME>/registry directory and its content which match with the search key,
     * as a JSON.
     *
     * @param searchKey String
     * @param axis2MessageContext     AXIS2 message context
     * @param microIntegratorRegistry Micro integrator registry
     */
    private void populateRegistryResourceJSON(String searchKey, org.apache.axis2.context.MessageContext axis2MessageContext,
            MicroIntegratorRegistry microIntegratorRegistry, String path) {

        String regRoot = microIntegratorRegistry.getRegRoot();
        String folderPath = formatPath(regRoot + File.separator + path + File.separator);
        File node = new File(folderPath);
        JSONObject jsonBody;
        if (node.exists() && node.isDirectory()) {
            jsonBody = microIntegratorRegistry.getRegistryResourceJSON(searchKey, folderPath);
        } else {
            jsonBody = Utils.createJsonError("Invalid registry path", axis2MessageContext, BAD_REQUEST);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    /**
     * This method is to get the immediate child files, folders of a given directory with their metadata and properties.
     *
     * @param axis2MessageContext     AXIS2 message context
     * @param microIntegratorRegistry Micro integrator registry
     * @param path                    Registry path
     */
    private void populateImmediateChildren(org.apache.axis2.context.MessageContext axis2MessageContext,
            MicroIntegratorRegistry microIntegratorRegistry, String path) {

        String regRoot = microIntegratorRegistry.getRegRoot();
        String registryPath = formatPath(regRoot + File.separator + path);
        File node = new File(registryPath);
        JSONObject jsonBody;
        if (node.exists() && node.isDirectory()) {
            JSONArray childrenList = microIntegratorRegistry.getChildrenList(registryPath, regRoot);
            jsonBody = Utils.createJSONList(childrenList.length());
            jsonBody.put(Constants.LIST, childrenList);
        } else {
            jsonBody = Utils.createJsonError("Invalid registry path", axis2MessageContext, BAD_REQUEST);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

}
