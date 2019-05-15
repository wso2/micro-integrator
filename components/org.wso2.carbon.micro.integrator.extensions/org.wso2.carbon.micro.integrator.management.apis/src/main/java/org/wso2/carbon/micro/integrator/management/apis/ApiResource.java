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

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiResource extends APIResource {

    private Utils utils = new Utils();

    public ApiResource(String urlTemplate){
        super(urlTemplate);
    }

    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
//        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = utils.getQueryParameters(axis2MessageContext);

        if (null != queryParameter) {
            for (NameValuePair nvPair : queryParameter) {
                if (nvPair.getName().equals("apiName")) {
                    populateApiData(messageContext, nvPair.getValue());
                }
            }
        } else {
            populateApiList(messageContext);
        }

        axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        return true;
    }

    private void populateApiList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<API> apis = configuration.getAPIs();

        JSONObject jsonBody = new JSONObject();
        JSONArray apiList = new JSONArray();
        jsonBody.put("count", apis.size());
        jsonBody.put("list", apiList);

        String serverUrl = getServerContext(axis2MessageContext.getConfigurationContext().getAxisConfiguration());

        for (API api: apis) {

            JSONObject apiObject = new JSONObject();

            String apiUrl = serverUrl.equals("err") ? api.getContext() : serverUrl + api.getContext();

            apiObject.put("name", api.getName());
            apiObject.put("url", apiUrl);

            apiList.put(apiObject);

        }
        utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateApiData(MessageContext messageContext, String apiName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getApiByName(messageContext, apiName);

        if (null != jsonBody) {
            utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private JSONObject getApiByName(MessageContext messageContext, String apiName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        API api = configuration.getAPI(apiName);
        return convertApiToOMElement(api, messageContext);
    }

    private JSONObject convertApiToOMElement(API api, MessageContext messageContext) {

        if (null == api) {
            return null;
        }

        JSONObject apiObject = new JSONObject();

        apiObject.put("name", api.getName());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String serverUrl = getServerContext(axis2MessageContext.getConfigurationContext().getAxisConfiguration());
        String apiUrl = serverUrl.equals("err") ? api.getContext() : serverUrl + api.getContext();

        apiObject.put("url", apiUrl);

//        hostElement.setText(api.getHost());
//        rootElement.addChild(hostElement);
//
//        portElement.setText(String.valueOf(api.getPort()));
//        rootElement.addChild(portElement);

        String version = api.getVersion().equals("") ? "N/A" : api.getVersion();

        apiObject.put("version", version);

        String statisticState = api.getAspectConfiguration().isStatisticsEnable() ? "enabled" : "disabled";
        apiObject.put("stats", statisticState);

        String tracingState = api.getAspectConfiguration().isTracingEnabled() ? "enabled" : "disabled";
        apiObject.put("tracing", tracingState);

        JSONArray resourceListObject = new JSONArray();
        apiObject.put("resources", resourceListObject);

        Resource[] resources = api.getResources();

        for (Resource resource : resources) {

            JSONObject resourceObject = new JSONObject();

            String[] methods = resource.getMethods();

            resourceObject.put("methods", methods);

            DispatcherHelper dispatcherHelper = resource.getDispatcherHelper();
            if (dispatcherHelper instanceof URITemplateHelper) {
                resourceObject.put("url", dispatcherHelper.getString());

            } else if (dispatcherHelper instanceof URLMappingHelper) {
                resourceObject.put("url", dispatcherHelper.getString());
            }
            resourceListObject.put(resourceObject);
        }
        return apiObject;
    }

    private String getServerContext(AxisConfiguration configuration) {

        String portValue;
        String protocol;

        TransportInDescription transportInDescription = configuration.getTransportIn("http");
        if (null == transportInDescription) {
            transportInDescription = configuration.getTransportIn("https");
        }

        if (null != transportInDescription) {
            protocol = transportInDescription.getName();
            portValue = (String) transportInDescription.getParameter("port").getValue();
        } else {
            return "err";
        }

        String host;

        Parameter hostParam =  configuration.getParameter("hostname");

        if (null != hostParam) {
            host = (String)hostParam.getValue();
        } else {
            try {
                host = NetworkUtils.getLocalHostname();
            } catch (SocketException e) {
                host = "localhost";
            }
        }

        String serverContext;

        try {
            int port = Integer.parseInt(portValue);
            if ("http".equals(protocol) && port == 80) {
                port = -1;
            } else if ("https".equals(protocol) && port == 443) {
                port = -1;
            }
            URL serverURL = new URL(protocol, host, port, "");
            serverContext = serverURL.toExternalForm();
        } catch (MalformedURLException e) {
            serverContext = "err";
        }
        return serverContext;
    }
}
