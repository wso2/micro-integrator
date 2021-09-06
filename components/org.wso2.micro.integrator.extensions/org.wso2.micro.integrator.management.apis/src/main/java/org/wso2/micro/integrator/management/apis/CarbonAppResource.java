/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayConstants;
import org.apache.synapse.transport.passthru.util.StreamingOnRequestDataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.micro.application.deployer.CarbonApplication;
import org.wso2.micro.application.deployer.config.Artifact;
import org.wso2.micro.integrator.initializer.deployment.application.deployer.CappDeployer;
import org.wso2.micro.integrator.management.apis.security.handler.SecurityUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.NOT_FOUND;

public class CarbonAppResource extends APIResource {

    private static final Log log = LogFactory.getLog(CarbonAppResource.class);
    private static final String MULTIPART_FORMDATA_DATA_TYPE = "multipart/form-data";
    private static final String CAPP_NAME = "name";
    // HTTP method types supported by the resource
    private Set<String> methods;

    public CarbonAppResource(String urlTemplate){
        super(urlTemplate);
        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
        methods.add(Constants.HTTP_DELETE);

    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String httpMethod = axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY).toString();
        if (log.isDebugEnabled()) {
            log.debug("Handling " + httpMethod + " request.");
        }

        switch (httpMethod) {
            case Constants.HTTP_GET: {
                String param = Utils.getQueryParameter(messageContext, "carbonAppName");

                if (Objects.nonNull(param)) {
                    String acceptHeader = (String) SecurityUtils.getHeaders(axis2MessageContext)
                                                                .get(HTTPConstants.HEADER_ACCEPT);
                    if (acceptHeader.equalsIgnoreCase(Constants.MEDIA_TYPE_APPLICATION_OCTET_STREAM)) {
                        populateFileContent(messageContext, param);
                    } else {
                        populateCarbonAppData(messageContext, param);
                    }
                } else {
                    populateCarbonAppList(messageContext);
                }
                break;
            }
            case Constants.HTTP_POST: {
                handlePost(axis2MessageContext);
                break;
            }
            case Constants.HTTP_DELETE: {
                handleDelete(messageContext, axis2MessageContext);
                break;
            }
            default: {
                Utils.setJsonPayLoad(axis2MessageContext,
                        Utils.createJsonError("Unsupported HTTP method, " + httpMethod + ". Only GET , " +
                                "POST and DELETE methods are supported.",
                        axis2MessageContext, BAD_REQUEST));
                break;
            }
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    /**
     * Populate file content.
     * @param synCtx message context
     * @param cAppName carbon application name
     */
    private void populateFileContent(MessageContext synCtx, String cAppName) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        DataHandler dataHandler = createDataHandler(cAppName);
        if (dataHandler != null) {
            try {
                InputStream fileInput = dataHandler.getInputStream();
                SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
                SOAPEnvelope env = factory.getDefaultEnvelope();
                OMNamespace ns =
                        factory.createOMNamespace(RelayConstants.BINARY_CONTENT_QNAME.getNamespaceURI(), "ns");
                OMElement omEle = factory.createOMElement(RelayConstants.BINARY_CONTENT_QNAME.getLocalPart(), ns);
                StreamingOnRequestDataSource ds = new StreamingOnRequestDataSource(fileInput);
                dataHandler = new DataHandler(ds);
                OMText textData = factory.createOMText(dataHandler, true);
                omEle.addChild(textData);
                env.getBody().addChild(omEle);
                synCtx.setEnvelope(env);
                axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
                axis2MessageContext.setProperty(Constants.MESSAGE_TYPE, Constants.MEDIA_TYPE_APPLICATION_OCTET_STREAM);
                axis2MessageContext.setProperty(Constants.CONTENT_TYPE, Constants.MEDIA_TYPE_APPLICATION_OCTET_STREAM);
            } catch (AxisFault e) {
                log.error("Error occurred while creating the response", e);
                sendFaultResponse(axis2MessageContext);
            } catch (IOException e) {
                log.error("Error occurred while reading the input stream", e);
                sendFaultResponse(axis2MessageContext);
            }
        }
        else {
            sendFaultResponse(axis2MessageContext);
        }
    }

    /**
     * Create a data-handler for carbon application.
     * @param cAppName carbon application name
     * @return datahandler
     */
    private DataHandler createDataHandler(String cAppName) {
        ByteArrayDataSource bytArrayDS;
        Path cAppPath = Paths.get(Utils.getCAppPath(), cAppName);
        File file = new File(cAppPath.toString());
        if (file.exists() && !file.isDirectory()) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(cAppPath.toString()))) {
                bytArrayDS = new ByteArrayDataSource(is, Constants.MEDIA_TYPE_APPLICATION_OCTET_STREAM);
                return new DataHandler(bytArrayDS);
            } catch (FileNotFoundException e) {
                log.error("Could not find the requested file : " + cAppName + " in : " + cAppPath.toString(), e);
                return null;
            } catch (IOException e) {
                log.error("Error occurred while reading the file : " + cAppName + " in : " + cAppPath.toString(), e);
                return null;
            }
        } else {
            log.error("Could not find the requested file : " + cAppName + " in : " + cAppPath.toString());
            return null;
        }
    }

    private void handlePost(org.apache.axis2.context.MessageContext axisMsgCtx) {
        JSONObject jsonResponse = new JSONObject();
        String contentType = axisMsgCtx.getProperty(Constants.CONTENT_TYPE).toString();
        if (!contentType.contains(MULTIPART_FORMDATA_DATA_TYPE)) {
            JSONObject response = Utils.createJsonError("Error when deploying the Carbon Application. " +
                    "Supports only for the Content-Type : " + MULTIPART_FORMDATA_DATA_TYPE, axisMsgCtx, BAD_REQUEST);
            Utils.setJsonPayLoad(axisMsgCtx, response);
            return;
        }
        SOAPBody soapBody = axisMsgCtx.getEnvelope().getBody();
        if (null != soapBody) {
            OMElement messageBody = soapBody.getFirstElement();
            if (null != messageBody) {
                Iterator iterator = messageBody.getChildElements();
                if (iterator.hasNext()) {
                    OMElement fileElement = (OMElement) iterator.next();
                    if (!iterator.hasNext()) {
                        String fileName = fileElement.getAttributeValue(new QName("filename"));
                        if (fileName != null && fileName.endsWith(".car")) {
                            byte[] bytes = Base64.getDecoder().decode(fileElement.getText());
                            Path cAppDirectoryPath = Paths.get(Utils.getCarbonHome(), "repository", "deployment",
                                    "server", "carbonapps", fileName);
                            try {
                                Files.write(cAppDirectoryPath, bytes);
                                log.info("Successfully added Carbon Application : " + fileName);
                                jsonResponse.put(Constants.MESSAGE_JSON_ATTRIBUTE, "Successfully added Carbon Application "
                                        + fileName);
                                Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
                            } catch (IOException e) {
                                String errorMessage = "Error when deploying the Carbon Application ";
                                log.error(errorMessage + fileName, e);
                                Utils.setJsonPayLoad(axisMsgCtx, Utils.createJsonErrorObject(errorMessage));
                            }
                        } else {
                            jsonResponse = Utils.createJsonError("Error when deploying the Carbon Application. " +
                                    "Only files with the extension .car is supported. ", axisMsgCtx, BAD_REQUEST);
                            Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
                        }
                    } else {
                        jsonResponse = Utils.createJsonError("Error when deploying the Carbon Application. " +
                                "Uploading Multiple files in one request is not supported. ", axisMsgCtx, BAD_REQUEST);
                        Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
                    }
                } else {
                    jsonResponse = Utils.createJsonError("Error when deploying the Carbon Application. " +
                            "No file exist to be uploaded. ", axisMsgCtx, BAD_REQUEST);
                    Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
                }
            } else {
                jsonResponse = Utils.createJsonError("Error when deploying the Carbon Application. " +
                        "No valid element found. ", axisMsgCtx, BAD_REQUEST);
                Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
            }
        } else {
            jsonResponse = Utils.createJsonError("Error when deploying the Carbon Application. " +
                            "No valid message body found. ", axisMsgCtx, BAD_REQUEST);
            Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
        }
    }

    private void handleDelete(MessageContext messageContext, org.apache.axis2.context.MessageContext axisMsgCtx) {
        String cAppName = Utils.getPathParameter(messageContext, CAPP_NAME);
        JSONObject jsonResponse = new JSONObject();
        if (!Objects.isNull(cAppName)) {
            try {
                String cAppsDirectoryPath = Paths.get(
                        Utils.getCarbonHome(), "repository", "deployment", "server", "carbonapps").toString();

                // List deployed CApp which has downloaded CApp name
                File carbonAppsDirectory = new File(cAppsDirectoryPath);
                File[] existingCApps = carbonAppsDirectory.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.equals(cAppName + ".car");
                    }
                });

                // Remove deployed CApp which has downloaded CApp name
                if (existingCApps != null && existingCApps.length != 0) {
                    //there should be only one capp entry
                    File cApp = existingCApps[0];
                    Files.delete(cApp.toPath());
                    log.info(cApp.getName() + " file deleted from " + cAppsDirectoryPath + " directory");
                    jsonResponse.put(Constants.MESSAGE_JSON_ATTRIBUTE, "Successfully removed Carbon Application " +
                            "named " + cAppName);
                } else {
                    jsonResponse = Utils.createJsonError("Cannot remove the Carbon Application." +
                            cAppName + " does not exist", axisMsgCtx, NOT_FOUND);
                }
                Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
            } catch (IOException e) {
                String message = "Error when removing the Carbon Application " + cAppName + ".car";
                log.error(message, e);
                Utils.setJsonPayLoad(axisMsgCtx, Utils.createJsonErrorObject(message));
            }
        } else {
            jsonResponse = Utils.createJsonError("Cannot remove the Carbon Application. Missing required " +
                    CAPP_NAME + " parameter in the path", axisMsgCtx, BAD_REQUEST);
            Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
        }
    }

    private void populateCarbonAppList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<CarbonApplication> appList
                = CappDeployer.getCarbonApps();

        List<CarbonApplication> faultyAppList = CappDeployer.getFaultyCAppObjects();

        JSONObject jsonBody = Utils.createCAppJSONList(appList.size(), faultyAppList.size());

        for (CarbonApplication app: appList) {

            JSONObject appObject = new JSONObject();
            appObject.put(Constants.NAME, app.getAppName());
            appObject.put(Constants.VERSION, app.getAppVersion());

            jsonBody.getJSONArray(Constants.ACTIVE_LIST).put(appObject);
        }

        for (CarbonApplication faultyApp: faultyAppList) {

            JSONObject appObject = new JSONObject();
            appObject.put(Constants.NAME, faultyApp.getAppName());
            appObject.put(Constants.VERSION, faultyApp.getAppVersion());

            jsonBody.getJSONArray(Constants.FAULTY_LIST).put(appObject);
        }

        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateCarbonAppData(MessageContext messageContext, String carbonAppName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getCarbonAppByName(carbonAppName);

        if (Objects.nonNull(jsonBody)) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        }
    }

    private JSONObject getCarbonAppByName(String carbonAppName) {

        List<CarbonApplication> appList
                = CappDeployer.getCarbonApps();

        for (CarbonApplication app: appList) {
            if (app.getAppName().equals(carbonAppName)) {
                return convertCarbonAppToJsonObject(app);
            }
        }
        return null;
    }

    private JSONObject convertCarbonAppToJsonObject(CarbonApplication carbonApp) {

        if (Objects.isNull(carbonApp)) {
            return null;
        }

        JSONObject appObject = new JSONObject();

        appObject.put(Constants.NAME, carbonApp.getAppName());
        appObject.put(Constants.VERSION, carbonApp.getAppVersion());

        JSONArray artifactListObject = new JSONArray();
        appObject.put("artifacts", artifactListObject);

        List<Artifact.Dependency> dependencies = carbonApp.getAppConfig().
                getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dependency : dependencies) {

            Artifact artifact = dependency.getArtifact();

            String type = artifact.getType().split("/")[1];
            String artifactName = artifact.getName();

            // if the artifactName is null, artifact deployment has failed..
            if (Objects.isNull(artifactName)) {
                continue;
            }

            JSONObject artifactObject = new JSONObject();

            artifactObject.put(Constants.NAME, artifactName);
            artifactObject.put(Constants.TYPE, type);

            artifactListObject.put(artifactObject);
        }
        return appObject;
    }

    private void sendFaultResponse(org.apache.axis2.context.MessageContext axis2MessageContext) {
        axis2MessageContext.setProperty(Constants.NO_ENTITY_BODY, true);
        axis2MessageContext.setProperty(SynapseConstants.HTTP_SC, 500);
    }
}
