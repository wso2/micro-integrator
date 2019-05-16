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

import org.apache.http.NameValuePair;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SequenceResource extends APIResource {

    public SequenceResource(String urlTemplate){
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
//        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = Utils.getQueryParameters(axis2MessageContext);

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
        return true;
    }

    private void populateSequenceList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Map<String, SequenceMediator> sequenceMediatorMap = configuration.getDefinedSequences();

        JSONObject jsonBody = new JSONObject();
        JSONArray sequenceList = new JSONArray();
        jsonBody.put("count", sequenceMediatorMap.size());
        jsonBody.put("list", sequenceList);

        for (SequenceMediator sequence: sequenceMediatorMap.values()) {

            JSONObject sequenceObject = new JSONObject();

            sequenceObject.put("name", sequence.getName());
            sequenceObject.put("container", sequence.getArtifactContainerName());

            String statisticState = sequence.getAspectConfiguration().isStatisticsEnable() ? "enabled" : "disabled";
            sequenceObject.put("stats", statisticState);

            String tracingState = sequence.getAspectConfiguration().isTracingEnabled() ? "enabled" : "disabled";
            sequenceObject.put("tracing", tracingState);

            sequenceList.put(sequenceObject);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateSequenceData(MessageContext messageContext, String sequenceName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getSequenceByName(messageContext, sequenceName);

        if (null != jsonBody) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private JSONObject getSequenceByName(MessageContext messageContext, String sequenceName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        SequenceMediator sequence = configuration.getDefinedSequences().get(sequenceName);
        return convertInboundEndpointToOMElement(sequence);
    }

    private JSONObject convertInboundEndpointToOMElement(SequenceMediator sequenceMediator) {

        if (null == sequenceMediator) {
            return null;
        }

        JSONObject sequenceObject = new JSONObject();

        sequenceObject.put("name", sequenceMediator.getName());
        sequenceObject.put("container", sequenceMediator.getArtifactContainerName());

        String statisticState = sequenceMediator.getAspectConfiguration().isStatisticsEnable() ? "enabled" : "disabled";
        sequenceObject.put("stats", statisticState);

        String tracingState = sequenceMediator.getAspectConfiguration().isTracingEnabled() ? "enabled" : "disabled";
        sequenceObject.put("tracing", tracingState);

        List<Mediator> mediators = sequenceMediator.getList();
        String []mediatorTypes = new String[mediators.size()];
        for(int i = 0; i < mediators.size(); i++){
            mediatorTypes[i] = mediators.get(i).getType();
        }
        sequenceObject.put("mediators", mediatorTypes);

        return sequenceObject;
    }
}
