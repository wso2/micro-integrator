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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MessageStoreSerializer;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore;
import org.apache.synapse.message.store.impl.jms.JmsStore;
import org.apache.synapse.message.store.impl.memory.InMemoryStore;
import org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore;
import org.apache.synapse.message.store.impl.resequencer.ResequenceMessageStore;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.wso2.carbon.micro.integrator.management.apis.Constants.SYNAPSE_CONFIGURATION;

/**
 * Represents Message store resource defined in the synapse configuration.
 **/
public class MessageStoreResource implements MiApiResource {

    private static final Log LOG = LogFactory.getLog(MessageStoreResource.class);

    //Message store types
    private static final String JDBCMESSAGESTORE_TYPE = "jdbc-message-store";
    private static final String JMSSTORE_TYPE = "jms-message-store";
    private static final String INMEMORYSTORE_TYPE = "in-memory-message-store";
    private static final String RABBITMQSTORE_TYPE = "rabbitmq-message-store";
    private static final String RESEQUENCEMESSAGESTORE_TYPE = "resequence-message-store";
    private static final String CUSTOMSTORE_TYPE = "custom-message-store";
    //Constants for message-processor JSON object
    private static final String STORE_TYPE_PROPERTY = "type";
    private static final String CONTAINER_ATTRIBUTE = "container";
    private static final String FILE_NAME_ATTRIBUTE = "file";
    private static final String CONSUMER_ATTRIBUTE = "consumer";
    private static final String PRODUCER_ATTRIBUTE = "producer";
    private static final String PROPERTIES_ATTRIBUTE = "properties";
    private static final String STORE_SIZE_ATTRIBUTE = "size";
    //HTTP method types supported by the resource
    Set<String> methods;

    public MessageStoreResource() {
        methods = new HashSet<>();
        methods.add("GET");
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext,
                          org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {

        String messageStoreName = Utils.getQueryParameter(messageContext, Constants.NAME);
        if (Objects.nonNull(messageStoreName)) {
            populateMessageStoreData(axis2MessageContext, synapseConfiguration, messageStoreName);
        } else {
            populateMessageStoreList(axis2MessageContext, synapseConfiguration);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    /**
     * Sets the list of all available message stores to the response as json
     *
     * @param axis2MessageContext AXIS2 message context
     * @param synapseConfiguration Synapse configuration object
     */
    private void populateMessageStoreList(org.apache.axis2.context.MessageContext axis2MessageContext,
                                          SynapseConfiguration synapseConfiguration) {
        Map<String, MessageStore> storeMap = synapseConfiguration.getMessageStores();
        JSONObject jsonBody = Utils.createJSONList(storeMap.size());
        storeMap.forEach((key, value) ->
                addToJsonList(jsonBody.getJSONArray(Constants.LIST), value));
        Utils.setJsonPayLoad(axis2MessageContext, jsonBody);
    }

    /**
     * Sets the information of the specified message store to the response as json
     *
     * @param axis2MessageContext AXIS2 message context
     * @param synapseConfiguration Synapse configuration object
     * @param messageStoreName           messageStoreName of the message store
     */
    private void populateMessageStoreData(org.apache.axis2.context.MessageContext axis2MessageContext,
                                          SynapseConfiguration synapseConfiguration,
                                          String messageStoreName) {
        MessageStore messageStore = synapseConfiguration.getMessageStore(messageStoreName);
        if (Objects.nonNull(messageStore)) {
            Utils.setJsonPayLoad(axis2MessageContext, getMessageStoreAsJson(messageStore));
        } else {
            LOG.warn("Message store " + messageStoreName + " does not exist");
            Utils.setJsonPayLoad(axis2MessageContext, Utils.createJsonErrorObject("Message store does not exist"));
        }
    }

    /**
     * Adds the provided message store to the json array
     * @param list json array
     * @param messageStore message store
     * */
    private void addToJsonList(JSONArray list, MessageStore messageStore) {
        JSONObject messageStoreObject = new JSONObject();
        messageStoreObject.put(Constants.NAME, messageStore.getName());
        messageStoreObject.put(STORE_TYPE_PROPERTY, getStoreType(messageStore));
        messageStoreObject.put(STORE_SIZE_ATTRIBUTE, messageStore.size());
        list.put(messageStoreObject);
    }

    /**
     * Returns the type of the message store
     * @param messageStore message store
     * @return String message store type
     * */
    private String getStoreType(MessageStore messageStore) {

        if (messageStore instanceof ResequenceMessageStore) {
            return RESEQUENCEMESSAGESTORE_TYPE;
        } else if (messageStore instanceof JDBCMessageStore) {
            return JDBCMESSAGESTORE_TYPE;
        } else if (messageStore instanceof JmsStore) {
            return JMSSTORE_TYPE;
        } else if (messageStore instanceof InMemoryStore) {
            return INMEMORYSTORE_TYPE;
        } else if (messageStore instanceof RabbitMQStore) {
            return RABBITMQSTORE_TYPE;
        } else {
            return CUSTOMSTORE_TYPE;
        }
    }

    /**
     * Returns the json representation of the message store
     * @param messageStore message store
     * @return JSONObject json representation of message store
     * */
    private JSONObject getMessageStoreAsJson(MessageStore messageStore) {
        JSONObject jsonObject  = new JSONObject();
        jsonObject.put(Constants.NAME, messageStore.getName());
        jsonObject.put(CONTAINER_ATTRIBUTE, messageStore.getArtifactContainerName());
        jsonObject.put(FILE_NAME_ATTRIBUTE, messageStore.getFileName());
        jsonObject.put(CONSUMER_ATTRIBUTE, messageStore.getConsumer());
        jsonObject.put(PRODUCER_ATTRIBUTE, messageStore.getProducer());
        jsonObject.put(PROPERTIES_ATTRIBUTE, messageStore.getParameters());
        jsonObject.put(STORE_SIZE_ATTRIBUTE, messageStore.size());
        jsonObject.put(SYNAPSE_CONFIGURATION, MessageStoreSerializer.serializeMessageStore(null, messageStore));

        return jsonObject;
    }
}
