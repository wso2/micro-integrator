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

package org.wso2.micro.integrator.dataservices.odata.endpoint;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import javax.xml.stream.XMLStreamException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class ODataPassThroughHandler extends AbstractSynapseHandler {
    private static final Log log = LogFactory.getLog(ODataPassThroughHandler.class);
    public final static String JSON_CONTENT_TYPE = "application/json";
    public final static String XML_CONTENT_TYPE = "application/xml";


    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        try {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            Object isODataService = axis2MessageContext.getProperty("IsODataService");
            // In this if block we are skipping proxy services, inbound related message contexts & api.
            if (axis2MessageContext.getProperty("TransportInURL") != null && isODataService != null) {
                RelayUtils.buildMessage(axis2MessageContext);
                ODataServletRequest request = new ODataServletRequest(axis2MessageContext);
                ODataServletResponse response = new ODataServletResponse(axis2MessageContext);
                ODataEndpoint.process(request, response);
                setContent(axis2MessageContext, response);
                setHeaders(axis2MessageContext, response);
                messageContext.setTo(null);
                messageContext.setResponse(true);
                Axis2Sender.sendBack(messageContext);
            }
            return true;
        } catch (Exception e) {
            this.handleException("Error occurred in integrator handler.", e, messageContext);
            return true;
        }
    }

    private void setHeaders(org.apache.axis2.context.MessageContext axis2MessageContext,
                            ODataServletResponse response) throws UnsupportedEncodingException {
        axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE, response.getContentType());
        Object o = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map headers = (Map) o;
        if (headers != null) {
            headers.remove(HTTP.CONTENT_TYPE);
            headers.put(HTTP.CONTENT_TYPE, response.getContentType());
        }
        if (response.getContentAsString() != null && response.getContentAsString().length() != 0) {
            axis2MessageContext.removeProperty(PassThroughConstants.NO_ENTITY_BODY);
        }
    }

    private void setContent(org.apache.axis2.context.MessageContext axis2MessageContext, ODataServletResponse response)
            throws UnsupportedEncodingException, AxisFault, XMLStreamException {
        String content = response.getContentAsString();
        if (response.getContentType() != null && response.getContentType().contains(JSON_CONTENT_TYPE)) {
            axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, JSON_CONTENT_TYPE);
            JsonUtil.getNewJsonPayload(axis2MessageContext, content, true, true);
        } else if (response.getContentType() != null && response.getContentType().contains(XML_CONTENT_TYPE)) {
            axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, response.getContentType());
            OMElement omXML = AXIOMUtil.stringToOM(content);
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope envelope = fac.getDefaultEnvelope();
            envelope.getBody().addChild(omXML.getFirstElement());
            axis2MessageContext.setEnvelope(envelope);
        }
    }

    private void handleException(String msg, Exception e, MessageContext msgContext) {
        log.error(msg, e);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().error(msg, e);
        }
        throw new SynapseException(msg, e);
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        return true;
    }

}
