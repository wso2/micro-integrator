package org.wso2.micro.integrator.initializer.state.monitor;

public class ArtifactStatus extends ArtifactIdentifier {
    private String artifactState;
    private String nodeId;

    ArtifactStatus(String artifactName, String artifactType, String artifactStatus, String nodeId) {
        this.setArtifactName(artifactName);
        this.setArtifactType(artifactType);
        this.artifactState = artifactStatus;
        this.nodeId = nodeId;
    }

    public String getArtifactState() {
        return artifactState;
    }

    public void setArtifactState(String artifactState) {
        this.artifactState = artifactState;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
