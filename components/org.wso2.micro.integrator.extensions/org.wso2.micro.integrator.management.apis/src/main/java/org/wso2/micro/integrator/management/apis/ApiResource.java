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

import com.google.gson.JsonObject;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.API;
import org.apache.synapse.api.Resource;
import org.apache.synapse.api.dispatch.DispatcherHelper;
import org.apache.synapse.api.dispatch.URITemplateHelper;
import org.apache.synapse.api.dispatch.URLMappingHelper;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.rest.APISerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.micro.core.util.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ApiResource extends APIResource {

    private static Log LOG = LogFactory.getLog(ApiResource.class);

    private static final String API_NAME = "apiName";
    private static final String URL_VERSION_TYPE = "url";
    private String serverContext = "";  // base server url

    public ApiResource(String urlTemplate) {
        super(urlTemplate);
    }

    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
        return methods;
    }

    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
        org.apache.axis2.context.MessageContext axisMsgCtx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String apiName = Utils.getQueryParameter(messageContext, API_NAME);
        if (messageContext.isDoingGET()) {
            if (Objects.nonNull(apiName)) {
                populateApiData(messageContext, apiName);
            } else {
                populateApiList(messageContext);
            }
        } else {
            handlePost(messageContext, axisMsgCtx);
        }
        return true;
    }

    private void handlePost(MessageContext msgCtx, org.apache.axis2.context.MessageContext axisMsgCtx) {

        JSONObject response;
        try {
            JsonObject payload = Utils.getJsonPayload(axisMsgCtx);
            if (payload.has(Constants.NAME)) {
                String apiName = payload.get(Constants.NAME).getAsString();
                SynapseConfiguration configuration = msgCtx.getConfiguration();
                API api = configuration.getAPI(apiName);
                if (api != null) {
                    response = Utils.handleTracing(api.getAspectConfiguration(), apiName, axisMsgCtx);
                } else {
                    response = Utils.createJsonError("Specified API ('" + apiName + "') not found", axisMsgCtx,
                            Constants.BAD_REQUEST);
                }
            } else {
                response = Utils.createJsonError("Unsupported operation", axisMsgCtx, Constants.BAD_REQUEST);
            }
            Utils.setJsonPayLoad(axisMsgCtx, response);
        } catch (IOException e) {
            LOG.error("Error when parsing JSON payload", e);
            Utils.setJsonPayLoad(axisMsgCtx, Utils.createJsonErrorObject("Error when parsing JSON payload"));
        }
    }

    private void populateApiList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<API> apis = configuration.getAPIs();

        JSONObject jsonBody = Utils.createJSONList(apis.size());
        for (API api: apis) {
            JSONObject apiObject = new JSONObject();
            String apiUrl = getApiUrl(api, messageContext);
            apiObject.put(Constants.NAME, api.getName());
            apiObject.put(Constants.URL, apiUrl);
            apiObject.put(Constants.TRACING,
                          api.getAspectConfiguration().isTracingEnabled() ? Constants.ENABLED : Constants.DISABLED);
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
        String apiUrl = getApiUrl(api, messageContext);
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

    private String getApiUrl(API api, MessageContext msgCtx) {

        org.apache.axis2.context.MessageContext axisMsgCtx = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        String serverUrl = getServerContext(axisMsgCtx.getConfigurationContext().getAxisConfiguration());
        String versionUrl = "";
        if (URL_VERSION_TYPE.equals(api.getVersionStrategy().getVersionType())) {
            versionUrl = "/" + api.getVersion();
        }
        return serverUrl.equals("err") ? api.getContext() : serverUrl + api.getContext() + versionUrl;
    }

    private String getServerContext(AxisConfiguration configuration) {

        if (!serverContext.isEmpty()) {
            return serverContext;
        }
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
        String url;
        try {
            int port = Integer.parseInt(portValue);
            if (("http".equals(protocol) && port == 80) || ("https".equals(protocol) && port == 443)) {
                port = -1;
            }
            URL serverURL = new URL(protocol, host, port, "");
            url = serverURL.toExternalForm();
        } catch (MalformedURLException e) {
            url = "err";
        }
        this.serverContext = url;
        return url;
    }
}
