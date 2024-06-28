/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.mediator.intelligent;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;

import javax.xml.namespace.QName;
import java.util.Properties;

public class IntelligentMediatorFactory extends AbstractMediatorFactory {
    private static final QName ATT_KEY = new QName(IntelligentConstants.KEY_STRING);
    private static final QName ATT_MODEL = new QName(IntelligentConstants.MODEL_STRING);
    private static final QName ATT_ENDPOINT = new QName(IntelligentConstants.ENDPOINT_STRING);
    private static final QName ATT_RETRY_COUNT = new QName(IntelligentConstants.RETRY_COUNT_STRING);
    private static final QName ATT_PROMPT = new QName(IntelligentConstants.PROMPT_STRING);
    private static final QName ATT_HEADERS = new QName(IntelligentConstants.HEADERS_STRING);
    private static final QName PAYLOAD_Q = IntelligentConstants.PAYLOAD_Q;

    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        if (!IntelligentConstants.INTELLIGENT_Q.equals(omElement.getQName())) {
            handleException("Unable to create the intelligent mediator. Unexpected element as the intelligent mediator configuration");
        }

        IntelligentMediator intelligentMediator = new IntelligentMediator();
        String key, model, endpoint, prompt, headers;
        int retryCount;

        // TODO: Key should be encrypted and decrypted during the process
        OMAttribute keyAttr = omElement.getAttribute(ATT_KEY);
        if (keyAttr != null && keyAttr.getAttributeValue() != null) {
            key = keyAttr.getAttributeValue();
            intelligentMediator.setOpenaiKey(key);
        } else {
            handleException("OpenAI key is required for the intelligent mediator");
        }

        OMAttribute taskAttr = omElement.getAttribute(ATT_PROMPT);
        if (taskAttr != null && taskAttr.getAttributeValue() != null) {
            prompt = taskAttr.getAttributeValue();
            intelligentMediator.setPrompt(prompt);
        } else {
            handleException("Prompt attribute is required for the intelligent mediator");
        }

        OMElement payloadElem = omElement.getFirstChildWithName(PAYLOAD_Q);
        if (payloadElem == null) {
            handleException("Payload element of Intelligent Mediator is required");
        } else {
            if (payloadElem.getFirstElement() != null) {
                intelligentMediator.setPayload(payloadElem.getFirstElement().toString());
            } else {
                intelligentMediator.setPayload(payloadElem.getText());
            }
        }

        OMAttribute modelAttr = omElement.getAttribute(ATT_MODEL);
        if (modelAttr != null && modelAttr.getAttributeValue() != null) {
            model = modelAttr.getAttributeValue();
            intelligentMediator.setOpenaiModel(model);
        }

        OMAttribute endpointAttr = omElement.getAttribute(ATT_ENDPOINT);
        if (endpointAttr != null && endpointAttr.getAttributeValue() != null) {
            endpoint = endpointAttr.getAttributeValue();
            intelligentMediator.setOpenaiEndpoint(endpoint);
        }

        OMAttribute retryCountAttr = omElement.getAttribute(ATT_RETRY_COUNT);
        if (retryCountAttr != null && retryCountAttr.getAttributeValue() != null) {
            retryCount = Integer.parseInt(retryCountAttr.getAttributeValue());
            intelligentMediator.setRetryCount(retryCount);
        }

        OMAttribute headersAttr = omElement.getAttribute(ATT_HEADERS);
        if (headersAttr != null && headersAttr.getAttributeValue() != null) {
            headers = headersAttr.getAttributeValue();
            intelligentMediator.setHeaders(headers);
        }

        intelligentMediator.initialize();

        return intelligentMediator;
    }

    @Override
    public QName getTagQName() {
        return IntelligentConstants.INTELLIGENT_Q;
    }
}
