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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.RegistrySerializer;
import org.apache.synapse.registry.Registry;

import java.io.File;

public class SynapseRegistryStore extends AbstractStore<Registry> {

    public SynapseRegistryStore(String configPath, String configName) {
        super(configPath);
    }

    // This is slightly different case.
    // So we have to override a bunch of methods in the super class to implement the
    // case specific behavior.

    @Override
    public void save(String name, SynapseConfiguration config) {
        if (name == null) {
            log.warn("Name of the configuration item is not given");
            return;
        }

        Registry registry = getObjectToPersist(name, config);
        if (registry == null) {
            log.warn("Unable to find the Synapse registry for persistence");
            return;
        }

        try {
            if (!Boolean.valueOf(config.getProperty(
                    MultiXMLConfigurationBuilder.SEPARATE_REGISTRY_DEFINITION))) {

                serializer.serializeSynapseXML(config);
            } else {
                serializer.serializeSynapseRegistry(registry, config, null);
            }

        } catch (Exception e) {
            handleException("Error while saving the mediation registry to the file system", e);
        }

    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File rootDir = new File(configPath);
        if (!rootDir.exists()) {
            return;
        }

        File registryFile = new File(configPath, fileName);
        registryFile.delete();
    }

    protected String getFileName(Registry registry) {
        return null;
    }

    protected Registry getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getRegistry();
    }

    protected OMElement saveToFile(Registry registry, SynapseConfiguration synapseConfiguration) {
        return null;
    }

    protected OMElement serialize(Registry registry) {
        return RegistrySerializer.serializeRegistry(null, registry);
    }

}
