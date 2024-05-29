package org.wso2.micro.integrator.mediator.sampleAI;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;
import java.util.Properties;

public class SampleAIMediatorFactory extends AbstractMediatorFactory {
    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        return new SampleAIMediator();
    }

    @Override
    public QName getTagQName() {
        return new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "SampleAI");
    }
}
