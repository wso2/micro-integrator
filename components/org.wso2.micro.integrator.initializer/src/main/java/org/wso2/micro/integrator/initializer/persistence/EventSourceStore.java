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
import org.apache.synapse.config.xml.eventing.EventSourceSerializer;
import org.apache.synapse.eventing.SynapseEventSource;

import java.io.File;

public class EventSourceStore extends AbstractStore<SynapseEventSource> {

    public EventSourceStore(String configPath, String configName) {
        super(configPath);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File eventsDir = new File(configPath, MultiXMLConfigurationBuilder.EVENTS_DIR);
        if (!eventsDir.exists()) {
            return;
        }
        File eventFile = new File(eventsDir, fileName);
        synapseConfiguration.getArtifactDeploymentStore().addBackedUpArtifact(
                eventFile.getAbsolutePath());
        eventFile.delete();
    }

    protected String getFileName(SynapseEventSource eventSrc) {
        return eventSrc.getFileName();
    }

    protected SynapseEventSource getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getEventSource(name);
    }

    protected OMElement saveToFile(SynapseEventSource eventSrc,
                                   SynapseConfiguration synapseConfiguration) {
        try {
            return serializer.serializeEventSource(eventSrc, synapseConfiguration, null);
        } catch (Exception e) {
            handleException("Error while saving the event source: " + eventSrc.getName() + " to " +
                    "the file system", e);
        }
        return null;
    }

    protected OMElement serialize(SynapseEventSource eventSrc) {
        return EventSourceSerializer.serializeEventSource(null, eventSrc);
    }
}
