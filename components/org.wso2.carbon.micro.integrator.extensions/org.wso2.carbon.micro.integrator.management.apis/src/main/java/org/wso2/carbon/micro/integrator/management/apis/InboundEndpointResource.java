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

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.inbound.InboundEndpointSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundEndpoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.wso2.carbon.micro.integrator.management.apis.Constants.SYNAPSE_CONFIGURATION;

public class InboundEndpointResource extends APIResource {

    public InboundEndpointResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String param = Utils.getQueryParameter(messageContext, "inboundEndpointName");

        if (Objects.nonNull(param)) {
            populateInboundEndpointData(messageContext, param);
        } else {
            populateInboundEndpointList(messageContext);
        }

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateInboundEndpointList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<InboundEndpoint> inboundEndpoints = configuration.getInboundEndpoints();

        JSONObject jsonBody = Utils.createJSONList(inboundEndpoints.size());

        for (InboundEndpoint inboundEndpoint : inboundEndpoints) {

            JSONObject inboundObject = new JSONObject();

            inboundObject.put(Constants.NAME, inboundEndpoint.getName());
            inboundObject.put("protocol", inboundEndpoint.getProtocol());

            jsonBody.getJSONArray(Constants.LIST).put(inboundObject);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateInboundEndpointData(MessageContext messageContext, String inboundEndpointName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getInboundEndpointByName(messageContext, inboundEndpointName);

        if (Objects.nonNull(jsonBody)) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        }
    }

    private JSONObject getInboundEndpointByName(MessageContext messageContext, String inboundEndpointName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        InboundEndpoint ep = configuration.getInboundEndpoint(inboundEndpointName);
        return convertInboundEndpointToJsonObject(ep);
    }

    private JSONObject convertInboundEndpointToJsonObject(InboundEndpoint inboundEndpoint) {

        if (Objects.isNull(inboundEndpoint)) {
            return null;
        }
        JSONObject inboundObject = new JSONObject();
        inboundObject.put(Constants.NAME, inboundEndpoint.getName());
        inboundObject.put("protocol", inboundEndpoint.getProtocol());
        inboundObject.put("sequence", inboundEndpoint.getInjectingSeq());
        inboundObject.put("error", inboundEndpoint.getOnErrorSeq());

        String statisticState = inboundEndpoint.getAspectConfiguration().isStatisticsEnable() ? Constants.ENABLED : Constants.DISABLED;
        inboundObject.put(Constants.STATS, statisticState);

        String tracingState = inboundEndpoint.getAspectConfiguration().isTracingEnabled() ? Constants.ENABLED : Constants.DISABLED;
        inboundObject.put(Constants.TRACING, tracingState);
        inboundObject.put(SYNAPSE_CONFIGURATION, InboundEndpointSerializer.serializeInboundEndpoint(inboundEndpoint));

        JSONArray parameterListObject = new JSONArray();

        inboundObject.put("parameters", parameterListObject);

        Map<String, String> params = inboundEndpoint.getParametersMap();

        for (Map.Entry<String,String> param : params.entrySet()) {

            JSONObject paramObject = new JSONObject();

            paramObject.put(Constants.NAME, param.getKey());
            paramObject.put("value", param.getValue());

            parameterListObject.put(paramObject);
        }
        return inboundObject;
    }
}
