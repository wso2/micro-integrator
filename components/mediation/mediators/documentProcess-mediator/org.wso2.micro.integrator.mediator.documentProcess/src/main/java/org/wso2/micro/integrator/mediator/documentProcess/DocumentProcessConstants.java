package org.wso2.micro.integrator.mediator.documentProcess;

import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

public class DocumentProcessConstants {
    /**
     * QName of the cache mediator which will be used by the module.
     */
    public static final QName DOCUMENTPROCESS_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            DocumentProcessConstants.DOCUMENTPROCESS_LOCAL_NAME);

    /**
     * Local name of the visionAI mediator which will be used by the module.
     */
    public static final String DOCUMENTPROCESS_LOCAL_NAME = "documentProcess";

    /**
     * This holds the default amount of GPT Tokens  .
     */
    public static final int DEFAULT_TOKENS = 500;

    /**
     * Default GPT model
     */
    public static final String DEFAULT_GPT_MODEL = "gpt-4-turbo";

    /**
     * Following names represent the local names used in QNames in MediatorFactory, Serializer and the UI
     * DocumentProcessMediator.
     */
    public static final String API_KEY_STRING = "api-key";
    public static final String SCHEMA_STRING = "schema";
    public static final String SCHEMA_KEY_STRING = "key";
    public static final String MAX_TOKENS_STRING = "max-tokens";
    public static final String GPT_MODEL_STRING = "gpt-model";
}
