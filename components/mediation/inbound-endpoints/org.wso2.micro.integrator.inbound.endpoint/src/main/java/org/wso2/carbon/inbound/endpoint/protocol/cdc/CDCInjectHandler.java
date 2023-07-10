/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import io.debezium.engine.ChangeEvent;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.mediators.MediatorFaultHandler;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.customlogsetter.CustomLogSetter;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericConstants;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.DATABASE_NAME;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.OPERATIONS;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TABLES;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.TS_MS;

public class CDCInjectHandler {

    private static final Log logger = LogFactory.getLog(CDCInjectHandler.class);

    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    private Properties cdcProperties;
    private SynapseEnvironment synapseEnvironment;
    private Map<String, Object> transportHeaders;
    private static final String contentType = "application/json";

    public CDCInjectHandler(String injectingSeq, String onErrorSeq, boolean sequential,
                            SynapseEnvironment synapseEnvironment, Properties cdcProperties) {
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.sequential = sequential;
        this.synapseEnvironment = synapseEnvironment;
        this.cdcProperties = cdcProperties;
    }

    /**
     * Inject the message to the sequence
     */
    public boolean invoke(Object object, String inboundEndpointName) throws SynapseException {

        ChangeEvent<String, String> eventRecord = (ChangeEvent<String, String>) object;
        if (eventRecord == null || eventRecord.value() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("CDC Source Handler received empty event record");
            }
        } else {
            InputStream in = null;
            try {
                org.apache.synapse.MessageContext msgCtx = createMessageContext();
                msgCtx.setProperty(SynapseConstants.INBOUND_ENDPOINT_NAME, inboundEndpointName);
                msgCtx.setProperty(SynapseConstants.ARTIFACT_NAME, SynapseConstants.FAIL_SAFE_MODE_INBOUND_ENDPOINT + inboundEndpointName);
                msgCtx.setProperty(SynapseConstants.IS_INBOUND, true);
                InboundEndpoint inboundEndpoint = msgCtx.getConfiguration().getInboundEndpoint(inboundEndpointName);
                CustomLogSetter.getInstance().setLogAppender(inboundEndpoint.getArtifactContainerName());

                CDCEventOutput cdcEventOutput = new CDCEventOutput(eventRecord);
                msgCtx.setProperty(DATABASE_NAME, cdcEventOutput.getDatabase());
                msgCtx.setProperty(TABLES, cdcEventOutput.getTable().toString());
                msgCtx.setProperty(OPERATIONS, cdcEventOutput.getOp());
                msgCtx.setProperty(TS_MS, cdcEventOutput.getTs_ms().toString());

                if (logger.isDebugEnabled()) {
                    logger.debug("Processed event : " + eventRecord);
                }
                MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                        .getAxis2MessageContext();

                //Builder builder = null;
                OMElement documentElement = null;
                try {
                    documentElement = JsonUtil.getNewJsonPayload(axis2MsgCtx,
                            cdcEventOutput.getOutputJsonPayload().toString(), true, true);

                } catch (AxisFault ex) {
                    logger.error("Error while creating the OMElement", ex);
                    msgCtx.setProperty(SynapseConstants.ERROR_CODE, GenericConstants.INBOUND_BUILD_ERROR);
                    msgCtx.setProperty(SynapseConstants.ERROR_MESSAGE, ex.getMessage());
                    SequenceMediator faultSequence = getFaultSequence(msgCtx);
                    faultSequence.mediate(msgCtx);
                    return true;
                }

                // Inject the message to the sequence.
                msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
                if (injectingSeq == null || injectingSeq.equals("")) {
                    logger.error("Sequence name not specified. Sequence : " + injectingSeq);
                    return false;
                }
                SequenceMediator seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration()
                        .getSequence(injectingSeq);
                if (seq != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("injecting message to sequence : " + injectingSeq);
                    }
                    if (!seq.isInitialized()) {
                        seq.init(synapseEnvironment);
                    }
                    SequenceMediator faultSequence = getFaultSequence(msgCtx);
                    MediatorFaultHandler mediatorFaultHandler = new MediatorFaultHandler(faultSequence);
                    msgCtx.pushFaultHandler(mediatorFaultHandler);

                    if (!synapseEnvironment.injectInbound(msgCtx, seq, sequential)) {
                        return false;
                    }
                } else {
                    logger.error("Sequence: " + injectingSeq + " not found");
                }

            } catch (AxisFault e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private SequenceMediator getFaultSequence(org.apache.synapse.MessageContext synCtx) {
        SequenceMediator faultSequence = null;
        if (this.onErrorSeq != null) {
            faultSequence = (SequenceMediator) synCtx.getSequence(this.onErrorSeq);
        }

        if (faultSequence == null) {
            faultSequence = (SequenceMediator) synCtx.getFaultSequence();
        }

        return faultSequence;
    }


    /**
     * @param transportHeaders the transportHeaders to set
     */
    public void setTransportHeaders(Map<String, Object> transportHeaders) {
        this.transportHeaders = transportHeaders;
    }

    /**
     * Create the initial message context for the file
     */
    private org.apache.synapse.MessageContext createMessageContext() {

        org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
        axis2MsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, transportHeaders);
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
        return msgCtx;
    }
}
