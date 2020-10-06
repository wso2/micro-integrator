/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.business.messaging.hl7.transport;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7ProcessingContext;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class HL7TransportSender extends AbstractTransportSender {

    private Parser xmlparser;

    public HL7TransportSender() {
        xmlparser = new DefaultXMLParser();
        xmlparser.setValidationContext(new NoValidation());
    }

    @Override
    public void sendMessage(MessageContext messageContext, String targetEPR, OutTransportInfo outTransportInfo)
            throws AxisFault {

        if (targetEPR != null) {
            // Forward the message to the given EPR
            sendUsingEPR(messageContext, targetEPR);
        } else {
            // Send the application ack message back to the client
            sendApplicationACKResponse(messageContext, outTransportInfo);
        }
    }

    private void processResponse(Message returnMsg, MessageContext messageContext) throws AxisFault {
        try {
            MessageContext rmc = createResponseMessageContext(messageContext);
            SOAPEnvelope soapEnvelope = createEnvelope(returnMsg);
            rmc.setEnvelope(soapEnvelope);
            AxisEngine.receive(rmc);
        } catch (Exception e) {
            handleException("Error while processing the response HL7 message", e);
        }
    }

    private Connection getConnection(String targetEPR, ConnectionHub hub) throws AxisFault {
        try {
            URI url = new URI(targetEPR);
            String targetHost = url.getHost();
            int targetPort = url.getPort();
            return hub.attach(targetHost, targetPort, new PipeParser(), MinLowerLayerProtocol.class);
        } catch (URISyntaxException e) {
            handleException("Malformed HL7 URI syntax: " + targetEPR, e);
        } catch (HL7Exception e) {
            handleException("Error while obtaining HL7 connection to: " + targetEPR, e);
        }
        return null;
    }

    private Map<String, String> getURLParameters(String url) throws AxisFault {
        try {
            Map<String, String> params = new HashMap<String, String>();
            URI hl7Url = new URI(url);
            String query = hl7Url.getQuery();
            if (query != null) {
                String[] paramStrings = query.split("&");
                for (String p : paramStrings) {
                    int index = p.indexOf('=');
                    params.put(p.substring(0, index), p.substring(index + 1));
                }
            }
            return params;
        } catch (URISyntaxException e) {
            handleException("Malformed HL7 url", e);
        }
        return null;
    }

    private SOAPEnvelope createEnvelope(Message message) throws HL7Exception, XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMElement messageEl = HL7Utils.generateHL7MessageElement(this.xmlparser.encode(message));
        envelope.getBody().addChild(messageEl);
        return envelope;
    }

    private void sendUsingEPR(MessageContext messageContext, String targetEPR) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Send HL7 message using EPR :" + targetEPR);
        }

        OMElement omElement = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();
        String xmlFormat = omElement.toString();
        Message returnMsg = null;

        Map<String, String> params = getURLParameters(targetEPR);

        try {
            Message message;
            Boolean isValid = (Boolean) messageContext.getProperty(HL7Constants.HL7_VALIDATION_PASSED);
            if ((isValid != null && !isValid) && messageContext.getProperty(
                    HL7Constants.HL7_PASS_THROUGH_INVALID_MESSAGES).equals(true)) {
                // pass through invalid messages
                message = (Message) messageContext.getProperty(HL7Constants.HL7_MESSAGE_OBJECT);
            } else {
                message = xmlparser.parse(xmlFormat);
            }

            ConnectionHub connectionHub = ConnectionHub.getInstance();
            Connection connection = getConnection(targetEPR, connectionHub);
            Initiator initiator = connection.getInitiator();
            String timeout = params.get(HL7Constants.TIMEOUT_PARAM);
            if (timeout != null) {
                initiator.setTimeoutMillis(Integer.parseInt(timeout));
            } else {
                initiator.setTimeoutMillis(HL7Constants.DEFAULT_TIMEOUT);
            }

            returnMsg = initiator.sendAndReceive(message);
            connectionHub.detach(connection);

            if (log.isDebugEnabled()) {
                log.debug("HL7 message successfully dispatched to URL " + targetEPR);
                log.debug("Response message received from target EP : " + returnMsg.toString());
            }

        } catch (Exception e) {
            handleException("Error while sending an HL7 message", e);
        }

        if (returnMsg != null) {
            processResponse(returnMsg, messageContext);
        } else {
            handleException("A response not received from the target HL7 endpoint");
        }
    }

    /**
     * Send application ack message from the incoming response  to the client
     *
     * @param messageContext
     * @param outTransportInfo
     * @throws AxisFault
     */
    private void sendApplicationACKResponse(MessageContext messageContext, OutTransportInfo outTransportInfo)
            throws AxisFault {
        HL7ProcessingContext processingContext = ((HL7TransportOutInfo) outTransportInfo).getProcessingContext();
        try {
            Message message = xmlPayloadToHL7Message(messageContext);
            processingContext.offerApplicationResponses(message, messageContext);
        } catch (HL7Exception e) {
            handleException("Error while sending an custom ack message", e);
        }
    }

    /**
     * Get the response from the messagecontext
     *
     * @param ctx
     * @return
     * @throws HL7Exception
     */
    private Message xmlPayloadToHL7Message(MessageContext ctx) throws HL7Exception {
        OMElement hl7MsgEl = (OMElement) ctx.getEnvelope().getBody().getChildrenWithName(
                new QName(HL7Constants.HL7_NAMESPACE, HL7Constants.HL7_MESSAGE_ELEMENT_NAME)).next();
        String hl7XMLPayload = hl7MsgEl.getFirstElement().toString();
        return this.xmlparser.parse(hl7XMLPayload);
    }
}
