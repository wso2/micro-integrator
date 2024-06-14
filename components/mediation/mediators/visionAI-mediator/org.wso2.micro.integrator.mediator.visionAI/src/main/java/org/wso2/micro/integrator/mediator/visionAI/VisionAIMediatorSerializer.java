package org.wso2.micro.integrator.mediator.visionAI;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;

public class VisionAIMediatorSerializer extends AbstractMediatorSerializer {
    @Override
    protected OMElement serializeSpecificMediator(Mediator mediator) {
        return null;
    }

    @Override
    public String getMediatorClassName() {
        return null;
    }
}
