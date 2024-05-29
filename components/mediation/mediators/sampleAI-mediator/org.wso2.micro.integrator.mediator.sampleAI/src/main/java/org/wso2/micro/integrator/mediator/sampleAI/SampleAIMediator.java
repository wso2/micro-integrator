package org.wso2.micro.integrator.mediator.sampleAI;

import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.MessageContext;
public class SampleAIMediator extends AbstractMediator {

    public boolean mediate(MessageContext synCtx) {
        System.out.println("SampleAI Mediator");
        return true;
    }
}
