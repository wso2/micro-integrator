package org.wso2.micro.integrator.mediator.visionAI;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;
import java.util.Properties;

public class VisionAIMediatorFactory extends AbstractMediatorFactory {

    /**
     * QName of the collector.
     */
    private static final QName API_KEY = new QName(VisionAIConstants.API_KEY);
    private static final QName JSON_FILE_PATH = new QName(VisionAIConstants.JSON_FILE_PATH);

    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        if (!VisionAIConstants.VISIONAI_Q.equals(omElement.getQName())) {
            handleException(
                    "Unable to create the vision AI mediator. Unexpected element as the vision AI mediator configuration");
        }
        VisionAIMediator visionAIMediator = new VisionAIMediator();

        OMAttribute apiKey = omElement.getAttribute(API_KEY);
        if (apiKey != null && apiKey.getAttributeValue() != null) {
            visionAIMediator.setApiKey(apiKey.getAttributeValue());
        } else {
            handleException("Unable to find attribute " + API_KEY.getLocalPart());
        }

        OMAttribute jsonFilePath = omElement.getAttribute(JSON_FILE_PATH);
        if(jsonFilePath != null && jsonFilePath.getAttributeValue() != null) {
            visionAIMediator.setJsonFilePath(jsonFilePath.getAttributeValue());
        } else {
            handleException("Unable to find attribute " + JSON_FILE_PATH.getLocalPart());
        }
        return visionAIMediator;
    }

    @Override
    public QName getTagQName() {
        return new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "visionAI");
    }
}
