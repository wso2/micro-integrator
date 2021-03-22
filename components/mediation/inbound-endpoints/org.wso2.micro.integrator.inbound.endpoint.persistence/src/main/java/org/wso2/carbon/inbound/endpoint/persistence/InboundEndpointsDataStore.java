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

package org.wso2.carbon.inbound.endpoint.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InboundEndpointsDataStore {

    private static final Log log = LogFactory.getLog(InboundEndpointsDataStore.class);

    private Map<Integer, List<InboundEndpointInfoDTO>> endpointListeningInfo;

    //Store polling endpoints with <TenantId<Endpoint_Name>> format
    private Map<String, Set<String>> endpointPollingInfo;

    private static InboundEndpointsDataStore instance = new InboundEndpointsDataStore();

    public static InboundEndpointsDataStore getInstance() {
        return instance;
    }

    private InboundEndpointsDataStore() {

        endpointListeningInfo = new ConcurrentHashMap<>();
        endpointPollingInfo = new ConcurrentHashMap<>();

    }

    /**
     * Register endpoint in the InboundEndpointsDataStore
     *
     * @param port         listener port
     * @param tenantDomain tenant domain
     * @param protocol     protocol
     * @param name         endpoint name
     */
    public void registerListeningEndpoint(int port, String tenantDomain, String protocol, String name,
                                          InboundProcessorParams params) {
        port = port - PersistenceUtils.getPortOffset(params.getProperties());
        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.get(port);
        if (tenantList == null) {
            // If there is no existing listeners in the port, create a new list
            tenantList = new ArrayList<>();
            endpointListeningInfo.put(port, tenantList);
        }
        tenantList.add(new InboundEndpointInfoDTO(tenantDomain, protocol, name, params));
    }

    /**
     * Register endpoint in the InboundEndpointsDataStore
     *
     * @param tenantDomain tenant domain
     * @param name         endpoint name
     */
    public void registerPollingEndpoint(String tenantDomain, String name) {

        Set<String> lNames = endpointPollingInfo.get(tenantDomain);

        if (lNames == null) {
            lNames = new HashSet<>();
        }
        lNames.add(name);
        endpointPollingInfo.put(tenantDomain, lNames);
    }

    /**
     * Register SSL endpoint in the InboundEndpointsDataStore
     *
     * @param port         listener port
     * @param tenantDomain tenant domain
     * @param protocol     protocol
     * @param name         endpoint name
     */
    public void registerSSLListeningEndpoint(int port, String tenantDomain, String protocol, String name,
                                             SSLConfiguration sslConfiguration, InboundProcessorParams params) {

        port = port - PersistenceUtils.getPortOffset(params.getProperties());
        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.computeIfAbsent(port, k -> new ArrayList<>());
        // If there is no existing listeners in the port, create a new list
        InboundEndpointInfoDTO inboundEndpointInfoDTO = new InboundEndpointInfoDTO(tenantDomain, protocol, name,
                                                                                   params);
        inboundEndpointInfoDTO.setSslConfiguration(sslConfiguration);
        tenantList.add(inboundEndpointInfoDTO);
    }

    /**
     * Get endpoint name for given port and domain
     *
     * @param port         port
     * @param tenantDomain tenant domain
     * @return endpoint name
     */
    public String getListeningEndpointName(int port, String tenantDomain) {
        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.get(port);
        if (tenantList != null) {
            for (InboundEndpointInfoDTO tenantInfo : tenantList) {
                if (tenantInfo.getTenantDomain().equals(tenantDomain)) {
                    return tenantInfo.getEndpointName();
                }
            }
        }
        return null;
    }

    /**
     * Unregister an endpoint from data store
     *
     * @param port         port
     * @param tenantDomain tenant domain name
     */
    public void unregisterListeningEndpoint(int port, String tenantDomain) {
        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.get(port);
        if (tenantList != null) {
            for (InboundEndpointInfoDTO tenantInfo : tenantList) {
                if (tenantInfo.getTenantDomain().equals(tenantDomain)) {
                    tenantList.remove(tenantInfo);
                    break;
                }
            }
        }
        if (endpointListeningInfo.get(port) != null && endpointListeningInfo.get(port).size() == 0) {
            endpointListeningInfo.remove(port);
        }
    }

    /**
     * Unregister an endpoint from data store
     *
     * @param tenantDomain
     * @param name
     */
    public void unregisterPollingEndpoint(String tenantDomain, String name) {
        Set<String> lNames = endpointPollingInfo.get(tenantDomain);
        if (lNames != null && !lNames.isEmpty()) {
            for (String strName : lNames) {
                if (strName.equals(name)) {
                    lNames.remove(strName);
                    break;
                }
            }
            if (lNames.isEmpty()) {
                endpointPollingInfo.remove(tenantDomain);
            }
        }
    }

    /**
     * Check polling endpoint from data store
     *
     * @param tenantDomain
     * @param name
     */
    public boolean isPollingEndpointRegistered(String tenantDomain, String name) {
        Set<String> lNames = endpointPollingInfo.get(tenantDomain);
        if (lNames != null && !lNames.isEmpty()) {
            for (String strName : lNames) {
                if (strName.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether endpoint registry is empty for a particular port
     *
     * @param port port
     * @return whether no endpoint is registered for a port
     */
    public boolean isEndpointRegistryEmpty(int port) {
        return endpointListeningInfo.get(port) == null;
    }

    /**
     * Get details of all endpoints
     *
     * @return information of all endpoints
     */
    public Map<Integer, List<InboundEndpointInfoDTO>> getAllListeningEndpointData() {
        return endpointListeningInfo;
    }

    /**
     * Get details of all polling endpoints
     *
     * @return information of all polling endpoints
     */
    public Map<String, Set<String>> getAllPollingingEndpointData() {
        return endpointPollingInfo;
    }

}
