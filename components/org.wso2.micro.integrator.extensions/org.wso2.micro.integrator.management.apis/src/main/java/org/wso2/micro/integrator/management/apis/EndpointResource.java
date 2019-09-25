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
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.Endpoint;
import org.json.JSONObject;
import org.wso2.micro.integrator.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EndpointResource extends APIResource {

    private static final String CHILDREN_ATTRIBUTE = "children";

    public EndpointResource(String urlTemplate){
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

        String param = Utils.getQueryParameter(messageContext, "endpointName");

        if (Objects.nonNull(param)) {
            populateEndpointData(messageContext, param);
        } else {
            populateEndpointList(messageContext);
        }

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateEndpointList(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Map<String, Endpoint> namedEndpointMap = configuration.getDefinedEndpoints();
        Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();

        JSONObject jsonBody = Utils.createJSONList(namedEndpointCollection.size());

        for (Endpoint ep : namedEndpointCollection) {

            JSONObject endpointObject = new JSONObject();

            String epName = ep.getName();
            endpointObject.put(Constants.NAME, epName);

            OMElement element = EndpointSerializer.getElementFromEndpoint(ep);
            OMElement firstElement = element.getFirstElement();

            String type = firstElement.getLocalName();
            endpointObject.put(Constants.TYPE, type);

            jsonBody.getJSONArray(Constants.LIST).put(endpointObject);
        }
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    private void populateEndpointData(MessageContext messageContext, String endpointName) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        JSONObject jsonBody = getEndpointByName(messageContext, endpointName);

        if (Objects.nonNull(jsonBody)) {
            Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
        } else {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.NOT_FOUND);
        }
    }

    private JSONObject getEndpointByName(MessageContext messageContext, String endpointName) {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        Endpoint ep = configuration.getEndpoint(endpointName);
        if (Objects.nonNull(ep)) {
            return getEndpointAsJson(ep);
        } else {
            return null;
        }
    }

    /**
     * Returns the json representation of the endpoint based on its type.
     *
     * @param endpoint endpoint
     * @return json-object with endpoint details
     */
    private JSONObject getEndpointAsJson(Endpoint endpoint) {

        JSONObject endpointObject = endpoint.getJsonRepresentation();
        OMElement synapseConfiguration = EndpointSerializer.getElementFromEndpoint(endpoint);
        endpointObject.put(Constants.SYNAPSE_CONFIGURATION, synapseConfiguration);

        return endpointObject;
    }

}
