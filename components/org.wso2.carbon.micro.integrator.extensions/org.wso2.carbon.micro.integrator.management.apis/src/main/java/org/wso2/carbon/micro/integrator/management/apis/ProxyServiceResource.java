package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.ProxyService;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class ProxyServiceResource extends APIResource {

    private static Log log = LogFactory.getLog(ProxyServiceResource.class);

    private static final String ROOT_ELEMENT_PROXY_SERVICES = "<ProxyServices></ProxyServices>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_PROXY_SERVICE = "<ProxyService></ProxyService>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String DESCRIPTION_ELEMENT = "<Description></Description>";
    private static final String IN_SEQUENCE_ELEMENT = "<InSequence></InSequence>";
    private static final String OUT_SEQUENCE_ELEMENT = "<OutSequence></OutSequence>";
    private static final String FAULT_SEQUENCE_ELEMENT = "<FaultSequence></FaultSequence>";
    private static final String TRANSPORTS_ELEMENT = "<Transports></Transports>";
    private static final String VALUE_ELEMENT = "<Value></Value>";
    private static final String ENDPOINT_ELEMENT = "<Endpoint></Endpoint>";

    public ProxyServiceResource(String urlTemplate){
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

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = getQueryParameters(axis2MessageContext);

        try {
            // if query params exists retrieve data about specific inbound endpoint
            if(queryParameter != null){
                for(NameValuePair nvPair : queryParameter){
                    if(nvPair.getName().equals("proxyServiceName")){
                        populateProxyServiceData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateProxyServiceList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }

        return true;

    }

    private void populateProxyServiceList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_PROXY_SERVICES);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        Collection<ProxyService> proxyServices = configuration.getProxyServices();

        countElement.setText(String.valueOf(proxyServices.size()));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for(ProxyService proxyService : proxyServices){
            OMElement nameElement = AXIOMUtil.stringToOM(LIST_ITEM);

            String epName = proxyService.getName();
            nameElement.setText(epName);

            listElement.addChild(nameElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }


    private void populateProxyServiceData(MessageContext messageContext, String proxyServiceName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getProxyServiceByName(messageContext, proxyServiceName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }


    }

    private OMElement getProxyServiceByName(MessageContext messageContext, String proxyServiceName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        ProxyService proxyService = configuration.getProxyService(proxyServiceName);
        return convertProxyServiceToOMElement(proxyService);

    }

    private OMElement convertProxyServiceToOMElement(ProxyService proxyService) throws XMLStreamException{

        if(proxyService == null){
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_PROXY_SERVICE);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement descriptionElement = AXIOMUtil.stringToOM(DESCRIPTION_ELEMENT);
        OMElement inSequenceElement = AXIOMUtil.stringToOM(IN_SEQUENCE_ELEMENT);
        OMElement outSequenceElement = AXIOMUtil.stringToOM(OUT_SEQUENCE_ELEMENT);
        OMElement faultSequenceElement = AXIOMUtil.stringToOM(FAULT_SEQUENCE_ELEMENT);
        OMElement endpointSequenceElement = AXIOMUtil.stringToOM(ENDPOINT_ELEMENT);
        OMElement transportsElement = AXIOMUtil.stringToOM(TRANSPORTS_ELEMENT);

        nameElement.setText(proxyService.getName());
        rootElement.addChild(nameElement);

        descriptionElement.setText(proxyService.getDescription());
        rootElement.addChild(descriptionElement);

        inSequenceElement.setText(proxyService.getTargetInSequence());
        rootElement.addChild(inSequenceElement);

        outSequenceElement.setText(proxyService.getTargetOutSequence());
        rootElement.addChild(outSequenceElement);

        faultSequenceElement.setText(proxyService.getTargetFaultSequence());
        rootElement.addChild(faultSequenceElement);

        endpointSequenceElement.setText(proxyService.getTargetEndpoint());
        rootElement.addChild(endpointSequenceElement);

        rootElement.addChild(transportsElement);

        ArrayList list = proxyService.getTransports();

        for(Object o : list){
            OMElement valueElement = AXIOMUtil.stringToOM(VALUE_ELEMENT);
            valueElement.setText((String) o);
            transportsElement.addChild(valueElement);
        }

        return rootElement;

    }

}
