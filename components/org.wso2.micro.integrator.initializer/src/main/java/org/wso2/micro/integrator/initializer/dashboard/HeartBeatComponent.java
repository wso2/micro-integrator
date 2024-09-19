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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.inbound.endpoint.internal.http.api.ConfigurationLoader;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.core.util.StringUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
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

/**
 * Manages heartbeats from micro integrator to dashboard.
 */
public class HeartBeatComponent {

    private HeartBeatComponent(){

    }

    private static final Log log = LogFactory.getLog(HeartBeatComponent.class);
    private static final Map<String, Object> configs = ConfigParser.getParsedConfigs();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void invokeHeartbeatExecutorService() {

        String heartbeatApiUrl = configs.get(DASHBOARD_CONFIG_URL)  + "/heartbeat";
        String groupId = getGroupId();
        String nodeId = getNodeId();
        long interval = getInterval();
        String mgtApiUrl = getMgtApiUrl();

        final HttpPost httpPost = new HttpPost(heartbeatApiUrl);

        httpPost.setHeader("Accept", HEADER_VALUE_APPLICATION_JSON);
        httpPost.setHeader("Content-type", HEADER_VALUE_APPLICATION_JSON);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnableTask = () -> {
            HeartbeatData heartbeatData = new HeartbeatData(groupId, nodeId, interval, mgtApiUrl,
                    getJMXMemoryMetrics(), getLiveThreadCount(), getCPULoadStats());
            String heartbeatPayload = null;
            try {
                heartbeatPayload = objectMapper.writeValueAsString(heartbeatData);
            } catch (JsonProcessingException e) {
                log.debug("Error serializing heartbeat payload: " + e.getMessage());
            }

            try (CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(
                    new SSLConnectionSocketFactory(
                            SSLContexts.custom().loadTrustMaterial(null,
                                    (TrustStrategy) new TrustSelfSignedStrategy()).build(),
                            NoopHostnameVerifier.INSTANCE)).build()) {
                assert heartbeatPayload != null;
                final StringEntity entity = new StringEntity(heartbeatPayload, ContentType.APPLICATION_JSON);
                httpPost.setEntity(entity);
                CloseableHttpResponse response = client.execute(httpPost);
                ObjectNode jsonResponse = getJsonResponse(response);
                if (jsonResponse != null && jsonResponse.get("status").asText().equals("success")) {
                    log.debug("Heartbeat sent successfully.");
                } else {
                    log.debug("Error occurred while sending the heartbeat.");
                }
            } catch (Exception e) {
                log.debug("Error occurred while processing the heartbeat.", e);
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(runnableTask, 1, interval, TimeUnit.SECONDS);
    }

    private static String getMgtApiUrl() {
        String serviceIp = System.getProperty("carbon.local.ip");
        String httpApiPort = Integer.toString(ConfigurationLoader.getInternalInboundHttpsPort());
        String mgtApiUrl = HTTPS_PREFIX.concat(serviceIp).concat(COLON).concat(httpApiPort).concat(FORWARD_SLASH)
                                       .concat(MANAGEMENT).concat(FORWARD_SLASH);

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

    private static long[] getJMXMemoryMetrics() {
        // Array format: [heapTotal, heapUsed, heapCommitted, heapMax]
        long[] jmxStats = new long[4];
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        jmxStats[0] = heapMemoryUsage.getInit();
        jmxStats[1] = heapMemoryUsage.getUsed();
        jmxStats[2] = heapMemoryUsage.getCommitted();
        jmxStats[3] = heapMemoryUsage.getMax();
        return jmxStats;
    }

    private static int getLiveThreadCount() {
        return ManagementFactory.getThreadMXBean().getThreadCount();
    }

    private static double[] getCPULoadStats() {
        // Array format: [systemCpuLoad, processCpuLoad]
        double[] cpuUsage = new double[2];
        OperatingSystemMXBean osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        cpuUsage[0] = osMXBean.getCpuLoad();
        cpuUsage[1] = osMXBean.getProcessCpuLoad();
        return cpuUsage;
    }

    public static boolean isDashboardConfigured() {
        return configs.get(DASHBOARD_CONFIG_URL) != null;
    }

    public static ObjectNode getJsonResponse(CloseableHttpResponse response) {
        String stringResponse = getStringResponse(response);
        ObjectNode responseObject = null;
        try {
            JsonNode jsonNode = objectMapper.readTree(stringResponse);
            if (jsonNode.isObject()) {
                responseObject = (ObjectNode) jsonNode;
            }
        } catch (IOException e) {
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
