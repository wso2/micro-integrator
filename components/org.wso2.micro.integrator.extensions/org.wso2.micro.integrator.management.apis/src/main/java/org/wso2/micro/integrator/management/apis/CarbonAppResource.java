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

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.micro.application.deployer.AppDeployerUtils;
import org.wso2.micro.application.deployer.CarbonApplication;
import org.wso2.micro.application.deployer.config.Artifact;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import  org.wso2.micro.integrator.initializer.deployment.application.deployer.CAppDeploymentManager;

public class CarbonAppResource extends APIResource {

    public CarbonAppResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String param = Utils.getQueryParameter(messageContext, "carbonAppName");

        if (Objects.nonNull(param)) {
            populateCarbonAppData(messageContext, param);
        } else {
            populateCarbonAppList(messageContext);
        }

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateCarbonAppList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<CarbonApplication> appList
                = CAppDeploymentManager.getCarbonApps();

        JSONObject jsonBody = Utils.createJSONList(appList.size());

        for (CarbonApplication app: appList) {

            JSONObject appObject = new JSONObject();

            appObject.put(Constants.NAME, app.getAppName());
            appObject.put(Constants.VERSION, app.getAppVersion());

            jsonBody.getJSONArray(Constants.LIST).put(appObject);
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
                = CAppDeploymentManager.getCarbonApps();

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
}
