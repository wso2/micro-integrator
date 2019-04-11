package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.mediation.initializer.ServiceBusInitializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class CarbonAppResource extends APIResource {

    private static Log log = LogFactory.getLog(CarbonAppResource.class);

    private static final String ROOT_ELEMENT_CARBON_APPS = "<Applications></Applications>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_CARBON_APP = "<Application></Application>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String ARTIFACTS_ELEMENT = "<Artifacts></Artifacts>";
    private static final String ARTIFACT_ELEMENT = "<Artifact></Artifact>";
    private static final String VERSION_ELEMENT = "<Version></Version>";
    private static final String TYPE_ELEMENT = "<Type></Type>";

    public CarbonAppResource(String urlTemplate){
        super(urlTemplate);
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
                    if(nvPair.getName().equals("carbonAppName")){
                        populateCarbonAppData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateCarbonAppList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }

        return true;
    }

    private void populateCarbonAppList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        // TODO Fix this to use the getTenantIdString
        String tenantId = String.valueOf(MultitenantConstants.SUPER_TENANT_ID);

        ArrayList<CarbonApplication> appList
                = ServiceBusInitializer.getAppManager().getCarbonApps(tenantId);

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_CARBON_APPS);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(appList.size()));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for (CarbonApplication app: appList) {

            OMElement nameElement = AXIOMUtil.stringToOM(LIST_ITEM);

            String apiName = app.getAppName();
            nameElement.setText(apiName);

            listElement.addChild(nameElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateCarbonAppData(MessageContext messageContext, String carbonAppName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getCarbonAppByName(messageContext, carbonAppName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }


    }

    private OMElement getCarbonAppByName(MessageContext messageContext, String carbonAppName) throws XMLStreamException {

        // TODO Fix this to use the getTenantIdString
        String tenantId = String.valueOf(MultitenantConstants.SUPER_TENANT_ID);

        ArrayList<CarbonApplication> appList
                = ServiceBusInitializer.getAppManager().getCarbonApps(tenantId);

        for (CarbonApplication app: appList) {

            if(app.getAppName().equals(carbonAppName)){
                return convertCarbonAppToOMElement(app);
            }

        }

        return null;

    }

    private OMElement convertCarbonAppToOMElement(CarbonApplication carbonApp) throws XMLStreamException{

        if(carbonApp == null){
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_CARBON_APP);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement versionElement = AXIOMUtil.stringToOM(VERSION_ELEMENT);
        OMElement artifactsElement = AXIOMUtil.stringToOM(ARTIFACTS_ELEMENT);

        nameElement.setText(carbonApp.getAppName());
        rootElement.addChild(nameElement);

        versionElement.setText(carbonApp.getAppVersion());
        rootElement.addChild(versionElement);

        rootElement.addChild(artifactsElement);

        List<Artifact.Dependency> dependencies = carbonApp.getAppConfig().
                getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dependency : dependencies) {

            Artifact artifact = dependency.getArtifact();

            String type = artifact.getType();
            String artifactName = artifact.getName();

            // if the artifactName is null, artifact deployment has failed..
            if (artifactName == null) {
                continue;
            }

            OMElement artifactElement = AXIOMUtil.stringToOM(ARTIFACT_ELEMENT);

            nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
            OMElement typeElement = AXIOMUtil.stringToOM(TYPE_ELEMENT);

            nameElement.setText(artifactName);
            typeElement.setText(type);

            artifactElement.addChild(nameElement);
            artifactElement.addChild(typeElement);

            artifactsElement.addChild(artifactElement);

        }

        return rootElement;

    }

}
