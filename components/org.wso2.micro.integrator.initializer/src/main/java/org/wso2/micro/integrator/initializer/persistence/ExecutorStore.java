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
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.commons.executors.config.PriorityExecutorSerializer;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;

import java.io.File;

public class ExecutorStore extends AbstractStore<PriorityExecutor>{
    public ExecutorStore(String configPath, String configName) {
        super(configPath);
    }

    protected OMElement saveToFile(PriorityExecutor executor, SynapseConfiguration synapseConfig) {
        try {
            return serializer.serializeExecutor(executor, synapseConfig, null);
        } catch (Exception e) {
            handleException("Error while saving the executor: " + executor.getName() + " to the " +
                    "file system", e);
        }

        return null;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File executorsDir = new File(configPath, MultiXMLConfigurationBuilder.EXECUTORS_DIR);
        if (!executorsDir.exists()) {
            return;
        }
        File executorFile = new File(executorsDir, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(
                executorFile.getAbsolutePath());
        executorFile.delete();
    }

    protected PriorityExecutor getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getPriorityExecutors().get(name);
    }

    protected String getFileName(PriorityExecutor executor) {
        return executor.getFileName();
    }

    protected OMElement serialize(PriorityExecutor executor) {
        return PriorityExecutorSerializer.serialize(null, executor,
                SynapseConstants.SYNAPSE_NAMESPACE);
    }
}
