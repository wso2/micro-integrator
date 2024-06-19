package org.wso2.micro.integrator.mediator.documentProcess;

import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

public class DocumentProcessConstants {
    /**
     * QName of the cache mediator which will be used by the module.
     */
    public static final QName VISIONAI_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            DocumentProcessConstants.VISIONAI_LOCAL_NAME);

    /**
     * Local name of the visionAI mediator which will be used by the module.
     */
    public static final String VISIONAI_LOCAL_NAME = "vision";

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
    public static final String API_KEY_STRING = "api";
    public static final String JSON_FILE_PATH_STRING = "json";
    public static final String MAX_TOKENS_STRING = "tokens";
    public static final String GPT_MODEL_STRING = "model";
}
