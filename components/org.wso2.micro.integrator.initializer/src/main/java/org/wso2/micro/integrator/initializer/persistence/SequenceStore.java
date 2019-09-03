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
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.mediators.base.SequenceMediator;

import java.io.File;

public class SequenceStore extends AbstractStore<SequenceMediator> {

    public SequenceStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(SequenceMediator sequence) {
        return sequence.getFileName();
    }

    protected SequenceMediator getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getDefinedSequences().get(name);
    }

    protected OMElement saveToFile(SequenceMediator sequence, SynapseConfiguration synapseConfig) {
        try {
            return serializer.serializeSequence(sequence, synapseConfig, null);
        } catch (Exception e) {
            handleException("Error while saving the sequence: " + sequence.getName() + " to the " +
                    "file system", e);
        }
        return null;
    }

    protected OMElement serialize(SequenceMediator sequence) {
        SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
        return serializer.serializeMediator(null, sequence);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfig) {
        File sequenceDir = new File(configPath, MultiXMLConfigurationBuilder.SEQUENCES_DIR);
        if (!sequenceDir.exists()) {
            return;
        }

        File sequenceFile = new File(sequenceDir, fileName);
        synapseConfig.getArtifactDeploymentStore().addBackedUpArtifact(
                sequenceFile.getAbsolutePath());
        sequenceFile.delete();
    }
}
