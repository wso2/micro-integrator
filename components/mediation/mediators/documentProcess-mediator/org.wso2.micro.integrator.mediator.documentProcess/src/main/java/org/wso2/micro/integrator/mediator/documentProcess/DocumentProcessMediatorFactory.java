package org.wso2.micro.integrator.mediator.documentProcess;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;

import javax.xml.namespace.QName;
import java.util.Properties;

public class DocumentProcessMediatorFactory extends AbstractMediatorFactory {

    /**
     * QName of the collector.
     */
    private static final QName API_KEY = new QName(DocumentProcessConstants.API_KEY_STRING);
    private static final QName JSON_FILE_PATH = new QName(DocumentProcessConstants.JSON_FILE_PATH_STRING);
    private static final QName MAX_TOKENS = new QName(DocumentProcessConstants.MAX_TOKENS_STRING);
    private static final QName GPT_MODEL = new QName(DocumentProcessConstants.GPT_MODEL_STRING);

    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        if (!DocumentProcessConstants.VISIONAI_Q.equals(omElement.getQName())) {
            handleException(
                    "Unable to create the vision AI mediator. Unexpected element as the vision AI mediator configuration");
        }
        DocumentProcessMediator documentProcessMediator = new DocumentProcessMediator();

        OMAttribute apiKey = omElement.getAttribute(API_KEY);
        if (apiKey != null && apiKey.getAttributeValue() != null) {
            documentProcessMediator.setApiKey(apiKey.getAttributeValue());
        } else {
            handleException("Unable to find attribute " + API_KEY.getLocalPart());
        }

        OMAttribute jsonFilePath = omElement.getAttribute(JSON_FILE_PATH);
        if(jsonFilePath != null && jsonFilePath.getAttributeValue() != null) {
            documentProcessMediator.setJsonFilePath(jsonFilePath.getAttributeValue());
        } else {
            handleException("Unable to find attribute " + JSON_FILE_PATH.getLocalPart());
        }

        OMAttribute maxTokens = omElement.getAttribute(MAX_TOKENS);
        if(maxTokens != null && maxTokens.getAttributeValue() != null){
            documentProcessMediator.setMaxTokens(Integer.parseInt(maxTokens.getAttributeValue().trim()));
        }
        else {
            documentProcessMediator.setMaxTokens(DocumentProcessConstants.DEFAULT_TOKENS);
        }

        OMAttribute gptModel = omElement.getAttribute(GPT_MODEL);
        if(gptModel != null && gptModel.getAttributeValue() != null){
            documentProcessMediator.setGptModel(gptModel.getAttributeValue());
        }
        else {
            documentProcessMediator.setGptModel(DocumentProcessConstants.DEFAULT_GPT_MODEL);
        }
        return documentProcessMediator;
    }

    /**
     * {@inheritDoc}
     */
    public QName getTagQName() {
        return DocumentProcessConstants.VISIONAI_Q;
    }
}
