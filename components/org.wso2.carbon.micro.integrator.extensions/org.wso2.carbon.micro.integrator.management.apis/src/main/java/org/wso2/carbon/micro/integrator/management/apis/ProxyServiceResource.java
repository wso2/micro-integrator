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
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.ProxyService;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.service.mgt.ServiceMetaData;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class ProxyServiceResource extends APIResource {

    private static Log log = LogFactory.getLog(ProxyServiceResource.class);

    private static final String ROOT_ELEMENT_PROXY_SERVICES = "<ProxyServices></ProxyServices>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";

    private static final String ROOT_ELEMENT_PROXY_SERVICE = "<ProxyService></ProxyService>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String WSDL1_1_ELEMENT = "<WSDL1_1></WSDL1_1>";
    private static final String WSDL2_0_ELEMENT = "<WSDL2_0></WSDL2_0>";
    private static final String STAT_ELEMENT = "<Stats></Stats>";
    private static final String TRACING_ELEMENT = "<Tracing></Tracing>";

    public ProxyServiceResource(String urlTemplate){
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

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = getQueryParameters(axis2MessageContext);

        try {
            // if query params exists retrieve data about specific inbound endpoint
            if (null != queryParameter) {
                for (NameValuePair nvPair : queryParameter) {
                    if (nvPair.getName().equals("proxyServiceName")) {
                        populateProxyServiceData(messageContext, nvPair.getValue());
                    }
                }
            } else {
                populateProxyServiceList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        } catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }
        return true;
    }

    private void populateProxyServiceList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_PROXY_SERVICES);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        Collection<ProxyService> proxyServices = configuration.getProxyServices();

        countElement.setText(String.valueOf(proxyServices.size()));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for (ProxyService proxyService : proxyServices) {

            OMElement proxyElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_PROXY_SERVICE);
            OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
            OMElement wsdl1Element = AXIOMUtil.stringToOM(WSDL1_1_ELEMENT);
            OMElement wsdl2Element = AXIOMUtil.stringToOM(WSDL2_0_ELEMENT);

            try {
                ServiceMetaData data = new ServiceAdmin().getServiceData(proxyService.getName());

                nameElement.setText(proxyService.getName());
                proxyElement.addChild(nameElement);

                String []wsdlUrls = data.getWsdlURLs();

                wsdl1Element.setText(wsdlUrls[0]);
                proxyElement.addChild(wsdl1Element);

                wsdl2Element.setText(wsdlUrls[1]);
                proxyElement.addChild(wsdl2Element);

            } catch (Exception e) {
                log.error("Error occurred while processing service data", e);
            }

            listElement.addChild(proxyElement);
        }
        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }


    private void populateProxyServiceData(MessageContext messageContext, String proxyServiceName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getProxyServiceByName(messageContext, proxyServiceName);

        if (rootElement != null) {
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private OMElement getProxyServiceByName(MessageContext messageContext, String proxyServiceName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        ProxyService proxyService = configuration.getProxyService(proxyServiceName);
        return convertProxyServiceToOMElement(proxyService);
    }

    private OMElement convertProxyServiceToOMElement(ProxyService proxyService) throws XMLStreamException{

        if (null == proxyService) {
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_PROXY_SERVICE);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement wsdl1Element = AXIOMUtil.stringToOM(WSDL1_1_ELEMENT);
        OMElement wsdl2Element = AXIOMUtil.stringToOM(WSDL2_0_ELEMENT);
        OMElement statsElement = AXIOMUtil.stringToOM(STAT_ELEMENT);
        OMElement tracingElement = AXIOMUtil.stringToOM(TRACING_ELEMENT);

        nameElement.setText(proxyService.getName());
        rootElement.addChild(nameElement);

        try {
            ServiceMetaData data = new ServiceAdmin().getServiceData(proxyService.getName());

            String []wsdlUrls = data.getWsdlURLs();

            wsdl1Element.setText(wsdlUrls[0]);
            rootElement.addChild(wsdl1Element);

            wsdl2Element.setText(wsdlUrls[1]);
            rootElement.addChild(wsdl2Element);
        } catch (Exception e) {
            log.error("Error occurred while processing service data", e);
        }

        String statisticState = proxyService.getAspectConfiguration().isStatisticsEnable() ? "enabled" : "disabled";

        statsElement.setText(statisticState);
        rootElement.addChild(statsElement);

        String tracingState = proxyService.getAspectConfiguration().isTracingEnabled() ? "enabled" : "disabled";

        tracingElement.setText(tracingState);
        rootElement.addChild(tracingElement);

        return rootElement;
    }
}
