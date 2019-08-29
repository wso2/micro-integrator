/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.micro.integrator.initializer.configurations;

import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.micro.integrator.initializer.ServiceBusConstants;
import org.wso2.carbon.micro.integrator.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationSerializer;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.Startup;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.AxisFault;

import java.util.Collection;

public class ConfigurationUtils {
    private static Log log = LogFactory.getLog(ConfigurationUtils.class);

    public static String getParameter(String name) {
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }

        ServerConfiguration serverConf = ServerConfiguration.getInstance();
        return serverConf.getFirstProperty(name);
    }
    
    public static boolean isInitialStartup(UserRegistry registry) {
        // this logic checks whether this is a initial startup or not
        // by reading a registry META-INF resource
        try {
            if (registry.resourceExists(ServiceBusConstants.META_INF_REGISTRY_PATH)) {
                Resource resource = registry.get(ServiceBusConstants.META_INF_REGISTRY_PATH);
                if (resource != null && ServiceBusConstants.SERIALIZED_TO_REGISTRY.equals(
                        resource.getProperty(ServiceBusConstants.CONFIGURATION_SERIALIZATION))) {
                    return false;
                }
            }
        } catch (RegistryException e) {
            log.error("Error while validating mediation configuration data in the registry", e);
        }

        return true;
    }

    public static void destroyConfiguration(SynapseConfiguration oldConfig,
                                      AxisConfiguration axisCfg,
                                      SynapseEnvironment synapseEnvironment) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("Stopping Proxy services...");
        }
        for (ProxyService proxyService : oldConfig.getProxyServices()) {
            if (proxyService != null) {
                try {
                    if (proxyService.getTargetInLineInSequence() != null) {
                        proxyService.getTargetInLineInSequence().destroy();
                    }

                    if (proxyService.getTargetInLineOutSequence() != null) {
                        proxyService.getTargetInLineOutSequence().destroy();
                    }

                    if (proxyService.getTargetInLineEndpoint() != null) {
                        proxyService.getTargetInLineEndpoint().destroy();
                    }
                    AxisService axisService = axisCfg.getServiceForActivation(
                            proxyService.getName());
                    if (axisService != null) {
                        axisService.getParent().addParameter(
                                CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, "true");
                    }
                    axisCfg.removeService(proxyService.getName());
                } catch (AxisFault e) {
                    throw new ConfigurationInitilizerException("Error removing Proxy service : " +
                            proxyService.getName(), e);
                }
            }
        }

        // destroy the managed mediators
        for (SequenceMediator seq : oldConfig.getDefinedSequences().values()) {
            if (seq != null) {
                seq.destroy();
            }
        }

        // destroy the startups
        for (Startup stp : oldConfig.getStartups()) {
            if (stp != null) {
                stp.destroy();
            }
        }

        TaskDescriptionRepository repository = synapseEnvironment.getTaskManager()
                .getTaskDescriptionRepository();
        if (repository != null) {
            repository.clear();
        }

        synapseEnvironment.getTaskManager().cleanup();

        Collection<SynapseEventSource> eventSources = oldConfig.getEventSources();
        for (SynapseEventSource ses : eventSources) {
            AxisService axisService = axisCfg.getServiceForActivation(ses.getName());
            if (axisService != null) {
                axisService.getParent().addParameter(
                        CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, "true");
            }
            axisCfg.removeService(ses.getName());
        }

        for (PriorityExecutor executor : oldConfig.getPriorityExecutors().values()) {
            executor.destroy();
        }
    }


    /**
     * Initialize the persistence for a Synapse Configuration
     *
     * @param synCfgConfiguration the synapse configuration to which to initialize the persistence
     * @param configurationLocation configuration place
     * @param axisConfiguration axisConfiguration to be used
     * @param name name of the configuration
     * @throws ConfigurationInitilizerException if an error occurs
     */
    public static void initPersistence(SynapseConfiguration synCfgConfiguration, String configurationLocation,
                                       AxisConfiguration axisConfiguration,
                                       String name) throws ConfigurationInitilizerException {
        // Initialize the mediation persistence manager if required
        ServerConfiguration serverConf = ServerConfiguration.getInstance();
        String persistence = serverConf.getFirstProperty(ServiceBusConstants.PERSISTENCE);

        // Check whether persistence is disabled
        if (!ServiceBusConstants.DISABLED.equals(persistence)) {
            // Check registry persistence is disabled or not
            String regPersistence = serverConf.getFirstProperty(
                    ServiceBusConstants.REGISTRY_PERSISTENCE);

            // Check the worker interval is set or not
            String interval = serverConf.getFirstProperty(ServiceBusConstants.WORKER_INTERVAL);
            long intervalInMillis = 5000L;
            if (interval != null && !"".equals(interval)) {
                try {
                    intervalInMillis = Long.parseLong(interval);
                } catch (NumberFormatException e) {
                    log.error("Invalid value " + interval + " specified for the mediation " +
                            "persistence worker interval, Using defaults", e);
                }
            }

            // Finally init the persistence manager
            MediationPersistenceManager pm = new MediationPersistenceManager(
                    configurationLocation,
                    synCfgConfiguration, intervalInMillis, name);

            try {
                axisConfiguration.addParameter(new Parameter(
                        ServiceBusConstants.PERSISTENCE_MANAGER, pm));
            } catch (AxisFault axisFault) {
                throw new ConfigurationInitilizerException("Cannot add the " +
                        ServiceBusConstants.PERSISTENCE_MANAGER +
                        " to the configuration", axisFault);
            }
        } else {
            log.info("Persistence for mediation configuration is disabled");
        }
    }

    public static void saveToFileSystem(SynapseConfiguration synapseConfig) {
        log.info("Saving the mediation configuration to the file system");
        String confPath = synapseConfig.getPathToConfigFile();
        MultiXMLConfigurationSerializer serializer = new MultiXMLConfigurationSerializer(confPath);
        serializer.serialize(synapseConfig);
    }
}
