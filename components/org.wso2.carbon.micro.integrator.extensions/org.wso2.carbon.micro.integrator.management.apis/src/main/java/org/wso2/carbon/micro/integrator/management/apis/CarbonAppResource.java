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


package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.micro.integrator.core.deployment.application.deployer.CAppDeploymentManager.getCarbonApps;

public class CarbonAppResource extends APIResource {

    private Utils utils = new Utils();

    public CarbonAppResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
//        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = utils.getQueryParameters(axis2MessageContext);

        // if query params exists retrieve data about specific inbound endpoint
        if (null != queryParameter) {
            for (NameValuePair nvPair : queryParameter) {
                if (nvPair.getName().equals("carbonAppName")) {
                    populateCarbonAppData(messageContext, nvPair.getValue());
                }
            }
        } else {
            populateCarbonAppList(messageContext);
        }

        axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        return true;
    }

    private void populateCarbonAppList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        ArrayList<CarbonApplication> appList
                = getCarbonApps(String.valueOf(AppDeployerUtils.getTenantId()));

        JSONObject jsonBody = new JSONObject();
        JSONArray cappList = new JSONArray();
        jsonBody.put("count", appList.size());
        jsonBody.put("list", cappList);

        for (CarbonApplication app: appList) {

            JSONObject appObject = new JSONObject();

            appObject.put("name", app.getAppName());
            appObject.put("version", app.getAppVersion());

            cappList.put(appObject);
        }
        utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateCarbonAppData(MessageContext messageContext, String carbonAppName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getCarbonAppByName(carbonAppName);

        if (null != jsonBody) {
            utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private JSONObject getCarbonAppByName(String carbonAppName) {

        ArrayList<CarbonApplication> appList
                = getCarbonApps(String.valueOf(AppDeployerUtils.getTenantId()));

        for (CarbonApplication app: appList) {
            if (app.getAppName().equals(carbonAppName)) {
                return convertCarbonAppToOMElement(app);
            }
        }
        return null;
    }

    private JSONObject convertCarbonAppToOMElement(CarbonApplication carbonApp) {

        if (null == carbonApp) {
            return null;
        }

        JSONObject appObject = new JSONObject();

        appObject.put("name", carbonApp.getAppName());
        appObject.put("version", carbonApp.getAppVersion());

        JSONArray artifactListObject = new JSONArray();
        appObject.put("artifacts", artifactListObject);

        List<Artifact.Dependency> dependencies = carbonApp.getAppConfig().
                getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dependency : dependencies) {

            Artifact artifact = dependency.getArtifact();

            String type = artifact.getType().split("/")[1];
            String artifactName = artifact.getName();

            // if the artifactName is null, artifact deployment has failed..
            if (null == artifactName) {
                continue;
            }

            JSONObject artifactObject = new JSONObject();

            artifactObject.put("name", artifactName);
            artifactObject.put("type", type);

            artifactListObject.put(artifactObject);
        }
        return appObject;
    }
}
