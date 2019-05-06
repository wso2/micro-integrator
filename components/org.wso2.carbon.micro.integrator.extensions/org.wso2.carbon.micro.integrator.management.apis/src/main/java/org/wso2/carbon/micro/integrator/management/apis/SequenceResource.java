/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class SequenceResource extends APIResource {

    private static Log log = LogFactory.getLog(TaskResource.class);

    private static final String ROOT_ELEMENT_SEQUENCES = "<Sequences></Sequences>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";

    private static final String ROOT_ELEMENT_SEQUENCE = "<Sequence></Sequence>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String CONTAINER_ELEMENT = "<Container></Container>";
    private static final String MEDIATORS_ELEMENT = "<Mediators></Mediators>";
    private static final String MEDIATOR_ELEMENT = "<Mediator></Mediator>";
    private static final String STAT_ELEMENT = "<Stats></Stats>";
    private static final String TRACING_ELEMENT = "<Tracing></Tracing>";

    public SequenceResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<String>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
//        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = getQueryParameters(axis2MessageContext);

        try {
            // if query params exists retrieve data about specific sequence
            if (null != queryParameter) {
                for (NameValuePair nvPair : queryParameter) {
                    if (nvPair.getName().equals("inboundEndpointName")) {
                        populateSequenceData(messageContext, nvPair.getValue());
                    }
                }
            } else {
                populateSequenceList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        } catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }
        return true;
    }

    private void populateSequenceList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Map<String, SequenceMediator> sequenceMediatorMap = configuration.getDefinedSequences();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_SEQUENCES);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(sequenceMediatorMap.size()));
        rootElement.addChild(countElement);

        rootElement.addChild(listElement);

        for (SequenceMediator sequence: sequenceMediatorMap.values()) {

            OMElement sequenceElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_SEQUENCE);
            OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
            OMElement containerElement = AXIOMUtil.stringToOM(CONTAINER_ELEMENT);
            OMElement statsElement = AXIOMUtil.stringToOM(STAT_ELEMENT);
            OMElement tracingElement = AXIOMUtil.stringToOM(TRACING_ELEMENT);

            nameElement.setText(sequence.getName());
            sequenceElement.addChild(nameElement);

            containerElement.setText(sequence.getArtifactContainerName());
            sequenceElement.addChild(containerElement);

            String statisticState = sequence.getAspectConfiguration().isStatisticsEnable() ? "enabled" : "disabled";

            statsElement.setText(statisticState);
            sequenceElement.addChild(statsElement);

            String tracingState = sequence.getAspectConfiguration().isTracingEnabled() ? "enabled" : "disabled";

            tracingElement.setText(tracingState);
            sequenceElement.addChild(tracingElement);

            listElement.addChild(sequenceElement);
        }
        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateSequenceData(MessageContext messageContext, String sequenceName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getSequenceByName(messageContext, sequenceName);

        if (null != rootElement) {
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private OMElement getSequenceByName(MessageContext messageContext, String sequenceName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        SequenceMediator sequence = configuration.getDefinedSequences().get(sequenceName);
        return convertInboundEndpointToOMElement(sequence);
    }

    private OMElement convertInboundEndpointToOMElement(SequenceMediator sequenceMediator) throws XMLStreamException{

        if (null == sequenceMediator) {
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_SEQUENCE);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement statsElement = AXIOMUtil.stringToOM(STAT_ELEMENT);
        OMElement tracingElement = AXIOMUtil.stringToOM(TRACING_ELEMENT);
        OMElement containerElement = AXIOMUtil.stringToOM(CONTAINER_ELEMENT);
        OMElement mediatorsElement = AXIOMUtil.stringToOM(MEDIATORS_ELEMENT);

        nameElement.setText(sequenceMediator.getName());
        rootElement.addChild(nameElement);

        containerElement.setText(sequenceMediator.getArtifactContainerName());
        rootElement.addChild(containerElement);

        String statisticState = sequenceMediator.getAspectConfiguration().isStatisticsEnable() ? "enabled" : "disabled";

        statsElement.setText(statisticState);
        rootElement.addChild(statsElement);

        String tracingState = sequenceMediator.getAspectConfiguration().isTracingEnabled() ? "enabled" : "disabled";

        tracingElement.setText(tracingState);
        rootElement.addChild(tracingElement);

        List<Mediator> mediators = sequenceMediator.getList();

        for (Mediator mediator : mediators) {

            OMElement mediatorElement = AXIOMUtil.stringToOM(MEDIATOR_ELEMENT);

            mediatorElement.setText(mediator.getType());
            mediatorsElement.addChild(mediatorElement);
        }
        rootElement.addChild(mediatorsElement);

        return rootElement;
    }
}
