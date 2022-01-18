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

package org.wso2.carbon.inbound.endpoint.protocol.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.ThreadContext;
import org.apache.synapse.commons.CorrelationConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;

import java.io.OutputStream;

/**
 * Extends {@link InboundHttpServerWorker} with correlation logs enabled.
 */
public class InboundCorrelationEnabledHttpServerWorker extends InboundHttpServerWorker {

    // logger for correlation.log
    private static final Log correlationLog = LogFactory.getLog(PassThroughConstants.CORRELATION_LOGGER);

    private long initiationTimestamp;
    private String correlationId;

    InboundCorrelationEnabledHttpServerWorker(int port, String tenantDomain, SourceRequest sourceRequest,
                                              SourceConfiguration sourceConfiguration, OutputStream outputStream,
                                              long initiationTimestamp, String correlationId) {
        super(port, tenantDomain, sourceRequest, sourceConfiguration, outputStream);
        this.initiationTimestamp = initiationTimestamp;
        this.correlationId = correlationId;
    }

    @Override
    public void run() {
        // Reset the correlation id MDC thread local value.
        ThreadContext.remove(CorrelationConstants.CORRELATION_MDC_PROPERTY);
        ThreadContext.put(CorrelationConstants.CORRELATION_MDC_PROPERTY, correlationId);
        // Log the time taken to switch from the previous thread to this thread
        correlationLog.info((System.currentTimeMillis() - initiationTimestamp) + "|Thread switch latency");
        super.run();
    }
}
