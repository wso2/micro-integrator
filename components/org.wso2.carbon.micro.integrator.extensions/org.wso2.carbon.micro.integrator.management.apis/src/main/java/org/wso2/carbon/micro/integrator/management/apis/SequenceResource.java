package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class SequenceResource extends APIResource {

    private static Log log = LogFactory.getLog(TaskResource.class);

    private static final String ROOT_ELEMENT_SEQUENCES = "<Sequences></Sequences>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_SEQUENCE = "<Sequence></Sequence>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String CONTAINER_ELEMENT = "<Container></Container>";
    private static final String MEDIATORS_ELEMENT = "<Mediators></Mediators>";
    private static final String MEDIATOR_ELEMENT = "<Mediator></Mediator>";

    public SequenceResource(String urlTemplate){
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
            // if query params exists retrieve data about specific sequence
            if(queryParameter != null){
                for(NameValuePair nvPair : queryParameter){
                    if(nvPair.getName().equals("inboundEndpointName")){
                        populateSequenceData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateSequenceList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }

        return true;
    }

    private void populateSequenceList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Map<String, SequenceMediator> sequenceMediatorMap = configuration.getDefinedSequences();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_SEQUENCES);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(sequenceMediatorMap.size()));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for (SequenceMediator sequence: sequenceMediatorMap.values()) {

            OMElement nameElement = AXIOMUtil.stringToOM(LIST_ITEM);

            String sequenceName = sequence.getName();
            nameElement.setText(sequenceName);

            listElement.addChild(nameElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateSequenceData(MessageContext messageContext, String sequenceName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getSequenceByName(messageContext, sequenceName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private OMElement getSequenceByName(MessageContext messageContext, String sequenceName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        SequenceMediator sequence = configuration.getDefinedSequences().get(sequenceName);

        return convertInboundEndpointToOMElement(sequence);

    }

    private OMElement convertInboundEndpointToOMElement(SequenceMediator sequenceMediator) throws XMLStreamException{

        if(sequenceMediator == null){
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_SEQUENCE);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement containerElement = AXIOMUtil.stringToOM(CONTAINER_ELEMENT);
        OMElement mediatorsElement = AXIOMUtil.stringToOM(MEDIATORS_ELEMENT);

        nameElement.setText(sequenceMediator.getName());
        rootElement.addChild(nameElement);

        containerElement.setText(sequenceMediator.getArtifactContainerName());
        rootElement.addChild(containerElement);

        List<Mediator> mediators = sequenceMediator.getList();

        for(Mediator mediator : mediators){

            OMElement mediatorElement = AXIOMUtil.stringToOM(MEDIATOR_ELEMENT);

            mediatorElement.setText(mediator.getType());
            mediatorsElement.addChild(mediatorElement);
        }

        rootElement.addChild(mediatorsElement);

        return rootElement;

    }

}
