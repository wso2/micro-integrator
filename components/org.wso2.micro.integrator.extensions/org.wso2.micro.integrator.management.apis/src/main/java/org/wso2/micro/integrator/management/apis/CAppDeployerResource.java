/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.NOT_FOUND;

public class CAppDeployerResource extends APIResource {

    private static Log log = LogFactory.getLog(CAppDeployerResource.class);

    // HTTP method types supported by the resource
    private Set<String> methods;
    private static final String MULTIPART_FORMDATA_DATA_TYPE = "multipart/form-data";

    CAppDeployerResource(String urlTemplate) {
        super(urlTemplate);
        methods = new HashSet<>();
        methods.add(Constants.HTTP_POST);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    public boolean invoke(MessageContext messageContext) {
        buildMessage(messageContext);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        if (Utils.isDoingPOST(axis2MessageContext)) {
            handleCAppDeployerPost(axis2MessageContext);
        } else {
            JSONObject response = Utils.createJsonError("No such method for management/capp-deployer",
                    axis2MessageContext, NOT_FOUND);
            Utils.setJsonPayLoad(axis2MessageContext, response);
        }
        return true;
    }

    private void handleCAppDeployerPost(org.apache.axis2.context.MessageContext axisMsgCtx) {
        JSONObject jsonResponse = new JSONObject();
        String contentType = axisMsgCtx.getProperty(Constants.CONTENT_TYPE).toString();
        if (!contentType.contains(MULTIPART_FORMDATA_DATA_TYPE)) {
            JSONObject response = Utils.createJsonError("Supports only for the Content-Type : "
                    + MULTIPART_FORMDATA_DATA_TYPE, axisMsgCtx, BAD_REQUEST);
            Utils.setJsonPayLoad(axisMsgCtx, response);
            return;
        }

        StringBuilder unMovedCApps = new StringBuilder();
        String errorMessage = "Error when deploying the Carbon Application : ";
        OMElement messageBody = axisMsgCtx.getEnvelope().getBody().getFirstElement();
        Iterator iterator = messageBody.getChildElements();
        boolean isDeployedSuccesfully = true;
        while (iterator.hasNext()) {
            OMElement fileElement = (OMElement) iterator.next();
            String fileName = fileElement.getAttributeValue(new QName("filename"));
            if (fileName != null && fileName.endsWith(".car")) {
                byte[] bytes = Base64.getDecoder().decode(fileElement.getText());
                Path cAppDirectoryPath = Paths.get(Utils.getCarbonHome(), "repository", "deployment",
                        "server", "carbonapps", fileName);
                try {
                    Files.write(cAppDirectoryPath, bytes);
                } catch (IOException e) {
                    isDeployedSuccesfully = false;
                    unMovedCApps.append(fileName).append(", ");
                    log.error(errorMessage + fileName, e);
                }
                log.info("Successfully added Carbon Application : " + fileName);
            } else {
                isDeployedSuccesfully = false;
                unMovedCApps.append((fileName != null ? fileName : "filename: <null>")).append(", ");
            }
        }

        if (isDeployedSuccesfully) {
            jsonResponse.put(Constants.MESSAGE_JSON_ATTRIBUTE, "Successfully added Carbon Application(s)");
            Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
        } else {
            Utils.setJsonPayLoad(axisMsgCtx, Utils.createJsonErrorObject(errorMessage + unMovedCApps));
        }
    }
}
