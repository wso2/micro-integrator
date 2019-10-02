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

package org.wso2.carbon.inbound.endpoint.protocol.hl7.core;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.apache.synapse.transport.passthru.util.ControlledByteBuffer;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.codec.HL7Codec;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.context.MLLPContext;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.context.MLLPContextFactory;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.util.HL7MessageUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MLLPSourceHandler implements IOEventDispatch {
    private static final Log log = LogFactory.getLog(MLLPSourceHandler.class);

    private volatile HL7Processor hl7Processor;

    private final ByteBuffer hl7TrailerBuf = ByteBuffer.wrap(MLLPConstants.HL7_TRAILER);
    private BufferFactory bufferFactory;
    private ControlledByteBuffer inputBuffer;
    private ControlledByteBuffer outputBuffer;

    public MLLPSourceHandler() { /* default constructor */ }

    public MLLPSourceHandler(HL7Processor hl7Processor) {
        super();
        this.hl7Processor = hl7Processor;
        this.bufferFactory = (BufferFactory) hl7Processor.getInboundParameterMap()
                .get(MLLPConstants.INBOUND_HL7_BUFFER_FACTORY);
    }

    @Override
    public void connected(IOSession session) {
        if (session.getAttribute(MLLPConstants.MLLP_CONTEXT) == null) {
            session.setAttribute(MLLPConstants.MLLP_CONTEXT,
                                 MLLPContextFactory.createMLLPContext(session, hl7Processor));
        }

        inputBuffer = bufferFactory.getBuffer();
        outputBuffer = bufferFactory.getBuffer();
    }

    @Override
    public void inputReady(IOSession session) {
        ReadableByteChannel ch = (ReadableByteChannel) session.channel();

        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);

        inputBuffer.clear();
        try {
            int read;
            while ((read = ch.read(inputBuffer.getByteBuffer())) > 0) {
                inputBuffer.flip();
                try {
                    mllpContext.getCodec().decode(inputBuffer.getByteBuffer(), mllpContext);
                } catch (MLLProtocolException e) {
                    handleException(session, mllpContext, e);
                    clearInputBuffers(mllpContext);
                    return;
                } catch (HL7Exception e) {
                    handleException(session, mllpContext, e);
                    if (mllpContext.isAutoAck()) {
                        mllpContext.setNackMode(true);
                        mllpContext.setHl7Message(HL7MessageUtils.createDefaultNack(e.getMessage()));
                        mllpContext.requestOutput();
                    } else {
                        hl7Processor.processError(mllpContext, e);
                    }
                    return;
                } catch (IOException e) {
                    shutdownConnection(session, mllpContext, e);
                    return;
                }
            }

            if (mllpContext.getCodec().isReadComplete()) {
                if (mllpContext.isAutoAck()) {
                    mllpContext.requestOutput();
                    bufferFactory.release(inputBuffer);
                    inputBuffer = bufferFactory.getBuffer();
                }
                try {
                    hl7Processor.processRequest(mllpContext);
                } catch (Exception e) {
                    shutdownConnection(session, mllpContext, e);
                }
            }

            if (read < 0) {
                clearInputBuffers(mllpContext);
                session.close();
            }

        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

    }

    private void clearInputBuffers(MLLPContext context) {
        bufferFactory.release(inputBuffer);
        inputBuffer = bufferFactory.getBuffer();
        context.reset();
    }

    @Override
    public void outputReady(IOSession session) {
        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);
        writeOut(session, mllpContext);
    }

    private void writeOut(IOSession session, MLLPContext mllpContext) {

        outputBuffer.clear();
        try {
            mllpContext.getCodec().encode(outputBuffer.getByteBuffer(), mllpContext);
        } catch (HL7Exception e) {
            shutdownConnection(session, mllpContext, e);
        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

        if (outputBuffer == null) {
            handleException(session, mllpContext, new MLLProtocolException(
                    "HL7 Codec is in an inconsistent state: " + mllpContext.getCodec().getState()
                            + ". Shutting down connection."));
            return;
        }

        try {
            session.channel().write(outputBuffer.getByteBuffer());
            if (mllpContext.getCodec().isWriteTrailer()) {
                session.channel().write(hl7TrailerBuf);
                hl7TrailerBuf.flip();
                mllpContext.getCodec().setState(HL7Codec.WRITE_COMPLETE);
            }
            //            bufferFactory.release(outputBuffer);
        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

        if (mllpContext.getCodec().isWriteComplete()) {
            if (mllpContext.isMarkForClose()) {
                shutdownConnection(session, mllpContext, null);
            } else {
                bufferFactory.release(outputBuffer);
                outputBuffer = bufferFactory.getBuffer();
                mllpContext.setMessageId("RESPONDED");
                mllpContext.reset();
                mllpContext.requestInput();
            }
        }

    }

    @Override
    public void timeout(IOSession session) {
        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);
        shutdownConnection(session, mllpContext, null);
    }

    @Override
    public void disconnected(IOSession session) {
        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);
        shutdownConnection(session, mllpContext, null);
    }

    private void shutdownConnection(IOSession session, MLLPContext mllpContext, Exception e) {
        if (e != null) {
            log.error("An unexpected error has occurred.");
            handleException(session, mllpContext, e);
        }

        bufferFactory.release(inputBuffer);
        bufferFactory.release(outputBuffer);
        session.close();
    }

    private void handleException(IOSession session, MLLPContext mllpContext, Exception e) {
        log.error("Exception caught in I/O handler.", e);
    }
}
