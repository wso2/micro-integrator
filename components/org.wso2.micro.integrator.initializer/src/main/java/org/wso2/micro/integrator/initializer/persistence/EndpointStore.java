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

package org.wso2.micro.integrator.initializer.persistence;

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.axiom.om.OMElement;

import java.io.File;

public class EndpointStore extends AbstractStore<Endpoint> {

    public EndpointStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(Endpoint endpoint) {
        if (endpoint instanceof AbstractEndpoint) {
            return endpoint.getFileName();
        }
        return null;
    }

    protected Endpoint getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getDefinedEndpoints().get(name);
    }

    protected OMElement saveToFile(Endpoint endpoint, SynapseConfiguration synapseConfig) {
        try {
            return serializer.serializeEndpoint(endpoint, synapseConfig, null);
        } catch (Exception e) {
            handleException("Error while saving the endpoint: " + endpoint.getName() + " to the " +
                    "file system", e);
        }
        return null;
    }

    protected OMElement serialize(Endpoint endpoint) {
        return EndpointSerializer.getElementFromEndpoint(endpoint);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File endpointsDir = new File(configPath, MultiXMLConfigurationBuilder.ENDPOINTS_DIR);
        if (!endpointsDir.exists()) {
            return;
        }
        File endpointFile = new File(endpointsDir, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(
                endpointFile.getAbsolutePath());
        endpointFile.delete();
    }
}
