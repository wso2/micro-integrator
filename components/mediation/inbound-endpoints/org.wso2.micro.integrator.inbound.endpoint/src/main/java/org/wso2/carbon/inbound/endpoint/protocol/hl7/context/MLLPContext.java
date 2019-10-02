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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.context;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.EventMask;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.codec.HL7Codec;

import java.nio.charset.CharsetDecoder;

public class MLLPContext {
    private static final Log log = LogFactory.getLog(MLLPContext.class);

    private IOSession session;
    private StringBuffer requestBuffer;
    private StringBuffer responseBuffer;
    private Message hl7Message;
    private volatile HL7Codec codec;
    private long requestTime;
    private int expiry;
    private boolean validateMessage;

    private volatile boolean autoAck = true;
    private volatile boolean nackMode = false;
    private volatile boolean markForClose = false;
    private boolean preProcess = true;
    private boolean applicationAck = false;

    private volatile String messageId;

    private Parser preProcessorParser = null;
    private BufferFactory bufferFactory;

    public MLLPContext(IOSession session, CharsetDecoder decoder, boolean autoAck, boolean validateMessage,
                       Parser preProcessorParser, BufferFactory bufferFactory) {
        this.session = session;
        this.codec = new HL7Codec(decoder);
        this.autoAck = autoAck;
        this.validateMessage = validateMessage;
        this.preProcessorParser = preProcessorParser;
        this.bufferFactory = bufferFactory;
        this.expiry = MLLPConstants.DEFAULT_HL7_TIMEOUT;
        this.requestBuffer = new StringBuffer();
        this.responseBuffer = new StringBuffer();

        if (preProcessorParser == null) {
            preProcess = false;
        }

    }

    public HL7Codec getCodec() {
        return codec;
    }

    public StringBuffer getRequestBuffer() {
        return this.requestBuffer;
    }

    public StringBuffer getResponseBuffer() {
        return responseBuffer;
    }

    public Message getHl7Message() {
        return this.hl7Message;
    }

    public void setHl7Message(Message hl7Message) {
        this.hl7Message = hl7Message;
    }

    public void requestOutput() {
        session.clearEvent(EventMask.READ);
        session.setEvent(EventMask.WRITE);
    }

    public void requestInput() {
        session.clearEvent(EventMask.WRITE);
        session.setEvent(EventMask.READ);
    }

    public void setRequestTime(long timeStamp) {
        this.requestTime = timeStamp;
    }

    public long getRequestTime() {
        return this.requestTime;
    }

    public void setExpiry(int milliseconds) {
        if (milliseconds < 1000) {
            milliseconds = 1000;
        }

        this.expiry = milliseconds;
    }

    public boolean isExpired() {
        if (System.currentTimeMillis() > requestTime + expiry) {
            return true;
        }

        return false;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    public boolean isApplicationAck() {
        return applicationAck;
    }

    public void setApplicationAck(boolean applicationAck) {
        this.applicationAck = applicationAck;
    }

    public boolean isPreProcess() {
        return preProcess;
    }

    public boolean isNackMode() {
        return nackMode;
    }

    public void setNackMode(boolean nackMode) {
        this.nackMode = nackMode;
    }

    public Parser getPreProcessParser() {
        return preProcessorParser;
    }

    public BufferFactory getBufferFactory() {
        return bufferFactory;
    }

    public boolean isValidateMessage() {
        return validateMessage;
    }

    public void setValidateMessage(boolean validateMessage) {
        this.validateMessage = validateMessage;
    }

    public boolean isMarkForClose() {
        return markForClose;
    }

    public void setMarkForClose(boolean markForClose) {
        this.markForClose = markForClose;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void reset() {
        // Resets MLLP Context and HL7Codec to default states.
        this.responseBuffer.setLength(0);
        this.requestBuffer.setLength(0);
        this.getCodec().setState(HL7Codec.READ_HEADER);
        this.setNackMode(false);
    }
}