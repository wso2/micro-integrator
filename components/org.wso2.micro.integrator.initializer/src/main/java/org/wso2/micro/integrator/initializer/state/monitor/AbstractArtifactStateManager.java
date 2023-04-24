package org.wso2.micro.integrator.initializer.state.monitor;

import org.wso2.micro.integrator.coordination.RDBMSCoordinationStrategy;

import java.util.Map;

abstract public class AbstractArtifactStateManager {
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
    abstract void handleProxyState(ArtifactStatus artifactStatus);
}
