/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.micro.integrator.mediator.documentProcess;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;

import javax.xml.namespace.QName;
import java.util.Properties;

public class DocumentProcessMediatorFactory extends AbstractMediatorFactory {

    /**
     * QName of the api-key.
     */
    private static final QName API_KEY = new QName(DocumentProcessConstants.API_KEY_STRING);

    /**
     * QName of the schema
     */
    private static final QName SCHEMA = new QName( DocumentProcessConstants.SCHEMA_STRING);

    /**
     * QName of the max-tokens
     */
    private static final QName MAX_TOKENS = new QName(DocumentProcessConstants.MAX_TOKENS_STRING);

    /**
     * QName of the gpt-model
     */
    private static final QName GPT_MODEL = new QName(DocumentProcessConstants.GPT_MODEL_STRING);


    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        if (!DocumentProcessConstants.DOCUMENTPROCESS_Q.equals(omElement.getQName())) {
            handleException(
                    "Unable to create the Document Process mediator. Unexpected element as the Document Process " +
                            "mediator configuration");
        }
        DocumentProcessMediator documentProcessMediator = new DocumentProcessMediator();

        OMAttribute apiKey = omElement.getAttribute(API_KEY);
        if (apiKey != null && apiKey.getAttributeValue() != null) {
            documentProcessMediator.setApiKey(apiKey.getAttributeValue());
        } else {
            handleException("Unable to find attribute " + API_KEY.getLocalPart());
        }

        OMAttribute maxTokens = omElement.getAttribute(MAX_TOKENS);
        if (maxTokens != null && maxTokens.getAttributeValue() != null) {
            int maxTokensValue = Integer.parseInt(maxTokens.getAttributeValue().trim());
            if (maxTokensValue > 0) {
                documentProcessMediator.setMaximumChatGptTokens(maxTokensValue);
            } else {
                handleException("Invalid number of tokens " + MAX_TOKENS.getLocalPart());
            }
        } else {
            documentProcessMediator.setMaximumChatGptTokens(DocumentProcessConstants.DEFAULT_TOKENS);
        }

        OMAttribute gptModel = omElement.getAttribute(GPT_MODEL);
        if (gptModel != null && gptModel.getAttributeValue() != null) {
            documentProcessMediator.setGptModel(gptModel.getAttributeValue());
        } else {
            documentProcessMediator.setGptModel(DocumentProcessConstants.DEFAULT_GPT_MODEL);
        }

        OMAttribute schema = omElement.getAttribute(SCHEMA);
        if (schema != null && schema.getAttributeValue() != null) {
            if (schema.getAttributeValue().trim().endsWith(".xsd") || schema.getAttributeValue().trim().
                    endsWith(".json") || schema.getAttributeValue().isEmpty()) {
                documentProcessMediator.setSchemaPath(schema.getAttributeValue());
            } else {
                handleException("Invalid file type, type should be xsd or json" + schema.getAttributeValue());
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
