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
package org.wso2.micro.integrator.analytics.messageflow.data.publisher.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.store.CompletedStructureStore;
import org.apache.synapse.aspects.flow.statistics.structuring.StructuringArtifact;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.observer.TenantInformation;
import org.wso2.micro.integrator.analytics.messageflow.data.publisher.publish.ConfigurationPublisher;
import org.wso2.micro.integrator.initializer.services.SynapseEnvironmentService;

import java.util.List;

public class MediationConfigReporterThread extends Thread implements TenantInformation {

    private static Log log = LogFactory.getLog(MediationConfigReporterThread.class);

    private boolean shutdownRequested = false;
    private int tenantId = -1234;

    /**
     * The reference to the synapse environment service
     */
    private SynapseEnvironmentService synapseEnvironmentService;

    private long delay = 2 * 1000;

    private boolean isPublishingAnalyticESB = true;

    public MediationConfigReporterThread(SynapseEnvironmentService synEnvSvc) {
        this.synapseEnvironmentService = synEnvSvc;
    }

    public void run() {
        while (!shutdownRequested) {
            try {
                collectDataAndReport();
                delay();
            } catch (Throwable t) {
                log.error("Error while collecting and reporting mediation statistics", t);
            }
        }
    }

    private void collectDataAndReport() {
        if (log.isDebugEnabled()) {
            log.trace("Starting new mediation statistics collection cycle");
        }

        CompletedStructureStore completedStructureStore = synapseEnvironmentService.getSynapseEnvironment()
                .getSynapseConfiguration().getCompletedStructureStore();

        if (completedStructureStore == null) {
            if (log.isDebugEnabled()) {
                log.debug("Statistics collector is not available in the Synapse environment");
            }
            delay();
            return;
        }

        if (!isPublishingAnalyticESB) {
            //Not publishing configuration data to analytic esb
            if (!completedStructureStore.isEmpty()) {
                completedStructureStore.getCompletedStructureEntries();
            }
            return;
        }

        try {
            if (!completedStructureStore.isEmpty()) {
                List<StructuringArtifact> completedStructureList = completedStructureStore
                        .getCompletedStructureEntries();
                try {
                    updateConfigurations(completedStructureList);
                } catch (Exception e) {
                    log.error("Failed to update configuration from DAS configuration-publisher", e);
                }
            }

        } catch (Exception e) {
            log.error("Error while obtaining statistic data.", e);
        }
    }

    private void updateConfigurations(List<StructuringArtifact> completedStructureList) {
        for (StructuringArtifact structuringArtifact : completedStructureList) {
            ConfigurationPublisher.process(structuringArtifact, tenantId);
        }
    }

    private void delay() {
        if (delay <= 0) {
            return;
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignore) {

        }
    }

    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Mediation config reporter thread is being stopped");
        }
        shutdownRequested = true;
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(int i) {
        tenantId = i;
    }

    public void setPublishingAnalyticESB(boolean publishingAnalyticESB) {
        isPublishingAnalyticESB = publishingAnalyticESB;
    }
}
