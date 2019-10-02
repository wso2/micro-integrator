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

package org.wso2.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.rest.APISerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.micro.core.util.NetworkUtils;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ApiResource extends APIResource {

    public ApiResource(String urlTemplate){
        super(urlTemplate);
    }

    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        return methods;
    }

    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String param = Utils.getQueryParameter(messageContext, "apiName");

        if (Objects.nonNull(param)) {
            populateApiData(messageContext, param);
        } else {
            populateApiList(messageContext);
        }

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateApiList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<API> apis = configuration.getAPIs();

        JSONObject jsonBody = Utils.createJSONList(apis.size());

        String serverUrl = getServerContext(axis2MessageContext.getConfigurationContext().getAxisConfiguration());

        for (API api: apis) {

            JSONObject apiObject = new JSONObject();

            String apiUrl = serverUrl.equals("err") ? api.getContext() : serverUrl + api.getContext();

            apiObject.put(Constants.NAME, api.getName());
            apiObject.put(Constants.URL, apiUrl);

            jsonBody.getJSONArray(Constants.LIST).put(apiObject);

        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateApiData(MessageContext messageContext, String apiName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getApiByName(messageContext, apiName);

        if (Objects.nonNull(jsonBody)) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        }
    }

    private JSONObject getApiByName(MessageContext messageContext, String apiName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        API api = configuration.getAPI(apiName);
        return convertApiToJsonObject(api, messageContext);
    }

    private JSONObject convertApiToJsonObject(API api, MessageContext messageContext) {

        if (Objects.isNull(api)) {
            return null;
        }

        JSONObject apiObject = new JSONObject();

        apiObject.put(Constants.NAME, api.getName());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String serverUrl = getServerContext(axis2MessageContext.getConfigurationContext().getAxisConfiguration());
        String apiUrl = serverUrl.equals("err") ? api.getContext() : serverUrl + api.getContext();

        apiObject.put(Constants.URL, apiUrl);

        String version = api.getVersion().equals("") ? "N/A" : api.getVersion();

        apiObject.put(Constants.VERSION, version);

        String statisticState = api.getAspectConfiguration().isStatisticsEnable() ? Constants.ENABLED : Constants.DISABLED;
        apiObject.put(Constants.STATS, statisticState);

        String tracingState = api.getAspectConfiguration().isTracingEnabled() ? Constants.ENABLED : Constants.DISABLED;
        apiObject.put(Constants.TRACING, tracingState);

        JSONArray resourceListObject = new JSONArray();
        apiObject.put("resources", resourceListObject);

        apiObject.put("host", api.getHost());
        apiObject.put("context", api.getContext());
        apiObject.put("port", api.getPort());

        OMElement apiConfiguration = APISerializer.serializeAPI(api);
        apiObject.put(Constants.SYNAPSE_CONFIGURATION, apiConfiguration.toString());
        Resource[] resources = api.getResources();

        for (Resource resource : resources) {

            JSONObject resourceObject = new JSONObject();

            String[] methods = resource.getMethods();

            resourceObject.put("methods", methods);

            DispatcherHelper dispatcherHelper = resource.getDispatcherHelper();
            if (dispatcherHelper instanceof URITemplateHelper) {
                resourceObject.put(Constants.URL, dispatcherHelper.getString());

            } else if (dispatcherHelper instanceof URLMappingHelper) {
                resourceObject.put(Constants.URL, dispatcherHelper.getString());
            } else {
                resourceObject.put(Constants.URL, "N/A");
            }
            resourceListObject.put(resourceObject);
        }
        return apiObject;
    }

    private String getServerContext(AxisConfiguration configuration) {

        String portValue;
        String protocol;

        TransportInDescription transportInDescription = configuration.getTransportIn("http");
        if (Objects.isNull(transportInDescription)) {
            transportInDescription = configuration.getTransportIn("https");
        }

        if (Objects.nonNull(transportInDescription)) {
            protocol = transportInDescription.getName();
            portValue = (String) transportInDescription.getParameter("port").getValue();
        } else {
            return "err";
        }

        String host;

        Parameter hostParam =  configuration.getParameter("hostname");

        if (Objects.nonNull(hostParam)) {
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
            if (("http".equals(protocol) && port == 80) || ("https".equals(protocol) && port == 443)) {
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
