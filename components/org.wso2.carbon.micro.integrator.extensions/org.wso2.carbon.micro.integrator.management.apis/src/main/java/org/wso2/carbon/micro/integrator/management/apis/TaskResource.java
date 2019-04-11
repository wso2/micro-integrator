package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.task.TaskDescription;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class TaskResource extends APIResource {

    private static Log log = LogFactory.getLog(TaskResource.class);

    private static final String ROOT_ELEMENT_TASKS = "<Tasks></Tasks>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

    private static final String ROOT_ELEMENT_TASK = "<Task></Task>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String CLASS_ELEMENT = "<Class></Class>";
    private static final String GROUP_ELEMENT = "<Group></Group>";
    private static final String TRIGGER_COUNT_ELEMENT = "<TriggerCount></TriggerCount>";
    private static final String TRIGGER_INTERVAL_ELEMENT = "<TriggerInterval></TriggerInterval>";
    private static final String TRIGGER_CRON_ELEMENT = "<TriggerCron></TriggerCron>";

    public TaskResource(String urlTemplate){
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
            // if query params exists retrieve data about specific task
            if(queryParameter != null){
                for(NameValuePair nvPair : queryParameter){
                    if(nvPair.getName().equals("taskName")){
                        populateTaskData(messageContext, nvPair.getValue());
                    }
                }
            }else {
                populateTasksList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        }catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }
        return true;
    }

    private void populateTasksList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        String[] taskNames = configuration.getTaskManager().getTaskNames();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_TASKS);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(taskNames.length));
        rootElement.addChild(countElement);
        rootElement.addChild(listElement);

        for(String taskName : taskNames){
            OMElement nameElement = AXIOMUtil.stringToOM(LIST_ITEM);

            nameElement.setText(taskName);

            listElement.addChild(nameElement);

        }

        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateTaskData(MessageContext messageContext, String taskName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getTaskByName(messageContext, taskName);

        if(rootElement != null){
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);

        }else{
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }

    }

    private OMElement getTaskByName(MessageContext messageContext, String taskName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();
        TaskDescription task = configuration.getTaskManager().getTask(taskName);
        return convertTaskToOMElement(task);

    }

    private OMElement convertTaskToOMElement(TaskDescription task) throws XMLStreamException{

        if(task == null){
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_TASK);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement classElement = AXIOMUtil.stringToOM(CLASS_ELEMENT);
        OMElement groupElement = AXIOMUtil.stringToOM(GROUP_ELEMENT);
        OMElement triggerCountElement = AXIOMUtil.stringToOM(TRIGGER_COUNT_ELEMENT);
        OMElement triggerIntervalElement = AXIOMUtil.stringToOM(TRIGGER_INTERVAL_ELEMENT);
        OMElement triggerCronElement = AXIOMUtil.stringToOM(TRIGGER_CRON_ELEMENT);

        nameElement.setText(task.getName());
        rootElement.addChild(nameElement);

        classElement.setText(task.getTaskImplClassName());
        rootElement.addChild(classElement);

        groupElement.setText(task.getTaskGroup());
        rootElement.addChild(groupElement);

        triggerCountElement.setText(String.valueOf(task.getCount()));
        rootElement.addChild(triggerCountElement);

        triggerIntervalElement.setText(String.valueOf(task.getInterval()));
        rootElement.addChild(triggerIntervalElement);

        triggerCronElement.setText(task.getCronExpression());
        rootElement.addChild(triggerCronElement);

        return rootElement;

    }

}
