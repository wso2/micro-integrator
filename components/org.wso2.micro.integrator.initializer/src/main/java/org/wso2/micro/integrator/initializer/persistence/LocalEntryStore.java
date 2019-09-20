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
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.EntrySerializer;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;

import java.io.File;

public class LocalEntryStore extends AbstractStore<Entry> {

    public LocalEntryStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(Entry entry) {
        return entry.getFileName();
    }

    protected Entry getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getDefinedEntries().get(name);
    }

    protected OMElement saveToFile(Entry entry, SynapseConfiguration synapseConfiguration) {
        try {
            return serializer.serializeLocalEntry(entry, synapseConfiguration, null);
        } catch (Exception e) {
            handleException("Error while saving the local entry: " + entry.getKey() + " to the " +
                    "file system", e);
        }
        return null;
    }

    protected OMElement serialize(Entry entry) {
        return EntrySerializer.serializeEntry(entry, null);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File entriesDir = new File(configPath, MultiXMLConfigurationBuilder.LOCAL_ENTRY_DIR);
        if (!entriesDir.exists()) {
            return;
        }
        File entryFile = new File(entriesDir, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(
                entryFile.getAbsolutePath());
        entryFile.delete();
    }
}
