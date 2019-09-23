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

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.json.JSONObject;
import org.wso2.micro.integrator.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SequenceResource extends APIResource {

    public SequenceResource(String urlTemplate){
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

        String param = Utils.getQueryParameter(messageContext, "sequenceName");

        if (Objects.nonNull(param)) {
            populateSequenceData(messageContext, param);
        } else {
            populateSequenceList(messageContext);
        }

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateSequenceList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Map<String, SequenceMediator> sequenceMediatorMap = configuration.getDefinedSequences();

        JSONObject jsonBody = Utils.createJSONList(sequenceMediatorMap.size());

        for (SequenceMediator sequence: sequenceMediatorMap.values()) {

            JSONObject sequenceObject = new JSONObject();

            sequenceObject.put(Constants.NAME, sequence.getName());
            sequenceObject.put(Constants.CONTAINER, sequence.getArtifactContainerName());

            String statisticState = sequence.getAspectConfiguration().isStatisticsEnable() ? Constants.ENABLED : Constants.DISABLED;
            sequenceObject.put(Constants.STATS, statisticState);

            String tracingState = sequence.getAspectConfiguration().isTracingEnabled() ? Constants.ENABLED : Constants.DISABLED;
            sequenceObject.put(Constants.TRACING, tracingState);

            jsonBody.getJSONArray(Constants.LIST).put(sequenceObject);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateSequenceData(MessageContext messageContext, String sequenceName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getSequenceByName(messageContext, sequenceName);

        if (Objects.nonNull(jsonBody)) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        }
    }

    private JSONObject getSequenceByName(MessageContext messageContext, String sequenceName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        SequenceMediator sequence = configuration.getDefinedSequences().get(sequenceName);
        return convertSequenceToJsonObject(sequence);
    }

    private JSONObject convertSequenceToJsonObject(SequenceMediator sequenceMediator) {

        if (Objects.isNull(sequenceMediator)) {
            return null;
        }

        JSONObject sequenceObject = new JSONObject();

        sequenceObject.put(Constants.NAME, sequenceMediator.getName());
        sequenceObject.put(Constants.CONTAINER, sequenceMediator.getArtifactContainerName());

        String statisticState = sequenceMediator.getAspectConfiguration().isStatisticsEnable() ? Constants.ENABLED : Constants.DISABLED;
        sequenceObject.put(Constants.STATS, statisticState);

        String tracingState = sequenceMediator.getAspectConfiguration().isTracingEnabled() ? Constants.ENABLED : Constants.DISABLED;
        sequenceObject.put(Constants.TRACING, tracingState);

        List<Mediator> mediators = sequenceMediator.getList();
        String []mediatorTypes = new String[mediators.size()];
        for(int i = 0; i < mediators.size(); i++){
            mediatorTypes[i] = mediators.get(i).getType();
        }
        sequenceObject.put("mediators", mediatorTypes);
        sequenceObject.put(Constants.SYNAPSE_CONFIGURATION,
                new SequenceMediatorSerializer().serializeAnonymousSequence(null, sequenceMediator));

        return sequenceObject;
    }
}
