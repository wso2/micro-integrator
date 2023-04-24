package org.wso2.micro.integrator.initializer.state.monitor;

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.ProxyService;
import org.wso2.micro.integrator.initializer.deployment.DataHolder;

public class ArtifactStartupStateManager extends AbstractArtifactStateManager {

    SynapseConfiguration synapseConfiguration;
    public ArtifactStartupStateManager() {
        super();
    }
    @Override
    void handleProxyState(ArtifactStatus artifactStatus) {
        synapseConfiguration = DataHolder.getInstance().getSynapseEnvironmentService()
                .getSynapseEnvironment().getSynapseConfiguration();
        ProxyService proxyService = synapseConfiguration.getProxyService(artifactStatus.getArtifactName());
        if (proxyService != null) {
            boolean isProxyServiceRunning = proxyService.isRunning();
            boolean artifactStatusState = "ENABLE".equals(artifactStatus.getArtifactState());
            if (isProxyServiceRunning != artifactStatusState) {
                if (artifactStatusState) {
                    proxyService.setStartOnLoad(true);
                } else {
                    proxyService.setStartOnLoad(false);
                }
            }
        }
    }
}
