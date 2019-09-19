/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.micro.integrator.mediator.publishevent;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapseXPathSerializer;

public class PublishEventMediatorSerializer extends AbstractMediatorSerializer {

    /**
     * Creates XML representation of the publishEvent mediator.
     *
     * @param mediator The mediator for which the XML representation should be created
     * @return The Created XML representation of mediator as an OMElement
     */
    @Override
    public OMElement serializeSpecificMediator(Mediator mediator) {
        assert mediator instanceof PublishEventMediator :
                PublishEventMediatorFactory.getTagName() + " mediator is expected";

        PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;
        OMElement mediatorElement = fac.createOMElement(PublishEventMediatorFactory.getTagName(), synNS);

        OMAttribute asyncAttribute = fac.createOMAttribute(PublishEventMediatorFactory.ATT_ASYNC.getLocalPart(), nullNS,
                String.valueOf(publishEventMediator.isAsync()));
        mediatorElement.addAttribute(asyncAttribute);

        if (!"false".equals(asyncAttribute.getAttributeValue())) {
            OMAttribute asyncTimeoutAttribute = fac
                    .createOMAttribute(PublishEventMediatorFactory.ATT_ASYNC_TIMEOUT.getLocalPart(), nullNS,
                            String.valueOf(publishEventMediator.getAsyncTimeout()));
            if (!"0".equals(asyncTimeoutAttribute.getAttributeValue())) {
                mediatorElement.addAttribute(asyncTimeoutAttribute);
            }
        }
        OMElement eventSinkElement =
                fac.createOMElement(PublishEventMediatorFactory.EVENT_SINK_QNAME.getLocalPart(), synNS);
        eventSinkElement.setText(publishEventMediator.getEventSinkName());
        mediatorElement.addChild(eventSinkElement);

        OMElement streamNameElement =
                fac.createOMElement(PublishEventMediatorFactory.STREAM_NAME_QNAME.getLocalPart(), synNS);
        streamNameElement.setText(publishEventMediator.getStreamName());
        mediatorElement.addChild(streamNameElement);

        OMElement streamVersionElement =
                fac.createOMElement(PublishEventMediatorFactory.STREAM_VERSION_QNAME.getLocalPart(), synNS);
        streamVersionElement.setText(publishEventMediator.getStreamVersion());
        mediatorElement.addChild(streamVersionElement);

        OMElement streamAttributesElement =
                fac.createOMElement(PublishEventMediatorFactory.ATTRIBUTES_QNAME.getLocalPart(), synNS);

        OMElement metaAttributesElement = fac.createOMElement(PublishEventMediatorFactory.META_QNAME.getLocalPart(), synNS);
        for (Property property : publishEventMediator.getMetaProperties()) {
            metaAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(metaAttributesElement);

        OMElement correlationAttributesElement =
                fac.createOMElement(PublishEventMediatorFactory.CORRELATION_QNAME.getLocalPart(), synNS);
        for (Property property : publishEventMediator.getCorrelationProperties()) {
            correlationAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(correlationAttributesElement);

        OMElement payloadAttributesElement =
                fac.createOMElement(PublishEventMediatorFactory.PAYLOAD_QNAME.getLocalPart(), synNS);
        for (Property property : publishEventMediator.getPayloadProperties()) {
            payloadAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(payloadAttributesElement);

        OMElement arbitraryAttributesElement =
                fac.createOMElement(PublishEventMediatorFactory.ARBITRARY_QNAME.getLocalPart(), synNS);
        for (Property property : publishEventMediator.getArbitraryProperties()) {
            arbitraryAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(arbitraryAttributesElement);

        mediatorElement.addChild(streamAttributesElement);

        serializeComments(mediatorElement, publishEventMediator.getCommentsList());

        return mediatorElement;
    }

    @Override
    public String getMediatorClassName() {
        return PublishEventMediator.class.getName();
    }

    /**
     * Creates the XML representation of the given mediator property.
     *
     * @param property Property for which the XML representation should be created
     * @return XML representation of the property as an OMElement
     */
    private OMElement createElementForProperty(Property property) {
        OMElement attributeElement = fac.createOMElement(PublishEventMediatorFactory.ATTRIBUTE_QNAME.getLocalPart(), synNS);
        attributeElement.addAttribute(
                fac.createOMAttribute(PublishEventMediatorFactory.getNameAttributeQ().getLocalPart(), nullNS,
                        property.getKey()));
        attributeElement.addAttribute(
                fac.createOMAttribute(PublishEventMediatorFactory.TYPE_QNAME.getLocalPart(), nullNS, property.getType()));
        attributeElement.addAttribute(
                fac.createOMAttribute(PublishEventMediatorFactory.DEFAULT_QNAME.getLocalPart(), nullNS,
                        property.getDefaultValue()));

        if (property.getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(property.getExpression(), attributeElement,
                    PublishEventMediatorFactory.getExpressionAttributeQ().getLocalPart());
        } else {
            attributeElement.addAttribute(
                    fac.createOMAttribute(PublishEventMediatorFactory.getValueAttributeQ().getLocalPart(), nullNS,
                            property.getValue()));
        }
        return attributeElement;
    }
}
