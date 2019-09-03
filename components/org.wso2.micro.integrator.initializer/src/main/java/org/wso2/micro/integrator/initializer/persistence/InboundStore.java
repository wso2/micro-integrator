/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.inbound.InboundEndpointSerializer;
import org.apache.synapse.inbound.InboundEndpoint;

import java.io.File;

public class InboundStore extends AbstractStore<InboundEndpoint> {

    public InboundStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(InboundEndpoint inboundEndpoint) {
        if (inboundEndpoint.getFileName() == null) {
            inboundEndpoint.setFileName(inboundEndpoint.getName() + ".xml");
        }
        return inboundEndpoint.getFileName();
    }

    protected OMElement saveToFile(InboundEndpoint inboundEndpoint, SynapseConfiguration synapseConfiguration) {
        try {
            return serializer.serializeInboundEndpoint(inboundEndpoint, synapseConfiguration, null);
        } catch (Exception e) {
            handleException("Error while saving the inbound ep service: " + inboundEndpoint.getName() + " to " +
                    "the file system", e);
        }
        return null;
    }

    protected OMElement serialize(InboundEndpoint inboundEndpoint) {
        return InboundEndpointSerializer.serializeInboundEndpoint(inboundEndpoint);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File inboundDir = new File(configPath, MultiXMLConfigurationBuilder.INBOUND_ENDPOINT_DIR);
        if (!inboundDir.exists()) {
            return;
        }
        File inboundFile = new File(inboundDir, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(inboundFile.getAbsolutePath());
        inboundFile.delete();
    }

    protected InboundEndpoint getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getInboundEndpoint(name);
    }

}
