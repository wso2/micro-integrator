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
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * MQTT connection Listener bounded per MQTT client, this the listener registered per client
 * when mqtt connection is being made. Delegates logic per successful and failed connection scenarios
 */
public class MqttConnectionListener {

    private static final Log log = LogFactory.getLog(MqttConnectionListener.class);
    private MqttConnectionConsumer mqttConnectionConsumer;
    private boolean execute = true;
    private static final int DEFAULT_RECONNECTION_INTERVAL = 10000;

    public MqttConnectionListener(MqttConnectionConsumer mqttConnectionConsumer) {
        this.mqttConnectionConsumer = mqttConnectionConsumer;
    }

    public void onFailure() {
        try {

            int retryInterval = mqttConnectionConsumer.getMqttConnectionFactory().
                    getReconnectionInterval();
            boolean isConnected = false;
            int retryCount = 1;
            while (execute && !isConnected) {
                if (retryInterval != -1) {
                    Thread.sleep(retryInterval);
                } else {
                    Thread.sleep(DEFAULT_RECONNECTION_INTERVAL);
                }
                try {
                    IMqttToken connectionToken = mqttConnectionConsumer.getMqttAsyncClient()
                            .connect(mqttConnectionConsumer.getConnectOptions());
                    connectionToken.waitForCompletion();
                    if (mqttConnectionConsumer.getMqttAsyncClient().isConnected()) {
                        isConnected = true;
                        log.info("Successfully reconnected MQTT inbound endpoint: " + mqttConnectionConsumer.getName());
                    }
                } catch (MqttException ex) {
                    log.error("MQTT inbound endpoint " + mqttConnectionConsumer.getName()
                                      + " error while reconnecting to the broker attempt " + retryCount++);
                }

            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Error while trying to subscribe to the remote", ex);
        }
    }

    public void shutdown() {
        this.execute = false;
    }
}
