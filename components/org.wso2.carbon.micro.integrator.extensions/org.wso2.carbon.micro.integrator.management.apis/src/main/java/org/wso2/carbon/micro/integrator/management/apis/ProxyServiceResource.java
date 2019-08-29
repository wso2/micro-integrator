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

import com.google.gson.JsonObject;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.ProxyServiceSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.ProxyService;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.service.mgt.ServiceMetaData;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.wso2.carbon.micro.integrator.management.apis.Constants.ACTIVE_STATUS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.INACTIVE_STATUS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.NAME;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.STATUS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.SYNAPSE_CONFIGURATION;

public class ProxyServiceResource extends APIResource {

    private static Log LOG = LogFactory.getLog(ProxyServiceResource.class);

    public ProxyServiceResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        if (messageContext.isDoingGET()) {
            String param = Utils.getQueryParameter(messageContext, "proxyServiceName");
            if (Objects.nonNull(param)) {
                populateProxyServiceData(messageContext, param);
            } else {
                populateProxyServiceList(messageContext);
            }
            axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        } else {
            try {
                if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
                    Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("JSON payload is missing"));
                    return  true;
                }
                JsonObject payload = Utils.getJsonPayload(axis2MessageContext);
                if (payload.has(NAME) && payload.has(STATUS)) {
                    changeProxyState(messageContext, axis2MessageContext, payload);
                } else {
                    Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Missing parameters in payload"));
                }
            } catch (IOException e) {
                LOG.error("Error when parsing JSON payload", e);
                Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Error when parsing JSON payload"));
            }
        }
        return true;
    }

    private void populateProxyServiceList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<ProxyService> proxyServices = configuration.getProxyServices();

        JSONObject jsonBody = Utils.createJSONList(proxyServices.size());

        for (ProxyService proxyService : proxyServices) {

            JSONObject proxyObject = new JSONObject();

            try {
                ServiceMetaData data = new ServiceAdmin().getServiceData(proxyService.getName());

                proxyObject.put(Constants.NAME, proxyService.getName());

                String []wsdlUrls = data.getWsdlURLs();
                proxyObject.put("wsdl1_1", wsdlUrls[0]);
                proxyObject.put("wsdl2_0", wsdlUrls[1]);

            } catch (Exception e) {
                LOG.error("Error occurred while processing service data", e);
            }

            jsonBody.getJSONArray(Constants.LIST).put(proxyObject);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }


    private void populateProxyServiceData(MessageContext messageContext, String proxyServiceName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getProxyServiceByName(messageContext, proxyServiceName);

        if (Objects.nonNull(jsonBody)) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        }
    }

    private JSONObject getProxyServiceByName(MessageContext messageContext, String proxyServiceName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        ProxyService proxyService = configuration.getProxyService(proxyServiceName);
        return convertProxyServiceToJsonObject(proxyService);
    }

    private JSONObject convertProxyServiceToJsonObject(ProxyService proxyService) {

        if (Objects.isNull(proxyService)) {
            return null;
        }

        JSONObject proxyObject = new JSONObject();

        proxyObject.put(Constants.NAME, proxyService.getName());

        try {
            ServiceMetaData data = new ServiceAdmin().getServiceData(proxyService.getName());

            String []wsdlUrls = data.getWsdlURLs();

            proxyObject.put("wsdl1_1", wsdlUrls[0]);
            proxyObject.put("wsdl2_0", wsdlUrls[1]);
        } catch (Exception e) {
            LOG.error("Error occurred while processing service data", e);
        }

        String statisticState = proxyService.getAspectConfiguration().isStatisticsEnable() ? Constants.ENABLED : Constants.DISABLED;
        proxyObject.put(Constants.STATS, statisticState);

        String tracingState = proxyService.getAspectConfiguration().isTracingEnabled() ? Constants.ENABLED : Constants.DISABLED;
        proxyObject.put(Constants.TRACING, tracingState);

        OMElement proxyConfiguration = ProxyServiceSerializer.serializeProxy(null, proxyService);
        proxyObject.put(SYNAPSE_CONFIGURATION, proxyConfiguration.toString());
        proxyObject.put("eprs", proxyService.getAxisService().getEPRs());
        return proxyObject;
    }

    /**
     * Change the state of the proxy based on the payload.
     *
     * @param messageContext      Synapse message context
     * @param axis2MessageContext AXIS2 message context
     * @param payload             json payload
     */
    private void changeProxyState(MessageContext messageContext,
                                  org.apache.axis2.context.MessageContext axis2MessageContext, JsonObject payload) {

        SynapseConfiguration synapseConfiguration = messageContext.getConfiguration();
        String name = payload.get(NAME).getAsString();
        String status = payload.get(STATUS).getAsString();
        ProxyService proxyService = synapseConfiguration.getProxyService(name);
        if (proxyService == null) {
            Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Proxy service could not be found."));
            return;
        }
        List pinnedServers = proxyService.getPinnedServers();
        JSONObject jsonResponse = new JSONObject();
        if (ACTIVE_STATUS.equalsIgnoreCase(status)) {
            if (pinnedServers.isEmpty() ||
                    pinnedServers.contains(getServerConfigInformation(synapseConfiguration).getServerName())) {
                proxyService.start(synapseConfiguration);
                jsonResponse.put("Message", "Proxy service " + name + " started successfully");
                Utils.setJsonPayLoad(axis2MessageContext, jsonResponse);
            }
        } else if (INACTIVE_STATUS.equalsIgnoreCase(status)) {
            if (pinnedServers.isEmpty() ||
                    pinnedServers.contains(getServerConfigInformation(synapseConfiguration).getSynapseXMLLocation())) {
                proxyService.stop(synapseConfiguration);
                jsonResponse.put("Message", "Proxy service " + name + " stopped successfully");
                Utils.setJsonPayLoad(axis2MessageContext, jsonResponse);
            }
        } else {
            Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Defined state is invalid."));
        }
    }

    /**
     * Return ServerConfigurationInformation of a given SynapseConfiguration.
     *
     * @param synapseConfiguration synapse configuration of the proxy
     * @return ServerConfigurationInformation
     */
    private ServerConfigurationInformation getServerConfigInformation(SynapseConfiguration synapseConfiguration) {

        return (ServerConfigurationInformation) synapseConfiguration.getAxisConfiguration().
                getParameter(SynapseConstants.SYNAPSE_SERVER_CONFIG_INFO).getValue();
    }

}
