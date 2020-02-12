/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.initializer.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.integrator.dataservices.core.DBDeployer;
import org.wso2.micro.integrator.ndatasource.capp.deployer.DataSourceCappDeployer;
import org.wso2.micro.integrator.initializer.deployment.synapse.deployer.FileRegistryResourceDeployer;
import org.wso2.micro.integrator.initializer.StartupFinalizer;
import org.wso2.micro.integrator.initializer.services.SynapseEnvironmentService;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.application.deployer.handler.DefaultAppDeployer;
import org.wso2.micro.integrator.initializer.deployment.application.deployer.CAppDeploymentManager;
import org.wso2.micro.integrator.initializer.deployment.artifact.deployer.ArtifactDeploymentManager;
import org.wso2.micro.integrator.initializer.deployment.synapse.deployer.SynapseAppDeployer;
import org.wso2.micro.integrator.initializer.utils.ConfigurationHolder;

import java.io.File;

@Component(name = "org.wso2.micro.integrator.initializer.deployment.AppDeployerServiceComponent", immediate = true)
public class AppDeployerServiceComponent {

    private static final Log log = LogFactory.getLog(AppDeployerServiceComponent.class);

    private ConfigurationContext configCtx;
    private SynapseEnvironmentService synapseEnvironmentService;
    private StartupFinalizer startupFinalizer;

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug(AppDeployerServiceComponent.class.getName() + "#activate() BEGIN - " + System.currentTimeMillis());
            log.debug("Activating AppDeployerServiceComponent");
        }

        // ConfigurationHolder is updated by org.wso2.micro.integrator.initializer.ServiceBusInitializer
        configCtx = ConfigurationHolder.getInstance().getAxis2ConfigurationContextService().getServerConfigContext();

        // Initialize deployers
        ArtifactDeploymentManager artifactDeploymentManager = new ArtifactDeploymentManager(configCtx.getAxisConfiguration());
        CAppDeploymentManager cAppDeploymentManager = CAppDeploymentManager.getInstance();
        cAppDeploymentManager.init(configCtx.getAxisConfiguration());

        initializeDeployers(artifactDeploymentManager, cAppDeploymentManager);

        // Register eventSink deployer
        registerEventSinkDeployer(artifactDeploymentManager);

        // Deploy artifacts
        artifactDeploymentManager.deploy();

        // Finalize server startup
        startupFinalizer = new StartupFinalizer(configCtx, ctxt.getBundleContext());
        startupFinalizer.finalizeStartup();

        if (log.isDebugEnabled()) {
            log.debug(AppDeployerServiceComponent.class.getName() + "#activate() COMPLETE - " + System.currentTimeMillis());
        }

    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Deactivating AppDeployerServiceComponent");
        startupFinalizer.cleanup();
    }

    // TODO :- uncomment when satisfied
  /*  @Reference(
            name = "ntask.service",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    protected void unsetTaskService(TaskService taskService) {
    }*/

    /**
     * Receive an event about the creation of a SynapseEnvironment. If this is
     * SuperTenant we have to wait until all the other constraints are met and actual
     * initialization is done in the activate method. Otherwise we have to do the activation here.
     *
     * @param synapseEnvironmentService SynapseEnvironmentService which contains information
     *                                  about the new Synapse Instance
     */
    @Reference(
            name = "synapse.env.service",
            service = org.wso2.micro.integrator.initializer.services.SynapseEnvironmentService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSynapseEnvironmentService")
    protected void setSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
        this.synapseEnvironmentService = synapseEnvironmentService;
        DataHolder.getInstance().setSynapseEnvironmentService(this.synapseEnvironmentService);
    }

    /**
     * Receive an event about Destroying a SynapseEnvironment. This can be the super tenant
     * destruction or a tenant destruction.
     *
     * @param synapseEnvironmentService synapseEnvironment
     */
    protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
        this.synapseEnvironmentService = null;
    }

    /**
     * Function to initialize deployer
     *
     * @param artifactDeploymentManager
     * @param cAppDeploymentManager
     */
    private void initializeDeployers(ArtifactDeploymentManager artifactDeploymentManager,
                                     CAppDeploymentManager cAppDeploymentManager) {

        String artifactRepoPath = configCtx.getAxisConfiguration().getRepository().getPath();

        log.debug("Initializing ArtifactDeploymentManager deployment manager");

        // TODO :- unComment after installing DSS
        // Create data services deployer
        DBDeployer dbDeployer = new DBDeployer();
        dbDeployer.setDirectory(artifactRepoPath + DeploymentConstants.DSS_DIR_NAME);
        dbDeployer.setExtension(DeploymentConstants.DSS_TYPE_EXTENSION);

        // Register artifact deployers in ArtifactDeploymentManager
        try {
            artifactDeploymentManager.registerDeployer(artifactRepoPath + DeploymentConstants.DSS_DIR_NAME, dbDeployer);
        } catch (DeploymentException e) {
            log.error("Error occurred while registering data services deployer");
        }

        // Initialize micro integrator carbon application deployer
        log.debug("Initializing carbon application deployment manager");

        // Register deployers in DeploymentEngine (required for CApp deployment)
        DeploymentEngine deploymentEngine = (DeploymentEngine) configCtx.getAxisConfiguration().getConfigurator();
        deploymentEngine.addDeployer(dbDeployer, DeploymentConstants.DSS_DIR_NAME, DeploymentConstants.DSS_TYPE_DBS);

        // Register application deployment handlers
        //TODO
       cAppDeploymentManager.registerDeploymentHandler(new FileRegistryResourceDeployer(
                synapseEnvironmentService.getSynapseEnvironment().getSynapseConfiguration().getRegistry()));
        cAppDeploymentManager.registerDeploymentHandler(new DataSourceCappDeployer());
        cAppDeploymentManager.registerDeploymentHandler(new DefaultAppDeployer());
        cAppDeploymentManager.registerDeploymentHandler(new SynapseAppDeployer());

    }

    /**
     * Function to register eventSink deployer.
     *
     * @param artifactDeploymentManager artifactDeploymentManager which manages and performs artifact deployment
     */
    public void registerEventSinkDeployer(ArtifactDeploymentManager artifactDeploymentManager) {
        try {
            String carbonRepoPath = configCtx.getAxisConfiguration().getRepository().getPath();
            String eventSinkPath = carbonRepoPath + File.separator + "event-sinks";
            Class deployerClass = Class.forName("org.wso2.micro.integrator.event.sink.EventSinkDeployer");
            Deployer deployer = (Deployer) deployerClass.newInstance();
            artifactDeploymentManager.registerDeployer(eventSinkPath, deployer);
            if (log.isDebugEnabled()) {
                log.debug("Successfully registered eventSink deployer");
            }
        } catch (ClassNotFoundException e) {
            log.error("Can not find class EventSinkDeployer", e);
        } catch (InstantiationException e) {
            log.error("Error instantiating EventSinkDeployer class", e);
        } catch (DeploymentException e) {
            log.error("Error registering eventSink deployer", e);
        } catch (IllegalAccessException e) {
            log.error("Error instantiating EventSinkDeployer class", e);
        }
    }
}
