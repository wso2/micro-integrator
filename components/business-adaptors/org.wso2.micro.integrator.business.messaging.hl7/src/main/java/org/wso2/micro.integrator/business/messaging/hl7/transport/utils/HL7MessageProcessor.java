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

package org.wso2.micro.integrator.business.messaging.hl7.transport.utils;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.DefaultValidation;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7ProcessingContext;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Utils;
import org.wso2.micro.integrator.business.messaging.hl7.transport.HL7Endpoint;
import org.wso2.micro.integrator.business.messaging.hl7.transport.HL7TransportOutInfo;

import javax.xml.stream.XMLStreamException;

public class HL7MessageProcessor implements Application {

    private static final Log log = LogFactory.getLog(HL7MessageProcessor.class);

    private HL7Endpoint endpoint;

    public HL7MessageProcessor(HL7Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Message processMessage(Message message) throws ApplicationException, HL7Exception {
        if (log.isDebugEnabled()) {
            log.debug("Received HL7 message: " + message.toString());
        }
        Terser terser = new Terser(message);
        String messageControlerID = terser.get("/MSH-10");
        if (messageControlerID == null || messageControlerID.length() == 0) {
            throw new HL7Exception("MSH segment missing required field: Control ID (MSH-10)",
                                   HL7Exception.REQUIRED_FIELD_MISSING);
        }

        HL7ProcessingContext procCtx = this.endpoint.getProcessingContext();
        try {
            MessageContext messageContext = endpoint.createMessageContext();

            // Message validation if transport.hl7.ValidateMessages is true
            if (procCtx.isValidateMessage()) {
                procCtx.getPipeParser().setValidationContext(new DefaultValidation());
                try {
                    procCtx.getPipeParser().parse(message.encode());
                    messageContext.setProperty(HL7Constants.HL7_DEFAULT_VALIDATION_PASSED, "success");
                } catch (HL7Exception e) {
                    messageContext.setProperty(HL7Constants.HL7_DEFAULT_VALIDATION_PASSED, "fault");
                    log.error("Validation of HL7 message failed. " + e.getMessage());
                }
            }
            // We set the validation context to NoValidation() or else subsequent messages may validate inside
            // hapi server.
            procCtx.getPipeParser().setValidationContext(new NoValidation());

            HL7TransportOutInfo outinfo = new HL7TransportOutInfo();
            outinfo.setMessageControllerID(messageControlerID);
            outinfo.setProcessingContext(procCtx);
            messageContext.setProperty(Constants.OUT_TRANSPORT_INFO, outinfo);

            messageContext.setIncomingTransportName(HL7Constants.TRANSPORT_NAME);
            procCtx.initMessageContext(message, messageContext);
            procCtx.checkConformanceProfile(message);
            messageContext.setEnvelope(createEnvelope(messageContext, message));
            AxisEngine.receive(messageContext);
            return procCtx.handleHL7Result(messageContext, message);
        } catch (AxisFault axisFault) {
            return procCtx.createNack(message, "Error while processing the HL7 message through the engine: " + axisFault
                    .getMessage());
        } catch (XMLStreamException e) {
            return procCtx.createNack(message, "IO error while processing the HL7 content: " + e.getMessage());
        }
    }

    private SOAPEnvelope createEnvelope(MessageContext msgCtx, Message message)
            throws HL7Exception, XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();

        Parser xmlParser = new DefaultXMLParser();
        String xmlDoc = "";
        try {
            xmlDoc = xmlParser.encode(message);
            msgCtx.setProperty(HL7Constants.HL7_VALIDATION_PASSED, new Boolean(true));
        } catch (HL7Exception e) {
            msgCtx.setProperty(HL7Constants.HL7_VALIDATION_PASSED, new Boolean(false));
            if (this.endpoint.getProcessingContext().isBuildRawMessages()) {
                xmlDoc = "<rawMessage>" + StringEscapeUtils.escapeXml(message.encode()) + "</rawMessage>";
            } else {
                log.error("Could not encode HL7 message into XML: " + e.getMessage() + " " + "Set "
                                  + HL7Constants.HL7_BUILD_RAW_MESSAGE
                                  + " to build invalid HL7 messages containing raw HL7 message.");
            }
        }

        OMElement messageEl = HL7Utils.generateHL7MessageElement(xmlDoc);
        envelope.getBody().addChild(messageEl);
        return envelope;
    }

    public boolean canProcess(Message message) {
        return true;
    }

}
