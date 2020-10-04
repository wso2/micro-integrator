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
package org.wso2.micro.integrator.business.messaging.hl7.common;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.conf.ProfileException;
import ca.uhn.hl7v2.conf.check.DefaultValidator;
import ca.uhn.hl7v2.conf.parser.ProfileParser;
import ca.uhn.hl7v2.conf.spec.RuntimeProfile;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v22.message.ACK;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.util.idgenerator.InMemoryIDGenerator;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants.MessageEncoding;
import org.wso2.micro.integrator.business.messaging.hl7.common.HL7Constants.MessageType;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.MessageData;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.conf.HL7MessagePublisherConfig;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.conf.ServerConfig;
import org.wso2.micro.integrator.business.messaging.hl7.common.data.publisher.HL7EventPublisher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.xml.namespace.QName;

/**
 * This class represents an HL7 message processing context.
 */
public class HL7ProcessingContext {

    private static final Log log = LogFactory.getLog(HL7ProcessingContext.class);

    private boolean publisherEnabled;

    private boolean autoAck;

    private boolean validateMessage;

    private int timeOutVal;

    private RuntimeProfile conformanceProfile;

    private PipeParser pipeParser;

    private Parser xmlparser;

    private DefaultValidator defaultValidator;

    private HL7MessagePreprocessor messagePreprocessor;

    private BlockingQueue<Message> applicationResponses;

    private boolean isPassThroughInvalidMessages;

    private boolean isBuildRawMessages;
    private HL7MessagePublisherConfig hl7PublisherConfig;

    private HL7EventPublisher eventPublisher;

    public HL7ProcessingContext(boolean autoAck, boolean validateMessage, boolean publisherEnabled,
                                String conformanceProfileURL, String messagePreprocessorClass, String serverUrl,
                                String secureServerUrl, String serverPassword, String serverUsername,
                                boolean passThroughInvalidMessages, boolean buildRawMessages) throws HL7Exception {

        this.autoAck = autoAck;
        this.validateMessage = validateMessage;
        this.publisherEnabled = publisherEnabled;
        this.defaultValidator = new DefaultValidator();
        this.applicationResponses = new LinkedBlockingQueue<Message>();
        this.xmlparser = new DefaultXMLParser();
        this.isPassThroughInvalidMessages = passThroughInvalidMessages;
        this.isBuildRawMessages = buildRawMessages;

        this.eventPublisher = new HL7EventPublisher(
                new ServerConfig(secureServerUrl, serverUrl, serverUsername, serverPassword));

        if (conformanceProfileURL != null) {
            this.conformanceProfile = this.createConformanceProfile(conformanceProfileURL);
        }
        if (messagePreprocessorClass != null) {
            try {
                this.messagePreprocessor = (HL7MessagePreprocessor) Class.forName(messagePreprocessorClass)
                        .newInstance();
            } catch (Exception e) {
                throw new HL7Exception("Error creating message preprocessor: " + e.getMessage(), e);
            }
        }
        if (this.getMessagePreprocessor() != null) {
            this.pipeParser = new PipeParser() {
                public Message parse(String message) throws HL7Exception {
                    message = getMessagePreprocessor().process(message, MessageType.V2X, MessageEncoding.ER7);
                    return super.parse(message);
                }

            };
        } else {
            this.pipeParser = new PipeParser();
        }

        // PipeParser is always set with NoValidation context, other wise invalid messages will result in halting
        // execution.
        this.getPipeParser().setValidationContext(new NoValidation());
        //Improve the performance for generating ID.
        this.getPipeParser().getParserConfiguration().setIdGenerator(new InMemoryIDGenerator());
    }

    public HL7ProcessingContext(ParameterInclude params) throws HL7Exception, AxisFault {
        this(ParamUtils.getOptionalParamBoolean(params, HL7Constants.HL7_AUTO_ACKNOWLEDGE, true),
             ParamUtils.getOptionalParamBoolean(params, HL7Constants.HL7_VALIDATE_MESSAGE, true),
             ParamUtils.getOptionalParamBoolean(params, HL7Constants.HL7_MESSAGE_PUBLISHER_ENABLE, false),
             ParamUtils.getOptionalParam(params, HL7Constants.HL7_CONFORMANCE_PROFILE_PATH),
             ParamUtils.getOptionalParam(params, HL7Constants.HL7_MESSAGE_PREPROCESSOR_CLASS),
             ParamUtils.getOptionalParam(params, HL7Constants.HL7_MESSAGE_PUBLISHER_SERVER_URL),
             ParamUtils.getOptionalParam(params, HL7Constants.HL7_MESSAGE_PUBLISHER_SECURE_SERVER_URL),
             ParamUtils.getOptionalParam(params, HL7Constants.HL7_MESSAGE_PUBLISHER_SERVER_PASSWORD),
             ParamUtils.getOptionalParam(params, HL7Constants.HL7_MESSAGE_PUBLISHER_SERVER_USERNAME),
             ParamUtils.getOptionalParamBoolean(params, HL7Constants.HL7_PASS_THROUGH_INVALID_MESSAGES, true),
             ParamUtils.getOptionalParamBoolean(params, HL7Constants.HL7_BUILD_RAW_MESSAGE, true));
    }

    public HL7ProcessingContext(AxisService service) throws HL7Exception {
        this(extractServiceBooleanParam(service, HL7Constants.HL7_AUTO_ACKNOWLEDGE, true),
             extractServiceBooleanParam(service, HL7Constants.HL7_VALIDATE_MESSAGE, true),
             extractServiceBooleanParam(service, HL7Constants.HL7_MESSAGE_PUBLISHER_ENABLE, false),
             extractServiceStringParam(service, HL7Constants.HL7_CONFORMANCE_PROFILE_PATH),
             extractServiceStringParam(service, HL7Constants.HL7_MESSAGE_PREPROCESSOR_CLASS),
             extractServiceStringParam(service, HL7Constants.HL7_MESSAGE_PUBLISHER_SERVER_URL),
             extractServiceStringParam(service, HL7Constants.HL7_MESSAGE_PUBLISHER_SECURE_SERVER_URL),
             extractServiceStringParam(service, HL7Constants.HL7_MESSAGE_PUBLISHER_SERVER_PASSWORD),
             extractServiceStringParam(service, HL7Constants.HL7_MESSAGE_PUBLISHER_SERVER_USERNAME),
             extractServiceBooleanParam(service, HL7Constants.HL7_PASS_THROUGH_INVALID_MESSAGES, true),
             extractServiceBooleanParam(service, HL7Constants.HL7_BUILD_RAW_MESSAGE, true));
    }

    public HL7MessagePreprocessor getMessagePreprocessor() {
        return messagePreprocessor;
    }

    private static boolean extractServiceBooleanParam(AxisService service, String name, boolean defaultVal) {
        if (service != null) {
            Parameter param = service.getParameter(name);
            if (param != null) {
                Object value = param.getValue();
                if (value != null) {
                    return Boolean.parseBoolean(value.toString());
                }
            }
        }
        return defaultVal;
    }

    private static String extractServiceStringParam(AxisService service, String name) {
        if (service != null) {
            Parameter param = service.getParameter(name);
            if (param != null) {
                Object value = param.getValue();
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }

    private Message createDefaultNackMessage(String errorMsg) throws DataTypeException {
        ACK ack = new ACK();
        ack.getMSH().getFieldSeparator().setValue(HL7Constants.HL7_DEFAULT_FIELD_SEPARATOR);
        ack.getMSH().getEncodingCharacters().setValue(HL7Constants.HL7_DEFAULT_ENCODING_CHARS);
        ack.getMSH().getReceivingApplication().setValue(HL7Constants.HL7_DEFAULT_RECEIVING_APPLICATION);
        ack.getMSH().getReceivingFacility().setValue(HL7Constants.HL7_DEFAULT_RECEIVING_FACILITY);
        ack.getMSH().getProcessingID().setValue(HL7Constants.HL7_DEFAULT_PROCESSING_ID);
        ack.getMSA().getAcknowledgementCode().setValue(HL7Constants.HL7_DEFAULT_ACK_CODE_AR);
        ack.getMSA().getMessageControlID().setValue(HL7Constants.HL7_DEFAULT_MESSAGE_CONTROL_ID);
        ack.getERR().getErrorCodeAndLocation(0).getCodeIdentifyingError().
                getIdentifier().setValue(errorMsg);
        return ack;
    }

    public DefaultValidator getDefaultValidator() {
        return defaultValidator;
    }

    private RuntimeProfile createConformanceProfile(String conformanceProfileURL) throws HL7Exception {
        InputStream in = null;
        try {
            in = new URL(conformanceProfileURL).openStream();
            RuntimeProfile profile = new ProfileParser(false).parse(HL7Utils.streamToString(in));
            return profile;
        } catch (Exception e) {
            throw new HL7Exception("Error creating conformance profile: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    public PipeParser getPipeParser() {
        return pipeParser;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public boolean isValidateMessage() {
        return validateMessage;
    }

    public boolean isPassThroughInvalidMessages() {
        return this.isPassThroughInvalidMessages;
    }

    public boolean isBuildRawMessages() {
        return this.isBuildRawMessages;
    }

    public RuntimeProfile getConformanceProfile() {
        return conformanceProfile;
    }

    public void offerApplicationResponses(Message message, MessageContext messageContext)
            throws HL7Exception, AxisFault {
        String resultMode = (String) messageContext.getProperty(HL7Constants.HL7_RESULT_MODE);
        if (resultMode != null) {
            handleAutoAckNack(resultMode, messageContext, message);
        } else {
            applicationResponses.offer(message);
        }
    }

    public Message parseMessage(String hl7TextMsg) throws HL7Exception, ProfileException {
        return this.getPipeParser().parse(hl7TextMsg);
    }

    public void checkConformanceProfile(Message message) throws HL7Exception {
        RuntimeProfile profile = this.getConformanceProfile();
        if (profile != null) {
            try {
                HL7Exception[] errors = this.getDefaultValidator().validate(message, profile.getMessage());
                //Throw an exception if there are errors
                if (errors.length > 0) {
                    throw new HL7Exception(Arrays.toString(errors), HL7Exception.UNSUPPORTED_MESSAGE_TYPE);
                }
            } catch (ProfileException e) {
                throw new HL7Exception(e.getMessage(), e);
            }
        }
    }

    public void initMessageContext(Message message, MessageContext msgCtx) {
        msgCtx.setProperty(HL7Constants.HL7_MESSAGE_OBJECT, message);
        msgCtx.setProperty(HL7Constants.HL7_PASS_THROUGH_INVALID_MESSAGES, isPassThroughInvalidMessages());
        msgCtx.setProperty(HL7Constants.HL7_BUILD_RAW_MESSAGE, isBuildRawMessages());
        publishMessage(message, msgCtx);

        try {
            msgCtx.setProperty(HL7Constants.HL7_RAW_MESSAGE_PROPERTY_NAME, message.encode());
        } catch (HL7Exception e) {
            log.error("Error while encoding for RAW HL7 message in EDI format");
        }
    }

    public void publishMessage(Message message, MessageContext msgCtx) {
        if (isPublisherEnabled()) {
            try {
                hl7PublisherConfig = new HL7MessagePublisherConfig();
                MessageData messageData = hl7PublisherConfig.createMessage(message, msgCtx);
                eventPublisher.publish(messageData);
            } catch (HL7Exception e) {
                log.error("Error in publishing the message", e);
            }
        }
    }

    public Message createAck(Message hl7Msg) throws HL7Exception {
        try {
            return hl7Msg.generateACK();
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
    }

    public Message createNack(Message hl7Msg, String errorMsg) throws HL7Exception {
        if (errorMsg == null) {
            errorMsg = "";
        }
        if (hl7Msg == null) {
            return this.createDefaultNackMessage(errorMsg);
        } else {
            try {
                return hl7Msg.generateACK(HL7Constants.HL7_MSA_ERROR_FIELD_VALUE, new HL7Exception(errorMsg));
            } catch (IOException e) {
                throw new HL7Exception(e);
            }
        }
    }

    /**
     * Handle the AUTOACK, NACK messages,which are to be sent back to the client
     *
     * @param ctx
     * @param hl7Msg
     * @return
     * @throws HL7Exception
     * @throws AxisFault
     */
    public Message handleHL7Result(MessageContext ctx, Message hl7Msg) throws HL7Exception, AxisFault {
        String resultMode = (String) ctx.getProperty(HL7Constants.HL7_RESULT_MODE);
        String applicationAck = (String) ctx.getProperty(HL7Constants.HL7_APPLICATION_ACK);

        if (resultMode != null) {
            return handleAutoAckNack(resultMode, ctx, hl7Msg);
        } else if (this.isAutoAck()) {
            return this.createAck(hl7Msg);
        }
        /*
         * no one ACKed or NACKed, want to create a application ACK message so we
         * will return the result from the downstream application.
         */

        return handleApplicationACK(applicationAck, hl7Msg);

    }

    /**
     * Handle Auto ack/nack properties in INFLOW and OUT FLOW
     *
     * @param resultMode
     * @param ctx
     * @param hl7Msg
     * @return
     * @throws HL7Exception
     * @throws AxisFault
     */
    private Message handleAutoAckNack(String resultMode, MessageContext ctx, Message hl7Msg)
            throws HL7Exception, AxisFault {
        Message msg;
        String applicationAck = (String) ctx.getProperty(HL7Constants.HL7_APPLICATION_ACK);
        if (HL7Constants.HL7_RESULT_MODE_ACK.equals(resultMode)) {
            if (ctx.getFLOW() == 1) { // it is inflow

                if (!(("true").equalsIgnoreCase(applicationAck))) {
                    //Since Result Mode is ACK and ACK Sends to Client. Response from back end is removed
                    //And HL7_APPLICATION_ACK is not enabled, Content will be removed from the Queue
                    //if it is failed to remove, System get memory Leak

                    applicationResponses.clear();
                }
                msg = this.createAck(hl7Msg);
                return msg;
            } else {
                MessageContext requestMessageCtx = ctx.getOperationContext().getMessageContext(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);

                Message requesthl7message = xmlPayloadToHL7Message(requestMessageCtx);
                msg = this.createAck(requesthl7message);
                applicationResponses.offer(msg);
            }
        } else if (HL7Constants.HL7_RESULT_MODE_NACK.equals(resultMode)) {
            String nackMessage = (String) ctx.getProperty(HL7Constants.HL7_NACK_MESSAGE);
            if (nackMessage == null) {
                nackMessage = "";
            }
            if (ctx.getFLOW() == 1) {
                msg = this.createNack(hl7Msg, nackMessage);
                if (!(("true").equalsIgnoreCase(applicationAck))) {
                    //Since Result Mode is NACK and NACK Sends to Client. Response from back end is removed
                    //And HL7_APPLICATION_ACK is not enabled, Content will be removed from the Queue
                    //if it is failed to remove, System get memory Leak
                    applicationResponses.clear();
                }
                return msg;
            } else {
                MessageContext requestMessageCtx = ctx.getOperationContext().getMessageContext(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);

                Message requesthl7message = xmlPayloadToHL7Message(requestMessageCtx);
                msg = this.createNack(requesthl7message, nackMessage);
                applicationResponses.offer(msg);
            }
        } else {
            return this.createNack(hl7Msg, "Application Error: ACK/NACK was not explicitly returned");
        }
        return null;
    }

    /**
     * ACK or NACK property is not set, want to create an application ACK
     * message so we will return the result from the downstream application. If
     * application-ack is set to 'false', we will generate a nack response,
     * since user has not set either auto-ack property or application-ack
     * property.
     */
    private Message handleApplicationACK(String applicationAck, Message hl7Msg) throws HL7Exception {
        String errMsg = "User has not set any property to indicate the system, whether it has to "
                + "generate ACK message or not.";
        if (("true").equalsIgnoreCase(applicationAck)) {
            try {

                Terser terser = new Terser(hl7Msg);
                String messageControlID_in = terser.get("/MSH-10");

                Message response = pollMyResponse(messageControlID_in);
                return response;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                applicationResponses.poll(timeOutVal, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error("Error During handle Application Ack - Removing from Queue", e);
            }
        }
        return this.createNack(hl7Msg, errMsg);
    }

    /**
     * Try to get ACK response from applicationResponses. If no response match with messageControlID_in
     * it retry every 1 second until the timeOutVal is triggered
     *
     * @param messageControlID_in
     * @return
     * @throws InterruptedException after Transport Timeout
     */
    private Message pollMyResponse(String messageControlID_in) throws InterruptedException {

        long timeStart = Calendar.getInstance().getTimeInMillis();
        long timeEnd = 0;
        long timeDiff = 0;
        log.debug("thread[" + Thread.currentThread().getId() + "] startTime: " + timeStart + " timeOutVal: "
                          + timeOutVal);
        MsgWrapper responseWrapper = new MsgWrapper();
        Hl7Consumer consumer = new Hl7Consumer(messageControlID_in, responseWrapper);
        Message response = null;
        do {
            applicationResponses.forEach(consumer);
            timeEnd = Calendar.getInstance().getTimeInMillis();
            timeDiff = timeEnd - timeStart;
            response = responseWrapper.getMsg();
            if (response == null)
                Thread.sleep(1000);
        } while (response == null && timeDiff < timeOutVal);
        if (response == null) {
            log.error("thread[" + Thread.currentThread().getId() + "] No response received for messageControlID_in:"
                              + messageControlID_in);
            throw new InterruptedException("No response received for messageControlID_in:" + messageControlID_in);
        }

        log.debug("thread[" + Thread.currentThread().getId() + "] found response for messageControlID_in:"
                          + messageControlID_in);
        return response;
    }

    /**
     * Get the hl7message from the messagecontext
     *
     * @param ctx
     * @return
     * @throws HL7Exception
     */
    private Message xmlPayloadToHL7Message(MessageContext ctx) throws HL7Exception {

        OMElement hl7MsgEl = (OMElement) ctx.getEnvelope().getBody().getChildrenWithName(
                new QName(HL7Constants.HL7_NAMESPACE, HL7Constants.HL7_MESSAGE_ELEMENT_NAME)).next();
        String hl7XMLPayload = hl7MsgEl.getFirstElement().toString();
        Message msg = null;
        try {
            msg = this.xmlparser.parse(hl7XMLPayload);
            return msg;
        } catch (EncodingNotSupportedException e) {
            log.error("Encoding error in the message", e);
            throw new HL7Exception("Encoding error in the message: " + e.getMessage(), e);
        } catch (DataTypeException e) {
            // Make this as warning.Since some remote systems require enriched messages that violate some HL7
            //rules it would 	be nice to be able to still send the message.
            log.warn("Rule validation fails." + e);
            if (!(this.isValidateMessage())) {
                this.xmlparser.setValidationContext(new NoValidation());
                msg = this.xmlparser.parse(hl7XMLPayload);
                return msg;
            }

        } catch (HL7Exception e) {
            log.error("Error in the Message :", e);
            throw new HL7Exception("Encoding error in the message: " + e.getMessage(), e);
        }
        return msg;
    }

    public boolean isPublisherEnabled() {
        return publisherEnabled;
    }

    public void setPublisherEnabled(boolean publisherEnabled) {
        this.publisherEnabled = publisherEnabled;
    }

    public void setHl7PublisherConfig(HL7MessagePublisherConfig hl7PublisherConfig) {
        this.hl7PublisherConfig = hl7PublisherConfig;
    }

    /**
     * @param timeOut Transport Timeout value
     */
    public void setTimeOutVal(int timeOut) {
        this.timeOutVal = timeOut;
    }

    /**
     * Custom Consumer for match and return response ACK
     */
    private class Hl7Consumer implements Consumer<Message> {

        private String msgControlId;
        private MsgWrapper responseWrapper;

        public Hl7Consumer(String msgControlId, MsgWrapper responseWrapper) {
            this.msgControlId = msgControlId;
            this.responseWrapper = responseWrapper;
        }

        @Override
        public void accept(Message t) {

            Terser terser = new Terser(t);
            String messageControlerID_out = null;
            try {
                messageControlerID_out = terser.get("/MSA-2");
            } catch (HL7Exception e) {
                log.error("Missing field MSA-2 for message:" + t);
                return;
            }
            if (msgControlId.equals(messageControlerID_out)) {
                log.debug("thread[" + Thread.currentThread().getId() + "] accept response");
                responseWrapper.setMsg(t);
                applicationResponses.remove(t);
            }

        }

    }

    /**
     * Simple message wrapper class
     */
    private class MsgWrapper {
        private Message msg;

        public void setMsg(Message msg) {
            this.msg = msg;
        }

        public Message getMsg() {
            return msg;
        }

    }
}
