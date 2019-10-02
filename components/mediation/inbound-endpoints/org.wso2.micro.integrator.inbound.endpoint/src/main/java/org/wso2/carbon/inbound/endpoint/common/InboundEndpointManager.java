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

package org.wso2.carbon.inbound.endpoint.common;

import org.apache.synapse.inbound.InboundProcessorParams;

public interface InboundEndpointManager {

    /**
     * Start Listener on a particular port
     *
     * @param port              port
     * @param name              endpoint name
     * @param inboundParameters Inbound endpoint parameters
     */
    public boolean startListener(int port, String name, InboundProcessorParams inboundParameters);

    /**
     * Start Inbound endpoint on a particular port
     *
     * @param port              port
     * @param name              endpoint name
     * @param inboundParameters Inbound endpoint parameters
     */
    public boolean startEndpoint(int port, String name, InboundProcessorParams inboundParameters);

    /**
     * Stop Inbound Endpoint
     *
     * @param port port of the endpoint
     */
    public void closeEndpoint(int port);

    /**
     * Get endpoint name from underlying store for particular port and tenant domain.
     *
     * @param port
     * @param domain
     */
    public String getEndpointName(int port, String domain);

}
