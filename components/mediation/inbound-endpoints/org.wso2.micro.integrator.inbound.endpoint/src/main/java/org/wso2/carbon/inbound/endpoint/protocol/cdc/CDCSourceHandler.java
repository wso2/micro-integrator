package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import io.debezium.engine.ChangeEvent;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.api.ApiConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.mediators.MediatorFaultHandler;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.inbound.endpoint.osgi.service.ServiceReferenceHolder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;


import static org.wso2.carbon.inbound.endpoint.common.Constants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.inbound.endpoint.common.Constants.TENANT_DOMAIN;
import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.*;


public class CDCSourceHandler {

    private int port;
    private InboundProcessorParams params;
    private static final String tenantDomain = SUPER_TENANT_DOMAIN_NAME;
    private static final Log log = LogFactory.getLog(CDCSourceHandler.class);
    private static final String contentType = "application/json";

    public CDCSourceHandler(int port, InboundProcessorParams params) {
        this.port = port;
        this.params = params;
    }

    public void requestReceived(ChangeEvent eventRecord) {
        if (eventRecord == null || eventRecord.value() == null) {
            log.debug("CDC Source Handler received empty event record");
        } else {
            log.debug("CDC Source Handler request received");

            MessageContext synCtx = null;
            try {

                synCtx = getSynapseMessageContext(tenantDomain);

                CDCEventOutput cdcEventOutput = new CDCEventOutput(eventRecord);
                synCtx.setProperty(DATABASE_NAME, cdcEventOutput.getDatabase());
                synCtx.setProperty(TABLES, cdcEventOutput.getTable().toString());
                synCtx.setProperty(OPERATIONS, cdcEventOutput.getOp());
                synCtx.setProperty(TS_MS, cdcEventOutput.getTs_ms().toString());

                org.apache.axis2.context.MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) synCtx)
                        .getAxis2MessageContext();
                Builder builder = BuilderUtil.getBuilderFromSelector(contentType, axis2MsgCtx);

                if (builder != null) {
                    String serializedChangeEvent = cdcEventOutput.getOutputJsonPayload().toString();
                    InputStream in = new AutoCloseInputStream(
                            new ByteArrayInputStream(serializedChangeEvent.getBytes()));

                    OMElement documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
                    synCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));

                    if (log.isDebugEnabled()) {
                        log.debug("CDCEvent being injected to Sequence");
                    }
                    injectForMediation(synCtx);
                    return;
                }

            } catch (AxisFault e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static org.apache.axis2.context.MessageContext createAxis2MessageContext() {
        org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();
        axis2MsgCtx.setMessageID(UIDGenerator.generateURNString());
        axis2MsgCtx.setConfigurationContext(
                ServiceReferenceHolder.getInstance().getConfigurationContextService().getServerConfigContext());
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING, Boolean.TRUE);
        axis2MsgCtx.setServerSide(true);

        return axis2MsgCtx;
    }

    private static org.apache.synapse.MessageContext createSynapseMessageContext(String tenantDomain) throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MsgCtx = createAxis2MessageContext();
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);

        axis2MsgCtx.setProperty(TENANT_DOMAIN, tenantDomain);

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        axis2MsgCtx.setEnvelope(envelope);
        return MessageContextCreatorForAxis2.getSynapseMessageContext(axis2MsgCtx);
    }

    public org.apache.synapse.MessageContext getSynapseMessageContext(String tenantDomain) throws AxisFault {
        MessageContext synCtx = createSynapseMessageContext(tenantDomain);
        synCtx.setProperty(SynapseConstants.IS_INBOUND, true);
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty(SynapseConstants.IS_INBOUND, true);

        return synCtx;
    }


    private void injectForMediation(org.apache.synapse.MessageContext synCtx) {
        SequenceMediator faultSequence = getFaultSequence(synCtx);

        MediatorFaultHandler mediatorFaultHandler = new MediatorFaultHandler(faultSequence);
        synCtx.pushFaultHandler(mediatorFaultHandler);
        if (log.isDebugEnabled()) {
            log.debug("injecting message to sequence : " + params.getInjectingSeq());
        }
        synCtx.setProperty("inbound.endpoint.name", params.getName());
        synCtx.setProperty(ApiConstants.API_CALLER, params.getName());

        SequenceMediator injectingSequence = null;
        if (params.getInjectingSeq() != null) {
            injectingSequence = (SequenceMediator) synCtx.getSequence(params.getInjectingSeq());
        }
        if (injectingSequence == null) {
            injectingSequence = (SequenceMediator) synCtx.getMainSequence();
        }

        synCtx.getEnvironment().injectMessage(synCtx, injectingSequence);

    }

    private SequenceMediator getFaultSequence(org.apache.synapse.MessageContext synCtx) {
        SequenceMediator faultSequence = null;
        if (params.getOnErrorSeq() != null) {
            faultSequence = (SequenceMediator) synCtx.getSequence(params.getOnErrorSeq());
        }
        if (faultSequence == null) {
            faultSequence = (SequenceMediator) synCtx.getFaultSequence();
        }
        return faultSequence;
    }

}
