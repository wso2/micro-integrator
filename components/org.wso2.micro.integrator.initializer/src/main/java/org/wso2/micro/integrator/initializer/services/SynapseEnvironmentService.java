/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.initializer.services;

import org.apache.synapse.core.SynapseEnvironment;
import org.apache.axis2.context.ConfigurationContext;

/**
 * All the ESB Components should use this OSGI service for accessing the SynapseEnvironment.
 * This service is populated at the System start up with ESB Super tenent confiruations. The service 
 * is populated again for each tenant creation. So a component wishing to know about the
 * tenant creation events should listen for this service.
 */
public interface SynapseEnvironmentService {
    /**
     * Get the tenant id corresponding to this synapse configuration.
     *
      * @return integer value of the tenant id
     */
    int getTenantId();

    /**
     * Retrieve the synapse configuration
     *
     * @return synapse configuration
     */
    SynapseEnvironment getSynapseEnvironment();

    /**
     * Set the SynapseEnvironment
     *
     * @param synapseEnvironment SynapseEnvrionment
     */
    public void setSynapseEnvironment(SynapseEnvironment synapseEnvironment);

    /**
     * Get the ConfigurationContext for this SynapseConfiguration
     *
     * @return the ConfigurationContext
     */
    public ConfigurationContext getConfigurationContext();
}
