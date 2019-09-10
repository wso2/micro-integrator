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

package org.wso2.micro.integrator.websocket.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.apache.axis2.transport.base.ProtocolEndpoint;

public class WebsocketTransportListener extends AbstractTransportListenerEx<ProtocolEndpoint> {

    private final static String exception = "Websocket Transport Listener does not available as Axis2 Transport "
            + "Listener. Only available as Inbound Transport Listener";

    @Override
    protected void doInit() throws AxisFault {
        throw new AxisFault(exception);
    }

    @Override
    protected ProtocolEndpoint createEndpoint() {
        return null;
    }

    @Override
    protected void startEndpoint(ProtocolEndpoint endpoint) throws AxisFault {
        throw new AxisFault(exception);
    }

    @Override
    protected void stopEndpoint(ProtocolEndpoint endpoint) {
        log.warn(exception);
    }
}
