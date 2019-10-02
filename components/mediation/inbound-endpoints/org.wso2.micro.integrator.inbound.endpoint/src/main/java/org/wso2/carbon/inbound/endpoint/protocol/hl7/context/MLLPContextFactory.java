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

import ca.uhn.hl7v2.parser.Parser;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.HL7Processor;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLPConstants;

import java.nio.charset.CharsetDecoder;

public class MLLPContextFactory {

    public static MLLPContext createMLLPContext(IOSession session, HL7Processor processor) {
        InboundProcessorParams inboundParams = (InboundProcessorParams) processor.getInboundParameterMap()
                .get(MLLPConstants.INBOUND_PARAMS);

        CharsetDecoder decoder = (CharsetDecoder) processor.getInboundParameterMap()
                .get(MLLPConstants.HL7_CHARSET_DECODER);
        boolean autoAck = processor.isAutoAck();
        boolean validate = Boolean.valueOf(inboundParams.getProperties().getProperty(MLLPConstants.PARAM_HL7_VALIDATE));
        Parser preParser = (Parser) processor.getInboundParameterMap().get(MLLPConstants.HL7_PRE_PROC_PARSER_CLASS);
        BufferFactory bufferFactory = (BufferFactory) processor.getInboundParameterMap()
                .get(MLLPConstants.INBOUND_HL7_BUFFER_FACTORY);

        return new MLLPContext(session, decoder, autoAck, validate, preParser, bufferFactory);
    }

}
