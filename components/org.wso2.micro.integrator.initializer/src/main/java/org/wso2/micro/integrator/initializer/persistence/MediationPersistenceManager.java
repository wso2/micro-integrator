/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.micro.integrator.initializer.persistence;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.api.API;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationSerializer;
import org.apache.synapse.config.xml.XMLConfigurationSerializer;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages all persistence activities related to mediation configuration. Takes care
 * of saving configurations to the file system, registry and updating such configurations
 * as and when necessary. All the mediation components (proxy services, sequences etc)
 * should use this implementation to handle their persistence requirements. This class
 * does not immediately carry out persistence requests. Requests are first queued up and
 * then executed as batch jobs. Therefore admin services which initiate persistence
 * requests does not have to 'wait' for disk and network I/O often associated with
 * persistence activities. This improves the UI response times and system usability
 * in a great deal.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MediationPersistenceManager {

    private static final Log log = LogFactory.getLog(MediationPersistenceManager.class);


    private boolean initialized = false;
    private String configPath;
    private boolean flatFileMode;    
    private SynapseConfiguration synapseConfiguration;
    private String configName;

    /** Queue to hold persistence requests - Make sure all accesses are synchronized */
    private final LinkedList<PersistenceRequest> requestQueue = new LinkedList<PersistenceRequest>();

    private MediationPersistenceWorker worker;
    private boolean acceptRequests;
    private long interval = 5000L;

    private Map<Integer, AbstractStore> dataStores;

    /**
     * Initialize the mediation persistence manager instance and start accepting
     * and processing persistence requests. Persistence requests are carried out
     * on the local file system and if required on the registry as well.
     *
     * @param configPath Path to the file/directory where configuration should be saved in
     * @param synapseConfiguration synapse configuration to be used
     * @param interval The wait time for the mediation persistence worker thread
     * @param configName Name of the configuration to be used
     */
    public MediationPersistenceManager(String configPath,
                     SynapseConfiguration synapseConfiguration,
                     long interval, String configName) {

        if (initialized) {
            log.warn("Mediation persistence manager is already initialized");
            return;
        }

        if (configPath == null) {
            log.warn("Synapse configuration location is not provided - Unable to initialize " +
                    "the mediation persistence manager.");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Initializing the mediation persistence manager");
        }

        this.configPath = configPath;
        this.synapseConfiguration = synapseConfiguration;
        this.configName = configName;
        if (interval > 0) {
            this.interval = interval;
        } else {
            log.warn("Invalid interval value " + interval + " for the mediation persistence " +
                    "worker, Using defaults");
        }

        File file = new File(configPath);
        flatFileMode = file.exists() && file.isFile();

        initDataStores();
        
        worker = new MediationPersistenceWorker();
        worker.start();

        // and we are ready to launch....
        acceptRequests = true;
        initialized = true;
    }

    private void initDataStores() {
        dataStores = new HashMap<Integer, AbstractStore>();
        dataStores.put(ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE,
                new ProxyServiceStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_SEQUENCE,
                new SequenceStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_ENDPOINT,
                new EndpointStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_TASK,
                new StartupStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_EVENT_SRC,
                new EventSourceStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_ENTRY,
                new LocalEntryStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_REGISTRY,
                new SynapseRegistryStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_EXECUTOR,
                new ExecutorStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_TEMPLATE,
                new TemplateStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_TEMPLATE_ENDPOINTS,
                new EndpointTemplateStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_MESSAGE_STORE,
                new MessageStoreStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR,
                new MessageProcessorStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_REST_API,
                new APIStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_IMPORT,
                new ImportStore(configPath, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_INBOUND,
                new InboundStore(configPath, configName));
    }

    public void destroy() {
        if (!initialized) {
            return;
        }

        // Stop accepting any more persistence requests
        acceptRequests = false;

        if (log.isDebugEnabled()) {
            log.debug("Shutting down mediation persistence manager");
        }

        // Wait till the jobs already in the queue are done
        while (!requestQueue.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) { }
        }

        // Halt the persistence worker thread
        worker.proceed = false;
        if (worker.isAlive()) {
            // If the worker is asleep, wake him up
            worker.interrupt();
        }
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Save changes made to a particular item of the mediation configuration. Changes are
     * saved to the local file system and if required to the registry as well. Types of
     * supported mediation configuration items are defined in ServiceBusConstants. If the
     * item to be saved should be written to its own configuration file, the file name
     * attribute must be set on the item (in the SynapseConfiguration). Leaving the file
     * name as null will cause the item to be persisted into the top level synapse.xml
     * file.
     *
     * @param name Name/ID of the configuration item
     * @param itemType type of the configuration item
     */
    public void saveItem(String name, int itemType) {
        if (!initialized || !acceptRequests) {
            log.warn("Mediation persistence manager is either not initialized or not in the " +
                    "'accepting' mode. Ignoring the save request.");
            return;
        }

        if (itemType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            saveFullConfiguration(false);
            return;
        }

        PersistenceRequest request = new PersistenceRequest(name, itemType, true);
        synchronized (requestQueue) {
            addToQueue(request);
        }
    }

    /**
     * Delete a particular item in the saved mediation configuration. Item is removed from the
     * local file system and if required to the registry as well. Types of
     * supported mediation configuration items are defined in ServiceBusConstants.
     *
     * @param name Name/ID of the configuration item
     * @param fileName Name of the file where the item is currently saved in
     * @param itemType Type of the configuration item
     */
    public void deleteItem(String name, String fileName, int itemType) {
        if (!initialized || !acceptRequests) {
            log.warn("Mediation persistence manager is either not initialized or not in the " +
                    "'accepting' mode. Ignoring the delete request.");
            return;
        }

        if (itemType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            return;
        }

        PersistenceRequest request = new PersistenceRequest(name, fileName, itemType, false);
        synchronized (requestQueue) {
            addToQueue(request);
        }
    }


    /**
     * This method ensures that only the latest persistence request for a particular object
     * will remain in the request queue. This helps reduce I/O overhead during persistence
     * operations by merging multiple save requests to one and giving delete requests priority
     * over save requests.
     *
     * @param request The latest request to be added to queue
     */
    private void addToQueue(PersistenceRequest request) {

        int i = 0;
        boolean matchFound = false;
        for (; i < requestQueue.size(); i++) {
            PersistenceRequest oldRequest = requestQueue.get(i);
            if (oldRequest.subjectType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
                // if a request to save the full configuration is already in the queue
                // we can ignore the current request - Configuration will get saved
                // to the disk anyway
                return;
            }

            if (oldRequest.subjectType == request.subjectType &&
                    oldRequest.subjectId.equals(request.subjectId)) {
                matchFound = true;
                break;
            }
        }

        if (matchFound) {
            // If an older request was found for the same item overwrite it
            requestQueue.remove(i);
            requestQueue.add(i, request);
        } else {
            // Otherwise add the current request to the tail of the queue
            requestQueue.offer(request);
        }
    }

    /**
     * Make a request to save the complete mediation configuration (the entire
     * SynapseConfiguration) to be saved to the file system and the registry.
     * This will remove all the existing requests already in the job queue and add
     * a single new entry.
     *
     * @param registryOnly Whether or not to save the configuration to the registry only
     */
    public void saveFullConfiguration(boolean registryOnly) {
        if (log.isDebugEnabled()) {
            log.debug("Received request to save full mediation configuration");
        }

        PersistenceRequest request = new PersistenceRequest(null,
                ServiceBusConstants.ITEM_TYPE_FULL_CONFIG, true);
        request.registryOnly = registryOnly;
        synchronized (requestQueue) {
            requestQueue.clear();
            requestQueue.offer(request);
        }
    }

    private void handleException(String msg, Throwable t) {
        log.error(msg, t);
        throw new ServiceBusPersistenceException(msg, t);
    }

    private class MediationPersistenceWorker extends Thread {

        boolean proceed = true;

        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Starting the mediation persistence worker thread");
            }

            while (proceed) {
                PersistenceRequest request;

                synchronized (requestQueue) {
                   request = requestQueue.poll();
                }

                if (request == null) {
                    try {
                        sleep(interval);
                    } catch (InterruptedException ignore) {
                        // This condition could occur only during system shutdown.
                        // We can safely ignore this.
                    }

                    // Simply go to the next iteration
                    continue;
                }
                try {
                    //SynapseConfiguration config = synapseConfiguration;
                    if (flatFileMode) {
                        saveToFlatFile(synapseConfiguration);
                    } else if (request.save) {
                        persistElement(synapseConfiguration, request);
                    } else {
                        deleteElement(synapseConfiguration, request);
                    }
                } catch (Throwable t) {
                    // Just log the error and continue
                    // DO NOT throw the error since that will kill the worker thread
                    log.error("Error while saving mediation configuration changes", t);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Stopping the mediation persistence worker thread");
            }
        }
    }

    private void persistElement(SynapseConfiguration config, PersistenceRequest request) {
        if (request.subjectType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            saveFullConfiguration(config);
        } else {
            AbstractStore dataStore = dataStores.get(request.subjectType);
            dataStore.save(request.subjectId, config);
        }
    }

    private void deleteElement(SynapseConfiguration config, PersistenceRequest request) {
        AbstractStore dataStore = dataStores.get(request.subjectType);
        dataStore.delete(request.subjectId, request.fileName, config);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void saveToFlatFile(SynapseConfiguration config) throws IOException,
            XMLStreamException {

        File outputFile = new File(configPath);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(outputFile);
        XMLConfigurationSerializer.serializeConfiguration(config, fos);
        fos.flush();
        fos.close();
    }

    private void saveFullConfiguration(SynapseConfiguration config) {
        if (log.isDebugEnabled()) {
            log.debug("Serializing full mediation configuration to the file system");
        }

        SynapseConfiguration deployedArtifacts = removeCAppArtifactsBeforePersist(config);
        MultiXMLConfigurationSerializer serializer = new MultiXMLConfigurationSerializer(configPath);
        serializer.serialize(config);

        addCAppArtifactsAfterPersist(config, deployedArtifacts);
    }

    /**
     * Add the CApp artifact configuration to the current configuration, after persist the other artifacts into the default location
     *
     * @param synapseConfiguration Current Configuration
     * @param cAppConfig           CApp artifact configuration
     */
    public void addCAppArtifactsAfterPersist(SynapseConfiguration synapseConfiguration,
                                             SynapseConfiguration cAppConfig) {
        final Lock lock = getLock(synapseConfiguration.getAxisConfiguration());

        try {
            lock.lock();
            Map<String, Endpoint> endpoints = cAppConfig.getDefinedEndpoints();
            for (String name : endpoints.keySet()) {
                Endpoint newEndpoint = endpoints.get(name);
                synapseConfiguration.addEndpoint(name, newEndpoint);
            }
            Map<String, SequenceMediator> sequences = cAppConfig.getDefinedSequences();
            for (String name : sequences.keySet()) {
                SequenceMediator newSequences = sequences.get(name);
                synapseConfiguration.addSequence(name, newSequences);
            }

            Collection<ProxyService> proxyServices = cAppConfig.getProxyServices();
            for (ProxyService proxy : proxyServices) {
                // Delete the persisted proxy service
                deleteItem(proxy.getName(), proxy.getFileName(), ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
            }

            Map<String, Entry> localEntries = cAppConfig.getDefinedEntries();
            for (String name : localEntries.keySet()) {
                Entry newEntry = localEntries.get(name);
                synapseConfiguration.addEntry(name, newEntry);
            }

            Collection<MessageStore> messageStores = cAppConfig.getMessageStores().values();
            for (MessageStore store : messageStores) {
                synapseConfiguration.addMessageStore(store.getName(), store);
            }

            Collection<MessageProcessor> messageProcessors = cAppConfig.getMessageProcessors().values();
            for (MessageProcessor processor : messageProcessors) {
                synapseConfiguration.addMessageProcessor(processor.getName(), processor);
            }

            Map<String, TemplateMediator> sequenceTemplates = cAppConfig.getSequenceTemplates();
            for (String name : sequenceTemplates.keySet()) {
                TemplateMediator newTemplate = sequenceTemplates.get(name);
                synapseConfiguration.addSequenceTemplate(name, newTemplate);
            }

            Map<String, Template> endpointTemplates = cAppConfig.getEndpointTemplates();
            for (String name : endpointTemplates.keySet()) {
                Template newEndpointTemplate = endpointTemplates.get(name);
                synapseConfiguration.addEndpointTemplate(name, newEndpointTemplate);
            }

            Collection<API> apiCollection = cAppConfig.getAPIs();
            for (API api : apiCollection) {
                synapseConfiguration.addAPI(api.getName(), api);
                api.init((SynapseEnvironment) synapseConfiguration.getAxisConfiguration()
                        .getParameter(SynapseConstants.SYNAPSE_ENV).getValue());
            }

            Collection<Startup> tasks = cAppConfig.getStartups();
            for (Startup task : tasks) {
                synapseConfiguration.addStartup(task);
            }

            Collection<InboundEndpoint> inboundEndpoints = cAppConfig.getInboundEndpoints();
            for (InboundEndpoint inboundEndpoint : inboundEndpoints) {
                InboundEndpoint newInbound = inboundEndpoint;
                synapseConfiguration.addInboundEndpoint(newInbound.getName(), newInbound);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Bean to store details of a persistence request
     */
    private class PersistenceRequest {

        private boolean save;
        private int subjectType;
        private String subjectId;
        private String fileName;
        private boolean registryOnly;

        public PersistenceRequest(String subjectId, int subjectType, boolean save) {
            this.save = save;
            this.subjectId = subjectId;
            this.subjectType = subjectType;
        }

        public PersistenceRequest(String subjectId, String fileName, int subjectType, boolean save) {
            this.save = save;
            this.subjectId = subjectId;
            this.subjectType = subjectType;
            this.fileName = fileName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PersistenceRequest pr = (PersistenceRequest) o;
            return pr.subjectType == this.subjectType &&
                    pr.save == this.save &&
                    pr.subjectId.equals(this.subjectId);
        }

        @Override
        public int hashCode() {
            int result = (save ? 1 : 0);
            result = 31 * result + subjectType;
            result = 31 * result + (subjectId != null ? subjectId.hashCode() : 0);
            return result;
        }
    }

    /**
     * @param synapseConfiguration Contains all the configuration includes CApp artifact configs
     * @return new Configuration which removes all the CApp related configs
     */
    public SynapseConfiguration removeCAppArtifactsBeforePersist(
            SynapseConfiguration synapseConfiguration) {

        HashMap<String, SynapseConfiguration> returnMap = new HashMap<>();
        final Lock lock = getLock(synapseConfiguration.getAxisConfiguration());
        SynapseConfiguration cAppArtifactConfig = new SynapseConfiguration();

        try {
            lock.lock();
            Map<String, Endpoint> endpoints = synapseConfiguration.getDefinedEndpoints();
            for (String name : endpoints.keySet()) {
                Endpoint ep = endpoints.get(name);
                if (ep != null && ep.getArtifactContainerName() != null) {
                    ep.setIsEdited(true);
                    cAppArtifactConfig.addEndpoint(name, ep);
                    synapseConfiguration.removeEndpoint(name);
                }
            }

            Map<String, SequenceMediator> sequences = synapseConfiguration.getDefinedSequences();
            for (String name : sequences.keySet()) {
                SequenceMediator seq = sequences.get(name);
                if (seq != null && seq.getArtifactContainerName() != null) {
                    seq.setIsEdited(true);
                    cAppArtifactConfig.addSequence(name, seq);
                    synapseConfiguration.removeSequence(name);
                }
            }

            Collection<ProxyService> proxyServices = synapseConfiguration.getProxyServices();
            for (ProxyService proxy : proxyServices) {
                if (proxy != null && proxy.getArtifactContainerName() != null) {
                    proxy.setIsEdited(true);
                    cAppArtifactConfig.addProxyService(proxy.getName(), proxy);
                    // Do not remove proxy service from the synapseConfiguration since the CGAgentAdminService will
                    // Throw an error while it try to find the service
                    // Therefore remove the persisted config xml after persisting
                }
            }

            Map<String, Entry> localEntries = synapseConfiguration.getDefinedEntries();
            for (String name : localEntries.keySet()) {
                Entry localEntry = localEntries.get(name);
                if (localEntry != null && localEntry.getArtifactContainerName() != null) {
                    localEntry.setIsEdited(true);
                    cAppArtifactConfig.addEntry(name, localEntry);
                    synapseConfiguration.removeEntry(name);
                }
            }

            Collection<MessageStore> messageStores = synapseConfiguration.getMessageStores().values();
            for (MessageStore store : messageStores) {
                if (store != null && store.getArtifactContainerName() != null) {
                    store.setIsEdited(true);
                    cAppArtifactConfig.addMessageStore(store.getName(), store);
                    synapseConfiguration.removeMessageStore(store.getName());
                }
            }

            Collection<MessageProcessor> messageProcessors = synapseConfiguration.getMessageProcessors().values();
            for (MessageProcessor processor : messageProcessors) {
                if (processor != null && processor.getArtifactContainerName() != null) {
                    processor.setIsEdited(true);
                    cAppArtifactConfig.addMessageProcessor(processor.getName(), processor);
                    synapseConfiguration.removeMessageProcessor(processor.getName());
                }
            }

            Map<String, TemplateMediator> sequenceTemplates = synapseConfiguration.getSequenceTemplates();
            for (String name : sequenceTemplates.keySet()) {
                TemplateMediator seqTemplate = sequenceTemplates.get(name);
                if (seqTemplate != null && seqTemplate.getArtifactContainerName() != null) {
                    seqTemplate.setIsEdited(true);
                    cAppArtifactConfig.addSequenceTemplate(name, seqTemplate);
                    synapseConfiguration.removeSequenceTemplate(name);
                }
            }

            Map<String, Template> endpointTemplates = synapseConfiguration.getEndpointTemplates();
            for (String name : endpointTemplates.keySet()) {
                Template template = endpointTemplates.get(name);
                if (template != null && template.getArtifactContainerName() != null) {
                    template.setIsEdited(true);
                    cAppArtifactConfig.addEndpointTemplate(name, template);
                    synapseConfiguration.removeEndpointTemplate(name);
                }
            }

            Collection<API> apiCollection = synapseConfiguration.getAPIs();
            for (API api : apiCollection) {
                if (api != null && api.getArtifactContainerName() != null) {
                    api.setIsEdited(true);
                    cAppArtifactConfig.addAPI(api.getName(), api);
                    synapseConfiguration.removeAPI(api.getName());
                }
            }

            Collection<Startup> tasks = synapseConfiguration.getStartups();
            for (Startup task : tasks) {
                if (task != null && task.getArtifactContainerName() != null) {
                    task.setIsEdited(true);
                    cAppArtifactConfig.addStartup(task);
                    synapseConfiguration.removeStartup(task.getName());
                }

            }

            Collection<InboundEndpoint> inboundEndpoints = synapseConfiguration.getInboundEndpoints();
            for (InboundEndpoint inboundEndpoint : inboundEndpoints) {
                if (inboundEndpoint != null && inboundEndpoint.getArtifactContainerName() != null) {
                    inboundEndpoint.setIsEdited(true);
                    cAppArtifactConfig.addInboundEndpoint(inboundEndpoint.getName(), inboundEndpoint);
                    synapseConfiguration.removeInboundEndpoint(inboundEndpoint.getName());
                }
            }
             return cAppArtifactConfig;
        } finally {
            lock.unlock();
        }
    }

    protected Lock getLock(AxisConfiguration axisConfig) {
        Parameter p = axisConfig.getParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
        if (p != null) {
            return (Lock) p.getValue();
        } else {
            log.warn(ServiceBusConstants.SYNAPSE_CONFIG_LOCK + " is null, Recreating a new lock");
            Lock lock = new ReentrantLock();
            try {
                axisConfig.addParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK, lock);
                return lock;
            } catch (AxisFault axisFault) {
                log.error("Error while setting " + ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
            }
        }

        return null;
    }

}
