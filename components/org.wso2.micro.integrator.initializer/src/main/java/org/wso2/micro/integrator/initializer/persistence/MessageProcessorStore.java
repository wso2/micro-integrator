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
import org.apache.synapse.config.xml.MessageProcessorSerializer;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.message.processor.MessageProcessor;

import java.io.File;

public class MessageProcessorStore extends AbstractStore<MessageProcessor> {


    public MessageProcessorStore(String configPath, String configName) {
        super(configPath);
    }

    @Override
    protected OMElement saveToFile(MessageProcessor processor, SynapseConfiguration synapseConfig) {
        try {
            return serializer.serializeMessageProcessor(processor, synapseConfig, null);
        } catch (Exception e) {
            handleException("Error while saving the Message Processr: " + processor.getName() + " to " +
                    "the file system", e);
        }
        return null;
    }

    @Override
    protected void deleteFile(String fileName, SynapseConfiguration config) {
        File mpDir = new File(configPath, MultiXMLConfigurationBuilder.MESSAGE_PROCESSOR_DIR);

        if (!mpDir.exists()) {
            return;
        }

        File mpFile = new File(mpDir, fileName);

        config.getArtifactDeploymentStore().addBackedUpArtifact(
                mpFile.getAbsolutePath());
        mpFile.delete();
    }

    @Override
    protected MessageProcessor getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getMessageProcessors().get(name);
    }

    @Override
    protected String getFileName(MessageProcessor processor) {
        return processor.getFileName();
    }

    @Override
    protected OMElement serialize(MessageProcessor obj) {
        return MessageProcessorSerializer.serializeMessageProcessor(null, obj);
    }
}
