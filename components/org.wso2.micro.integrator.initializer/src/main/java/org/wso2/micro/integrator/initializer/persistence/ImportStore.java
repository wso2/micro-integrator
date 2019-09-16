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
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.SynapseImportSerializer;
import org.apache.synapse.libraries.imports.SynapseImport;

import java.io.File;

public class ImportStore extends AbstractStore<SynapseImport> {
    public ImportStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(SynapseImport synapseImport) {
        return synapseImport.getFileName();
    }

    protected SynapseImport getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getSynapseImports().get(name);
    }

    protected OMElement saveToFile(SynapseImport synImport, SynapseConfiguration synapseConfig) {
        try {
            return serializer.serializeImport(synImport, synapseConfig, null);
        } catch (Exception e) {
            handleException("Error while saving the API: " + synImport.getName() + " to the " +
                    "file system", e);
        }
        return null;
    }

    protected OMElement serialize(SynapseImport synImport) {
        return new SynapseImportSerializer().serializeImport(synImport);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File importsDirectory = new File(configPath, MultiXMLConfigurationBuilder.SYNAPSE_IMPORTS_DIR);
        if (!importsDirectory.exists()) {
            return;
        }
        File importFile = new File(importsDirectory, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(
                importFile.getAbsolutePath());
        importFile.delete();
    }
}
