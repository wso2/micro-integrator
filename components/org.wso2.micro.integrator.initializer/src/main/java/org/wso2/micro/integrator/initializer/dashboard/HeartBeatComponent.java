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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.inbound.endpoint.internal.http.api.ConfigurationLoader;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.core.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.wso2.micro.integrator.initializer.dashboard.Constants.COLON;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.DASHBOARD_CONFIG_GROUP_ID;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.DASHBOARD_CONFIG_HEARTBEAT_INTERVAL;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.DASHBOARD_CONFIG_NODE_ID;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.DASHBOARD_CONFIG_URL;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.DEFAULT_GROUP_ID;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.FORWARD_SLASH;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.HEADER_VALUE_APPLICATION_JSON;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.HTTPS_PREFIX;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.MANAGEMENT;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.NODE_ID_SYSTEM_PROPERTY;
import static org.wso2.micro.integrator.initializer.dashboard.Constants.PRODUCT_MI;

/**
 * Manages heartbeats from micro integrator to dashboard.
 */
public class HeartBeatComponent {

    private HeartBeatComponent(){

    }

    private static final Log log = LogFactory.getLog(HeartBeatComponent.class);
    private static final Map<String, Object> configs = ConfigParser.getParsedConfigs();
    public static final String MGT_API_VERSION = "management.api.version";
    private static final String CHANGE_NOTIFICATION = "changeNotification";
    private static final String DEPLOYED_ARTIFACTS = "deployedArtifacts";
    private static final String UNDEPLOYED_ARTIFACTS = "undeployedArtifacts";
    private static final String STATE_CHANGED_ARTIFACTS = "stateChangedArtifacts";
    public static void invokeHeartbeatExecutorService() {

        String heartbeatApiUrl = configs.get(DASHBOARD_CONFIG_URL)  + "/heartbeat";
        String groupId = getGroupId();
        String nodeId = getNodeId();
        long interval = getInterval();
        String mgtApiUrl = getMgtApiUrl();

        final HttpPost httpPost = new HttpPost(heartbeatApiUrl);

        JsonObject heartbeatPayload = new JsonObject();
        heartbeatPayload.addProperty("product", PRODUCT_MI);
        heartbeatPayload.addProperty("groupId", groupId);
        heartbeatPayload.addProperty("nodeId", nodeId);
        heartbeatPayload.addProperty("interval", interval);
        heartbeatPayload.addProperty("mgtApiUrl", mgtApiUrl);

        httpPost.setHeader("Accept", HEADER_VALUE_APPLICATION_JSON);
        httpPost.setHeader("Content-type", HEADER_VALUE_APPLICATION_JSON);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnableTask = () -> {
            JsonObject changeNotification = createChangeNotification();
            heartbeatPayload.add(CHANGE_NOTIFICATION, changeNotification);

            try (CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(
                    new SSLConnectionSocketFactory(
                            SSLContexts.custom().loadTrustMaterial(null,
                                    (TrustStrategy) new TrustSelfSignedStrategy()).build(),
                            NoopHostnameVerifier.INSTANCE)).build()) {
                final StringEntity entity = new StringEntity(heartbeatPayload.toString());
                httpPost.setEntity(entity);
                CloseableHttpResponse response = client.execute(httpPost);
                JsonObject jsonResponse = getJsonResponse(response);
                if (jsonResponse != null && jsonResponse.get("status").getAsString().equals("success")) {
                    int deployedArtifactsCount = heartbeatPayload.get(CHANGE_NOTIFICATION).getAsJsonObject()
                                                                    .get(DEPLOYED_ARTIFACTS).getAsJsonArray().size();
                    int undeployedArtifactsCount = heartbeatPayload.get(CHANGE_NOTIFICATION).getAsJsonObject()
                                                                    .get(UNDEPLOYED_ARTIFACTS).getAsJsonArray().size();
                    int updatedArtifactsCount = heartbeatPayload.get(CHANGE_NOTIFICATION).getAsJsonObject()
                                                                .get(STATE_CHANGED_ARTIFACTS).getAsJsonArray().size();
                    ArtifactDeploymentListener.removeFromUndeployedArtifactsQueue(undeployedArtifactsCount);
                    ArtifactDeploymentListener.removeFromDeployedArtifactsQueue(deployedArtifactsCount);
                    ArtifactUpdateListener.removeFromUpdatedArtifactQueue(updatedArtifactsCount);
                }
            } catch (Exception e) {
                log.debug("Error occurred while processing the heartbeat.", e);
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(runnableTask, 1, interval, TimeUnit.SECONDS);
    }

    private static String getMgtApiUrl() {
        String serviceIp = System.getProperty("carbon.local.ip");
        // get the management API version from the system property
        String mgtAPIVersion = System.getProperty(MGT_API_VERSION);
        String httpApiPort = Integer.toString(ConfigurationLoader.getInternalInboundHttpsPort());
        String mgtApiUrl = HTTPS_PREFIX.concat(serviceIp).concat(COLON).concat(httpApiPort).concat(FORWARD_SLASH)
                                       .concat(MANAGEMENT).concat(FORWARD_SLASH);
        if (!StringUtils.isEmpty(mgtAPIVersion)) {
            mgtApiUrl = mgtApiUrl.concat(mgtAPIVersion).concat(FORWARD_SLASH);
        }

        Object mgtApiServiceName = configs.get(Constants.DASHBOARD_CONFIG_MANAGEMENT_HOSTNAME);
        if (null != mgtApiServiceName) {
            serviceIp = mgtApiServiceName.toString();
            Object configuredMgtPort = configs.get(Constants.DASHBOARD_CONFIG_MANAGEMENT_PORT);
            if (null != configuredMgtPort) {
                String servicePort = configuredMgtPort.toString();
                mgtApiUrl = HTTPS_PREFIX.concat(serviceIp).concat(COLON).concat(servicePort).concat(FORWARD_SLASH)
                                        .concat(MANAGEMENT).concat(FORWARD_SLASH);
            } else {
                mgtApiUrl = HTTPS_PREFIX.concat(serviceIp).concat(FORWARD_SLASH).concat(MANAGEMENT)
                                        .concat(FORWARD_SLASH);
            }
        }
        return mgtApiUrl;
    }

    private static String getGroupId() {
        String groupId;
        Object id = configs.get(DASHBOARD_CONFIG_GROUP_ID);
        if (null != id) {
            groupId = id.toString();
        } else {
            groupId = DEFAULT_GROUP_ID;
        }
        return groupId;
    }

    private static String getNodeId() {
        String nodeId = System.getProperty(NODE_ID_SYSTEM_PROPERTY);
        if (StringUtils.isEmpty(nodeId)) {
            Object id = configs.get(DASHBOARD_CONFIG_NODE_ID);
            if (null != id) {
                nodeId = id.toString();
            } else {
                nodeId = generateRandomId();
            }
        }
        return nodeId;
    }

    private static long getInterval() {
        long interval = Constants.DEFAULT_HEARTBEAT_INTERVAL;
        Object configuredInterval = configs.get(DASHBOARD_CONFIG_HEARTBEAT_INTERVAL);
        if (null != configuredInterval) {
            interval = Integer.parseInt(configuredInterval.toString());
        }
        return interval;
    }

    private static String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    private static JsonObject createChangeNotification() {
        JsonObject changeNotification = new JsonObject();
        JsonArray deployedArtifacts = ArtifactDeploymentListener.getDeployedArtifacts();
        JsonArray undeployedArtifacts = ArtifactDeploymentListener.getUndeployedArtifacts();
        JsonArray stateChangedArtifacts = ArtifactUpdateListener.getStateChangedArtifacts();
        changeNotification.add(DEPLOYED_ARTIFACTS, deployedArtifacts);
        changeNotification.add(UNDEPLOYED_ARTIFACTS, undeployedArtifacts);
        changeNotification.add(STATE_CHANGED_ARTIFACTS, stateChangedArtifacts);
        return changeNotification;
    }

    public static boolean isDashboardConfigured() {
        return configs.get(DASHBOARD_CONFIG_URL) != null;
    }

    public static JsonObject getJsonResponse(CloseableHttpResponse response) {
        String stringResponse = getStringResponse(response);
        JsonObject responseObject = null;
        try {
            responseObject = new JsonParser().parse(stringResponse).getAsJsonObject();
        } catch (JsonParseException e) {
            log.debug("Error occurred while parsing the heartbeat response.", e);
        }
        return responseObject;
    }

    public static String getStringResponse(CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();
        String stringResponse = "";
        try {
            stringResponse = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            log.debug("Error occurred while converting entity to string.", e);
        }
        return stringResponse;
    }
}
