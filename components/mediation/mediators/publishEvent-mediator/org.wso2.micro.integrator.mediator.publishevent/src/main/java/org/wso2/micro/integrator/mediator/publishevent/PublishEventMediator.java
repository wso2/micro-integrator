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

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.micro.integrator.event.sink.EventSink;
import org.wso2.micro.integrator.event.sink.internal.EventSinkStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mediator that extracts data from current message payload/header according to the given configuration.
 * Extracted information is sent as an event.
 */
public class PublishEventMediator extends AbstractMediator {

    private String streamName;
    private String streamVersion;
    private List<Property> metaProperties = new ArrayList<Property>();
    private List<Property> correlationProperties = new ArrayList<Property>();
    private List<Property> payloadProperties = new ArrayList<Property>();
    private List<Property> arbitraryProperties = new ArrayList<Property>();
    private EventSink eventSink;
    private String eventSinkName;
    private boolean isAsync = true;
    private long asyncTimeout;

    @Override
    public boolean isContentAware() {
        return true;
    }

    /**
     * This is called when a new message is received for mediation.
     * Extracts data from message to construct an event based on the mediator configuration
     * Sends the constructed event to the event sink specified in mediator configuration
     *
     * @param messageContext Message context of the message to be mediated
     * @return Always returns true. (instructs to proceed with next mediator)
     */
    @Override
    public boolean mediate(MessageContext messageContext) {

        if (messageContext.getEnvironment().isDebuggerEnabled()) {
            if (super.divertMediationRoute(messageContext)) {
                return true;
            }
        }

        // first "getEventSink() == null" check is done to avoid synchronized(this) block each time mediate()
        // gets called (to improve performance).
        // second "getEventSink() == null" check inside synchronized(this) block is used to ensure only one thread
        // sets event sink.
        if (getEventSink() == null) {
            synchronized (this) {
                if (getEventSink() == null) {
                    try {
                        setEventSink(loadEventSink());
                    } catch (SynapseException e) {
                        String errorMsg = "Cannot mediate message. Failed to load event sink '" + getEventSinkName() +
                                "'. Error: " + e.getLocalizedMessage();
                        handleException(errorMsg, e, messageContext);
                    }
                }
            }
        }

        SynapseLog synLog = getLog(messageContext);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : " + PublishEventMediatorFactory.getTagName() + " mediator");
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        ActivityIDSetter.setActivityIdInTransportHeader(messageContext);

        try {
            Object[] metaData = new Object[metaProperties.size()];
            for (int i = 0; i < metaProperties.size(); ++i) {
                metaData[i] = metaProperties.get(i).extractPropertyValue(messageContext);
            }

            Object[] correlationData = new Object[correlationProperties.size()];
            for (int i = 0; i < correlationProperties.size(); ++i) {
                correlationData[i] = correlationProperties.get(i).extractPropertyValue(messageContext);
            }

            Object[] payloadData = new Object[payloadProperties.size()];
            for (int i = 0; i < payloadProperties.size(); ++i) {
                payloadData[i] = payloadProperties.get(i).extractPropertyValue(messageContext);
            }

            Map<String, String> arbitraryData = new HashMap<String, String>();
            for (int i = 0; i < arbitraryProperties.size(); ++i) {
                Property arbitraryProperty = arbitraryProperties.get(i);
                arbitraryData.put(arbitraryProperty.getKey(),
                        arbitraryProperty.extractPropertyValue(messageContext).toString());
            }

            if (isAsync) { // use async publishing(default behavior)
                if (asyncTimeout != 0L) { //asyncTimeout is not set or set to zero
                    eventSink.getDataPublisher()
                            .tryPublish(DataBridgeCommonsUtils.generateStreamId(getStreamName(), getStreamVersion()),
                                    metaData, correlationData, payloadData, arbitraryData, asyncTimeout);
                } else {
                    eventSink.getDataPublisher()
                            .tryPublish(DataBridgeCommonsUtils.generateStreamId(getStreamName(), getStreamVersion()),
                                    metaData, correlationData, payloadData, arbitraryData);
                }
            } else {
                eventSink.getDataPublisher()
                        .publish(DataBridgeCommonsUtils.generateStreamId(getStreamName(), getStreamVersion()), metaData,
                                correlationData, payloadData, arbitraryData);
            }
        } catch (SynapseException e) {
            String errorMsg = "Error occurred while constructing the event: " + e.getLocalizedMessage();
            handleException(errorMsg, e, messageContext);
        } catch (Exception e) {
            String errorMsg = "Error occurred while sending the event: " + e.getLocalizedMessage();
            handleException(errorMsg, e, messageContext);
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : " + PublishEventMediatorFactory.getTagName() + " mediator");
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        return true;
    }

    /**
     * Finds the event sink by eventSinkName and sets the stream definition to the data publisher of event sink.
     *
     * @return Found EventSink
     */
    private EventSink loadEventSink() throws SynapseException {

        EventSink eventSink = EventSinkStore.getInstance().getEventSink(getEventSinkName());

        if (eventSink == null) {
            throw new SynapseException("Event sink \"" + getEventSinkName() + "\" not found");
        }

        try {
            StreamDefinition streamDef = new StreamDefinition(getStreamName(), getStreamVersion());
            streamDef.setCorrelationData(generateAttributeList(getCorrelationProperties()));
            streamDef.setMetaData(generateAttributeList(getMetaProperties()));
            streamDef.setPayloadData(generateAttributeList(getPayloadProperties()));
        } catch (MalformedStreamDefinitionException e) {
            String errorMsg = "Failed to set stream definition. Malformed Stream Definition: " + e.getMessage();
            throw new SynapseException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while creating the Stream Definition: " + e.getMessage();
            throw new SynapseException(errorMsg, e);
        }
        return eventSink;
    }

    /**
     * Creates a list of data-bridge attributes for the given property list.
     *
     * @param propertyList List of properties for which attribute list should be created.
     * @return Created data-bridge attribute list.
     */
    private List<Attribute> generateAttributeList(List<Property> propertyList) throws SynapseException {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (Property property : propertyList) {
            attributeList.add(new Attribute(property.getKey(), property.getDatabridgeAttributeType()));
        }
        return attributeList;
    }

    public EventSink getEventSink() {
        return eventSink;
    }

    public String getEventSinkName() {
        return eventSinkName;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public List<Property> getMetaProperties() {
        return metaProperties;
    }

    public List<Property> getCorrelationProperties() {
        return correlationProperties;
    }

    public List<Property> getPayloadProperties() {
        return payloadProperties;
    }

    public List<Property> getArbitraryProperties() {
        return arbitraryProperties;
    }

    public void setEventSink(EventSink eventSink) {
        this.eventSink = eventSink;
    }

    public void setEventSinkName(String eventSinkName) {
        this.eventSinkName = eventSinkName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }

    public void setMetaProperties(List<Property> metaProperties) {
        this.metaProperties = metaProperties;
    }

    public void setCorrelationProperties(List<Property> correlationProperties) {
        this.correlationProperties = correlationProperties;
    }

    public void setPayloadProperties(List<Property> payloadProperties) {
        this.payloadProperties = payloadProperties;
    }

    public void setArbitraryProperties(List<Property> arbitraryProperties) {
        this.arbitraryProperties = arbitraryProperties;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public long getAsyncTimeout() {
        return asyncTimeout;
    }

    public void setAsyncTimeout(long asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }
}
