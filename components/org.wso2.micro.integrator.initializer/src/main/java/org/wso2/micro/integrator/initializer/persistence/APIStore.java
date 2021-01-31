/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.micro.integrator.initializer.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.api.API;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.rest.APISerializer;

import java.io.File;

public class APIStore extends AbstractStore<API> {

    public APIStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(API api) {
        return api.getFileName();
    }

    protected API getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getAPI(name);
    }

    protected OMElement saveToFile(API api, SynapseConfiguration synapseConfig) {
        try {
            return serializer.serializeAPI(api, synapseConfig, null);
        } catch (Exception e) {
            handleException("Error while saving the API: " + api.getName() + " to the " +
                    "file system", e);
        }
        return null;
    }

    protected OMElement serialize(API api) {
        return APISerializer.serializeAPI(api);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File apiDirectory = new File(configPath, MultiXMLConfigurationBuilder.REST_API_DIR);
        if (!apiDirectory.exists()) {
            return;
        }
        File apiFile = new File(apiDirectory, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(
                apiFile.getAbsolutePath());
        apiFile.delete();
    }
}
