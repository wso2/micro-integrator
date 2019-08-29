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

package org.wso2.carbon.micro.integrator.initializer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Axis2SynapseController;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.snmp.SNMPConstants;
import org.apache.synapse.commons.snmp.SynapseSNMPAgent;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.config.xml.MultiXMLConfigurationSerializer;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.deployers.ClassMediatorDeployer;
import org.apache.synapse.deployers.ExtensionDeployer;
import org.wso2.carbon.CarbonConstants;
//import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.MultitenantDispatcher;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CarbonSynapseController extends Axis2SynapseController {

    private static final Log log = LogFactory.getLog(CarbonSynapseController.class);

    private static final String PARAMETER_VALUE_TRUE = Boolean.toString(true);

    private String currentConfigurationName;

    private String synapseXMLLocation;

    private SynapseSNMPAgent snmpAgent;

    @Override
    public void init(ServerConfigurationInformation serverConfigurationInformation,
                     ServerContextInformation serverContextInformation) {
        
        Object context = serverContextInformation.getServerContext();
        if (context instanceof ConfigurationContext) {
            AxisConfiguration axisCfg = ((ConfigurationContext) context).getAxisConfiguration();
            synchronized (axisCfg) {
                DeploymentEngine deploymentEngine = (DeploymentEngine) axisCfg.getConfigurator();
                // File carbonHome = new File(System.getProperty(ServerConstants.CARBON_HOME));
                // subjected to change
                String carbonRepoPath = axisCfg.getRepository().getPath();
                String mediatorsPath = carbonRepoPath + File.separator + "mediators";
                String extensionsPath = carbonRepoPath + File.separator + "extensions";
                ExtensionDeployer deployer = new ExtensionDeployer();
                deploymentEngine.addDeployer(deployer, mediatorsPath, "xar");
                deploymentEngine.addDeployer(deployer, extensionsPath, "xar");
                deploymentEngine.addDeployer(deployer, mediatorsPath, "jar");
                deploymentEngine.addDeployer(deployer, extensionsPath, "jar");

                // Register deployer for class mediators
                String classMediatorsPath = carbonRepoPath + File.separator + "class-mediators";
                deploymentEngine.addDeployer(new ClassMediatorDeployer(),
                                             classMediatorsPath,
                                             ServiceBusConstants.CLASS_MEDIATOR_EXTENSION);
            }
            this.currentConfigurationName = ((ConfigurationContext) context).
                    getAxisConfiguration().getParameterValue(
                    ServiceBusConstants.SYNAPSE_CURRENT_CONFIGURATION).toString();

            this.synapseXMLLocation = serverConfigurationInformation.getSynapseXMLLocation();
        }

        super.init(serverConfigurationInformation, serverContextInformation);
    }

    /**
     * Adds the synapse handlers to the inflow Dispatch phase if not already exists and starts the listener manager
     * if the axis2 instance is created by the Synapse
     */
    public void start() {
        /*Starting SNMP Agent if activated in */
        Properties properties = SynapsePropertiesLoader.loadSynapseProperties();
        String enabled = properties.getProperty(SNMPConstants.SNMP_ENABLED);
        try {
            if (enabled != null && JavaUtils.isTrueExplicitly(enabled)) {
                snmpAgent = new SynapseSNMPAgent(properties);
                snmpAgent.start();
            }
        } catch (IOException e) {
            log.error("Error while initializing SNMP", e);
        } catch (Exception e){
            log.info("SNMP not initialized, SNMP not supported in tenants. Message : " + e.getMessage());
        }

        // add the Synapse handlers
        if (getContext() != null) {
            List<Phase> inflowPhases = ((ConfigurationContext)
                    getContext()).getAxisConfiguration().getInFlowPhases();
            for (Phase inPhase : inflowPhases) {
                // we are interested about the Dispatch phase in the inflow
                if (PhaseMetadata.PHASE_DISPATCH.equals(inPhase.getPhaseName())) {

                    for (Handler handler : inPhase.getHandlers()) {
                        if (MultitenantDispatcher.NAME.equals(handler.getName())) {
                            return;
                        }
                    }
                    super.start();
                }
            }
        } else {
            handleFatal("Couldn't start Synapse, ConfigurationContext not found");
        }
    }

    public SynapseConfiguration createSynapseConfiguration() {

        SynapseConfiguration synapseConfig = null;
//        UserRegistry registry = getRegistry();

        Properties properties = SynapsePropertiesLoader.loadSynapseProperties();
        if (serverConfigurationInformation.getResolveRoot() != null) {
            properties.put(SynapseConstants.RESOLVE_ROOT,
                    serverConfigurationInformation.getResolveRoot());
        }

        if (serverConfigurationInformation.getSynapseHome() != null) {
            properties.put(SynapseConstants.SYNAPSE_HOME,
                    serverConfigurationInformation.getSynapseHome());
        }

        //TODO load synapse config from the file

        if (synapseConfig == null) {
            // If the configuration has not been loaded from the registry load it
            // from the file system
            log.debug("Loading the mediation configuration from the file system");
            synapseConfig = super.createSynapseConfiguration();


        } else if ("true".equals(getParameter(ServiceBusConstants.SAVE_TO_FILE))) {
            // If the configuration was loaded from the registry and the 'saveConfigToFile'
            // system property is set we should serialize the current configuration to the
            // file system
            saveToFileSystem(synapseConfig);
        }
        return synapseConfig;
    }

    public void destroySynapseEnvironment() {

        // adding the Keep history parameter to every ProxyService to prevent the service
        // history being deleted on undeploying services at destroy time
        for(ProxyService proxy : synapseConfiguration.getProxyServices()) {
            try {
                proxy.getAxisService().addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, PARAMETER_VALUE_TRUE);
            } catch (AxisFault axisFault) {
                log.error("Error while accessing the Proxy Service " + proxy.getName()
                        + ". Service configuration history might get lost", axisFault);
            }

        }

        // finally call the super logic to do the destroy tasks after persisting
        super.destroySynapseEnvironment();
    }

    private void saveToFileSystem(SynapseConfiguration synapseConfig) {
        log.info("Saving the mediation configuration to the file system");
        String confPath = synapseConfig.getPathToConfigFile();
        MultiXMLConfigurationSerializer serializer = new MultiXMLConfigurationSerializer(confPath);
        serializer.serialize(synapseConfig);
    }

  /*  private void saveToRegistry(UserRegistry registry, SynapseConfiguration synapseConfig) {
        if (registry == null) {
            log.warn("Unable to persist the mediation configuration to the registry. System " +
                    "registry is not available.");
            return;
        }

        RegistryBasedSynapseConfigSerializer serializer =
                new RegistryBasedSynapseConfigSerializer(registry, currentConfigurationName);
        serializer.serializeConfiguration(synapseConfig);

        try {
            Resource resource;
            if (registry.resourceExists(ServiceBusConstants.META_INF_REGISTRY_PATH)) {
                resource = registry.get(
                        ServiceBusConstants.META_INF_REGISTRY_PATH);
            } else {
                resource = registry.newResource();
            }

            resource.setProperty(ServiceBusConstants.CONFIGURATION_SERIALIZATION,
                    ServiceBusConstants.SERIALIZED_TO_REGISTRY);
            registry.put(ServiceBusConstants.META_INF_REGISTRY_PATH, resource);

        } catch (RegistryException e) {
            log.warn("Error ocured while saving the mediation configuration in the registry. " +
                    "Mediation configuration might get loaded from the file system next time.", e);
        }
    }*/

    private SynapseConfiguration loadFromRegistry(UserRegistry registry, Properties properties) {
        if (registry == null) {
            log.warn("Unable to load the mediation configuration from the registry. System " +
                    "registry is not available.");
            return null;
        }

        if (isInitialStartup(registry)) {
            log.warn("Unable to load the mediation configuration from the registry. Mediation " +
                    "configuration data is not available in the registry.");
            return null;
        }
        String registryFailSafeProperty = getParameter(ServiceBusConstants.REGISTRY_FAIL_SAFE);
        boolean registryFailSafe = true;
        if ("false".equals(registryFailSafeProperty)) {
            registryFailSafe = false;
        }

        log.debug("Loading the mediation configuration from the registry");
        //TODO
/*        RegistryBasedSynapseConfigBuilder builder =
                new RegistryBasedSynapseConfigBuilder(registry, currentConfigurationName,
                        synapseXMLLocation, properties, registryFailSafe);*/
        SynapseConfiguration synapseConfig = null;

        synapseConfig.setProperties(SynapsePropertiesLoader.loadSynapseProperties());

        // Set the Axis2 ConfigurationContext to the SynapseConfiguration
        AxisConfiguration axisConfiguration
                = ((ConfigurationContext) getContext()).getAxisConfiguration();
        synapseConfig.setAxisConfiguration(axisConfiguration);
        MessageContextCreatorForAxis2.setSynConfig(synapseConfig);

        // set the Synapse configuration into the Axis2 configuration as a parameter.
        // Every component that accesses the synapse configuration should get it from
        // the axis configuration
        Parameter synapseConfigurationParameter = new Parameter(
                SynapseConstants.SYNAPSE_CONFIG, synapseConfig);
        try {
            axisConfiguration.addParameter(synapseConfigurationParameter);
        } catch (AxisFault e) {
            log.warn("Could not set parameter '" + SynapseConstants.SYNAPSE_CONFIG
                    + "' to the Axis2 configuration : " + e.getMessage());
        }

        addServerIPAndHostEnrties(synapseConfig);

        // even though we are starting from the registry still we need to specify
        // the xml config file location, just in case for a file export of the
        // configuration
        synapseConfig.setPathToConfigFile(ServiceBusInitializer
                .getServerConfigurationInformation().getSynapseXMLLocation());
        return synapseConfig;
    }


    private void addServerIPAndHostEnrties(SynapseConfiguration configuration) {
        String hostName = ServiceBusInitializer.getServerConfigurationInformation().getHostName();
        String ipAddress = ServiceBusInitializer.getServerConfigurationInformation().getIpAddress();
        if (hostName != null && !"".equals(hostName)) {
            Entry entry = new Entry(SynapseConstants.SERVER_HOST);
            entry.setValue(hostName);
            configuration.addEntry(SynapseConstants.SERVER_HOST, entry);
        }

        if (ipAddress != null && !"".equals(ipAddress)) {
            Entry entry = new Entry(SynapseConstants.SERVER_IP);
            entry.setValue(ipAddress);
            configuration.addEntry(SynapseConstants.SERVER_IP, entry);
        }
    }

    private boolean isInitialStartup(UserRegistry registry) {
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

    private String  getParameter(String name) {
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }

        CarbonServerConfigurationService serverConf = CarbonServerConfigurationService.getInstance();
        return serverConf.getFirstProperty(name);
    }
    
    private void handleFatal(String msg) {
        log.fatal(msg);
        throw new SynapseException(msg);
    }

}
