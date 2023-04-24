package org.wso2.micro.integrator.initializer.state.monitor;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.ProxyService;
import org.wso2.micro.integrator.coordination.RDBMSCoordinationStrategy;
import org.wso2.micro.integrator.initializer.deployment.DataHolder;

public class ArtifactClusterStateManager {
    private static final Log LOG = LogFactory.getLog(ArtifactClusterStateManager.class);
    private static SynapseConfiguration synapseConfiguration = DataHolder.getInstance().getSynapseEnvironmentService()
            .getSynapseEnvironment().getSynapseConfiguration();

    public void start() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnableTask = () -> {
            LOG.info("Running scheduled task");
            updateArtifactState();
        };
        scheduledExecutorService.scheduleAtFixedRate(runnableTask, 0, 30, TimeUnit.SECONDS);
    }

    public void updateArtifactState() {
        String currentNodeId = RDBMSCoordinationStrategy.getNodeId();
        try {
            ClusterDatabaseHandler clusterDatabaseHandler = new ClusterDatabaseHandler();
            Map<ArtifactIdentifier, ArtifactStatus> artifactStatusTableMap = clusterDatabaseHandler.getArtifactStatusTableMap(currentNodeId);
            for (ArtifactStatus artifactStatus : artifactStatusTableMap.values()) {
                switch (artifactStatus.getArtifactType()) {
                    case "PROXY_SERVICE": handleProxyState(artifactStatus); break;
                    // todo add other artifact types
                }
            }
        } catch (StateMonitorException e) {
            throw new RuntimeException(e);
        }
    }

    void handleProxyState(ArtifactStatus artifactStatus) {
        ProxyService proxyService = synapseConfiguration.getProxyService(artifactStatus.getArtifactName());
        if (proxyService != null) {
            boolean isProxyServiceRunning = proxyService.isRunning();
            boolean artifactStatusState = "ENABLE".equals(artifactStatus.getArtifactState());
            if (isProxyServiceRunning != artifactStatusState) {
                if (artifactStatusState) {
                    proxyService.start(synapseConfiguration);
                } else {
                    proxyService.stop(synapseConfiguration);
                }
            }
        }
    }
}
