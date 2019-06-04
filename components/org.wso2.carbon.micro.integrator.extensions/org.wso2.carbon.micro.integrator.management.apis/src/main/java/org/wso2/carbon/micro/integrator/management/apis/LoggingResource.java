package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LoggingResource extends ApiResource {

    public LoggingResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);

        String requestPayload = messageContext.getEnvelope().getBody().toString();

        // axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

}
