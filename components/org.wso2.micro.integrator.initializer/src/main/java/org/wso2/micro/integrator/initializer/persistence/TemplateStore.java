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
import org.apache.synapse.config.xml.TemplateMediatorSerializer;
import org.apache.synapse.mediators.template.TemplateMediator;

import java.io.File;

public class TemplateStore extends AbstractStore<TemplateMediator> {

    public TemplateStore(String configPath, String configName) {
        super(configPath);
    }

    protected String getFileName(TemplateMediator template) {
        return template.getFileName();
    }

    protected TemplateMediator getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getSequenceTemplates().get(name);
    }

    protected OMElement saveToFile(TemplateMediator template, SynapseConfiguration synapseConfig) {
        try {
            return serializer.serializeTemplate(template, synapseConfig, null);
        } catch (Exception e) {
            handleException("Error while saving the template: " + template.getName() + " to the " +
                    "file system", e);
        }
        return null;
    }

    protected OMElement serialize(TemplateMediator sequence) {
        TemplateMediatorSerializer serializer = new TemplateMediatorSerializer();
        return serializer.serializeMediator(null, sequence);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfig) {
        File templateDir = new File(configPath, MultiXMLConfigurationBuilder.TEMPLATES_DIR);
        if (!templateDir.exists()) {
            return;
        }

        File templateFile = new File(templateDir, fileName);
        synapseConfig.getArtifactDeploymentStore().addBackedUpArtifact(
                templateFile.getAbsolutePath());
        templateFile.delete();
    }

}
