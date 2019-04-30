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
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class ApiResource extends APIResource {

    private static Log log = LogFactory.getLog(ApiResource.class);

    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_API = "<API></API>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String CONTEXT_ELEMENT = "<Context></Context>";
    private static final String URL_ELEMENT = "<Url></Url>";
    private static final String HOST_ELEMENT = "<Host></Host>";
    private static final String PORT_ELEMENT = "<Port></Port>";
    private static final String FILENAME_ELEMENT = "<FileName></FileName>";
    private static final String VERSION_ELEMENT = "<Version></Version>";

    private static final String RESOURCES_ELEMENT = "<Resources></Resources>";
    private static final String RESOURCE_ELEMENT = "<Resource></Resource>";
    private static final String METHOD_ELEMENT = "<Methods></Methods>";
    private static final String STAT_ELEMENT = "<Stats></Stats>";
    private static final String TRACING_ELEMENT = "<Tracing></Tracing>";


    public ApiResource(String urlTemplate){
        super(urlTemplate);
    }

    public Set<String> getMethods() {

        Set<String> methods = new HashSet<String>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
//        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = getQueryParameters(axis2MessageContext);

        try {

            if(queryParameter != null){
                for(NameValuePair nvPair : queryParameter){
                    if(nvPair.getName().equals("apiName")){
                        populateApiData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateApiList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
           log.error("Error occurred while processing response", e);
        }

        return true;
    }

    private void populateApiList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<API> apis = configuration.getAPIs();

        OMElement rootElement = AXIOMUtil.stringToOM(LIST_ELEMENT);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);

        countElement.setText(String.valueOf(apis.size()));
        rootElement.addChild(countElement);

        for (API api: apis) {

            OMElement apiElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_API);
            OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
            OMElement contextElement = AXIOMUtil.stringToOM(CONTEXT_ELEMENT);

            nameElement.setText(api.getName());
            contextElement.setText(api.getContext());

            apiElement.addChild(nameElement);
            apiElement.addChild(contextElement);

            rootElement.addChild(apiElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateApiData(MessageContext messageContext, String apiName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getApiByName(messageContext, apiName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }

    }

    private OMElement getApiByName(MessageContext messageContext, String apiName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        API api = configuration.getAPI(apiName);
        return convertApiToOMElement(api);

    }

    private OMElement convertApiToOMElement(API api) throws XMLStreamException{

        if (api == null) {
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_API);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement contextElement = AXIOMUtil.stringToOM(CONTEXT_ELEMENT);
        OMElement hostElement = AXIOMUtil.stringToOM(HOST_ELEMENT);
        OMElement portElement = AXIOMUtil.stringToOM(PORT_ELEMENT);
        OMElement versionElement = AXIOMUtil.stringToOM(VERSION_ELEMENT);
        OMElement statsElement = AXIOMUtil.stringToOM(STAT_ELEMENT);
        OMElement tracingElement = AXIOMUtil.stringToOM(TRACING_ELEMENT);

        nameElement.setText(api.getName());
        rootElement.addChild(nameElement);

        contextElement.setText(api.getContext());
        rootElement.addChild(contextElement);

        hostElement.setText(api.getHost());
        rootElement.addChild(hostElement);

        portElement.setText(String.valueOf(api.getPort()));
        rootElement.addChild(portElement);

        versionElement.setText(api.getVersion());
        rootElement.addChild(versionElement);

        String statisticState = api.getAspectConfiguration().isStatisticsEnable() ? "enabled" : "disabled";

        statsElement.setText(statisticState);
        rootElement.addChild(statsElement);

        String tracingState = api.getAspectConfiguration().isTracingEnabled() ? "enabled" : "disabled";

        tracingElement.setText(tracingState);
        rootElement.addChild(tracingElement);

        OMElement resourcesElement = AXIOMUtil.stringToOM(RESOURCES_ELEMENT);
        rootElement.addChild(resourcesElement);

        Resource[] resources = api.getResources();

        for (Resource resource : resources) {

            OMElement resourceElement = AXIOMUtil.stringToOM(RESOURCE_ELEMENT);
            resourcesElement.addChild(resourceElement);

            OMElement methodElement = AXIOMUtil.stringToOM(METHOD_ELEMENT);
            resourceElement.addChild(methodElement);

            OMElement urlElement = AXIOMUtil.stringToOM(URL_ELEMENT);
            resourceElement.addChild(urlElement);

            String[] methods = resource.getMethods();

            for(String method : methods){
                OMElement itemElement = AXIOMUtil.stringToOM(LIST_ITEM);
                itemElement.setText(method);
                methodElement.addChild(itemElement);
            }

            DispatcherHelper dispatcherHelper = resource.getDispatcherHelper();
            if (dispatcherHelper instanceof URITemplateHelper) {
                urlElement.setText(dispatcherHelper.getString());

            } else if (dispatcherHelper instanceof URLMappingHelper) {
                urlElement.setText(dispatcherHelper.getString());
            }
        }
        return rootElement;
    }
}
