package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class ApiResource extends APIResource {

    private static Log log = LogFactory.getLog(ApiResource.class);

    private static final String ROOT_ELEMENT_APIS = "<APIs></APIs>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_API = "<API></API>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String CONTEXT_ELEMENT = "<Context></Context>";
    private static final String HOST_ELEMENT = "<Host></Host>";
    private static final String PORT_ELEMENT = "<Port></Port>";
    private static final String FILENAME_ELEMENT = "<FileName></FileName>";
    private static final String VERSION_ELEMENT = "<Version></Version>";

    private static final String RESOURCES_ELEMENT = "<Resources></Resources>";
    private static final String RESOURCE_ELEMENT = "<Resource></Resource>";
    private static final String METHOD_ELEMENT = "<Methods></Methods>";
    private static final String STYLE_ELEMENT = "<Style></Style>";
    private static final String TEMPLATE_ELEMENT = "<Template></Template>";
    private static final String MAPPING_ELEMENT = "<Mapping></Mapping>";
    private static final String INSEQ_ELEMENT = "<Inseq></Inseq>";
    private static final String OUTSEQ_ELEMENT = "<Outseq></Outseq>";
    private static final String FAULT_ELEMENT = "<Faultseq></Faultseq>";


    public ApiResource(String urlTemplate){
        super(urlTemplate);
        log.info("Created");
    }

    public Set<String> getMethods() {

        Set<String> methods = new HashSet<String>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = getQueryParameters(axis2MessageContext);

        try {

            if(queryParameter != null){
                for(NameValuePair nvPair : queryParameter){
                    if(nvPair.getName().equals("apiName")){
                        populateApiData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateApiList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
           log.error("Error occurred while processing response", e);
        }

        return true;
    }

    private void populateApiList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        Collection<API> apis = configuration.getAPIs();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_APIS);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(apis.size()));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for (API api: apis) {

            OMElement nameElement = AXIOMUtil.stringToOM(LIST_ITEM);

            String apiName = api.getAPIName();
            nameElement.setText(apiName);

            listElement.addChild(nameElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateApiData(MessageContext messageContext, String apiName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getApiByName(messageContext, apiName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }

    }

    private OMElement getApiByName(MessageContext messageContext, String apiName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        API api = configuration.getAPI(apiName);
        return convertApiToOMElement(api);

    }

    private OMElement convertApiToOMElement(API api) throws XMLStreamException{

        if (api == null) {
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_API);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement contextElement = AXIOMUtil.stringToOM(CONTEXT_ELEMENT);
        OMElement hostElement = AXIOMUtil.stringToOM(HOST_ELEMENT);
        OMElement portElement = AXIOMUtil.stringToOM(PORT_ELEMENT);
        OMElement fileNameElement = AXIOMUtil.stringToOM(FILENAME_ELEMENT);
        OMElement versionElement = AXIOMUtil.stringToOM(VERSION_ELEMENT);

        nameElement.setText(api.getName());
        rootElement.addChild(nameElement);

        contextElement.setText(api.getContext());
        rootElement.addChild(contextElement);

        hostElement.setText(api.getHost());
        rootElement.addChild(hostElement);

        portElement.setText(String.valueOf(api.getPort()));
        rootElement.addChild(portElement);

        fileNameElement.setText(api.getFileName());
        rootElement.addChild(fileNameElement);

        versionElement.setText(api.getVersion());
        rootElement.addChild(versionElement);

        OMElement resourcesElement = AXIOMUtil.stringToOM(RESOURCES_ELEMENT);
        rootElement.addChild(resourcesElement);

        Resource[] resources = api.getResources();

        for (Resource resource : resources) {

            OMElement resourceElement = AXIOMUtil.stringToOM(RESOURCE_ELEMENT);
            resourcesElement.addChild(resourceElement);

            OMElement methodElement = AXIOMUtil.stringToOM(METHOD_ELEMENT);
            resourceElement.addChild(methodElement);

            OMElement styleElement = AXIOMUtil.stringToOM(STYLE_ELEMENT);
            resourceElement.addChild(styleElement);

            OMElement templateElement = AXIOMUtil.stringToOM(TEMPLATE_ELEMENT);
            resourceElement.addChild(templateElement);

            OMElement mappingElement = AXIOMUtil.stringToOM(MAPPING_ELEMENT);
            resourceElement.addChild(mappingElement);

            OMElement inSeqElement = AXIOMUtil.stringToOM(INSEQ_ELEMENT);
            resourceElement.addChild(inSeqElement);

            OMElement outSeqElement = AXIOMUtil.stringToOM(OUTSEQ_ELEMENT);
            resourceElement.addChild(outSeqElement);

            OMElement faultSeqElement = AXIOMUtil.stringToOM(FAULT_ELEMENT);
            resourceElement.addChild(faultSeqElement);

            String[] methods = resource.getMethods();

            for(String method : methods){
                OMElement itemElement = AXIOMUtil.stringToOM(LIST_ITEM);
                itemElement.setText(method);
                methodElement.addChild(itemElement);
            }


            DispatcherHelper dispatcherHelper = resource.getDispatcherHelper();
            if (dispatcherHelper instanceof URITemplateHelper) {
                styleElement.setText("URL-Template");
                templateElement.setText(dispatcherHelper.getString());

            } else if (dispatcherHelper instanceof URLMappingHelper) {
                styleElement.setText("URL-mapping");
                mappingElement.setText(dispatcherHelper.getString());
            }

//            if (resource.getInSequenceKey() != null) {
//                data.setInSequenceKey(resource.getInSequenceKey());
//            } else if (resource.getInSequence() != null) {
//                data.setInSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
//                        resource.getInSequence(), "inSequence").toString());
//            }
//
//            if (resource.getOutSequenceKey() != null) {
//                data.setOutSequenceKey(resource.getOutSequenceKey());
//            } else if (resource.getOutSequence() != null) {
//                data.setOutSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
//                        resource.getOutSequence(), "outSequence").toString());
//            }
//
//            if (resource.getFaultSequenceKey() != null) {
//                data.setFaultSequenceKey(resource.getFaultSequenceKey());
//            } else if (resource.getFaultSequence() != null) {
//                data.setFaultSeqXml(RestApiAdminUtils.createAnonymousSequenceElement(
//                        resource.getFaultSequence(), "faultSequence").toString());
//            }
//            data.setUserAgent(resource.getUserAgent());
//            resourceDatas[i] = data;
        }
        return rootElement;
    }

}
