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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.codec;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.context.MLLPContext;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLProtocolException;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.util.HL7MessageUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;

public class HL7Codec {
    private static final Log log = LogFactory.getLog(HL7Codec.class);

    public static final int READ_HEADER = 0;
    public static final int READ_CONTENT = 1;
    public static final int READ_TRAILER = 2;
    public static final int READ_COMPLETE = 3;

    public static final int WRITE_HEADER = 4;
    public static final int WRITE_CONTENT = 5;
    public static final int WRITE_TRAILER = 6;
    public static final int WRITE_COMPLETE = 7;

    private CharsetDecoder charsetDecoder;

    private volatile int state;

    private volatile boolean firstTrailingCharacterFound = false;
    private volatile boolean lastTrailingCharacterFound = false;
    private volatile boolean addMissingCharacter = false;

    private int responseReadPosition = 0;
    private byte[] responseBytes = null;

    public HL7Codec() {
        this.state = READ_HEADER;
        this.charsetDecoder = MLLPConstants.UTF8_CHARSET.newDecoder();
    }

    public HL7Codec(CharsetDecoder charsetDecoder) {
        this.state = READ_HEADER;
        setCharsetDecoder(charsetDecoder);
    }

    public int decode(ByteBuffer dst, MLLPContext context) throws IOException, MLLProtocolException, HL7Exception {

        if (this.state >= READ_COMPLETE || dst.position() < 0) {
            return -1;
        }

        if (this.state == READ_HEADER) {
            if (dst.get(0) == MLLPConstants.HL7_HEADER[0]) {
                dst.position(1);
                this.state = READ_CONTENT;
            } else {
                throw new MLLProtocolException("Could not find header in incoming message.");
            }
        }

        if (this.state == READ_CONTENT) {

            int trailerIndex = findTrailer(dst);

            if (trailerIndex > -1) {
                dst.limit(trailerIndex);
            }
            if (lastTrailingCharacterFound) {
                this.state = READ_TRAILER;
                this.lastTrailingCharacterFound = false;
                this.firstTrailingCharacterFound = false;
            }
            if (this.addMissingCharacter) {
                context.getRequestBuffer().append(MLLPConstants.HL7_TRAILER[0]);
                this.addMissingCharacter = false;
            }

            context.getRequestBuffer().append(charsetDecoder.decode(dst).toString());
        }

        if (this.state == READ_TRAILER) {
            this.state = READ_COMPLETE;
            try {
                if (context.isPreProcess()) {
                    context.setHl7Message(HL7MessageUtils.parse(context.getRequestBuffer().toString(),
                                                                context.getPreProcessParser()));
                } else {
                    context.setHl7Message(
                            HL7MessageUtils.parse(context.getRequestBuffer().toString(), context.isValidateMessage()));
                }
                context.getRequestBuffer().setLength(0);
            } catch (HL7Exception e) {
                log.error("Error while parsing request message: " + context.getRequestBuffer());
                throw e;
            }
        }

        return 0;

    }

    private int findTrailer(ByteBuffer dst) {
            if (firstTrailingCharacterFound) {
                firstTrailingCharacterFound = false;
                if (dst.get(0) == MLLPConstants.HL7_TRAILER[1]) {
                    lastTrailingCharacterFound = true;
                    return 0;
                } else {
                    addMissingCharacter = true;
                }
            }
            for (int i = 0; i < dst.limit(); i++) {
                if (dst.get(i) == MLLPConstants.HL7_TRAILER[0]) {
                    if (i + 1 >= dst.limit()) {
                        this.firstTrailingCharacterFound = true;
                        return i - 1;
                    } else if (dst.get(i + 1) == MLLPConstants.HL7_TRAILER[1]) {
                        lastTrailingCharacterFound = true;
                        return i - 1 < 0 ? 0 : i - 1;
                }
            }
        }

        return -1;
    }

    public int encode(ByteBuffer outBuf, MLLPContext context) throws HL7Exception, IOException {

        if (this.state < READ_COMPLETE) {
            return 0;
        }

        if (this.state == READ_COMPLETE) {

            if ((context.isAutoAck() || context.isApplicationAck()) && !context.isNackMode()) {
                responseBytes = context.getHl7Message().generateACK().encode().getBytes(charsetDecoder.charset());
                context.setApplicationAck(false);
            } else {
                responseBytes = context.getHl7Message().encode().getBytes(charsetDecoder.charset());
            }

            this.state = WRITE_HEADER;
        }

        if (this.state >= WRITE_HEADER) {
            return fillBuffer(outBuf, responseBytes);
        }

        return 0;
    }

    private int fillBuffer(ByteBuffer byteBuffer, byte[] responseBytes) {
        if (responseBytes == null) {
            return 0;
        }

        byte b;
        int count = 0;
        int headerPosition = 0;

        if (this.state == WRITE_HEADER) {
            byteBuffer.put(MLLPConstants.HL7_HEADER[0]);
            headerPosition = 1;
            this.state = WRITE_CONTENT;
        }

        int MAX = byteBuffer.capacity();
        if (byteBuffer.capacity() - (responseBytes.length - responseReadPosition + headerPosition) > 0) {
            MAX = responseBytes.length - responseReadPosition + headerPosition;
        }

        for (int i = responseReadPosition; i < MAX + responseReadPosition - headerPosition; i++) {
            count++;
            b = responseBytes[i];
            byteBuffer.put(b);
        }

        responseReadPosition += count;

        if (responseReadPosition == responseBytes.length) {
            this.state = WRITE_TRAILER;
            responseReadPosition = 0;
        }

        byteBuffer.flip();
        return count;
    }

    public boolean isReadComplete() {
        if (this.state >= READ_COMPLETE) {
            return true;
        }

        return false;
    }

    public boolean isWriteTrailer() {
        if (this.state == WRITE_TRAILER) {
            return true;
        }

        return false;
    }

    public boolean isWriteComplete() {
        if (this.state == WRITE_COMPLETE) {
            return true;
        }

        return false;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public CharsetDecoder getCharsetDecoder() {
        return charsetDecoder;
    }

    private void setCharsetDecoder(CharsetDecoder charsetDecoder) {
        this.charsetDecoder = charsetDecoder;
    }
}
