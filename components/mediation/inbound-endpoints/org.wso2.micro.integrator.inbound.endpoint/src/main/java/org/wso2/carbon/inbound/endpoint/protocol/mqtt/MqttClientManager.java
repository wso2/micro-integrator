/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.protocol.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Reference Holder for MQTT clients in existence per ESB Server instance
 */
public class MqttClientManager {

    private static MqttClientManager instance;
    private static final Log log = LogFactory.getLog(MqttClientManager.class);
    private ConcurrentHashMap<String, MqttAsyncClient> mqttClientMap;
    private ConcurrentHashMap<String, MqttAsyncCallback> mqttCallbackMap;
    //keep a flag to indicate the cases where manually load the tenant
    private ConcurrentHashMap<String, Boolean> tenantLoadingFlagMap;
    private ConcurrentHashMap<String, String> inboundNameToIdentifierMap;
    private ConcurrentHashMap<String, MqttDefaultFilePersistence> mqttClientDataStoreMap;

    private MqttClientManager() {
        mqttClientMap = new ConcurrentHashMap<>();
        mqttCallbackMap = new ConcurrentHashMap<>();
        tenantLoadingFlagMap = new ConcurrentHashMap<>();
        inboundNameToIdentifierMap = new ConcurrentHashMap<>();
        mqttClientDataStoreMap = new ConcurrentHashMap<>();
    }

    public static synchronized MqttClientManager getInstance() {
        if (instance == null) {
            log.info("Initializing.. MQTT Client Manager");
            instance = new MqttClientManager();
        }
        return instance;
    }

    public void registerMqttClient(String identifier, MqttAsyncClient mqttClient) {
        mqttClientMap.put(identifier, mqttClient);
    }

    public void unregisterMqttClient(String identifier, String name) {
        mqttClientMap.remove(identifier);
        mqttCallbackMap.remove(identifier);
        tenantLoadingFlagMap.remove(identifier);
        inboundNameToIdentifierMap.remove(name);
        mqttClientDataStoreMap.remove(identifier);
    }

    public boolean hasMqttClient(String identifier) {
        return mqttClientMap.containsKey(identifier);
    }

    public MqttAsyncClient getMqttClient(String identifier) {
        if (tenantLoadingFlagMap.containsKey(identifier)) {
            //this is manually tenant loading case should return the client
            return mqttClientMap.get(identifier);
        } else {
            MqttAsyncCallback callback = mqttCallbackMap.get(identifier);
            //this is the case where recreation of same bounded inbound endpoint for server host
            //server port, client id
            String msg = "Client ID: " + callback.getMqttConnectionConsumer().getMqttAsyncClient().getClientId()
                    + " Server Host: " + callback.getMqttConnectionConsumer().getMqttConnectionFactory().getServerHost()
                    + " Server Port: " + callback.getMqttConnectionConsumer().getMqttConnectionFactory().getServerPort()
                    + " is bound to existing MQTT Inbound Endpoint.";
            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    public void registerMqttCallback(String identifier, MqttAsyncCallback mqttCallback) {
        mqttCallbackMap.put(identifier, mqttCallback);
    }

    public boolean hasMqttCallback(String identifier) {
        return mqttCallbackMap.containsKey(identifier);
    }

    public MqttAsyncCallback getMqttCallback(String identifier) {
        return mqttCallbackMap.get(identifier);
    }

    public void registerInboundTenantLoadingFlag(String identifier) {
        tenantLoadingFlagMap.put(identifier, true);
    }

    public void unRegisterInboundTenantLoadingFlag(String identifier) {
        tenantLoadingFlagMap.remove(identifier);
    }

    public boolean isInboundTenantLoadingFlagSet(String identifier) {
        return tenantLoadingFlagMap.containsKey(identifier);
    }

    public String buildIdentifier(String clientId, String host, String port) {
        //this identifier is unique per Mqtt connection
        return clientId + "." + host + "." + port;
    }

    public void registerInboundEndpoint(String name, String identifier) {
        inboundNameToIdentifierMap.put(name, identifier);
    }

    public String getInboundEndpointIdentifier(String name) {
        return inboundNameToIdentifierMap.get(name);
    }

    public boolean hasInboundEndpoint(String name) {
        return inboundNameToIdentifierMap.containsKey(name);
    }

    public String buildNameIdentifier(String name, String tenantId) {
        //this identifier is unique per inbound deployment existing among multiple tenants
        return name + "." + tenantId;
    }

    public void registerClientDataStore(String identifier, MqttDefaultFilePersistence dataStore) {
        mqttClientDataStoreMap.put(identifier, dataStore);
    }

    public boolean hasClientDataStore(String identifier) {
        return mqttClientDataStoreMap.containsKey(identifier);
    }

    public MqttDefaultFilePersistence getMqttClientDataStore(String identifier) {
        return mqttClientDataStoreMap.get(identifier);
    }

}
