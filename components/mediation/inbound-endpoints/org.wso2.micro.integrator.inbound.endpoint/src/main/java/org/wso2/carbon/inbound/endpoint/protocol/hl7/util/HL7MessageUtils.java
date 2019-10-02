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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.util;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v22.message.ACK;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.util.idgenerator.UUIDGenerator;
import ca.uhn.hl7v2.validation.impl.DefaultValidation;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.osgi.service.ServiceReferenceHolder;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLProtocolException;

import java.io.IOException;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

public class HL7MessageUtils {

    private static final Log log = LogFactory.getLog(HL7MessageUtils.class);

    private static HapiContext validationContext = new DefaultHapiContext();
    private static HapiContext noValidationContext = new DefaultHapiContext();

    private static ConfigurationContext context;

    static {
        noValidationContext.setValidationContext(new NoValidation());
        validationContext.setValidationContext(new DefaultValidation());
    }

    private static PipeParser noValidationPipeParser = new PipeParser(noValidationContext);
    private static XMLParser noValidationXmlParser = new DefaultXMLParser(noValidationContext);
    private static PipeParser pipeParser = new PipeParser(validationContext);
    private static XMLParser xmlParser = new DefaultXMLParser(validationContext);

    private static SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
    private static OMNamespace ns = fac
            .createOMNamespace(Axis2HL7Constants.HL7_NAMESPACE, Axis2HL7Constants.HL7_ELEMENT_NAME);
    private static OMFactory omFactory = new OMDOMFactory();

    static {

        if (HL7Configuration.getInstance().getStringProperty(MLLPConstants.HL7_ID_GENERATOR, "file").equals("uuid")) {
            validationContext.getParserConfiguration().setIdGenerator(new UUIDGenerator());
            noValidationContext.getParserConfiguration().setIdGenerator(new UUIDGenerator());
        }
    }

    public static Message parse(String msg, boolean validate) throws HL7Exception {
        if (validate) {
            return pipeParser.parse(msg);
        } else {
            return noValidationPipeParser.parse(msg);
        }
    }

    public static Message parse(String msg, final Parser preProcessor) throws HL7Exception {
        return preProcessor.parse(msg);
    }

    public static MessageContext createSynapseMessageContext(Message message, InboundProcessorParams params)
            throws HL7Exception, AxisFault {

        MessageContext synCtx = createSynapseMessageContext(
                params.getProperties().getProperty(MLLPConstants.HL7_INBOUND_TENANT_DOMAIN));

        if (params.getProperties().getProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED) != null) {
            synCtx.setProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED,
                               params.getProperties().getProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED));
        }

        try {
            synCtx.setEnvelope(createEnvelope(synCtx, message, params));
        } catch (Exception e) {
            throw new HL7Exception(e);
        }

        return synCtx;
    }

    public static MessageContext createErrorMessageContext(String rawMessage, Exception errorMsg,
                                                           InboundProcessorParams params)
            throws AxisFault, HL7Exception {
        MessageContext synCtx = createSynapseMessageContext(
                params.getProperties().getProperty(MLLPConstants.HL7_INBOUND_TENANT_DOMAIN));

        if (params.getProperties().getProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED) != null) {
            synCtx.setProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED,
                               params.getProperties().getProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED));
        }

        try {
            synCtx.setProperty(SynapseConstants.ERROR_CODE, SynapseConstants.RCV_IO_ERROR_RECEIVING);
            synCtx.setProperty(SynapseConstants.ERROR_MESSAGE, errorMsg.getMessage());
            synCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                               (errorMsg.getCause() == null ? "null" : errorMsg.getCause().getMessage()));
            synCtx.setProperty(SynapseConstants.ERROR_EXCEPTION, errorMsg);
            synCtx.setEnvelope(createErrorEnvelope(synCtx, rawMessage, errorMsg.getMessage(), params));
        } catch (Exception e) {
            throw new HL7Exception(e);
        }

        return synCtx;
    }

    // Create Synapse Message Context
    private static org.apache.synapse.MessageContext createSynapseMessageContext(String tenantDomain) throws AxisFault {

        // Create super tenant message context
        org.apache.axis2.context.MessageContext axis2MsgCtx = createAxis2MessageContext();
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);

        return MessageContextCreatorForAxis2.getSynapseMessageContext(axis2MsgCtx);
    }

    // Create Axis2 Message Context
    private static org.apache.axis2.context.MessageContext createAxis2MessageContext() {

        org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();
        axis2MsgCtx.setMessageID(UIDGenerator.generateURNString());

        axis2MsgCtx.setConfigurationContext(
                ServiceReferenceHolder.getInstance().getConfigurationContextService().getServerConfigContext());

        // Axis2 spawns a new threads to send a message if this is TRUE
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING, Boolean.FALSE);

        axis2MsgCtx.setServerSide(true);

        return axis2MsgCtx;
    }

    private static SOAPEnvelope createEnvelope(MessageContext synCtx, Message message, InboundProcessorParams params)
            throws HL7Exception, XMLStreamException, MLLProtocolException {
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        boolean rawMessage = false;
        String xmlDoc = "";
        try {
            xmlDoc = xmlParser.encode(message);
            synCtx.setProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED, new Boolean(true));
        } catch (HL7Exception e) {
            synCtx.setProperty(Axis2HL7Constants.HL7_VALIDATION_PASSED, new Boolean(false));
            if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE) != null && params
                    .getProperties().getProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE).equals("true")) {
                xmlDoc = message.encode();
                rawMessage = true;
            } else {
                log.error("Could not encode HL7 message into XML. " + "Set " + MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE
                                  + " to build invalid HL7 messages containing raw HL7 message.", e);
                throw new HL7Exception("Could not encode HL7 message into XML", e);
            }
        }

        OMElement messageEl;
        if (!rawMessage) {
            messageEl = generateHL7MessageElement(xmlDoc);
        } else {
            messageEl = generateHL7RawMessaegElement(xmlDoc);
        }
        envelope.getBody().addChild(messageEl);
        return envelope;
    }

    private static SOAPEnvelope createErrorEnvelope(MessageContext synCtx, String rawMsg, String erroMsg,
                                                    InboundProcessorParams params) {
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMElement messageEl;
        boolean rawMessage = false;
        if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE) != null && params
                .getProperties().getProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE).equals("true")) {
            rawMessage = true;
        }

        if (rawMessage) {
            messageEl = generateHL7RawMessaegElement(rawMsg);
            envelope.getBody().addChild(messageEl);
        }

        return envelope;

    }

    public static OMElement generateHL7MessageElement(String hl7XmlMessage) throws XMLStreamException {
        OMElement hl7Element = AXIOMUtil.stringToOM(hl7XmlMessage);

        OMElement messageEl = fac.createOMElement(Axis2HL7Constants.HL7_MESSAGE_ELEMENT_NAME, ns);
        messageEl.addChild(hl7Element);
        return messageEl;
    }

    public static OMElement generateHL7RawMessaegElement(String hl7XmlMessage) {
        OMElement hl7Element = omFactory.createOMElement(new QName("rawMessage"));
        OMText rawMessage = hl7Element.getOMFactory().createOMText(hl7Element, hl7XmlMessage, XMLStreamConstants.CDATA);
        hl7Element.addChild(rawMessage);
        OMElement messageEl = fac.createOMElement(Axis2HL7Constants.HL7_MESSAGE_ELEMENT_NAME, ns);
        messageEl.addChild(hl7Element);
        return messageEl;
    }

    public static Message createNack(Message hl7Msg, String errorMsg) throws HL7Exception {
        if (errorMsg == null) {
            errorMsg = "";
        }
        if (hl7Msg == null) {
            return createDefaultNackMessage(errorMsg);
        } else {
            try {
                return hl7Msg.generateACK(AcknowledgmentCode.AE, new HL7Exception(errorMsg));
            } catch (IOException e) {
                throw new HL7Exception(e);
            }
        }
    }

    public static Message createDefaultNack(String errorMsg) {
        try {
            return createDefaultNackMessage(errorMsg);
        } catch (DataTypeException e) {
            log.error("Error while creating default NACK message.", e);
        }

        return null;
    }

    private static Message createDefaultNackMessage(String errorMsg) throws DataTypeException {
        ACK ack = new ACK();
        ack.getMSH().getFieldSeparator().setValue(Axis2HL7Constants.HL7_DEFAULT_FIELD_SEPARATOR);
        ack.getMSH().getEncodingCharacters().setValue(Axis2HL7Constants.HL7_DEFAULT_ENCODING_CHARS);
        ack.getMSH().getReceivingApplication().setValue(Axis2HL7Constants.HL7_DEFAULT_RECEIVING_APPLICATION);
        ack.getMSH().getReceivingFacility().setValue(Axis2HL7Constants.HL7_DEFAULT_RECEIVING_FACILITY);
        ack.getMSH().getProcessingID().setValue(Axis2HL7Constants.HL7_DEFAULT_PROCESSING_ID);
        ack.getMSA().getAcknowledgementCode().setValue(Axis2HL7Constants.HL7_DEFAULT_ACK_CODE_AR);
        ack.getMSA().getMessageControlID().setValue(Axis2HL7Constants.HL7_DEFAULT_MESSAGE_CONTROL_ID);
        ack.getERR().getErrorCodeAndLocation(0).getCodeIdentifyingError().
                getIdentifier().setValue(errorMsg);
        return ack;
    }

    public static int getInt(String key, InboundProcessorParams params) throws NumberFormatException {
        return Integer.valueOf(params.getProperties().getProperty(key));
    }

    public static boolean getBoolean(String key, InboundProcessorParams params) {
        return Boolean.valueOf(params.getProperties().getProperty(key));
    }

    /**
     * Get the hl7message from the MessageContext
     *
     * @param ctx
     * @return
     * @throws HL7Exception
     */
    public static Message payloadToHL7Message(MessageContext ctx, InboundProcessorParams params)
            throws HL7Exception, NoSuchElementException {

        OMElement hl7MsgEl = (OMElement) ctx.getEnvelope().getBody().getChildrenWithName(
                new QName(Axis2HL7Constants.HL7_NAMESPACE, Axis2HL7Constants.HL7_MESSAGE_ELEMENT_NAME)).next();
        String hl7XMLPayload = hl7MsgEl.getFirstElement().toString();
        String pipeMsg;
        Message msg = null;
        try {
            msg = xmlParser.parse(hl7XMLPayload);
            pipeMsg = pipeParser.encode(msg);
            msg = pipeParser.parse(pipeMsg);
            return msg;
        } catch (EncodingNotSupportedException e) {
            log.error("Encoding error in the message", e);
            throw new HL7Exception("Encoding error in the message: " + e.getMessage(), e);
        } catch (DataTypeException e) {
            // Make this as warning.Since some remote systems require enriched messages that violate some HL7
            //rules it would be nice to be able to still send the message.
            log.warn("Rule validation fails.", e);
            if (!(params.getProperties().getProperty(Axis2HL7Constants.HL7_VALIDATE_MESSAGE).equals("false"))) {
                msg = noValidationXmlParser.parse(hl7XMLPayload);
                return msg;
            }

        } catch (HL7Exception e) {
            log.error("Error in the Message :", e);
            throw new HL7Exception("Encoding error in the message: " + e.getMessage(), e);
        }
        return msg;
    }

}
