package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundEndpoint;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class InboundEndpointResource extends APIResource {

    private static Log log = LogFactory.getLog(InboundEndpointResource.class);

    private static final String ROOT_ELEMENT_INBOUND_ENDPOINTS = "<InboundEndpoints></InboundEndpoints>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_INBOUND_ENDPOINT = "<InboundEndpoint></InboundEndpoint>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String CLASS_ELEMENT = "<Class></Class>";
    private static final String PROTOCOL_ELEMENT = "<Protocol></Protocol>";
    private static final String SEQUENCE_ELEMENT = "<Sequence></Sequence>";
    private static final String ERROR_SEQUENCE_ELEMENT = "<ErrorSequence></ErrorSequence>";

    private static final String PARAMETERS_ELEMENT = "<Parameters></Parameters>";
    private static final String PARAMETER_ELEMENT = "<Parameter></Parameter>";
    private static final String KEY_ELEMENT = "<Name></Name>";
    private static final String VALUE_ELEMENT = "<Value></Value>";

    public InboundEndpointResource(String urlTemplate){
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
            // if query params exists retrieve data about specific inbound endpoint
            if(queryParameter != null){
                for(NameValuePair nvPair : queryParameter){
                    if(nvPair.getName().equals("inboundEndpointName")){
                        populateInboundEndpointData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateInboundEndpointList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }

        return true;
    }

    private void populateInboundEndpointList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<InboundEndpoint> inboundEndpoints = configuration.getInboundEndpoints();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_INBOUND_ENDPOINTS);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(inboundEndpoints.size()));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for(InboundEndpoint inboundEndpoint : inboundEndpoints){
            OMElement nameElement = AXIOMUtil.stringToOM(LIST_ITEM);

            String epName = inboundEndpoint.getName();
            nameElement.setText(epName);

            listElement.addChild(nameElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateInboundEndpointData(MessageContext messageContext, String inboundEndpointName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getInboundEndpointByName(messageContext, inboundEndpointName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }


    }

    private OMElement getInboundEndpointByName(MessageContext messageContext, String inboundEndpointName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        InboundEndpoint ep = configuration.getInboundEndpoint(inboundEndpointName);
        return convertInboundEndpointToOMElement(ep);

    }

    private OMElement convertInboundEndpointToOMElement(InboundEndpoint inboundEndpoint) throws XMLStreamException{

        if(inboundEndpoint == null){
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_INBOUND_ENDPOINT);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement classElement = AXIOMUtil.stringToOM(CLASS_ELEMENT);
        OMElement protocolElement = AXIOMUtil.stringToOM(PROTOCOL_ELEMENT);
        OMElement sequenceElement = AXIOMUtil.stringToOM(SEQUENCE_ELEMENT);
        OMElement errorSequenceElement = AXIOMUtil.stringToOM(ERROR_SEQUENCE_ELEMENT);
        OMElement parametersElement = AXIOMUtil.stringToOM(PARAMETERS_ELEMENT);

        nameElement.setText(inboundEndpoint.getName());
        rootElement.addChild(nameElement);

        classElement.setText(inboundEndpoint.getClassImpl());
        rootElement.addChild(classElement);

        protocolElement.setText(inboundEndpoint.getProtocol());
        rootElement.addChild(protocolElement);

        sequenceElement.setText(inboundEndpoint.getInjectingSeq());
        rootElement.addChild(sequenceElement);

        errorSequenceElement.setText(inboundEndpoint.getOnErrorSeq());
        rootElement.addChild(errorSequenceElement);

        rootElement.addChild(parametersElement);

        Map<String, String> params = inboundEndpoint.getParametersMap();

        for(Map.Entry<String,String> param : params.entrySet()){

            OMElement parameterElement = AXIOMUtil.stringToOM(PARAMETER_ELEMENT);

            OMElement keyElement = AXIOMUtil.stringToOM(KEY_ELEMENT);
            OMElement valueElement = AXIOMUtil.stringToOM(VALUE_ELEMENT);

            keyElement.setText(param.getKey());
            valueElement.setText(param.getValue());

            parameterElement.addChild(keyElement);
            parameterElement.addChild(valueElement);
            parametersElement.addChild(parameterElement);
        }

        return rootElement;

    }
}
