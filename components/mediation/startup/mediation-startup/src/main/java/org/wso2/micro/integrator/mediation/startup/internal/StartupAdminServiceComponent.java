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
package org.wso2.micro.integrator.mediation.startup.internal;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Startup;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.apache.synapse.deployers.TaskDeployer;
import org.apache.synapse.task.service.TaskManagementService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.core.Constants;
import org.wso2.micro.integrator.initializer.ServiceBusConstants;
import org.wso2.micro.integrator.initializer.ServiceBusUtils;
import org.wso2.micro.integrator.initializer.services.SynapseEnvironmentService;
import org.wso2.micro.integrator.initializer.services.SynapseRegistrationsService;
import org.wso2.micro.integrator.mediation.startup.StartupAdminService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "UnusedDeclaration", "JavaDoc" })
@Component(name = "org.wso2.micro.integrator.mediation.startup.internal.StartupAdminServiceComponent",
        immediate = true)
public class StartupAdminServiceComponent {

    private static final Log log = LogFactory.getLog(StartupAdminServiceComponent.class);

    private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices = new HashMap<>();

    /*private TaskDescriptionRepositoryService repositoryService;*/
    private boolean initialized = false;

    @Activate
    protected void activate(ComponentContext context) throws Exception {

        try {
            initialized = true;
            SynapseEnvironmentService synEnvService = synapseEnvironmentServices
                    .get(Constants.SUPER_TENANT_ID);
            if (synEnvService != null) {
                context.getBundleContext()
                        .registerService(TaskManagementService.class.getName(), new StartupAdminService(), null);
                registerDeployer(synEnvService.getConfigurationContext().getAxisConfiguration(),
                                 synEnvService.getSynapseEnvironment());
            } else {
                log.error("Couldn't initialize the StartupManager, SynapseEnvironment service and/or "
                                  + "TaskDescriptionRepositoryService not found");
            }
        } catch (Throwable t) {
            log.error("Couldn't initialize the StartupManager, SynapseEnvironment service and/or "
                              + "TaskDescriptionRepositoryService not found");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) throws Exception {

        Set<Map.Entry<Integer, SynapseEnvironmentService>> entrySet = synapseEnvironmentServices.entrySet();
        for (Map.Entry<Integer, SynapseEnvironmentService> entry : entrySet) {
            unregistryDeployer(entry.getValue().getConfigurationContext().getAxisConfiguration(),
                               entry.getValue().getSynapseEnvironment());
        }
    }

    private void registerDeployer(AxisConfiguration axisConfig, SynapseEnvironment synEnv) {

        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        SynapseArtifactDeploymentStore deploymentStore = synEnv.getSynapseConfiguration().getArtifactDeploymentStore();
        String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(synEnv.getServerContextInformation());
        String taskDirDirPath = synapseConfigPath + File.separator + MultiXMLConfigurationBuilder.TASKS_DIR;
        for (Startup stp : synEnv.getSynapseConfiguration().getStartups()) {
            if (stp.getFileName() != null) {
                deploymentStore.addRestoredArtifact(taskDirDirPath + File.separator + stp.getFileName());
            }
        }
        synchronized (axisConfig) {
            deploymentEngine.addDeployer(new TaskDeployer(), taskDirDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
    }

    @Reference(name = "synapse.env.service",
            service = org.wso2.micro.integrator.initializer.services.SynapseEnvironmentService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSynapseEnvironmentService")
    protected void setSynapseEnvironmentService(SynapseEnvironmentService synEnvSvc) {

        synapseEnvironmentServices.put(synEnvSvc.getTenantId(), synEnvSvc);
    }

    protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {

        synapseEnvironmentServices.remove(synapseEnvironmentService.getTenantId());
    }

    @Reference(name = "synapse.registrations.service",
            service = org.wso2.micro.integrator.initializer.services.SynapseRegistrationsService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSynapseRegistrationsService")
    protected void setSynapseRegistrationsService(SynapseRegistrationsService synapseRegistrationsService) {

    }

    protected void unsetSynapseRegistrationsService(SynapseRegistrationsService synapseRegistrationsService) {

        int tenantId = synapseRegistrationsService.getTenantId();
        if (synapseEnvironmentServices.containsKey(tenantId)) {
            SynapseEnvironment env = synapseEnvironmentServices.get(tenantId).getSynapseEnvironment();
            synapseEnvironmentServices.remove(synapseRegistrationsService.getTenantId());
            AxisConfiguration axisConfig = synapseRegistrationsService.getConfigurationContext().getAxisConfiguration();
            if (axisConfig != null) {
                unregistryDeployer(axisConfig, env);
            }
        }
    }

    /**
     * Un-registers the Task Deployer.
     *
     * @param axisConfig         AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void unregistryDeployer(AxisConfiguration axisConfig, SynapseEnvironment synapseEnvironment) {

        if (axisConfig != null && synapseEnvironment != null) {
            DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
            String synapseConfigPath = ServiceBusUtils
                    .getSynapseConfigAbsPath(synapseEnvironment.getServerContextInformation());
            String proxyDirPath = synapseConfigPath + File.separator + MultiXMLConfigurationBuilder.TASKS_DIR;
            deploymentEngine.removeDeployer(proxyDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
    }
}
