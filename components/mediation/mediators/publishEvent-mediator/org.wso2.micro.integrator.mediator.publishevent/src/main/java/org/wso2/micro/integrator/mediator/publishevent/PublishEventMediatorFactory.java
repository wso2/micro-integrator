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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Creates the publishEvent mediator with given configuration.
 */
public class PublishEventMediatorFactory extends AbstractMediatorFactory {
    private static final Log log = LogFactory.getLog(PublishEventMediatorFactory.class);

    public static final QName PUBLISH_EVENT_QNAME = new QName(SynapseConstants.SYNAPSE_NAMESPACE, getTagName());
    public static final QName EVENT_SINK_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "eventSink");
    public static final QName STREAM_NAME_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamName");
    public static final QName STREAM_VERSION_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamVersion");
    public static final QName ATTRIBUTES_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attributes");
    public static final QName ATTRIBUTE_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");
    public static final QName META_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "meta");
    public static final QName CORRELATION_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "correlation");
    public static final QName PAYLOAD_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "payload");
    public static final QName ARBITRARY_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "arbitrary");
    public static final QName TYPE_QNAME = new QName("type");
    public static final QName DEFAULT_QNAME = new QName("defaultValue");
    public static final QName ATT_ASYNC = new QName("async");
    public static final QName ATT_ASYNC_TIMEOUT = new QName("timeout");


    public static String getTagName() {
        return "publishEvent";
    }

    @Override
    public QName getTagQName() {
        return PUBLISH_EVENT_QNAME;
    }

    /**
     * Creates a publishEvent mediator instance from given OMElement xml config.
     *
     * @param omElement  XML config of the mediator
     * @param properties
     * @return Created publishEvent mediator object
     */
    @Override
    public Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        PublishEventMediator mediator = new PublishEventMediator();
        OMAttribute isAsync = omElement.getAttribute(ATT_ASYNC);
        if ((isAsync != null && !Boolean.parseBoolean(isAsync.getAttributeValue()))) { //async set to false
            mediator.setAsync(false);
        } else { // async not set or set to true
            OMAttribute asyncTimeout = omElement.getAttribute(ATT_ASYNC_TIMEOUT);
            if (asyncTimeout != null) { //timeout set for async
                try {
                    long timeout = Long.parseLong(asyncTimeout.getAttributeValue());
                    if (timeout > 0) {
                        mediator.setAsyncTimeout(timeout);
                    } else {
                        log.warn("Provided timeout value - " + asyncTimeout.getAttributeValue()
                                + " is negative. Expecting a positive whole numerical value. Hence ignoring the provided timeout property.");
                    }
                } catch (NumberFormatException e) {
                    log.warn("Provided timeout value - " + asyncTimeout.getAttributeValue()
                            + " is invalid. Expecting a positive whole numerical value. Hence ignoring the provided timeout property.");
                    //ignore the timeout property if the timeout is not a number
                }
            }
        }

        OMElement streamName = omElement.getFirstChildWithName(STREAM_NAME_QNAME);
        if (streamName == null) {
            throw new SynapseException(STREAM_NAME_QNAME.getLocalPart() + " element missing");
        }
        mediator.setStreamName(streamName.getText());

        OMElement streamVersion = omElement.getFirstChildWithName(STREAM_VERSION_QNAME);
        if (streamVersion == null) {
            throw new SynapseException(STREAM_VERSION_QNAME.getLocalPart() + " element missing");
        }
        mediator.setStreamVersion(streamVersion.getText());

        OMElement attributes = omElement.getFirstChildWithName(ATTRIBUTES_QNAME);
        if (attributes != null) {
            OMElement meta = attributes.getFirstChildWithName(META_QNAME);
            if (meta != null) {
                Iterator<OMElement> iterator = meta.getChildrenWithName(ATTRIBUTE_QNAME);
                List<Property> propertyList = generatePropertyList(iterator);
                mediator.setMetaProperties(propertyList);
            }
            OMElement correlation = attributes.getFirstChildWithName(CORRELATION_QNAME);
            if (correlation != null) {
                Iterator<OMElement> iterator = correlation.getChildrenWithName(ATTRIBUTE_QNAME);
                List<Property> propertyList = generatePropertyList(iterator);
                mediator.setCorrelationProperties(propertyList);
            }
            OMElement payload = attributes.getFirstChildWithName(PAYLOAD_QNAME);
            if (payload != null) {
                Iterator<OMElement> iterator = payload.getChildrenWithName(ATTRIBUTE_QNAME);
                List<Property> propertyList = generatePropertyList(iterator);
                mediator.setPayloadProperties(propertyList);
            }
            OMElement arbitrary = attributes.getFirstChildWithName(ARBITRARY_QNAME);
            if (arbitrary != null) {
                Iterator<OMElement> iterator = arbitrary.getChildrenWithName(ATTRIBUTE_QNAME);
                List<Property> propertyList = generatePropertyList(iterator);

                for (Property property : propertyList) {
                    if (!property.getType().equals(Property.DATA_TYPE_STRING)) {
                        throw new SynapseException(
                                "Invalid type " + property.getType() + " for arbitrary property " + property.getKey() +
                                        ". Type of arbitrary attributes must be " +
                                        Property.DATA_TYPE_STRING);
                    }
                }

                mediator.setArbitraryProperties(propertyList);
            }
        } else {
            throw new SynapseException(ATTRIBUTES_QNAME.getLocalPart() + " attribute missing");
        }

        OMElement eventSinkElement = omElement.getFirstChildWithName(EVENT_SINK_QNAME);
        if (eventSinkElement == null) {
            throw new SynapseException(EVENT_SINK_QNAME.getLocalPart() + " element missing");
        }
        mediator.setEventSinkName(eventSinkElement.getText());

        addAllCommentChildrenToList(omElement, mediator.getCommentsList());

        return mediator;
    }

    /**
     * Creates a list of Property objects with XML elements pointed by provided iterator.
     *
     * @param iterator OMElement iterator. (Each OMElement contains XML config for a Property object)
     * @return Created Property list
     */
    private List<Property> generatePropertyList(Iterator<OMElement> iterator) {
        List<Property> propertyList = new ArrayList<Property>();
        while (iterator.hasNext()) {
            OMElement element = iterator.next();
            OMAttribute nameAttr = element.getAttribute(ATT_NAME);
            if (nameAttr == null) {
                throw new SynapseException(ATT_NAME.getLocalPart() + " attribute missing in " + element.getLocalName());
            }
            OMAttribute typeAttr = element.getAttribute(TYPE_QNAME);
            if (typeAttr == null) {
                throw new SynapseException(
                        TYPE_QNAME.getLocalPart() + " attribute missing in " + element.getLocalName());
            }
            OMAttribute valueAttr = element.getAttribute(ATT_VALUE);
            OMAttribute expressionAttr = element.getAttribute(ATT_EXPRN);
            if (valueAttr != null && expressionAttr != null) {
                throw new SynapseException(
                        element.getLocalName() + " element can either have \"" + ATT_VALUE.getLocalPart() +
                                "\" or \"" + ATT_EXPRN.getLocalPart() + "\" attribute but not both");
            }

            if (valueAttr == null && expressionAttr == null) {
                throw new SynapseException(
                        element.getLocalName() + " element must have either \"" + ATT_VALUE.getLocalPart() +
                                "\" or \"" + ATT_EXPRN.getLocalPart() + "\" attribute");
            }

            Property property = new Property();
            property.setKey(nameAttr.getAttributeValue());
            property.setType(typeAttr.getAttributeValue());
            if (valueAttr != null) {
                property.setValue(valueAttr.getAttributeValue());
            } else {
                try {
                    property.setExpression(SynapseXPathFactory.getSynapseXPath(element, ATT_EXPRN));
                } catch (JaxenException e) {
                    throw new SynapseException("Invalid expression attribute in " + element.getLocalName(), e);
                }
            }

            OMAttribute defaultAtr = element.getAttribute(DEFAULT_QNAME);
            if (defaultAtr != null) {
                property.setDefaultValue(defaultAtr.getAttributeValue());
            }

            propertyList.add(property);
        }
        return propertyList;
    }

    public static QName getNameAttributeQ() {
        return ATT_NAME;
    }

    public static QName getValueAttributeQ() {
        return ATT_VALUE;
    }

    public static QName getExpressionAttributeQ() {
        return ATT_EXPRN;
    }
}
