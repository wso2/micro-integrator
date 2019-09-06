/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MessageProcessorSerializer;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.impl.failover.FailoverScheduledMessageForwardingProcessor;
import org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor;
import org.apache.synapse.message.processor.impl.sampler.SamplingProcessor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.wso2.carbon.micro.integrator.management.apis.Constants.ACTIVE_STATUS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.INACTIVE_STATUS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.NAME;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.STATUS;
import static org.wso2.carbon.micro.integrator.management.apis.Constants.SYNAPSE_CONFIGURATION;

/**
 * Represents message processor resources defined in the synapse configuration.
 */
public class MessageProcessorResource extends APIResource {

    private static final Log LOG = LogFactory.getLog(MessageProcessorResource.class);

    //Message processor types
    private static final String SAMPLING_PROCESSOR_TYPE = "Sampling-processor";
    private static final String SCHEDULED_MESSAGE_FORWARDING_TYPE = "Scheduled-message-forwarding-processor";
    private static final String FAILOVER_SCHEDULED_MESSAGE_FORWARDING_TYPE = "Failover-scheduled-message-forwarding-processor";
    private static final String CUSTOM_PROCESSOR_TYPE = "Custom-message-processor";
    //Constants for message-processor JSON object
    private static final String TYPE_PROPERTY = "type";
    private static final String CONTAINER_PROPERTY = "artifactContainer";
    private static final String FILE_NAME_PROPERTY = "fileName";
    private static final String PARAMETER_PROPERTY = "parameters";
    private static final String MESSAGE_STORE_PROPERTY = "messageStore";
    //HTTP method types supported by the resource
    Set<String> methods;

    public MessageProcessorResource(String urlTemplate) {
        super(urlTemplate);
        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_POST);
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {
        //Building message since POST requests are facilitated
        buildMessage(messageContext);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        if (messageContext.isDoingGET()) {
            String nameParameter = Utils.getQueryParameter(messageContext, NAME);
            if (Objects.nonNull(nameParameter)) {
                populateMessageProcessorData(messageContext, nameParameter);
            } else {
                populateMessageProcessorList(messageContext);
            }
            axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        } else if (Utils.isDoingPOST(axis2MessageContext)) {
            try {
                if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
                    return false;
                }
                JsonObject payload = getJsonPayload(messageContext);
                if (payload.has(NAME) && payload.has(STATUS)) {
                     changeProcessorStatus(messageContext, payload);
                } else {
                    Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Missing parameters in payload"));
                }
            } catch (IOException e) {
                LOG.error("Error when parsing JSON payload", e);
                Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Error when parsing JSON payload"));
            }
        }
        return true;
    }

    /**
     * Create a JSON response with all available message processors.
     *
     * @param messageContext synapse message context
     */
    private void populateMessageProcessorList(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        SynapseConfiguration synapseConfiguration = messageContext.getConfiguration();
        Map<String, MessageProcessor> processorMap = synapseConfiguration.getMessageProcessors();
        JSONObject jsonBody = Utils.createJSONList(processorMap.size());
        processorMap.forEach((key, value) -> addToJSONList(value, jsonBody.getJSONArray(Constants.LIST)));
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    /**
     * Create JSON response with information related a defined message processor.
     *
     * @param messageContext synapse message context
     * @param name           name of the message processor
     */
    private void populateMessageProcessorData(MessageContext messageContext, String name) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        SynapseConfiguration synapseConfiguration = messageContext.getConfiguration();
        MessageProcessor messageProcessor = synapseConfiguration.getMessageProcessors().get(name);
        if (Objects.nonNull(messageProcessor)) {
            Utils.setJsonPayLoad(axis2MessageContext, getMessageProcessorAsJson(messageProcessor));
        } else {
            Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Message processor does not exist"));
        }
    }

    /**
     * Adds the specified message processor to a JSON list.
     *
     * @param messageProcessor reference to message processor
     * @param processorList    JSON list of processors
     */
    private void addToJSONList(MessageProcessor messageProcessor, JSONArray processorList) {
        JSONObject messageProcessorObject = new JSONObject();
        messageProcessorObject.put(Constants.NAME, messageProcessor.getName());
        messageProcessorObject.put(TYPE_PROPERTY, getMessageProcessorType(messageProcessor));
        messageProcessorObject.put(STATUS, getProcessorState(messageProcessor.isDeactivated()));
        processorList.put(messageProcessorObject);
    }

    /**
     * Returns the JSON representation of a message processor.
     *
     * @param messageProcessor reference to message processor
     * @return JSONObject MessageProcessor
     */
    private JSONObject getMessageProcessorAsJson(MessageProcessor messageProcessor) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put(Constants.NAME, messageProcessor.getName());
        jsonBody.put(TYPE_PROPERTY, getMessageProcessorType(messageProcessor));
        jsonBody.put(CONTAINER_PROPERTY, messageProcessor.getArtifactContainerName());
        jsonBody.put(FILE_NAME_PROPERTY, messageProcessor.getFileName());
        jsonBody.put(PARAMETER_PROPERTY, messageProcessor.getParameters());
        jsonBody.put(MESSAGE_STORE_PROPERTY, messageProcessor.getMessageStoreName());
        jsonBody.put(STATUS, getProcessorState(messageProcessor.isDeactivated()));
        jsonBody.put(SYNAPSE_CONFIGURATION, MessageProcessorSerializer.serializeMessageProcessor(null, messageProcessor));

        return jsonBody;
    }

    /**
     * Returns the type of the message processor.
     *
     * @param messageProcessor reference to message processor
     * @return String type of the message processor
     */
    private String getMessageProcessorType(MessageProcessor messageProcessor) {
        if (messageProcessor instanceof ScheduledMessageForwardingProcessor) {
            return SCHEDULED_MESSAGE_FORWARDING_TYPE;
        } else if (messageProcessor instanceof FailoverScheduledMessageForwardingProcessor) {
            return FAILOVER_SCHEDULED_MESSAGE_FORWARDING_TYPE;
        } else if (messageProcessor instanceof SamplingProcessor) {
            return SAMPLING_PROCESSOR_TYPE;
        } else {
            return CUSTOM_PROCESSOR_TYPE;
        }
    }

    /**
     * Changes the processor state to the specified state.
     *
     * @param messageContext Axis2message context
     * @param jsonObject     message payload
     */
    private void changeProcessorStatus(MessageContext messageContext, JsonObject jsonObject) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        SynapseConfiguration synapseConfiguration = messageContext.getConfiguration();
        String processorName = jsonObject.get(NAME).getAsString();
        String status = jsonObject.get(STATUS).getAsString();

        MessageProcessor messageProcessor = synapseConfiguration.getMessageProcessors().get(processorName);
        if (Objects.nonNull(messageProcessor)) {
            JSONObject jsonResponse = new JSONObject();
            if (INACTIVE_STATUS.equalsIgnoreCase(status)) {
                messageProcessor.deactivate();
                jsonResponse.put(Constants.MESSAGE_JSON_ATTRIBUTE, processorName + " : is deactivated");
            } else if (ACTIVE_STATUS.equalsIgnoreCase(status)) {
                messageProcessor.activate();
                jsonResponse.put(Constants.MESSAGE_JSON_ATTRIBUTE, processorName + " : is activated");
            } else {
                jsonResponse.put(Constants.MESSAGE_JSON_ATTRIBUTE, "Provided state is not valid");
            }
            Utils.setJsonPayLoad(axis2MessageContext, jsonResponse);
        } else {
            LOG.warn("Specified message processor does not exist");
            Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Message processor does not exist"));
        }

    }

    /**
     * Returns the JSON payload of a given message.
     *
     * @param messageContext synapseMessageContext
     * @return JsonObject payload
     */
    private JsonObject getJsonPayload(MessageContext messageContext) throws IOException {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        InputStream jsonStream = JsonUtil.getJsonPayload(axis2MessageContext);
        String jsonString = IOUtils.toString(jsonStream);
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

    /**
     * Returns the state of the MessageProcessor.
     *
     * @param isDeactivated state of the message processor
     * @return String state
     */
    private String getProcessorState(Boolean isDeactivated) {
        if (isDeactivated) {
            return INACTIVE_STATUS;
        }
        return ACTIVE_STATUS;
    }
}
