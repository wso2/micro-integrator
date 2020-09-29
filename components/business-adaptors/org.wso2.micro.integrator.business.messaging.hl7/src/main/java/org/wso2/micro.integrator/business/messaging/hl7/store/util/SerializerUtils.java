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

package org.wso2.micro.integrator.business.messaging.hl7.store.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.message.store.impl.commons.Axis2Message;
import org.apache.synapse.message.store.impl.commons.SynapseMessage;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SerializerUtils {

    private static final Log logger = LogFactory.getLog(SerializerUtils.class.getName());

    public static byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(obj);
            return b.toByteArray();
        } catch (IOException e) {
            logger.error("Error serializing message context. " + e.getMessage());
            return null;
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

    /**
     * Converts a Synapse Message Context to a representation that can be stored in the
     * Message store queue.
     *
     * @param synCtx
     * @return
     */
    public static SerializableMessageContext toStorableMessage(MessageContext synCtx, String messageId) {
        SerializableMessageContext message = new SerializableMessageContext();
        Axis2Message axis2msg = new Axis2Message();
        SynapseMessage synMsg = new SynapseMessage();
        Axis2MessageContext axis2MessageContext;

        if (synCtx instanceof Axis2MessageContext) {
            axis2MessageContext = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext msgCtx = axis2MessageContext.getAxis2MessageContext();
            axis2msg.setMessageID(messageId);
            if (msgCtx.getAxisOperation() != null) {
                axis2msg.setOperationAction(msgCtx.getAxisOperation().getSoapAction());
                axis2msg.setOperationName(msgCtx.getAxisOperation().getName());
            }
            axis2msg.setAction(msgCtx.getOptions().getAction());
            if (msgCtx.getAxisService() != null) {
                axis2msg.setService(msgCtx.getAxisService().getName());
            }
            if (msgCtx.getRelatesTo() != null) {
                axis2msg.setRelatesToMessageId(msgCtx.getRelatesTo().getValue());
            }
            if (msgCtx.getReplyTo() != null) {
                axis2msg.setReplyToAddress(msgCtx.getReplyTo().getAddress());
            }
            if (msgCtx.getFaultTo() != null) {
                axis2msg.setFaultToAddress(msgCtx.getFaultTo().getAddress());
            }
            if (msgCtx.getTo() != null) {
                axis2msg.setToAddress(msgCtx.getTo().getAddress());
            }
            axis2msg.setDoingPOX(msgCtx.isDoingREST());
            axis2msg.setDoingMTOM(msgCtx.isDoingMTOM());
            axis2msg.setDoingSWA(msgCtx.isDoingSwA());
            String soapEnvelope = msgCtx.getEnvelope().toString();
            axis2msg.setSoapEnvelope(soapEnvelope);
            axis2msg.setFLOW(msgCtx.getFLOW());
            if (msgCtx.getTransportIn() != null) {
                axis2msg.setTransportInName(msgCtx.getTransportIn().getName());
            }
            if (msgCtx.getTransportOut() != null) {
                axis2msg.setTransportOutName(msgCtx.getTransportOut().getName());
            }

            axis2msg.addProperty(HL7Constants.HL7_MESSAGE_OBJECT,
                                 ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                                         .getProperty(HL7Constants.HL7_MESSAGE_OBJECT));

            Iterator<String> abstractMCProperties = msgCtx.getPropertyNames();
            Map<String, Object> copy = new HashMap<String, Object>(msgCtx.getProperties().size());
            while (abstractMCProperties.hasNext()) {
                String propertyName = abstractMCProperties.next();
                Object propertyValue = msgCtx.getProperty(propertyName);
                if (propertyValue instanceof String || propertyValue instanceof Boolean
                        || propertyValue instanceof Integer || propertyValue instanceof Double
                        || propertyValue instanceof Character) {
                    copy.put(propertyName, propertyValue);
                }

            }

            Iterator<String> properties = msgCtx.getProperties().keySet().iterator();
            while (properties.hasNext()) {
                String key = properties.next();
                Object value = msgCtx.getProperty(key);
                if (value instanceof String) {
                    axis2msg.addProperty(key, value);
                }
            }
            message.setAxis2message(axis2msg);
            synMsg.setFaultResponse(synCtx.isFaultResponse());
            synMsg.setTracingState(synCtx.getTracingState());
            synMsg.setResponse(synCtx.isResponse());
            properties = synCtx.getPropertyKeySet().iterator();
            while (properties.hasNext()) {
                String key = properties.next();
                Object value = synCtx.getProperty(key);
                if (value instanceof String) {
                    synMsg.addProperty(key, (String) value);
                }
            }
            message.setSynapseMessage(synMsg);
        } else {
            throw new SynapseException("Cannot store message to store.");
        }
        return message;
    }

    /**
     * Converts a message read from the message store to a Synapse Message Context object.
     *
     * @param message  Message from the message store
     * @param axis2Ctx Final Axis2 Message Context
     * @param synCtx   Final Synapse message Context
     * @return Final Synapse Message Context
     */
    public static MessageContext toMessageContext(SerializableMessageContext message,
                                                  org.apache.axis2.context.MessageContext axis2Ctx,
                                                  MessageContext synCtx) {
        if (message == null) {
            logger.error("Cannot create Message Context. Message is null.");
            return null;
        }

        AxisConfiguration axisConfig = axis2Ctx.getConfigurationContext().getAxisConfiguration();
        if (axisConfig == null) {
            logger.warn("Cannot create AxisConfiguration. AxisConfiguration is null.");
            return null;
        }
        Axis2Message axis2Msg = message.getAxis2message();
        SOAPEnvelope envelope = getSoapEnvelope(axis2Msg.getSoapEnvelope());
        try {
            axis2Ctx.setEnvelope(envelope);
            // set the RMSMessageDto properties
            axis2Ctx.getOptions().setAction(axis2Msg.getAction());
            if (axis2Msg.getRelatesToMessageId() != null) {
                axis2Ctx.addRelatesTo(new RelatesTo(axis2Msg.getRelatesToMessageId()));
            }
            axis2Ctx.setMessageID(axis2Msg.getMessageID());
            axis2Ctx.getOptions().setAction(axis2Msg.getAction());
            axis2Ctx.setDoingREST(axis2Msg.isDoingPOX());
            axis2Ctx.setDoingMTOM(axis2Msg.isDoingMTOM());
            axis2Ctx.setDoingSwA(axis2Msg.isDoingSWA());
            if (axis2Msg.getService() != null) {
                AxisService axisService = axisConfig.getServiceForActivation(axis2Msg.getService());
                AxisOperation axisOperation = axisService.getOperation(axis2Msg.getOperationName());
                axis2Ctx.setFLOW(axis2Msg.getFLOW());
                ArrayList executionChain = new ArrayList();
                if (axis2Msg.getFLOW() == org.apache.axis2.context.MessageContext.OUT_FLOW) {
                    executionChain.addAll(axisOperation.getPhasesOutFlow());
                    executionChain.addAll(axisConfig.getOutFlowPhases());
                } else if (axis2Msg.getFLOW() == org.apache.axis2.context.MessageContext.OUT_FAULT_FLOW) {
                    executionChain.addAll(axisOperation.getPhasesOutFaultFlow());
                    executionChain.addAll(axisConfig.getOutFlowPhases());
                }
                axis2Ctx.setExecutionChain(executionChain);
                ConfigurationContext configurationContext = axis2Ctx.getConfigurationContext();
                axis2Ctx.setAxisService(axisService);
                ServiceGroupContext serviceGroupContext = configurationContext.createServiceGroupContext(
                        axisService.getAxisServiceGroup());
                ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);
                OperationContext operationContext = serviceContext.createOperationContext(axis2Msg.getOperationName());
                axis2Ctx.setServiceContext(serviceContext);
                axis2Ctx.setOperationContext(operationContext);
                axis2Ctx.setAxisService(axisService);
                axis2Ctx.setAxisOperation(axisOperation);
            }
            if (axis2Msg.getReplyToAddress() != null) {
                axis2Ctx.setReplyTo(new EndpointReference(axis2Msg.getReplyToAddress().trim()));
            }
            if (axis2Msg.getFaultToAddress() != null) {
                axis2Ctx.setFaultTo(new EndpointReference(axis2Msg.getFaultToAddress().trim()));
            }
            if (axis2Msg.getFromAddress() != null) {
                axis2Ctx.setFrom(new EndpointReference(axis2Msg.getFromAddress().trim()));
            }
            if (axis2Msg.getToAddress() != null) {
                axis2Ctx.getOptions().setTo(new EndpointReference(axis2Msg.getToAddress().trim()));
            }

            axis2Ctx.setProperties(axis2Msg.getProperties());
            axis2Ctx.setTransportIn(axisConfig.getTransportIn(axis2Msg.getTransportInName()));
            axis2Ctx.setTransportOut(axisConfig.getTransportOut(axis2Msg.getTransportOutName()));
            Object headers = axis2Msg.getProperties().get(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            Object map = axis2Msg.getProperties().get("ABSTRACT_MC_PROPERTIES");
            axis2Msg.getProperties().remove("ABSTRACT_MC_PROPERTIES");

            if (map instanceof Map) {
                Map<String, Object> abstractMCProperties = (Map) map;
                Iterator<String> properties = abstractMCProperties.keySet().iterator();
                while (properties.hasNext()) {
                    String property = properties.next();
                    Object value = abstractMCProperties.get(property);
                    axis2Ctx.setProperty(property, value);
                }
            }
            SynapseMessage synMsg = message.getSynapseMessage();
            synCtx.setTracingState(synMsg.getTracingState());
            Iterator<String> properties = synMsg.getProperties().keySet().iterator();
            while (properties.hasNext()) {
                String key = properties.next();
                Object value = synMsg.getProperties().get(key);
                synCtx.setProperty(key, value);
            }
            synCtx.setFaultResponse(synMsg.isFaultResponse());
            synCtx.setResponse(synMsg.isResponse());
            return synCtx;
        } catch (Exception e) {
            logger.error("Cannot create Message Context. Error:" + e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static SOAPEnvelope getSoapEnvelope(String soapEnvelpe) {
        try {
            //This is a temporary fix for ESBJAVA-1157 for Andes based(QPID) Client libraries
            //Thread.currentThread().setContextClassLoader(SynapseEnvironment.class.getClassLoader());
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                    new ByteArrayInputStream(getUTF8Bytes(soapEnvelpe)));
            StAXBuilder builder = new StAXSOAPModelBuilder(xmlReader);
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) builder.getDocumentElement();
            soapEnvelope.build();
            String soapNamespace = soapEnvelope.getNamespace().getNamespaceURI();
            if (soapEnvelope.getHeader() == null) {
                SOAPFactory soapFactory;
                if (soapNamespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                    soapFactory = OMAbstractFactory.getSOAP12Factory();
                } else {
                    soapFactory = OMAbstractFactory.getSOAP11Factory();
                }
                soapFactory.createSOAPHeader(soapEnvelope);
            }
            return soapEnvelope;
        } catch (XMLStreamException e) {
            logger.error("Cannot create SOAP Envelop. Error:" + e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static byte[] getUTF8Bytes(String soapEnvelpe) {
        byte[] bytes;
        try {
            bytes = soapEnvelpe.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(
                    "Unable to extract bytes in UTF-8 encoding. " + "Extracting bytes in the system default encoding"
                            + e.getMessage());
            bytes = soapEnvelpe.getBytes();
        }
        return bytes;
    }

}
