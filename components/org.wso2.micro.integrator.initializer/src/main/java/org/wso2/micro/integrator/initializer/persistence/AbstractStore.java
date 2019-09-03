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

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;

/**
 * The abstract class which can be used to access a persistence storage of a
 * specific type of mediation configuration elements. Implementations of this
 * abstract class should ideally be parameterized to prevent it from handling
 * more than one type of objects. This abstract class provides supports basic
 * persistence activities of save and delete. Registry persistence activities
 * can also be handled by this implementation if required.
 *
 * @param <T>
 */
public abstract class AbstractStore<T> {

    protected Log log = LogFactory.getLog(this.getClass());

    protected String configPath;
    protected MultiXMLConfigurationSerializer serializer;

    public AbstractStore(String configPath) {
        this.configPath = configPath;
        serializer = new MultiXMLConfigurationSerializer(configPath);
    }

    public void save(String name, SynapseConfiguration config) {
        if (name == null) {
            log.warn("Name of the configuration item to be saved is not given");
            return;
        }

        T obj = getObjectToPersist(name, config);
        if (obj == null) {
            log.warn("Unable to find the item: " + name + " for persistence");
            return;
        }

        String fileName = getFileName(obj);
        if (fileName == null) {
            // This is an element declared in the top level synapse.xml file
            // So we need to rewrite the entire synapse.xml file
            try {
                serializer.serializeSynapseXML(config);
            } catch (Exception e) {
                handleException("Error while saving the mediation configuration to " +
                        "the file system", e);
            }
        } else {
            // This is an element declared in its own file
            // Just write the content to the file
            saveToFile(obj, config);
        }

    }

    public void delete(String name, String fileName, SynapseConfiguration config) {
        if (fileName == null) {
            // This is an element declared in the top level synapse.xml file
            // So we need to rewrite the entire synapse.xml file
            try {
                serializer.serializeSynapseXML(config);
            } catch (Exception e) {
                handleException("Error while saving the mediation configuration to " +
                        "the file system", e);
            }
        } else {
            // This is an element declared in its own file
            // Just delete the file
            deleteFile(fileName, config);
        }

    }

    /**
     * Serialize and save the given object to the local file system
     *
     * @param obj Object to be saved
     * @param synapseConfig synapse configuration
     * @return The OMElement generated as a result of the serialization or null
     */
    protected abstract OMElement saveToFile(T obj, SynapseConfiguration synapseConfig);

    /**
     * Delete the specified file from the persistence store. File name is
     * provided as an argument and it is up to the implementation to the
     * calculate the exact file path using the file name and other available
     * data.
     *
     * @param fileName Name of the file to be deleted
     * @param config synapse configuration
     */
    protected abstract void deleteFile(String fileName, SynapseConfiguration config);

    /**
     * Find the specified object from the SynaspeConfiguration to be saved to
     * the persistence store
     *
     * @param name Name or unique ID of the object
     * @param config Current SynapseConfiguration
     * @return The named object or null if such an object doesn't exist
     */
    protected abstract T getObjectToPersist(String name, SynapseConfiguration config);

    /**
     * Inspect the given object and find the file name to which it should be
     * serialized to
     *
     * @param obj The object to be inspected
     * @return A file name as a string or null if no file name is specified
     */
    protected abstract String getFileName(T obj);

    /**
     * Serialize the given object into XML
     *
     * @param obj The object to be serialized
     * @return The resulting OMElement
     */
    protected abstract OMElement serialize(T obj);

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new ServiceBusPersistenceException(msg, e);
    }

}
