/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.micro.integrator.initializer.configurations;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.SynapseConfigurationBuilder;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.registry.Registry;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.synapse.task.TaskScheduler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.micro.integrator.initializer.ServiceBusConstants;
import org.wso2.carbon.micro.integrator.initializer.ServiceBusUtils;
import org.wso2.carbon.micro.integrator.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseConfigurationServiceImpl;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseEnvironmentServiceImpl;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseRegistrationsServiceImpl;
import org.wso2.carbon.micro.integrator.initializer.utils.ConfigurationHolder;
import org.wso2.carbon.mediation.registry.WSO2Registry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Responsible for managing the differrence configurations inside a single ConfigurationContext.
 */
public class ConfigurationManager {
    private static Log log = LogFactory.getLog(ConfigurationManager.class);

    public static final String CONFIGURATION_MANAGER = "CONFIGURATION_MANAGER";

    /** We are defaulting to this, we need to take this from a configuration */
    private String synpaseConfigurationsRoot = ServiceBusConstants.DEFAULT_SYNAPSE_CONFIGS_LOCATION;

    /** The configuration context of the server */
    private ConfigurationContext configurationContext;

    /** Tenant for this configuration belongs */
    private int tenantId = MultitenantConstants.SUPER_TENANT_ID;

    /**
     *
     * @param configurationContext servers configuration context
     */
    public ConfigurationManager(ConfigurationContext configurationContext) {
/*        if (registry == null) {
            throw new IllegalArgumentException("Registry is required");
        }*/

        if (configurationContext == null) {
            throw new IllegalArgumentException("ConfigurationContext is required");
        }

        this.configurationContext = configurationContext;
    }

    /**
     * Initilize the configuration manager
     *
     * @throws ConfigurationInitilizerException if an error occurs
     */
    public void init() throws ConfigurationInitilizerException {
        Parameter parameter = configurationContext.getAxisConfiguration().getParameter(
                SynapseConstants.Axis2Param.SYNAPSE_CONFIG_LOCATION);

        if (parameter != null && parameter.getValue() != null) {
            synpaseConfigurationsRoot = parameter.getValue().toString();
        }
    }

    /**
     * Activate the given configuration. This will intern deactivate the current
     * configuration and then activate the new configuration.
     *
     * @param name name of the configuration to be activated
     * @return true if activation is successful
     * @throws ConfigurationInitilizerException if an error occurs while activating
     */
    public boolean activate(String name) throws ConfigurationInitilizerException {
        try {

            SynapseConfiguration newSynapseConfiguration = null;
            SynapseConfiguration oldSynapseConfiguration = getSynapseConfiguration();
            SynapseEnvironment oldSynapseEnvironment = getSynapseEnvironment();

            TaskDescriptionRepository repository = getSynapseEnvironment().getTaskManager().
                    getTaskDescriptionRepository();
            TaskScheduler taskScheduler = getSynapseEnvironment().getTaskManager().
                    getTaskScheduler();

            int loadLocation = 0;

            // if this configuration is not created before we are going to
            // create a default configuration
            // if this configuration is not created before we are going to
            // create a default configuration
            newSynapseConfiguration = createDefaultConfiguration();
            loadLocation = 1;
            log.info("A default Synapse Configuration is created " +
                    "for the configuration: " + name);


            Properties properties = SynapsePropertiesLoader.loadSynapseProperties();
            if (getServerConfigurationInformation().getResolveRoot() != null) {
                properties.put(SynapseConstants.RESOLVE_ROOT,
                        getServerConfigurationInformation().getResolveRoot());
            }

            if (getServerConfigurationInformation().getSynapseHome() != null) {
                properties.put(SynapseConstants.SYNAPSE_HOME,
                        getServerConfigurationInformation().getSynapseHome());
            }

            if (loadLocation != 1) {
                // we have to try to load the configuration from the file system
                if (newSynapseConfiguration == null) {
                    newSynapseConfiguration = SynapseConfigurationBuilder.
                            getConfiguration(synpaseConfigurationsRoot + File.separator + name,
                                    properties);

                    if (newSynapseConfiguration != null) {
                        log.info("Successfully loaded the Synapse Configuration: " +
                                name + " from the file system");
                        loadLocation = 3;
                    } else {
                        log.warn("Failed to load the Synapse Configuration: " + name);
                        return false;
                    }
                }
            }

            // destroy the old configuration
            synchronized (oldSynapseConfiguration) {
                // first make sure we have completed all the persistence requests
                getMediationPersistenceManager().destroy();

                try {
                    ConfigurationUtils.destroyConfiguration(oldSynapseConfiguration,
                            configurationContext.getAxisConfiguration(), oldSynapseEnvironment);
                } catch (Exception e) {
                    // we are going to ignore and continue
                    log.warn("Error while destroying the current configuration.. " +
                            "Continuing to load the new configuration");
                }
            }

            // we are going to switch to the new configuration location
            newSynapseConfiguration.setPathToConfigFile(
                    synpaseConfigurationsRoot + File.separator + name);
            getServerConfigurationInformation().
                    setSynapseXMLLocation(synpaseConfigurationsRoot + File.separator + name);

            if (loadLocation == 1) {
                ConfigurationUtils.saveToFileSystem(newSynapseConfiguration);
            } else if (loadLocation == 2) {
                if ("true".equals(ConfigurationUtils.getParameter(ServiceBusConstants.SAVE_TO_FILE))) {
                    // If the configuration was loaded from the registry and the 'saveConfigToFile'
                    // system property is set we should serialize the current configuration to the
                    // file system
                    ConfigurationUtils.saveToFileSystem(newSynapseConfiguration);
                }
            } else {
                // If the configuration was loaded from the file system we should
                // save it to the registry
            }

            configurationContext.getAxisConfiguration().addParameter(
                    new Parameter(ServiceBusConstants.SYNAPSE_CURRENT_CONFIGURATION, name));

            // initilze the configuration
            initializeConfiguration(synpaseConfigurationsRoot + File.separator + name,
                    oldSynapseConfiguration, newSynapseConfiguration, repository, taskScheduler);

            // initialize the persistence
            ConfigurationUtils.initPersistence(newSynapseConfiguration,
                    synpaseConfigurationsRoot + File.separator + name,
                    configurationContext.getAxisConfiguration(), name);

            // un-register the old configuration
            unregisterServices();

            registerServices();

            return true;
        } catch (AxisFault axisFault) {
            handleException("Error while setting up the new configuration");
        }

        return false;
    }

    private void registerServices() {
        SynapseEnvironment synEnv = getSynapseEnvironment();
        SynapseConfiguration synConfig = getSynapseConfiguration();

        SynapseConfigurationService synCfgSvc
                = new SynapseConfigurationServiceImpl(synConfig,
                tenantId, configurationContext);

        //Properties props = new Properties();
        ServiceRegistration confRegistration =
                ConfigurationHolder.getInstance().getBundleContext().registerService(
                        SynapseConfigurationService.class.getName(), synCfgSvc, null);

        //props = new Properties();
        SynapseEnvironmentService synEnvSvc
                = new SynapseEnvironmentServiceImpl(synEnv,
                tenantId, configurationContext);
        ServiceRegistration envRegistration =
                ConfigurationHolder.getInstance().getBundleContext().registerService(
                        SynapseEnvironmentService.class.getName(), synEnvSvc, null);

        //props = new Properties();
        SynapseRegistrationsService synRegistrationsSvc
                = new SynapseRegistrationsServiceImpl(
                confRegistration, envRegistration, tenantId, configurationContext);

        ServiceRegistration synapseRegistration =
                ConfigurationHolder.getInstance().getBundleContext().registerService(
                        SynapseRegistrationsService.class.getName(),
                        synRegistrationsSvc, null);

    }

    private void unregisterServices() {
        BundleContext bundleContext = ConfigurationHolder.getInstance().getBundleContext();

        ServiceRegistration serviceRegistration = ConfigurationHolder.getInstance().
                getSynapseRegistration(tenantId);

        if (serviceRegistration != null) {
            SynapseRegistrationsService synapseRegistrationsService =
                    (SynapseRegistrationsService) bundleContext.getService(serviceRegistration.getReference());

            ServiceRegistration synConfigServiceRegistration =
                    synapseRegistrationsService.getSynapseConfigurationServiceRegistration();

            if (synConfigServiceRegistration != null) {
                bundleContext.ungetService(synConfigServiceRegistration.getReference());
            }

            ServiceRegistration synEnvServiceRegistration =
                    synapseRegistrationsService.getSynapseEnvironmentServiceRegistration();

            if (synEnvServiceRegistration != null) {
                bundleContext.ungetService(synEnvServiceRegistration.getReference());
            }

            bundleContext.ungetService(serviceRegistration.getReference());
        }
    }

    /**
     * Create the default configuration
     *
     * @return the default configuration
     */
    private SynapseConfiguration createDefaultConfiguration() {
        SynapseConfiguration newSynapseConfiguration;
        newSynapseConfiguration = SynapseConfigurationBuilder.getDefaultConfiguration();
        SequenceMediator mainSequence = (SequenceMediator)
                newSynapseConfiguration.getMainSequence();
        mainSequence.setFileName(mainSequence.getName());

        SequenceMediator faultSequence = (SequenceMediator)
                newSynapseConfiguration.getFaultSequence();
        faultSequence.setFileName(faultSequence.getName());

        // set the governance registry
        Registry registry = new WSO2Registry();
        newSynapseConfiguration.setRegistry(registry);

        newSynapseConfiguration.setProperty(
                MultiXMLConfigurationBuilder.SEPARATE_REGISTRY_DEFINITION, "true");
        return newSynapseConfiguration;
    }

    /**
     * Initialize a newly created configuration. This will initialize the Synapse Env as well.
     *
     * @param configurationLocation file path which this configuration is based on
     * @param oldSynapseConfiguration previous synapse configuration
     * @param newSynapseConfiguration newly created synapse configuration     
     * @param repository task repository
     * @param taskScheduler @throws ConfigurationInitilizerException if an error occurs
     * @throws org.apache.axis2.AxisFault if an error occurs
     * @throws ConfigurationInitilizerException if an error occurs
     */
    private void initializeConfiguration(String configurationLocation,
                                         SynapseConfiguration oldSynapseConfiguration,
                                         SynapseConfiguration newSynapseConfiguration,
                                         TaskDescriptionRepository repository,
                                         TaskScheduler taskScheduler)
            throws ConfigurationInitilizerException, AxisFault {
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        // Set the Axis2 ConfigurationContext to the SynapseConfiguration
        newSynapseConfiguration.setAxisConfiguration(axisConfiguration);

        Entry hostEntry = oldSynapseConfiguration.getEntryDefinition(SynapseConstants.SERVER_HOST);
        Entry ipEntry = oldSynapseConfiguration.getEntryDefinition(SynapseConstants.SERVER_IP);

        // setup the properties
        Properties properties = SynapsePropertiesLoader.loadSynapseProperties();
        if (properties != null) {
            Enumeration keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                newSynapseConfiguration.setProperty(key, properties.getProperty(key));
            }
        }

        // Add the old parameters to the new configuration
        newSynapseConfiguration.setPathToConfigFile(configurationLocation);
        newSynapseConfiguration.addEntry(SynapseConstants.SERVER_HOST, hostEntry);
        newSynapseConfiguration.addEntry(SynapseConstants.SERVER_IP, ipEntry);

        // Check for the main sequence and add a default main sequence if not present
        if (newSynapseConfiguration.getMainSequence() == null) {
            SynapseConfigUtils.setDefaultMainSequence(newSynapseConfiguration);
        }

        // Check for the fault sequence and add a deafult fault sequence if not present
        if (newSynapseConfiguration.getFaultSequence() == null) {
            SynapseConfigUtils.setDefaultFaultSequence(newSynapseConfiguration);
        }

        ServerContextInformation contextInformation = getServerContextInformation();

        // set the synapse configuration to the axis2 configuration
        newSynapseConfiguration.setAxisConfiguration(axisConfiguration);
        Parameter synapseCtxParam = new Parameter(SynapseConstants.SYNAPSE_CONFIG, null);
        synapseCtxParam.setValue(newSynapseConfiguration);
        MessageContextCreatorForAxis2.setSynConfig(newSynapseConfiguration);

        //set up synapse env
        Parameter synapseEnvParam = new Parameter(SynapseConstants.SYNAPSE_ENV, null);
        Axis2SynapseEnvironment synEnv = new Axis2SynapseEnvironment(configurationContext,
                newSynapseConfiguration, contextInformation);
        synapseEnvParam.setValue(synEnv);
        MessageContextCreatorForAxis2.setSynEnv(synEnv);

        if (contextInformation != null) {
            // set the new information to the server context
            contextInformation.setSynapseEnvironment(synEnv);
            contextInformation.setSynapseConfiguration(newSynapseConfiguration);
        } else {
            throw new IllegalStateException("ServerContextInformation not found");
        }

        try {
            axisConfiguration.addParameter(synapseCtxParam);
            axisConfiguration.addParameter(synapseEnvParam);
        } catch (AxisFault e) {
            String msg =
                    "Could not set parameters '" + SynapseConstants.SYNAPSE_CONFIG +
                            "' and/or '" + SynapseConstants.SYNAPSE_ENV +
                            "'to the Axis2 configuration : " + e.getMessage();
            throw new ConfigurationInitilizerException(msg, e);
        }

        // redeploy proxy services
        if (log.isTraceEnabled()) {
            log.trace("Re-deploying Proxy services...");
        }

        for (ProxyService proxyService : newSynapseConfiguration.getProxyServices()) {
            if (proxyService != null) {
                proxyService.buildAxisService(newSynapseConfiguration, axisConfiguration);
                if (log.isDebugEnabled()) {
                    log.debug("Deployed Proxy service : " + proxyService.getName());
                }
                if (!proxyService.isStartOnLoad()) {
                    proxyService.stop(newSynapseConfiguration);
                }
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Re-deploying Event Sources...");
        }

        for (SynapseEventSource eventSource : newSynapseConfiguration.getEventSources()) {
            if (eventSource != null) {
                eventSource.buildService(axisConfiguration);
                if (log.isDebugEnabled()) {
                    log.debug("Deployed Event Source : " + eventSource.getName());
                }
            }
        }

        synEnv.getTaskManager().init(repository, taskScheduler,
                newSynapseConfiguration.getTaskManager());
        // init the synapse configuration
        newSynapseConfiguration.init(synEnv);
        synEnv.setInitialized(true);
    }

    /**
     * Return true if we have to load the configuration from the registry
     * @return true if we have to load the configuration from the registry
     */
    private boolean isLoadFromRegistry() {
        return "true".equals(
                ConfigurationUtils.getParameter(ServiceBusConstants.LOAD_FROM_REGISTRY));
    }

    private SynapseEnvironment getSynapseEnvironment() {
        return (SynapseEnvironment) configurationContext.getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_ENV).getValue();
    }

    private SynapseConfiguration getSynapseConfiguration() {
        return (SynapseConfiguration) configurationContext.getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_CONFIG).getValue();
    }

    private MediationPersistenceManager getMediationPersistenceManager() {
        return ServiceBusUtils.getMediationPersistenceManager(
                configurationContext.getAxisConfiguration());
    }

    private ServerConfigurationInformation getServerConfigurationInformation() {
        Parameter p = configurationContext.getAxisConfiguration().
                getParameter(SynapseConstants.SYNAPSE_SERVER_CONFIG_INFO);
        if (p != null) {
            return (ServerConfigurationInformation) p.getValue();
        }
        return null;
    }

    private ServerContextInformation getServerContextInformation() {
        Parameter p = configurationContext.getAxisConfiguration().
                getParameter(SynapseConstants.SYNAPSE_SERVER_CTX_INFO);
        if (p != null) {
            return (ServerContextInformation) p.getValue();
        }
        return null;
    }

    private void handleException(String msg) throws ConfigurationInitilizerException {
        log.warn(msg);
        throw new ConfigurationInitilizerException(msg);
    }

    private void handleException(String msg, Exception e) throws ConfigurationInitilizerException {
        log.warn(msg, e);
        throw new ConfigurationInitilizerException(msg, e);
    }
}
