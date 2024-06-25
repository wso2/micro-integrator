package org.wso2.micro.integrator.mediator.documentProcess;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;
import java.util.Properties;

public class DocumentProcessMediatorFactory extends AbstractMediatorFactory {

    /**
     * QName of the api-key.
     */
    private static final QName API_KEY = new QName(DocumentProcessConstants.API_KEY_STRING);

    /**
     * QName of the schema child
     */
    private static final QName SCHEMA = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,DocumentProcessConstants.SCHEMA_STRING);

    /**
     * QName of the max-tokens
     */
    private static final QName MAX_TOKENS = new QName(DocumentProcessConstants.MAX_TOKENS_STRING);

    /**
     * QName of the gpt-model
     */
    private static final QName GPT_MODEL = new QName(DocumentProcessConstants.GPT_MODEL_STRING);

    /**
     * QName of the schema key
     */
    private static final QName SCHEMA_KEY = new QName(DocumentProcessConstants.SCHEMA_KEY_STRING);

    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        if (!DocumentProcessConstants.DOCUMENTPROCESS_Q.equals(omElement.getQName())) {
            handleException(
                    "Unable to create the Document Process mediator. Unexpected element as the Document Process mediator configuration");
        }
        DocumentProcessMediator documentProcessMediator = new DocumentProcessMediator();

        OMAttribute apiKey = omElement.getAttribute(API_KEY);
        if (apiKey != null && apiKey.getAttributeValue() != null) {
            documentProcessMediator.setApiKey(apiKey.getAttributeValue());
        } else {
            handleException("Unable to find attribute " + API_KEY.getLocalPart());
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

        OMElement schema = omElement.getFirstChildWithName(SCHEMA);
        if(schema != null){
            OMAttribute schemaRegistryKey = schema.getAttribute(SCHEMA_KEY);
            if(schemaRegistryKey != null && schemaRegistryKey.getAttributeValue() != null){
                documentProcessMediator.setSchemaPath(schemaRegistryKey.getAttributeValue());
            } else {
                documentProcessMediator.setSchemaPath("");
            }
        } else {
            documentProcessMediator.setSchemaPath("");
        }
        return documentProcessMediator;
    }

    /**
     * {@inheritDoc}
     */
    public QName getTagQName() {
        return DocumentProcessConstants.DOCUMENTPROCESS_Q;
    }
}
