/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.event.sink;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.event.sink.internal.EventSinkStore;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Axis2 deployer which deploys event sinks.
 */
public class EventSinkDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(EventSinkDeployer.class);

    @Override
    public void init(ConfigurationContext configurationContext) {
    }

    /**
     * Deploys event sink specified by deploymentFileData. This is called whenever an event sink artifact
     * is added, modified, deleted from the event sink artifact directory
     *
     * @param deploymentFileData File data about the event sink artifact to be deployed
     * @throws DeploymentException
     */
    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(deploymentFileData.getAbsolutePath())));
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement eventSink = builder.getDocumentElement();

            String eventSinkName = FilenameUtils.getBaseName(deploymentFileData.getFile().getName());
            EventSinkStore.getInstance().addEventSink(EventSink.createEventSink(eventSink, eventSinkName));
            log.info("Deploying event sink: " + eventSinkName + " - file: " + deploymentFileData.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new DeploymentException(
                    "Deployment artifact file \"" + deploymentFileData.getAbsolutePath() + "\" not found", e);
        } catch (XMLStreamException e) {
            throw new DeploymentException(
                    "Event sink XML in \"" + deploymentFileData.getAbsolutePath() + "\" is malformed", e);
        } catch (EventSinkException e) {
            throw new DeploymentException(
                    "Event sink configuration in \"" + deploymentFileData.getAbsolutePath() + "\" is invalid", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Failed to close file input stream after deploying event sink from file "
                            + deploymentFileData.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void setDirectory(String s) {
    }

    @Override
    public void setExtension(String s) {
    }

    /**
     * Undeploys previously deployed event sink artifact. This is called whenever an event sink artifact is deleted
     * from event sink artifact directory. Also, this is called when an already existing artifact is modified, followed
     * by a call to deploy()
     *
     * @param fileName File name of the artifact to be undeployed
     * @throws DeploymentException
     */
    @Override
    public void undeploy(String fileName) throws DeploymentException {
        String eventSinkName = FilenameUtils.getBaseName(fileName);
        try {
            EventSink.stopDataPublisher(EventSinkStore.getInstance().getEventSink(eventSinkName));
        } catch (EventSinkException e) {
            throw new DeploymentException("Error un-deploying event sink " + eventSinkName, e);
        }
        EventSinkStore.getInstance().removeEventSink(eventSinkName);
        log.info("Event sink named '" + eventSinkName + "' has been undeployed");
    }

    @Override
    public void cleanup() throws DeploymentException {
    }
}
