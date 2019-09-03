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
import org.apache.synapse.Startup;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.StartupFinder;
import org.apache.synapse.startup.AbstractStartup;

import java.io.File;

public class StartupStore extends AbstractStore<Startup> {

    public StartupStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(Startup startup) {
        if (startup instanceof AbstractStartup) {
            return startup.getFileName();
        }
        return null;
    }

    protected Startup getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getStartup(name);
    }

    protected OMElement saveToFile(Startup startup, SynapseConfiguration synapseConfiguration) {
        try {
            serializer.serializeTask(startup, synapseConfiguration, null);
        } catch (Exception e) {
            handleException("Error while saving the task: " + startup.getName() + " to the " +
                    "file system", e);
        }
        return null;
    }

    protected OMElement serialize(Startup startup) {
        return StartupFinder.getInstance().serializeStartup(null, startup);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File tasksDir = new File(configPath, MultiXMLConfigurationBuilder.TASKS_DIR);
        if (!tasksDir.exists()) {
            return;
        }

        File taskFile = new File(tasksDir, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(
                taskFile.getAbsolutePath());
        taskFile.delete();
    }
}
