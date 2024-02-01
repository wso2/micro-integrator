package org.wso2.micro.integrator.initializer.state.monitor;

public class ArtifactIdentifier {
    private String artifactName;
    private String artifactType;

    public ArtifactIdentifier() {
    }

    public ArtifactIdentifier(String artifactName, String artifactType) {
        this.artifactName = artifactName;
        this.artifactType = artifactType;
    }
    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }
}
