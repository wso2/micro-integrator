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

    /**
     * API Endpoint for ChatGPT
     */
    public static final String GPT_API_ENDPOINT_STRING = "https://api.openai.com/v1/chat/completions";

    /**
     * Prompt String when schema is not provided
     */
    public static final String NO_SCHEMA_PROMPT_STRING = "Retrieve all form fields and values as JSON (get XML " +
            "containing fields also not xml tags whole one as one field with actual field name in front it), return as " +
            "JSON object {fieldName:FieldValue} including all fields . The key names should match the field names " +
            "exactly and should be case sensitive. Scan the whole document and retrieve all fields.";

    /**
     * Prompt String when schema is provided
     */
    public static final String SCHEMA_PROMPT_STRING_1 = "Retrieve all form fields and  values as json (get XML " +
            "containing fields also not xml tags whole one as one field with actual field name in front it), return " +
            "as provided schema (Only values related to schema) ,";
    public static final String SCHEMA_PROMPT_STRING_2 = " as json object {fieldName:FieldValue} only fields in " +
            "schema, only return this object is enough, these are should be keys of the json under content, key " +
            "names should be same as provided, case sensitive (scan whole document and check, get all fields there " +
            "could be some lengthy fields,xml fields like value).";

    /**
     * Quality of image , converted from pdf
     * Use less dpi to save more space on the hard disk. For professional usage, you can use more than 300dpi
     */
    public static final int dpi = 300 ;



}
