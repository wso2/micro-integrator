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
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.DefaultEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.endpoints.FailoverEndpoint;
import org.apache.synapse.endpoints.HTTPEndpoint;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;
import org.apache.synapse.endpoints.RecipientListEndpoint;
import org.apache.synapse.endpoints.TemplateEndpoint;
import org.apache.synapse.endpoints.WSDLEndpoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EndpointResource extends APIResource {

    private static final String CHILDREN_ATTRIBUTE = "children";

    public EndpointResource(String urlTemplate) {

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
        return getEndpointAsJson(ep);
    }

    /**
     * Returns the json representation of the endpoint based on its type.
     *
     * @param endpoint endpoint
     * @return json-object with endpoint details
     */
    private JSONObject getEndpointAsJson(Endpoint endpoint) {

        JSONObject endpointObject = new JSONObject();
        endpointObject.put(Constants.NAME, endpoint.getName());
        OMElement synapseConfiguration = EndpointSerializer.getElementFromEndpoint(endpoint);
        endpointObject.put(Constants.SYNAPSE_CONFIGURATION, synapseConfiguration);

        if (endpoint instanceof AddressEndpoint) {
            populateAddressEndpointData((AddressEndpoint) endpoint, endpointObject);
        } else if (endpoint instanceof DefaultEndpoint) {
            populateDefaultEndpointData((DefaultEndpoint) endpoint, endpointObject);
        } else if (endpoint instanceof FailoverEndpoint) {
            populateFailoverEndpointData((FailoverEndpoint) endpoint, endpointObject);
        } else if (endpoint instanceof HTTPEndpoint) {
            populateHTTPEndpointData((HTTPEndpoint) endpoint, endpointObject);
        } else if (endpoint instanceof LoadbalanceEndpoint) {
            populateLoadBalanceEndpointData((LoadbalanceEndpoint) endpoint, endpointObject);
        } else if (endpoint instanceof RecipientListEndpoint) {
            populateRecipientListEndpointData((RecipientListEndpoint) endpoint, endpointObject);
        } else if (endpoint instanceof TemplateEndpoint) {
            populateTemplateEndpointData((TemplateEndpoint) endpoint, endpointObject);
        } else if (endpoint instanceof WSDLEndpoint) {
            populateWsdlEndpointData((WSDLEndpoint) endpoint, endpointObject);
        }
        return endpointObject;
    }

    /**
     * Populate information specific for default endpoints to json object.
     */
    private void populateDefaultEndpointData(DefaultEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "Default Endpoint");
        endpointObject.put(CHILDREN_ATTRIBUTE, endpoint.getChildren());
        setAdvancedProperties(endpointObject, endpoint.getDefinition());
    }

    /**
     * Populate information specific for addressing endpoints to json object.
     */
    private void populateAddressEndpointData(AddressEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "Addressing Endpoint");
        endpointObject.put("address", endpoint.getDefinition().getAddress());
        setAdvancedProperties(endpointObject, endpoint.getDefinition());
    }

    /**
     * Populate information specific for wsdl endpoints to json object.
     */
    private void populateWsdlEndpointData(WSDLEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "WSDL Endpoint");
        endpointObject.put("wsdlUri", endpoint.getWsdlURI());
        endpointObject.put("serviceName", endpoint.getServiceName());
        endpointObject.put("portName", endpoint.getPortName());
        setAdvancedProperties(endpointObject, endpoint.getDefinition());
    }

    /**
     * Populate information specific for template endpoints to json object.
     */
    private void populateTemplateEndpointData(TemplateEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "Template Endpoint");
        endpointObject.put("parameters", endpoint.getParameters());
        endpointObject.put("template", endpoint.getTemplate());
    }

    /**
     * Populate information specific for recipient-list endpoints to json object.
     */
    private void populateRecipientListEndpointData(RecipientListEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "RecipientListEndpoint");
        endpointObject.put("poolSize", endpoint.getCurrentPoolSize());
        List<Endpoint> children = endpoint.getChildren();
        endpointObject.put(CHILDREN_ATTRIBUTE, populateEndpointChildren(children));
    }

    /**
     * Populate information specific for load-balance endpoints to json object.
     */
    private void populateLoadBalanceEndpointData(LoadbalanceEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "Load Balance Endpoint");
        endpointObject.put("algorithm", endpoint.getAlgorithm().getName());
        endpointObject.put("buildMessage", endpoint.isBuildMessageAtt());
        endpointObject.put("isFailover", endpoint.isFailover());
        List<Endpoint> children = endpoint.getChildren();
        endpointObject.put(CHILDREN_ATTRIBUTE, populateEndpointChildren(children));
    }

    /**
     * populate information of a given list of child endpoints.
     *
     * @param children list of child endpoints
     * @return json array with child endpoint information
     */
    private JSONArray populateEndpointChildren(List<Endpoint> children) {

        JSONArray childrenJsonList = new JSONArray();
        for (Endpoint child : children) {
            childrenJsonList.put(getEndpointAsJson(child));
        }
        return childrenJsonList;
    }

    /**
     * Populate information specific for HTTP endpoints to json object.
     */
    private void populateHTTPEndpointData(HTTPEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "HTTP Endpoint");
        endpointObject.put("method", endpoint.getHttpMethod());
        endpointObject.put("uriTemplate", endpoint.getUriTemplate().expand());
        endpointObject.put("errorHandler", endpoint.getErrorHandler());
        setAdvancedProperties(endpointObject, endpoint.getDefinition());
    }

    /**
     * Populate information specific for HTTP endpoints to json object.
     */
    private void populateFailoverEndpointData(FailoverEndpoint endpoint, JSONObject endpointObject) {

        endpointObject.put(Constants.TYPE, "Failover Endpoint");
        List<Endpoint> children = endpoint.getChildren();
        endpointObject.put(CHILDREN_ATTRIBUTE, populateEndpointChildren(children));
    }

    /**
     * Set advanced properties of the endpoint to json object.
     */
    private void setAdvancedProperties(JSONObject jsonObject, EndpointDefinition definition) {

        JSONObject advancedProps = new JSONObject();
        jsonObject.put("advanced", advancedProps);
        setSuspendStateProperties(definition, advancedProps);
        setTimeoutStateProperties(definition, advancedProps);
    }

    /**
     * Set time-out state properties of the endpoint to json object.
     */
    private void setTimeoutStateProperties(EndpointDefinition definition, JSONObject advancedProps) {

        JSONObject timeoutStateProps = new JSONObject();
        advancedProps.put("timeoutState", timeoutStateProps);
        timeoutStateProps.put("errorCodes", definition.getTimeoutErrorCodes());
        timeoutStateProps.put("reties", definition.getRetriesOnTimeoutBeforeSuspend());
    }

    /**
     * Set suspend state properties of the endpoint to json object.
     */
    private void setSuspendStateProperties(EndpointDefinition definition, JSONObject advancedProps) {

        JSONObject suspendStatePros = new JSONObject();
        advancedProps.put("suspendState", suspendStatePros);
        suspendStatePros.put("errorCodes", definition.getSuspendErrorCodes());
        suspendStatePros.put("maxDuration", definition.getSuspendMaximumDuration());
        suspendStatePros.put("initialDuration", definition.getInitialSuspendDuration());
    }
}
