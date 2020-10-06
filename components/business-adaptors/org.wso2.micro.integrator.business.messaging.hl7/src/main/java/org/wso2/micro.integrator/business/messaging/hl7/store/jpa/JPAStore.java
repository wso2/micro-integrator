/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.business.messaging.hl7.store.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.MessageProducer;
import org.apache.synapse.message.store.Constants;
import org.apache.synapse.message.store.MessageStore;
import org.wso2.micro.integrator.business.messaging.hl7.store.entity.PersistentHL7Message;
import org.wso2.micro.integrator.business.messaging.hl7.store.util.SerializableMessageContext;
import org.wso2.micro.integrator.business.messaging.hl7.store.util.SerializerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class JPAStore implements MessageStore {

    private static final Log logger = LogFactory.getLog(JPAStore.class.getName());

    /**
     * synapse environment reference
     */
    protected SynapseEnvironment synapseEnvironment;

    private boolean isInitialized = false;
    private String name;
    private String description;
    private Map<String, Object> parameters;
    private String fileName;

    private int maxProducerId = Integer.MAX_VALUE;
    /**
     * Message producer id
     */
    private AtomicInteger producerId = new AtomicInteger(0);
    /**
     * Message consumer id
     */
    private AtomicInteger consumerId = new AtomicInteger(0);

    private EntityManagerFactory entityManagerFactory;

    private ThreadLocal<EntityManager> localEntityManager = new ThreadLocal();

    private Properties jpaProperties = new Properties();

    private void parseParameters() {

        for (String key : parameters.keySet()) {
            this.jpaProperties.put(key, parameters.get(key));
        }

        if (!this.jpaProperties.containsKey("openjpa.FetchBatchSize")) {
            this.jpaProperties.put("openjpa.FetchBatchSize", "1000");
        }
        if (!this.jpaProperties.containsKey("openjpa.jdbc.ResultSetType")) {
            this.jpaProperties.put("openjpa.jdbc.ResultSetType", "scroll-insensitive");
        }
        if (!this.jpaProperties.containsKey("openjpa.jdbc.FetchDirection")) {
            this.jpaProperties.put("openjpa.jdbc.FetchDirection", "forward");
        }
        if (!this.jpaProperties.containsKey("openjpa.jdbc.LRSSize")) {
            this.jpaProperties.put("openjpa.jdbc.LRSSize", "last");
        }
        if (!this.jpaProperties.containsKey("openjpa.Multithreaded")) {
            this.jpaProperties.put("openjpa.Multithreaded", "true");
        }
        if (!this.jpaProperties.containsKey("openjpa.MetaDataFactory")) {
            this.jpaProperties.put("openjpa.MetaDataFactory",
                                   "jpa(Types=" + PersistentHL7Message.class.getName() + ")");
        }
        if (!this.jpaProperties.containsKey("openjpa.jdbc.SynchronizeMappings")) {
            this.jpaProperties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        }

        if (!this.jpaProperties.contains("openjpa.Log")) {
            this.jpaProperties.put("openjpa.Log", "none");
        }

    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        if (synapseEnvironment == null) {
            logger.error("Cannot initialize HL7 JPA Store");
            return;
        }

        this.synapseEnvironment = synapseEnvironment;

        parseParameters();

        this.isInitialized = initJPAStore();

        if (this.isInitialized) {
            logger.info(toString() + ". Initialized... ");
        } else {
            logger.warn(toString() + ". Initialization Failed... ");
        }
    }

    public EntityManager getEntityManager() {
        EntityManager em = localEntityManager.get();

        if (em == null) {
            em = entityManagerFactory.createEntityManager();
            localEntityManager.set(em);
        }

        return em;
    }

    public void closeEntityManager() {
        EntityManager em = localEntityManager.get();
        if (em != null) {
            em.close();
            localEntityManager.set(null);
        }
    }

    // TODO: add exception handling and logging
    private boolean initJPAStore() {
        // Create a new EntityManagerFactory using the System properties.
        Properties prop = new Properties();
        entityManagerFactory = Persistence.createEntityManagerFactory("hl7store", this.jpaProperties);

        getEntityManager();

        return true;
    }

    @Override
    public void destroy() {
        closeEntityManager();

        if (this.entityManagerFactory != null && this.entityManagerFactory.isOpen()) {
            this.entityManagerFactory.close();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public MessageProducer getProducer() {
        JPAProducer producer = new JPAProducer(this);
        producer.setId(nextProducerId());

        return producer;
    }

    @Override
    public MessageConsumer getConsumer() {
        return null;
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    @Override
    public void addParameter(String s, String s1) {

    }

    @Override
    public void addParameterKey(String s, String s1) {

    }

    @Override
    public String getParameterKey(String s) {
        return null;
    }

    @Override
    public Map<String, String> getParameterKeyMap() {
        return null;
    }

    @Override
    public int getType() {
        return Constants.JDBC_MS;
    }

    @Override
    public int size() {
        EntityManager manager = getEntityManager();
        Query q = manager.createQuery("SELECT x FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "'");
        int size = q.getResultList().size();
        return size;
    }

    public PersistentHL7Message getMessage(String messageId) {
        EntityManager manager = getEntityManager();
        Query q = manager.createQuery(
                "SELECT x FROM " + getTableName() + " x WHERE x.messageId='" + messageId + "' AND x.storeName='"
                        + getName() + "'");

        PersistentHL7Message result = (PersistentHL7Message) q.getSingleResult();
        return result;
    }

    public List<PersistentHL7Message> getMessages() {
        EntityManager manager = getEntityManager();
        Query q = manager.createQuery(
                "SELECT x FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "' ORDER BY x.date DESC");

        List<PersistentHL7Message> result = q.getResultList();
        return result;
    }

    public List<PersistentHL7Message> search(String query) {
        EntityManager manager = getEntityManager();
        Query q = manager.createQuery("SELECT x FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "' "
                                              + "AND (x.controlId LIKE '" + query + "' OR x.messageId LIKE '" + query
                                              + "') ORDER BY x.date DESC");

        List<PersistentHL7Message> result = q.getResultList();
        return result;
    }

    public SynapseEnvironment getSynapseEnvironment() {
        return synapseEnvironment;
    }

    public List<PersistentHL7Message> getMessages(int pageNumber, int rowsPerPage) {

        pageNumber = pageNumber - 1; // offset

        int itemsPerPageInt = rowsPerPage;
        int numberOfPages = (int) Math.ceil((double) this.size() / itemsPerPageInt);

        if (numberOfPages == 0) {
            numberOfPages = 1;
        }
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }

        int startIndex = (pageNumber * itemsPerPageInt);

        EntityManager manager = getEntityManager();
        Query q = manager.createQuery(
                "SELECT x FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "' ORDER BY x.date DESC");

        q.setFirstResult(startIndex);
        q.setMaxResults(itemsPerPageInt);

        List<PersistentHL7Message> result = q.getResultList();
        return result;
    }

    public int flushMessages() {
        EntityManager manager = getEntityManager();
        manager.getTransaction().begin();

        Query q = manager.createQuery("DELETE FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "'");

        int deleted = q.executeUpdate();
        manager.getTransaction().commit();
        return deleted;
    }

    @Override
    public MessageContext remove() throws NoSuchElementException {
        throw new UnsupportedOperationException("HL7 store does not support this operation.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("HL7 store does not support this operation.");
    }

    @Override
    public MessageContext remove(String s) {
        throw new UnsupportedOperationException("HL7 store does not support this operation.");
    }

    @Override
    public MessageContext get(int i) {
        EntityManager manager = getEntityManager();
        try {
            Query q = manager.createQuery(
                    "SELECT x FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "'");

            List<PersistentHL7Message> messages = q.getResultList();
            SerializableMessageContext serializableMessageContext =
                    (SerializableMessageContext) SerializerUtils.deserialize(messages.get(i).getMessage());
            return retrieveMessageContext(serializableMessageContext);

        } catch (IOException e) {
            logger.error("Error while deserializing message context object. " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("Error while deserializing message context object. " + e.getMessage());
        }

        return null;
    }

    public org.apache.axis2.context.MessageContext newAxis2Mc() {
        return ((Axis2SynapseEnvironment) synapseEnvironment).getAxis2ConfigurationContext().createMessageContext();
    }

    public org.apache.synapse.MessageContext newSynapseMc(org.apache.axis2.context.MessageContext msgCtx) {
        SynapseConfiguration configuration = synapseEnvironment.getSynapseConfiguration();
        return new Axis2MessageContext(msgCtx, configuration, synapseEnvironment);
    }

    @Override
    public List<MessageContext> getAll() {
        EntityManager manager = getEntityManager();
        Query q = manager.createQuery("SELECT x FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "'");

        List<PersistentHL7Message> messages = q.getResultList();
        return retrieveMessageContextList(messages);
    }

    private List<MessageContext> retrieveMessageContextList(List<PersistentHL7Message> persistentHL7Messages) {
        List<MessageContext> messageContexts = new ArrayList<MessageContext>();

        for (PersistentHL7Message message : persistentHL7Messages) {
            try {
                SerializableMessageContext serializableMessageContext =
                        (SerializableMessageContext) SerializerUtils.deserialize(message.getMessage());
                messageContexts.add(retrieveMessageContext(serializableMessageContext));
            } catch (ClassNotFoundException e) {
                logger.error("Error while deserializing message context object. " + e.getMessage());
            } catch (IOException e) {
                logger.error("Error while deserializing message context object. " + e.getMessage());
            }
        }

        return messageContexts;
    }

    private MessageContext retrieveMessageContext(SerializableMessageContext message) {
        org.apache.axis2.context.MessageContext axis2Mc = this.newAxis2Mc();
        MessageContext synapseMc = this.newSynapseMc(axis2Mc);

        return SerializerUtils.toMessageContext(message, axis2Mc, synapseMc);
    }

    @Override
    public MessageContext get(String s) {
        EntityManager manager = getEntityManager();
        try {
            Query q = manager.createQuery(
                    "SELECT x FROM " + getTableName() + " x WHERE x.storeName='" + getName() + "' AND x.messageId='" + s
                            + "'");

            PersistentHL7Message message = (PersistentHL7Message) q.getSingleResult();
            SerializableMessageContext serializableMessageContext =
                    (SerializableMessageContext) SerializerUtils.deserialize(message.getMessage());
            return retrieveMessageContext(serializableMessageContext);

        } catch (IOException e) {
            logger.error("Error while deserializing message context object. " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("Error while deserializing message context object. " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean isEdited() {
        return false;
    }

    @Override
    public void setIsEdited(boolean b) {

    }

    @Override
    public String getArtifactContainerName() {
        return null;
    }

    @Override
    public void setArtifactContainerName(String s) {

    }

    private String getTableName() {
        return PersistentHL7Message.class.getSimpleName();
    }

    @Override
    public String toString() {
        return "HL7 Store [" + getName() + "]";
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    private int nextConsumerId() {
        int id = consumerId.incrementAndGet();
        return id;
    }

    private int nextProducerId() {
        int id = producerId.incrementAndGet();
        if (id == maxProducerId) {
            logger.info("Setting producer ID generator to 0...");
            producerId.set(0);
            id = producerId.incrementAndGet();
        }
        return id;
    }
}
