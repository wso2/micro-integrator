package org.wso2.carbon.inbound.endpoint.protocol.cdc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.wso2.carbon.inbound.endpoint.persistence.PersistenceUtils;

import static org.wso2.carbon.inbound.endpoint.protocol.cdc.InboundCDCConstants.INBOUND_ENDPOINT_PARAMETER_CDC_PORT;

public class CDCListener implements InboundRequestProcessor {

    private InboundProcessorParams processorParams;
    private int port;
    private String name;
    private static final Log LOGGER = LogFactory.getLog(CDCListener.class);

    public CDCListener(InboundProcessorParams params) {

        processorParams = params;
        String portParam = params.getProperties()
                .getProperty(INBOUND_ENDPOINT_PARAMETER_CDC_PORT);
        try {
            port = Integer.parseInt(portParam);
        } catch (NumberFormatException e) {
            handleException("Validation failed for the port parameter " + portParam, e);
        }
        name = params.getName();
    }

    protected void handleException(String msg, Exception e) {
        LOGGER.error(msg, e);
        throw new SynapseException(msg, e);
    }

    @Override
    public void init() {
        System.out.println("Init called");
        int offsetPort = port + PersistenceUtils.getPortOffset(processorParams.getProperties());
        CDCEndpointManager.getInstance().startEndpoint(offsetPort, name, processorParams);
    }

    @Override
    public void destroy() {
        System.out.println("Destroy called");
        int offsetPort = port + PersistenceUtils.getPortOffset(processorParams.getProperties());
        CDCEndpointManager.getInstance().closeEndpoint(offsetPort);
    }
}
