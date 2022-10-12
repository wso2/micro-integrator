/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.dataservices.odata.endpoint;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to invoke a message from the server to the client with streaming capabilities.
 */
public class ODataAxisEngine extends AxisEngine {
    private static final Log LOG = LogFactory.getLog(ODataAxisEngine.class);
    private static ODataTransportSender sender;

    /**
     * This method invokes a streaming response from the server to the client.
     *
     * @param messageContext Message context.
     * @param response       OData Servlet response.
     * @throws AxisFault if any SOAP fault occurred while building or/and sending the message.
     */
    public void stream(MessageContext messageContext, ODataServletResponse response) throws AxisFault {
        if (LoggingControl.debugLoggingAllowed && LOG.isTraceEnabled()) {
            LOG.trace(messageContext.getLogIDString() + " send:" + messageContext.getMessageID());
        }
        if (sender == null) {
            sender = new ODataTransportSender(messageContext.getConfigurationContext(),
                                              messageContext.getTransportOut(), response);
        } else {
            sender.setResponse(response);
        }
        sender.invoke(messageContext);
        flowComplete(messageContext);
    }
}
