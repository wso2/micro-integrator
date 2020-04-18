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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayConstants;
import org.apache.synapse.transport.passthru.util.StreamingOnRequestDataSource;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import static org.apache.synapse.SynapseConstants.HTTP_SC;
import static org.wso2.micro.integrator.management.apis.Constants.NO_ENTITY_BODY;

/**
 * This resource will provide list of log files in the repository/logs directory and the capability to download the
 * files.
 */
public class LogFilesResource extends APIResource {

    private static final Log log = LogFactory.getLog(LogFilesResource.class);

    public LogFilesResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        return methods;
    }

    @Override
    public boolean invoke(MessageContext synCtx) {

        String pathParameter = Utils.getQueryParameter(synCtx, "file");
        if (StringUtils.isNotEmpty(pathParameter)) {
            populateFileContent(synCtx, pathParameter);
        } else {
            populateLogFileInfo(synCtx);
        }
        return true;
    }

    private void populateLogFileInfo(MessageContext synCtx) {

        List<LogFileInfo> logFileInfoList = Utils.getLogFileInfoList();
        JSONObject jsonBody = Utils.createJSONList(logFileInfoList.size());

        for (LogFileInfo logFileInfo : logFileInfoList) {
            JSONObject logfileObject = new JSONObject();
            logfileObject.put("FileName", logFileInfo.getLogName());
            logfileObject.put("Date", logFileInfo.getLogDate());
            logfileObject.put("Size", logFileInfo.getFileSize());
            jsonBody.getJSONArray(Constants.LIST).put(logfileObject);
        }
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        axis2MessageContext.removeProperty(NO_ENTITY_BODY);
    }

    private void populateFileContent(MessageContext synCtx, String pathParameter) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        DataHandler dataHandler = downloadArchivedLogFiles(pathParameter);
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
                axis2MessageContext.removeProperty(NO_ENTITY_BODY);
                axis2MessageContext.setProperty(Constants.MESSAGE_TYPE, "application/octet-stream");
                axis2MessageContext.setProperty(Constants.CONTENT_TYPE, "application/txt");
            } catch (AxisFault e) {
                log.error("Error occurred while creating the response", e);
                sendFaultResponse(axis2MessageContext);
            } catch (IOException e) {
                log.error("Error occurred while reading the input stream", e);
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

    private DataHandler downloadArchivedLogFiles(String logFile) {

        InputStream is = null;
        ByteArrayDataSource bytArrayDS;
        try {
            Path logFilePath = Paths.get(Utils.getCarbonLogsPath(), logFile);
            File file = new File(logFilePath.toString());
            if (file.exists() && !file.isDirectory()) {
                is = new BufferedInputStream(new FileInputStream(logFilePath.toString()));
                bytArrayDS = new ByteArrayDataSource(is, "text/xml");
                return new DataHandler(bytArrayDS);
            } else {
                log.error("Could not find the requested file : " + logFile);
                return null;
            }
        } catch (IOException e) {
            log.error("Error occurred while reading file : " + logFile, e);
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("Error while closing inputStream of log file", e);
                }
            }
        }
    }
}
