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
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class EndpointResource extends APIResource {

    private static Log log = LogFactory.getLog(EndpointResource.class);

    private static final String ROOT_ELEMENT_ENDPOINTS = "<Endpoints></Endpoints>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";

    private static final String ROOT_ELEMENT_ENDPOINT = "<Endpoint></Endpoint>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String TYPE_ELEMENT = "<Type></Type>";
    private static final String METHOD_ELEMENT = "<Method></Method>";
    private static final String URL_ELEMENT = "<Url></Url>";
    private static final String STAT_ELEMENT = "<Stats></Stats>";

    public EndpointResource(String urlTemplate){
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
            // if query params exists retrieve data about specific endpoint
            if (null != queryParameter) {
                for (NameValuePair nvPair : queryParameter) {
                    if (nvPair.getName().equals("endpointName")) {
                        populateEndpointData(messageContext, nvPair.getValue());
                    }
                }
            } else {
                populateEndpointList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        } catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }
        return true;
    }

    private void populateEndpointList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Map<String, Endpoint> namedEndpointMap = configuration.getDefinedEndpoints();
        Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_ENDPOINTS);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(namedEndpointCollection.size()));
        rootElement.addChild(countElement);

        rootElement.addChild(listElement);

        for (Endpoint ep : namedEndpointCollection) {

            OMElement endpointElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_ENDPOINT);
            OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
            OMElement typeElement = AXIOMUtil.stringToOM(TYPE_ELEMENT);
            OMElement methodElement = AXIOMUtil.stringToOM(METHOD_ELEMENT);
            OMElement urlElement = AXIOMUtil.stringToOM(URL_ELEMENT);

            String epName = ep.getName();
            nameElement.setText(epName);
            endpointElement.addChild(nameElement);

            OMElement element = EndpointSerializer.getElementFromEndpoint(ep);
            OMElement firstElement = element.getFirstElement();

            String type = firstElement.getLocalName();
            typeElement.setText(type);
            endpointElement.addChild(typeElement);

            String method = firstElement.getAttributeValue(new QName("method"));
            methodElement.setText(method);
            endpointElement.addChild(methodElement);

            String url = firstElement.getAttributeValue(new QName("uri-template"));
            urlElement.setText(url);
            endpointElement.addChild(urlElement);

            listElement.addChild(endpointElement);
        }
        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateEndpointData(MessageContext messageContext, String endpointName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getEndpointByName(messageContext, endpointName);

        if (null != rootElement) {
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private OMElement getEndpointByName(MessageContext messageContext, String endpointName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        Endpoint ep = configuration.getEndpoint(endpointName);
        return convertEndpointToOMElement(ep);
    }

    private OMElement convertEndpointToOMElement(Endpoint endpoint) throws XMLStreamException{

        if (null == endpoint) {
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_ENDPOINT);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement typeElement = AXIOMUtil.stringToOM(TYPE_ELEMENT);
        OMElement methodElement = AXIOMUtil.stringToOM(METHOD_ELEMENT);
        OMElement urlElement = AXIOMUtil.stringToOM(URL_ELEMENT);
        OMElement statsElement = AXIOMUtil.stringToOM(STAT_ELEMENT);

        nameElement.setText(endpoint.getName());
        rootElement.addChild(nameElement);

        OMElement epElement = EndpointSerializer.getElementFromEndpoint(endpoint);
        OMElement firstElement = epElement.getFirstElement();

        String type = firstElement.getLocalName();
        typeElement.setText(type);
        rootElement.addChild(typeElement);

        String method = firstElement.getAttributeValue(new QName("method"));
        methodElement.setText(method);
        rootElement.addChild(methodElement);

        String url = firstElement.getAttributeValue(new QName("uri-template"));
        urlElement.setText(url);
        rootElement.addChild(urlElement);

        EndpointDefinition def = ((AbstractEndpoint) endpoint).getDefinition();
        if (null != def) {
            if (def.isStatisticsEnable()) {
                statsElement.setText("enabled");
            } else {
                statsElement.setText("disabled");
            }
        }
        rootElement.addChild(statsElement);
        return rootElement;
    }
}
