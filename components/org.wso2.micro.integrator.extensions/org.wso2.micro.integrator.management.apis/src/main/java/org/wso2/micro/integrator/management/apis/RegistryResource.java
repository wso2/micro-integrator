/**
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.transport.passthru.util.RelayConstants;
import org.apache.synapse.transport.passthru.util.StreamingOnRequestDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.micro.integrator.registry.MicroIntegratorRegistry;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import static org.apache.synapse.SynapseConstants.HTTP_SC;
import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.CONFIGURATION_REGISTRY_PATH;
import static org.wso2.micro.integrator.management.apis.Constants.CONFIGURATION_REGISTRY_PREFIX;
import static org.wso2.micro.integrator.management.apis.Constants.CONTENT;
import static org.wso2.micro.integrator.management.apis.Constants.ERROR_KEY;
import static org.wso2.micro.integrator.management.apis.Constants.EXPAND_PARAM;
import static org.wso2.micro.integrator.management.apis.Constants.FETCH_TYPE;
import static org.wso2.micro.integrator.management.apis.Constants.GOVERNANCE_REGISTRY_PATH;
import static org.wso2.micro.integrator.management.apis.Constants.GOVERNANCE_REGISTRY_PREFIX;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.META_DATA;
import static org.wso2.micro.integrator.management.apis.Constants.NOT_FOUND;
import static org.wso2.micro.integrator.management.apis.Constants.NO_ENTITY_BODY;
import static org.wso2.micro.integrator.management.apis.Constants.PROPERTIES;
import static org.wso2.micro.integrator.management.apis.Constants.PROPERTY_NAME;
import static org.wso2.micro.integrator.management.apis.Constants.PROPERTY_VALUE;
import static org.wso2.micro.integrator.management.apis.Constants.REGISTRY_PATH;
import static org.wso2.micro.integrator.management.apis.Constants.REGISTRY_ROOT_PATH;
import static org.wso2.micro.integrator.management.apis.Constants.VALUE_TRUE;

/**
 * This class provides mechanisms to monitor registry resources.
 */
public class RegistryResource implements MiApiResource {

    private static final Log LOG = LogFactory.getLog(RegistryResource.class);

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

        handleGet(messageContext, axis2MessageContext);
        return true;
    }

    /**
     * This method handles GET.
     *
     * @param messageContext      Synapse message context
     * @param axis2MessageContext AXIS2 message context
     */
    private void handleGet(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext) {

        String registryPath = Utils.getQueryParameter(messageContext, REGISTRY_PATH);
        String fetchType = Utils.getQueryParameter(messageContext, FETCH_TYPE);
        String expandedEnabled = Utils.getQueryParameter(messageContext, EXPAND_PARAM);
        MicroIntegratorRegistry microIntegratorRegistry = new MicroIntegratorRegistry();
        String resolvedPath;
        if (Objects.nonNull(registryPath)) {
            resolvedPath = resolvePath(registryPath, axis2MessageContext);
            if (Objects.nonNull(resolvedPath) && Objects.nonNull(fetchType) && fetchType.equals(META_DATA)) {
                populateRegistryMetadata(axis2MessageContext, microIntegratorRegistry, resolvedPath);
            } else if (Objects.nonNull(resolvedPath) && Objects.nonNull(fetchType) && fetchType.equals(PROPERTIES)) {
                populateRegistryProperties(axis2MessageContext, microIntegratorRegistry, resolvedPath);
            } else if (Objects.nonNull(resolvedPath) && Objects.nonNull(fetchType) && fetchType.equals(CONTENT)) {
                populateRegistryContent(messageContext, axis2MessageContext, resolvedPath);
            } else if (Objects.nonNull(resolvedPath) && Objects.nonNull(fetchType)) {
                JSONObject jsonBody = Utils.createJsonError("Invalid type :" + fetchType, axis2MessageContext,
                        BAD_REQUEST);
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
            } else if (Objects.nonNull(resolvedPath) && Objects.nonNull(expandedEnabled) && expandedEnabled.equals(
                    VALUE_TRUE)) {
                populateRegistryResourceJSON(axis2MessageContext, microIntegratorRegistry, resolvedPath);
            } else if (Objects.nonNull(resolvedPath)) {
                populateImmediateChildren(axis2MessageContext, microIntegratorRegistry, resolvedPath);
            }
        } else {
            populateRegistryResourceJSON(axis2MessageContext, microIntegratorRegistry, REGISTRY_ROOT_PATH);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
    }

    /**
     * This method is used to check the validity of the input path.
     *
     * @param registryPath        File path
     * @param axis2MessageContext AXIS2 message context
     * @return Resolved path according to the OS
     */
    private String resolvePath(String registryPath, org.apache.axis2.context.MessageContext axis2MessageContext) {

        String carbonHomePath = Utils.getCarbonHome();
        String registryRoot = formatPath(carbonHomePath + File.separator + REGISTRY_ROOT_PATH);
        String resolvedPath;

        try {
            File resolvedPathFile = new File(formatPath(carbonHomePath + File.separator + registryPath));
            File registryRootFile = new File(registryRoot);
            if (!resolvedPathFile.getCanonicalPath().startsWith(registryRootFile.getCanonicalPath())) {
                JSONObject jsonBody = Utils.createJsonError("The registry path  '" + registryPath
                                + "' is illegal which points to a location outside the registry", axis2MessageContext,
                        BAD_REQUEST);
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
                return null;
            } else {
                resolvedPath = formatPath(
                        resolvedPathFile.getCanonicalPath().replace(carbonHomePath + File.separator, ""));
            }
        } catch (IOException | IllegalArgumentException e) {
            JSONObject jsonBody = Utils.createJsonError(
                    "Error while resolving the canonical path of the registry path : " + registryPath,
                    axis2MessageContext, BAD_REQUEST);
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
            return null;
        }
        return resolvedPath;
    }

    /**
     * Format the string paths to match any platform.. windows, linux etc..
     *
     * @param path input file path
     * @return formatted file path
     */
    public static String formatPath(String path) {
        // removing white spaces
        String pathformatted = path.replaceAll("\\b\\s+\\b", "%20");
        try {
            pathformatted = java.net.URLDecoder.decode(pathformatted, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported Encoding in the path :" + pathformatted);
        }
        // replacing all "\" with "/"
        return pathformatted.replace('\\', '/');
    }

    /**
     * This method is used to get the <MI-HOME>/registry directory and its content as a JSON.
     *
     * @param axis2MessageContext     AXIS2 message context
     * @param microIntegratorRegistry Micro integrator registry
     */
    private void populateRegistryResourceJSON(org.apache.axis2.context.MessageContext axis2MessageContext,
            MicroIntegratorRegistry microIntegratorRegistry, String path) {

        String carbonHomePath = Utils.getCarbonHome();
        String folderPath = formatPath(carbonHomePath + File.separator + path + File.separator);
        File node = new File(folderPath);
        JSONObject jsonBody;
        if (node.exists() && node.isDirectory()) {
            jsonBody = microIntegratorRegistry.getRegistryResourceJSON(folderPath);
        } else {
            jsonBody = Utils.createJsonError("Invalid registry path", axis2MessageContext, BAD_REQUEST);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
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

        String carbonHomePath = Utils.getCarbonHome();
        String registryPath = formatPath(carbonHomePath + File.separator + path);
        JSONObject jsonBody = microIntegratorRegistry.getRegistryMediaType(registryPath);
        try {
            String error = jsonBody.getString(ERROR_KEY);
            if (Objects.nonNull(error)) {
                jsonBody = Utils.createJsonError(error, axis2MessageContext, INTERNAL_SERVER_ERROR);
            }
        } catch (JSONException ignored) {
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    /**
     * This method is used to fetch all the properties of a specified registry file.
     *
     * @param axis2MessageContext     AXIS2 message context
     * @param microIntegratorRegistry Micro integrator registry
     * @param path                    Registry path
     */
    private void populateRegistryProperties(org.apache.axis2.context.MessageContext axis2MessageContext,
            MicroIntegratorRegistry microIntegratorRegistry, String path) {

        JSONObject jsonBody;
        if (!path.startsWith(CONFIGURATION_REGISTRY_PATH) && !path.startsWith(GOVERNANCE_REGISTRY_PATH)) {
            jsonBody = Utils.createJsonError("Invalid path to fetch properties", axis2MessageContext, BAD_REQUEST);
        } else {
            String registryPath;
            if (path.startsWith(CONFIGURATION_REGISTRY_PATH)) {
                registryPath = path.replace(CONFIGURATION_REGISTRY_PATH, CONFIGURATION_REGISTRY_PREFIX);
            } else {
                registryPath = path.replace(GOVERNANCE_REGISTRY_PATH, GOVERNANCE_REGISTRY_PREFIX);
            }
            Properties propertiesList = microIntegratorRegistry.getResourceProperties(registryPath);
            if (propertiesList != null) {
                jsonBody = Utils.createJSONList(propertiesList.size());
                for (Object property : propertiesList.keySet()) {
                    Object value = propertiesList.get(property);
                    JSONObject propertyObject = new JSONObject();
                    propertyObject.put(PROPERTY_NAME, property);
                    propertyObject.put(PROPERTY_VALUE, value);
                    jsonBody.getJSONArray(Constants.LIST).put(propertyObject);
                }
            } else {
                jsonBody = Utils.createJsonError("Error while fetching properties", axis2MessageContext, NOT_FOUND);
            }
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

        String carbonHomePath = formatPath(Utils.getCarbonHome());
        String registryPath = formatPath(carbonHomePath + File.separator + path);
        File node = new File(registryPath);
        JSONObject jsonBody;
        if (node.exists() && node.isDirectory()) {
            JSONArray childrenList = microIntegratorRegistry.getChildrenList(registryPath, carbonHomePath);
            jsonBody = Utils.createJSONList(childrenList.length());
            jsonBody.put(Constants.LIST, childrenList);
        } else {
            jsonBody = Utils.createJsonError("Invalid registry path", axis2MessageContext, BAD_REQUEST);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    /**
     * This method is to get the content of the specified registry file.
     *
     * @param synCtx              Synapse message context
     * @param axis2MessageContext AXIS2 message context
     * @param pathParameter       Registry path
     */
    private void populateRegistryContent(MessageContext synCtx,
            org.apache.axis2.context.MessageContext axis2MessageContext, String pathParameter) {

        DataHandler dataHandler = downloadRegistryFiles(pathParameter);
        if (dataHandler != null) {
            try {
                InputStream fileInput = dataHandler.getInputStream();
                SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
                SOAPEnvelope env = factory.getDefaultEnvelope();
                OMNamespace ns = factory.createOMNamespace(RelayConstants.BINARY_CONTENT_QNAME.getNamespaceURI(), "ns");
                OMElement omEle = factory.createOMElement(RelayConstants.BINARY_CONTENT_QNAME.getLocalPart(), ns);
                StreamingOnRequestDataSource ds = new StreamingOnRequestDataSource(fileInput);
                dataHandler = new DataHandler(ds);
                OMText textData = factory.createOMText(dataHandler, true);
                omEle.addChild(textData);
                env.getBody().addChild(omEle);
                synCtx.setEnvelope(env);
                axis2MessageContext.removeProperty(NO_ENTITY_BODY);
                axis2MessageContext.setProperty(Constants.MESSAGE_TYPE, "application/octet-stream");
                axis2MessageContext.setProperty(Constants.CONTENT_TYPE, "application/txt");
            } catch (AxisFault e) {
                LOG.error("Error occurred while creating the response", e);
                sendFaultResponse(axis2MessageContext);
            } catch (IOException e) {
                LOG.error("Error occurred while reading the input stream", e);
                sendFaultResponse(axis2MessageContext);
            }
        } else {
            sendFaultResponse(axis2MessageContext);
        }
    }

    private void sendFaultResponse(org.apache.axis2.context.MessageContext axis2MessageContext) {

        axis2MessageContext.setProperty(NO_ENTITY_BODY, true);
        axis2MessageContext.setProperty(HTTP_SC, 500);
    }

    private DataHandler downloadRegistryFiles(String path) {

        ByteArrayDataSource bytArrayDS;
        String carbonHomePath = Utils.getCarbonHome();
        String registryPath = formatPath(carbonHomePath + File.separator + path);
        File file = new File(registryPath);
        if (file.exists() && !file.isDirectory()) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(registryPath))) {
                bytArrayDS = new ByteArrayDataSource(is, "text/xml");
                return new DataHandler(bytArrayDS);
            } catch (FileNotFoundException e) {
                LOG.error("Could not find the requested file : " + path + " in : " + registryPath, e);
                return null;
            } catch (IOException e) {
                LOG.error("Error occurred while reading the file : " + path + " in : " + registryPath, e);
                return null;
            }
        } else {
            LOG.error("Could not find the requested file : " + path + " in : " + registryPath);
            return null;
        }
    }
}
