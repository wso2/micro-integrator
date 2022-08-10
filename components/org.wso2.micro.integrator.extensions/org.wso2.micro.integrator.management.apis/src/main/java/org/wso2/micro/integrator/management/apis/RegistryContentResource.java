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
import org.json.JSONObject;
import org.wso2.micro.integrator.management.apis.security.handler.SecurityUtils;
import org.wso2.micro.integrator.registry.MicroIntegratorRegistry;
import org.wso2.micro.integrator.security.user.api.UserStoreException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.DEFAULT_MEDIA_TYPE;
import static org.wso2.micro.integrator.management.apis.Constants.HTTP_DELETE;
import static org.wso2.micro.integrator.management.apis.Constants.HTTP_GET;
import static org.wso2.micro.integrator.management.apis.Constants.HTTP_POST;
import static org.wso2.micro.integrator.management.apis.Constants.HTTP_PUT;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.MEDIA_TYPE_KEY;
import static org.wso2.micro.integrator.management.apis.Constants.NO_ENTITY_BODY;
import static org.wso2.micro.integrator.management.apis.Constants.REGISTRY_PATH;
import static org.wso2.micro.integrator.management.apis.Constants.USERNAME_PROPERTY;
import static org.wso2.micro.integrator.management.apis.Utils.formatPath;
import static org.wso2.micro.integrator.management.apis.Utils.getPayloadAsString;
import static org.wso2.micro.integrator.management.apis.Utils.getRegistryPathPrefix;
import static org.wso2.micro.integrator.management.apis.Utils.isRegistryExist;
import static org.wso2.micro.integrator.management.apis.Utils.isValidFileType;
import static org.wso2.micro.integrator.management.apis.Utils.validatePath;

/**
 * This class provides mechanisms to monitor content of the registry content.
 */
public class RegistryContentResource implements MiApiResource {

    private static final Log LOG = LogFactory.getLog(RegistryContentResource.class);
    Set<String> methods;

    public RegistryContentResource() {

        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
        methods.add(Constants.HTTP_PUT);
        methods.add(Constants.HTTP_DELETE);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
            SynapseConfiguration synapseConfiguration) {

        String httpMethod = axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY).toString();
        if (httpMethod.equals(HTTP_GET)) {
            handleGet(messageContext, axis2MessageContext);
        } else {
            try {
                if (SecurityUtils.isAdmin(messageContext.getProperty(USERNAME_PROPERTY).toString())) {
                    switch (httpMethod) {
                    case HTTP_POST:
                        handlePost(messageContext, axis2MessageContext);
                        break;
                    case HTTP_PUT:
                        handlePut(messageContext, axis2MessageContext);
                        break;
                    case HTTP_DELETE:
                        handleDelete(messageContext, axis2MessageContext);
                        break;
                    default:
                        JSONObject jsonBody = Utils.createJsonError("Unsupported HTTP method, " + httpMethod
                                        + ". Only GET, POST, PUT and DELETE methods are supported", axis2MessageContext,
                                BAD_REQUEST);
                        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
                        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
                        break;
                    }
                } else {
                    Utils.sendForbiddenFaultResponse(axis2MessageContext);
                }
            } catch (UserStoreException e) {
                JSONObject jsonBody = Utils.createJsonError("Error occurs while retrieving the user data",
                        axis2MessageContext, INTERNAL_SERVER_ERROR);
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
                axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
            }
        }
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
        String validatedPath;
        if (Objects.nonNull(registryPath)) {
            validatedPath = validatePath(registryPath, axis2MessageContext);
            if (Objects.nonNull(validatedPath)) {
                if (!isRegistryExist(validatedPath)) {
                    JSONObject jsonBody = Utils.createJsonError("Can not find the registry: " + registryPath,
                            axis2MessageContext, BAD_REQUEST);
                    Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
                } else {
                    populateRegistryContent(messageContext, axis2MessageContext, validatedPath);
                }
            }
        } else {
            JSONObject jsonBody = Utils.createJsonError("Registry path not found in the request", axis2MessageContext,
                    BAD_REQUEST);
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
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
                JSONObject jsonBody = Utils.createJsonError("Error occurred while creating the response",
                        axis2MessageContext, INTERNAL_SERVER_ERROR);
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
            } catch (IOException e) {
                LOG.error("Error occurred while reading the input stream", e);
                JSONObject jsonBody = Utils.createJsonError("Error occurred while reading the input stream",
                        axis2MessageContext, INTERNAL_SERVER_ERROR);
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
            }
        } else {
            JSONObject jsonBody = Utils.createJsonError("Error occurred while reading the requested file",
                    axis2MessageContext, INTERNAL_SERVER_ERROR);
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        }
    }

    /**
     * This method fetches content from the registry.
     *
     * @param path  Registry path
     * @return      DataHandler object with registry content
     */
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

    /**
     * This method handles POST.
     *
     * @param messageContext      Synapse message context
     * @param axis2MessageContext AXIS2 message context
     */
    private void handlePost(MessageContext messageContext,
            org.apache.axis2.context.MessageContext axis2MessageContext) {

        String registryPath = Utils.getQueryParameter(messageContext, REGISTRY_PATH);
        String validatedPath;
        JSONObject jsonBody;
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        if (Objects.nonNull(registryPath)) {
            if (isValidFileType(registryPath)) {
                validatedPath = validatePath(registryPath, axis2MessageContext);
                if (Objects.nonNull(validatedPath)) {
                    String pathWithPrefix = getRegistryPathPrefix(validatedPath);
                    if (isRegistryExist(validatedPath)) {
                        jsonBody = Utils.createJsonError("Registry already exists. Can not POST an existing registry",
                                axis2MessageContext, BAD_REQUEST);
                    } else if (Objects.nonNull(pathWithPrefix)) {
                        jsonBody = postRegistryArtifact(messageContext, axis2MessageContext, pathWithPrefix);
                    } else {
                        jsonBody = Utils.createJsonError("Invalid registry path: " + registryPath, axis2MessageContext,
                                BAD_REQUEST);
                    }
                    Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
                }
            } else {
                jsonBody = Utils.createJsonError("File type of the registry is not supported", axis2MessageContext,
                        BAD_REQUEST);
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
            }
        } else {
            jsonBody = Utils.createJsonError("Registry path not found in the request", axis2MessageContext,
                    BAD_REQUEST);
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
    }

    /**
     * This method handles adding registry resources.
     *
     * @param messageContext        Synapse message context
     * @param axis2MessageContext   AXIS2 message context
     * @param registryPath          Registry path
     * @return                      A JSON object with the final status
     */
    private JSONObject postRegistryArtifact(MessageContext messageContext,
            org.apache.axis2.context.MessageContext axis2MessageContext, String registryPath) {

        String fileContent = getPayloadAsString(axis2MessageContext);
        String mediaType = Utils.getQueryParameter(messageContext, MEDIA_TYPE_KEY);
        JSONObject jsonBody = new JSONObject();
        String contentType = DEFAULT_MEDIA_TYPE;
        if (Objects.nonNull(mediaType)) {
            contentType = mediaType;
        }
        if (Objects.nonNull(fileContent)) {
            MicroIntegratorRegistry microIntegratorRegistry = new MicroIntegratorRegistry();
            microIntegratorRegistry.newNonEmptyResource(registryPath, false, contentType, fileContent, "");
            jsonBody.put("message", "Successfully added the registry resource");
        } else {
            jsonBody = Utils.createJsonError("Error while fetching file content from payload", axis2MessageContext,
                    BAD_REQUEST);
        }
        return jsonBody;
    }

    /**
     * This method handles PUT.
     *
     * @param messageContext      Synapse message context
     * @param axis2MessageContext AXIS2 message context
     */
    private void handlePut(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext) {

        String registryPath = Utils.getQueryParameter(messageContext, REGISTRY_PATH);
        String validatedPath;
        JSONObject jsonBody;
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        if (Objects.nonNull(registryPath)) {
            validatedPath = validatePath(registryPath, axis2MessageContext);
            if (Objects.nonNull(validatedPath)) {
                String pathWithPrefix = getRegistryPathPrefix(validatedPath);
                if (!isRegistryExist(validatedPath)) {
                    jsonBody = Utils.createJsonError("Registry does not exists in the path: " + registryPath,
                            axis2MessageContext, BAD_REQUEST);
                } else if (Objects.nonNull(pathWithPrefix)) {
                    jsonBody = putRegistryArtifact(axis2MessageContext, pathWithPrefix);
                } else {
                    jsonBody = Utils.createJsonError("Invalid registry path: " + registryPath, axis2MessageContext,
                            BAD_REQUEST);
                }
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
            }
        } else {
            jsonBody = Utils.createJsonError("Registry path not found in the request", axis2MessageContext,
                    BAD_REQUEST);
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
    }

    /**
     * This method handles modifying registry resources.
     *
     * @param axis2MessageContext   AXIS2 message context
     * @param registryPath          Registry path
     * @return                      A JSON object with the final status
     */
    private JSONObject putRegistryArtifact(org.apache.axis2.context.MessageContext axis2MessageContext,
            String registryPath) {

        String fileContent = getPayloadAsString(axis2MessageContext);
        JSONObject jsonBody = new JSONObject();
        if (Objects.nonNull(fileContent)) {
            MicroIntegratorRegistry microIntegratorRegistry = new MicroIntegratorRegistry();
            microIntegratorRegistry.updateResource(registryPath, fileContent);
            jsonBody.put("message", "Successfully modified the registry resource");
        } else {
            jsonBody = Utils.createJsonError("Error while fetching file content from payload", axis2MessageContext,
                    BAD_REQUEST);
        }
        return jsonBody;
    }

    /**
     * This method handles DELETE.
     *
     * @param messageContext      Synapse message context
     * @param axis2MessageContext AXIS2 message context
     */
    private void handleDelete(MessageContext messageContext,
            org.apache.axis2.context.MessageContext axis2MessageContext) {

        String registryPath = Utils.getQueryParameter(messageContext, REGISTRY_PATH);
        String validatedPath;
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        JSONObject jsonBody;
        if (Objects.nonNull(registryPath)) {
            validatedPath = validatePath(registryPath, axis2MessageContext);
            if (Objects.nonNull(validatedPath)) {
                String pathWithPrefix = getRegistryPathPrefix(validatedPath);
                if (!isRegistryExist(validatedPath)) {
                    jsonBody = Utils.createJsonError("Registry does not exists in the path: " + registryPath,
                            axis2MessageContext, BAD_REQUEST);
                } else if (Objects.nonNull(pathWithPrefix)) {
                    jsonBody = deleteRegistryArtifact(pathWithPrefix);
                } else {
                    jsonBody = Utils.createJsonError("Invalid registry path: " + registryPath, axis2MessageContext,
                            BAD_REQUEST);
                }
                Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
            }
        } else {
            jsonBody = Utils.createJsonError("Registry path not found in the request", axis2MessageContext,
                    BAD_REQUEST);
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
    }

    /**
     * Deletes the registry resources and directories.
     *
     * @param registryPath  Registry path
     * @return              JSON object indicating the final status
     */
    private JSONObject deleteRegistryArtifact(String registryPath) {
        MicroIntegratorRegistry microIntegratorRegistry = new MicroIntegratorRegistry();
        microIntegratorRegistry.delete(registryPath);
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("message", "Successfully deleted the registry resource");
        return jsonBody;
    }
}
