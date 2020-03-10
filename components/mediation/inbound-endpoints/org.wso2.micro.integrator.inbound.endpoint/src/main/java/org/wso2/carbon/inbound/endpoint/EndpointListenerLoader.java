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
package org.wso2.carbon.inbound.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.inboundfactory.InboundRequestProcessorFactoryImpl;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointInfoDTO;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointsDataStore;
import org.wso2.carbon.inbound.endpoint.persistence.PersistenceUtils;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericInboundListener;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpConstants;
import org.wso2.carbon.inbound.endpoint.protocol.http.management.HTTPEndpointManager;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.management.WebsocketEndpointManager;
import org.wso2.carbon.inbound.endpoint.internal.http.api.ConfigurationLoader;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.management.HL7EndpointManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for starting Listeners( like HTTP, HTTPS, HL7) on server startup for
 * Listening Inbound Endpoints.
 */
public class EndpointListenerLoader {

    private static Log log = LogFactory.getLog(EndpointListenerLoader.class);

    /**
     * Start listeners for all the Listening Inbound Endpoints. This should be called in the
     * server startup to load all the required listeners for endpoints in all tenants
     * <p>
     * Inbound Endpoint Persistence service need to be up and running before calling this method.
     * So the ServiceBusInitializer activate() method is the ideal place as it
     * guarantee that Inbound Endpoint Persistence Service is activated.
     * We cannot make this a osgi service as this is a fragment of synapse-core.
     * So to make sure persistence service is available, we need to depend on some other technique
     * like the one described above
     */
    public static void loadListeners() {

        Map<Integer, List<InboundEndpointInfoDTO>> tenantData = InboundEndpointsDataStore.getInstance()
                .getAllListeningEndpointData();

        for (Map.Entry tenantInfoEntry : tenantData.entrySet()) {

            InboundEndpointInfoDTO inboundEndpointInfoDTO = (InboundEndpointInfoDTO) ((ArrayList) tenantInfoEntry
                    .getValue()).get(0);
            int port = (Integer) tenantInfoEntry.getKey() + PersistenceUtils
                    .getPortOffset(inboundEndpointInfoDTO.getInboundParams().getProperties());

            if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTP)) {
                HTTPEndpointManager.getInstance().
                        startListener(port, inboundEndpointInfoDTO.getEndpointName(),
                                      inboundEndpointInfoDTO.getInboundParams());
            } else if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTPS)) {
                HTTPEndpointManager.getInstance().
                        startSSLListener(port, inboundEndpointInfoDTO.getEndpointName(),
                                         inboundEndpointInfoDTO.getSslConfiguration(),
                                         inboundEndpointInfoDTO.getInboundParams());
            } else if (inboundEndpointInfoDTO.getProtocol()
                    .equals(InboundRequestProcessorFactoryImpl.Protocols.ws.toString())) {
                WebsocketEndpointManager.getInstance().
                        startListener(port, inboundEndpointInfoDTO.getEndpointName(),
                                      inboundEndpointInfoDTO.getInboundParams());
            } else if (inboundEndpointInfoDTO.getProtocol()
                    .equals(InboundRequestProcessorFactoryImpl.Protocols.wss.toString())) {
                WebsocketEndpointManager.getInstance().
                        startSSLListener(port, inboundEndpointInfoDTO.getEndpointName(),
                                         inboundEndpointInfoDTO.getInboundParams());
            } else if (inboundEndpointInfoDTO.getProtocol()
                    .equals(InboundRequestProcessorFactoryImpl.Protocols.hl7.toString())) {
                HL7EndpointManager.getInstance().
                        startListener(port, inboundEndpointInfoDTO.getEndpointName(),
                                      inboundEndpointInfoDTO.getInboundParams());
            } else {
                // Check for custom-listening-InboundEndpoints
                InboundProcessorParams inboundParams = inboundEndpointInfoDTO.getInboundParams();

                if (GenericInboundListener.isListeningInboundEndpoint(inboundParams)) {
                    GenericInboundListener.getInstance(inboundParams).init();
                }
            }
        }

        loadInternalInboundApis();
    }

    /**
     * This loads both internal http and https apis.
     */
    private static void loadInternalInboundApis() {

        HTTPEndpointManager manager = HTTPEndpointManager.getInstance();

        if (manager.isAnyInternalHttpApiEnabled()) {
            HTTPEndpointManager.getInstance().startListener(manager.getInternalInboundHttpPort(),
                                                            InboundHttpConstants.INTERNAL_HTTP_INBOUND_ENDPOINT_NAME,
                                                            null);
        }

        if (manager.isAnyInternalHttpsApiEnabled()) {
            if (ConfigurationLoader.isSslConfiguredSuccessfully()) {
                manager.startSSLListener(manager.getInternalInboundHttpsPort(),
                                         InboundHttpConstants.INTERNAL_HTTPS_INBOUND_ENDPOINT_NAME,
                                         ConfigurationLoader.getSslConfiguration(), null);
            } else {
                log.error("SSL is not configured for Internal apis. Hence Internal Apis will not be available via "
                                  + "https.");
            }
        }
    }

}
