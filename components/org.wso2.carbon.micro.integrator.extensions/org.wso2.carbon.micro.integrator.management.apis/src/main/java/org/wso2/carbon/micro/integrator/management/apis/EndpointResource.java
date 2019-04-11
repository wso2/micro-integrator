package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.Endpoint;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class EndpointResource extends APIResource {

    private static Log log = LogFactory.getLog(EndpointResource.class);

    private static final String ROOT_ELEMENT_ENDPOINTS = "<Endpoints></Endpoints>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_ENDPOINT = "<Endpoint></Endpoint>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String DESCRIPTION_ELEMENT = "<Description></Description>";
    private static final String CONTAINER_ELEMENT = "<Container></Container>";
    private static final String ENDPOINT_STRING_ELEMENT = "<EndpointString></EndpointString>";

    public EndpointResource(String urlTemplate){
        super(urlTemplate);
        log.info("Created");
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<String>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = getQueryParameters(axis2MessageContext);

        try {
            // if query params exists retrieve data about specific endpoint
            if(queryParameter != null){
                for(NameValuePair nvPair : queryParameter){
                    if(nvPair.getName().equals("endpointName")){
                        populateEndpointData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateEndpointList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }

        return true;

    }

    private void populateEndpointList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Map<String, Endpoint> namedEndpointMap = configuration.getDefinedEndpoints();
        Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_ENDPOINTS);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(namedEndpointCollection.size()));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for(Endpoint ep : namedEndpointCollection){
            OMElement nameElement = AXIOMUtil.stringToOM(LIST_ITEM);

            String epName = ep.getName();
            nameElement.setText(epName);

            listElement.addChild(nameElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateEndpointData(MessageContext messageContext, String endpointName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getEndpointByName(messageContext, endpointName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }


    }

    private OMElement getEndpointByName(MessageContext messageContext, String endpointName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        Endpoint ep = configuration.getEndpoint(endpointName);
        return convertEndpointToOMElement(ep);

    }

    private OMElement convertEndpointToOMElement(Endpoint endpoint) throws XMLStreamException{

        if(endpoint == null){
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_ENDPOINT);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement descriptionElement = AXIOMUtil.stringToOM(DESCRIPTION_ELEMENT);
        OMElement containerElement = AXIOMUtil.stringToOM(CONTAINER_ELEMENT);
        OMElement endpointStringElement = AXIOMUtil.stringToOM(ENDPOINT_STRING_ELEMENT);

        nameElement.setText(endpoint.getName());
        rootElement.addChild(nameElement);

        descriptionElement.setText(endpoint.getDescription());
        rootElement.addChild(descriptionElement);

        containerElement.setText(endpoint.getArtifactContainerName());
        rootElement.addChild(containerElement);

        endpointStringElement.setText(EndpointSerializer.getElementFromEndpoint(endpoint).toString());
        rootElement.addChild(endpointStringElement);

        return rootElement;

    }

}
