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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.ProxyService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.service.mgt.ServiceMetaData;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProxyServiceResource extends APIResource {

    private static Log log = LogFactory.getLog(ProxyServiceResource.class);

    public ProxyServiceResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = Utils.getQueryParameters(axis2MessageContext);

        // if query params exists retrieve data about specific inbound endpoint
        if (Objects.nonNull(queryParameter)) {
            for (NameValuePair nvPair : queryParameter) {
                if (nvPair.getName().equals("proxyServiceName")) {
                    populateProxyServiceData(messageContext, nvPair.getValue());
                    break;
                }
            }
        } else {
            populateProxyServiceList(messageContext);
        }

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateProxyServiceList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<ProxyService> proxyServices = configuration.getProxyServices();

        JSONObject jsonBody = new JSONObject();
        JSONArray proxyList = new JSONArray();
        jsonBody.put(Constants.COUNT, proxyServices.size());
        jsonBody.put(Constants.LIST, proxyList);

        for (ProxyService proxyService : proxyServices) {

            JSONObject proxyObject = new JSONObject();

            try {
                ServiceMetaData data = new ServiceAdmin().getServiceData(proxyService.getName());

                proxyObject.put(Constants.NAME, proxyService.getName());

                String []wsdlUrls = data.getWsdlURLs();
                proxyObject.put("wsdl1_1", wsdlUrls[0]);
                proxyObject.put("wsdl2_0", wsdlUrls[1]);

            } catch (Exception e) {
                log.error("Error occurred while processing service data", e);
            }

            proxyList.put(proxyObject);
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
        return convertProxyServiceToOMElement(proxyService);
    }

    private JSONObject convertProxyServiceToOMElement(ProxyService proxyService) {

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
            log.error("Error occurred while processing service data", e);
        }

        String statisticState = proxyService.getAspectConfiguration().isStatisticsEnable() ? Constants.ENABLED : Constants.DISABLED;
        proxyObject.put(Constants.STATS, statisticState);

        String tracingState = proxyService.getAspectConfiguration().isTracingEnabled() ? Constants.ENABLED : Constants.DISABLED;
        proxyObject.put(Constants.TRACING, tracingState);

        return proxyObject;
    }
}
