/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.httpwebsocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.httpwebsocket.management.HttpWebsocketEndpointManager;

public class InboundHttpWebsocketListener implements InboundRequestProcessor {

    private static final Log LOGGER = LogFactory.getLog(InboundHttpWebsocketListener.class);

    protected final String name;
    protected int port;
    protected InboundProcessorParams processorParams;

    public InboundHttpWebsocketListener(InboundProcessorParams params) {

        processorParams = params;
        String portParam = params.getProperties()
                .getProperty(InboundHttpWebSocketConstants.INBOUND_ENDPOINT_PARAMETER_HTTP_WS_PORT);
        try {
            port = Integer.parseInt(portParam);
        } catch (NumberFormatException e) {
            handleException("Validation failed for the port parameter " + portParam, e);
        }
        name = params.getName();
    }

    @Override
    public void init() {

        HttpWebsocketEndpointManager.getInstance().startEndpoint(port, name, processorParams);
    }

    @Override
    public void destroy() {

        HttpWebsocketEndpointManager.getInstance().closeEndpoint(port);
    }

    protected void handleException(String msg, Exception e) {

        LOGGER.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
