/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.micro.integrator.initializer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerConfigurationInformationFactory;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.ServerManager;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.debug.SynapseDebugInterface;
import org.apache.synapse.debug.SynapseDebugManager;
import org.apache.synapse.deployers.InboundEndpointDeployer;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.task.TaskConstants;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.synapse.task.TaskScheduler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.micro.core.ServerShutdownHandler;
import org.wso2.carbon.inbound.endpoint.EndpointListenerLoader;
//import org.wso2.carbon.inbound.endpoint.persistence.service.InboundEndpointPersistenceService;
import org.wso2.carbon.micro.integrator.initializer.configurations.ConfigurationManager;
import org.wso2.carbon.micro.integrator.initializer.handler.ProxyLogHandler;
import org.wso2.carbon.micro.integrator.initializer.handler.SynapseExternalPropertyConfigurator;
import org.wso2.carbon.micro.integrator.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseConfigurationServiceImpl;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseEnvironmentServiceImpl;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.micro.integrator.initializer.services.SynapseRegistrationsServiceImpl;
import org.wso2.carbon.micro.integrator.initializer.utils.ConfigurationHolder;
import org.wso2.carbon.micro.integrator.initializer.utils.SynapseArtifactInitUtils;
//import org.wso2.carbon.mediation.ntask.internal.NtaskService;
//import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.task.services.TaskDescriptionRepositoryService;
import org.wso2.carbon.task.services.TaskSchedulerService;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.securevault.SecurityConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
@Component(
        name = "mi.core.initializer",
        immediate = true)
public class ServiceBusInitializer {

    private static final Log log = LogFactory.getLog(ServiceBusInitializer.class);

    // private static RegistryService registryService;
    private static ServerConfigurationInformation configurationInformation;

    private static String configPath;

    private Axis2ConfigurationContextService configCtxSvc;

    // private SynapseRegistryService synRegSvc;
    // private DataSourceInformationRepositoryService dataSourceInformationRepositoryService;
    private TaskDescriptionRepositoryService repositoryService;

    private TaskSchedulerService taskSchedulerService;

    private SecretCallbackHandlerService secretCallbackHandlerService;

    private ServerManager serverManager;

//    private TaskService taskService;

    @Activate
    protected void activate(ComponentContext ctxt) {

        log.info("Activating Micro Integrator...");
//        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//        privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
//        privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
//        if (taskService != null && !taskService.isServerInit()) {
//            log.info("Initialize Task Service");
//            taskService.serverInitialized();
//        }
        // FIXME: this is a hack to get rid of the https port retrieval from the axis2
        // configuration returning the non blocking https transport. Ideally we should be able
        // to fix this by making it possible to let the authentication of carbon be done through
        // the non blocking https transport
        setHttpsProtForConsole();
        // clean up temp folder created for connector class loader reference
        String javaTempDir = System.getProperty("java.io.tmpdir");
        String APP_UNZIP_DIR = javaTempDir.endsWith(File.separator) ? javaTempDir + "libs" : javaTempDir + File
                .separator + "libs";
        cleanupTempDirectory(APP_UNZIP_DIR);
        try {
            BundleContext bndCtx = ctxt.getBundleContext();
            ConfigurationHolder.getInstance().setBundleContext(bndCtx);
            // initialize the lock
            Lock lock = new ReentrantLock();
            configCtxSvc.getServerConfigContext().getAxisConfiguration().addParameter(ServiceBusConstants
                    .SYNAPSE_CONFIG_LOCK, lock);
            // first check which configuration should be active
            // UserRegistry registry = registryService.getConfigSystemRegistry();
            // init the multiple configuration tracker
            ConfigurationManager configurationManager = new ConfigurationManager(configCtxSvc.getServerConfigContext());
            configurationManager.init();
            // set the event broker as a property
            /*
            if (eventBroker != null) {
                configCtxSvc.getServerConfigContext().setProperty("mediation.event.broker", eventBroker);
            }
*/
            // Initialize Synapse
            ServerContextInformation contextInfo = initESB("default");
            ServiceRegistration synCfgRegistration = null;
            ServiceRegistration synEnvRegistration = null;
            if (contextInfo.getSynapseConfiguration() != null) {
                // Properties props = new Properties();
                SynapseConfigurationService synCfgSvc = new SynapseConfigurationServiceImpl(contextInfo
                        .getSynapseConfiguration(), MultitenantConstants.SUPER_TENANT_ID, configCtxSvc
                        .getServerConfigContext());
                synCfgRegistration = bndCtx.registerService(SynapseConfigurationService.class.getName(), synCfgSvc,
                        null);
                bndCtx.registerService(ServerShutdownHandler.class.getName(), new MPMShutdownHandler(synCfgSvc
                        .getSynapseConfiguration().getAxisConfiguration()), null);
                initPersistence(synCfgSvc, "default");
                if (log.isDebugEnabled()) {
                    log.debug("SynapseConfigurationService Registered");
                }
            } else {
                handleFatal("Couldn't register the SynapseConfigurationService, " + "SynapseConfiguration not found");
            }
            SynapseEnvironment synapseEnvironment = contextInfo.getSynapseEnvironment();
            if (synapseEnvironment != null) {
                // Properties props = new Properties();
                SynapseEnvironmentService synEnvSvc = new SynapseEnvironmentServiceImpl(synapseEnvironment,
                        MultitenantConstants.SUPER_TENANT_ID, configCtxSvc.getServerConfigContext());
                synEnvRegistration = bndCtx.registerService(SynapseEnvironmentService.class.getName(), synEnvSvc, null);
                synapseEnvironment.registerSynapseHandler(new SynapseExternalPropertyConfigurator());
                synapseEnvironment.registerSynapseHandler(new ProxyLogHandler());
                if (log.isDebugEnabled()) {
                    log.debug("SynapseEnvironmentService Registered");
                }
            } else {
                handleFatal("Couldn't register the SynapseEnvironmentService, " + "SynapseEnvironment not found");
            }
            // Properties props = new Properties();
            SynapseRegistrationsService synRegistrationsSvc = new SynapseRegistrationsServiceImpl(synCfgRegistration,
                    synEnvRegistration, MultitenantConstants.SUPER_TENANT_ID, configCtxSvc.getServerConfigContext());
            bndCtx.registerService(SynapseRegistrationsService.class.getName(), synRegistrationsSvc, null);
            configCtxSvc.getServerConfigContext().setProperty(ConfigurationManager.CONFIGURATION_MANAGER,
                    configurationManager);
            // Start Inbound Endpoint Listeners
            // tOdO need to fix inbound endpoints
            EndpointListenerLoader.loadListeners();
            registerInboundDeployer(configCtxSvc.getServerConfigContext().getAxisConfiguration(), contextInfo
                    .getSynapseEnvironment());
        } catch (Exception e) {
            handleFatal("Couldn't initialize the ESB...", e);
        } catch (Throwable t) {
            log.fatal("Failed to initialize ESB due to a fatal error", t);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        serverManager.stop();
        serverManager.shutdown();
    }

    private void initPersistence(SynapseConfigurationService synCfgSvc, String configName) throws AxisFault {
        // Initialize the mediation persistence manager if required
        CarbonServerConfigurationService serverConf = CarbonServerConfigurationService.getInstance();
        String persistence = serverConf.getFirstProperty(ServiceBusConstants.PERSISTENCE);
        // Check whether persistence is disabled
        if (!ServiceBusConstants.DISABLED.equals(persistence)) {
            // Check the worker interval is set or not
            String interval = serverConf.getFirstProperty(ServiceBusConstants.WORKER_INTERVAL);
            long intervalInMillis = 5000L;
            if (interval != null && !"".equals(interval)) {
                try {
                    intervalInMillis = Long.parseLong(interval);
                } catch (NumberFormatException e) {
                    log.error("Invalid value " + interval + " specified for the mediation " + "persistence worker " +
                            "interval, Using defaults", e);
                }
            }
            // Finally init the persistence manager
            MediationPersistenceManager pm = new MediationPersistenceManager(configurationInformation
                    .getSynapseXMLLocation(), synCfgSvc.getSynapseConfiguration(), intervalInMillis, configName);
            configCtxSvc.getServerConfigContext().getAxisConfiguration().addParameter(new Parameter
                    (ServiceBusConstants.PERSISTENCE_MANAGER, pm));
        } else {
            log.info("Persistence for mediation configuration is disabled");
        }
    }

    private void setHttpsProtForConsole() {

        CarbonServerConfigurationService config = CarbonServerConfigurationService.getInstance();
        //todo: handle properly when clustering is implemented
//        if (CarbonUtils.isRunningInStandaloneMode()) {
            // Try to get the port information from the Carbon TransportManager
            // -- Standalone Mode --
            final String TRANSPORT_MANAGER = "org.wso2.carbon.tomcat.ext.transport.ServletTransportManager";
            try {
                Class transportManagerClass = Class.forName(TRANSPORT_MANAGER);
                Object transportManager = transportManagerClass.newInstance();
                Method method = transportManagerClass.getMethod("getPort", String.class);
                int httpsPort = (Integer) method.invoke(transportManager, "https");
                int httpPort = (Integer) method.invoke(transportManager, "http");
                // required to properly log the management console URL
                System.setProperty("carbon.https.port", Integer.toString(httpsPort));
                System.setProperty("carbon.http.port", Integer.toString(httpPort));
                System.setProperty("httpPort", Integer.toString(httpPort));
                System.setProperty("httpsPort", Integer.toString(httpsPort));
                // this is required for the dashboard to work
                config.setConfigurationProperty("RegistryHttpPort", Integer.toString(httpPort));
            } catch (ClassNotFoundException e) {
                log.error("Failed to load the transport manager class using reflection", e);
            } catch (Exception e) {
                log.error("failed to set ports http/https", e);
            }
        /*} else {
            // -- Webapp Deployment Mode --
            if (log.isDebugEnabled()) {
                log.debug("TransportManager implementation not found. Switching to " + "webapp deployment mode. " +
                        "Reading HTTPS port from the carbon.xml.");
            }
            String serverURL = config.getFirstProperty("ServerURL");
            if (serverURL != null) {
                try {
                    URL url = new URL(serverURL);
                    if ("https".equals(url.getProtocol())) {
                        System.setProperty("carbon.https.port", String.valueOf(url.getPort()));
                    } else {
                        log.warn("Invalid protocol " + url.getProtocol() + " in Carbon server URL");
                    }
                } catch (MalformedURLException ex) {
                    log.error("Error while parsing the server URL " + serverURL, ex);
                }
            } else {
                log.warn("Server URL is not specified in the carbon.xml. Unable to " + "set the HTTPS port as a " +
                        "system property");
            }
        }*/
    }

    private ServerContextInformation initESB(String name) throws AxisFault {

        if (configCtxSvc != null) {
            ConfigurationContext configContext = configCtxSvc.getServerConfigContext();
            log.debug("Initializing Apache Synapse...");
            configurationInformation = ServerConfigurationInformationFactory.createServerConfigurationInformation
                    (configContext.getAxisConfiguration());
            // ability to specify the SynapseServerName as a system property
            if (System.getProperty("SynapseServerName") != null) {
                configurationInformation.setServerName(System.getProperty("SynapseServerName"));
            }
            // for now we override the default configuration location with the value in registry
            String synapseConfigsLocation = configurationInformation.getSynapseXMLLocation();
            if (synapseConfigsLocation != null) {
                configurationInformation.setSynapseXMLLocation(synapseConfigsLocation + File.separator + name);
            } else {
                configurationInformation.setSynapseXMLLocation(ServiceBusConstants.DEFAULT_SYNAPSE_CONFIGS_LOCATION +
                        name);
            }
            configurationInformation.setCreateNewInstance(false);
            configurationInformation.setServerControllerProvider(CarbonSynapseController.class.getName());
            if (isRunningSamplesMode()) {
                if (System.getProperty(ServiceBusConstants.ESB_SAMPLE_SYSTEM_PROPERTY) != null) {
                    configurationInformation.setSynapseXMLLocation("repository" + File.separator + "samples" + File
                            .separator + "synapse_sample_" + System.getProperty(ServiceBusConstants
                            .ESB_SAMPLE_SYSTEM_PROPERTY) + ".xml");
                } else {
                    configurationInformation.setSynapseXMLLocation("samples" + File.separator + "service-bus" + File
                            .separator + "synapse_sample_" + System.getProperty(ServiceBusConstants
                            .EI_SAMPLE_SYSTEM_PROPERTY) + ".xml");
                }
            }
            serverManager = new ServerManager();
            ServerContextInformation contextInfo = new ServerContextInformation(configContext,
                    configurationInformation);
            // }
            if (taskSchedulerService != null) {
                TaskScheduler scheduler = taskSchedulerService.getTaskScheduler();
                contextInfo.addProperty(TaskConstants.TASK_SCHEDULER, scheduler);
            }
            if (repositoryService != null) {
                TaskDescriptionRepository repository = repositoryService.getTaskDescriptionRepository();
                contextInfo.addProperty(TaskConstants.TASK_DESCRIPTION_REPOSITORY, repository);
            }
            if (secretCallbackHandlerService != null) {
                contextInfo.addProperty(SecurityConstants.PROP_SECRET_CALLBACK_HANDLER, secretCallbackHandlerService
                        .getSecretCallbackHandler());
            }
            AxisConfiguration axisConf = configContext.getAxisConfiguration();
            axisConf.addParameter(new Parameter(ServiceBusConstants.SYNAPSE_CURRENT_CONFIGURATION, name));
            if (isRunningDebugMode()) {
                log.debug("Micro Integrator started in Debug mode for super tenant");
                createSynapseDebugEnvironment(contextInfo);
            }
            boolean initConnectors = !Boolean.valueOf(System.getProperty(ServiceBusConstants
                    .DISABLE_CONNECTOR_INIT_SYSTEM_PROPERTY));
            if (initConnectors) {
                // Enable connectors
                SynapseArtifactInitUtils.initializeConnectors(axisConf);
            }
            serverManager.init(configurationInformation, contextInfo);
            serverManager.start();
            AxisServiceGroup serviceGroup = axisConf.getServiceGroup(SynapseConstants.SYNAPSE_SERVICE_NAME);
            serviceGroup.addParameter("hiddenService", "true");
            return contextInfo;
        } else {
            handleFatal("Couldn't initialize Synapse, " + "ConfigurationContext service or SynapseRegistryService is " +
                    "not available");
        }
        // never executes, but keeps the compiler happy
        return null;
    }

    public static boolean isRunningSamplesMode() {

        return System.getProperty(ServiceBusConstants.ESB_SAMPLE_SYSTEM_PROPERTY) != null || System.getProperty
                (ServiceBusConstants.EI_SAMPLE_SYSTEM_PROPERTY) != null;
    }

    public static boolean isRunningDebugMode() {

        String debugMode = System.getProperty(ServiceBusConstants.ESB_DEBUG_SYSTEM_PROPERTY);
        return "true".equals(debugMode) || "super".equals(debugMode);
    }

    /**
     * creates Synapse debug environment
     * creates TCP channels using command and event ports which initializes the interface to outer debugger
     * set the relevant information in the server configuration so that it can be used when Synapse environment
     * initializes
     *
     * @param contextInfo Server Context Information
     */
    public void createSynapseDebugEnvironment(ServerContextInformation contextInfo) {

        try {
            String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
            File synapseProperties = Paths.get(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), "synapse.properties").toFile();
            Properties properties = new Properties();
            InputStream inputStream = new FileInputStream(synapseProperties);
            properties.load(inputStream);
            inputStream.close();
            int event_port = Integer.parseInt(properties.getProperty(ServiceBusConstants.ESB_DEBUG_EVENT_PORT));
            int command_port = Integer.parseInt(properties.getProperty(ServiceBusConstants.ESB_DEBUG_COMMAND_PORT));
            SynapseDebugInterface debugInterface = SynapseDebugInterface.getInstance();
            debugInterface.init(command_port, event_port);
            contextInfo.setServerDebugModeEnabled(true);
            contextInfo.setSynapseDebugInterface(debugInterface);
            SynapseDebugManager debugManager = SynapseDebugManager.getInstance();
            contextInfo.setSynapseDebugManager(debugManager);
            log.debug("Synapse debug Environment created successfully");
        } catch (IOException ex) {
            log.error("Error while creating Synapse debug environment ", ex);
        } catch (InterruptedException ex) {
            log.error("Error while creating Synapse debug environment ", ex);
        }
    }

    @Reference(
            name = "config.context.service",
            service = Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(Axis2ConfigurationContextService configurationContextService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the ESB initialization process");
            log.debug("configurationContextService : " + configurationContextService);
        }
        this.configCtxSvc = configurationContextService;
    }

    protected void unsetConfigurationContextService(Axis2ConfigurationContextService configurationContextService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the ESB environment");
        }
        this.configCtxSvc = null;
    }

    @Reference(
            name = "task.description.repository.service",
            service = org.wso2.carbon.task.services.TaskDescriptionRepositoryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskDescriptionRepositoryService")
    protected void setTaskDescriptionRepositoryService(TaskDescriptionRepositoryService repositoryService) {

        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService bound to the ESB initialization process");
        }
        this.repositoryService = repositoryService;
    }

    protected void unsetTaskDescriptionRepositoryService(TaskDescriptionRepositoryService repositoryService) {

        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService unbound from the ESB environment");
        }
        this.repositoryService = null;
    }

    @Reference(
            name = "task.scheduler.service",
            service = org.wso2.carbon.task.services.TaskSchedulerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskSchedulerService")
    protected void setTaskSchedulerService(TaskSchedulerService schedulerService) {

        if (log.isDebugEnabled()) {
            log.debug("TaskSchedulerService bound to the ESB initialization process");
        }
        this.taskSchedulerService = schedulerService;
    }

    protected void unsetTaskSchedulerService(TaskSchedulerService schedulerService) {

        if (log.isDebugEnabled()) {
            log.debug("TaskSchedulerService unbound from the ESB environment");
        }
        this.taskSchedulerService = null;
    }

    @Reference(
            name = "secret.callback.handler.service",
            service = org.wso2.carbon.securevault.SecretCallbackHandlerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretCallbackHandlerService")
    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {

        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService bound to the ESB initialization process");
        }
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {

        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService unbound from the ESB environment");
        }
        this.secretCallbackHandlerService = null;
    }

//    @Reference(
//            name = "esbntask.taskservice",
//            service = org.wso2.carbon.mediation.ntask.internal.NtaskService.class,
//            cardinality = ReferenceCardinality.OPTIONAL,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unsetTaskService")
    /*protected void setTaskService(NtaskService taskService) {

    }

    protected void unsetTaskService(NtaskService ntaskService) {

    }*/

    public static ServerConfigurationInformation getConfigurationInformation() {

        return configurationInformation;
    }

    protected static ServerConfigurationInformation getServerConfigurationInformation() {

        return configurationInformation;
    }

    private void handleFatal(String message) {

        log.fatal(message);
        // Do not do this -- throw new RuntimeException(message);
        // it causes the OSGi environment to reinitialize synapse which will result in a looping
    }

    private void handleFatal(String message, Exception e) {

        log.fatal(message, e);
        // Do not do this -- throw new RuntimeException(message, e);
        // it causes the OSGi environment to reinitialize synapse which will result in a looping
    }

    public class MPMShutdownHandler implements ServerShutdownHandler {

        private AxisConfiguration configuration;

        public MPMShutdownHandler(AxisConfiguration configuration) {

            this.configuration = configuration;
        }

        public void invoke() {

            Parameter p = configuration.getParameter(ServiceBusConstants.PERSISTENCE_MANAGER);
            if (p != null && p.getValue() instanceof MediationPersistenceManager) {
                ((MediationPersistenceManager) p.getValue()).destroy();
            }
        }
    }

    /**
     * Clean up temp files
     *
     * @param appUnzipDir
     */
    private static void cleanupTempDirectory(String appUnzipDir) {

        File tempDirector = new File(appUnzipDir);
        if (tempDirector.isDirectory()) {
            File[] entries = tempDirector.listFiles();
            int size = entries.length;
            for (int i = 0; i < size; i++) {
                try {
                    FileUtils.deleteDirectory(entries[i]);
                } catch (IOException e) {
                    log.warn("Could not build lib artifact for path : " + entries[i].getAbsolutePath());
                }
            }
        }
    }

    /**
     * Register for inbound hot depoyment
     */
    private void registerInboundDeployer(AxisConfiguration axisConfig, SynapseEnvironment synEnv) {

        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        SynapseArtifactDeploymentStore deploymentStore = synEnv.getSynapseConfiguration().getArtifactDeploymentStore();
        String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(synEnv.getServerContextInformation());
        String inboundDirPath = synapseConfigPath + File.separator + MultiXMLConfigurationBuilder.INBOUND_ENDPOINT_DIR;
        for (InboundEndpoint inboundEndpoint : synEnv.getSynapseConfiguration().getInboundEndpoints()) {
            if (inboundEndpoint.getFileName() != null) {
                deploymentStore.addRestoredArtifact(inboundDirPath + File.separator + inboundEndpoint.getFileName());
            }
        }
        deploymentEngine.addDeployer(new InboundEndpointDeployer(), inboundDirPath, ServiceBusConstants
                .ARTIFACT_EXTENSION);
    }

//    @Reference(
//            name = "inbound.endpoint.persistence.service",
//            service = org.wso2.carbon.inbound.endpoint.persistence.service.InboundEndpointPersistenceService.class,
//            cardinality = ReferenceCardinality.OPTIONAL,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unsetInboundPersistenceService")
    /*protected void setInboundPersistenceService(InboundEndpointPersistenceService inboundEndpoint) {
        // This service is just here to make sure that ServiceBus is not getting initialized
        // before the Inbound Endpoint Persistence component is up and running
    }

    protected void unsetInboundPersistenceService(InboundEndpointPersistenceService inboundEndpoint) {

    }*/

    /*protected void setTaskService(TaskService taskService) {

        log.info("Set Task Service");
        this.taskService = taskService;
    }

    protected void unsetTaskService(TaskService taskService) {

        this.taskService = null;
    }*/
}
