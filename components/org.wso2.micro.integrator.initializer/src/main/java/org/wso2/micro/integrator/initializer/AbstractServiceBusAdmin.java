/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.micro.core.AbstractAdmin;
import org.wso2.micro.integrator.initializer.persistence.MediationPersistenceManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
@SuppressWarnings({"UnusedDeclaration"})
public class AbstractServiceBusAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(AbstractServiceBusAdmin.class);

    /**
     * Setting the tenant id for the local thread CarbonContextHolder
     *
     *
     */
   // protected AbstractServiceBusAdmin() {
     //   CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
       //         getCurrentCarbonContextHolder().getTenantId());
    //}

    /**
     * Helper method to retrieve the Synapse configuration from the relevant axis configuration
     *
     * @return extracted SynapseConfiguration from the relevant AxisConfiguration
     */
    protected SynapseConfiguration getSynapseConfiguration() {
        return (SynapseConfiguration) getAxisConfig().getParameter(
                SynapseConstants.SYNAPSE_CONFIG).getValue();
    }

    /**
     * Helper method to retrieve the Synapse environment from the relevant axis configuration
     *
     * @return extracted SynapseEnvironment from the relevant AxisConfiguration
     */
    protected SynapseEnvironment getSynapseEnvironment() {
        return getSynapseEnvironment(getAxisConfig());
    }

    protected SynapseEnvironment getSynapseEnvironment(AxisConfiguration axisCfg) {
        return (SynapseEnvironment) axisCfg.getParameter(
                SynapseConstants.SYNAPSE_ENV).getValue();
    }

    /**
     * Helper method to get the persistence manger
     * @return persistence manager for this configuration context
     */
    protected MediationPersistenceManager getMediationPersistenceManager() {
        return ServiceBusUtils.getMediationPersistenceManager(getAxisConfig());
    }

    protected ServerConfigurationInformation getServerConfigurationInformation() {
        Parameter p = getAxisConfig().getParameter(SynapseConstants.SYNAPSE_SERVER_CONFIG_INFO);
        if (p != null) {
            return (ServerConfigurationInformation) p.getValue();
        }
        return null;
    }

    protected ServerContextInformation getServerContextInformation() {
        Parameter p = getAxisConfig().getParameter(SynapseConstants.SYNAPSE_SERVER_CTX_INFO);
        if (p != null) {
            return (ServerContextInformation) p.getValue();
        }
        return null;
    }

    protected Lock getLock() {
        Parameter p = getAxisConfig().getParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
        if (p != null) {
            return (Lock) p.getValue();
        } else {
            log.warn(ServiceBusConstants.SYNAPSE_CONFIG_LOCK + " is null, Recreating a new lock");
            Lock lock = new ReentrantLock();
            try {
                getAxisConfig().addParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK, lock);
                return lock;
            } catch (AxisFault axisFault) {
                log.error("Error while setting " + ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
            }
        }

        return null;
    }

    protected void lockSynapseConfiguration() {
        final Lock lock = getLock();
        if (lock != null) {
            lock.lock();
        }
    }

    protected void unlockSynapseConfiguration() {
        final Lock lock = getLock();
        if (lock != null) {
            lock.unlock();
        }
    }
}
