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

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.NOT_FOUND;

public class CAppUnDeployerResource extends APIResource {

    private static Log log = LogFactory.getLog(CAppUnDeployerResource.class);

    // HTTP method types supported by the resource
    private Set<String> methods;

    private static final String CAPP_NAME_PATTERN = "cAppNamePattern";

    CAppUnDeployerResource(String urlTemplate) {
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
            handleCAppUnDeployerPost(axis2MessageContext);
        } else {
            JSONObject response = Utils.createJsonError("No such method for management/capp-undeployer",
                    axis2MessageContext, NOT_FOUND);
            Utils.setJsonPayLoad(axis2MessageContext, response);
        }

        return true;
    }

    private void handleCAppUnDeployerPost(org.apache.axis2.context.MessageContext axisMsgCtx) {
        try {
            JsonObject payload = Utils.getJsonPayload(axisMsgCtx);
            JSONObject jsonResponse = new JSONObject();
            if (payload.has(CAPP_NAME_PATTERN) && !payload.get(CAPP_NAME_PATTERN).getAsString().isEmpty()) {
                String cAppNamePattern = payload.get(CAPP_NAME_PATTERN).getAsString();
                String cAppsDirectoryPath = Paths.get(
                        Utils.getCarbonHome(), "repository", "deployment", "server", "carbonapps").toString();

                // List deployed CApps which has downloaded CApp name prefix
                File carbonAppsDirectory = new File(cAppsDirectoryPath);
                File[] existingCApps = carbonAppsDirectory.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.contains(cAppNamePattern) && name.endsWith(".car");
                    }
                });

                // Remove deployed CApps which has downloaded CApp name prefix
                if (existingCApps != null && existingCApps.length != 0) {
                    for (File cApp : existingCApps) {
                        Files.delete(cApp.toPath());
                        log.info(cApp.getName() + " file deleted from " + cAppsDirectoryPath + " directory");
                    }
                    jsonResponse.put(Constants.MESSAGE_JSON_ATTRIBUTE, "Successfully removed Carbon Application(s) " +
                            "named " + cAppNamePattern);
                } else {
                    jsonResponse = Utils.createJsonError("Carbon Application(s) named or patterned " +
                            cAppNamePattern + "' does not exist", axisMsgCtx, INTERNAL_SERVER_ERROR);
                }
            } else {
                jsonResponse = Utils.createJsonError("Missing required " + CAPP_NAME_PATTERN
                        + " parameter in the payload", axisMsgCtx, BAD_REQUEST);
            }
            Utils.setJsonPayLoad(axisMsgCtx, jsonResponse);
        } catch (IOException e) {
            String message = "Error when undeploying the Carbon Application";
            log.error(message, e);
            Utils.setJsonPayLoad(axisMsgCtx, Utils.createJsonErrorObject(message));
        }
    }
}
