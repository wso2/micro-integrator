/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.initializer.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.inbound.endpoint.internal.http.api.ConfigurationLoader;
import org.wso2.config.mapper.ConfigParser;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages heartbeats from micro integrator to dashboard.
 */
public class HeartBeatComponent {

    private HeartBeatComponent(){

    }

    private static final Log log = LogFactory.getLog(HeartBeatComponent.class);
    private static Map<String, Object> configs = ConfigParser.getParsedConfigs();
    public static void invokeHeartbeatExecutorService() {

        String heartbeatApiUrl = configs.get("dashboard_config.dashboard_url") + File.separator + "heartbeat";
        String groupId = configs.get("dashboard_config.group_id").toString();
        String nodeId = configs.get("dashboard_config.node_id").toString();
        long interval = Integer.parseInt(configs.get("dashboard_config.heartbeat_interval").toString());
        String carbonLocalIp = System.getProperty("carbon.local.ip");
        int internalHttpApiPort = ConfigurationLoader.getInternalInboundHttpsPort();
        String mgtApiUrl = "https://" + carbonLocalIp + ":" + internalHttpApiPort + "/management/";
        final HttpPost httpPost = new HttpPost(heartbeatApiUrl);
        final String productName = "mi";

        JsonObject heartbeatPayload = new JsonObject();
        heartbeatPayload.addProperty("product", productName);
        heartbeatPayload.addProperty("groupId", groupId);
        heartbeatPayload.addProperty("nodeId", nodeId);
        heartbeatPayload.addProperty("interval", interval);
        heartbeatPayload.addProperty("mgtApiUrl", mgtApiUrl);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnableTask = () -> {
            JsonObject changeNotification = createChangeNotification();
            heartbeatPayload.add("changeNotification",changeNotification);

            final CloseableHttpClient client = HttpClients.createDefault();
            try {
                final StringEntity entity = new StringEntity(heartbeatPayload.toString());
                httpPost.setEntity(entity);
                CloseableHttpResponse response = client.execute(httpPost);
                JsonObject jsonResponse = getJsonResponse(response);
                if (jsonResponse.get("status").getAsString().equals("success")) {
                    int deployedArtifactsCount = heartbeatPayload.get("changeNotification").getAsJsonObject()
                                                                    .get("deployedArtifacts").getAsJsonArray().size();
                    int undeployedArtifactsCount = heartbeatPayload.get("changeNotification").getAsJsonObject()
                                                                    .get("undeployedArtifacts").getAsJsonArray().size();
                    ArtifactDeploymentListener.removeFromUndeployedArtifactsQueue(undeployedArtifactsCount);
                    ArtifactDeploymentListener.removeFromDeployedArtifactsQueue(deployedArtifactsCount);
                }
            } catch (IOException e) {
                log.error("Error occurred while sending heartbeat request to dashboard.");
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing the connection.", e);
                }
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(runnableTask, 1, interval, TimeUnit.SECONDS);
    }

    private static JsonObject createChangeNotification() {
        JsonObject changeNotification = new JsonObject();
        JsonArray deployedArtifacts = ArtifactDeploymentListener.getDeployedArtifacts();
        JsonArray undeployedArtifacts = ArtifactDeploymentListener.getUndeployedArtifacts();
        changeNotification.add("deployedArtifacts", deployedArtifacts);
        changeNotification.add("undeployedArtifacts", undeployedArtifacts);
        return changeNotification;
    }

    public static boolean isDashboardConfigured() {
        return configs.get("dashboard_config.dashboard_url") != null;
    }

    public static JsonObject getJsonResponse(CloseableHttpResponse response) {
        String stringResponse = getStringResponse(response);
        return new JsonParser().parse(stringResponse).getAsJsonObject();
    }

    public static String getStringResponse(CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();
        String stringResponse = "";
        try {
            stringResponse = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            log.error("Error occurred while converting entity to string.", e);
        }
        return stringResponse;
    }
}
