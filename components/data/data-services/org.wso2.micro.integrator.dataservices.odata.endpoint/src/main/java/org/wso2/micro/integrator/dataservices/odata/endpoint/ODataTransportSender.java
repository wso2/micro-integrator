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

import javax.ws.rs.core.MediaType;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.SynapseException;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.PassThroughHttpSender;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.ProtocolState;
import org.apache.synapse.transport.passthru.SourceContext;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.apache.synapse.transport.passthru.SourceResponse;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.apache.synapse.transport.passthru.util.PassThroughTransportUtils;
import org.apache.synapse.transport.passthru.util.SourceResponseFactory;
import org.wso2.caching.CachingConstants;
import org.wso2.caching.digest.DigestGenerator;
import org.wso2.micro.integrator.core.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import javax.xml.namespace.QName;

/**
 * This class streams a SOAP message to the client given a response that is also streaming.
 */
public class ODataTransportSender extends PassThroughHttpSender implements TransportSender {

    private static final String PASS_THROUGH_SOURCE_CONNECTION = "pass-through.Source-Connection";
    private static final String PASS_THROUGH_SOURCE_CONFIGURATION = "PASS_THROUGH_SOURCE_CONFIGURATION";
    private static final String RESPONSE_MESSAGE_CONTEXT = "RESPONSE_MESSAGE_CONTEXT";
    private static final String CONTENT_TYPE = "ContentType";
    private DigestGenerator digestGenerator;
    private ODataServletResponse response;

    public ODataTransportSender(ConfigurationContext configurationContext, TransportOutDescription transportOut,
                                ODataServletResponse response) {
        this.digestGenerator = CachingConstants.DEFAULT_XML_IDENTIFIER;
        this.response = response;
        try {
            init(configurationContext, transportOut);
        } catch (AxisFault e) {
            throw new SynapseException("Error occurred while initializing the Stream Sender.", e);
        }
    }

    /**
     * This method builds a SOAP message from a streaming Servlet response and stream the message back to the client.
     *
     * @param msgContext Message context.
     * @throws IOException if any interrupted I/O operations occurred while sending the message.
     */
    public void submitResponse(MessageContext msgContext) throws IOException {
        SourceConfiguration sourceConfiguration = (SourceConfiguration) msgContext.getProperty(
                PASS_THROUGH_SOURCE_CONFIGURATION);
        NHttpServerConnection conn = (NHttpServerConnection) msgContext.getProperty(PASS_THROUGH_SOURCE_CONNECTION);
        SourceRequest sourceRequest = SourceContext.getRequest(conn);
        if (sourceRequest == null) {
            if (conn.getContext().getAttribute(PassThroughConstants.SOURCE_CONNECTION_DROPPED) == null
                    || !(Boolean) conn.getContext().getAttribute(PassThroughConstants.SOURCE_CONNECTION_DROPPED)) {
                this.log.warn("Trying to submit a response to an already closed connection : " + conn);
            }
        } else {
            SourceResponse sourceResponse = SourceResponseFactory.create(msgContext, sourceRequest,
                                                                         sourceConfiguration);
            conn.getContext().setAttribute(RESPONSE_MESSAGE_CONTEXT, msgContext);
            SourceContext.setResponse(conn, sourceResponse);
            Boolean noEntityBody = (Boolean) msgContext.getProperty(PassThroughConstants.NO_ENTITY_BODY);
            Pipe pipe = (Pipe) msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
            if (noEntityBody == null || !noEntityBody || pipe != null) {
                if (pipe == null) {
                    pipe = new Pipe(sourceConfiguration.getBufferFactory().getBuffer(), "Pipe", sourceConfiguration);
                    msgContext.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, pipe);
                    msgContext.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
                }
                pipe.attachConsumer(conn);
                sourceResponse.connect(pipe);
            }
            Integer errorCode = (Integer) msgContext.getProperty(PassThroughConstants.ERROR_CODE);
            if (errorCode != null) {
                sourceResponse.setStatus(Constants.BAD_GATEWAY);
                SourceContext.get(conn).setShutDown(true);
            }
            ProtocolState state = SourceContext.getState(conn);
            if (state != null && state.compareTo(ProtocolState.REQUEST_DONE) <= 0) {
                boolean noEntityBodyResponse = false;
                OutputStream out;
                if (noEntityBody != null && Boolean.TRUE == noEntityBody && pipe != null) {
                    out = pipe.getOutputStream();
                    out.write(new byte[0]);
                    pipe.setRawSerializationComplete(true);
                    out.close();
                    noEntityBodyResponse = true;
                }
                if (!noEntityBodyResponse && msgContext.isPropertyTrue(PassThroughConstants.MESSAGE_BUILDER_INVOKED)
                        && pipe != null) {
                    out = pipe.getOutputStream();
                    MessageFormatter formatter = MessageFormatterDecoratorFactory.createMessageFormatterDecorator(
                            msgContext);
                    OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgContext);
                    boolean initResponseComplete = false;
                    try {
                        while (!response.isComplete()) {
                            if (setContent(msgContext, response)) {
                                if (!initResponseComplete) {
                                    initResponse(msgContext, response, formatter, format, sourceResponse);
                                    initResponseComplete = true;
                                }
                                formatter.writeTo(msgContext, format, out, false);
                            }
                        }
                    } catch (RemoteException e) {
                        IOUtils.closeQuietly(out);
                        throw new SynapseException("Error occurred while building the message context.", e);
                    } finally {
                        pipe.setSerializationComplete(true);
                        out.close();
                        response.close();
                    }
                }
                conn.requestOutput();
            } else {
                if (errorCode != null) {
                    if (this.log.isDebugEnabled()) {
                        this.log.warn("A Source connection is closed because of an error in target: " + conn);
                    }
                } else {
                    this.log.debug(
                            "A Source Connection is closed, because source handler is already in the process of writing a response while another response is submitted: "
                                    + conn);
                }
                pipe.consumerError();
                SourceContext.updateState(conn, ProtocolState.CLOSED);
                sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
            }
        }
    }

    /**
     * This method sets the content type and the status code of the source response.
     *
     * @param msgContext     Message context.
     * @param response       OData servlet response.
     * @param formatter      Message formatter to serialize the message.
     * @param format         Output format.
     * @param sourceResponse Source response.
     */
    private void initResponse(MessageContext msgContext, ODataServletResponse response, MessageFormatter formatter,
                              OMOutputFormat format, SourceResponse sourceResponse) {
        msgContext.setProperty(PassThroughConstants.HTTP_SC, response.getStatus());
        if (response.getContentType() != null && response.getContentType().contains(MediaType.APPLICATION_JSON)) {
            msgContext.setProperty(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        } else if (response.getContentType() != null && response.getContentType().contains(MediaType.APPLICATION_XML)) {
            msgContext.setProperty(CONTENT_TYPE, MediaType.APPLICATION_XML);
        }
        this.setContentType(msgContext, sourceResponse, formatter, format);
        sourceResponse.setStatus(response.getStatus());
    }

    /**
     * This method builds the SOAP envelope according to the state of the response.
     *
     * @param axis2MessageContext Message context.
     * @param response            OData Servlet response.
     * @return a boolean value to notify whether any content is added to the message context or not.
     * @throws IOException if any interrupted I/O operations occurred while reading the response or building the
     *                     message envelope.
     */
    private boolean setContent(org.apache.axis2.context.MessageContext axis2MessageContext,
                               ODataServletResponse response) throws IOException {
        if (isInvalidResponse(response) && !response.startStream()) {
            setMessageEnvelope(axis2MessageContext, StringUtils.EMPTY);
            response.forceComplete();
            return true;
        }
        String content = response.getContentAsString();
        if (response.startStream() && StringUtils.isNotEmpty(content)) {
            setMessageEnvelope(axis2MessageContext, content);
            return true;
        }
        return false;
    }

    /**
     * This method builds the SOAP envelope using the given string content.
     *
     * @param axis2MessageContext Message context.
     * @param content             Content to be written to the message.
     * @throws AxisFault if any SOAP fault occurred while building the message envelop.
     */
    private void setMessageEnvelope(MessageContext axis2MessageContext, String content) throws AxisFault {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(getTextElement(content));
        axis2MessageContext.setEnvelope(envelope);
    }

    /**
     * Checks for unsuccessful responses or responses without a body.
     *
     * @param response OData servlet response.
     * @return true if the response is an error response or a success response with an empty response body. Otherwise,
     * return false.
     */
    private boolean isInvalidResponse(ODataServletResponse response) {
        int statusCode = response.getStatus();
        return statusCode != 0 && ((statusCode < 200 || statusCode >= 300) || statusCode == Constants.NO_CONTENT);
    }

    /**
     * This method builds an OMElement using the given text.
     *
     * @param content Content to be written to the message.
     * @return an OMElement with the given string content.
     */
    private OMElement getTextElement(String content) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement textElement = factory.createOMElement(new QName("http://ws.apache.org/commons/ns/payload", "text"));
        if (content == null) {
            content = StringUtils.EMPTY;
        }
        textElement.setText(content);
        return textElement;
    }

    /**
     * This method sets the content type headers of the source response.
     *
     * @param msgContext     Message context.
     * @param sourceResponse Source response.
     * @param formatter      Message formatter to serialize the message.
     * @param format         Output format.
     */
    public void setContentType(MessageContext msgContext, SourceResponse sourceResponse, MessageFormatter formatter,
                               OMOutputFormat format) {
        Object contentTypeInMsgCtx = msgContext.getProperty(CONTENT_TYPE);
        boolean isContentTypeSetFromMsgCtx = false;
        if (contentTypeInMsgCtx != null) {
            String contentTypeValueInMsgCtx = contentTypeInMsgCtx.toString();
            if (!contentTypeValueInMsgCtx.contains(PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED)
                    && !contentTypeValueInMsgCtx.contains(PassThroughConstants.CONTENT_TYPE_MULTIPART_FORM_DATA)) {
                if (format != null && contentTypeValueInMsgCtx.indexOf(HTTPConstants.CHAR_SET_ENCODING) == -1
                        && !"false".equals(msgContext.getProperty(PassThroughConstants.SET_CHARACTER_ENCODING))) {
                    String encoding = format.getCharSetEncoding();
                    if (encoding != null) {
                        contentTypeValueInMsgCtx = contentTypeValueInMsgCtx + "; charset=" + encoding;
                    }
                }
                sourceResponse.removeHeader(HTTP.CONTENT_TYPE);
                sourceResponse.addHeader(HTTP.CONTENT_TYPE, contentTypeValueInMsgCtx);
                isContentTypeSetFromMsgCtx = true;
            }
        }
        if (!isContentTypeSetFromMsgCtx) {
            sourceResponse.removeHeader(HTTP.CONTENT_TYPE);
            sourceResponse.addHeader(HTTP.CONTENT_TYPE,
                                     formatter.getContentType(msgContext, format, msgContext.getSoapAction()));
        }
    }

    /**
     * This method sets the servlet response.
     *
     * @param response OData Servlet response.
     */
    public void setResponse(ODataServletResponse response) {
        this.response = response;
    }
}